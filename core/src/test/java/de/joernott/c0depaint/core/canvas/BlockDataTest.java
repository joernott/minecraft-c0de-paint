package de.joernott.c0depaint.core.canvas;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Base64;
import java.util.Map;
import java.util.zip.DeflaterOutputStream;
import org.junit.jupiter.api.Test;

/** Blocks with the data of their block entities (task 9.2). */
class BlockDataTest {
    private static final String SIGN_DATA = "{front_text:{messages:['\"Hi, [friend]\"','\"\"','\"\"','\"\"']}}";

    @Test
    void blocksCarryTheDataOfTheirBlockEntity() {
        Block sign = Block.of("minecraft:oak_sign", Map.of("rotation", "4"), SIGN_DATA);
        assertTrue(sign.hasData());
        assertEquals("minecraft:oak_sign[rotation=4]", sign.state());
        assertEquals("minecraft:oak_sign[rotation=4]" + SIGN_DATA, sign.text());
        assertEquals(sign, Block.parse(sign.text()));
        // Without states, the data follows the id
        Block chest = new Block("minecraft:chest").withData("{Items:[{Slot:0b,id:\"minecraft:stone\",count:3}]}");
        assertEquals(chest, Block.parse(chest.text()));
        // States and data are kept apart by the changes
        assertEquals(SIGN_DATA, sign.with("rotation", "8").data());
        assertEquals(SIGN_DATA, sign.withProperties(Map.of()).data());
        assertFalse(new Block("minecraft:stone").hasData());
        assertTrue(new Block("minecraft:stone", new java.util.TreeMap<>(), null).data().isEmpty());
    }

    @Test
    void dataIsACompoundOnOneLine() {
        assertThrows(IllegalArgumentException.class, () -> new Block("minecraft:chest").withData("Items"));
        assertThrows(IllegalArgumentException.class, () -> new Block("minecraft:chest").withData("{a:1,\nb:2}"));
        assertThrows(IllegalArgumentException.class, () -> Block.parse("minecraft:stairs[facing=north"));
    }

    @Test
    void savedCanvasesKeepTheDataEvenWhenItIsLong() {
        Canvas canvas = new Canvas();
        canvas.set(0, 0, 0, Block.of("minecraft:oak_sign", Map.of("rotation", "4"), SIGN_DATA));
        // More than the 64 KiB that DataOutputStream.writeUTF can write
        String items = "{Items:[" + "{Slot:0b,id:\"minecraft:written_book\",count:1},".repeat(2000) + "]}";
        canvas.set(1, 0, 0, new Block("minecraft:chest").withData(items));
        canvas.set(2, 0, 0, new Block("minecraft:chest"));
        Canvas loaded = Canvas.load(canvas.save());
        assertEquals(canvas, loaded);
        assertEquals(items, loaded.get(1, 0, 0).data());
        assertFalse(loaded.get(2, 0, 0).hasData());
    }

    @Test
    void canvasesOfVersionOneStillLoad() throws IOException {
        // A canvas saved by c0de paint 0.1.0: one section at 0, 0, 0 full of stone
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        try (DataOutputStream out = new DataOutputStream(new DeflaterOutputStream(bytes))) {
            out.writeByte(1);
            out.writeInt(1);
            out.writeLong(0);
            out.writeShort(1);
            out.writeUTF("minecraft:oak_stairs[facing=north,half=bottom]");
            out.writeShort(16 * 16 * 16 - 1);
            out.writeShort(1);
        }
        Canvas canvas = Canvas.load(Base64.getEncoder().encodeToString(bytes.toByteArray()));
        assertEquals(16 * 16 * 16, canvas.count());
        assertEquals(Block.of("minecraft:oak_stairs", Map.of("facing", "north", "half", "bottom")), canvas.get(15, 15, 15));
    }
}
