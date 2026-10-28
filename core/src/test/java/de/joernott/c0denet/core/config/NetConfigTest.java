package de.joernott.c0denet.core.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.io.StringReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/** The settings files of the server and the client (task 3.5). */
class NetConfigTest {
    private static Properties properties(String text) throws IOException {
        Properties properties = new Properties();
        properties.load(new StringReader(text));
        return properties;
    }

    @Test
    void missingFilesAreCreatedWithTheDefaults(@TempDir Path dir) throws IOException {
        Path file = dir.resolve("config").resolve(NetConfig.SERVER_FILE);
        NetConfig config = NetConfig.load(file, NetConfig.SERVER);
        assertEquals(1 << 20, config.get(NetConfig.MAX_RESPONSE_SIZE));
        assertEquals(8, config.get(NetConfig.MAX_REQUESTS_PER_PLAYER));
        String text = Files.readString(file);
        assertTrue(text.contains("maxResponseSize=1048576"), text);
        // The created file reads back without problems
        assertTrue(NetConfig.load(file, NetConfig.SERVER).problems().isEmpty());
    }

    @Test
    void validValuesAreUsed() throws IOException {
        NetConfig config =
                NetConfig.of(properties("maxResponseSize=2048\nmaxRequestsPerPlayer=64"), NetConfig.SERVER, "test");
        assertEquals(2048, config.get(NetConfig.MAX_RESPONSE_SIZE));
        assertEquals(64, config.get(NetConfig.MAX_REQUESTS_PER_PLAYER));
        assertTrue(config.problems().isEmpty());
        assertEquals(
                false,
                NetConfig.of(properties("allowRequests=false"), NetConfig.CLIENT, "test")
                        .get(NetConfig.ALLOW_REQUESTS));
    }

    @Test
    void invalidAndUnknownEntriesAreReported() throws IOException {
        NetConfig config = NetConfig.of(
                properties("maxResponseSize=12\nmaxRequestsPerPlayer=many\ncolour=red"), NetConfig.SERVER, "test");
        assertEquals(1 << 20, config.get(NetConfig.MAX_RESPONSE_SIZE));
        assertEquals(8, config.get(NetConfig.MAX_REQUESTS_PER_PLAYER));
        assertEquals(3, config.problems().size(), config.problems().toString());
        NetConfig client = NetConfig.of(properties("allowRequests=yes"), NetConfig.CLIENT, "test");
        assertEquals(true, client.get(NetConfig.ALLOW_REQUESTS));
        assertEquals(1, client.problems().size());
    }
}
