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
import java.util.function.Function;
import java.util.function.Supplier;
import net.minecraft.server.MinecraftServer;

/**
 * The requests of the processes on the server (docs/concept.md#waiting-for-the-answer): HTTP requests of
 * c0de_net/http and calls of c0de_net/mail. A function sends its request to the client of the player who runs the
 * process and returns {@link NativeResult#waitForValue()}: the cursor calls it again in every step until the answer,
 * an error or the timeout ends the call.
 *
 * <p>Only used on the server thread.
 */
public final class PendingRequests {
    /** The largest request in bytes: a packet to a client may have 1 MiB. */
    public static final int MAX_REQUEST_SIZE = 1_000_000;

    /** The largest part of an answer: a packet to the server may have 32 KiB. */
    public static final int PART_SIZE = 30_000;

    /** Bytes of an answer besides its body: status and headers, or the fields of a mail answer. */
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

    /**
     * What the client of a player does for a call: the request as bytes, its timeout and target (for the message of
     * REQUEST_TIMEOUT), the largest answer, and how the bytes of the answer become the result of the call.
     */
    public record Job(byte[] request, int timeout, String target, long maxAnswer, Function<byte[], NativeResult> result) {}

    /** A request of a process. */
    private static final class Pending {
        final long id;
        final Object process;
        final UUID player;
        final Job job;
        boolean sent;
        long deadline;
        int lastCalled;
        int nextPart;
        final ByteArrayOutputStream answer = new ByteArrayOutputStream();
        byte[] complete;
        NetException failure;

        Pending(long id, Object process, UUID player, Job job) {
            this.id = id;
            this.process = process;
            this.player = player;
            this.job = job;
        }

        boolean done() {
            return complete != null || failure != null;
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

    /** The server settings. */
    public static NetConfig config() {
        return config;
    }

    /** The function of an HTTP method: sends the request, then waits for the answer. */
    public static NativeResult call(NativeCall call, HttpMethod method) {
        return run(call, HttpLibrary.NAME, () -> {
            NetRequest request = NetRequest.of(
                    HttpLibrary.connection((StructValue) call.argument("connection")),
                    method,
                    ((StringValue) call.argument("endpoint")).value(),
                    method.sendsData() ? ((StringValue) call.argument("data")).value() : "");
            return new Job(
                    request.toBytes(),
                    request.timeout(),
                    request.uri().getHost(),
                    config.get(NetConfig.MAX_RESPONSE_SIZE),
                    bytes -> HttpLibrary.result(NetResponse.fromBytes(bytes)));
        });
    }

    /**
     * A call that the client of the player who runs the process does: the job is made at the first call of the
     * function (it may throw a NetException, the error of the call), then the cursor waits for the answer.
     *
     * @param library the name of the library, for the message outside the game
     */
    public static NativeResult run(NativeCall call, String library, Supplier<Job> makeJob) {
        if (!(call.process().host() instanceof ProcessHost host)) {
            return NativeResult.error(NetError.INVALID_ARGUMENT.code(), library + " only works in the game");
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
            Job job;
            try {
                job = makeJob.get();
            } catch (NetException e) {
                return error(e);
            }
            if (job.request().length > MAX_REQUEST_SIZE) {
                return error(new NetException(
                        NetError.INVALID_ARGUMENT, "The request is larger than " + MAX_REQUEST_SIZE + " bytes"));
            }
            pending = new Pending(nextId++, call.process(), player, job);
            BY_PROCESS.put(call.process(), pending);
            BY_ID.put(pending.id, pending);
        }
        pending.lastCalled = tick;
        if (pending.done()) {
            remove(pending);
            if (pending.failure != null) {
                return error(pending.failure);
            }
            try {
                return pending.job.result().apply(pending.complete);
            } catch (NetException e) {
                return error(e);
            }
        }
        Optional<String> unavailable = channel.unavailable(host.server(), pending.player);
        if (unavailable.isPresent()) {
            cancel(host.server(), pending);
            return error(new NetException(NetError.CLIENT_UNAVAILABLE, unavailable.get()));
        }
        if (!pending.sent) {
            if (running(pending.player) < config.get(NetConfig.MAX_REQUESTS_PER_PLAYER)) {
                pending.sent = true;
                pending.deadline = System.currentTimeMillis() + (pending.job.timeout() + GRACE_SECONDS) * 1000L;
                channel.send(host.server(), pending.player, pending.id, pending.job.request(), pending.job.maxAnswer());
            }
        } else if (System.currentTimeMillis() > pending.deadline) {
            cancel(host.server(), pending);
            return error(new NetException(
                    NetError.REQUEST_TIMEOUT,
                    "No answer from " + pending.job.target() + " within " + pending.job.timeout() + " seconds"));
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
        if (pending.answer.size() + part.length > pending.job.maxAnswer() + HEADER_ALLOWANCE) {
            pending.failure = new NetException(
                    NetError.RESPONSE_TOO_LARGE, "The answer is larger than " + pending.job.maxAnswer() + " bytes");
            channel.cancel(server, player, id);
            return;
        }
        pending.answer.write(part, 0, part.length);
        pending.nextPart++;
        if (index == total - 1) {
            pending.complete = pending.answer.toByteArray();
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

    private static NativeResult error(NetException e) {
        return HttpLibrary.error(e);
    }
}
