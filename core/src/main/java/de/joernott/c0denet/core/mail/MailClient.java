package de.joernott.c0denet.core.mail;

import de.joernott.c0denet.core.http.NetError;
import de.joernott.c0denet.core.http.NetException;
import jakarta.mail.AuthenticationFailedException;
import jakarta.mail.Flags;
import jakarta.mail.Folder;
import jakarta.mail.FolderNotFoundException;
import jakarta.mail.Message;
import jakarta.mail.MessagingException;
import jakarta.mail.Multipart;
import jakarta.mail.Part;
import jakarta.mail.SendFailedException;
import jakarta.mail.Session;
import jakarta.mail.Store;
import jakarta.mail.Transport;
import jakarta.mail.UIDFolder;
import jakarta.mail.internet.AddressException;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import jakarta.mail.search.SearchTerm;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InterruptedIOException;
import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import javax.net.ssl.SSLException;

/**
 * Runs the calls of c0de_net/mail with Angus Mail (docs/concept.md#the-library-c0de_netmail): every call opens a
 * connection of its own, logs in, does its work and logs out. The client of the player runs it in a thread of its
 * own; the program runner calls it directly.
 */
public final class MailClient {
    private static final Map<String, Flags.Flag> SYSTEM_FLAGS = Map.of(
            "\\SEEN", Flags.Flag.SEEN,
            "\\ANSWERED", Flags.Flag.ANSWERED,
            "\\FLAGGED", Flags.Flag.FLAGGED,
            "\\DELETED", Flags.Flag.DELETED,
            "\\DRAFT", Flags.Flag.DRAFT);

    private MailClient() {}

    /**
     * Runs a call.
     *
     * @throws NetException the error the program sees
     */
    public static MailAnswer run(MailRequest request) {
        // Jakarta Mail finds its protocols and the handlers of text through the context class loader, which is not
        // the one of the mod in the game
        Thread thread = Thread.currentThread();
        ClassLoader previous = thread.getContextClassLoader();
        thread.setContextClassLoader(MailClient.class.getClassLoader());
        try {
            return request.operation() == MailOperation.SEND ? send(request) : imap(request);
        } catch (MessagingException e) {
            throw translate(e, request);
        } catch (IOException e) {
            throw translate(e);
        } finally {
            thread.setContextClassLoader(previous);
        }
    }

    // ------------------------------------------------------------------ SMTP

    private static MailAnswer send(MailRequest request) throws MessagingException, IOException {
        MailServer server = request.server();
        List<InternetAddress> to = addresses(request.list("to"), "to");
        List<InternetAddress> cc = addresses(request.list("cc"), "cc");
        List<InternetAddress> bcc = addresses(request.list("bcc"), "bcc");
        int recipients = to.size() + cc.size() + bcc.size();
        if (recipients == 0) {
            throw new NetException(NetError.INVALID_ARGUMENT, "SMTP_SEND needs at least one recipient");
        }
        if (recipients > request.limits().maxRecipients()) {
            throw new NetException(
                    NetError.MAIL_LIMIT,
                    recipients + " recipients are more than " + request.limits().maxRecipients() + " (maxRecipients)");
        }
        Session session = session(server, "smtp");
        MimeMessage message = new MimeMessage(session);
        message.setFrom(address(server.from(), "from"));
        message.setRecipients(Message.RecipientType.TO, to.toArray(new InternetAddress[0]));
        message.setRecipients(Message.RecipientType.CC, cc.toArray(new InternetAddress[0]));
        message.setRecipients(Message.RecipientType.BCC, bcc.toArray(new InternetAddress[0]));
        if (!request.text("reply_to").isEmpty()) {
            message.setReplyTo(new InternetAddress[] {address(request.text("reply_to"), "reply_to")});
        }
        message.setSubject(request.text("subject"), "UTF-8");
        message.setText(request.text("text"), "UTF-8");
        message.setSentDate(new Date());
        message.saveChanges();
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        message.writeTo(bytes);
        if (bytes.size() > request.limits().maxMailSize()) {
            throw new NetException(
                    NetError.MAIL_LIMIT,
                    "The message has " + bytes.size() + " bytes, more than "
                            + request.limits().maxMailSize() + " (maxMailSize)");
        }
        try (Transport transport = session.getTransport("smtp")) {
            if (server.user().isEmpty()) {
                transport.connect();
            } else {
                transport.connect(server.server(), server.port(), server.user(), server.password());
            }
            transport.sendMessage(message, message.getAllRecipients());
        }
        return MailAnswer.sent(message.getMessageID() == null ? "" : message.getMessageID());
    }

