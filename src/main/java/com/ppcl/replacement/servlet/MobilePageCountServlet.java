package com.ppcl.replacement.servlet;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.ppcl.replacement.util.DBConnectionPool;

import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.Part;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

@WebServlet("/mobile/pageCount")
@MultipartConfig
public class MobilePageCountServlet extends HttpServlet {

    private final Gson gson = new Gson();
    private static final String IMAGE_UPLOAD_DIR_PARAM = "mobile.pagecount.image.dir";
    private static final DateTimeFormatter FILE_TS_FORMAT = DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS");

    @Override
    protected void doPost(final HttpServletRequest request, final HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        final PrintWriter out = response.getWriter();

        final JsonObject responseJson = new JsonObject();
        final JsonArray detailsArray = new JsonArray();

        try {
            final JsonObject requestBody = parseRequestBody(request);
            final String replacementRequestId = requestBody.get("replacementRequestId").getAsString();
            final JsonArray statusArray = requestBody.getAsJsonArray("status");

            final String imagePath = saveImageIfPresent(request, replacementRequestId);
            if (imagePath != null) {
                responseJson.addProperty("uploadedImagePath", imagePath);
            }

            boolean allSuccess = true;

            try (final Connection conn = DBConnectionPool.getConnection()) {
                for (int i = 0; i < statusArray.size(); i++) {
                    final JsonObject printerStatus = statusArray.get(i).getAsJsonObject();
                    final String printerSerialNo = printerStatus.get("printerSerialNo").getAsString();
                    final int pageCount = printerStatus.get("pageCount").getAsInt();
                    final String comment = printerStatus.has("comment") ? printerStatus.get("comment").getAsString() : null;

                    final JsonObject detail = new JsonObject();
                    detail.addProperty("printerSerialNo", printerSerialNo);

                    // Validate: comment is mandatory if pageCount is 0
                    if (pageCount == 0 && (comment == null || comment.trim().isEmpty())) {
                        detail.addProperty("message", "Comment is mandatory when page count is 0");
                        allSuccess = false;
                        detailsArray.add(detail);
                        continue;
                    }

                    // Check if printer exists for this request
                    if (!printerExistsForRequest(conn, replacementRequestId, printerSerialNo)) {
                        detail.addProperty("message", "Invalid Serial No");
                        allSuccess = false;
                        detailsArray.add(detail);
                        continue;
                    }

                    // Update page count
                    final boolean updated = updatePageCount(conn, replacementRequestId, printerSerialNo, pageCount, comment);
                    if (updated) {
                        detail.addProperty("message", "Updated Successfully");
                    } else {
                        detail.addProperty("message", "Update Failed");
                        allSuccess = false;
                    }
                    detailsArray.add(detail);
                }
            }

            responseJson.addProperty("status", allSuccess ? "SUCCESS" : "FAILURE");
            responseJson.add("details", detailsArray);

        } catch (final Exception e) {
            e.printStackTrace();
            responseJson.addProperty("status", "FAILURE");
            final JsonObject errorDetail = new JsonObject();
            errorDetail.addProperty("message", "Error processing request: " + e.getMessage());
            detailsArray.add(errorDetail);
            responseJson.add("details", detailsArray);
        }

        out.print(gson.toJson(responseJson));
        out.flush();
    }

    private JsonObject parseRequestBody(final HttpServletRequest request) throws Exception {
        if (isMultipartRequest(request)) {
            final String replacementRequestId = request.getParameter("replacementRequestId");
            final String statusRaw = request.getParameter("status");
            if (replacementRequestId == null || replacementRequestId.trim().isEmpty()) {
                throw new IllegalArgumentException("replacementRequestId is required");
            }
            if (statusRaw == null || statusRaw.trim().isEmpty()) {
                throw new IllegalArgumentException("status is required");
            }
            final JsonObject body = new JsonObject();
            body.addProperty("replacementRequestId", replacementRequestId);
            body.add("status", JsonParser.parseString(statusRaw).getAsJsonArray());
            return body;
        }

        final StringBuilder sb = new StringBuilder();
        final BufferedReader reader = request.getReader();
        String line;
        while ((line = reader.readLine()) != null) {
            sb.append(line);
        }
        return JsonParser.parseString(sb.toString()).getAsJsonObject();
    }

    private String saveImageIfPresent(final HttpServletRequest request, final String replacementRequestId) throws Exception {
        if (!isMultipartRequest(request)) {
            return null;
        }

        final Part imagePart = request.getPart("image");
        if (imagePart == null || imagePart.getSize() == 0) {
            return null;
        }

        final String originalFileName = getSubmittedFileName(imagePart);
        final String extension = extractSafeExtension(originalFileName);
        final String safeRequestId = replacementRequestId.replaceAll("[^0-9A-Za-z_-]", "_");
        final String generatedName = "pagecount_" + safeRequestId + "_" + FILE_TS_FORMAT.format(LocalDateTime.now()) + extension;

        final ServletContext servletContext = request.getServletContext();
        final String uploadDirConfig = servletContext.getInitParameter(IMAGE_UPLOAD_DIR_PARAM);
        if (uploadDirConfig == null || uploadDirConfig.trim().isEmpty()) {
            throw new IllegalStateException("Missing context-param: " + IMAGE_UPLOAD_DIR_PARAM);
        }

        final Path uploadDir = Paths.get(uploadDirConfig.trim());
        Files.createDirectories(uploadDir);

        final Path targetFile = uploadDir.resolve(generatedName);
        Files.copy(imagePart.getInputStream(), targetFile, StandardCopyOption.REPLACE_EXISTING);
        return targetFile.toString();
    }

    private boolean isMultipartRequest(final HttpServletRequest request) {
        final String contentType = request.getContentType();
        return contentType != null && contentType.toLowerCase(Locale.ROOT).startsWith("multipart/");
    }

    private String getSubmittedFileName(final Part part) {
        final String partName = part.getSubmittedFileName();
        return partName == null ? "" : partName;
    }

    private String extractSafeExtension(final String fileName) {
        if (fileName == null) {
            return ".bin";
        }
        final int dotIndex = fileName.lastIndexOf('.');
        if (dotIndex < 0 || dotIndex == fileName.length() - 1) {
            return ".bin";
        }
        final String ext = fileName.substring(dotIndex).toLowerCase(Locale.ROOT);
        if (ext.matches("\\.[a-z0-9]{1,10}")) {
            return ext;
        }
        return ".bin";
    }

    private boolean printerExistsForRequest(final Connection conn, final String requestId, final String serialNo) throws Exception {
        final String sql = "SELECT COUNT(*) FROM REPLACEMENT_PRINTER_DETAILS " +
                "WHERE REPLACEMENT_REQUEST_ID = ? AND EXISTING_SERIAL = ?";
        try (final PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, Integer.parseInt(requestId));
            ps.setString(2, serialNo);
            try (final ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        }
        return false;
    }

    private boolean updatePageCount(final Connection conn, final String requestId, final String serialNo,
                                    final int pageCount, final String comment) throws Exception {
        final String sql = "UPDATE REPLACEMENT_PRINTER_DETAILS " +
                "SET PAGE_COUNT = ?, PAGE_COUNT_COMMENT = ?, UPDATE_DATE_TIME = SYSTIMESTAMP " +
                "WHERE REPLACEMENT_REQUEST_ID = ? AND EXISTING_SERIAL = ?";
        try (final PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, pageCount);
            ps.setString(2, comment);
            ps.setInt(3, Integer.parseInt(requestId));
            ps.setString(4, serialNo);
            return ps.executeUpdate() > 0;
        }
    }
}
