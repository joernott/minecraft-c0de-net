package de.joernott.c0denet.core.http;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * The answer to a request (docs/concept.md#functions): the client sends it to the server as bytes ({@link #toBytes});
 * the server turns it into the results of the function.
 *
 * @param method the method of the request
 * @param status the HTTP status code
 * @param headers the headers with lower-case names; repeated headers are joined with ", "
 * @param body the raw bytes of the body
 */
public record NetResponse(HttpMethod method, int status, Map<String, String> headers, byte[] body) {
    private static final Pattern CHARSET = Pattern.compile("(?i)charset\\s*=\\s*\"?([^\";\\s]+)\"?");

    /** The reason phrases of the status codes (RFC 9110 and common extensions), for the message of HTTP_STATUS. */
    private static final Map<Integer, String> REASONS = Map.ofEntries(
            Map.entry(400, "Bad Request"),
            Map.entry(401, "Unauthorized"),
            Map.entry(402, "Payment Required"),
            Map.entry(403, "Forbidden"),
            Map.entry(404, "Not Found"),
            Map.entry(405, "Method Not Allowed"),
            Map.entry(406, "Not Acceptable"),
            Map.entry(407, "Proxy Authentication Required"),
            Map.entry(408, "Request Timeout"),
            Map.entry(409, "Conflict"),
            Map.entry(410, "Gone"),
            Map.entry(411, "Length Required"),
            Map.entry(412, "Precondition Failed"),
            Map.entry(413, "Content Too Large"),
            Map.entry(414, "URI Too Long"),
            Map.entry(415, "Unsupported Media Type"),
            Map.entry(416, "Range Not Satisfiable"),
            Map.entry(417, "Expectation Failed"),
            Map.entry(418, "I'm a teapot"),
            Map.entry(421, "Misdirected Request"),
            Map.entry(422, "Unprocessable Content"),
            Map.entry(423, "Locked"),
            Map.entry(424, "Failed Dependency"),
            Map.entry(425, "Too Early"),
            Map.entry(426, "Upgrade Required"),
            Map.entry(428, "Precondition Required"),
            Map.entry(429, "Too Many Requests"),
            Map.entry(431, "Request Header Fields Too Large"),
            Map.entry(451, "Unavailable For Legal Reasons"),
            Map.entry(500, "Internal Server Error"),
            Map.entry(501, "Not Implemented"),
            Map.entry(502, "Bad Gateway"),
            Map.entry(503, "Service Unavailable"),
            Map.entry(504, "Gateway Timeout"),
            Map.entry(505, "HTTP Version Not Supported"),
            Map.entry(507, "Insufficient Storage"),
            Map.entry(511, "Network Authentication Required"));

    public NetResponse {
        TreeMap<String, String> sorted = new TreeMap<>();
        headers.forEach((name, value) -> sorted.put(name.toLowerCase(Locale.ROOT), value));
        headers = java.util.Collections.unmodifiableMap(sorted);
        body = body.clone();
    }

    @Override
    public byte[] body() {
        return body.clone();
    }

    /** True for a status of 400 or higher, which raises HTTP_STATUS. */
    public boolean isError() {
        return status >= 400;
    }

    /** The status code and its reason phrase, e.g. {@code 404 Not Found}; the message of HTTP_STATUS. */
    public String statusLine() {
        String reason = REASONS.get(status);
        return reason == null ? String.valueOf(status) : status + " " + reason;
    }

    /**
     * The result {@code result}: the body decoded with the charset of the {@code Content-Type} header (UTF-8 if there
     * is none or it is not known; invalid bytes become U+FFFD). For HEAD, the headers as a JSON object, as in lra.
     */
    public String result() {
        return method == HttpMethod.HEAD ? headersAsJson() : new String(body, charset());
    }

    /** The charset of the body, from the {@code Content-Type} header. */
    Charset charset() {
        String type = headers.get("content-type");
        if (type != null) {
            Matcher matcher = CHARSET.matcher(type);
            if (matcher.find()) {
                try {
                    return Charset.forName(matcher.group(1));
                } catch (IllegalArgumentException e) {
                    // An unknown or illegal charset: UTF-8
                }
            }
        }
        return StandardCharsets.UTF_8;
    }

    /** The headers as a JSON object with the names in alphabetical order. */
    String headersAsJson() {
        StringBuilder json = new StringBuilder("{");
        headers.forEach((name, value) -> {
            if (json.length() > 1) {
                json.append(',');
            }
            appendJsonString(json, name);
            json.append(':');
            appendJsonString(json, value);
        });
        return json.append('}').toString();
    }

    private static void appendJsonString(StringBuilder json, String value) {
        json.append('"');
        for (int i = 0; i < value.length(); i++) {
            char c = value.charAt(i);
            switch (c) {
                case '"' -> json.append("\\\"");
                case '\\' -> json.append("\\\\");
                case '\n' -> json.append("\\n");
                case '\r' -> json.append("\\r");
                case '\t' -> json.append("\\t");
                default -> {
                    if (c < ' ') {
                        json.append(String.format("\\u%04x", (int) c));
                    } else {
                        json.append(c);
                    }
                }
            }
        }
        json.append('"');
    }

    /** The answer as bytes, for the network. */
    public byte[] toBytes() {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        try (DataOutputStream out = new DataOutputStream(bytes)) {
            out.writeUTF(method.name());
            out.writeInt(status);
            out.writeInt(headers.size());
            for (Map.Entry<String, String> header : headers.entrySet()) {
                Bytes.writeString(out, header.getKey());
                Bytes.writeString(out, header.getValue());
            }
            Bytes.writeBytes(out, body);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        return bytes.toByteArray();
    }

    /**
     * An answer read from bytes of {@link #toBytes}.
     *
     * @throws NetException REQUEST_FAILED if the bytes are not an answer
     */
    public static NetResponse fromBytes(byte[] data) {
        try (DataInputStream in = new DataInputStream(new ByteArrayInputStream(data))) {
            HttpMethod method = HttpMethod.valueOf(in.readUTF());
            int status = in.readInt();
            int count = in.readInt();
            Map<String, String> headers = new TreeMap<>();
            for (int i = 0; i < count; i++) {
                headers.put(Bytes.readString(in), Bytes.readString(in));
            }
            return new NetResponse(method, status, headers, Bytes.readBytes(in));
        } catch (IOException | IllegalArgumentException e) {
            throw new NetException(NetError.REQUEST_FAILED, "The client sent an invalid answer: " + e.getMessage(), e);
        }
    }
}
