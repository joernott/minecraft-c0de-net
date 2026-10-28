package de.joernott.c0denet.gametest;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import de.joernott.c0de.blockentity.TriggerBlockEntity;
import de.joernott.c0de.content.C0deBlocks;
import de.joernott.c0de.content.C0deItems;
import de.joernott.c0de.dimension.C0deDimension;
import de.joernott.c0de.item.ValueItem;
import de.joernott.c0de.sheet.SheetValidation;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

/**
 * GameTests of c0de_net/http (task 3.6): programs send requests to a local HTTP server through the stand-in for the
 * client ({@link LocalClients}) and get their answers; a player without the mod can't send requests.
 */
public final class HttpTests {
    private HttpTests() {}

    /** Size of the answer of /api/big: four parts of the network. */
    static final int BIG = 100_000;

    private static final String WEB = """
            PROGRAM web
            NOOP
            INCLUDE c0de_net/http
            NOOP
            FUNCTION run
                PARAMETER port INTEGER
                RESULT got STRING
                RESULT posted STRING
                RESULT missing INTEGER
                RESULT status INTEGER
                RESULT body STRING
                RESULT size INTEGER
                RESULT invalid INTEGER
            BEGIN
                VARIABLE problem ERROR
                VARIABLE api STRUCT c0de_net/http/HTTP_CONNECTION
                VARIABLE big STRING
                ASSIGN api.server "127.0.0.1" ;
                ASSIGN api.port port ;
                ASSIGN api.base_endpoint "/api" ;
                CALL c0de_net/http/HTTP_GET
                    PARAMETER connection api ;
                    PARAMETER endpoint "/echo?x=1" ;
                    RESULT result got
                CALL c0de_net/http/HTTP_POST
                    PARAMETER connection api ;
                    PARAMETER endpoint "/echo" ;
                    PARAMETER data "hello" ;
                    RESULT result posted
                CALL c0de_net/http/HTTP_GET problem
                    PARAMETER connection api ;
                    PARAMETER endpoint "/missing" ;
                    RESULT result body
                    RESULT status status
                ASSIGN missing problem.code ;
                CALL c0de_net/http/HTTP_GET
                    PARAMETER connection api ;
                    PARAMETER endpoint "/big" ;
                    RESULT result big
                CALL LEN
                    PARAMETER value big ;
                    RESULT result size
                ASSIGN api.port 70000 ;
                CALL c0de_net/http/HTTP_GET problem
                    PARAMETER connection api ;
                ASSIGN invalid problem.code ;
            END FUNCTION
            """;

    private static final String OFFLINE = """
            PROGRAM offline
            NOOP
            INCLUDE c0de_net/http
            NOOP
            FUNCTION run
                RESULT unavailable INTEGER
            BEGIN
                VARIABLE problem ERROR
                VARIABLE api STRUCT c0de_net/http/HTTP_CONNECTION
                ASSIGN api.server "127.0.0.1" ;
                CALL c0de_net/http/HTTP_GET problem
                    PARAMETER connection api ;
                ASSIGN unavailable problem.code ;
            END FUNCTION
            """;

    private static ItemStack variable(String name, String type, String value) {
        ItemStack item = new ItemStack(C0deItems.VARIABLE_ITEM.get());
        new ValueItem.Contents(name, type, value).writeTo(item);
        return item;
    }

    /** The results of a finished process by name, as JSON (strings in quotes). */
    private static Map<String, String> results(TriggerBlockEntity trigger) {
        Map<String, String> results = new HashMap<>();
        for (int i = 0; i < trigger.output().getContainerSize(); i++) {
            ValueItem.Contents result = ValueItem.Contents.of(trigger.output().getItem(i));
            results.put(result.name(), result.value());
        }
        return results;
    }

