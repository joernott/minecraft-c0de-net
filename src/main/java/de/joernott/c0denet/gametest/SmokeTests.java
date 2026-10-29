package de.joernott.c0denet.gametest;

import de.joernott.c0de.core.line.Notation;
import de.joernott.c0de.core.validation.Validation;
import de.joernott.c0de.core.validation.Validator;
import de.joernott.c0de.library.ModLibraries;
import de.joernott.c0denet.C0deNet;
import de.joernott.c0denet.library.HttpLibrary;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.minecraft.gametest.framework.GameTestHelper;

/**
 * GameTests that check that the mod is loaded with c0de and its library is available. The loader-specific classes
 * in {@code gametest.fabric} and {@code gametest.neoforge} register them with the GameTest framework.
 */
public final class SmokeTests {
    /** Structure for tests that don't need any blocks: a single air block. */
    public static final String EMPTY_STRUCTURE = C0deNet.MOD_ID + ":empty";

    /** The system property with the path of the handbook, set by the GameTest runs of the build. */
    public static final String HANDBOOK = "c0denet.handbook";

    private static final Pattern CODE = Pattern.compile("```c0de\\n(.*?)```", Pattern.DOTALL);

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

    /**
     * The complete programs of the handbook (docs/reference/net.md: the code blocks that start with PROGRAM) are
     * valid.
     */
    public static void handbookProgramsAreValid(GameTestHelper helper) {
        String path = System.getProperty(HANDBOOK);
        helper.assertTrue(path != null, "The system property " + HANDBOOK + " is not set");
        String handbook;
        try {
            handbook = Files.readString(Path.of(path), StandardCharsets.UTF_8).replace("\r\n", "\n");
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        List<String> programs = new ArrayList<>();
        Matcher matcher = CODE.matcher(handbook);
        while (matcher.find()) {
            if (matcher.group(1).startsWith("PROGRAM")) {
                programs.add(matcher.group(1));
            }
        }
        helper.assertTrue(!programs.isEmpty(), "The handbook has no complete programs");
        for (String program : programs) {
            Validation validation = Validator.validate(Notation.parse(program), ModLibraries.registry());
            helper.assertTrue(
                    validation.problems().isEmpty(),
                    program.lines().findFirst().orElse("") + ": " + validation.problems());
        }
        helper.succeed();
    }
}
