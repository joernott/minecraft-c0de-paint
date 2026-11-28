package de.joernott.c0depaint.runner;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.joernott.c0de.core.cursor.Cursor;
import de.joernott.c0de.core.cursor.CursorSettings;
import de.joernott.c0de.core.cursor.CursorState;
import de.joernott.c0de.core.library.LibraryRegistry;
import de.joernott.c0de.core.library.minecraft.MinecraftLibraryDefinition;
import de.joernott.c0de.core.line.Notation;
import de.joernott.c0de.core.program.ProgramParser;
import de.joernott.c0de.core.validation.Validator;
import de.joernott.c0de.core.value.IntegerValue;
import de.joernott.c0de.core.value.StringValue;
import de.joernott.c0de.core.value.StructValue;
import de.joernott.c0depaint.core.config.PaintConfig;
import de.joernott.c0depaint.core.library.CpaintLibrary;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.ServiceLoader;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/** c0de_paint/cpaint in the program runner of c0de (task 8.2). */
class PaintRunnerLibrariesTest {
    private static final String PROGRAM = """
            PROGRAM tower
            NOOP
            INCLUDE c0de/minecraft
            INCLUDE c0de_paint/cpaint
            NOOP
            FUNCTION main
                PARAMETER at STRUCT c0de/minecraft/POSITION
            BEGIN
                VARIABLE c TYPE c0de_paint/cpaint/CANVAS
                VARIABLE a STRUCT c0de_paint/cpaint/POINT
                VARIABLE b STRUCT c0de_paint/cpaint/POINT
                VARIABLE stone STRUCT c0de_paint/cpaint/BLOCK
                VARIABLE s TYPE c0de_paint/cpaint/STYLE
                ASSIGN stone.id "stone" ;
                ASSIGN b.x 1 ;
                ASSIGN b.y 1 ;
                CALL c0de_paint/cpaint/STYLE_SOLID
                    PARAMETER block stone ;
                    RESULT result s
                CALL c0de_paint/cpaint/DRAW_BOX
                    PARAMETER canvas c ;
                    PARAMETER from a ;
                    PARAMETER to b ;
                    PARAMETER fill_style s ;
                CALL c0de_paint/cpaint/RENDER_CANVAS
                    PARAMETER canvas c ;
                    PARAMETER position at ;
            END FUNCTION
            """;

    @Test
    void theProviderIsFoundByTheRunner() {
        LibraryRegistry registry = new LibraryRegistry();
        ServiceLoader.load(de.joernott.c0de.core.library.LibraryProvider.class)
                .forEach(provider -> provider.register(registry));
        assertTrue(registry.find(CpaintLibrary.NAME).isPresent());
        assertTrue(registry.find(de.joernott.c0depaint.core.library.SchematicsLibrary.NAME)
                .isPresent());
    }

    /** The runner reads schematics from a folder, with the rules of the client for names (task 9.7). */
    @Test
    void schematicsComeFromAFolder(@TempDir Path dir) throws IOException {
        Files.createDirectories(dir.resolve("castles"));
        byte[] structure = de.joernott.c0depaint.core.schematic.Nbt.write(new java.util.LinkedHashMap<>(Map.of(
                "DataVersion", 3955,
                "size", java.util.List.of(1, 1, 1),
                "palette", java.util.List.of(Map.of("Name", "minecraft:stone")),
                "blocks", java.util.List.of(Map.of("pos", java.util.List.of(0, 0, 0), "state", 0)))));
        Files.write(dir.resolve("castles/keep.nbt"), structure);
        RunnerSchematics files = new RunnerSchematics(dir);
        assertEquals(
                java.util.List.of("castles/keep.nbt"),
                files.list(
                                null,
                                "",
                                names -> de.joernott.c0de.core.library.NativeResult.of(
                                        new StringValue(String.join(",", names))))
                        .results()
                        .get("result")
                        .text()
                        .lines()
                        .toList());
        assertEquals(
                structure.length,
                files.read(
                                        null,
                                        "castles/keep.nbt",
                                        1000,
                                        bytes -> de.joernott.c0de.core.library.NativeResult.of(
                                                new IntegerValue(bytes.length)))
                                .results()
                                .get("result")
                                .equals(new IntegerValue(structure.length))
                        ? structure.length
                        : -1);
        org.junit.jupiter.api.Assertions.assertThrows(
                IllegalArgumentException.class, () -> files.read(null, "../keep.nbt", 1000, bytes -> null));
    }

