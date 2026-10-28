package de.joernott.c0denet.gametest.fabric;

//? if fabric {
import de.joernott.c0denet.gametest.SmokeTests;
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
}
//?}
