package de.joernott.c0denet.core.http;

import java.io.IOException;
import java.net.ConnectException;
import java.net.InetSocketAddress;
import java.net.ProxySelector;
import java.net.Socket;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpHeaders;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpTimeoutException;
import java.nio.ByteBuffer;
import java.nio.channels.UnresolvedAddressException;
import java.security.GeneralSecurityException;
import java.security.cert.X509Certificate;
import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Flow;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLEngine;
import javax.net.ssl.SSLException;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509ExtendedTrustManager;

/**
 * Sends requests with the HTTP client of Java (docs/concept.md#running-requests-on-the-client). The clients are
 * kept per proxy and SSL setting, so that requests to the same server reuse their connections.
 */
public final class RequestSender {
    private record ClientKey(URI proxy, boolean skipSslValidation) {}

    private final Map<ClientKey, HttpClient> clients = new ConcurrentHashMap<>();

    /**
     * Sends a request. The answer arrives in a thread of the HTTP client; cancelling the future cancels the request.
     *
     * @param maxResponseSize the largest body in bytes; a larger one fails with RESPONSE_TOO_LARGE
     * @return the answer, or a {@link NetException} with INVALID_ARGUMENT, REQUEST_FAILED, REQUEST_TIMEOUT or
     *     RESPONSE_TOO_LARGE
     */
    public CompletableFuture<NetResponse> send(NetRequest request, long maxResponseSize) {
        HttpRequest httpRequest;
        try {
            HttpRequest.Builder builder = HttpRequest.newBuilder(request.uri())
                    .timeout(Duration.ofSeconds(request.timeout()))
                    .method(
                            request.method().name(),
                            request.method().sendsData()
                                    ? HttpRequest.BodyPublishers.ofString(request.body())
                                    : HttpRequest.BodyPublishers.noBody());
            request.headers().forEach(builder::header);
            httpRequest = builder.build();
        } catch (IllegalArgumentException e) {
            // e.g. a header that Java sets itself (Host, Content-Length, Connection, Expect, Upgrade)
            return CompletableFuture.failedFuture(new NetException(NetError.INVALID_ARGUMENT, e.getMessage(), e));
        }
        HttpClient client = clients.computeIfAbsent(
                new ClientKey(request.proxy(), request.skipSslValidation()), RequestSender::client);
        CompletableFuture<HttpResponse<byte[]>> sent =
                client.sendAsync(httpRequest, info -> new LimitedBody(maxResponseSize));
        // The timeout of the request ends with the headers of the answer; the whole answer has the same limit
        sent.orTimeout(request.timeout(), TimeUnit.SECONDS);
        CompletableFuture<NetResponse> answer = sent.handle((response, failure) -> {
            if (failure != null) {
                throw translate(failure, request);
            }
            return new NetResponse(
                    request.method(), response.statusCode(), joined(response.headers()), response.body());
        });
        // Cancelling the answer cancels the exchange of the HTTP client
        answer.whenComplete((response, failure) -> {
            if (answer.isCancelled()) {
                sent.cancel(true);
            }
        });
        return answer;
    }

    private static HttpClient client(ClientKey key) {
        HttpClient.Builder builder = HttpClient.newBuilder().followRedirects(HttpClient.Redirect.NORMAL);
        if (key.proxy() != null) {
            builder.proxy(ProxySelector.of(InetSocketAddress.createUnresolved(
                    key.proxy().getHost(), key.proxy().getPort())));
        }
        if (key.skipSslValidation()) {
            builder.sslContext(trustingEverything());
        }
        return builder.build();
    }

    /** The headers with lower-case names; repeated headers are joined with ", ". */
    private static Map<String, String> joined(HttpHeaders headers) {
        Map<String, String> result = new LinkedHashMap<>();
        headers.map().forEach((name, values) -> {
            // HTTP/2 pseudo headers such as :status are no headers of the answer
            if (!name.startsWith(":")) {
                result.put(name, String.join(", ", values));
            }
        });
        return result;
    }

