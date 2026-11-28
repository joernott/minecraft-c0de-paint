package de.joernott.c0depaint.network;

import de.joernott.c0depaint.C0dePaint;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

/** The server no longer waits for a request: its process ended or the time ran out (task 9.5). */
public record SchematicCancelPayload(long id) implements CustomPacketPayload {
    public static final Type<SchematicCancelPayload> TYPE = new Type<>(C0dePaint.id("schematic_cancel"));

    public static final StreamCodec<RegistryFriendlyByteBuf, SchematicCancelPayload> CODEC =
            StreamCodec.composite(ByteBufCodecs.VAR_LONG, SchematicCancelPayload::id, SchematicCancelPayload::new);

    @Override
    public Type<SchematicCancelPayload> type() {
        return TYPE;
    }
}
