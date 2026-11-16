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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * A call of a function of c0de_net/mail as the server sends it to the client of the player (or the program runner
 * runs it): the operation, the connection, the arguments and the limits of the server.
 *
 * @param texts the STRING arguments by name, e.g. subject, text, folder, criteria
 * @param lists the ARRAY STRING arguments by name, e.g. to, cc, bcc, flags
 * @param uids the UIDs of the messages (IMAP_FETCH, IMAP_STORE, IMAP_MOVE)
 * @param withText whether IMAP_FETCH reads the text of the messages
 */
public record MailRequest(
        MailOperation operation,
        MailServer server,
        Map<String, String> texts,
        Map<String, List<String>> lists,
        List<Long> uids,
        boolean withText,
        Limits limits) {

    /** The first string of the bytes of a mail request; requests of c0de_net/http start with their HTTP method. */
    public static final String MARKER = "c0de_net/mail";

    /** The limits of the server that the client applies. */
    public record Limits(int maxMailSize, int maxRecipients, int maxFetchSize, int maxFetchMessages) {}

    public MailRequest {
        texts = Map.copyOf(texts);
        lists = Map.copyOf(lists);
        uids = List.copyOf(uids);
    }

    /** A STRING argument, "" if it has none. */
    public String text(String name) {
        return texts.getOrDefault(name, "");
    }

    /** An ARRAY STRING argument, empty if it has none. */
    public List<String> list(String name) {
        return lists.getOrDefault(name, List.of());
    }

    /** Whether bytes are a mail request (and not an HTTP request). */
    public static boolean isMail(byte[] data) {
        try (DataInputStream in = new DataInputStream(new ByteArrayInputStream(data))) {
            return in.readUTF().equals(MARKER);
        } catch (IOException e) {
            return false;
        }
    }

    /** The request in the network format. */
    public byte[] toBytes() {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        try (DataOutputStream out = new DataOutputStream(bytes)) {
            out.writeUTF(MARKER);
            out.writeUTF(operation.name());
            out.writeBoolean(server.imap());
            Bytes.writeString(out, server.server());
            out.writeInt(server.port());
            out.writeUTF(server.security());
            Bytes.writeString(out, server.user());
            Bytes.writeString(out, server.password());
            Bytes.writeString(out, server.from());
            Bytes.writeString(out, server.folder());
            out.writeInt(server.timeout());
            out.writeBoolean(server.skipSslValidation());
            out.writeInt(texts.size());
            for (Map.Entry<String, String> text : texts.entrySet()) {
                out.writeUTF(text.getKey());
                Bytes.writeString(out, text.getValue());
            }
            out.writeInt(lists.size());
            for (Map.Entry<String, List<String>> list : lists.entrySet()) {
                out.writeUTF(list.getKey());
                out.writeInt(list.getValue().size());
                for (String element : list.getValue()) {
                    Bytes.writeString(out, element);
                }
            }
            out.writeInt(uids.size());
            for (long uid : uids) {
                out.writeLong(uid);
            }
            out.writeBoolean(withText);
            out.writeInt(limits.maxMailSize());
            out.writeInt(limits.maxRecipients());
            out.writeInt(limits.maxFetchSize());
            out.writeInt(limits.maxFetchMessages());
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        return bytes.toByteArray();
    }

    /**
     * A request read from bytes of {@link #toBytes}.
     *
     * @throws NetException INVALID_ARGUMENT if the bytes are not a mail request
     */
    public static MailRequest fromBytes(byte[] data) {
        try (DataInputStream in = new DataInputStream(new ByteArrayInputStream(data))) {
            if (!in.readUTF().equals(MARKER)) {
                throw new IOException("Not a mail request");
            }
            MailOperation operation = MailOperation.valueOf(in.readUTF());
            MailServer server = new MailServer(
                    in.readBoolean(),
                    Bytes.readString(in),
                    in.readInt(),
                    in.readUTF(),
                    Bytes.readString(in),
                    Bytes.readString(in),
                    Bytes.readString(in),
                    Bytes.readString(in),
                    in.readInt(),
                    in.readBoolean());
            Map<String, String> texts = new LinkedHashMap<>();
            for (int count = in.readInt(), i = 0; i < count; i++) {
                texts.put(in.readUTF(), Bytes.readString(in));
            }
            Map<String, List<String>> lists = new LinkedHashMap<>();
            for (int count = in.readInt(), i = 0; i < count; i++) {
                String name = in.readUTF();
                List<String> elements = new ArrayList<>();
                for (int size = in.readInt(), j = 0; j < size; j++) {
                    elements.add(Bytes.readString(in));
                }
                lists.put(name, elements);
            }
            List<Long> uids = new ArrayList<>();
            for (int count = in.readInt(), i = 0; i < count; i++) {
                uids.add(in.readLong());
            }
            boolean withText = in.readBoolean();
            Limits limits = new Limits(in.readInt(), in.readInt(), in.readInt(), in.readInt());
            return new MailRequest(operation, server, texts, lists, uids, withText, limits);
        } catch (IOException | IllegalArgumentException e) {
            throw new NetException(NetError.INVALID_ARGUMENT, "The bytes are not a mail request: " + e.getMessage());
        }
    }
}
