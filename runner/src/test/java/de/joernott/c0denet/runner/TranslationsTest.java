package de.joernott.c0denet.runner;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.junit.jupiter.api.Test;

/**
 * The German translation of the handbook (docs/de/reference/net.md, tasks 6.2 and 6.3) has the structure of the
 * English file, which the Patchouli book and the website c0de.at rely on: the same headings in the same order and
 * levels (names like {@code ### HTTP_GET} unchanged), the same programs (only the text of comments is translated),
 * and images that exist.
 */
class TranslationsTest {
    private static final Path DOCS = Path.of(System.getProperty("c0denet.docs"));
    private static final Path ENGLISH = DOCS.resolve("reference/net.md");
    private static final Path GERMAN = DOCS.resolve("de/reference/net.md");
    private static final Pattern HEADING = Pattern.compile("^(#{1,6}) (.*)$");
    /** Headings that are names: functions, types, errors and libraries. */
    private static final Pattern NAME = Pattern.compile("[A-Z0-9_]+|c0de[a-z_]*/[a-z]+");

    private static final Pattern IMAGE = Pattern.compile("!\\[[^\\]]*\\]\\(([^)]+)\\)");

    private static List<String> lines(Path file) throws IOException {
        return Files.readString(file, StandardCharsets.UTF_8)
                .replace("\r\n", "\n")
                .lines()
                .toList();
    }

    private static List<String> headings(Path file) throws IOException {
        List<String> result = new ArrayList<>();
        boolean code = false;
        for (String line : lines(file)) {
            if (line.startsWith("```")) {
                code = !code;
            } else if (!code && HEADING.matcher(line).matches()) {
                result.add(line.strip());
            }
        }
        return result;
    }

    private static List<String> programs(Path file) throws IOException {
        List<String> result = new ArrayList<>();
        StringBuilder program = null;
        for (String line : lines(file)) {
            if (program != null) {
                if (line.startsWith("```")) {
                    result.add(program.toString());
                    program = null;
                } else {
                    int comment = line.indexOf("//");
                    program.append(comment < 0 ? line : line.substring(0, comment + 2))
                            .append('\n');
                }
            } else if (line.startsWith("```")) {
                program = new StringBuilder(line).append('\n');
            }
        }
        return result;
    }

    @Test
    void theHeadingsMatchTheEnglishFile() throws IOException {
        List<String> english = headings(ENGLISH);
        List<String> german = headings(GERMAN);
        assertEquals(english.size(), german.size(), "number of headings");
        for (int i = 0; i < english.size(); i++) {
            String level = english.get(i).substring(0, english.get(i).indexOf(' '));
            assertEquals(level, german.get(i).substring(0, german.get(i).indexOf(' ')), "level of " + german.get(i));
            if (NAME.matcher(english.get(i).substring(level.length() + 1)).matches()) {
                assertEquals(english.get(i), german.get(i), "names in headings stay unchanged");
            }
        }
    }

    @Test
    void theProgramsAreTheSame() throws IOException {
        assertEquals(programs(ENGLISH), programs(GERMAN));
    }

    @Test
    void theImagesExist() throws IOException {
        Matcher matcher = IMAGE.matcher(Files.readString(GERMAN, StandardCharsets.UTF_8));
        int images = 0;
        while (matcher.find()) {
            images++;
            Path image = GERMAN.getParent().resolve(matcher.group(1)).normalize();
            assertTrue(Files.exists(image), "The image " + matcher.group(1) + " doesn't exist");
        }
        assertTrue(images > 0, "The translation has no images");
    }
}
