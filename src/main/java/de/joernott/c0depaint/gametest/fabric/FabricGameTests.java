package de.joernott.c0depaint.gametest.fabric;

//? if fabric {
import de.joernott.c0depaint.gametest.InventoryTests;
import de.joernott.c0depaint.gametest.RenderTests;
import de.joernott.c0depaint.gametest.SchematicTests;
import de.joernott.c0depaint.gametest.SmokeTests;
import de.joernott.c0depaint.gametest.TransformTests;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;

/**
 * Registers the GameTests with Fabric (entry point {@code fabric-gametest}). Since 1.21.5 the annotation is the one
 * of Fabric, with other attribute names: replacements in stonecutter.gradle.kts. All tests have a timeout, because
 * the default timeouts differ.
 */
public class FabricGameTests {
    @GameTest(template = SmokeTests.EMPTY_STRUCTURE, timeoutTicks = 100)
    public void libraryIsRegistered(GameTestHelper helper) {
        SmokeTests.libraryIsRegistered(helper);
    }

    @GameTest(template = SmokeTests.EMPTY_STRUCTURE, timeoutTicks = 100)
    public void programsIncludeTheLibrary(GameTestHelper helper) {
        SmokeTests.programsIncludeTheLibrary(helper);
    }

    @GameTest(template = SmokeTests.EMPTY_STRUCTURE, timeoutTicks = 200)
    public void farAreasAreLoadedForRendering(GameTestHelper helper) {
        RenderTests.farAreasAreLoadedForRendering(helper);
    }

    @GameTest(template = SmokeTests.EMPTY_STRUCTURE, timeoutTicks = 200)
    public void inventoriesAreFilledFromTheInput(GameTestHelper helper) {
        RenderTests.inventoriesAreFilledFromTheInput(helper);
    }

    @GameTest(template = SmokeTests.EMPTY_STRUCTURE, timeoutTicks = 200)
    public void schematicsAreReadUpgradedAndCounted(GameTestHelper helper) {
        SchematicTests.schematicsAreReadUpgradedAndCounted(helper);
    }

    @GameTest(template = SmokeTests.EMPTY_STRUCTURE, timeoutTicks = 100)
    public void schematicsNeedThePlayerOnline(GameTestHelper helper) {
        SchematicTests.schematicsNeedThePlayerOnline(helper);
    }

    @GameTest(template = SmokeTests.EMPTY_STRUCTURE, timeoutTicks = 100)
    public void inventoryBlocksFollowTheSlots(GameTestHelper helper) {
        InventoryTests.inventoryBlocksFollowTheSlots(helper);
    }

    @GameTest(template = SmokeTests.EMPTY_STRUCTURE, timeoutTicks = 100)
    public void statesTurnAndMirror(GameTestHelper helper) {
        TransformTests.statesTurnAndMirror(helper);
    }

    @GameTest(template = SmokeTests.EMPTY_STRUCTURE, timeoutTicks = 2000)
    public void renderingWithoutInputNeedsRights(GameTestHelper helper) {
        RenderTests.renderingWithoutInputNeedsRights(helper);
    }

    @GameTest(template = SmokeTests.TALL_STRUCTURE, timeoutTicks = 2000)
    public void theFirstProgramOfTheHandbookBuildsAHut(GameTestHelper helper) {
        RenderTests.theFirstProgramOfTheHandbookBuildsAHut(helper);
    }

    @GameTest(template = SmokeTests.EMPTY_STRUCTURE, timeoutTicks = 2000)
    public void renderingTakesTheBlocksFromTheInput(GameTestHelper helper) {
        RenderTests.renderingTakesTheBlocksFromTheInput(helper);
    }

    @GameTest(template = SmokeTests.EMPTY_STRUCTURE, timeoutTicks = 2000)
    public void blocksInTheWayAreMined(GameTestHelper helper) {
        RenderTests.blocksInTheWayAreMined(helper);
    }

    @GameTest(template = SmokeTests.EMPTY_STRUCTURE, timeoutTicks = 2000)
    public void theWorldIsCopiedIntoACanvas(GameTestHelper helper) {
        RenderTests.theWorldIsCopiedIntoACanvas(helper);
    }
}
//?}
