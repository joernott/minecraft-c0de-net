package de.joernott.c0denet.core.http;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpsConfigurator;
import com.sun.net.httpserver.HttpsServer;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.nio.charset.StandardCharsets;
import java.security.KeyStore;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/** Sending requests with the HTTP client of Java to local servers (task 2.3). */
class RequestSenderTest {
    private static final long LIMIT = 1 << 20;

    private final RequestSender sender = new RequestSender();
    private HttpServer server;
    private int port;

    /** Answers every request with its method, its body and its X-Test and Authorization headers. */
    private static void echo(HttpExchange exchange) throws IOException {
        byte[] body;
        try (InputStream in = exchange.getRequestBody()) {
            body = in.readAllBytes();
        }
        String text = exchange.getRequestMethod() + " " + exchange.getRequestURI() + " "
                + new String(body, StandardCharsets.UTF_8) + " "
                + exchange.getRequestHeaders().getFirst("X-Test") + " "
                + exchange.getRequestHeaders().getFirst("Authorization");
        respond(exchange, 200, text);
    }

    private static void respond(HttpExchange exchange, int status, String text) throws IOException {
        byte[] bytes = text.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().add("Content-Type", "text/plain; charset=UTF-8");
        exchange.getResponseHeaders().add("X-Multi", "a");
        exchange.getResponseHeaders().add("X-Multi", "b");
        if ("HEAD".equals(exchange.getRequestMethod())) {
            exchange.sendResponseHeaders(status, -1);
        } else {
            exchange.sendResponseHeaders(status, bytes.length);
            try (OutputStream out = exchange.getResponseBody()) {
                out.write(bytes);
            }
        }
        exchange.close();
    }

    @BeforeEach
    void start() throws IOException {
        server = HttpServer.create(new InetSocketAddress(InetAddress.getLoopbackAddress(), 0), 0);
        server.createContext("/echo", RequestSenderTest::echo);
        server.createContext("/missing", exchange -> respond(exchange, 404, "no such thing"));
        server.createContext("/slow", exchange -> {
            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            respond(exchange, 200, "late");
        });
        server.createContext("/big", exchange -> respond(exchange, 200, "x".repeat(2 * (int) LIMIT)));
        server.createContext("/moved", exchange -> {
            exchange.getResponseHeaders().add("Location", "/echo");
            exchange.sendResponseHeaders(302, -1);
            exchange.close();
        });
        server.start();
        port = server.getAddress().getPort();
    }

    @AfterEach
    void stop() {
        server.stop(0);
    }

    private HttpConnection local(long timeout, String proxy, Map<String, String> headers) {
        return new HttpConnection(false, "127.0.0.1", port, "", "", "", false, proxy, headers, timeout);
    }

    private NetResponse send(HttpConnection connection, HttpMethod method, String endpoint, String data)
            throws Exception {
        return sender.send(NetRequest.of(connection, method, endpoint, data), LIMIT)
                .get(30, TimeUnit.SECONDS);
    }

    /** The error of a request that fails. */
    private NetError failure(HttpConnection connection, String endpoint, long limit) throws Exception {
        CompletableFuture<NetResponse> answer =
                sender.send(NetRequest.of(connection, HttpMethod.GET, endpoint, ""), limit);
        try {
            answer.get(30, TimeUnit.SECONDS);
        } catch (ExecutionException e) {
            assertTrue(e.getCause() instanceof NetException, "Not a NetException: " + e.getCause());
            return ((NetException) e.getCause()).error();
        }
        throw new AssertionError("The request didn't fail");
    }

    @Test
    void everyMethodIsSent() throws Exception {
        HttpConnection connection = local(0, "", Map.of("X-Test", "t"));
        for (HttpMethod method : HttpMethod.values()) {
            NetResponse response = send(connection, method, "/echo?n=1", "data");
            assertEquals(200, response.status(), method.name());
            if (method == HttpMethod.HEAD) {
                assertTrue(
                        response.result().startsWith("{\"content-type\":\"text/plain; charset=UTF-8\""),
                        response.result());
            } else {
                String body = method.sendsData() ? "data" : "";
                assertEquals(method.name() + " /echo?n=1 " + body + " t null", response.result());
            }
        }
    }

    @Test
    void answersHaveTheirStatusAndHeaders() throws Exception {
        NetResponse response = send(local(0, "", Map.of()), HttpMethod.GET, "/missing", "");
        assertEquals(404, response.status());
        assertTrue(response.isError());
        assertEquals("no such thing", response.result());
        assertEquals("a, b", response.headers().get("x-multi"));
    }

