package de.joernott.c0denet.http;

import de.joernott.c0de.core.library.NativeCall;
import de.joernott.c0de.core.library.NativeResult;
import de.joernott.c0de.core.value.StringValue;
import de.joernott.c0de.core.value.StructValue;
import de.joernott.c0de.library.ProcessHost;
import de.joernott.c0denet.core.config.NetConfig;
import de.joernott.c0denet.core.http.HttpMethod;
import de.joernott.c0denet.core.http.NetError;
import de.joernott.c0denet.core.http.NetException;
import de.joernott.c0denet.core.http.NetRequest;
import de.joernott.c0denet.core.http.NetResponse;
import de.joernott.c0denet.library.HttpLibrary;
import java.io.ByteArrayOutputStream;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import net.minecraft.server.MinecraftServer;

/**
 * The requests of the processes on the server (docs/concept.md#waiting-for-the-answer). A function sends its request
 * to the client of the player who runs the process and returns {@link NativeResult#waitForValue()}: the cursor
 * calls it again in every step until the answer, an error or the timeout ends the call.
 *
 * <p>Only used on the server thread.
 */
public final class PendingRequests {
    /** The largest request in bytes: a packet to a client may have 1 MiB. */
    public static final int MAX_REQUEST_SIZE = 1_000_000;

    /** The largest part of an answer: a packet to the server may have 32 KiB. */
    public static final int PART_SIZE = 30_000;

    /** Bytes of an answer besides its body: status and headers. */
    private static final int HEADER_ALLOWANCE = 64 * 1024;

    /** A request whose process didn't call its function for this many ticks is cancelled (the process ended). */
    static final int STALE_TICKS = 100;

    /** Seconds the server waits for an answer after the timeout of the request, for the way back. */
    private static final int GRACE_SECONDS = 5;

    private static ClientChannel channel;
    private static NetConfig config = NetConfig.defaults(NetConfig.SERVER);

    /** The pending request of every process, by the process (its {@code ProcessContext}). */
    private static final Map<Object, Pending> BY_PROCESS = new IdentityHashMap<>();

    private static final Map<Long, Pending> BY_ID = new HashMap<>();
    private static MinecraftServer server;
    private static long nextId = 1;

    private PendingRequests() {}

    /** A request of a process. */
    private static final class Pending {
        final long id;
        final Object process;
        final UUID player;
        final NetRequest request;
        boolean sent;
        long deadline;
        int lastCalled;
        int nextPart;
        final ByteArrayOutputStream answer = new ByteArrayOutputStream();
        NetResponse response;
        NetException failure;

        Pending(long id, Object process, UUID player, NetRequest request) {
            this.id = id;
            this.process = process;
            this.player = player;
            this.request = request;
        }

        boolean done() {
            return response != null || failure != null;
        }
    }

    /** Sets the way to the clients. Called when the mod is initialized, and by GameTests. */
    public static void channel(ClientChannel newChannel) {
        channel = newChannel;
    }

    /** The way to the clients. */
    public static ClientChannel channel() {
        return channel;
    }

    /** Sets the server settings. */
    public static void config(NetConfig newConfig) {
        config = newConfig;
    }

    /** The function of an HTTP method: sends the request, then waits for the answer. */
    public static NativeResult call(NativeCall call, HttpMethod method) {
        if (!(call.process().host() instanceof ProcessHost host)) {
            return NativeResult.error(NetError.INVALID_ARGUMENT.code(), HttpLibrary.NAME + " only works in the game");
        }
        forget(host.server());
        int tick = host.server().getTickCount();
        Pending pending = BY_PROCESS.get(call.process());
        if (pending == null) {
            UUID player = host.activator() != null ? host.activator() : host.owner();
            if (player == null) {
                return error(new NetException(
                        NetError.CLIENT_UNAVAILABLE,
                        "Nobody runs the process: it was started without a player and its trigger block has no owner"));
            }
            NetRequest request;
            try {
                request = NetRequest.of(
                        HttpLibrary.connection((StructValue) call.argument("connection")),
                        method,
                        ((StringValue) call.argument("endpoint")).value(),
                        method.sendsData() ? ((StringValue) call.argument("data")).value() : "");
            } catch (NetException e) {
                return error(e);
            }
            if (request.toBytes().length > MAX_REQUEST_SIZE) {
                return error(new NetException(
                        NetError.INVALID_ARGUMENT, "The request is larger than " + MAX_REQUEST_SIZE + " bytes"));
            }
            pending = new Pending(nextId++, call.process(), player, request);
            BY_PROCESS.put(call.process(), pending);
            BY_ID.put(pending.id, pending);
        }
        pending.lastCalled = tick;
        if (pending.done()) {
            remove(pending);
            return pending.failure != null ? error(pending.failure) : result(pending.response);
        }
        Optional<String> unavailable = channel.unavailable(host.server(), pending.player);
        if (unavailable.isPresent()) {
            cancel(host.server(), pending);
            return error(new NetException(NetError.CLIENT_UNAVAILABLE, unavailable.get()));
        }
        if (!pending.sent) {
            if (running(pending.player) < config.get(NetConfig.MAX_REQUESTS_PER_PLAYER)) {
                pending.sent = true;
                pending.deadline =
                        System.currentTimeMillis() + (pending.request.timeout() + GRACE_SECONDS) * 1000L;
                channel.send(
                        host.server(),
                        pending.player,
                        pending.id,
                        pending.request,
                        config.get(NetConfig.MAX_RESPONSE_SIZE));
            }
        } else if (System.currentTimeMillis() > pending.deadline) {
            cancel(host.server(), pending);
            return error(new NetException(
                    NetError.REQUEST_TIMEOUT,
                    "No answer from " + pending.request.uri().getHost() + " within " + pending.request.timeout()
                            + " seconds"));
        }
        return NativeResult.waitForValue();
    }

