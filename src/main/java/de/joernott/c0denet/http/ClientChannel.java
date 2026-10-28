package de.joernott.c0denet.http;

import de.joernott.c0denet.core.http.NetRequest;
import java.util.Optional;
import java.util.UUID;
import net.minecraft.server.MinecraftServer;

/**
 * The way to the client that sends the requests of a player. In the game, it is the network ({@code
 * NetworkChannel}); GameTests, which have no clients, use a stand-in that sends the requests in the server.
 *
 * <p>The answers come back through {@link PendingRequests#answered} and {@link PendingRequests#failed}, on the server
 * thread.
 */
public interface ClientChannel {
    /**
     * Why the client of a player can't send requests now, or empty if it can.
     *
     * @return e.g. "Tester1 is not online" or "Tester1 doesn't have c0de net installed"
     */
    Optional<String> unavailable(MinecraftServer server, UUID player);

    /** Sends a request to the client of a player. */
    void send(MinecraftServer server, UUID player, long id, NetRequest request, long maxResponseSize);

    /** Cancels a request that the client of a player is sending. */
    void cancel(MinecraftServer server, UUID player, long id);
}
