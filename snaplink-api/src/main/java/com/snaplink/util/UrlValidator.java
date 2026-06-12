package com.snaplink.util;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * Validates URLs submitted for shortening.
 *
 * Rejects:
 * <ul>
 *   <li>Malformed URLs</li>
 *   <li>Non-HTTP(S) schemes (ftp, file, etc.)</li>
 *   <li>Localhost / loopback addresses</li>
 *   <li>Private IP ranges (10.x, 172.16-31.x, 192.168.x)</li>
 * </ul>
 */
public final class UrlValidator {

    private static final Set<String> ALLOWED_SCHEMES = Set.of("http", "https");

    private static final Pattern PRIVATE_IP_PATTERN = Pattern.compile(
            "^(10\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3})"
                    + "|(172\\.(1[6-9]|2\\d|3[0-1])\\.\\d{1,3}\\.\\d{1,3})"
                    + "|(192\\.168\\.\\d{1,3}\\.\\d{1,3})"
                    + "|(127\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3})$"
    );

    private static final Set<String> BLOCKED_HOSTS = Set.of(
            "localhost", "0.0.0.0", "[::1]"
    );

    private UrlValidator() {
        // Utility class — no instantiation
    }

    /**
     * Validates the given URL string.
     *
     * @param urlString the URL to validate
     * @return {@code true} if the URL is valid and safe to shorten
     */
    public static boolean isValid(String urlString) {
        if (urlString == null || urlString.isBlank()) {
            return false;
        }

        try {
            URI uri = new URI(urlString);
            URL url = uri.toURL();

            // Must be http or https
            String scheme = url.getProtocol().toLowerCase();
            if (!ALLOWED_SCHEMES.contains(scheme)) {
                return false;
            }

            // Must have a host
            String host = url.getHost();
            if (host == null || host.isBlank()) {
                return false;
            }

            // Block localhost and loopback
            String hostLower = host.toLowerCase();
            if (BLOCKED_HOSTS.contains(hostLower)) {
                return false;
            }

            // Block private IP ranges
            if (PRIVATE_IP_PATTERN.matcher(host).matches()) {
                return false;
            }

            return true;
        } catch (URISyntaxException | MalformedURLException | IllegalArgumentException e) {
            return false;
        }
    }

    /**
     * Validates the given URL string and returns a descriptive error message
     * if invalid, or {@code null} if valid.
     *
     * @param urlString the URL to validate
     * @return error message if invalid, {@code null} if valid
     */
    public static String validate(String urlString) {
        if (urlString == null || urlString.isBlank()) {
            return "URL must not be empty";
        }

        try {
            URI uri = new URI(urlString);
            URL url = uri.toURL();

            String scheme = url.getProtocol().toLowerCase();
            if (!ALLOWED_SCHEMES.contains(scheme)) {
                return "URL scheme must be http or https, got: " + scheme;
            }

            String host = url.getHost();
            if (host == null || host.isBlank()) {
                return "URL must have a valid host";
            }

            String hostLower = host.toLowerCase();
            if (BLOCKED_HOSTS.contains(hostLower)) {
                return "Localhost URLs are not allowed";
            }

            if (PRIVATE_IP_PATTERN.matcher(host).matches()) {
                return "Private IP addresses are not allowed";
            }

            return null; // Valid
        } catch (URISyntaxException | MalformedURLException | IllegalArgumentException e) {
            return "Malformed URL: " + e.getMessage();
        }
    }
}
