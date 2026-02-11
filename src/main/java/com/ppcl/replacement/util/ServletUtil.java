package com.ppcl.replacement.util;

import com.google.gson.Gson;

import jakarta.servlet.http.HttpServletRequest;
import java.io.BufferedReader;
import java.io.IOException;

public class ServletUtil {

    private static final Gson gson = new Gson();

    /**
     * Read the full request body as a string.
     */
    public static String readRequestBody(final HttpServletRequest request) throws IOException {
        final StringBuilder sb = new StringBuilder();
        String line;
        try (final BufferedReader reader = request.getReader()) {
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
        }
        return sb.toString();
    }

    /**
     * Parse JSON request body to the specified class.
     */
    public static <T> T readJson(final HttpServletRequest request, final Class<T> clazz) throws IOException {
        final String body = readRequestBody(request);
        return gson.fromJson(body, clazz);
    }

    /**
     * Get a required string parameter. Throws IllegalArgumentException if missing or empty.
     */
    public static String requireParam(final HttpServletRequest request, final String name) {
        final String value = request.getParameter(name);
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException(name + " is required");
        }
        return value.trim();
    }

    /**
     * Get a required integer parameter. Throws IllegalArgumentException if missing, empty, or not an integer.
     */
    public static int requireIntParam(final HttpServletRequest request, final String name) {
        final String value = requireParam(request, name);
        try {
            return Integer.parseInt(value);
        } catch (final NumberFormatException e) {
            throw new IllegalArgumentException(name + " must be a valid integer");
        }
    }

    /**
     * Get an optional integer parameter. Returns null if missing or empty.
     */
    public static Integer optionalIntParam(final HttpServletRequest request, final String name) {
        final String value = request.getParameter(name);
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        try {
            return Integer.parseInt(value.trim());
        } catch (final NumberFormatException e) {
            return null;
        }
    }

    /**
     * Get an optional string parameter. Returns null if missing or empty.
     */
    public static String optionalParam(final HttpServletRequest request, final String name) {
        final String value = request.getParameter(name);
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        return value.trim();
    }

    /**
     * Check if request has JSON content type.
     */
    public static boolean isJsonRequest(final HttpServletRequest request) {
        final String contentType = request.getContentType();
        return contentType != null && contentType.contains("application/json");
    }
}