    /** A server that answers /api/echo with method, path and body, /api/missing with 404 and /api/big with 100 KB. */
    private static HttpServer server() {
        try {
            // The programs connect to 127.0.0.1; the loopback address of Java may be ::1 (NeoForge prefers IPv6)
            HttpServer server = HttpServer.create(new InetSocketAddress(InetAddress.getByName("127.0.0.1"), 0), 0);
            server.createContext("/api/echo", exchange -> {
                String body;
                try (InputStream in = exchange.getRequestBody()) {
                    body = new String(in.readAllBytes(), StandardCharsets.UTF_8);
                }
                respond(exchange, 200, exchange.getRequestMethod() + " " + exchange.getRequestURI() + " " + body);
            });
            server.createContext("/api/missing", exchange -> respond(exchange, 404, "nothing here"));
            server.createContext("/api/big", exchange -> respond(exchange, 200, "x".repeat(BIG)));
            server.start();
            return server;
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private static void respond(HttpExchange exchange, int status, String text) throws IOException {
        byte[] bytes = text.getBytes(StandardCharsets.UTF_8);
        exchange.sendResponseHeaders(status, bytes.length);
        try (OutputStream out = exchange.getResponseBody()) {
            out.write(bytes);
        }
    }

    /**
     * A program sends GET and POST requests and gets their answers, the results and HTTP_STATUS of a 404, a large
     * answer in several parts, and INVALID_ARGUMENT for an invalid port.
     */
    public static void programsSendRequests(GameTestHelper helper) {
        ServerLevel c0de = helper.getLevel().getServer().getLevel(C0deDimension.LEVEL);
        SheetBuilder sheet = new SheetBuilder(c0de, 600).place(WEB);
        SheetValidation.validate(c0de, 600);
        HttpServer server = server();
        LocalClients.install();
        ServerPlayer player = helper.makeMockServerPlayerInLevel();
        LocalClients.add(player.getUUID());
        helper.setBlock(BlockPos.ZERO, C0deBlocks.TRIGGER_BLOCK.get());
        TriggerBlockEntity trigger = (TriggerBlockEntity) TestCompat.blockEntity(helper, BlockPos.ZERO);
        trigger.configure(player, "web", "run", 20);
        trigger.input().setItem(0, variable("port", "INTEGER", String.valueOf(server.getAddress().getPort())));
        for (int i = 1; i <= 7; i++) {
            trigger.input().setItem(i, new ItemStack(C0deItems.VARIABLE_ITEM.get()));
        }
        trigger.start();
        helper.assertTrue(trigger.isRunning(), "The process didn't start: " + trigger.status());
        TestCompat.await(helper, () -> !trigger.isRunning(), () -> {
            try {
                helper.assertTrue(!trigger.isRunning(), "The process is still running");
                helper.assertTrue(trigger.exitCode() == 0, "Exit code " + trigger.exitCode() + " " + trigger.error());
                Map<String, String> results = results(trigger);
                helper.assertTrue(results.get("got").equals("\"GET /api/echo?x=1 \""), "GET: " + results.get("got"));
                helper.assertTrue(
                        results.get("posted").equals("\"POST /api/echo hello\""), "POST: " + results.get("posted"));
                helper.assertTrue(
                        results.get("missing").equals("1000")
                                && results.get("status").equals("404")
                                && results.get("body").equals("\"nothing here\""),
                        "404: " + results);
                helper.assertTrue(results.get("size").equals(String.valueOf(BIG)), "Size: " + results.get("size"));
                helper.assertTrue(results.get("invalid").equals("309"), "Invalid port: " + results.get("invalid"));
            } finally {
                server.stop(0);
                LocalClients.remove(player.getUUID());
                sheet.clear();
                SheetValidation.validate(c0de, 600);
            }
            helper.succeed();
        });
    }

    /** The client of a player without c0de net can't send requests: CLIENT_UNAVAILABLE. */
    public static void playersWithoutTheModCantSendRequests(GameTestHelper helper) {
        ServerLevel c0de = helper.getLevel().getServer().getLevel(C0deDimension.LEVEL);
        SheetBuilder sheet = new SheetBuilder(c0de, 601).place(OFFLINE);
        SheetValidation.validate(c0de, 601);
        LocalClients.install();
        helper.setBlock(BlockPos.ZERO, C0deBlocks.TRIGGER_BLOCK.get());
        TriggerBlockEntity trigger = (TriggerBlockEntity) TestCompat.blockEntity(helper, BlockPos.ZERO);
        trigger.configure(helper.makeMockServerPlayerInLevel(), "offline", "run", 20);
        trigger.input().setItem(0, new ItemStack(C0deItems.VARIABLE_ITEM.get()));
        trigger.start();
        helper.assertTrue(trigger.isRunning(), "The process didn't start: " + trigger.status());
        TestCompat.await(helper, () -> !trigger.isRunning(), () -> {
            try {
                helper.assertTrue(!trigger.isRunning(), "The process is still running");
                helper.assertTrue(trigger.exitCode() == 0, "Exit code " + trigger.exitCode() + " " + trigger.error());
                Map<String, String> results = results(trigger);
                helper.assertTrue("1001".equals(results.get("unavailable")), "The results are " + results);
            } finally {
                sheet.clear();
                SheetValidation.validate(c0de, 601);
            }
            helper.succeed();
        });
    }
}
