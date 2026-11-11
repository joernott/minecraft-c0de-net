package de.joernott.c0denet.runner;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.sun.net.httpserver.HttpServer;
import de.joernott.c0de.core.library.LibraryRegistry;
import de.joernott.c0de.core.line.Notation;
import de.joernott.c0de.core.runner.ProgramRunner;
import de.joernott.c0de.core.validation.Validator;
import de.joernott.c0denet.core.http.NetError;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

/** c0de_net/http and C0DE_NET_VERSION in the program runner of c0de (task 6.1). */
class NetRunnerLibrariesTest {
    private static final Pattern CODE = Pattern.compile("```c0de\\n(.*?)```", Pattern.DOTALL);
    private static HttpServer server;

    @BeforeAll
    static void start() throws IOException {
        server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.createContext("/", exchange -> {
            boolean found = exchange.getRequestURI().getPath().equals("/hello");
            byte[] body = (found ? "{\"greeting\": \"Hello\"}" : "missing").getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().add("Content-Type", "application/json");
            exchange.sendResponseHeaders(found ? 200 : 404, body.length);
            try (OutputStream out = exchange.getResponseBody()) {
                out.write(body);
            }
        });
        server.start();
    }

    @AfterAll
    static void stop() {
        server.stop(0);
    }

    private static String program(String endpoint) {
        return """
                PROGRAM web
                NOOP
                INCLUDE c0de_net/http
                NOOP
                FUNCTION main
                    RESULT answer STRING
                    RESULT status INTEGER
                    RESULT version STRING
                BEGIN
                    VARIABLE api STRUCT c0de_net/http/HTTP_CONNECTION
                    ASSIGN api.server "127.0.0.1" ;
                    ASSIGN api.port %d ;
                    ASSIGN version C0DE_NET_VERSION ;
                    CALL c0de_net/http/HTTP_GET
                        PARAMETER connection api ;
                        PARAMETER endpoint "%s" ;
                        RESULT result answer
                        RESULT status status
                END FUNCTION
                """.formatted(server.getAddress().getPort(), endpoint);
    }

    @Test
    void theRunnerFindsTheLibrary() {
        LibraryRegistry libraries = ProgramRunner.libraries();
        assertTrue(libraries.find("c0de_net/http").isPresent());
        assertTrue(libraries.constants().containsKey(NetRunnerLibraries.VERSION_CONSTANT));
    }

    @Test
    void programsSendRequestsDirectly() {
        Map<String, Object> report =
                ProgramRunner.run(program("/hello"), ProgramRunner.Options.DEFAULT, ProgramRunner.libraries());
        assertEquals("FINISHED", report.get("state"), () -> "report: " + report);
        assertEquals(
                Map.of(
                        "answer",
                        "{\"greeting\": \"Hello\"}",
                        "status",
                        200L,
                        "version",
                        System.getProperty("c0denet.version")),
                report.get("results"));
    }

    @Test
    void errorStatusesRaiseHttpStatus() {
        Map<String, Object> report =
                ProgramRunner.run(program("/other"), ProgramRunner.Options.DEFAULT, ProgramRunner.libraries());
        assertEquals("CRASHED", report.get("state"), () -> "report: " + report);
        Map<?, ?> error = (Map<?, ?>) report.get("error");
        assertEquals((long) NetError.HTTP_STATUS.code(), error.get("code"));
    }

    static Stream<String> handbookPrograms() throws IOException {
        Path docs = Path.of(System.getProperty("c0denet.docs"));
        List<String> programs = new ArrayList<>();
        for (Path file : List.of(docs.resolve("reference/net.md"), docs.resolve("de/reference/net.md"))) {
            if (!Files.exists(file)) {
                continue;
            }
            Matcher matcher =
                    CODE.matcher(Files.readString(file, StandardCharsets.UTF_8).replace("\r\n", "\n"));
            while (matcher.find()) {
                if (matcher.group(1).startsWith("PROGRAM")) {
                    programs.add(file + ": " + matcher.group(1));
                }
            }
        }
        assertFalse(programs.isEmpty(), "The handbook has no complete programs");
        return programs.stream();
    }

    @ParameterizedTest
    @MethodSource("handbookPrograms")
    void theProgramsOfTheHandbookAreValid(String example) {
        String program = example.substring(example.indexOf(": ") + 2);
        assertEquals(
                Map.of(),
                Validator.validate(Notation.parse(program), ProgramRunner.libraries())
                        .problems(),
                example);
    }
}
