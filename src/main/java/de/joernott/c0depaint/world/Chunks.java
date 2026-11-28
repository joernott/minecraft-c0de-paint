package de.joernott.c0depaint.world;

import de.joernott.c0de.compat.ChunkTickets;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;

/**
 * Loads the chunks of the areas that RENDER_CANVAS builds and CANVAS_FROM_WORLD copies, like c0de loads the parts of
 * long programs (c0de task 21.1): a chunk that isn't loaded is loaded on the server thread and kept for a while (200
 * ticks) with the temporary ticket of c0de. Large structures don't depend on what the players have loaded.
 */
final class Chunks {
    private Chunks() {}

    /** Makes sure that the chunk of a position is loaded; returns false only off the server thread. */
    static boolean ensureLoaded(ServerLevel level, BlockPos pos) {
        if (level.isLoaded(pos)) {
            return true;
        }
        if (!level.getServer().isSameThread()) {
            return false;
        }
        ChunkPos chunk = new ChunkPos(pos.getX() >> 4, pos.getZ() >> 4);
        ChunkTickets.addTemporary(level, chunk);
        level.getChunk(chunk.x, chunk.z);
        return level.isLoaded(pos);
    }
}