    @Test
    void usersAreAuthenticated() throws Exception {
        HttpConnection connection =
                new HttpConnection(false, "127.0.0.1", port, "", "admin", "1234", false, "", Map.of(), 0);
        assertTrue(send(connection, HttpMethod.GET, "/echo", "").result().endsWith("Basic YWRtaW46MTIzNA=="));
    }

    @Test
    void redirectsAreFollowed() throws Exception {
        assertTrue(send(local(0, "", Map.of()), HttpMethod.GET, "/moved", "")
                .result()
                .startsWith("GET /echo"));
    }

    @Test
    void slowAnswersTimeOut() throws Exception {
        assertEquals(NetError.REQUEST_TIMEOUT, failure(local(1, "", Map.of()), "/slow", LIMIT));
    }

    @Test
    void largeAnswersAreCancelled() throws Exception {
        assertEquals(NetError.RESPONSE_TOO_LARGE, failure(local(0, "", Map.of()), "/big", LIMIT));
    }

    @Test
    void unreachableServersFail() throws Exception {
        int closed;
        try (ServerSocket socket = new ServerSocket(0, 1, InetAddress.getLoopbackAddress())) {
            closed = socket.getLocalPort();
        }
        HttpConnection refused = new HttpConnection(false, "127.0.0.1", closed, "", "", "", false, "", Map.of(), 5);
        assertEquals(NetError.REQUEST_FAILED, failure(refused, "/", LIMIT));
        HttpConnection unknown = new HttpConnection(false, "host.invalid", 0, "", "", "", false, "", Map.of(), 5);
        assertEquals(NetError.REQUEST_FAILED, failure(unknown, "/", LIMIT));
    }

    @Test
    void headersThatJavaSetsItselfAreInvalid() throws Exception {
        assertEquals(NetError.INVALID_ARGUMENT, failure(local(0, "", Map.of("Host", "other")), "/echo", LIMIT));
    }

    @Test
    void proxiesReceiveTheRequests() throws Exception {
        HttpServer proxy = HttpServer.create(new InetSocketAddress(InetAddress.getLoopbackAddress(), 0), 0);
        // An HTTP proxy gets the absolute URL of the target; this one answers itself
        proxy.createContext("/", exchange -> respond(exchange, 200, "proxy " + exchange.getRequestURI()));
        proxy.start();
        try {
            HttpConnection connection = new HttpConnection(
                    false,
                    "target.example",
                    8080,
                    "",
                    "",
                    "",
                    false,
                    "http://127.0.0.1:" + proxy.getAddress().getPort(),
                    Map.of(),
                    0);
            assertEquals(
                    "proxy http://target.example:8080/path",
                    send(connection, HttpMethod.GET, "/path", "").result());
        } finally {
            proxy.stop(0);
        }
    }

    @Test
    void selfSignedCertificatesNeedSkippedValidation() throws Exception {
        HttpsServer https = HttpsServer.create(new InetSocketAddress(InetAddress.getLoopbackAddress(), 0), 0);
        https.setHttpsConfigurator(new HttpsConfigurator(selfSigned()));
        https.createContext("/", exchange -> respond(exchange, 200, "secret"));
        https.start();
        try {
            int httpsPort = https.getAddress().getPort();
            // The certificate is for localhost, the connection uses the IP address: neither is checked when skipped
            HttpConnection checked =
                    new HttpConnection(true, "127.0.0.1", httpsPort, "", "", "", false, "", Map.of(), 10);
            assertEquals(NetError.REQUEST_FAILED, failure(checked, "/", LIMIT));
            HttpConnection skipped =
                    new HttpConnection(true, "127.0.0.1", httpsPort, "", "", "", true, "", Map.of(), 10);
            assertEquals("secret", send(skipped, HttpMethod.GET, "/", "").result());
        } finally {
            https.stop(0);
        }
    }

    @Test
    void cancelledRequestsEnd() throws Exception {
        CompletableFuture<NetResponse> answer =
                sender.send(NetRequest.of(local(0, "", Map.of()), HttpMethod.GET, "/slow", ""), LIMIT);
        assertTrue(answer.cancel(true));
        assertTrue(answer.isCancelled());
    }

    private static SSLContext selfSigned() throws Exception {
        char[] password = "c0denet".toCharArray();
        KeyStore keys = KeyStore.getInstance("PKCS12");
        try (InputStream in = RequestSenderTest.class.getResourceAsStream("/selfsigned.p12")) {
            keys.load(in, password);
        }
        KeyManagerFactory factory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
        factory.init(keys, password);
        SSLContext context = SSLContext.getInstance("TLS");
        context.init(factory.getKeyManagers(), null, null);
        return context;
    }
}
