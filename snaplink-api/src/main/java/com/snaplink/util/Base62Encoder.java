package com.snaplink.util;

import java.security.SecureRandom;

/**
 * Generates URL-safe short codes using a base-62 character set.
 *
 * Character set: {@code 0-9 A-Z a-z} (62 characters).
 * A 6-character code yields 62^6 ≈ 56.8 billion possible values,
 * which is sufficient for a URL shortener at this scale.
 */
public final class Base62Encoder {

    private static final String ALPHABET = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
    private static final int BASE = ALPHABET.length(); // 62
    private static final int CODE_LENGTH = 6;
    private static final SecureRandom RANDOM = new SecureRandom();

    private Base62Encoder() {
        // Utility class — no instantiation
    }

    /**
     * Generates a random 6-character base-62 code.
     *
     * @return a random short code such as {@code "aB3xYz"}
     */
    public static String generateRandomCode() {
        StringBuilder sb = new StringBuilder(CODE_LENGTH);
        for (int i = 0; i < CODE_LENGTH; i++) {
            sb.append(ALPHABET.charAt(RANDOM.nextInt(BASE)));
        }
        return sb.toString();
    }

    /**
     * Encodes a positive long value into a base-62 string.
     *
     * @param num a positive number
     * @return the base-62 encoded string
     * @throws IllegalArgumentException if num is negative
     */
    public static String encode(long num) {
        if (num < 0) {
            throw new IllegalArgumentException("Cannot encode negative number: " + num);
        }
        if (num == 0) {
            return String.valueOf(ALPHABET.charAt(0));
        }
        StringBuilder sb = new StringBuilder();
        while (num > 0) {
            sb.append(ALPHABET.charAt((int) (num % BASE)));
            num /= BASE;
        }
        return sb.reverse().toString();
    }

    /**
     * Decodes a base-62 string back to its numeric value.
     *
     * @param str a base-62 encoded string
     * @return the decoded long value
     * @throws IllegalArgumentException if the string contains invalid characters
     */
    public static long decode(String str) {
        if (str == null || str.isEmpty()) {
            throw new IllegalArgumentException("Input string must not be null or empty");
        }
        long result = 0;
        for (char c : str.toCharArray()) {
            int index = ALPHABET.indexOf(c);
            if (index < 0) {
                throw new IllegalArgumentException("Invalid base-62 character: " + c);
            }
            result = result * BASE + index;
        }
        return result;
    }
}
