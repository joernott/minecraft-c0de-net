package de.joernott.c0denet.gametest.neoforge;

//? if neoforge {
/*import de.joernott.c0denet.C0deNet;
import de.joernott.c0denet.gametest.HttpTests;
import de.joernott.c0denet.gametest.SmokeTests;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

// Registers the GameTests with NeoForge, which finds this class by its annotation
@GameTestHolder(C0deNet.MOD_ID)
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
    public static void handbookProgramsAreValid(GameTestHelper helper) {
        SmokeTests.handbookProgramsAreValid(helper);
    }

    @GameTest(template = "empty", timeoutTicks = 2000)
    public static void programsSendRequests(GameTestHelper helper) {
        HttpTests.programsSendRequests(helper);
    }

    @GameTest(template = "empty", timeoutTicks = 2000)
    public static void playersWithoutTheModCantSendRequests(GameTestHelper helper) {
        HttpTests.playersWithoutTheModCantSendRequests(helper);
    }
}
*///?}