    private static List<InternetAddress> addresses(List<String> texts, String what) {
        List<InternetAddress> addresses = new ArrayList<>();
        for (String text : texts) {
            if (!text.isBlank()) {
                addresses.add(address(text, what));
            }
        }
        return addresses;
    }

    private static InternetAddress address(String text, String what) {
        try {
            InternetAddress address = new InternetAddress(text.strip(), true);
            address.validate();
            return address;
        } catch (AddressException e) {
            throw new NetException(NetError.INVALID_ARGUMENT, "Not a mail address (" + what + "): " + text);
        }
    }

    // ------------------------------------------------------------------ IMAP

    private static MailAnswer imap(MailRequest request) throws MessagingException, IOException {
        MailServer server = request.server();
        if (request.operation().inFolder() && server.folder().isEmpty()) {
            throw new NetException(
                    NetError.INVALID_ARGUMENT,
                    request.operation().function() + " needs a selected folder: call IMAP_SELECT first");
        }
        Session session = session(server, "imap");
        try (Store store = session.getStore("imap")) {
            store.connect(server.server(), server.port(), server.user(), server.password());
            return switch (request.operation()) {
                case LIST -> list(store, request.text("pattern").isEmpty() ? "*" : request.text("pattern"));
                case CREATE -> create(store, request.text("folder"));
                case DELETE -> delete(store, request.text("folder"));
                case RENAME -> rename(store, request.text("folder"), request.text("new_name"));
                case STATUS -> MailAnswer.of(status(existing(store, request.text("folder"))));
                case SELECT -> MailAnswer.of(status(existing(store, request.text("folder"))));
                case SEARCH -> search(existing(store, server.folder()), request);
                case FETCH -> fetch(existing(store, server.folder()), request);
                case STORE -> store(existing(store, server.folder()), request);
                case MOVE -> move(store, existing(store, server.folder()), request);
                case EXPUNGE -> expunge(existing(store, server.folder()));
                case SEND -> throw new IllegalStateException("SMTP_SEND is not IMAP");
            };
        }
    }

    private static Folder existing(Store store, String name) throws MessagingException {
        Folder folder = store.getFolder(name);
        if (!folder.exists()) {
            throw new NetException(NetError.MAIL_REJECTED, "The folder " + name + " doesn't exist");
        }
        return folder;
    }

    private static MailAnswer list(Store store, String pattern) throws MessagingException {
        List<String> names = new ArrayList<>();
        for (Folder folder : store.getDefaultFolder().list(pattern)) {
            names.add(folder.getFullName());
        }
        return MailAnswer.folders(names);
    }

    private static MailAnswer create(Store store, String name) throws MessagingException {
        if (name.isBlank()) {
            throw new NetException(NetError.INVALID_ARGUMENT, "IMAP_CREATE needs the name of the folder");
        }
        Folder folder = store.getFolder(name);
        if (folder.exists()) {
            throw new NetException(NetError.MAIL_REJECTED, "The folder " + name + " exists already");
        }
        if (!folder.create(Folder.HOLDS_MESSAGES)) {
            throw new NetException(NetError.MAIL_REJECTED, "The server didn't create the folder " + name);
        }
        return MailAnswer.empty();
    }

