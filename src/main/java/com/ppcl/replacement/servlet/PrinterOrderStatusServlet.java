package com.ppcl.replacement.servlet;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.ppcl.replacement.util.DBConnectionPool;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

@WebServlet("/printerOrder/status")
public class PrinterOrderStatusServlet extends HttpServlet {

    private final Gson gson = new Gson();

    @Override
    protected void doPost(final HttpServletRequest request, final HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        final PrintWriter out = response.getWriter();

        final JsonObject responseJson = new JsonObject();
        final JsonArray detailsArray = new JsonArray();

        try {
            final StringBuilder sb = new StringBuilder();
            final BufferedReader reader = request.getReader();
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }

            final JsonObject requestBody = JsonParser.parseString(sb.toString()).getAsJsonObject();
            final String replacementRequestId = requestBody.get("replacementRequestId").getAsString();
            final JsonArray statusArray = requestBody.getAsJsonArray("status");

            boolean allSuccess = true;

            try (final Connection conn = DBConnectionPool.getConnection()) {
                for (int i = 0; i < statusArray.size(); i++) {
                    final JsonObject printerStatus = statusArray.get(i).getAsJsonObject();
                    final String printerSerialNo = printerStatus.get("printerSerialNo").getAsString();
                    final String destinationStage = printerStatus.get("destinationStage").getAsString();

                    final JsonObject detail = new JsonObject();
                    detail.addProperty("printerSerialNo", printerSerialNo);

                    // Validate destination stage
                    final int stageId = getStageIdByCode(conn, destinationStage);
                    if (stageId == -1) {
                        detail.addProperty("message", "Invalid stage: " + destinationStage);
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

                    // Update printer stage
                    final boolean updated = updatePrinterStage(conn, replacementRequestId, printerSerialNo, stageId, destinationStage);
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

    private int getStageIdByCode(final Connection conn, final String stageCode) throws Exception {
        final String fullStageCode = stageCode.toUpperCase();
        final String sql = "SELECT ID FROM PRINTER_STAGE_MASTER WHERE STAGE = ? ";
        try (final PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, fullStageCode);
            try (final ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("ID");
                }
            }
        }
        return -1;
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

    private boolean updatePrinterStage(final Connection conn, final String requestId, final String serialNo,
                                       final int stageId, final String stageCode) throws Exception {
        final String sql = "UPDATE REPLACEMENT_PRINTER_DETAILS " +
                "SET PRINTER_STAGE_ID = ?, UPDATE_DATE_TIME = SYSTIMESTAMP " +
                "WHERE REPLACEMENT_REQUEST_ID = ? AND EXISTING_SERIAL = ?";
        try (final PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, stageId);
//            ps.setString(2, "PRINTER_" + stageCode.toUpperCase());
            ps.setInt(2, Integer.parseInt(requestId));
            ps.setString(3, serialNo);
            return ps.executeUpdate() > 0;
        }
    }
}
