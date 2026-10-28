package de.joernott.c0denet.core.http;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Map;
import org.junit.jupiter.api.Test;

/** The values of HTTP_CONNECTION, their validation and the URL of a request (task 2.1). */
class HttpConnectionTest {
    /** A connection with the defaults of the struct and a server. */
    static HttpConnection connection(String server) {
        return new HttpConnection(false, server, 0, "", "", "", false, "", Map.of(), 0);
    }

    static HttpConnection withPort(HttpConnection c, long port) {
        return new HttpConnection(
                c.useSsl(),
                c.server(),
                port,
                c.baseEndpoint(),
                c.user(),
                c.password(),
                c.skipSslValidation(),
                c.proxy(),
                c.headers(),
                c.timeout());
    }

    private static void assertInvalid(HttpConnection connection) {
        NetException e = assertThrows(NetException.class, connection::validate);
        assertEquals(NetError.INVALID_ARGUMENT, e.error());
    }

    @Test
    void urlsJoinProtocolServerPortBaseEndpointAndEndpoint() {
        assertEquals("http://example.com/x", connection("example.com").uri("/x").toString());
        HttpConnection api = new HttpConnection(true, "api.example.com", 8443, "/v2", "", "", false, "", Map.of(), 0);
        assertEquals(
                "https://api.example.com:8443/v2/users/7?q=a",
                api.uri("/users/7?q=a").toString());
        assertEquals(
                "http://[::1]:8080/", withPort(connection("::1"), 8080).uri("/").toString());
        assertEquals("http://example.com", connection("example.com").uri("").toString());
    }

    @Test
    void endpointsThatAreNoUrlAreInvalid() {
        NetException e =
                assertThrows(NetException.class, () -> connection("example.com").uri("/a b"));
        assertEquals(NetError.INVALID_ARGUMENT, e.error());
    }

    @Test
    void theTimeoutHasADefault() {
        assertEquals(60, connection("example.com").effectiveTimeout());
        HttpConnection quick = new HttpConnection(false, "example.com", 0, "", "", "", false, "", Map.of(), 5);
        assertEquals(5, quick.effectiveTimeout());
    }

    @Test
    void invalidValuesAreRejected() {
        connection("example.com").validate();
        assertInvalid(connection(""));
        assertInvalid(connection("example.com/path"));
        assertInvalid(connection("user@example.com"));
        assertInvalid(withPort(connection("example.com"), -1));
        assertInvalid(withPort(connection("example.com"), 65536));
        assertInvalid(new HttpConnection(false, "example.com", 0, "", "", "", false, "", Map.of(), 301));
        assertInvalid(new HttpConnection(false, "example.com", 0, "", "", "", false, "", Map.of(), -1));
        assertInvalid(new HttpConnection(false, "example.com", 0, "", "", "", false, "", Map.of("Bad Name", "x"), 0));
        assertInvalid(new HttpConnection(
                false, "example.com", 0, "", "", "", false, "", Map.of("X-Test", "a\r\nInjected: b"), 0));
    }

    @Test
    void proxiesAreHttpUrlsWithHostAndPort() {
        new HttpConnection(false, "example.com", 0, "", "", "", false, "http://proxy:3128", Map.of(), 0).validate();
        assertInvalid(new HttpConnection(false, "example.com", 0, "", "", "", false, "proxy:3128", Map.of(), 0));
        assertInvalid(new HttpConnection(false, "example.com", 0, "", "", "", false, "http://proxy", Map.of(), 0));
        assertInvalid(
                new HttpConnection(false, "example.com", 0, "", "", "", false, "socks5://proxy:1080", Map.of(), 0));
    }
}
