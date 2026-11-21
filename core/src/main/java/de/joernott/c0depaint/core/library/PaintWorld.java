package de.joernott.c0depaint.core.library;

import de.joernott.c0de.core.error.C0deException;
import de.joernott.c0de.core.error.ErrorCode;
import de.joernott.c0de.core.library.NativeCall;
import de.joernott.c0de.core.value.StructValue;
import de.joernott.c0depaint.core.canvas.Block;
import de.joernott.c0depaint.core.canvas.Canvas;
import de.joernott.c0depaint.core.canvas.Limits;
import de.joernott.c0depaint.core.config.PaintConfig;
import de.joernott.c0depaint.core.style.Weighted;
import de.joernott.c0depaint.core.transform.Job;
import java.util.List;

/**
 * What the functions of c0de_paint/cpaint need of the world: the mod implements it with Minecraft, the runner library
 * without it. Positions are {@code c0de/minecraft/POSITION}s of the calling process.
 */
public interface PaintWorld {
    /** Without a world, e.g. in the unit tests: the functions raise INVALID_ARGUMENT. */
    PaintWorld NONE = new PaintWorld() {};

    /**
     * The blocks of an inventory (INVENTORY_BLOCKS): in the order of the slots, with the number of items as weight; a
     * block in several slots is listed once, at its first slot, with the sum of the items; items that can't be placed
     * as blocks are ignored.
     */
    default List<Weighted> inventoryBlocks(NativeCall call, StructValue position) {
        throw unavailable("INVENTORY_BLOCKS");
    }

    /**
     * The block if it can take its states, or null: a transformation keeps the states of blocks that can't take the
     * transformed ones (docs/concept.md#block-states-in-transformations). Without a world, every block is valid.
     */
    default Block valid(Block block) {
        return block;
    }

    /**
     * The rendering of a canvas (RENDER_CANVAS) as a job that runs while the process waits: the checks before the first
     * block happen when the job is created.
     *
     * @param call the CALL of RENDER_CANVAS
     * @param canvas the canvas, a copy that doesn't change while rendering
     * @param position where the voxel 0,0,0 lands
     * @param settings the settings of c0de paint
     */
    default Job render(NativeCall call, Canvas canvas, StructValue position, PaintConfig settings) {
        throw unavailable("RENDER_CANVAS");
    }

    /**
     * The blocks of the world between two positions as a canvas (CANVAS_FROM_WORLD): {@code from} becomes 0,0,0; air
     * becomes not set, or air if {@code air} is true.
     */
    default Canvas fromWorld(NativeCall call, StructValue from, StructValue to, boolean air, Limits limits) {
        throw unavailable("CANVAS_FROM_WORLD");
    }

    /** The error of a function that needs the world where there is none. */
    static C0deException unavailable(String function) {
        return C0deException.of(
                ErrorCode.INVALID_ARGUMENT, function + " of " + PaintTypes.LIBRARY + " is only available in the game");
    }
}
