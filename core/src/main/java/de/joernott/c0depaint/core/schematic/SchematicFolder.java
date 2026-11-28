package de.joernott.c0depaint.core.schematic;

import de.joernott.c0depaint.core.PaintError;
import de.joernott.c0depaint.core.PaintException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.stream.Stream;

/**
 * Reading the folder {@code schematics} (task 9.5): only inside the folder, also after links are followed. Used by the
 * client and by the stand-in of the GameTests; it doesn't refer to client classes.
 */
public final class SchematicFolder {
    /** The most names a list returns. */
    private static final int MAX_NAMES = 10_000;

    /** The deepest folder that a list visits. */
    private static final int MAX_DEPTH = 8;

    private SchematicFolder() {}

    /** The bytes of a schematic. */
    public static byte[] read(Path root, String name, long maxBytes) {
        Path file = inside(root, SchematicNames.file(name), name);
        if (!Files.isRegularFile(file)) {
            throw new PaintException(PaintError.SCHEMATIC_NOT_FOUND, "There is no schematic " + name);
        }
        try {
            if (Files.size(file) > maxBytes) {
                throw new PaintException(
                        PaintError.SCHEMATIC_TOO_LARGE,
                        name + " has " + Files.size(file) + " bytes, more than the " + maxBytes + " the server reads");
            }
            return Files.readAllBytes(file);
        } catch (IOException e) {
            throw new PaintException(PaintError.SCHEMATIC_NOT_FOUND, name + " can't be read: " + e.getMessage());
        }
    }

    /** The names of the schematics of a folder and its subfolders, one per line. */
    public static byte[] list(Path root, String folderName) {
        String checked = SchematicNames.folder(folderName);
        if (!Files.isDirectory(root)) {
            return new byte[0];
        }
        Path folder = checked.isEmpty() ? real(root) : inside(root, checked, folderName);
        if (!Files.isDirectory(folder)) {
            throw new PaintException(PaintError.SCHEMATIC_NOT_FOUND, "There is no folder " + folderName);
        }
        Path realRoot = real(root);
        List<String> names = new ArrayList<>();
        try (Stream<Path> files = Files.walk(folder, MAX_DEPTH)) {
            files.filter(Files::isRegularFile)
                    .filter(file -> {
                        String lower = file.getFileName().toString().toLowerCase(Locale.ROOT);
                        return lower.endsWith(".litematic") || lower.endsWith(".nbt");
                    })
                    .limit(MAX_NAMES)
                    .forEach(file -> {
                        Path real = real(file);
                        if (real.startsWith(realRoot)) {
                            String name = realRoot.relativize(real).toString().replace('\\', '/');
                            try {
                                names.add(SchematicNames.file(name));
                            } catch (IllegalArgumentException e) {
                                // A name that a program couldn't read anyway
                            }
                        }
                    });
        } catch (IOException e) {
            throw new PaintException(PaintError.SCHEMATIC_NOT_FOUND, "The folder can't be read: " + e.getMessage());
        }
        return String.join("\n", names).getBytes(StandardCharsets.UTF_8);
    }

    /** The path of a checked name, which must lie inside the folder even after links are followed. */
    private static Path inside(Path root, String checked, String name) {
        Path realRoot = real(root);
        Path path = realRoot.resolve(checked).normalize();
        if (!path.startsWith(realRoot)) {
            throw new IllegalArgumentException("The name " + name + " leaves the folder schematics");
        }
        if (Files.exists(path) && !real(path).startsWith(realRoot)) {
            throw new IllegalArgumentException(name + " links to a file outside of the folder schematics");
        }
        return path;
    }

    private static Path real(Path path) {
        try {
            return path.toRealPath();
        } catch (IOException e) {
            return path.toAbsolutePath().normalize();
        }
    }

}
