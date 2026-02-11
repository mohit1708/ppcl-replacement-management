package com.ppcl.replacement.util;

import jakarta.servlet.http.Part;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;

/**
 * Configurable file upload utility.
 * Saves files in: baseDir/year/month/date/filename
 * Dynamically creates year, month & date folders.
 */
public class FileUploadUtil {

    /**
     * Upload a file (from servlet Part) to the configured directory with date-based subfolder structure.
     *
     * @param baseDir  Base directory from AppConstants (e.g., D:\PPC_DOCS\Quotation)
     * @param part     The uploaded file part from multipart request
     * @param prefix   Optional prefix for the saved file name (e.g., "quotation", "creditnote")
     * @return The full path of the saved file
     */
    public static String uploadFile(final String baseDir, final Part part, final String prefix) throws IOException {
        final String originalFileName = extractFileName(part);
        final String extension = getFileExtension(originalFileName);
        final String newFileName = buildFileName(prefix, extension);
        final Path targetDir = buildDatePath(baseDir);

        Files.createDirectories(targetDir);

        final Path targetFile = targetDir.resolve(newFileName);
        try (InputStream input = part.getInputStream()) {
            Files.copy(input, targetFile, StandardCopyOption.REPLACE_EXISTING);
        }

        return targetFile.toString();
    }

    /**
     * Upload a file from a byte array.
     *
     * @param baseDir  Base directory from AppConstants
     * @param data     File content as byte array
     * @param prefix   Prefix for file name
     * @param extension File extension including dot (e.g., ".pdf")
     * @return The full path of the saved file
     */
    public static String uploadFile(final String baseDir, final byte[] data, final String prefix, final String extension) throws IOException {
        final String newFileName = buildFileName(prefix, extension);
        final Path targetDir = buildDatePath(baseDir);

        Files.createDirectories(targetDir);

        final Path targetFile = targetDir.resolve(newFileName);
        Files.write(targetFile, data);

        return targetFile.toString();
    }

    /**
     * Upload a file by copying from an existing source path.
     *
     * @param baseDir    Base directory from AppConstants
     * @param sourcePath Path of the source file to copy
     * @param prefix     Prefix for file name
     * @return The full path of the saved file
     */
    public static String uploadFromPath(final String baseDir, final String sourcePath, final String prefix) throws IOException {
        final Path source = Paths.get(sourcePath);
        if (!Files.exists(source)) {
            throw new IOException("Source file not found: " + sourcePath);
        }

        final String extension = getFileExtension(source.getFileName().toString());
        final String newFileName = buildFileName(prefix, extension);
        final Path targetDir = buildDatePath(baseDir);

        Files.createDirectories(targetDir);

        final Path targetFile = targetDir.resolve(newFileName);
        Files.copy(source, targetFile, StandardCopyOption.REPLACE_EXISTING);

        return targetFile.toString();
    }

    /**
     * Build the date-based directory path: baseDir/yyyy/MM/dd
     */
    private static Path buildDatePath(final String baseDir) {
        final LocalDate today = LocalDate.now();
        return Paths.get(baseDir,
                String.valueOf(today.getYear()),
                String.format("%02d", today.getMonthValue()),
                String.format("%02d", today.getDayOfMonth()));
    }

    /**
     * Build a unique file name with prefix and timestamp.
     */
    private static String buildFileName(final String prefix, final String extension) {
        final String safePrefix = (prefix != null && !prefix.isEmpty()) ? prefix + "_" : "";
        return safePrefix + System.currentTimeMillis() + extension;
    }

    /**
     * Extract file name from Part content-disposition header.
     */
    private static String extractFileName(final Part part) {
        final String contentDisposition = part.getHeader("content-disposition");
        if (contentDisposition != null) {
            for (final String token : contentDisposition.split(";")) {
                if (token.trim().startsWith("filename")) {
                    String name = token.substring(token.indexOf('=') + 1).trim().replace("\"", "");
                    // Handle IE full path
                    final int lastSep = Math.max(name.lastIndexOf('/'), name.lastIndexOf('\\'));
                    if (lastSep >= 0) {
                        name = name.substring(lastSep + 1);
                    }
                    return name;
                }
            }
        }
        return "upload.pdf";
    }

    /**
     * Get file extension including the dot.
     */
    private static String getFileExtension(final String fileName) {
        final int dotIndex = fileName.lastIndexOf('.');
        if (dotIndex >= 0) {
            return fileName.substring(dotIndex);
        }
        return ".pdf";
    }
}
