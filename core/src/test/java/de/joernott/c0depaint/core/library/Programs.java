package de.joernott.c0depaint.core.library;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.joernott.c0de.core.cursor.Cursor;
import de.joernott.c0de.core.cursor.CursorSettings;
import de.joernott.c0de.core.cursor.CursorState;
import de.joernott.c0de.core.library.LibraryRegistry;
import de.joernott.c0de.core.library.NativeCall;
import de.joernott.c0de.core.library.NativeResult;
import de.joernott.c0de.core.line.Notation;
import de.joernott.c0de.core.program.Program;
import de.joernott.c0de.core.program.ProgramParser;
import de.joernott.c0de.core.validation.Validation;
import de.joernott.c0de.core.validation.Validator;
import de.joernott.c0de.core.value.IntegerValue;
import de.joernott.c0de.core.value.StructValue;
import de.joernott.c0de.core.value.Value;
import de.joernott.c0depaint.core.PaintError;
import de.joernott.c0depaint.core.config.PaintConfig;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

/** Runs programs with c0de_paint/cpaint on the interpreter of c0de, as the mod does. */
public final class Programs {
    private Programs() {}

    /** The settings of the tests: the defaults, with a small limit. */
    static PaintConfig config = PaintConfig.defaults(PaintConfig.SERVER);

    /** The files of the folder schematics in the tests of c0de_paint/schematics, by name. */
    static final Map<String, byte[]> SCHEMATICS = new java.util.concurrent.ConcurrentHashMap<>();

    /** The folder of the tests: read at once, without a client. */
    static final SchematicsLibrary.Files FILES = new SchematicsLibrary.Files() {
        @Override
        public NativeResult read(NativeCall call, String name, long maxBytes, Function<byte[], NativeResult> then) {
            byte[] bytes = SCHEMATICS.get(name);
            if (bytes == null) {
                return NativeResult.error(PaintError.SCHEMATIC_NOT_FOUND.code(), "There is no " + name);
            }
            return then.apply(bytes);
        }

        @Override
        public NativeResult list(NativeCall call, String folder, Function<List<String>, NativeResult> then) {
            return then.apply(SCHEMATICS.keySet().stream()
                    .filter(name -> folder.isEmpty() || name.startsWith(folder + "/"))
                    .toList());
        }
    };

    /** The libraries with the settings of the tests. */
    public static LibraryRegistry libraries() {
        return new LibraryRegistry()
                .add(new CpaintLibrary(() -> config))
                .add(new SchematicsLibrary(() -> config, FILES, SchematicsLibrary.Server.NONE));
    }

    /** A program with the header and INCLUDE of the library, and the function main with its body and results. */
    public static String program(String results, String body) {
        return "PROGRAM painting\nNOOP\nINCLUDE c0de_paint/cpaint\nINCLUDE c0de_paint/schematics\nNOOP\nFUNCTION main\n"
                + results + "BEGIN\n" + body.stripIndent().indent(4) + "END FUNCTION\n";
    }

    /** Asserts that the program is valid and runs it until it ends. */
    public static Cursor run(String text) {
        Validation validation = Validator.validate(Notation.parse(text), libraries());
        assertTrue(validation.problems().isEmpty(), () -> "invalid: " + validation.problems() + "\n" + text);
        Program program = ProgramParser.parse(Notation.parse(text));
        Cursor cursor = new Cursor(program, "main", Map.of(), settings());
        cursor.run(1_000_000);
        assertTrue(cursor.ended(), "The program didn't end");
        return cursor;
    }

    /** The settings of a cursor: a fixed clock and seed. */
    public static CursorSettings settings() {
        return new CursorSettings(
                CursorSettings.DEFAULT_STACK_SIZE,
                libraries(),
                Clock.fixed(Instant.ofEpochMilli(1792411509250L), ZoneOffset.UTC),
                ZoneId.of("UTC"),
                null,
                42);
    }

    /** Runs the function main with the results and the body, and returns its results. */
    public static Map<String, Value> results(String results, String body) {
        Cursor cursor = run(program(results, body));
        assertEquals(CursorState.FINISHED, cursor.state(), () -> "crashed: " + cursor.error());
        return cursor.results();
    }

    /** Runs the function main, which must crash, and returns its error. */
    public static StructValue error(String body) {
        Cursor cursor = run(program("", body));
        assertEquals(CursorState.CRASHED, cursor.state(), () -> "results: " + cursor.results());
        return cursor.error();
    }

    /** Asserts that the function main crashes with the error. */
    public static void crashes(long code, String body) {
        StructValue error = error(body);
        assertEquals(new IntegerValue(code), error.get("code"), () -> "error: " + error);
    }
}
