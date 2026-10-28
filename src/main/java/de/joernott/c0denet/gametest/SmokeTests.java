package de.joernott.c0denet.gametest;

import de.joernott.c0de.core.line.Notation;
import de.joernott.c0de.core.validation.Validation;
import de.joernott.c0de.core.validation.Validator;
import de.joernott.c0de.library.ModLibraries;
import de.joernott.c0denet.C0deNet;
import de.joernott.c0denet.library.HttpLibrary;
import net.minecraft.gametest.framework.GameTestHelper;

/**
 * GameTests that check that the mod is loaded with c0de and its library is available. The loader-specific classes
 * in {@code gametest.fabric} and {@code gametest.neoforge} register them with the GameTest framework.
 */
public final class SmokeTests {
    /** Structure for tests that don't need any blocks: a single air block. */
    public static final String EMPTY_STRUCTURE = C0deNet.MOD_ID + ":empty";

    private SmokeTests() {}

    /** The library is registered with c0de under its name. */
    public static void libraryIsRegistered(GameTestHelper helper) {
        helper.assertTrue(
                ModLibraries.registry().find(HttpLibrary.NAME).isPresent(),
                "The library " + HttpLibrary.NAME + " is not registered");
        helper.succeed();
    }

    /** A program that includes the library is valid. */
    public static void programsIncludeTheLibrary(GameTestHelper helper) {
        Validation validation = Validator.validate(
                Notation.parse(
                        """
                        PROGRAM networking
                        NOOP
                        INCLUDE c0de_net/http
                        NOOP
                        FUNCTION run
                        BEGIN
                            EXIT 0 ;
                        END FUNCTION
                        """),
                ModLibraries.registry());
        for (int z = 0; z < 8; z++) {
            helper.assertTrue(
                    validation.isValid(z),
                    "Line " + z + " is not valid: " + validation.problem(z).map(Object::toString).orElse("?"));
        }
        helper.succeed();
    }
}
