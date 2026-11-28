package de.joernott.c0depaint.gametest;

import de.joernott.c0depaint.core.PaintError;
import de.joernott.c0depaint.core.PaintException;
import de.joernott.c0depaint.network.PendingFiles;
import de.joernott.c0depaint.network.SchematicAnswerPayload;
import de.joernott.c0depaint.network.SchematicFolder;
import de.joernott.c0depaint.network.SchematicRequestPayload;
import java.nio.file.Path;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.server.MinecraftServer;

/**
 * A stand-in for the clients of the test players, which can't receive payloads (task 9.5): their requests are
 * answered from a folder on the server, in parts like the payloads of a client, one tick later. The requests of all
 * other players go to the network as usual.
 */
final class LocalSchematics implements PendingFiles.Channel {
    private static final Map<UUID, Path> LOCAL = new ConcurrentHashMap<>();

    private final PendingFiles.Channel network;

    private LocalSchematics(PendingFiles.Channel network) {
        this.network = network;
    }

    /** Puts the stand-in in front of the network, once for all tests. */
    static synchronized void install() {
        if (!(PendingFiles.channel() instanceof LocalSchematics)) {
            PendingFiles.channel(new LocalSchematics(PendingFiles.channel()));
        }
    }

    /** The requests of the player are answered from the folder. */
    static void add(UUID player, Path folder) {
        LOCAL.put(player, folder);
    }

    static void remove(UUID player) {
        LOCAL.remove(player);
    }

    @Override
    public Optional<String> unavailable(MinecraftServer server, UUID player) {
        return LOCAL.containsKey(player) ? Optional.empty() : network.unavailable(server, player);
    }

    @Override
    public void send(MinecraftServer server, UUID player, SchematicRequestPayload request) {
        Path folder = LOCAL.get(player);
        if (folder == null) {
            network.send(server, player, request);
            return;
        }
        SchematicAnswerPayload[] answer;
        try {
            byte[] bytes = request.list()
                    ? SchematicFolder.list(folder, request.name())
                    : SchematicFolder.read(folder, request.name(), request.maxBytes());
            answer = SchematicAnswerPayload.parts(request.id(), bytes);
        } catch (PaintException e) {
            answer = new SchematicAnswerPayload[] {
                SchematicAnswerPayload.failure(request.id(), e.error().code(), e.getMessage())
            };
        } catch (IllegalArgumentException e) {
            answer = new SchematicAnswerPayload[] {
                SchematicAnswerPayload.failure(request.id(), PaintError.INVALID_ARGUMENT.code(), e.getMessage())
            };
        }
        SchematicAnswerPayload[] parts = answer;
        server.execute(() -> {
            for (SchematicAnswerPayload part : parts) {
                PendingFiles.answered(player, part);
            }
        });
    }

    @Override
    public void cancel(MinecraftServer server, UUID player, long id) {
        if (!LOCAL.containsKey(player)) {
            network.cancel(server, player, id);
        }
    }
}
