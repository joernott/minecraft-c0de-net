package de.joernott.c0denet.network;

import de.joernott.c0denet.C0deNet;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

/**
 * A request the server sends to the client of the player who runs the process.
 *
 * @param id the number of the request, for its answer
 * @param maxResponseSize the largest answer in bytes
 * @param request the request ({@code NetRequest#toBytes})
 */
public record RequestPayload(long id, long maxResponseSize, byte[] request) implements CustomPacketPayload {
    public static final Type<RequestPayload> TYPE = new Type<>(C0deNet.id("request"));

    public static final StreamCodec<RegistryFriendlyByteBuf, RequestPayload> CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_LONG,
            RequestPayload::id,
            ByteBufCodecs.VAR_LONG,
            RequestPayload::maxResponseSize,
            ByteBufCodecs.BYTE_ARRAY,
            RequestPayload::request,
            RequestPayload::new);

    @Override
    public Type<RequestPayload> type() {
        return TYPE;
    }
}
