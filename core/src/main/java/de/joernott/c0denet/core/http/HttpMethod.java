package de.joernott.c0denet.core.http;

/** The HTTP methods of the library, one function each (HTTP_GET, …). CONNECT is left out (docs/concept.md). */
public enum HttpMethod {
    GET(false),
    HEAD(false),
    OPTIONS(false),
    TRACE(false),
    DELETE(true),
    PATCH(true),
    POST(true),
    PUT(true);

    private final boolean sendsData;

    HttpMethod(boolean sendsData) {
        this.sendsData = sendsData;
    }

    /** True if the function has the parameter {@code data}, which is sent as the body of the request. */
    public boolean sendsData() {
        return sendsData;
    }
}