    private static MailAnswer delete(Store store, String name) throws MessagingException {
        Folder folder = existing(store, name);
        if (!folder.delete(false)) {
            throw new NetException(NetError.MAIL_REJECTED, "The server didn't delete the folder " + name);
        }
        return MailAnswer.empty();
    }

    private static MailAnswer rename(Store store, String name, String newName) throws MessagingException {
        Folder folder = existing(store, name);
        if (newName.isBlank()) {
            throw new NetException(NetError.INVALID_ARGUMENT, "IMAP_RENAME needs the new name of the folder");
        }
        Folder target = store.getFolder(newName);
        if (target.exists()) {
            throw new NetException(NetError.MAIL_REJECTED, "The folder " + newName + " exists already");
        }
        if (!folder.renameTo(target)) {
            throw new NetException(NetError.MAIL_REJECTED, "The server didn't rename the folder " + name);
        }
        return MailAnswer.empty();
    }

    private static MailAnswer.Mailbox status(Folder folder) throws MessagingException {
        UIDFolder uids = (UIDFolder) folder;
        return new MailAnswer.Mailbox(
                folder.getFullName(),
                folder.getMessageCount(),
                folder.getNewMessageCount(),
                folder.getUnreadMessageCount(),
                uids.getUIDNext(),
                uids.getUIDValidity());
    }

    private static MailAnswer search(Folder folder, MailRequest request) throws MessagingException {
        SearchTerm term = MailSearch.parse(request.text("criteria"));
        folder.open(Folder.READ_ONLY);
        try {
            Message[] found = term == null ? folder.getMessages() : folder.search(term);
            List<Long> uids = new ArrayList<>();
            for (Message message : found) {
                uids.add(((UIDFolder) folder).getUID(message));
            }
            uids.sort(null);
            int max = request.limits().maxFetchMessages();
            return MailAnswer.found(uids.size() > max ? uids.subList(uids.size() - max, uids.size()) : uids);
        } finally {
            folder.close(false);
        }
    }

    private static MailAnswer fetch(Folder folder, MailRequest request) throws MessagingException, IOException {
        if (request.uids().size() > request.limits().maxFetchMessages()) {
            throw new NetException(
                    NetError.RESPONSE_TOO_LARGE,
                    request.uids().size() + " messages are more than "
                            + request.limits().maxFetchMessages() + " (maxFetchMessages)");
        }
        // Reading the text marks a message as seen, like a mail program does
        folder.open(request.withText() ? Folder.READ_WRITE : Folder.READ_ONLY);
        try {
            Message[] messages = ((UIDFolder) folder).getMessagesByUID(uidArray(request.uids()));
            List<MailAnswer.Message> result = new ArrayList<>();
            long bytes = 0;
            for (Message message : messages) {
                if (message == null) {
                    continue;
                }
                String text = request.withText() ? plainText(message) : "";
                bytes += text.getBytes(StandardCharsets.UTF_8).length + 512;
                if (bytes > request.limits().maxFetchSize()) {
                    throw new NetException(
                            NetError.RESPONSE_TOO_LARGE,
                            "The messages are larger than " + request.limits().maxFetchSize()
                                    + " bytes (maxFetchSize)");
                }
                Date date = message.getSentDate() != null ? message.getSentDate() : message.getReceivedDate();
                result.add(new MailAnswer.Message(
                        ((UIDFolder) folder).getUID(message),
                        addressText(message.getFrom()),
                        addressTexts(message.getRecipients(Message.RecipientType.TO)),
                        addressTexts(message.getRecipients(Message.RecipientType.CC)),
                        message.getSubject() == null ? "" : message.getSubject(),
                        date == null ? 0 : date.getTime(),
                        text,
                        flags(message.getFlags()),
                        Math.max(0, message.getSize())));
            }
            return MailAnswer.read(result);
        } finally {
            folder.close(false);
        }
    }

