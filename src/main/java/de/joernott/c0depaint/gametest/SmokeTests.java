package de.joernott.c0depaint.gametest;

import de.joernott.c0de.core.line.Notation;
import de.joernott.c0de.core.validation.Validation;
import de.joernott.c0de.core.validation.Validator;
import de.joernott.c0de.library.ModLibraries;
import de.joernott.c0depaint.C0dePaint;
import de.joernott.c0depaint.core.library.CpaintLibrary;
import net.minecraft.gametest.framework.GameTestHelper;

/**
 * GameTests that check that the mod is loaded with c0de and its library is available (task 2.4). The loader-specific
 * classes in {@code gametest.fabric} and {@code gametest.neoforge} register them with the GameTest framework.
 */
public final class SmokeTests {
    /** Structure for tests that don't need any blocks: a single air block. */
    public static final String EMPTY_STRUCTURE = C0dePaint.MOD_ID + ":empty";

    /** An empty structure of 8 x 8 x 8 blocks, for buildings higher than the empty one. */
    public static final String TALL_STRUCTURE = C0dePaint.MOD_ID + ":tall";

    private SmokeTests() {}

    /** The library is registered with c0de under its name, with its types. */
    public static void libraryIsRegistered(GameTestHelper helper) {
        var library = ModLibraries.registry().find(CpaintLibrary.NAME);
        helper.assertTrue(library.isPresent(), "The library " + CpaintLibrary.NAME + " is not registered");
        helper.assertTrue(
                library.get().libraryTypes().keySet().containsAll(java.util.Set.of("CANVAS", "STYLE")),
                "The library types are missing: " + library.get().libraryTypes().keySet());
        helper.assertTrue(
                library.get().types().keySet().containsAll(java.util.Set.of("POINT", "BLOCK")),
                "The structs are missing: " + library.get().types().keySet());
        helper.succeed();
    }

    /** A program that includes the library and declares variables of its types is valid. */
    public static void programsIncludeTheLibrary(GameTestHelper helper) {
        String program =
                """
                PROGRAM painting
                NOOP
                INCLUDE c0de_paint/cpaint
                NOOP
                FUNCTION run
                BEGIN
                    VARIABLE c TYPE c0de_paint/cpaint/CANVAS
                    VARIABLE s TYPE c0de_paint/cpaint/STYLE
                    VARIABLE p STRUCT c0de_paint/cpaint/POINT
                    VARIABLE b STRUCT c0de_paint/cpaint/BLOCK
                    EXIT 0 ;
                END FUNCTION
                """;
        Validation validation = Validator.validate(Notation.parse(program), ModLibraries.registry());
        helper.assertTrue(validation.problems().isEmpty(), "The program is not valid: " + validation.problems());
        helper.succeed();
    }
}
