package de.joernott.c0denet.core.mail;

import de.joernott.c0denet.core.http.Bytes;
import de.joernott.c0denet.core.http.NetError;
import de.joernott.c0denet.core.http.NetException;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.ArrayList;
import java.util.List;

/**
 * The answer to a {@link MailRequest}: what the function returns. Only the parts of its operation are filled.
 *
 * @param text the Message-ID of a message sent (SMTP_SEND)
 * @param names the folders (IMAP_LIST)
 * @param mailbox the status of a folder (IMAP_STATUS, IMAP_SELECT)
 * @param uids the UIDs found (IMAP_SEARCH)
 * @param messages the messages read (IMAP_FETCH)
 * @param count the messages removed (IMAP_EXPUNGE)
 */
public record MailAnswer(
        String text, List<String> names, Mailbox mailbox, List<Long> uids, List<Message> messages, long count) {

    /** A message of a folder, the struct MAIL. */
    public record Message(
            long uid,
            String from,
            List<String> to,
            List<String> cc,
            String subject,
            long date,
            String text,
            List<String> flags,
            long size) {
        public Message {
            to = List.copyOf(to);
            cc = List.copyOf(cc);
            flags = List.copyOf(flags);
        }
    }

    /** The status of a folder, the struct MAILBOX. */
    public record Mailbox(String name, long messages, long recent, long unseen, long uidNext, long uidValidity) {
        static final Mailbox NONE = new Mailbox("", 0, 0, 0, 0, 0);
    }

    public MailAnswer {
        names = List.copyOf(names);
        uids = List.copyOf(uids);
        messages = List.copyOf(messages);
    }

    /** An answer without content (IMAP_CREATE, IMAP_DELETE, IMAP_RENAME, IMAP_STORE, IMAP_MOVE). */
    public static MailAnswer empty() {
        return new MailAnswer("", List.of(), Mailbox.NONE, List.of(), List.of(), 0);
    }

    public static MailAnswer sent(String messageId) {
        return new MailAnswer(messageId, List.of(), Mailbox.NONE, List.of(), List.of(), 0);
    }

    public static MailAnswer folders(List<String> names) {
        return new MailAnswer("", names, Mailbox.NONE, List.of(), List.of(), 0);
    }

    public static MailAnswer of(Mailbox mailbox) {
        return new MailAnswer("", List.of(), mailbox, List.of(), List.of(), 0);
    }

    public static MailAnswer found(List<Long> uids) {
        return new MailAnswer("", List.of(), Mailbox.NONE, uids, List.of(), 0);
    }

    public static MailAnswer read(List<Message> messages) {
        return new MailAnswer("", List.of(), Mailbox.NONE, List.of(), messages, 0);
    }

    public static MailAnswer removed(long count) {
        return new MailAnswer("", List.of(), Mailbox.NONE, List.of(), List.of(), count);
    }

    /** The answer in the network format. */
    public byte[] toBytes() {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        try (DataOutputStream out = new DataOutputStream(bytes)) {
            Bytes.writeString(out, text);
            writeStrings(out, names);
            Bytes.writeString(out, mailbox.name());
            out.writeLong(mailbox.messages());
            out.writeLong(mailbox.recent());
            out.writeLong(mailbox.unseen());
            out.writeLong(mailbox.uidNext());
            out.writeLong(mailbox.uidValidity());
            out.writeInt(uids.size());
            for (long uid : uids) {
                out.writeLong(uid);
            }
            out.writeInt(messages.size());
            for (Message message : messages) {
                out.writeLong(message.uid());
                Bytes.writeString(out, message.from());
                writeStrings(out, message.to());
                writeStrings(out, message.cc());
                Bytes.writeString(out, message.subject());
                out.writeLong(message.date());
                Bytes.writeString(out, message.text());
                writeStrings(out, message.flags());
                out.writeLong(message.size());
            }
            out.writeLong(count);
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
    public static MailAnswer fromBytes(byte[] data) {
        try (DataInputStream in = new DataInputStream(new ByteArrayInputStream(data))) {
            String text = Bytes.readString(in);
            List<String> names = readStrings(in);
            Mailbox mailbox = new Mailbox(
                    Bytes.readString(in), in.readLong(), in.readLong(), in.readLong(), in.readLong(), in.readLong());
            List<Long> uids = new ArrayList<>();
            for (int count = in.readInt(), i = 0; i < count; i++) {
                uids.add(in.readLong());
            }
            List<Message> messages = new ArrayList<>();
            for (int count = in.readInt(), i = 0; i < count; i++) {
                messages.add(new Message(
                        in.readLong(),
                        Bytes.readString(in),
                        readStrings(in),
                        readStrings(in),
                        Bytes.readString(in),
                        in.readLong(),
                        Bytes.readString(in),
                        readStrings(in),
                        in.readLong()));
            }
            return new MailAnswer(text, names, mailbox, uids, messages, in.readLong());
        } catch (IOException e) {
            throw new NetException(NetError.REQUEST_FAILED, "The client sent an answer that can't be read");
        }
    }

    private static void writeStrings(DataOutputStream out, List<String> strings) throws IOException {
        out.writeInt(strings.size());
        for (String string : strings) {
            Bytes.writeString(out, string);
        }
    }

    private static List<String> readStrings(DataInputStream in) throws IOException {
        List<String> strings = new ArrayList<>();
        for (int count = in.readInt(), i = 0; i < count; i++) {
            strings.add(Bytes.readString(in));
        }
        return strings;
    }
}