    private static MailAnswer store(Folder folder, MailRequest request) throws MessagingException {
        String mode =
                request.text("mode").isEmpty() ? "add" : request.text("mode").toLowerCase(Locale.ROOT);
        if (!mode.equals("add") && !mode.equals("remove") && !mode.equals("set")) {
            throw new NetException(
                    NetError.INVALID_ARGUMENT, "mode is add, remove or set, not " + request.text("mode"));
        }
        Flags flags = flagsOf(request.list("flags"));
        folder.open(Folder.READ_WRITE);
        try {
            for (Message message : ((UIDFolder) folder).getMessagesByUID(uidArray(request.uids()))) {
                if (message == null) {
                    continue;
                }
                if (mode.equals("set")) {
                    Flags current = message.getFlags();
                    current.remove(Flags.Flag.RECENT);
                    message.setFlags(current, false);
                }
                message.setFlags(flags, !mode.equals("remove"));
            }
            return MailAnswer.empty();
        } finally {
            folder.close(false);
        }
    }

    private static MailAnswer move(Store store, Folder folder, MailRequest request) throws MessagingException {
        Folder target = existing(store, request.text("folder"));
        folder.open(Folder.READ_WRITE);
        try {
            Message[] messages = Arrays.stream(((UIDFolder) folder).getMessagesByUID(uidArray(request.uids())))
                    .filter(java.util.Objects::nonNull)
                    .toArray(Message[]::new);
            if (messages.length > 0) {
                if (folder instanceof org.eclipse.angus.mail.imap.IMAPFolder imapFolder) {
                    imapFolder.moveMessages(messages, target);
                } else {
                    folder.copyMessages(messages, target);
                    folder.setFlags(messages, new Flags(Flags.Flag.DELETED), true);
                    folder.expunge();
                }
            }
            return MailAnswer.empty();
        } finally {
            folder.close(false);
        }
    }

    private static MailAnswer expunge(Folder folder) throws MessagingException {
        folder.open(Folder.READ_WRITE);
        try {
            return MailAnswer.removed(folder.expunge().length);
        } finally {
            folder.close(false);
        }
    }

    /** The plain text of a message: its text/plain part, "" if it has none. */
    private static String plainText(Part part) throws MessagingException, IOException {
        if (part.isMimeType("text/plain")) {
            Object content = part.getContent();
            return content instanceof String text ? text : "";
        }
        if (part.isMimeType("multipart/*") && part.getContent() instanceof Multipart multipart) {
            for (int i = 0; i < multipart.getCount(); i++) {
                String text = plainText(multipart.getBodyPart(i));
                if (!text.isEmpty()) {
                    return text;
                }
            }
        }
        return "";
    }

    private static long[] uidArray(List<Long> uids) {
        return uids.stream().mapToLong(Long::longValue).toArray();
    }

    private static Flags flagsOf(List<String> names) {
        Flags flags = new Flags();
        for (String name : names) {
            if (name.isBlank()) {
                continue;
            }
            Flags.Flag system = SYSTEM_FLAGS.get(name.strip().toUpperCase(Locale.ROOT));
            if (system != null) {
                flags.add(system);
            } else if (name.startsWith("\\")) {
                throw new NetException(NetError.INVALID_ARGUMENT, "Unknown flag " + name);
            } else {
                flags.add(name.strip());
            }
        }
        return flags;
    }

    private static List<String> flags(Flags flags) {
        List<String> names = new ArrayList<>();
        for (Flags.Flag flag : flags.getSystemFlags()) {
            if (flag == Flags.Flag.SEEN) {
                names.add("\\Seen");
            } else if (flag == Flags.Flag.ANSWERED) {
                names.add("\\Answered");
            } else if (flag == Flags.Flag.FLAGGED) {
                names.add("\\Flagged");
            } else if (flag == Flags.Flag.DELETED) {
                names.add("\\Deleted");
            } else if (flag == Flags.Flag.DRAFT) {
                names.add("\\Draft");
            }
        }
        names.addAll(Arrays.asList(flags.getUserFlags()));
        return names;
    }

