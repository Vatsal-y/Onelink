package com.snaplink.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link IpHasher}.
 */
class IpHasherTest {

    @Test
    @DisplayName("hash produces consistent 64-character hex output")
    void hash_consistency() {
        String ip = "203.0.113.42";
        String hash1 = IpHasher.hash(ip);
        String hash2 = IpHasher.hash(ip);

        assertEquals(hash1, hash2, "Same IP should produce same hash");
        assertEquals(64, hash1.length(), "SHA-256 hex digest should be 64 chars");
        assertTrue(hash1.matches("^[0-9a-f]+$"), "Hash should be lowercase hex");
    }

    @Test
    @DisplayName("Different IPs produce different hashes")
    void hash_differentIps() {
        String hash1 = IpHasher.hash("203.0.113.42");
        String hash2 = IpHasher.hash("198.51.100.1");
        assertNotEquals(hash1, hash2);
    }

    @Test
    @DisplayName("Null or blank IP returns 'unknown'")
    void hash_nullOrBlank() {
        assertEquals("unknown", IpHasher.hash(null));
        assertEquals("unknown", IpHasher.hash(""));
        assertEquals("unknown", IpHasher.hash("   "));
    }
}
