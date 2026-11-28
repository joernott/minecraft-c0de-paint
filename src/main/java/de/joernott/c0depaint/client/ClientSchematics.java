package de.joernott.c0depaint.client;

import de.joernott.c0de.network.ClientReceivers;
import de.joernott.c0de.platform.Platform;
import de.joernott.c0depaint.C0dePaint;
import de.joernott.c0depaint.core.PaintError;
import de.joernott.c0depaint.core.PaintException;
import de.joernott.c0depaint.core.config.PaintConfig;
import de.joernott.c0depaint.network.SchematicAnswerPayload;
import de.joernott.c0depaint.network.SchematicCancelPayload;
import de.joernott.c0depaint.network.SchematicFolder;
import de.joernott.c0depaint.network.SchematicRequestPayload;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.client.Minecraft;

/**
 * The client side of c0de_paint/schematics (task 9.5, docs/concept.md#files): reads the schematics of the folder
 * {@code schematics} of the game for the processes its player runs and sends them to the server in parts. Files are
 * read in a thread of their own; the payloads are sent on the client thread.
 */
public final class ClientSchematics {
    private static final Map<Long, CompletableFuture<?>> RUNNING = new ConcurrentHashMap<>();
    private static PaintConfig config = PaintConfig.defaults(PaintConfig.CLIENT);

    private ClientSchematics() {}

    /** Loads the client settings and registers the receivers. Called by the client entry points. */
    public static void init() {
        try {
            config = PaintConfig.load(Platform.INSTANCE.configDir().resolve(PaintConfig.CLIENT_FILE), PaintConfig.CLIENT);
            config.problems().forEach(C0dePaint.LOGGER::warn);
        } catch (IOException e) {
            C0dePaint.LOGGER.error("Can't read {}, using the defaults", PaintConfig.CLIENT_FILE, e);
        }
        ClientReceivers.register(SchematicRequestPayload.TYPE, ClientSchematics::receive);
        ClientReceivers.register(SchematicCancelPayload.TYPE, payload -> {
            CompletableFuture<?> running = RUNNING.remove(payload.id());
            if (running != null) {
                running.cancel(true);
            }
        });
    }

    /** The folder {@code schematics} of the game, next to {@code config}. */
    static Path folder() {
        return Platform.INSTANCE.configDir().toAbsolutePath().getParent().resolve("schematics");
    }

    private static void receive(SchematicRequestPayload request) {
        if (!config.get(PaintConfig.ALLOW_SCHEMATICS)) {
            send(SchematicAnswerPayload.failure(
                    request.id(),
                    PaintError.SCHEMATICS_DISABLED.code(),
                    "The client of the player doesn't read schematics (allowSchematics is false)"));
            return;
        }
        Path root = folder();
        CompletableFuture<byte[]> running = CompletableFuture.supplyAsync(
                () -> request.list()
                        ? SchematicFolder.list(root, request.name())
                        : SchematicFolder.read(root, request.name(), request.maxBytes()));
        // Recorded first: a request that fails at once completes before whenComplete returns
        RUNNING.put(request.id(), running);
        running.whenComplete((answer, failure) -> Minecraft.getInstance().execute(() -> {
            if (RUNNING.remove(request.id()) == null) {
                // Cancelled by the server
                return;
            }
            if (failure != null) {
                Throwable cause = failure.getCause() != null ? failure.getCause() : failure;
                if (cause instanceof PaintException paint) {
                    send(SchematicAnswerPayload.failure(request.id(), paint.error().code(), paint.getMessage()));
                } else if (cause instanceof IllegalArgumentException invalid) {
                    send(SchematicAnswerPayload.failure(
                            request.id(), PaintError.INVALID_ARGUMENT.code(), invalid.getMessage()));
                } else {
                    send(SchematicAnswerPayload.failure(
                            request.id(), PaintError.SCHEMATIC_INVALID.code(), String.valueOf(cause.getMessage())));
                }
                return;
            }
            for (SchematicAnswerPayload part : SchematicAnswerPayload.parts(request.id(), answer)) {
                send(part);
            }
        }));
    }

    private static void send(SchematicAnswerPayload payload) {
        Platform.INSTANCE.sendToServer(payload);
    }
}
