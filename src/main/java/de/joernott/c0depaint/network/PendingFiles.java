package de.joernott.c0depaint.network;

import de.joernott.c0de.core.library.NativeCall;
import de.joernott.c0de.core.library.NativeResult;
import de.joernott.c0de.library.ProcessHost;
import de.joernott.c0depaint.core.PaintError;
import de.joernott.c0depaint.core.PaintException;
import de.joernott.c0depaint.core.library.SchematicsLibrary;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;
import net.minecraft.server.MinecraftServer;

/**
 * The schematics that processes wait for (task 9.5, docs/concept.md#files): a function sends the name to the client
 * of the player who runs the process and returns {@link NativeResult#waitForValue()}; the cursor calls it again in
 * every step until the file, an error or the timeout ends the call. Built like the requests of c0de net.
 *
 * <p>Only used on the server thread.
 */
public final class PendingFiles implements SchematicsLibrary.Files {
    /** Seconds the server waits for a client. */
    static final int TIMEOUT_SECONDS = 60;

    /** A request whose process didn't call its function for this many ticks is cancelled (the process ended). */
    static final int STALE_TICKS = 100;

    /** The largest list of names in bytes. */
    static final long MAX_LIST_BYTES = 1024 * 1024;

    /** The way to the clients: the network in the game, a stand-in in the GameTests. */
    public interface Channel {
        /** Why the client of a player can't read schematics now, or empty if it can. */
        Optional<String> unavailable(MinecraftServer server, UUID player);

        void send(MinecraftServer server, UUID player, SchematicRequestPayload request);

        void cancel(MinecraftServer server, UUID player, long id);
    }

    private static final class Pending {
        final long id;
        final Object process;
        final UUID player;
        final SchematicRequestPayload request;
        final long maxBytes;
        long deadline;
        int lastCalled;
        int nextPart;
        final ByteArrayOutputStream answer = new ByteArrayOutputStream();
        byte[] complete;
        PaintException failure;

        Pending(long id, Object process, UUID player, SchematicRequestPayload request, long maxBytes) {
            this.id = id;
            this.process = process;
            this.player = player;
            this.request = request;
            this.maxBytes = maxBytes;
        }

        boolean done() {
            return complete != null || failure != null;
        }
    }

    private static Channel channel;
    private static final Map<Object, Pending> BY_PROCESS = new IdentityHashMap<>();
    private static final Map<Long, Pending> BY_ID = new HashMap<>();
    private static MinecraftServer server;
    private static long nextId = 1;

    /** Sets the way to the clients. Called when the mod is initialized, and by GameTests. */
    public static void channel(Channel newChannel) {
        channel = newChannel;
    }

    public static Channel channel() {
        return channel;
    }

    @Override
    public NativeResult read(NativeCall call, String name, long maxBytes, Function<byte[], NativeResult> then) {
        return run(call, false, name, maxBytes, then);
    }

    @Override
    public NativeResult list(NativeCall call, String folder, Function<List<String>, NativeResult> then) {
        return run(call, true, folder, MAX_LIST_BYTES, bytes -> {
            String text = new String(bytes, StandardCharsets.UTF_8);
            return then.apply(text.isEmpty() ? List.of() : Arrays.asList(text.split("\n")));
        });
    }

    private static NativeResult run(
            NativeCall call, boolean list, String name, long maxBytes, Function<byte[], NativeResult> then) {
        if (!(call.process().host() instanceof ProcessHost host)) {
            throw new PaintException(PaintError.INVALID_ARGUMENT, "c0de_paint/schematics only works in the game");
        }
        forget(host.server());
        int tick = host.server().getTickCount();
        Pending pending = BY_PROCESS.get(call.process());
        if (pending == null) {
            UUID player = host.activator() != null ? host.activator() : host.owner();
            if (player == null) {
                throw new PaintException(
                        PaintError.CLIENT_UNAVAILABLE,
                        "Nobody runs the process: it was started without a player and its trigger block has no owner");
            }
            long id = nextId++;
            pending = new Pending(id, call.process(), player, new SchematicRequestPayload(id, list, name, maxBytes), maxBytes);
            BY_PROCESS.put(call.process(), pending);
            BY_ID.put(id, pending);
            Optional<String> unavailable = channel.unavailable(host.server(), player);
            if (unavailable.isPresent()) {
                remove(pending);
                throw new PaintException(PaintError.CLIENT_UNAVAILABLE, unavailable.get());
            }
            pending.deadline = System.currentTimeMillis() + TIMEOUT_SECONDS * 1000L;
            channel.send(host.server(), player, pending.request);
        }
        pending.lastCalled = tick;
        if (pending.done()) {
            remove(pending);
            if (pending.failure != null) {
                throw pending.failure;
            }
            return then.apply(pending.complete);
        }
        if (channel.unavailable(host.server(), pending.player).isPresent()) {
            cancel(host.server(), pending);
            throw new PaintException(PaintError.CLIENT_UNAVAILABLE, "The player who runs the process left the game");
        }
        if (System.currentTimeMillis() > pending.deadline) {
            cancel(host.server(), pending);
            throw new PaintException(
                    PaintError.CLIENT_UNAVAILABLE,
                    "The client didn't answer within " + TIMEOUT_SECONDS + " seconds");
        }
        return NativeResult.waitForValue();
    }

    /** A part of an answer arrived from the client of a player. */
    public static void answered(UUID player, SchematicAnswerPayload part) {
        Pending pending = BY_ID.get(part.id());
        if (pending == null || !pending.player.equals(player) || pending.done()) {
            return;
        }
        if (part.error() != 0) {
            PaintError error = PaintError.of(part.error());
            pending.failure = new PaintException(error == null ? PaintError.SCHEMATIC_INVALID : error, part.message());
            return;
        }
        if (part.index() != pending.nextPart || part.index() >= part.total()) {
            pending.failure = new PaintException(PaintError.SCHEMATIC_INVALID, "The client sent the file in a wrong order");
            return;
        }
        if (pending.answer.size() + part.data().length > pending.maxBytes) {
            pending.failure = new PaintException(
                    PaintError.SCHEMATIC_TOO_LARGE, "The file is larger than " + pending.maxBytes + " bytes");
            channel.cancel(server, player, pending.id);
            return;
        }
        pending.answer.write(part.data(), 0, part.data().length);
        pending.nextPart++;
        if (part.index() == part.total() - 1) {
            pending.complete = pending.answer.toByteArray();
        }
    }

    /** Cancels the requests of processes that ended. Called at the end of every server tick. */
    public static void tick(MinecraftServer current) {
        forget(current);
        int tick = current.getTickCount();
        for (Iterator<Pending> it = BY_ID.values().iterator(); it.hasNext(); ) {
            Pending pending = it.next();
            if (tick - pending.lastCalled > STALE_TICKS) {
                it.remove();
                BY_PROCESS.remove(pending.process);
                if (!pending.done()) {
                    channel.cancel(current, pending.player, pending.id);
                }
            }
        }
    }

    private static void forget(MinecraftServer current) {
        if (server != current) {
            BY_ID.clear();
            BY_PROCESS.clear();
            server = current;
        }
    }

    private static void cancel(MinecraftServer current, Pending pending) {
        remove(pending);
        if (!pending.done()) {
            channel.cancel(current, pending.player, pending.id);
        }
    }

    private static void remove(Pending pending) {
        BY_ID.remove(pending.id);
        BY_PROCESS.remove(pending.process);
    }
}
