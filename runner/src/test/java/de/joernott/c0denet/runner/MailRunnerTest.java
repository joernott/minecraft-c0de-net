package de.joernott.c0denet.runner;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.icegreen.greenmail.configuration.GreenMailConfiguration;
import com.icegreen.greenmail.junit5.GreenMailExtension;
import com.icegreen.greenmail.util.ServerSetup;
import de.joernott.c0de.core.library.LibraryRegistry;
import de.joernott.c0de.core.runner.ProgramRunner;
import de.joernott.c0denet.core.http.NetError;
import jakarta.mail.internet.MimeMessage;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

/** c0de_net/mail in the program runner of c0de, against GreenMail (tasks 8.6 to 8.8). */
class MailRunnerTest {
    @RegisterExtension
    static GreenMailExtension mail = new GreenMailExtension(new ServerSetup[] {
                // Other ports than the tests of core, which Gradle may run at the same time
                new ServerSetup(3125, null, ServerSetup.PROTOCOL_SMTP),
                new ServerSetup(3243, null, ServerSetup.PROTOCOL_IMAP)
            })
            .withConfiguration(GreenMailConfiguration.aConfig().withUser("steve@example.com", "steve", "pw"))
            .withPerMethodLifecycle(true);

    private static Map<String, Object> run(String program) {
        Map<String, Object> report =
                ProgramRunner.run(program, ProgramRunner.Options.DEFAULT, ProgramRunner.libraries());
        return report;
    }

    @Test
    void theRunnerFindsTheLibrary() {
        LibraryRegistry libraries = ProgramRunner.libraries();
        assertTrue(libraries.find("c0de_net/mail").isPresent());
    }

    @Test
    void programsSendAndReadMail() {
        String program = """
                PROGRAM letters
                NOOP
                INCLUDE c0de_net/mail
                NOOP
                FUNCTION main
                    RESULT folder STRING
                    RESULT count INTEGER
                    RESULT subject STRING
                    RESULT text STRING
                    RESULT flagged INTEGER
                    RESULT removed INTEGER
                BEGIN
                    VARIABLE smtp STRUCT c0de_net/mail/SMTP_SERVER
                    VARIABLE imap STRUCT c0de_net/mail/IMAP_SERVER
                    VARIABLE box STRUCT c0de_net/mail/MAILBOX
                    VARIABLE uids ARRAY INTEGER
                    VARIABLE found ARRAY INTEGER
                    VARIABLE letters ARRAY STRUCT c0de_net/mail/MAIL
                    VARIABLE id STRING
                    ASSIGN smtp.server "localhost" ;
                    ASSIGN smtp.port 3125 ;
                    ASSIGN smtp.security "none" ;
                    ASSIGN smtp.user "steve" ;
                    ASSIGN smtp.password "pw" ;
                    ASSIGN smtp.from "steve@example.com" ;
                    CALL c0de_net/mail/SMTP_SEND
                        PARAMETER server smtp ;
                        PARAMETER to "steve@example.com" ;
                        PARAMETER subject "Creepers" ;
                        PARAMETER text "Two at the gate" ;
                        RESULT result id
                    CALL c0de_net/mail/SMTP_SEND
                        PARAMETER server smtp ;
                        PARAMETER to "steve@example.com" ;
                        PARAMETER subject "Wheat" ;
                        PARAMETER text "The farm is full" ;
                    ASSIGN imap.server "localhost" ;
                    ASSIGN imap.port 3243 ;
                    ASSIGN imap.security "none" ;
                    ASSIGN imap.user "steve" ;
                    ASSIGN imap.password "pw" ;
                    CALL c0de_net/mail/IMAP_SELECT
                        PARAMETER server imap ;
                        RESULT result box
                    ASSIGN folder imap.folder ;
                    ASSIGN count box.messages ;
                    CALL c0de_net/mail/IMAP_SEARCH
                        PARAMETER server imap ;
                        PARAMETER criteria "SUBJECT Creepers" ;
                        RESULT result uids
                    CALL c0de_net/mail/IMAP_FETCH
                        PARAMETER server imap ;
                        PARAMETER uids uids ;
                        RESULT result letters
                    ASSIGN subject letters[0].subject ;
                    ASSIGN text letters[0].text ;
                    CALL c0de_net/mail/IMAP_STORE
                        PARAMETER server imap ;
                        PARAMETER uids uids ;
                        PARAMETER flags "\\\\Deleted" ;
                    CALL c0de_net/mail/IMAP_SEARCH
                        PARAMETER server imap ;
                        PARAMETER criteria "DELETED" ;
                        RESULT result found
                    CALL LEN
                        PARAMETER value found ;
                        RESULT result flagged
                    CALL c0de_net/mail/IMAP_EXPUNGE
                        PARAMETER server imap ;
                        RESULT result removed
                END FUNCTION
                """;
        Map<String, Object> report = run(program);
        assertEquals("FINISHED", report.get("state"), () -> "report: " + report);
        Map<?, ?> results = (Map<?, ?>) report.get("results");
        assertEquals("INBOX", results.get("folder"), () -> "results: " + results);
        assertEquals(2L, results.get("count"));
        assertEquals("Creepers", results.get("subject"));
        assertEquals("Two at the gate", String.valueOf(results.get("text")).strip());
        assertEquals(1L, results.get("flagged"));
        assertEquals(1L, results.get("removed"));
        // Two were sent, one was expunged
        MimeMessage[] received = mail.getReceivedMessages();
        assertEquals(1, received.length);
    }

