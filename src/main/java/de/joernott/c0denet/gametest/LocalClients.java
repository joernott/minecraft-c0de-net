package de.joernott.c0denet.gametest;

import de.joernott.c0denet.core.http.NetError;
import de.joernott.c0denet.core.http.NetException;
import de.joernott.c0denet.core.http.NetRequest;
import de.joernott.c0denet.core.http.NetResponse;
import de.joernott.c0denet.core.http.RequestSender;
import de.joernott.c0denet.http.ClientChannel;
import de.joernott.c0denet.http.PendingRequests;
import de.joernott.c0denet.network.AnswerPayload;
import de.joernott.c0denet.network.NetworkChannel;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.server.MinecraftServer;

/**
 * A stand-in for the clients of mock players, which can't receive payloads: their requests are sent in the server,
 * like a client sends them, and their answers arrive in parts like the payloads of a client. The requests of all
 * other players go to the network as usual.
 */
final class LocalClients implements ClientChannel {
    private static final Set<UUID> LOCAL = ConcurrentHashMap.newKeySet();

    private final ClientChannel network;
    private final RequestSender sender = new RequestSender();
    private final Map<Long, CompletableFuture<NetResponse>> running = new ConcurrentHashMap<>();

    private LocalClients(ClientChannel network) {
        this.network = network;
    }

    /** Puts the stand-in in front of the network, once for all tests. */
    static synchronized void install() {
        if (!(PendingRequests.channel() instanceof LocalClients)) {
            PendingRequests.channel(new LocalClients(PendingRequests.channel()));
        }
    }

    /** The requests of the player are sent by the stand-in. */
    static void add(UUID player) {
        LOCAL.add(player);
    }

    static void remove(UUID player) {
        LOCAL.remove(player);
    }

    @Override
    public Optional<String> unavailable(MinecraftServer server, UUID player) {
        return LOCAL.contains(player) ? Optional.empty() : network.unavailable(server, player);
    }

    @Override
    public void send(MinecraftServer server, UUID player, long id, NetRequest request, long maxResponseSize) {
        if (!LOCAL.contains(player)) {
            network.send(server, player, id, request, maxResponseSize);
            return;
        }
        // Through the network format, as from a real server to a real client
        CompletableFuture<NetResponse> answer =
                sender.send(NetRequest.fromBytes(request.toBytes()), maxResponseSize);
        running.put(id, answer);
        answer.whenComplete((response, failure) -> server.execute(() -> {
            if (running.remove(id) == null) {
                return;
            }
            if (failure != null) {
                NetException net = failure.getCause() instanceof NetException cause
                        ? cause
                        : new NetException(NetError.REQUEST_FAILED, String.valueOf(failure));
                PendingRequests.failed(player, id, net.error().code(), net.getMessage());
                return;
            }
            for (AnswerPayload part : NetworkChannel.parts(id, response.toBytes())) {
                PendingRequests.answered(player, id, part.index(), part.total(), part.data());
            }
        }));
    }

    @Override
    public void cancel(MinecraftServer server, UUID player, long id) {
        CompletableFuture<NetResponse> answer = running.remove(id);
        if (answer != null) {
            answer.cancel(true);
        } else {
            network.cancel(server, player, id);
        }
    }
}
