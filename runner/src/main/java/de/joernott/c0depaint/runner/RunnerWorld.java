package de.joernott.c0depaint.runner;

import de.joernott.c0de.core.library.NativeCall;
import de.joernott.c0de.core.value.IntegerValue;
import de.joernott.c0de.core.value.StructValue;
import de.joernott.c0depaint.core.canvas.Canvas;
import de.joernott.c0depaint.core.config.PaintConfig;
import de.joernott.c0depaint.core.library.PaintWorld;
import de.joernott.c0depaint.core.render.RenderPlan;
import de.joernott.c0depaint.core.transform.Job;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

/**
 * The world of the program runner: there are no blocks and no inventories. RENDER_CANVAS appends the blocks it would
 * place to a YAML file, layer by layer in the order of the rendering (without hanging blocks, which only the game
 * knows), so tests compare it with the expected result; INVENTORY_BLOCKS and CANVAS_FROM_WORLD raise INVALID_ARGUMENT.
 *
 * <pre>
 * ---
 * position: {x: 0, y: 64, z: 0}
 * layers:
 *   - y: 64
 *     blocks:
 *       - {x: 0, z: 0, block: "minecraft:stone"}
 * </pre>
 */
final class RunnerWorld implements PaintWorld {
    private final Path file;

    RunnerWorld(Path file) {
        this.file = file;
    }

    @Override
    public Job render(NativeCall call, Canvas canvas, StructValue position, PaintConfig settings) {
        long x = ((IntegerValue) position.get("x")).value();
        long y = ((IntegerValue) position.get("y")).value();
        long z = ((IntegerValue) position.get("z")).value();
        StringBuilder text = new StringBuilder("---\nposition: {x: ")
                .append(x)
                .append(", y: ")
                .append(y)
                .append(", z: ")
                .append(z)
                .append("}\nlayers:\n");
        Integer layer = null;
        for (RenderPlan.Step step : RenderPlan.of(canvas, block -> false).steps()) {
            if (layer == null || layer != step.voxel().y()) {
                layer = step.voxel().y();
                text.append("  - y: ").append(y + layer).append("\n    blocks:\n");
            }
            text.append("      - {x: ")
                    .append(x + step.voxel().x())
                    .append(", z: ")
                    .append(z + step.voxel().z())
                    .append(", block: \"")
                    .append(step.block().text())
                    .append("\"}\n");
        }
        if (layer == null) {
            text.append("  []\n");
        }
        try {
            Files.writeString(file, text, StandardCharsets.UTF_8, StandardOpenOption.CREATE, StandardOpenOption.APPEND);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        return (budget, random) -> true;
    }
}
