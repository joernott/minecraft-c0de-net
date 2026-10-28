package de.joernott.c0denet.network;

import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;

/** What c0de net needs of the loaders beyond the platform of c0de. */
final class NetPlatform {
    private NetPlatform() {}

    /** True if the client of a player has the channel of a payload, i.e. has c0de net installed. */
    static boolean canSend(ServerPlayer player, CustomPacketPayload payload) {
        //? if fabric {
        return net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking.canSend(player, payload.type());
        //?} else {
        /*return player.connection != null && player.connection.hasChannel(payload);
        *///?}
    }
}
