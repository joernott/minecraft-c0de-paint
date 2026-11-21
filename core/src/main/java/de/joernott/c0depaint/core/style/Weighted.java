package de.joernott.c0depaint.core.style;

import de.joernott.c0depaint.core.canvas.Block;
import java.util.List;

/**
 * A block of a style with its weight, an integer of 1 or more (docs/concept.md#styles).
 *
 * @param block the block
 * @param weight the weight
 */
public record Weighted(Block block, int weight) {
    public Weighted {
        if (weight < 1) {
            throw new IllegalArgumentException("The weight of " + block.text() + " is " + weight + ", not 1 or more");
        }
    }

    /** The sum of the weights. */
    static int total(List<Weighted> blocks) {
        long sum = 0;
        for (Weighted block : blocks) {
            sum += block.weight;
        }
        if (sum > Integer.MAX_VALUE) {
            throw new IllegalArgumentException("The sum of the weights is too large");
        }
        return (int) sum;
    }

    /** The text of a saved style: the weight and the block. */
    String save() {
        return weight + " " + block.text();
    }

    /** The block of a text created by {@link #save()}. */
    static Weighted load(String text) {
        int space = text.indexOf(' ');
        return new Weighted(Block.parse(text.substring(space + 1)), Integer.parseInt(text.substring(0, space)));
    }
}
