package de.joernott.c0denet.core.mail;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.icegreen.greenmail.configuration.GreenMailConfiguration;
import com.icegreen.greenmail.junit5.GreenMailExtension;
import com.icegreen.greenmail.util.ServerSetupTest;
import de.joernott.c0denet.core.http.NetError;
import de.joernott.c0denet.core.http.NetException;
import jakarta.mail.internet.MimeMessage;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

/** The calls of c0de_net/mail against GreenMail, a mail server in the test (task 8.6 to 8.8). */
class MailClientTest {
    @RegisterExtension
    static GreenMailExtension mail = new GreenMailExtension(ServerSetupTest.SMTP_IMAP)
            .withConfiguration(GreenMailConfiguration.aConfig().withUser("steve@example.com", "steve", "pw"))
            .withPerMethodLifecycle(true);

    private static final MailRequest.Limits LIMITS = new MailRequest.Limits(64 << 10, 20, 1 << 20, 100);

    private static MailServer smtp() {
        return MailServer.of(
                false, "localhost", 3025, "none", "steve", "pw", "Steve <steve@example.com>", "", 5, false, 30);
    }

    private static MailServer imap(String folder) {
        return MailServer.of(true, "localhost", 3143, "none", "steve", "pw", "", folder, 5, false, 30);
    }

    private static MailAnswer run(
            MailOperation operation,
            MailServer server,
            Map<String, String> texts,
            Map<String, List<String>> lists,
            List<Long> uids,
            boolean withText) {
        MailRequest request = new MailRequest(operation, server, texts, lists, uids, withText, LIMITS);
        // Through the network format, like from the server to the client
        return MailAnswer.fromBytes(
                MailClient.run(MailRequest.fromBytes(request.toBytes())).toBytes());
    }

    private static void send(String subject, String text) {
        run(
                MailOperation.SEND,
                smtp(),
                Map.of("subject", subject, "text", text),
                Map.of("to", List.of("steve@example.com")),
                List.of(),
                false);
    }

    @Test
    void sendsPlainTextMail() throws Exception {
        MailAnswer answer = run(
                MailOperation.SEND,
                smtp(),
                Map.of("subject", "Grüße", "text", "Die Tür ist offen.\nCreeper: 0", "reply_to", "base@example.com"),
                Map.of("to", List.of("steve@example.com"), "cc", List.of("alex@example.com")),
                List.of(),
                false);
        assertFalse(answer.text().isEmpty(), "a Message-ID");
        MimeMessage[] received = mail.getReceivedMessages();
        assertEquals(2, received.length, "to and cc");
        assertEquals("Grüße", received[0].getSubject());
        assertTrue(((String) received[0].getContent()).contains("Creeper: 0"));
        assertEquals("base@example.com", received[0].getReplyTo()[0].toString());
    }

    @Test
    void refusesTooManyRecipientsAndLargeMessages() {
        MailRequest.Limits small = new MailRequest.Limits(1024, 2, 1 << 20, 100);
        NetException recipients = assertThrows(
                NetException.class,
                () -> MailClient.run(new MailRequest(
                        MailOperation.SEND,
                        smtp(),
                        Map.of("subject", "x", "text", "x"),
                        Map.of("to", List.of("a@example.com", "b@example.com", "c@example.com")),
                        List.of(),
                        false,
                        small)));
        assertEquals(NetError.MAIL_LIMIT, recipients.error());
        NetException size = assertThrows(
                NetException.class,
                () -> MailClient.run(new MailRequest(
                        MailOperation.SEND,
                        smtp(),
                        Map.of("subject", "x", "text", "x".repeat(5000)),
                        Map.of("to", List.of("a@example.com")),
                        List.of(),
                        false,
                        small)));
        assertEquals(NetError.MAIL_LIMIT, size.error());
        NetException address = assertThrows(
                NetException.class,
                () -> run(
                        MailOperation.SEND,
                        smtp(),
                        Map.of("subject", "x", "text", "x"),
                        Map.of("to", List.of("not an address")),
                        List.of(),
                        false));
        assertEquals(NetError.INVALID_ARGUMENT, address.error());
    }

