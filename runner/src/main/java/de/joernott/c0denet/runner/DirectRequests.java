package de.joernott.c0denet.runner;

import de.joernott.c0de.core.library.NativeCall;
import de.joernott.c0de.core.library.NativeResult;
import de.joernott.c0de.core.value.StringValue;
import de.joernott.c0de.core.value.StructValue;
import de.joernott.c0denet.core.config.NetConfig;
import de.joernott.c0denet.core.http.HttpMethod;
import de.joernott.c0denet.core.http.NetError;
import de.joernott.c0denet.core.http.NetException;
import de.joernott.c0denet.core.http.NetRequest;
import de.joernott.c0denet.core.http.NetResponse;
import de.joernott.c0denet.core.http.RequestSender;
import de.joernott.c0denet.library.HttpLibrary;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Sends the requests of c0de_net/http in the program runner: directly with the HTTP client of Java, and the cursor
 * waits until the answer is there. In the game, the client of the player who runs the process sends them.
 */
final class DirectRequests {
    /** The time the HTTP client gets beyond the timeout of the request, like in the game. */
    private static final int GRACE_SECONDS = 5;

    private static final RequestSender SENDER = new RequestSender();

    private DirectRequests() {}

    static NativeResult call(NativeCall call, HttpMethod method) {
        NetRequest request;
        try {
            request = NetRequest.of(
                    HttpLibrary.connection((StructValue) call.argument("connection")),
                    method,
                    ((StringValue) call.argument("endpoint")).value(),
                    method.sendsData() ? ((StringValue) call.argument("data")).value() : "");
        } catch (NetException e) {
            return HttpLibrary.error(e);
        }
        try {
            NetResponse response = SENDER.send(request, NetConfig.MAX_RESPONSE_SIZE.defaultValue())
                    .get(request.timeout() + GRACE_SECONDS, TimeUnit.SECONDS);
            return HttpLibrary.result(response);
        } catch (ExecutionException e) {
            if (e.getCause() instanceof NetException failure) {
                return HttpLibrary.error(failure);
            }
            return HttpLibrary.error(new NetException(NetError.REQUEST_FAILED, String.valueOf(e.getCause())));
        } catch (TimeoutException e) {
            return HttpLibrary.error(new NetException(
                    NetError.REQUEST_TIMEOUT,
                    "No answer from " + request.uri().getHost() + " within " + request.timeout() + " seconds"));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return HttpLibrary.error(new NetException(NetError.REQUEST_FAILED, "The runner was interrupted"));
        }
    }
}
