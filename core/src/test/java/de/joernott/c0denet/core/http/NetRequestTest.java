package de.joernott.c0denet.core.http;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Map;
import org.junit.jupiter.api.Test;

/** Requests built from connections and their network format (task 2.2). */
class NetRequestTest {
    @Test
    void usersAreSentWithBasicAuthentication() {
        HttpConnection connection = new HttpConnection(
                false, "example.com", 0, "", "admin", "1234", false, "", Map.of("Accept", "text/plain"), 0);
        NetRequest request = NetRequest.of(connection, HttpMethod.GET, "/", "");
        assertEquals("Basic YWRtaW46MTIzNA==", request.headers().get("Authorization"));
        assertEquals("text/plain", request.headers().get("Accept"));
    }

    @Test
    void anAuthorizationHeaderOfTheConnectionWins() {
        HttpConnection connection = new HttpConnection(
                false, "example.com", 0, "", "admin", "1234", false, "", Map.of("authorization", "Bearer t"), 0);
        NetRequest request = NetRequest.of(connection, HttpMethod.GET, "/", "");
        assertEquals("Bearer t", request.headers().get("authorization"));
        assertFalse(request.headers().containsKey("Authorization"));
    }

    @Test
    void onlyTheMethodsThatSendDataHaveABody() {
        HttpConnection connection = HttpConnectionTest.connection("example.com");
        assertEquals(
                "", NetRequest.of(connection, HttpMethod.GET, "/", "ignored").body());
        assertEquals("{}", NetRequest.of(connection, HttpMethod.POST, "/", "{}").body());
        assertEquals(60, NetRequest.of(connection, HttpMethod.POST, "/", "{}").timeout());
        assertNull(NetRequest.of(connection, HttpMethod.POST, "/", "{}").proxy());
    }

    @Test
    void invalidConnectionsMakeNoRequest() {
        NetException e = assertThrows(
                NetException.class, () -> NetRequest.of(HttpConnectionTest.connection(""), HttpMethod.GET, "/", ""));
        assertEquals(NetError.INVALID_ARGUMENT, e.error());
    }

    @Test
    void requestsSurviveTheNetwork() {
        HttpConnection connection = new HttpConnection(
                true,
                "example.com",
                8443,
                "/api",
                "u",
                "p",
                true,
                "http://proxy:3128",
                Map.of("X-Long", "x".repeat(70_000)),
                12);
        NetRequest request = NetRequest.of(connection, HttpMethod.PUT, "/items/1", "ü".repeat(40_000));
        NetRequest copy = NetRequest.fromBytes(request.toBytes());
        assertEquals(request, copy);
        assertArrayEquals(request.toBytes(), copy.toBytes());
    }

    @Test
    void brokenBytesAreNoRequest() {
        byte[] bytes = NetRequest.of(HttpConnectionTest.connection("example.com"), HttpMethod.GET, "/", "")
                .toBytes();
        byte[] truncated = java.util.Arrays.copyOf(bytes, bytes.length - 3);
        assertEquals(
                NetError.INVALID_ARGUMENT,
                assertThrows(NetException.class, () -> NetRequest.fromBytes(truncated))
                        .error());
    }
}
