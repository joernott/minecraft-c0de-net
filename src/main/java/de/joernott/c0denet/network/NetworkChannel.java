package de.joernott.c0denet.network;

import de.joernott.c0de.platform.Platform;
import de.joernott.c0denet.core.http.NetRequest;
import de.joernott.c0denet.http.ClientChannel;
import de.joernott.c0denet.http.PendingRequests;
import java.util.Arrays;
import java.util.Optional;
import java.util.UUID;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;

/** The way to the clients in the game: the payloads of c0de net (docs/concept.md#running-requests-on-the-client). */
public final class NetworkChannel implements ClientChannel {
    /** Registers the payloads. Called once when the mod is initialized. */
    public static void register() {
        Platform.INSTANCE.registerClientbound(RequestPayload.TYPE, RequestPayload.CODEC);
        Platform.INSTANCE.registerClientbound(CancelPayload.TYPE, CancelPayload.CODEC);
        Platform.INSTANCE.registerServerbound(AnswerPayload.TYPE, AnswerPayload.CODEC, (payload, player) -> {
            if (payload.error() != 0) {
                PendingRequests.failed(player.getUUID(), payload.id(), payload.error(), payload.message());
            } else {
                PendingRequests.answered(
                        player.getUUID(), payload.id(), payload.index(), payload.total(), payload.data());
            }
        });
    }

    /** Splits an answer into the payloads of its parts. */
    public static AnswerPayload[] parts(long id, byte[] answer) {
        int total = Math.max(1, (answer.length + PendingRequests.PART_SIZE - 1) / PendingRequests.PART_SIZE);
        AnswerPayload[] parts = new AnswerPayload[total];
        for (int i = 0; i < total; i++) {
            int from = i * PendingRequests.PART_SIZE;
            parts[i] = new AnswerPayload(
                    id,
                    i,
                    total,
                    0,
                    Arrays.copyOfRange(answer, from, Math.min(answer.length, from + PendingRequests.PART_SIZE)));
        }
        return parts;
    }

    @Override
    public Optional<String> unavailable(MinecraftServer server, UUID player) {
        ServerPlayer online = server.getPlayerList().getPlayer(player);
        if (online == null) {
            return Optional.of("The player who runs the process is not online");
        }
        if (!NetPlatform.canSend(online, new CancelPayload(0))) {
            return Optional.of(online.getName().getString() + " doesn't have c0de net installed");
        }
        return Optional.empty();
    }

    @Override
    public void send(MinecraftServer server, UUID player, long id, NetRequest request, long maxResponseSize) {
        ServerPlayer online = server.getPlayerList().getPlayer(player);
        if (online != null) {
            Platform.INSTANCE.sendToPlayer(online, new RequestPayload(id, maxResponseSize, request.toBytes()));
        }
    }

    @Override
    public void cancel(MinecraftServer server, UUID player, long id) {
        ServerPlayer online = server.getPlayerList().getPlayer(player);
        if (online != null) {
            Platform.INSTANCE.sendToPlayer(online, new CancelPayload(id));
        }
    }
}
