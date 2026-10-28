package de.joernott.c0denet.core.http;

/**
 * The errors of the network libraries (docs/concept.md#errors): INVALID_ARGUMENT of c0de and the codes from 1000 on.
 * The mod raises them as c0de errors with the same code and name.
 */
public enum NetError {
    /** The connection or the request is not valid. The code of c0de. */
    INVALID_ARGUMENT(309),
    /** The server answered with a status of 400 or higher. */
    HTTP_STATUS(1000),
    /** The player who runs the process is not online, or their client doesn't have the mod. */
    CLIENT_UNAVAILABLE(1001),
    /** The client of the player who runs the process doesn't send requests. */
    REQUESTS_DISABLED(1002),
    /** The request could not be sent or had no answer. */
    REQUEST_FAILED(1003),
    /** There was no complete answer within the timeout. */
    REQUEST_TIMEOUT(1004),
    /** The answer is larger than allowed. */
    RESPONSE_TOO_LARGE(1005);

    private final int code;

    NetError(int code) {
        this.code = code;
    }

    /** The error code that programs see. */
    public int code() {
        return code;
    }

    /** The error with a code, or null if no error has it. */
    public static NetError of(int code) {
        for (NetError error : values()) {
            if (error.code == code) {
                return error;
            }
        }
        return null;
    }
}
