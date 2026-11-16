package de.joernott.c0denet.core.mail;

/** The operations of c0de_net/mail: one per function (docs/concept.md#the-library-c0de_netmail). */
public enum MailOperation {
    SEND("SMTP_SEND", false),
    LIST("IMAP_LIST", false),
    CREATE("IMAP_CREATE", false),
    DELETE("IMAP_DELETE", false),
    RENAME("IMAP_RENAME", false),
    STATUS("IMAP_STATUS", false),
    SELECT("IMAP_SELECT", false),
    SEARCH("IMAP_SEARCH", true),
    FETCH("IMAP_FETCH", true),
    STORE("IMAP_STORE", true),
    MOVE("IMAP_MOVE", true),
    EXPUNGE("IMAP_EXPUNGE", true);

    private final String function;
    private final boolean inFolder;

    MailOperation(String function, boolean inFolder) {
        this.function = function;
        this.inFolder = inFolder;
    }

    /** The name of the function, e.g. SMTP_SEND. */
    public String function() {
        return function;
    }

    /** Whether the operation works in the selected folder of the IMAP_SERVER. */
    public boolean inFolder() {
        return inFolder;
    }

    /** Whether the operation talks IMAP (all but SMTP_SEND). */
    public boolean imap() {
        return this != SEND;
    }
}