    @Test
    void readsAndManagesMessages() {
        send("First", "one");
        send("Second", "two");
        send("Third", "three");
        MailAnswer.Mailbox inbox = run(
                        MailOperation.SELECT, imap(""), Map.of("folder", "INBOX"), Map.of(), List.of(), false)
                .mailbox();
        assertEquals("INBOX", inbox.name());
        assertEquals(3, inbox.messages());
        assertEquals(3, inbox.unseen());

        List<Long> all = run(MailOperation.SEARCH, imap("INBOX"), Map.of(), Map.of(), List.of(), false)
                .uids();
        assertEquals(3, all.size());
        List<Long> second = run(
                        MailOperation.SEARCH,
                        imap("INBOX"),
                        Map.of("criteria", "SUBJECT \"Second\""),
                        Map.of(),
                        List.of(),
                        false)
                .uids();
        assertEquals(List.of(all.get(1)), second);

        // Without text, the messages stay unseen; with text, they are seen
        List<MailAnswer.Message> headers = run(MailOperation.FETCH, imap("INBOX"), Map.of(), Map.of(), all, false)
                .messages();
        assertEquals("", headers.get(0).text());
        assertEquals(
                3,
                run(MailOperation.STATUS, imap("INBOX"), Map.of("folder", "INBOX"), Map.of(), List.of(), false)
                        .mailbox()
                        .unseen());
        List<MailAnswer.Message> read = run(
                        MailOperation.FETCH, imap("INBOX"), Map.of(), Map.of(), List.of(all.get(0), 9999L), true)
                .messages();
        assertEquals(1, read.size(), "a UID that doesn't exist is left out");
        assertEquals("First", read.get(0).subject());
        assertEquals("one", read.get(0).text().strip());
        assertTrue(read.get(0).from().contains("steve@example.com"));
        assertTrue(read.get(0).flags().contains("\\Seen"));
        assertEquals(
                1,
                run(MailOperation.SEARCH, imap("INBOX"), Map.of("criteria", "SEEN"), Map.of(), List.of(), false)
                        .uids()
                        .size());

        // Flags, folders, moving and expunging
        run(
                MailOperation.STORE,
                imap("INBOX"),
                Map.of(),
                Map.of("flags", List.of("\\Flagged", "important")),
                List.of(all.get(1)),
                false);
        assertEquals(
                List.of(all.get(1)),
                run(MailOperation.SEARCH, imap("INBOX"), Map.of("criteria", "FLAGGED"), Map.of(), List.of(), false)
                        .uids());
        run(MailOperation.CREATE, imap(""), Map.of("folder", "Archive"), Map.of(), List.of(), false);
        assertTrue(run(MailOperation.LIST, imap(""), Map.of(), Map.of(), List.of(), false)
                .names()
                .contains("Archive"));
        run(MailOperation.MOVE, imap("INBOX"), Map.of("folder", "Archive"), Map.of(), List.of(all.get(1)), false);
        assertEquals(
                1,
                run(MailOperation.STATUS, imap(""), Map.of("folder", "Archive"), Map.of(), List.of(), false)
                        .mailbox()
                        .messages());
        run(
                MailOperation.STORE,
                imap("INBOX"),
                Map.of(),
                Map.of("flags", List.of("\\Deleted")),
                List.of(all.get(2)),
                false);
        assertEquals(
                1,
                run(MailOperation.EXPUNGE, imap("INBOX"), Map.of(), Map.of(), List.of(), false)
                        .count());
        assertEquals(
                1,
                run(MailOperation.STATUS, imap(""), Map.of("folder", "INBOX"), Map.of(), List.of(), false)
                        .mailbox()
                        .messages());
        run(MailOperation.RENAME, imap(""), Map.of("folder", "Archive", "new_name", "Old"), Map.of(), List.of(), false);
        run(MailOperation.DELETE, imap(""), Map.of("folder", "Old"), Map.of(), List.of(), false);
        assertFalse(run(MailOperation.LIST, imap(""), Map.of(), Map.of(), List.of(), false)
                .names()
                .contains("Old"));
    }

    @Test
    void reportsErrors() {
        NetException folder = assertThrows(
                NetException.class,
                () -> run(MailOperation.SELECT, imap(""), Map.of("folder", "Nowhere"), Map.of(), List.of(), false));
        assertEquals(NetError.MAIL_REJECTED, folder.error());
        NetException unselected = assertThrows(
                NetException.class, () -> run(MailOperation.EXPUNGE, imap(""), Map.of(), Map.of(), List.of(), false));
        assertEquals(NetError.INVALID_ARGUMENT, unselected.error());
        MailServer wrong = MailServer.of(true, "localhost", 3143, "none", "steve", "wrong", "", "", 5, false, 30);
        NetException password = assertThrows(
                NetException.class, () -> run(MailOperation.LIST, wrong, Map.of(), Map.of(), List.of(), false));
        assertEquals(NetError.AUTHENTICATION_FAILED, password.error());
        MailServer closed = MailServer.of(true, "localhost", 1, "none", "steve", "pw", "", "", 5, false, 30);
        NetException refused = assertThrows(
                NetException.class, () -> run(MailOperation.LIST, closed, Map.of(), Map.of(), List.of(), false));
        assertEquals(NetError.REQUEST_FAILED, refused.error());
        NetException criteria = assertThrows(
                NetException.class,
                () -> run(
                        MailOperation.SEARCH,
                        imap("INBOX"),
                        Map.of("criteria", "LARGER 100"),
                        Map.of(),
                        List.of(),
                        false));
        assertEquals(NetError.INVALID_ARGUMENT, criteria.error());
    }

    @Test
    void checksTheConnection() {
        assertEquals(
                587,
                MailServer.of(false, "smtp.example.com", 0, "", "", "", "a@b.c", "", 0, false, 30)
                        .port());
        assertEquals(
                465,
                MailServer.of(false, "smtp.example.com", 0, "TLS", "", "", "a@b.c", "", 0, false, 30)
                        .port());
        assertEquals(
                993,
                MailServer.of(true, "imap.example.com", 0, "", "u", "", "", "", 0, false, 30)
                        .port());
        assertEquals(
                30,
                MailServer.of(true, "imap.example.com", 0, "", "u", "", "", "", 0, false, 30)
                        .timeout());
        assertEquals(
                NetError.INVALID_ARGUMENT,
                assertThrows(
                                NetException.class,
                                () -> MailServer.of(false, "smtp.example.com", 0, "", "", "", "", "", 0, false, 30))
                        .error());
        assertEquals(
                NetError.INVALID_ARGUMENT,
                assertThrows(
                                NetException.class,
                                () -> MailServer.of(true, "imap.example.com", 0, "ssl", "u", "", "", "", 0, false, 30))
                        .error());
        assertEquals(
                NetError.INVALID_ARGUMENT,
                assertThrows(
                                NetException.class,
                                () -> MailServer.of(true, "imap.example.com", 0, "", "u", "", "", "", 31, false, 30))
                        .error());
        assertEquals(List.of("FROM", "Alex Smith", "UNSEEN"), MailSearch.tokens("FROM \"Alex Smith\"  UNSEEN"));
    }
}
