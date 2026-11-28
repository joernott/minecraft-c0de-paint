package de.joernott.c0depaint.network;

import de.joernott.c0depaint.C0dePaint;
import java.nio.charset.StandardCharsets;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

/**
 * A part of the answer of a client, or its error (task 9.5). A packet to the server may have 32 KiB, so files are sent
 * in parts.
 *
 * @param index the number of the part, from 0
 * @param total the number of parts
 * @param error 0 for a part of the answer, otherwise the code of the error; {@code data} is then its message
 * @param data the bytes of the part (of the file, or of the names of a list, one per line), or the message in UTF-8
 */
public record SchematicAnswerPayload(long id, int index, int total, int error, byte[] data)
        implements CustomPacketPayload {
    public static final Type<SchematicAnswerPayload> TYPE = new Type<>(C0dePaint.id("schematic_answer"));

    /** The largest part: a packet to the server may have 32 KiB. */
    public static final int PART_SIZE = 30_000;

    public static final StreamCodec<RegistryFriendlyByteBuf, SchematicAnswerPayload> CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_LONG,
            SchematicAnswerPayload::id,
            ByteBufCodecs.VAR_INT,
            SchematicAnswerPayload::index,
            ByteBufCodecs.VAR_INT,
            SchematicAnswerPayload::total,
            ByteBufCodecs.VAR_INT,
            SchematicAnswerPayload::error,
            ByteBufCodecs.BYTE_ARRAY,
            SchematicAnswerPayload::data,
            SchematicAnswerPayload::new);

    /** The error of a request. */
    public static SchematicAnswerPayload failure(long id, int error, String message) {
        return new SchematicAnswerPayload(id, 0, 1, error, message.getBytes(StandardCharsets.UTF_8));
    }

    /** Splits an answer into its parts. */
    public static SchematicAnswerPayload[] parts(long id, byte[] answer) {
        int total = Math.max(1, (answer.length + PART_SIZE - 1) / PART_SIZE);
        SchematicAnswerPayload[] parts = new SchematicAnswerPayload[total];
        for (int i = 0; i < total; i++) {
            int from = i * PART_SIZE;
            parts[i] = new SchematicAnswerPayload(
                    id, i, total, 0, java.util.Arrays.copyOfRange(answer, from, Math.min(answer.length, from + PART_SIZE)));
        }
        return parts;
    }

    /** The message of an error. */
    public String message() {
        return new String(data, StandardCharsets.UTF_8);
    }

    @Override
    public Type<SchematicAnswerPayload> type() {
        return TYPE;
    }
}
