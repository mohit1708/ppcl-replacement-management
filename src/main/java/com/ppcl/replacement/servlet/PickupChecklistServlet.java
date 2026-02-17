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

@WebServlet("/pickup/checklist")
public class PickupChecklistServlet extends HttpServlet {

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
                    final JsonObject checklist = statusArray.get(i).getAsJsonObject();
                    final String printerSerialNo = checklist.get("printerSerialNo").getAsString();
                    final boolean printer = checklist.get("printer").getAsBoolean();
                    final boolean powerCable = checklist.get("powerCable").getAsBoolean();
                    final boolean lanCable = checklist.get("lanCable").getAsBoolean();
                    final boolean tray = checklist.get("tray").getAsBoolean();
                    final int emptyCartridges = checklist.get("emptyCartridges").getAsInt();
                    final int unusedCartridge = checklist.get("unusedCartridge").getAsInt();

                    final JsonObject detail = new JsonObject();
                    detail.addProperty("printerSerialNo", printerSerialNo);

                    // Check if printer exists for this request
                    if (!printerExistsForRequest(conn, replacementRequestId, printerSerialNo)) {
                        detail.addProperty("message", "Invalid Serial No");
                        allSuccess = false;
                        detailsArray.add(detail);
                        continue;
                    }

                    // Update checklist and set status to 2 on success
                    final boolean updated = updatePullbackChecklist(conn, replacementRequestId, printerSerialNo,
                            printer, powerCable, lanCable, tray, emptyCartridges, unusedCartridge, true);

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

    private boolean updatePullbackChecklist(final Connection conn, final String requestId, final String serialNo,
                                            final boolean printer, final boolean powerCable, final boolean lanCable,
                                            final boolean tray, final int emptyCartridges, final int unusedCartridge,
                                            final boolean updateStatus) throws Exception {

        // Check if pullback record exists
        final String checkSql = "SELECT ID FROM REPLACEMENT_PULLBACK WHERE REPLACEMENT_REQ_ID = ? AND P_SERIAL_NO = ?";
        Integer pullbackId = null;

        try (final PreparedStatement ps = conn.prepareStatement(checkSql)) {
            ps.setInt(1, Integer.parseInt(requestId));
            ps.setString(2, serialNo);
            try (final ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    pullbackId = rs.getInt("ID");
                }
            }
        }

        if (pullbackId != null) {
            if (updateStatus) {
                // Update existing record with status = 2
                final String updateSql = "UPDATE REPLACEMENT_PULLBACK SET " +
                        "PRINTER = ?, POWER_CABLE = ?, LAN_CABLE = ?, TRAY = ?, " +
                        "EMPTY_CARTRIDGE = ?, UNUSED_CARTRIDGE = ?, STATUS = ? " +
                        "WHERE ID = ?";
                try (final PreparedStatement ps = conn.prepareStatement(updateSql)) {
                    ps.setInt(1, printer ? 1 : 0);
                    ps.setInt(2, powerCable ? 1 : 0);
                    ps.setInt(3, lanCable ? 1 : 0);
                    ps.setInt(4, tray ? 1 : 0);
                    ps.setInt(5, emptyCartridges);
                    ps.setInt(6, unusedCartridge);
                    ps.setInt(7, 2);
                    ps.setInt(8, pullbackId);
                    return ps.executeUpdate() > 0;
                }
            } else {
                // Update existing record without changing status
                final String updateSql = "UPDATE REPLACEMENT_PULLBACK SET " +
                        "PRINTER = ?, POWER_CABLE = ?, LAN_CABLE = ?, TRAY = ?, " +
                        "EMPTY_CARTRIDGE = ?, UNUSED_CARTRIDGE = ? " +
                        "WHERE ID = ?";
                try (final PreparedStatement ps = conn.prepareStatement(updateSql)) {
                    ps.setInt(1, printer ? 1 : 0);
                    ps.setInt(2, powerCable ? 1 : 0);
                    ps.setInt(3, lanCable ? 1 : 0);
                    ps.setInt(4, tray ? 1 : 0);
                    ps.setInt(5, emptyCartridges);
                    ps.setInt(6, unusedCartridge);
                    ps.setInt(7, pullbackId);
                    return ps.executeUpdate() > 0;
                }
            }
        }
        return false;
    }
}
