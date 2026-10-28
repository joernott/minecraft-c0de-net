package de.joernott.c0denet.client;

import de.joernott.c0de.network.ClientReceivers;
import de.joernott.c0de.platform.Platform;
import de.joernott.c0denet.C0deNet;
import de.joernott.c0denet.core.config.NetConfig;
import de.joernott.c0denet.core.http.NetError;
import de.joernott.c0denet.core.http.NetException;
import de.joernott.c0denet.core.http.NetRequest;
import de.joernott.c0denet.core.http.NetResponse;
import de.joernott.c0denet.core.http.RequestSender;
import de.joernott.c0denet.network.AnswerPayload;
import de.joernott.c0denet.network.CancelPayload;
import de.joernott.c0denet.network.NetworkChannel;
import de.joernott.c0denet.network.RequestPayload;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.client.Minecraft;

/**
 * The client side of the requests (docs/concept.md#running-requests-on-the-client): it sends the requests of the
 * processes its player runs and sends the answers back to the server in parts. The requests run in the threads of
 * the HTTP client; the payloads are sent on the client thread.
 */
public final class ClientRequests {
    private static final RequestSender SENDER = new RequestSender();
    private static final Map<Long, CompletableFuture<?>> RUNNING = new ConcurrentHashMap<>();
    private static NetConfig config = NetConfig.defaults(NetConfig.CLIENT);

    private ClientRequests() {}

    /** Loads the client settings and registers the receivers. Called by the client entry points. */
    public static void init() {
        try {
            config = NetConfig.load(Platform.INSTANCE.configDir().resolve(NetConfig.CLIENT_FILE), NetConfig.CLIENT);
            config.problems().forEach(C0deNet.LOGGER::warn);
        } catch (IOException e) {
            C0deNet.LOGGER.error("Can't read {}, using the defaults", NetConfig.CLIENT_FILE, e);
        }
        ClientReceivers.register(RequestPayload.TYPE, ClientRequests::receive);
        ClientReceivers.register(CancelPayload.TYPE, payload -> {
            CompletableFuture<?> request = RUNNING.remove(payload.id());
            if (request != null) {
                request.cancel(true);
            }
        });
    }

    private static void receive(RequestPayload payload) {
        if (!config.get(NetConfig.ALLOW_REQUESTS)) {
            send(AnswerPayload.failure(
                    payload.id(),
                    NetError.REQUESTS_DISABLED.code(),
                    "The client of the player doesn't send requests (allowRequests is false)"));
            return;
        }
        NetRequest request;
        try {
            request = NetRequest.fromBytes(payload.request());
        } catch (NetException e) {
            send(AnswerPayload.failure(payload.id(), e.error().code(), e.getMessage()));
            return;
        }
        CompletableFuture<NetResponse> running = SENDER.send(request, payload.maxResponseSize());
        // Recorded first: a request that fails at once completes before whenComplete returns
        RUNNING.put(payload.id(), running);
        running.whenComplete((response, failure) -> Minecraft.getInstance().execute(() -> {
            if (RUNNING.remove(payload.id()) == null) {
                // Cancelled by the server
                return;
            }
            if (failure != null) {
                Throwable cause = failure.getCause() instanceof NetException ? failure.getCause() : failure;
                if (cause instanceof NetException net) {
                    send(AnswerPayload.failure(payload.id(), net.error().code(), net.getMessage()));
                } else {
                    send(AnswerPayload.failure(
                            payload.id(), NetError.REQUEST_FAILED.code(), String.valueOf(cause)));
                }
                return;
            }
            for (AnswerPayload part : NetworkChannel.parts(payload.id(), response.toBytes())) {
                send(part);
            }
        }));
    }

    private static void send(AnswerPayload payload) {
        Platform.INSTANCE.sendToServer(payload);
    }
}