    @Test
    void foldersNeedASelect() {
        String program = """
                PROGRAM unselected
                NOOP
                INCLUDE c0de_net/mail
                NOOP
                FUNCTION main
                    RESULT code INTEGER
                BEGIN
                    VARIABLE problem ERROR
                    VARIABLE imap STRUCT c0de_net/mail/IMAP_SERVER
                    ASSIGN imap.server "localhost" ;
                    ASSIGN imap.port 3243 ;
                    ASSIGN imap.security "none" ;
                    ASSIGN imap.user "steve" ;
                    ASSIGN imap.password "pw" ;
                    CALL c0de_net/mail/IMAP_EXPUNGE problem
                        PARAMETER server imap ;
                    ASSIGN code problem.code ;
                END FUNCTION
                """;
        Map<String, Object> report = run(program);
        assertEquals("FINISHED", report.get("state"), () -> "report: " + report);
        assertEquals(
                Map.of("code", (long) NetError.INVALID_ARGUMENT.code()),
                report.get("results"),
                () -> "report: " + report);
    }

    @Test
    void port25IsBlocked() {
        String program = """
                PROGRAM spam
                NOOP
                INCLUDE c0de_net/mail
                NOOP
                FUNCTION main
                    RESULT code INTEGER
                    RESULT denied INTEGER
                BEGIN
                    VARIABLE problem ERROR
                    VARIABLE smtp STRUCT c0de_net/mail/SMTP_SERVER
                    ASSIGN smtp.server "localhost" ;
                    ASSIGN smtp.port 25 ;
                    ASSIGN smtp.from "steve@example.com" ;
                    CALL c0de_net/mail/SMTP_SEND problem
                        PARAMETER server smtp ;
                        PARAMETER to "alex@example.com" ;
                    ASSIGN code problem.code ;
                    ASSIGN denied PERMISSION_DENIED ;
                END FUNCTION
                """;
        Map<String, Object> report = run(program);
        assertEquals("FINISHED", report.get("state"), () -> "report: " + report);
        Map<?, ?> results = (Map<?, ?>) report.get("results");
        assertEquals((long) NetError.PERMISSION_DENIED.code(), results.get("code"), () -> "report: " + report);
        assertEquals(results.get("code"), results.get("denied"));
    }
}
