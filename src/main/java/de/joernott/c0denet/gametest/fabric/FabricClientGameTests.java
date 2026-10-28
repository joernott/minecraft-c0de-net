package de.joernott.c0denet.gametest.fabric;

//? if fabric && >=1.21.11 {
/*import com.sun.net.httpserver.HttpServer;
import de.joernott.c0de.blockentity.TriggerBlockEntity;
import de.joernott.c0denet.gametest.HttpTests;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestServerContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestSingleplayerContext;
import net.minecraft.server.level.ServerPlayer;

/^*
 * Client GameTests (task 3.6, entry point {@code fabric-client-gametest}): in a single-player world, a program sends
 * its requests through the network to the real client, which sends them and returns the answers in parts. They run
 * with {@code runClientGameTest} where Fabric API has client GameTests (1.21.4 and later).
 ^/
public class FabricClientGameTests implements FabricClientGameTest {
    /^* The sheet of the program; the GameTests of the server use 600 and 601. ^/
    private static final int SHEET = 602;

    /^* The most ticks the test waits for the process. ^/
    private static final int MAX_TICKS = 1200;

    @Override
    public void runTest(ClientGameTestContext context) {
        // As in the client GameTests of c0de: few chunks and no blur, for software rendering in GitHub Actions
        context.runOnClient(client -> {
            client.options.renderDistance().set(4);
            client.options.simulationDistance().set(5);
            client.options.menuBackgroundBlurriness().set(0);
        });
        HttpServer server = HttpTests.server();
        try (TestSingleplayerContext world = context.worldBuilder().create()) {
            TestServerContext gameServer = world.getServer();
            AtomicReference<TriggerBlockEntity> trigger = new AtomicReference<>();
            gameServer.runOnServer(minecraft -> {
                ServerPlayer player = minecraft.getPlayerList().getPlayers().get(0);
                trigger.set(HttpTests.startWeb(
                        minecraft.overworld(),
                        player.blockPosition().above(2),
                        player,
                        server.getAddress().getPort(),
                        SHEET));
            });
            for (int tick = 0; tick < MAX_TICKS && gameServer.computeOnServer(minecraft -> trigger.get().isRunning());
                    tick++) {
                context.waitTick();
            }
            Optional<String> problem = gameServer.computeOnServer(minecraft -> HttpTests.checkWeb(trigger.get()));
            gameServer.runOnServer(minecraft -> HttpTests.clearWeb(minecraft.overworld(), SHEET));
            problem.ifPresent(message -> {
                throw new AssertionError("Requests through the client: " + message);
            });
        } finally {
            server.stop(0);
        }
    }
}
*///?}
