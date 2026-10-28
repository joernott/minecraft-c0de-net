package de.joernott.c0denet.network;

import de.joernott.c0denet.C0deNet;
import java.nio.charset.StandardCharsets;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

/**
 * A part of the answer to a request, or its error, from the client to the server. A packet to the server may have
 * 32 KiB, so answers are sent in parts.
 *
 * @param index the number of the part, from 0
 * @param total the number of parts
 * @param error 0 for a part of the answer, otherwise the code of the error; {@code data} is then its message
 * @param data the bytes of the part ({@code NetResponse#toBytes}), or the message of the error in UTF-8
 */
public record AnswerPayload(long id, int index, int total, int error, byte[] data) implements CustomPacketPayload {
    public static final Type<AnswerPayload> TYPE = new Type<>(C0deNet.id("answer"));

    public static final StreamCodec<RegistryFriendlyByteBuf, AnswerPayload> CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_LONG,
            AnswerPayload::id,
            ByteBufCodecs.VAR_INT,
            AnswerPayload::index,
            ByteBufCodecs.VAR_INT,
            AnswerPayload::total,
            ByteBufCodecs.VAR_INT,
            AnswerPayload::error,
            ByteBufCodecs.BYTE_ARRAY,
            AnswerPayload::data,
            AnswerPayload::new);

    /** The error of a request. */
    public static AnswerPayload failure(long id, int error, String message) {
        return new AnswerPayload(id, 0, 1, error, message.getBytes(StandardCharsets.UTF_8));
    }

    /** The message of an error. */
    public String message() {
        return new String(data, StandardCharsets.UTF_8);
    }

    @Override
    public Type<AnswerPayload> type() {
        return TYPE;
    }
}
