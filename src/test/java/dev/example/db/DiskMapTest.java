package dev.example.db;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class DiskMapTest {
    @TempDir
    Path directory;

    @Test
    void storesAndReloadsValues() throws Exception {
        Path file = directory.resolve("map.db");

        try (DiskMap<String, String> map = new DiskMap<>(file)) {
            map.put("language", "Java");
            assertEquals("Java", map.get("language"));
        }

        try (DiskMap<String, String> map = new DiskMap<>(file)) {
            assertEquals("Java", map.get("language"));
        }
    }

    @Test
    void removesValues() throws Exception {
        try (DiskMap<String, String> map =
                     new DiskMap<>(directory.resolve("map.db"))) {
            map.put("key", "value");
            assertEquals("value", map.remove("key"));
            assertNull(map.get("key"));
        }
    }
}