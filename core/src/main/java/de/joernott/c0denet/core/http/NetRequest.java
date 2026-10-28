package de.joernott.c0denet.core.http;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Everything a client needs to send a request (docs/concept.md#running-requests-on-the-client): the server builds
 * it from the connection and sends it to the client as bytes ({@link #toBytes}).
 *
 * @param headers the headers by name, including the {@code Authorization} header of basic authentication
 * @param body the data of the request, empty for the methods that send no data
 * @param timeout seconds
 * @param proxy the URL of the HTTP proxy, or null
 */
public record NetRequest(
        HttpMethod method,
        URI uri,
        Map<String, String> headers,
        String body,
        int timeout,
        URI proxy,
        boolean skipSslValidation) {
    public NetRequest {
        headers = Map.copyOf(new LinkedHashMap<>(headers));
    }

    /**
     * The request of a function: the URL of the connection and the endpoint, the headers of the connection and, for
     * a user, basic authentication (unless the headers have an {@code Authorization} header of their own).
     *
     * @param data the data for the methods that send data; ignored for the others
     * @throws NetException INVALID_ARGUMENT if the connection or the endpoint is not valid
     */
    public static NetRequest of(HttpConnection connection, HttpMethod method, String endpoint, String data) {
        connection.validate();
        Map<String, String> headers = new LinkedHashMap<>(connection.headers());
        boolean hasAuthorization = headers.keySet().stream()
                .anyMatch(name -> name.toLowerCase(Locale.ROOT).equals("authorization"));
        if (!connection.user().isEmpty() && !hasAuthorization) {
            String credentials = connection.user() + ":" + connection.password();
            headers.put(
                    "Authorization",
                    "Basic " + Base64.getEncoder().encodeToString(credentials.getBytes(StandardCharsets.UTF_8)));
        }
        return new NetRequest(
                method,
                connection.uri(endpoint),
                headers,
                method.sendsData() ? data : "",
                connection.effectiveTimeout(),
                connection.proxy().isEmpty() ? null : connection.proxyUri(),
                connection.skipSslValidation());
    }

    /** The request as bytes, for the network. */
    public byte[] toBytes() {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        try (DataOutputStream out = new DataOutputStream(bytes)) {
            out.writeUTF(method.name());
            Bytes.writeString(out, uri.toString());
            out.writeInt(headers.size());
            for (Map.Entry<String, String> header : headers.entrySet()) {
                Bytes.writeString(out, header.getKey());
                Bytes.writeString(out, header.getValue());
            }
            Bytes.writeString(out, body);
            out.writeInt(timeout);
            Bytes.writeString(out, proxy == null ? "" : proxy.toString());
            out.writeBoolean(skipSslValidation);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        return bytes.toByteArray();
    }

    /**
     * A request read from bytes of {@link #toBytes}.
     *
     * @throws NetException INVALID_ARGUMENT if the bytes are not a request
     */
    public static NetRequest fromBytes(byte[] data) {
        try (DataInputStream in = new DataInputStream(new ByteArrayInputStream(data))) {
            HttpMethod method = HttpMethod.valueOf(in.readUTF());
            URI uri = URI.create(Bytes.readString(in));
            int count = in.readInt();
            Map<String, String> headers = new LinkedHashMap<>();
            for (int i = 0; i < count; i++) {
                headers.put(Bytes.readString(in), Bytes.readString(in));
            }
            String body = Bytes.readString(in);
            int timeout = in.readInt();
            String proxy = Bytes.readString(in);
            boolean skipSslValidation = in.readBoolean();
            return new NetRequest(
                    method, uri, headers, body, timeout, proxy.isEmpty() ? null : URI.create(proxy), skipSslValidation);
        } catch (IOException | IllegalArgumentException e) {
            throw new NetException(NetError.INVALID_ARGUMENT, "Not a request: " + e.getMessage(), e);
        }
    }
}
