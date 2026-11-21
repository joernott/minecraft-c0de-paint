package de.joernott.c0depaint.core.transform;

import java.util.random.RandomGenerator;

/**
 * Work on a canvas that may take several ticks (docs/concept.md#configuration, canvasVoxelsPerTick): the function keeps
 * the job with {@code call.state()} while the process waits on its CALL line, and calls {@link #step} once per tick.
 */
public interface Job {
    /**
     * Does the next part of the work.
     *
     * @param budget the most voxels to process
     * @param random the random number generator of the process
     * @return true when the work is done
     */
    boolean step(int budget, RandomGenerator random);
}
