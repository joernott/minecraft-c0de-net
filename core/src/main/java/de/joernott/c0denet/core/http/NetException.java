package de.joernott.c0denet.core.http;

/** An error of a request, with the error that programs see. */
public final class NetException extends RuntimeException {
    private final NetError error;

    public NetException(NetError error, String message) {
        super(message);
        this.error = error;
    }

    public NetException(NetError error, String message, Throwable cause) {
        super(message, cause);
        this.error = error;
    }

    public NetError error() {
        return error;
    }
}
