package de.joernott.c0depaint.gametest.neoforge;

//? if neoforge {
/*import de.joernott.c0depaint.C0dePaint;
import de.joernott.c0depaint.gametest.InventoryTests;
import de.joernott.c0depaint.gametest.RenderTests;
import de.joernott.c0depaint.gametest.SmokeTests;
import de.joernott.c0depaint.gametest.TransformTests;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

// Registers the GameTests with NeoForge, which finds this class by its annotation
@GameTestHolder(C0dePaint.MOD_ID)
@PrefixGameTestTemplate(false)
public class NeoForgeGameTests {
    @GameTest(template = "empty")
    public static void libraryIsRegistered(GameTestHelper helper) {
        SmokeTests.libraryIsRegistered(helper);
    }

    @GameTest(template = "empty")
    public static void programsIncludeTheLibrary(GameTestHelper helper) {
        SmokeTests.programsIncludeTheLibrary(helper);
    }

    @GameTest(template = "empty")
    public static void inventoryBlocksFollowTheSlots(GameTestHelper helper) {
        InventoryTests.inventoryBlocksFollowTheSlots(helper);
    }

    @GameTest(template = "empty")
    public static void statesTurnAndMirror(GameTestHelper helper) {
        TransformTests.statesTurnAndMirror(helper);
    }

    @GameTest(template = "empty", timeoutTicks = 2000)
    public static void renderingWithoutInputNeedsRights(GameTestHelper helper) {
        RenderTests.renderingWithoutInputNeedsRights(helper);
    }

    @GameTest(template = "empty", timeoutTicks = 2000)
    public static void renderingTakesTheBlocksFromTheInput(GameTestHelper helper) {
        RenderTests.renderingTakesTheBlocksFromTheInput(helper);
    }

    @GameTest(template = "empty", timeoutTicks = 2000)
    public static void blocksInTheWayAreMined(GameTestHelper helper) {
        RenderTests.blocksInTheWayAreMined(helper);
    }

    @GameTest(template = "empty", timeoutTicks = 2000)
    public static void theWorldIsCopiedIntoACanvas(GameTestHelper helper) {
        RenderTests.theWorldIsCopiedIntoACanvas(helper);
    }
}
*///?}
