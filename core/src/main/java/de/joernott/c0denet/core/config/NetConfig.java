package de.joernott.c0denet.core.config;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * The settings of c0de net (docs/concept.md#settings): the server settings in {@code c0de_net.properties} and the
 * client settings in {@code c0de_net-client.properties}, like {@code c0de.properties} of c0de.
 *
 * <p>A missing file is created with the default values. Invalid or unknown entries don't stop the mod: they are
 * reported as {@link #problems()} and the default value is used instead.
 */
public final class NetConfig {
    public static final String SERVER_FILE = "c0de_net.properties";
    public static final String CLIENT_FILE = "c0de_net-client.properties";

    /** The largest answer in bytes. */
    public static final IntSetting MAX_RESPONSE_SIZE = new IntSetting(
            "maxResponseSize",
            1 << 20,
            1 << 10,
            16 << 20,
            "Largest answer to a request in bytes (1024 to 16777216). Larger answers raise RESPONSE_TOO_LARGE.");

    /** The requests that may run at the same time on the client of one player. */
    public static final IntSetting MAX_REQUESTS_PER_PLAYER = new IntSetting(
            "maxRequestsPerPlayer",
            8,
            1,
            64,
            "Requests that may run at the same time on the client of one player (1 to 64); further ones wait.");

    /** Whether the client sends the requests of the processes its player runs. */
    public static final BooleanSetting ALLOW_REQUESTS = new BooleanSetting(
            "allowRequests",
            true,
            "Send the HTTP requests of the c0de processes you run from this computer. With false, they raise",
            "REQUESTS_DISABLED.");

    /** The settings of the server file. */
    public static final List<Setting<?>> SERVER = List.of(MAX_RESPONSE_SIZE, MAX_REQUESTS_PER_PLAYER);

    /** The settings of the client file. */
    public static final List<Setting<?>> CLIENT = List.of(ALLOW_REQUESTS);

    private final Map<Setting<?>, Object> values;
    private final List<String> problems;

    private NetConfig(Map<Setting<?>, Object> values, List<String> problems) {
        this.values = values;
        this.problems = List.copyOf(problems);
    }

    /** The default values of some settings. */
    public static NetConfig defaults(List<Setting<?>> settings) {
        Map<Setting<?>, Object> values = new LinkedHashMap<>();
        settings.forEach(setting -> values.put(setting, setting.defaultValue()));
        return new NetConfig(values, List.of());
    }

    /**
     * Reads the settings from a file, or creates it with the default values if it doesn't exist.
     *
     * @throws IOException if the file can't be read or created
     */
    public static NetConfig load(Path file, List<Setting<?>> settings) throws IOException {
        if (!Files.exists(file)) {
            Files.createDirectories(file.toAbsolutePath().getParent());
            try (Writer writer = Files.newBufferedWriter(file, StandardCharsets.UTF_8)) {
                writer.write(defaultFile(settings));
            }
            return defaults(settings);
        }
        Properties properties = new Properties();
        try (Reader reader = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
            properties.load(reader);
        }
        return of(properties, settings, file.getFileName().toString());
    }

    /** The settings of some properties; {@code source} names them in the problems. */
    public static NetConfig of(Properties properties, List<Setting<?>> settings, String source) {
        Map<Setting<?>, Object> values = new LinkedHashMap<>();
        List<String> problems = new ArrayList<>();
        for (Setting<?> setting : settings) {
            String text = properties.getProperty(setting.key());
            Object value = setting.defaultValue();
            if (text != null) {
                Object parsed = setting.parse(text.strip());
                if (parsed == null) {
                    problems.add(source + ": invalid value '" + text + "' for " + setting.key() + ", using "
                            + setting.defaultValue());
                } else {
                    value = parsed;
                }
            }
            values.put(setting, value);
        }
        for (String key : properties.stringPropertyNames()) {
            if (settings.stream().noneMatch(setting -> setting.key().equals(key))) {
                problems.add(source + ": unknown setting " + key);
            }
        }
        return new NetConfig(values, problems);
    }

    /** The content of a new file: every setting with its description and default value. */
    static String defaultFile(List<Setting<?>> settings) {
        StringBuilder text = new StringBuilder("# Settings of c0de net\n");
        for (Setting<?> setting : settings) {
            text.append('\n');
            for (String line : setting.description()) {
                text.append("# ").append(line).append('\n');
            }
            text.append(setting.key())
                    .append('=')
                    .append(setting.defaultValue())
                    .append('\n');
        }
        return text.toString();
    }

    /** The value of a setting of this file. */
    @SuppressWarnings("unchecked")
    public <T> T get(Setting<T> setting) {
        Object value = values.get(setting);
        if (value == null) {
            throw new IllegalArgumentException("Not a setting of this file: " + setting.key());
        }
        return (T) value;
    }

    /** The invalid and unknown entries of the file. */
    public List<String> problems() {
        return problems;
    }

    /** A setting: its key, default value and description. */
    public sealed interface Setting<T> permits IntSetting, BooleanSetting {
        String key();

        T defaultValue();

        List<String> description();

        /** The value of a text, or null if it is not valid. */
        T parse(String text);
    }

    /** An INTEGER setting with a range. */
    public record IntSetting(String key, Integer defaultValue, int min, int max, List<String> description)
            implements Setting<Integer> {
        public IntSetting(String key, int defaultValue, int min, int max, String... description) {
            this(key, defaultValue, min, max, List.of(description));
        }

        @Override
        public Integer parse(String text) {
            try {
                int value = Integer.parseInt(text);
                return value >= min && value <= max ? value : null;
            } catch (NumberFormatException e) {
                return null;
            }
        }
    }

    /** A BOOLEAN setting. */
    public record BooleanSetting(String key, Boolean defaultValue, List<String> description)
            implements Setting<Boolean> {
        public BooleanSetting(String key, boolean defaultValue, String... description) {
            this(key, defaultValue, List.of(description));
        }

        @Override
        public Boolean parse(String text) {
            return switch (text) {
                case "true" -> true;
                case "false" -> false;
                default -> null;
            };
        }
    }
}
