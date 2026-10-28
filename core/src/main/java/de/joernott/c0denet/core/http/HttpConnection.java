package de.joernott.c0denet.core.http;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * The values of a struct HTTP_CONNECTION (docs/concept.md#http_connection): the base information of the requests
 * to one server. The values are those of the struct, including the defaults of their types (0, "", false).
 *
 * @param port 1 to 65535, or 0 for the default port of the protocol
 * @param headers the headers sent with every request, by name
 * @param timeout seconds, 1 to 300, or 0 for {@link #DEFAULT_TIMEOUT}
 */
public record HttpConnection(
        boolean useSsl,
        String server,
        long port,
        String baseEndpoint,
        String user,
        String password,
        boolean skipSslValidation,
        String proxy,
        Map<String, String> headers,
        long timeout) {
    /** The timeout of a connection whose timeout is 0, in seconds, as in lra. */
    public static final int DEFAULT_TIMEOUT = 60;

    /** The longest timeout, in seconds. */
    public static final int MAX_TIMEOUT = 300;

    public HttpConnection {
        headers = Map.copyOf(new LinkedHashMap<>(headers));
    }

    /**
     * Checks the values.
     *
     * @throws NetException INVALID_ARGUMENT for an empty server, a port outside of 0 to 65535, a timeout outside of 0
     *     to 300, a proxy that is not an {@code http://} URL or a header name or value that can't be sent
     */
    public void validate() {
        if (server.isBlank()) {
            throw invalid("The server of the connection is empty");
        }
        if (server.chars().anyMatch(c -> c <= ' ' || c == '/' || c == '?' || c == '#' || c == '@')) {
            throw invalid("The server must be a host name or IP address: " + server);
        }
        if (port < 0 || port > 65535) {
            throw invalid("The port must be 1 to 65535 (0 for the default port): " + port);
        }
        if (timeout < 0 || timeout > MAX_TIMEOUT) {
            throw invalid(
                    "The timeout must be 1 to " + MAX_TIMEOUT + " seconds (0 for " + DEFAULT_TIMEOUT + "): " + timeout);
        }
        if (!proxy.isEmpty()) {
            proxyUri();
        }
        headers.forEach((name, value) -> {
            if (name.isEmpty() || !name.chars().allMatch(HttpConnection::isTokenChar)) {
                throw invalid("Not a header name: " + name);
            }
            if (value.chars().anyMatch(c -> c == '\r' || c == '\n')) {
                throw invalid("The header " + name + " contains a line break");
            }
        });
    }

    /** The timeout in seconds, with the default for 0. */
    public int effectiveTimeout() {
        return timeout == 0 ? DEFAULT_TIMEOUT : (int) timeout;
    }

    /**
     * The URL of a request: {@code http[s]://server[:port]base_endpoint endpoint}. The endpoint is used as it is and
     * may contain a query.
     *
     * @throws NetException INVALID_ARGUMENT if the result is not a valid URL
     */
    public URI uri(String endpoint) {
        String host = server.contains(":") && !server.startsWith("[") ? "[" + server + "]" : server;
        String text =
                (useSsl ? "https" : "http") + "://" + host + (port == 0 ? "" : ":" + port) + baseEndpoint + endpoint;
        try {
            URI uri = new URI(text);
            if (uri.getHost() == null) {
                throw invalid("Not a valid URL: " + text);
            }
            return uri;
        } catch (URISyntaxException e) {
            throw invalid("Not a valid URL: " + text + " (" + e.getReason() + ")");
        }
    }

    /**
     * The URL of the proxy.
     *
     * @throws NetException INVALID_ARGUMENT if it is not an {@code http://} URL with a host and a port
     */
    public URI proxyUri() {
        try {
            URI uri = new URI(proxy);
            if (!"http".equals(uri.getScheme()) || uri.getHost() == null || uri.getPort() < 0) {
                throw invalid("The proxy must be an http:// URL with host and port, e.g. http://proxy:3128: " + proxy);
            }
            return uri;
        } catch (URISyntaxException e) {
            throw invalid("The proxy is not a valid URL: " + proxy);
        }
    }

    /** True for the characters of a header name (a token of RFC 9110). */
    private static boolean isTokenChar(int c) {
        return c > ' ' && c < 127 && "\"(),/:;<=>?@[\\]{}".indexOf(c) < 0;
    }

    private static NetException invalid(String message) {
        return new NetException(NetError.INVALID_ARGUMENT, message);
    }
}
