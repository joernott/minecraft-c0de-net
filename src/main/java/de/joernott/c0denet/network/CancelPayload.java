package de.joernott.c0denet.network;

import de.joernott.c0denet.C0deNet;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

/** The server cancels a request that a client is sending, e.g. because its process ended. */
public record CancelPayload(long id) implements CustomPacketPayload {
    public static final Type<CancelPayload> TYPE = new Type<>(C0deNet.id("cancel"));

    public static final StreamCodec<RegistryFriendlyByteBuf, CancelPayload> CODEC =
            StreamCodec.composite(ByteBufCodecs.VAR_LONG, CancelPayload::id, CancelPayload::new);

    @Override
    public Type<CancelPayload> type() {
        return TYPE;
    }
}
