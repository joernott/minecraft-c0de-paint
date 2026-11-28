package de.joernott.c0depaint.core.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/** The settings in config/c0de_paint.properties (task 2.3). */
class PaintConfigTest {
    @Test
    void defaultsMatchTheConcept() {
        PaintConfig config = PaintConfig.defaults(PaintConfig.SERVER);
        assertEquals(-128, config.get(PaintConfig.CANVAS_MIN));
        assertEquals(255, config.get(PaintConfig.CANVAS_MAX));
        assertEquals(64, config.get(PaintConfig.RENDER_BLOCKS_PER_TICK));
        assertEquals(1, config.get(PaintConfig.RENDER_MINING_SPEED));
        assertEquals(65536, config.get(PaintConfig.CANVAS_VOXELS_PER_TICK));
    }

    @Test
    void aMissingFileIsCreatedWithTheDefaults(@TempDir Path dir) throws IOException {
        Path file = dir.resolve("config").resolve(PaintConfig.SERVER_FILE);
        PaintConfig.load(file, PaintConfig.SERVER);
        String text = Files.readString(file, StandardCharsets.UTF_8);
        assertTrue(text.contains("canvasMin=-128"), text);
        assertTrue(text.contains("renderMiningSpeed=1"), text);
    }

    @Test
    void invalidAndUnknownEntriesAreReported() {
        Properties properties = new Properties();
        properties.setProperty("canvasMax", "2000");
        properties.setProperty("renderMiningSpeed", "0");
        properties.setProperty("brush", "big");
        PaintConfig config = PaintConfig.of(properties, PaintConfig.SERVER, "c0de_paint.properties");
        assertEquals(255, config.get(PaintConfig.CANVAS_MAX));
        assertEquals(0, config.get(PaintConfig.RENDER_MINING_SPEED));
        assertEquals(2, config.problems().size(), config.problems().toString());
    }
}
