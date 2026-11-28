package de.joernott.c0depaint.runner;

import de.joernott.c0de.core.library.NativeCall;
import de.joernott.c0de.core.library.NativeResult;
import de.joernott.c0depaint.core.library.SchematicsLibrary;
import de.joernott.c0depaint.core.schematic.SchematicFolder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;

/**
 * The schematics of the program runner (task 9.7): read at once from a folder on disk instead of from a client, with
 * the same rules for names as the client of the game.
 */
final class RunnerSchematics implements SchematicsLibrary.Files {
    private final Path folder;

    RunnerSchematics(Path folder) {
        this.folder = folder;
    }

    @Override
    public NativeResult read(NativeCall call, String name, long maxBytes, Function<byte[], NativeResult> then) {
        return then.apply(SchematicFolder.read(folder, name, maxBytes));
    }

    @Override
    public NativeResult list(NativeCall call, String name, Function<List<String>, NativeResult> then) {
        String text = new String(SchematicFolder.list(folder, name), StandardCharsets.UTF_8);
        return then.apply(text.isEmpty() ? List.of() : Arrays.asList(text.split("\n")));
    }
}