    private static String addressText(jakarta.mail.Address[] addresses) {
        return addresses == null || addresses.length == 0 ? "" : unicode(addresses[0]);
    }

    private static List<String> addressTexts(jakarta.mail.Address[] addresses) {
        List<String> texts = new ArrayList<>();
        if (addresses != null) {
            for (jakarta.mail.Address address : addresses) {
                texts.add(unicode(address));
            }
        }
        return texts;
    }

    private static String unicode(jakarta.mail.Address address) {
        return address instanceof InternetAddress internet ? internet.toUnicodeString() : address.toString();
    }

    // ------------------------------------------------------------------ connection

    private static Session session(MailServer server, String protocol) {
        Properties properties = new Properties();
        String prefix = "mail." + protocol + ".";
        properties.put(prefix + "host", server.server());
        properties.put(prefix + "port", String.valueOf(server.port()));
        String millis = String.valueOf(server.timeout() * 1000L);
        properties.put(prefix + "connectiontimeout", millis);
        properties.put(prefix + "timeout", millis);
        properties.put(prefix + "writetimeout", millis);
        if (server.security().equals(MailServer.TLS)) {
            properties.put(prefix + "ssl.enable", "true");
        } else if (server.security().equals(MailServer.STARTTLS)) {
            properties.put(prefix + "starttls.enable", "true");
            properties.put(prefix + "starttls.required", "true");
        }
        if (server.skipSslValidation()) {
            properties.put(prefix + "ssl.trust", "*");
            properties.put(prefix + "ssl.checkserveridentity", "false");
        } else {
            properties.put(prefix + "ssl.checkserveridentity", "true");
        }
        if (protocol.equals("smtp")) {
            properties.put("mail.smtp.auth", String.valueOf(!server.user().isEmpty()));
        }
        return Session.getInstance(properties);
    }

    // ------------------------------------------------------------------ errors

    private static NetException translate(MessagingException e, MailRequest request) {
        if (e instanceof AuthenticationFailedException) {
            return new NetException(
                    NetError.AUTHENTICATION_FAILED, "The server didn't accept the user or the password", e);
        }
        if (e instanceof FolderNotFoundException) {
            return new NetException(NetError.MAIL_REJECTED, "The folder doesn't exist: " + e.getMessage(), e);
        }
        if (e instanceof SendFailedException) {
            return new NetException(NetError.MAIL_REJECTED, "The server didn't accept the message: " + reason(e), e);
        }
        for (Throwable cause = e; cause != null; cause = cause.getCause()) {
            if (cause instanceof SocketTimeoutException || cause instanceof InterruptedIOException) {
                return new NetException(
                        NetError.REQUEST_TIMEOUT,
                        "No answer from " + request.server().server() + " within "
                                + request.server().timeout() + " seconds",
                        e);
            }
            if (cause instanceof UnknownHostException
                    || cause instanceof ConnectException
                    || cause instanceof SSLException
                    || cause instanceof org.eclipse.angus.mail.util.MailConnectException) {
                return new NetException(
                        NetError.REQUEST_FAILED,
                        "No connection to " + request.server().server() + ":"
                                + request.server().port() + ": " + reason(cause),
                        e);
            }
        }
        return new NetException(NetError.MAIL_REJECTED, "The server refused: " + reason(e), e);
    }

    private static NetException translate(IOException e) {
        return new NetException(NetError.REQUEST_FAILED, "The message can't be read: " + reason(e), e);
    }

    private static String reason(Throwable e) {
        String message = e.getMessage();
        return message == null || message.isBlank() ? e.getClass().getSimpleName() : message.strip();
    }
}