    /** The error of a failed request. */
    private static NetException translate(Throwable failure, NetRequest request) {
        Throwable cause =
                failure instanceof CompletionException && failure.getCause() != null ? failure.getCause() : failure;
        if (cause instanceof NetException net) {
            return net;
        }
        if (cause instanceof ResponseTooLargeException tooLarge) {
            return new NetException(NetError.RESPONSE_TOO_LARGE, tooLarge.getMessage(), cause);
        }
        if (cause instanceof HttpTimeoutException || cause instanceof TimeoutException) {
            return new NetException(
                    NetError.REQUEST_TIMEOUT,
                    "No answer from " + request.uri().getHost() + " within " + request.timeout() + " seconds",
                    cause);
        }
        return new NetException(NetError.REQUEST_FAILED, reason(cause, request), cause);
    }

    /** A message for the reason why a request failed. */
    private static String reason(Throwable cause, NetRequest request) {
        String host = request.uri().getHost();
        for (Throwable t = cause; t != null; t = t.getCause()) {
            if (t instanceof UnresolvedAddressException || t instanceof java.net.UnknownHostException) {
                return "Unknown host "
                        + (request.proxy() != null ? request.proxy().getHost() : host);
            }
            if (t instanceof ConnectException) {
                return "No connection to "
                        + (request.proxy() != null ? "the proxy " + request.proxy() : host + ":" + port(request.uri()));
            }
            if (t instanceof SSLException) {
                return "SSL error with " + host + ": " + t.getMessage();
            }
        }
        return cause.getClass().getSimpleName() + (cause.getMessage() == null ? "" : ": " + cause.getMessage());
    }

    private static int port(URI uri) {
        return uri.getPort() >= 0 ? uri.getPort() : "https".equals(uri.getScheme()) ? 443 : 80;
    }

    /** An SSL context that accepts every certificate for every host, for {@code skip_ssl_validation}. */
    private static SSLContext trustingEverything() {
        TrustManager trustAll = new X509ExtendedTrustManager() {
            @Override
            public void checkClientTrusted(X509Certificate[] chain, String authType) {}

            @Override
            public void checkServerTrusted(X509Certificate[] chain, String authType) {}

            @Override
            public void checkClientTrusted(X509Certificate[] chain, String authType, Socket socket) {}

            @Override
            public void checkServerTrusted(X509Certificate[] chain, String authType, Socket socket) {}

            @Override
            public void checkClientTrusted(X509Certificate[] chain, String authType, SSLEngine engine) {}

            @Override
            public void checkServerTrusted(X509Certificate[] chain, String authType, SSLEngine engine) {}

            @Override
            public X509Certificate[] getAcceptedIssuers() {
                return new X509Certificate[0];
            }
        };
        try {
            SSLContext context = SSLContext.getInstance("TLS");
            context.init(null, new TrustManager[] {trustAll}, null);
            return context;
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException("TLS is not available", e);
        }
    }

    /** A body that fails with {@link ResponseTooLargeException} as soon as it exceeds its limit. */
    private static final class LimitedBody implements HttpResponse.BodySubscriber<byte[]> {
        private final long limit;
        private final CompletableFuture<byte[]> result = new CompletableFuture<>();
        private final java.io.ByteArrayOutputStream bytes = new java.io.ByteArrayOutputStream();
        private Flow.Subscription subscription;

        LimitedBody(long limit) {
            this.limit = limit;
        }

        @Override
        public CompletionStage<byte[]> getBody() {
            return result;
        }

        @Override
        public void onSubscribe(Flow.Subscription subscription) {
            this.subscription = subscription;
            subscription.request(Long.MAX_VALUE);
        }

        @Override
        public void onNext(List<ByteBuffer> buffers) {
            if (result.isDone()) {
                return;
            }
            for (ByteBuffer buffer : buffers) {
                if (bytes.size() + (long) buffer.remaining() > limit) {
                    subscription.cancel();
                    result.completeExceptionally(
                            new ResponseTooLargeException("The answer is larger than " + limit + " bytes"));
                    return;
                }
                byte[] chunk = new byte[buffer.remaining()];
                buffer.get(chunk);
                bytes.write(chunk, 0, chunk.length);
            }
        }

        @Override
        public void onError(Throwable throwable) {
            result.completeExceptionally(throwable);
        }

        @Override
        public void onComplete() {
            result.complete(bytes.toByteArray());
        }
    }

    /** The body of an answer is larger than the limit. */
    private static final class ResponseTooLargeException extends IOException {
        ResponseTooLargeException(String message) {
            super(message);
        }
    }
}
