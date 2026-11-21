package de.joernott.c0depaint.gametest;

import de.joernott.c0de.core.cursor.Cursor;
import de.joernott.c0de.core.cursor.CursorSettings;
import de.joernott.c0de.core.cursor.CursorState;
import de.joernott.c0de.core.library.minecraft.MinecraftLibraryDefinition;
import de.joernott.c0de.core.line.Notation;
import de.joernott.c0de.core.program.Program;
import de.joernott.c0de.core.program.ProgramParser;
import de.joernott.c0de.core.validation.Validation;
import de.joernott.c0de.core.validation.Validator;
import de.joernott.c0de.core.value.IntegerValue;
import de.joernott.c0de.core.value.StringValue;
import de.joernott.c0de.core.value.StructValue;
import de.joernott.c0de.core.value.Value;
import de.joernott.c0de.library.ModLibraries;
import de.joernott.c0de.library.ProcessHost;
import java.time.Clock;
import java.time.ZoneId;
import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;

/**
 * Runs programs in GameTests on a cursor of their own, with the process host of c0de as for a trigger block in the test
 * structure, but without an owner, so the program runs with the rights of a block placed by a command.
 */
final class Processes {
    private Processes() {}

    /** A POSITION in the dimension of the trigger block. */
    static StructValue position(BlockPos pos) {
        StructValue position = new StructValue(MinecraftLibraryDefinition.POSITION);
        position.set("x", new IntegerValue(pos.getX()));
        position.set("y", new IntegerValue(pos.getY()));
        position.set("z", new IntegerValue(pos.getZ()));
        position.set("dimension", new StringValue(""));
        return position;
    }

    /** A cursor of the program for the function main, with the inputs. */
    static Cursor cursor(GameTestHelper helper, String text, Map<String, Value> inputs) {
        Validation validation = Validator.validate(Notation.parse(text), ModLibraries.registry());
        helper.assertTrue(validation.problems().isEmpty(), "The program is invalid: " + validation.problems());
        Program program = ProgramParser.parse(Notation.parse(text));
        ProcessHost host = new ProcessHost(
                helper.getLevel().getServer(),
                helper.getLevel().dimension(),
                helper.absolutePos(BlockPos.ZERO),
                null,
                "paint-test",
                "main",
                0,
                null);
        CursorSettings settings = new CursorSettings(
                CursorSettings.DEFAULT_STACK_SIZE, ModLibraries.registry(), Clock.systemUTC(), ZoneId.of("UTC"), host, 42);
        return new Cursor(program, "main", inputs, settings);
    }

    /** Runs the program until it ends and returns its results; it must finish. */
    static Map<String, Value> run(GameTestHelper helper, String text, Map<String, Value> inputs) {
        Cursor cursor = cursor(helper, text, inputs);
        cursor.run(100_000);
        helper.assertTrue(cursor.state() == CursorState.FINISHED, "The program didn't finish: " + cursor.error());
        return cursor.results();
    }

    /**
     * Runs the cursor over ticks, a few steps per tick, and runs the checks when it has ended (at the latest after
     * {@link TestCompat#MAX_WAIT} ticks).
     */
    static void finish(GameTestHelper helper, Cursor cursor, Runnable checks) {
        finish(helper, cursor, checks, TestCompat.MAX_WAIT);
    }

    private static void finish(GameTestHelper helper, Cursor cursor, Runnable checks, int ticksLeft) {
        for (int i = 0; i < 20 && !cursor.ended(); i++) {
            cursor.step();
        }
        if (cursor.ended() || ticksLeft <= 0) {
            checks.run();
        } else {
            helper.runAfterDelay(1, () -> finish(helper, cursor, checks, ticksLeft - 1));
        }
    }
}
