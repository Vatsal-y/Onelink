package com.snaplink.util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

/**
 * Hashes IP addresses with SHA-256 for GDPR-compliant storage.
 * No raw IP addresses are persisted — only the one-way hash.
 */
public final class IpHasher {

    private IpHasher() {
        // Utility class — no instantiation
    }

    /**
     * Returns the SHA-256 hex digest of the given IP address.
     *
     * @param ipAddress raw IP address (e.g. "203.0.113.42")
     * @return 64-character lowercase hex string
     */
    public static String hash(String ipAddress) {
        if (ipAddress == null || ipAddress.isBlank()) {
            return "unknown";
        }
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = digest.digest(ipAddress.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hashBytes);
        } catch (NoSuchAlgorithmException e) {
            // SHA-256 is guaranteed by the JDK — should never happen
            throw new RuntimeException("SHA-256 algorithm not available", e);
        }
    }
}