    @Test
    void renderingWritesTheBlocksLayerByLayer(@TempDir Path dir) throws IOException {
        Path file = dir.resolve("render.yaml");
        PaintConfig settings = PaintConfig.defaults(PaintConfig.SERVER);
        LibraryRegistry registry = new LibraryRegistry()
                .add(new CpaintLibrary(() -> settings, new RunnerWorld(file)))
                .add(new de.joernott.c0de.core.library.Library() {
                    @Override
                    public String name() {
                        return MinecraftLibraryDefinition.NAME;
                    }

                    @Override
                    public Map<String, de.joernott.c0de.core.value.StructDefinition> types() {
                        return Map.of("POSITION", MinecraftLibraryDefinition.POSITION);
                    }

                    @Override
                    public Map<String, de.joernott.c0de.core.library.NativeFunction> functions() {
                        return Map.of();
                    }
                });
        assertTrue(
                Validator.validate(Notation.parse(PROGRAM), registry).problems().isEmpty());
        StructValue at = new StructValue(MinecraftLibraryDefinition.POSITION);
        at.set("x", new IntegerValue(10));
        at.set("y", new IntegerValue(64));
        at.set("z", new IntegerValue(-3));
        at.set("dimension", new StringValue(""));
        Cursor cursor = new Cursor(
                ProgramParser.parse(Notation.parse(PROGRAM)), "main", Map.of("at", at), CursorSettings.of(registry));
        cursor.run(10_000);
        assertEquals(CursorState.FINISHED, cursor.state(), () -> "" + cursor.error());
        assertEquals("""
                ---
                position: {x: 10, y: 64, z: -3}
                layers:
                  - y: 64
                    blocks:
                      - {x: 10, z: -3, block: "minecraft:stone"}
                      - {x: 11, z: -3, block: "minecraft:stone"}
                  - y: 65
                    blocks:
                      - {x: 10, z: -3, block: "minecraft:stone"}
                      - {x: 11, z: -3, block: "minecraft:stone"}
                """, Files.readString(file, StandardCharsets.UTF_8));
    }

    /** The complete programs of the handbook, in English and German, are valid with the library (task 8.1). */
    @Test
    void handbookProgramsAreValid() throws IOException {
        LibraryRegistry registry = new LibraryRegistry().add(MinecraftLibraryDefinition.withoutGame());
        new PaintRunnerLibraries().register(registry);
        Path docs = Path.of(System.getProperty("c0depaint.docs"));
        java.util.regex.Pattern code =
                java.util.regex.Pattern.compile("```c0de\\n(.*?)```", java.util.regex.Pattern.DOTALL);
        int programs = 0;
        for (Path file : java.util.List.of(
                docs.resolve("reference").resolve("paint.md"),
                docs.resolve("de").resolve("reference").resolve("paint.md"))) {
            java.util.regex.Matcher matcher =
                    code.matcher(Files.readString(file, StandardCharsets.UTF_8).replace("\r\n", "\n"));
            while (matcher.find()) {
                if (matcher.group(1).startsWith("PROGRAM")) {
                    var problems = Validator.validate(Notation.parse(matcher.group(1)), registry)
                            .problems();
                    assertTrue(
                            problems.isEmpty(),
                            file + ": " + matcher.group(1).lines().findFirst() + " " + problems);
                    programs++;
                }
            }
        }
        assertTrue(programs >= 6, "only " + programs + " programs");
    }
}
