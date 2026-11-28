package de.joernott.c0depaint.network;

import de.joernott.c0de.platform.Platform;
import java.util.Optional;
import java.util.UUID;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;

/** The way to the clients in the game: the payloads of c0de paint (task 9.5). */
public final class SchematicChannel implements PendingFiles.Channel {
    /** Registers the payloads. Called once when the mod is initialized. */
    public static void register() {
        Platform.INSTANCE.registerClientbound(SchematicRequestPayload.TYPE, SchematicRequestPayload.CODEC);
        Platform.INSTANCE.registerClientbound(SchematicCancelPayload.TYPE, SchematicCancelPayload.CODEC);
        Platform.INSTANCE.registerServerbound(
                SchematicAnswerPayload.TYPE,
                SchematicAnswerPayload.CODEC,
                (payload, player) -> PendingFiles.answered(player.getUUID(), payload));
    }

    @Override
    public Optional<String> unavailable(MinecraftServer server, UUID player) {
        ServerPlayer online = server.getPlayerList().getPlayer(player);
        if (online == null) {
            return Optional.of("The player who runs the process is not online");
        }
        if (!canSend(online, new SchematicCancelPayload(0))) {
            return Optional.of(online.getName().getString() + " doesn't have c0de paint installed");
        }
        return Optional.empty();
    }

    @Override
    public void send(MinecraftServer server, UUID player, SchematicRequestPayload request) {
        ServerPlayer online = server.getPlayerList().getPlayer(player);
        if (online != null) {
            Platform.INSTANCE.sendToPlayer(online, request);
        }
    }

    @Override
    public void cancel(MinecraftServer server, UUID player, long id) {
        ServerPlayer online = server.getPlayerList().getPlayer(player);
        if (online != null) {
            Platform.INSTANCE.sendToPlayer(online, new SchematicCancelPayload(id));
        }
    }

    /** True if the client of a player has the channel of a payload, i.e. has c0de paint installed. */
    private static boolean canSend(ServerPlayer player, CustomPacketPayload payload) {
        //? if fabric {
        return net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking.canSend(player, payload.type());
        //?} else {
        /*return player.connection != null && player.connection.hasChannel(payload);
        *///?}
    }
}