    /**
     * A part of the answer to a request arrived from the client of a player.
     *
     * @param index the number of the part, from 0
     * @param total the number of parts
     */
    public static void answered(UUID player, long id, int index, int total, byte[] part) {
        Pending pending = BY_ID.get(id);
        if (pending == null || !pending.player.equals(player) || pending.done()) {
            return;
        }
        if (index != pending.nextPart || index >= total) {
            pending.failure = new NetException(NetError.REQUEST_FAILED, "The client sent the answer in a wrong order");
            return;
        }
        if (pending.answer.size() + part.length > config.get(NetConfig.MAX_RESPONSE_SIZE) + HEADER_ALLOWANCE) {
            pending.failure = new NetException(
                    NetError.RESPONSE_TOO_LARGE,
                    "The answer is larger than " + config.get(NetConfig.MAX_RESPONSE_SIZE) + " bytes");
            channel.cancel(server, player, id);
            return;
        }
        pending.answer.write(part, 0, part.length);
        pending.nextPart++;
        if (index == total - 1) {
            try {
                pending.response = NetResponse.fromBytes(pending.answer.toByteArray());
            } catch (NetException e) {
                pending.failure = e;
            }
        }
    }

    /** The client of a player reports that a request failed. */
    public static void failed(UUID player, long id, int code, String message) {
        Pending pending = BY_ID.get(id);
        if (pending == null || !pending.player.equals(player) || pending.done()) {
            return;
        }
        NetError error = NetError.of(code);
        pending.failure = new NetException(error == null ? NetError.REQUEST_FAILED : error, message);
    }

    /**
     * Cancels the requests of processes that didn't call their function for {@link #STALE_TICKS} ticks: their
     * processes ended or were aborted. Called at the end of every server tick.
     */
    public static void tick(MinecraftServer current) {
        forget(current);
        int tick = current.getTickCount();
        for (Iterator<Pending> it = BY_ID.values().iterator(); it.hasNext(); ) {
            Pending pending = it.next();
            if (tick - pending.lastCalled > STALE_TICKS) {
                it.remove();
                BY_PROCESS.remove(pending.process);
                if (pending.sent && !pending.done()) {
                    channel.cancel(current, pending.player, pending.id);
                }
            }
        }
    }

    /** The number of requests that are running on the client of a player. */
    private static int running(UUID player) {
        int count = 0;
        for (Pending pending : BY_ID.values()) {
            if (pending.sent && !pending.done() && pending.player.equals(player)) {
                count++;
            }
        }
        return count;
    }

    /** Forgets the requests of an earlier server, e.g. another single-player world. */
    private static void forget(MinecraftServer current) {
        if (server != current) {
            BY_ID.clear();
            BY_PROCESS.clear();
            server = current;
        }
    }

    private static void cancel(MinecraftServer current, Pending pending) {
        remove(pending);
        if (pending.sent && !pending.done()) {
            channel.cancel(current, pending.player, pending.id);
        }
    }

    private static void remove(Pending pending) {
        BY_ID.remove(pending.id);
        BY_PROCESS.remove(pending.process);
    }

    /** The results of an answer; with HTTP_STATUS for a status of 400 or higher, whose results are delivered too. */
    private static NativeResult result(NetResponse response) {
        if (response.isError()) {
            return new NativeResult(
                    HttpLibrary.results(response), NetError.HTTP_STATUS.code(), response.statusLine(), 0, false);
        }
        return NativeResult.of(HttpLibrary.results(response));
    }

    private static NativeResult error(NetException e) {
        return NativeResult.error(e.error().code(), e.getMessage());
    }
}
