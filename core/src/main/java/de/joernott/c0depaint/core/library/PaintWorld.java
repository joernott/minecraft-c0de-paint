package de.joernott.c0depaint.core.library;

import de.joernott.c0de.core.error.C0deException;
import de.joernott.c0de.core.error.ErrorCode;
import de.joernott.c0de.core.library.NativeCall;
import de.joernott.c0de.core.value.StructValue;
import de.joernott.c0depaint.core.style.Weighted;
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

    /** The error of a function that needs the world where there is none. */
    static C0deException unavailable(String function) {
        return C0deException.of(
                ErrorCode.INVALID_ARGUMENT, function + " of " + PaintTypes.LIBRARY + " is only available in the game");
    }
}
