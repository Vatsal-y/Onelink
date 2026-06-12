package com.snaplink.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link UrlValidator}.
 */
class UrlValidatorTest {

    @ParameterizedTest
    @DisplayName("Valid HTTP/HTTPS URLs are accepted")
    @ValueSource(strings = {
            "https://www.google.com",
            "https://example.com/path?query=1&foo=bar",
            "http://sub.domain.example.org/page",
            "https://github.com/user/repo",
            "https://example.com:8080/api"
    })
    void validUrls(String url) {
        assertTrue(UrlValidator.isValid(url), "Should accept: " + url);
        assertNull(UrlValidator.validate(url));
    }

    @ParameterizedTest
    @DisplayName("Localhost URLs are rejected")
    @ValueSource(strings = {
            "http://localhost",
            "http://localhost:8080/path",
            "https://localhost/api",
            "http://127.0.0.1",
            "http://127.0.0.1:3000"
    })
    void localhostRejected(String url) {
        assertFalse(UrlValidator.isValid(url), "Should reject localhost: " + url);
        assertNotNull(UrlValidator.validate(url));
    }

    @ParameterizedTest
    @DisplayName("Private IP addresses are rejected")
    @ValueSource(strings = {
            "http://10.0.0.1",
            "http://172.16.0.1",
            "http://172.31.255.255",
            "http://192.168.1.1",
            "http://192.168.0.100:8080/api"
    })
    void privateIpsRejected(String url) {
        assertFalse(UrlValidator.isValid(url), "Should reject private IP: " + url);
    }

    @ParameterizedTest
    @DisplayName("Non-HTTP schemes are rejected")
    @ValueSource(strings = {
            "ftp://example.com/file.txt",
            "file:///etc/passwd",
            "javascript:alert(1)",
            "data:text/html,<h1>hi</h1>"
    })
    void nonHttpSchemesRejected(String url) {
        assertFalse(UrlValidator.isValid(url), "Should reject non-HTTP: " + url);
    }

    @Test
    @DisplayName("Null and blank URLs are rejected")
    void nullAndBlankRejected() {
        assertFalse(UrlValidator.isValid(null));
        assertFalse(UrlValidator.isValid(""));
        assertFalse(UrlValidator.isValid("   "));
    }

    @Test
    @DisplayName("Malformed URLs are rejected")
    void malformedRejected() {
        assertFalse(UrlValidator.isValid("not-a-url"));
        assertFalse(UrlValidator.isValid("://missing-scheme.com"));
    }
}
