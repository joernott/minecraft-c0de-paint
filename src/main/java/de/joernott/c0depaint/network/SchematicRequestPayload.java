package de.joernott.c0depaint.network;

import de.joernott.c0depaint.C0dePaint;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

/**
 * What the server asks the client of the player who runs the process (task 9.5): to read a schematic or to list a
 * folder of {@code schematics}.
 *
 * @param id the number of the request, for its answer
 * @param list true to list the folder {@code name}, false to read the file {@code name}
 * @param name the name, relative to {@code schematics}
 * @param maxBytes the largest file the server reads
 */
public record SchematicRequestPayload(long id, boolean list, String name, long maxBytes) implements CustomPacketPayload {
    public static final Type<SchematicRequestPayload> TYPE = new Type<>(C0dePaint.id("schematic_request"));

    public static final StreamCodec<RegistryFriendlyByteBuf, SchematicRequestPayload> CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_LONG,
            SchematicRequestPayload::id,
            ByteBufCodecs.BOOL,
            SchematicRequestPayload::list,
            ByteBufCodecs.STRING_UTF8,
            SchematicRequestPayload::name,
            ByteBufCodecs.VAR_LONG,
            SchematicRequestPayload::maxBytes,
            SchematicRequestPayload::new);

    @Override
    public Type<SchematicRequestPayload> type() {
        return TYPE;
    }
}
