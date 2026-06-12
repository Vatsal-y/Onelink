package com.snaplink.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link Base62Encoder}.
 */
class Base62EncoderTest {

    @Test
    @DisplayName("generateRandomCode produces 6-character codes")
    void generateRandomCode_length() {
        String code = Base62Encoder.generateRandomCode();
        assertEquals(6, code.length(), "Code should be exactly 6 characters");
    }

    @RepeatedTest(10)
    @DisplayName("generateRandomCode produces only base-62 characters")
    void generateRandomCode_charset() {
        String code = Base62Encoder.generateRandomCode();
        assertTrue(code.matches("^[0-9A-Za-z]+$"),
                "Code should only contain base-62 characters: " + code);
    }

    @Test
    @DisplayName("encode(0) returns '0'")
    void encode_zero() {
        assertEquals("0", Base62Encoder.encode(0));
    }

    @Test
    @DisplayName("encode(1) returns '1'")
    void encode_one() {
        assertEquals("1", Base62Encoder.encode(1));
    }

    @Test
    @DisplayName("encode(62) returns '10' (base-62)")
    void encode_62() {
        assertEquals("10", Base62Encoder.encode(62));
    }

    @Test
    @DisplayName("encode and decode are inverse operations")
    void encode_decode_roundtrip() {
        long[] testValues = {0, 1, 61, 62, 100, 999, 123456789L, 56800235583L};
        for (long value : testValues) {
            String encoded = Base62Encoder.encode(value);
            long decoded = Base62Encoder.decode(encoded);
            assertEquals(value, decoded,
                    "Roundtrip failed for " + value + " → " + encoded + " → " + decoded);
        }
    }

    @Test
    @DisplayName("encode rejects negative numbers")
    void encode_negative() {
        assertThrows(IllegalArgumentException.class, () -> Base62Encoder.encode(-1));
    }

    @Test
    @DisplayName("decode rejects null and empty input")
    void decode_nullOrEmpty() {
        assertThrows(IllegalArgumentException.class, () -> Base62Encoder.decode(null));
        assertThrows(IllegalArgumentException.class, () -> Base62Encoder.decode(""));
    }

    @Test
    @DisplayName("decode rejects invalid characters")
    void decode_invalidChars() {
        assertThrows(IllegalArgumentException.class, () -> Base62Encoder.decode("abc!@#"));
    }

    @RepeatedTest(50)
    @DisplayName("generateRandomCode produces unique codes (probabilistic)")
    void generateRandomCode_uniqueness() {
        String code1 = Base62Encoder.generateRandomCode();
        String code2 = Base62Encoder.generateRandomCode();
        // With 62^6 ≈ 56.8B possible codes, collision probability is negligible
        assertNotEquals(code1, code2, "Two random codes should differ");
    }
}
