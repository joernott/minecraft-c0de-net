package de.joernott.c0denet.core.http;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.charset.StandardCharsets;
import java.util.Map;
import org.junit.jupiter.api.Test;

/** Answers: decoding, headers, HEAD, status and their network format (task 2.4). */
class NetResponseTest {
    private static NetResponse response(HttpMethod method, int status, Map<String, String> headers, byte[] body) {
        return new NetResponse(method, status, headers, body);
    }

    @Test
    void bodiesAreDecodedWithTheirCharset() {
        byte[] latin1 = "Grüße".getBytes(StandardCharsets.ISO_8859_1);
        assertEquals(
                "Grüße",
                response(HttpMethod.GET, 200, Map.of("Content-Type", "text/plain; charset=ISO-8859-1"), latin1)
                        .result());
        assertEquals(
                "Grüße",
                response(HttpMethod.GET, 200, Map.of(), "Grüße".getBytes(StandardCharsets.UTF_8))
                        .result());
        // An unknown charset is read as UTF-8
        assertEquals(
                "ok",
                response(HttpMethod.GET, 200, Map.of("content-type", "text/plain; charset=\"nope\""), "ok".getBytes())
                        .result());
        // Invalid bytes become U+FFFD
        assertEquals(
                "a\uFFFDb",
                response(HttpMethod.GET, 200, Map.of(), new byte[] {'a', (byte) 0xFF, 'b'})
                        .result());
    }

    @Test
    void headerNamesAreLowerCase() {
        NetResponse response = response(HttpMethod.GET, 200, Map.of("X-Count", "1, 2"), new byte[0]);
        assertEquals(Map.of("x-count", "1, 2"), response.headers());
    }

    @Test
    void headIsAnsweredWithTheHeadersAsJson() {
        NetResponse response =
                response(HttpMethod.HEAD, 200, Map.of("Content-Length", "12", "X-Quote", "say \"hi\"\n"), new byte[0]);
        assertEquals("{\"content-length\":\"12\",\"x-quote\":\"say \\\"hi\\\"\\n\"}", response.result());
    }

    @Test
    void statusesFrom400AreErrors() {
        assertFalse(response(HttpMethod.GET, 302, Map.of(), new byte[0]).isError());
        assertTrue(response(HttpMethod.GET, 404, Map.of(), new byte[0]).isError());
        assertEquals(
                "404 Not Found",
                response(HttpMethod.GET, 404, Map.of(), new byte[0]).statusLine());
        assertEquals("599", response(HttpMethod.GET, 599, Map.of(), new byte[0]).statusLine());
    }

    @Test
    void answersSurviveTheNetwork() {
        byte[] body = new byte[100_000];
        new java.util.Random(7).nextBytes(body);
        NetResponse response = response(HttpMethod.POST, 201, Map.of("Location", "/items/2"), body);
        NetResponse copy = NetResponse.fromBytes(response.toBytes());
        assertEquals(201, copy.status());
        assertEquals(response.headers(), copy.headers());
        assertArrayEquals(body, copy.body());
        assertEquals(HttpMethod.POST, copy.method());
    }

    @Test
    void brokenBytesAreNoAnswer() {
        assertEquals(
                NetError.REQUEST_FAILED,
                org.junit.jupiter.api.Assertions.assertThrows(
                                NetException.class, () -> NetResponse.fromBytes(new byte[] {0, 3, 'G'}))
                        .error());
    }
}
