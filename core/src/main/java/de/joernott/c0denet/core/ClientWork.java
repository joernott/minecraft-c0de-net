package de.joernott.c0denet.core;

import de.joernott.c0denet.core.http.NetRequest;
import de.joernott.c0denet.core.http.NetResponse;
import de.joernott.c0denet.core.http.RequestSender;
import de.joernott.c0denet.core.mail.MailClient;
import de.joernott.c0denet.core.mail.MailRequest;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * What the client of a player does with a request of the server (docs/concept.md#running-requests-on-the-client): an
 * HTTP request of c0de_net/http goes to the HTTP client of Java, a call of c0de_net/mail to {@link MailClient} in a
 * thread of its own. Both answer with bytes, which go back to the server in parts. The GameTests use it in the server
 * for mock players.
 */
public final class ClientWork {
    private static final RequestSender HTTP = new RequestSender();

    /** The threads of the mail calls: daemons, so they don't keep the game running. */
    private static final ExecutorService MAIL = Executors.newFixedThreadPool(4, task -> {
        Thread thread = new Thread(task, "c0de net mail");
        thread.setDaemon(true);
        return thread;
    });

    private ClientWork() {}

    /** Whether a request is a call of c0de_net/mail. */
    public static boolean isMail(byte[] request) {
        return MailRequest.isMail(request);
    }

    /**
     * Runs a request and returns the bytes of its answer. A failure completes the future with a NetException (also
     * wrapped in a CompletionException).
     *
     * @throws de.joernott.c0denet.core.http.NetException INVALID_ARGUMENT if the bytes are not a request
     */
    public static CompletableFuture<byte[]> run(byte[] request, long maxResponseSize) {
        if (isMail(request)) {
            MailRequest mail = MailRequest.fromBytes(request);
            return CompletableFuture.supplyAsync(() -> MailClient.run(mail).toBytes(), MAIL);
        }
        NetRequest http = NetRequest.fromBytes(request);
        return HTTP.send(http, maxResponseSize).thenApply(NetResponse::toBytes);
    }
}
