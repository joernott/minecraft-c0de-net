package de.joernott.c0denet.core.mail;

import de.joernott.c0denet.core.http.NetError;
import de.joernott.c0denet.core.http.NetException;
import java.util.Locale;

/**
 * The connection to a mail server (docs/concept.md#the-library-c0de_netmail): the values of a struct SMTP_SERVER or
 * IMAP_SERVER, checked, with the defaults filled in.
 *
 * @param imap true for IMAP, false for SMTP
 * @param security {@code tls}, {@code starttls} or {@code none}
 * @param from the sender (SMTP), "" for IMAP
 * @param folder the selected folder (IMAP), "" if none
 * @param timeout seconds
 */
public record MailServer(
        boolean imap,
        String server,
        int port,
        String security,
        String user,
        String password,
        String from,
        String folder,
        int timeout,
        boolean skipSslValidation) {

    public static final String TLS = "tls";
    public static final String STARTTLS = "starttls";
    public static final String NONE = "none";

    /**
     * The connection of the values of a struct, with the defaults filled in.
     *
     * @param maxTimeout the default and the largest timeout (mailTimeout)
     * @throws NetException INVALID_ARGUMENT if a value is not valid
     */
    public static MailServer of(
            boolean imap,
            String server,
            long port,
            String security,
            String user,
            String password,
            String from,
            String folder,
            long timeout,
            boolean skipSslValidation,
            int maxTimeout) {
        String type = imap ? "IMAP_SERVER" : "SMTP_SERVER";
        if (server.isBlank() || server.contains(" ") || server.contains("/")) {
            throw invalid("The server of the " + type + " is not a host name: \"" + server + "\"");
        }
        String mode = security.isBlank()
                ? (imap ? TLS : STARTTLS)
                : security.toLowerCase(Locale.ROOT).strip();
        if (!mode.equals(TLS) && !mode.equals(STARTTLS) && !mode.equals(NONE)) {
            throw invalid("security is tls, starttls or none, not \"" + security + "\"");
        }
        int resolvedPort;
        if (port == 0) {
            resolvedPort = imap ? (mode.equals(TLS) ? 993 : 143) : (mode.equals(TLS) ? 465 : 587);
        } else if (port < 1 || port > 65535) {
            throw invalid("The port is 1 to 65535, not " + port);
        } else {
            resolvedPort = (int) port;
        }
        int resolvedTimeout;
        if (timeout == 0) {
            resolvedTimeout = maxTimeout;
        } else if (timeout < 1 || timeout > maxTimeout) {
            throw invalid("The timeout is 1 to " + maxTimeout + " seconds, not " + timeout);
        } else {
            resolvedTimeout = (int) timeout;
        }
        if (!imap && from.isBlank()) {
            throw invalid("The SMTP_SERVER has no sender (from)");
        }
        if (imap && user.isBlank()) {
            throw invalid("The IMAP_SERVER has no user");
        }
        return new MailServer(
                imap,
                server.strip(),
                resolvedPort,
                mode,
                user,
                password,
                from,
                folder,
                resolvedTimeout,
                skipSslValidation);
    }

    private static NetException invalid(String message) {
        return new NetException(NetError.INVALID_ARGUMENT, message);
    }
}
