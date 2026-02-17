package com.ppcl.replacement.dao;

import com.ppcl.replacement.model.*;

import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class PrinterPullbackDAO extends BaseDAO {

    public boolean isValidReplacementRequest(final int reqId) throws SQLException {
        final String sql = "SELECT 1 FROM REPLACEMENT_REQUEST WHERE ID = ?";
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            conn = getConnection();
            ps = conn.prepareStatement(sql);
            ps.setInt(1, reqId);
            rs = ps.executeQuery();
            return rs.next();
        } finally {
            closeResources(conn, ps, rs);
        }
    }

    public Integer getExistingPullbackId(final int reqId, final String serialNo) throws SQLException {
        final String sql = "SELECT ID FROM REPLACEMENT_PULLBACK WHERE REPLACEMENT_REQ_ID = ? AND P_SERIAL_NO = ?";
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            conn = getConnection();
            ps = conn.prepareStatement(sql);
            ps.setInt(1, reqId);
            ps.setString(2, serialNo);
            rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getInt("ID");
            }
        } finally {
            closeResources(conn, ps, rs);
        }
        return null;
    }

    public int insertPullback(final PrinterPullback p) throws SQLException {
        Connection conn = null;
        try {
            conn = getConnection();
            return insertPullback(conn, p);
        } finally {
            if (conn != null) {
                try { conn.close(); } catch (final SQLException e) { logger.error("Error closing Connection", e); }
            }
        }
    }

    /**
     * Insert pullback using a shared connection (for transaction management).
     */
    public int insertPullback(final Connection conn, final PrinterPullback p) throws SQLException {
        final String sql = """
                INSERT INTO REPLACEMENT_PULLBACK (REPLACEMENT_REQ_ID, CALL_ID, CLIENT_DOT_ID, LOCATION,
                    P_MODEL, P_SERIAL_NO, PICKED_BY, STATUS, COURIER_ID, COURIER_NAME, CONSIGNMENT_NO,
                    DISPATCH_DATE, ARRIVAL_DATE, RECEIPT, DESTINATION_BRANCH, TRANSPORT_MODE,
                    CONTACT_PERSON, CONTACT_NUMBER, COMMENTS, PRINTER, POWER_CABLE, LAN_CABLE, TRAY,
                    EMPTY_CARTRIDGE, UNUSED_CARTRIDGE, PULLBACK_MODE, REPLACEMENT_PRINTER_DETAILS_ID)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """;

        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            ps = conn.prepareStatement(sql, new String[]{"ID"});
            setPullbackParams(ps, p, false);
            setOptionalInt(ps, 27, p.getReplacementPrinterDetailsId());
            ps.executeUpdate();

            rs = ps.getGeneratedKeys();
            if (rs.next()) {
                return rs.getInt(1);
            }
        } finally {
            if (rs != null) { try { rs.close(); } catch (final SQLException e) { logger.error("Error closing RS", e); } }
            if (ps != null) { try { ps.close(); } catch (final SQLException e) { logger.error("Error closing PS", e); } }
        }
        return -1;
    }

    public boolean updatePullback(final PrinterPullback p) throws SQLException {
        final String sql = """
                UPDATE REPLACEMENT_PULLBACK SET CALL_ID = ?, CLIENT_DOT_ID = ?, LOCATION = ?,
                    P_MODEL = ?, PICKED_BY = ?, STATUS = ?, COURIER_ID = ?, COURIER_NAME = ?,
                    CONSIGNMENT_NO = ?, DISPATCH_DATE = ?, ARRIVAL_DATE = ?, RECEIPT = ?,
                    DESTINATION_BRANCH = ?, TRANSPORT_MODE = ?, CONTACT_PERSON = ?, CONTACT_NUMBER = ?,
                    COMMENTS = ?, PRINTER = ?, POWER_CABLE = ?, LAN_CABLE = ?, TRAY = ?,
                    EMPTY_CARTRIDGE = ?, UNUSED_CARTRIDGE = ?, PULLBACK_MODE = ?,
                    REPLACEMENT_PRINTER_DETAILS_ID = ?
                WHERE ID = ?
                """;

        Connection conn = null;
        PreparedStatement ps = null;

        try {
            conn = getConnection();
            ps = conn.prepareStatement(sql);
            setPullbackParams(ps, p, true);
            return ps.executeUpdate() > 0;
        } finally {
            closeResources(conn, ps, null);
        }
    }

    public List<PrinterPullback> getPullbacksByReqId(final int reqId, final String serialNo) throws SQLException {
        final List<PrinterPullback> list = new ArrayList<>();
        final StringBuilder sql = new StringBuilder("""
                SELECT p.ID, p.REPLACEMENT_REQ_ID, p.CALL_ID, p.CLIENT_DOT_ID, p.LOCATION,
                       p.P_MODEL, p.P_SERIAL_NO, p.PICKED_BY, p.STATUS, p.COURIER_ID,
                       p.COURIER_NAME, p.CONSIGNMENT_NO, p.DISPATCH_DATE, p.ARRIVAL_DATE,
                       p.RECEIPT, p.DESTINATION_BRANCH, p.TRANSPORT_MODE, p.CONTACT_PERSON,
                       p.CONTACT_NUMBER, p.COMMENTS, p.PRINTER, p.POWER_CABLE, p.LAN_CABLE,
                       p.TRAY, p.EMPTY_CARTRIDGE,
                       poi.CART_PICKUP_QUANITY AS UNUSED_CARTRIDGE,
                       p.PULLBACK_MODE,
                       p.PRINTER_QC, p.POWER_CABLE_QC, p.LAN_CABLE_QC, p.TRAY_QC, p.EMPTY_CARTRIDGE_QC, p.UNUSED_CARTRIDGE_QC,
                       p.REPLACEMENT_PRINTER_DETAILS_ID,
                       c.NAME AS CLIENT_NAME,
                       pm.MODEL_NAME AS PRINTER_MODEL_NAME,
                       'REQ-' || LPAD(p.REPLACEMENT_REQ_ID, 4, '0') AS REPLACEMENT_REQ_NO
                FROM REPLACEMENT_PULLBACK p
                LEFT JOIN CLIENT c ON c.ID = p.CLIENT_DOT_ID
                LEFT JOIN P_MODEL pm ON pm.ID = p.P_MODEL
                LEFT JOIN REPLACEMENT_PRINTER_DETAILS rpd ON rpd.ID = p.REPLACEMENT_PRINTER_DETAILS_ID
                LEFT JOIN PRINTER_ORDER_ITEM poi ON poi.ID = rpd.PRINTER_ORDER_ITEM_ID
                WHERE p.REPLACEMENT_REQ_ID = ?
                """);

        if (serialNo != null && !serialNo.isEmpty()) {
            sql.append(" AND p.P_SERIAL_NO = ?");
        }
        sql.append(" ORDER BY p.ID DESC");

        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            conn = getConnection();
            ps = conn.prepareStatement(sql.toString());
            ps.setInt(1, reqId);
            if (serialNo != null && !serialNo.isEmpty()) {
                ps.setString(2, serialNo);
            }
            rs = ps.executeQuery();
            while (rs.next()) {
                list.add(mapResultSet(rs));
            }
        } finally {
            closeResources(conn, ps, rs);
        }
        return list;
    }

    private void setPullbackParams(final PreparedStatement ps, final PrinterPullback p, final boolean isUpdate) throws SQLException {
        int idx = 1;
        if (!isUpdate) {
            ps.setInt(idx++, p.getReplacementReqId());
        }
        setOptionalInt(ps, idx++, p.getCallId());
        setOptionalInt(ps, idx++, p.getClientDotId());
        ps.setString(idx++, p.getLocation());
        setOptionalInt(ps, idx++, p.getPrinterModel());
        if (!isUpdate) {
            ps.setString(idx++, p.getSerialNo());
        }
        ps.setString(idx++, p.getPickedBy() != null ? p.getPickedBy() : "ENGINEER");
        setOptionalInt(ps, idx++, p.getStatus());
        setOptionalInt(ps, idx++, p.getCourierId());
        ps.setString(idx++, p.getCourierName());
        ps.setString(idx++, p.getConsignmentNo());
        setOptionalDate(ps, idx++, p.getDispatchDate());
        setOptionalDate(ps, idx++, p.getArrivalDate());
        ps.setString(idx++, p.getReceipt());
        ps.setString(idx++, p.getDestinationBranch());
        ps.setString(idx++, p.getTransportMode() != null ? p.getTransportMode() : "BUS");
        ps.setString(idx++, p.getContactPerson());
        ps.setString(idx++, p.getContactNumber());
        ps.setString(idx++, p.getComments());
        ps.setInt(idx++, p.getPrinter());
        ps.setInt(idx++, p.getPowerCable());
        ps.setInt(idx++, p.getLanCable());
        ps.setInt(idx++, p.getTray());
        setOptionalInt(ps, idx++, p.getEmptyCartridge());
        setOptionalInt(ps, idx++, p.getUnusedCartridge());
        ps.setString(idx++, p.getPullbackMode());
        setOptionalInt(ps, idx++, p.getReplacementPrinterDetailsId());
        if (isUpdate) {
            ps.setInt(idx, p.getId());
        }
    }

    private void setOptionalInt(final PreparedStatement ps, final int index, final Integer value) throws SQLException {
        if (value != null) {
            ps.setInt(index, value);
        } else {
            ps.setNull(index, Types.INTEGER);
        }
    }

    private void setOptionalDate(final PreparedStatement ps, final int index, final java.util.Date date) throws SQLException {
        if (date != null) {
            ps.setDate(index, new java.sql.Date(date.getTime()));
        } else {
            ps.setNull(index, Types.DATE);
        }
    }

    public List<PrinterPullback> getPendingPullbacks() throws SQLException {
        final List<PrinterPullback> list = new ArrayList<>();
        final String sql = """
                SELECT p.ID, p.REPLACEMENT_REQ_ID, p.CALL_ID, p.CLIENT_DOT_ID, p.LOCATION,
                       p.P_MODEL, p.P_SERIAL_NO, p.PICKED_BY, p.STATUS, p.COURIER_ID,
                       p.COURIER_NAME, p.CONSIGNMENT_NO, p.DISPATCH_DATE, p.ARRIVAL_DATE,
                       p.RECEIPT, p.DESTINATION_BRANCH, p.TRANSPORT_MODE, p.CONTACT_PERSON,
                       p.CONTACT_NUMBER, p.COMMENTS, p.PRINTER, p.POWER_CABLE, p.LAN_CABLE,
                       p.TRAY, p.EMPTY_CARTRIDGE,
                       poi.CART_PICKUP_QUANITY AS UNUSED_CARTRIDGE,
                       p.PULLBACK_MODE,
                       p.PRINTER_QC, p.POWER_CABLE_QC, p.LAN_CABLE_QC, p.TRAY_QC, p.EMPTY_CARTRIDGE_QC, p.UNUSED_CARTRIDGE_QC,
                       p.REPLACEMENT_PRINTER_DETAILS_ID,
                       c.NAME AS CLIENT_NAME,
                       pm.MODEL_NAME AS PRINTER_MODEL_NAME,
                       'REQ-' || LPAD(p.REPLACEMENT_REQ_ID, 4, '0') AS REPLACEMENT_REQ_NO
                FROM REPLACEMENT_PULLBACK p
                LEFT JOIN CLIENT c ON c.ID = p.CLIENT_DOT_ID
                LEFT JOIN P_MODEL pm ON pm.ID = p.P_MODEL
                LEFT JOIN REPLACEMENT_PRINTER_DETAILS rpd ON rpd.ID = p.REPLACEMENT_PRINTER_DETAILS_ID
                LEFT JOIN PRINTER_ORDER_ITEM poi ON poi.ID = rpd.PRINTER_ORDER_ITEM_ID
                ORDER BY p.ID DESC
                """;

        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            conn = getConnection();
            ps = conn.prepareStatement(sql);
            rs = ps.executeQuery();

            while (rs.next()) {
                list.add(mapResultSet(rs));
            }
        } finally {
            closeResources(conn, ps, rs);
        }
        return list;
    }

    public PrinterPullback getPullbackById(final int id) throws SQLException {
        final String sql = """
                SELECT p.ID, p.REPLACEMENT_REQ_ID, p.CALL_ID, p.CLIENT_DOT_ID, p.LOCATION,
                       p.P_MODEL, p.P_SERIAL_NO, p.PICKED_BY, p.STATUS, p.COURIER_ID,
                       p.COURIER_NAME, p.CONSIGNMENT_NO, p.DISPATCH_DATE, p.ARRIVAL_DATE,
                       p.RECEIPT, p.DESTINATION_BRANCH, p.TRANSPORT_MODE, p.CONTACT_PERSON,
                       p.CONTACT_NUMBER, p.COMMENTS, p.PRINTER, p.POWER_CABLE, p.LAN_CABLE,
                       p.TRAY, p.EMPTY_CARTRIDGE,
                       poi.CART_PICKUP_QUANITY AS UNUSED_CARTRIDGE,
                       p.PULLBACK_MODE,
                       p.PRINTER_QC, p.POWER_CABLE_QC, p.LAN_CABLE_QC, p.TRAY_QC, p.EMPTY_CARTRIDGE_QC, p.UNUSED_CARTRIDGE_QC,
                       p.REPLACEMENT_PRINTER_DETAILS_ID,
                       c.NAME AS CLIENT_NAME,
                       pm.MODEL_NAME AS PRINTER_MODEL_NAME,
                       'REQ-' || LPAD(p.REPLACEMENT_REQ_ID, 4, '0') AS REPLACEMENT_REQ_NO
                FROM REPLACEMENT_PULLBACK p
                LEFT JOIN CLIENT c ON c.ID = p.CLIENT_DOT_ID
                LEFT JOIN P_MODEL pm ON pm.ID = p.P_MODEL
                LEFT JOIN REPLACEMENT_PRINTER_DETAILS rpd ON rpd.ID = p.REPLACEMENT_PRINTER_DETAILS_ID
                LEFT JOIN PRINTER_ORDER_ITEM poi ON poi.ID = rpd.PRINTER_ORDER_ITEM_ID
                WHERE p.ID = ?
                """;

        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            conn = getConnection();
            ps = conn.prepareStatement(sql);
            ps.setInt(1, id);
            rs = ps.executeQuery();

            if (rs.next()) {
                return mapResultSet(rs);
            }
        } finally {
            closeResources(conn, ps, rs);
        }
        return null;
    }

    /**
     * Fetch raw pullback row for update operations.
     * Uses REPLACEMENT_PULLBACK.UNUSED_CARTRIDGE directly (no POI override).
     */
    public PrinterPullback getPullbackByIdForUpdate(final int id) throws SQLException {
        final String sql = """
                SELECT p.ID, p.REPLACEMENT_REQ_ID, p.CALL_ID, p.CLIENT_DOT_ID, p.LOCATION,
                       p.P_MODEL, p.P_SERIAL_NO, p.PICKED_BY, p.STATUS, p.COURIER_ID,
                       p.COURIER_NAME, p.CONSIGNMENT_NO, p.DISPATCH_DATE, p.ARRIVAL_DATE,
                       p.RECEIPT, p.DESTINATION_BRANCH, p.TRANSPORT_MODE, p.CONTACT_PERSON,
                       p.CONTACT_NUMBER, p.COMMENTS, p.PRINTER, p.POWER_CABLE, p.LAN_CABLE,
                       p.TRAY, p.EMPTY_CARTRIDGE, p.UNUSED_CARTRIDGE, p.PULLBACK_MODE,
                       p.PRINTER_QC, p.POWER_CABLE_QC, p.LAN_CABLE_QC, p.TRAY_QC, p.EMPTY_CARTRIDGE_QC, p.UNUSED_CARTRIDGE_QC,
                       p.REPLACEMENT_PRINTER_DETAILS_ID,
                       c.NAME AS CLIENT_NAME,
                       pm.MODEL_NAME AS PRINTER_MODEL_NAME,
                       'REQ-' || LPAD(p.REPLACEMENT_REQ_ID, 4, '0') AS REPLACEMENT_REQ_NO
                FROM REPLACEMENT_PULLBACK p
                LEFT JOIN CLIENT c ON c.ID = p.CLIENT_DOT_ID
                LEFT JOIN P_MODEL pm ON pm.ID = p.P_MODEL
                WHERE p.ID = ?
                """;

        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            conn = getConnection();
            ps = conn.prepareStatement(sql);
            ps.setInt(1, id);
            rs = ps.executeQuery();

            if (rs.next()) {
                return mapResultSet(rs);
            }
        } finally {
            closeResources(conn, ps, rs);
        }
        return null;
    }

    public void markAsReceived(final int id, final String comments, final int printer, final int powerCable,
                               final int lanCable, final int tray, final Integer emptyCartridge,
                               final Integer unusedCartridge) throws SQLException {
        final String sql = """
                UPDATE REPLACEMENT_PULLBACK
                SET ARRIVAL_DATE = SYSDATE,
                    COMMENTS = ?,
                    PRINTER = ?,
                    POWER_CABLE = ?,
                    LAN_CABLE = ?,
                    TRAY = ?,
                    EMPTY_CARTRIDGE = ?,
                    UNUSED_CARTRIDGE = ?
                WHERE ID = ?
                """;

        Connection conn = null;
        PreparedStatement ps = null;

        try {
            conn = getConnection();
            ps = conn.prepareStatement(sql);
            ps.setString(1, comments);
            ps.setInt(2, printer);
            ps.setInt(3, powerCable);
            ps.setInt(4, lanCable);
            ps.setInt(5, tray);
            if (emptyCartridge != null) {
                ps.setInt(6, emptyCartridge);
            } else {
                ps.setNull(6, Types.INTEGER);
            }
            if (unusedCartridge != null) {
                ps.setInt(7, unusedCartridge);
            } else {
                ps.setNull(7, Types.INTEGER);
            }
            ps.setInt(8, id);
            ps.executeUpdate();
        } finally {
            closeResources(conn, ps, null);
        }
    }

    public int getPendingCount() throws SQLException {
        final String sql = "SELECT COUNT(*) FROM REPLACEMENT_PULLBACK WHERE ARRIVAL_DATE IS NULL";

        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            conn = getConnection();
            ps = conn.prepareStatement(sql);
            rs = ps.executeQuery();

            if (rs.next()) {
                return rs.getInt(1);
            }
        } finally {
            closeResources(conn, ps, rs);
        }
        return 0;
    }

    public List<PrinterPullback> getPullbacksWithFilters(final Integer reqId, final Integer status,
                                                         final java.sql.Date dateFrom, final java.sql.Date dateTo) throws SQLException {
        final List<PrinterPullback> list = new ArrayList<>();
        final StringBuilder sql = new StringBuilder("""
                SELECT p.ID, p.REPLACEMENT_REQ_ID, p.CALL_ID, p.CLIENT_DOT_ID, p.LOCATION,
                       p.P_MODEL, p.P_SERIAL_NO, p.PICKED_BY, tpm.DESCRIPTION as STATUS, p.COURIER_ID,
                       p.COURIER_NAME, p.CONSIGNMENT_NO, p.DISPATCH_DATE, p.ARRIVAL_DATE,
                       p.RECEIPT, p.DESTINATION_BRANCH, p.TRANSPORT_MODE, p.CONTACT_PERSON,
                       p.CONTACT_NUMBER, p.COMMENTS, p.PRINTER, p.POWER_CABLE, p.LAN_CABLE,
                       p.TRAY, p.EMPTY_CARTRIDGE,
                       poi.CART_PICKUP_QUANITY AS UNUSED_CARTRIDGE,
                       p.PULLBACK_MODE,
                       p.PRINTER_QC, p.POWER_CABLE_QC, p.LAN_CABLE_QC, p.TRAY_QC, p.EMPTY_CARTRIDGE_QC, p.UNUSED_CARTRIDGE_QC,
                       p.REPLACEMENT_PRINTER_DETAILS_ID,
                       c.NAME AS CLIENT_NAME,
                       pm.MODEL_NAME AS PRINTER_MODEL_NAME,
                       'REQ-' || LPAD(p.REPLACEMENT_REQ_ID, 4, '0') AS REPLACEMENT_REQ_NO
                FROM REPLACEMENT_PULLBACK p
                LEFT JOIN TAT_PRINTER_MASTER TPM on p.STATUS=tpm.ID
                LEFT JOIN CLIENT c ON c.ID = p.CLIENT_DOT_ID
                LEFT JOIN P_MODEL pm ON pm.ID = p.P_MODEL
                LEFT JOIN REPLACEMENT_PRINTER_DETAILS rpd ON rpd.ID = p.REPLACEMENT_PRINTER_DETAILS_ID
                LEFT JOIN PRINTER_ORDER_ITEM poi ON poi.ID = rpd.PRINTER_ORDER_ITEM_ID
                WHERE 1=1
                """);

        if (reqId != null) sql.append(" AND p.REPLACEMENT_REQ_ID = ?");
        if (status != null) sql.append(" AND p.STATUS = ?");
        if (dateFrom != null) sql.append(" AND p.DISPATCH_DATE >= ?");
        if (dateTo != null) sql.append(" AND p.DISPATCH_DATE <= ?");
        sql.append(" ORDER BY p.ID DESC");

        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            conn = getConnection();
            ps = conn.prepareStatement(sql.toString());
            int paramIndex = 1;
            if (reqId != null) ps.setInt(paramIndex++, reqId);
            if (status != null) ps.setInt(paramIndex++, status);
            if (dateFrom != null) ps.setDate(paramIndex++, dateFrom);
            if (dateTo != null) ps.setDate(paramIndex++, dateTo);

            rs = ps.executeQuery();
            while (rs.next()) {
                list.add(mapResultSet(rs));
            }
        } finally {
            closeResources(conn, ps, rs);
        }
        return list;
    }

    public boolean updateStatusOnly(final int id, final int status) throws SQLException {
        final String sql = "UPDATE REPLACEMENT_PULLBACK SET STATUS = ? WHERE ID = ?";

        Connection conn = null;
        PreparedStatement ps = null;

        try {
            conn = getConnection();
            ps = conn.prepareStatement(sql);
            ps.setInt(1, status);
            ps.setInt(2, id);
            return ps.executeUpdate() > 0;
        } finally {
            closeResources(conn, ps, null);
        }
    }

    public boolean updateQc(final int id, final int printerQc, final int powerCableQc, final int lanCableQc,
                             final int trayQc, final int emptyCartridgeQc, final int unusedCartridgeQc,
                             final int status, final String comments) throws SQLException {
        final String sql = """
                UPDATE REPLACEMENT_PULLBACK SET
                    PRINTER_QC = ?, POWER_CABLE_QC = ?, LAN_CABLE_QC = ?, TRAY_QC = ?,
                    EMPTY_CARTRIDGE_QC = ?, UNUSED_CARTRIDGE_QC = ?, STATUS = ?, COMMENTS = ?
                WHERE ID = ?
                """;
        Connection conn = null;
        PreparedStatement ps = null;
        try {
            conn = getConnection();
            ps = conn.prepareStatement(sql);
            ps.setInt(1, printerQc);
            ps.setInt(2, powerCableQc);
            ps.setInt(3, lanCableQc);
            ps.setInt(4, trayQc);
            ps.setInt(5, emptyCartridgeQc);
            ps.setInt(6, unusedCartridgeQc);
            ps.setInt(7, status);
            ps.setString(8, comments);
            ps.setInt(9, id);
            return ps.executeUpdate() > 0;
        } finally {
            closeResources(conn, ps, null);
        }
    }

    public boolean updateStatus(final int id, final int status, final String pickedBy, final String comments) throws SQLException {
        final String sql = "UPDATE REPLACEMENT_PULLBACK SET STATUS = ?, PICKED_BY = ?, COMMENTS = ? WHERE ID = ?";

        Connection conn = null;
        PreparedStatement ps = null;

        try {
            conn = getConnection();
            ps = conn.prepareStatement(sql);
            ps.setInt(1, status);
            ps.setString(2, pickedBy);
            ps.setString(3, comments);
            ps.setInt(4, id);
            return ps.executeUpdate() > 0;
        } finally {
            closeResources(conn, ps, null);
        }
    }

    public boolean updateVerification(final int id, final int printer, final int powerCable, final int lanCable, final int tray,
                                      final int emptyCartridge, final int unusedCartridge, final int status,
                                      final String comments) throws SQLException {
        final String sql = """
                UPDATE REPLACEMENT_PULLBACK SET 
                    PRINTER = ?, POWER_CABLE = ?, LAN_CABLE = ?, TRAY = ?,
                    EMPTY_CARTRIDGE = ?, UNUSED_CARTRIDGE = ?, STATUS = ?, COMMENTS = ?
                WHERE ID = ?
                """;
        Connection conn = null;
        PreparedStatement ps = null;
        try {
            conn = getConnection();
            ps = conn.prepareStatement(sql);
            ps.setInt(1, printer);
            ps.setInt(2, powerCable);
            ps.setInt(3, lanCable);
            ps.setInt(4, tray);
            ps.setInt(5, emptyCartridge);
            ps.setInt(6, unusedCartridge);
            ps.setInt(7, status);
            ps.setString(8, comments);
            ps.setInt(9, id);
            return ps.executeUpdate() > 0;
        } finally {
            closeResources(conn, ps, null);
        }
    }

    public boolean triggerCreditNote(final int id, final String creditInfo) throws SQLException {
        return true;
    }

    public int getStatusCode(final String status) {
        if (status == null) return 0;
        return switch (status.toUpperCase()) {
            case "RECEIVED" -> 1;
            case "VERIFIED" -> 2;
            case "DAMAGED" -> 3;
            default -> 0;
        };
    }
    // ==================== Printer Pullback After Booking ====================

    /**
     * Process printer pullback booking for all printers in a replacement request.
     * This method must be called ONLY AFTER a printer booking is successfully completed.
     *
     * <p>For each printer in REPLACEMENT_PRINTER_DETAILS:
     * <ul>
     *   <li>Scenario 1 (Courier pincode mapping exists): Insert directly into REPLACEMENT_PULLBACK
     *       with PICKED_BY=VENDOR and PULLBACK_MODE=COURIER.</li>
     *   <li>Scenario 2 (No mapping): Insert into CLIENT_REQUEST first, then into REPLACEMENT_PULLBACK
     *       with PICKED_BY=ENGINEER.</li>
     * </ul>
     *
     * <p>Entire operation is wrapped in a single transaction — all inserts succeed or all rollback.
     *
     * @param replacementReqId the ID of the replacement request
     * @throws Exception if any database or business logic error occurs
     */
    public void processPrinterPullbackAfterBooking(final int replacementReqId) throws Exception {
        System.out.println("DEBUG [PrinterPullbackDAO]: processPrinterPullbackAfterBooking ENTERED for reqId=" + replacementReqId);
        final ReplacementRequestDAO replacementRequestDAO = new ReplacementRequestDAO();
        final ClientDAO clientDAO = new ClientDAO();
        final CourierPincodeMappingDAO courierPincodeMappingDAO = new CourierPincodeMappingDAO();
        final ClientRequestDAO clientRequestDAO = new ClientRequestDAO();

        // 1. Fetch the replacement request with all printer details
        final ReplacementRequest request = replacementRequestDAO.getRequestById(replacementReqId);
        if (request == null) {
            System.out.println("DEBUG [PrinterPullbackDAO]: Replacement request NOT FOUND for reqId=" + replacementReqId);
            throw new Exception("Replacement request not found: " + replacementReqId);
        }

        final List<ReplacementPrinter> printers = request.getPrinters();
        if (printers == null || printers.isEmpty()) {
            System.out.println("DEBUG [PrinterPullbackDAO]: No printers found for reqId=" + replacementReqId + ". Skipping pullback.");
            logger.info("No printers found for replacement request: {}. Skipping pullback.", replacementReqId);
            return;
        }
        System.out.println("DEBUG [PrinterPullbackDAO]: Found " + printers.size() + " printer(s) for reqId=" + replacementReqId);
        // 2. Process all printers in a single transaction
        Connection conn = null;
        try {
            conn = getConnection();
            conn.setAutoCommit(false);

            for (final ReplacementPrinter printer : printers) {
                processSinglePrinterPullback(conn, request, printer, clientDAO,
                        courierPincodeMappingDAO, clientRequestDAO);
            }

            conn.commit();
            logger.info("Successfully processed {} pullback(s) for replacement request: {}",
                    printers.size(), replacementReqId);

        } catch (final Exception e) {
            if (conn != null) {
                try {
                    conn.rollback();
                    logger.info("Rolled back pullback transaction for replacement request: {}", replacementReqId);
                } catch (final SQLException rollbackEx) {
                    logger.error("Rollback failed for replacement request: " + replacementReqId, rollbackEx);
                }
            }
            throw e;
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (final SQLException closeEx) {
                    logger.error("Error closing connection", closeEx);
                }
            }
        }
    }

    /**
     * Process pullback for a single printer within the replacement request.
     * Determines the scenario (courier mapping exists or not) and performs the appropriate inserts.
     */
    private void processSinglePrinterPullback(final Connection conn,
                                              final ReplacementRequest request,
                                              final ReplacementPrinter printer,
                                              final ClientDAO clientDAO,
                                              final CourierPincodeMappingDAO courierPincodeMappingDAO,
                                              final ClientRequestDAO clientRequestDAO) throws Exception {

        // Get CLIENT_DOT_ID from REPLACEMENT_PRINTER_DETAILS
        final int clientDotId = printer.getClientBrId();
        System.out.println("DEBUG [PrinterPullbackDAO]: Processing printer ID=" + printer.getId()
                + ", clientDotId=" + clientDotId + ", serial=" + printer.getExistingSerial());

        // Fetch client data using REPLACEMENT_PRINTER_DETAILS.CLIENT_DOT_ID = CLIENT.ID
        final Client client = clientDAO.getClientById(clientDotId);
        if (client == null) {
            throw new Exception("Client not found for CLIENT_DOT_ID: " + clientDotId
                    + " (Replacement Printer Details ID: " + printer.getId() + ")");
        }

        // Get PINCODE from CLIENT
        final String pincode = client.getPincode();

        // Check if pincode exists in COURIER_PINCODE_MAPPING.ACTIVE_PINCODE
        CourierPincodeMapping mapping = null;
        if (pincode != null && !pincode.trim().isEmpty()) {
            try {
                mapping = courierPincodeMappingDAO.getActiveMappingByPincode(pincode.trim());
            } catch (final Exception e) {
                logger.warn("Error checking courier pincode mapping for pincode: {}. Treating as no mapping.", pincode, e);
            }
        }

        // Build the common pullback fields
        final PrinterPullback pullback = new PrinterPullback();
        pullback.setReplacementReqId(request.getId());
        pullback.setReplacementPrinterDetailsId(printer.getId());
        pullback.setClientDotId(clientDotId);
        pullback.setLocation(client.getBranch());                       // LOCATION from CLIENT
        pullback.setPrinterModel(printer.getExistingPModelId());        // P_MODEL (ID) from REPLACEMENT_PRINTER_DETAILS
        pullback.setSerialNo(printer.getExistingSerial());              // P_SERIAL_NO
        pullback.setStatus(1);                                          // STATUS = 1

        if (mapping != null) {
            // ── Scenario 1: Courier Pincode Mapping Exists ──
            System.out.println("DEBUG [PrinterPullbackDAO]: Scenario 1 (COURIER) for printer ID=" + printer.getId()
                    + ", pincode=" + pincode + ", courierName=" + mapping.getCourierName());
            pullback.setCallId(null);                                   // CALL_ID = NULL
            pullback.setCourierId(mapping.getId());                     // COURIER_ID = COURIER_PINCODE_MAPPING.ID
            pullback.setPickedBy("VENDOR");                             // PICKED_BY = VENDOR
            pullback.setCourierName(mapping.getCourierName());          // COURIER_NAME from mapping
            pullback.setPullbackMode("COURIER");                        // PULLBACK_MODE = COURIER

            // Get CONTACT_PERSON from COURIER_MASTER using COURIER_PINCODE_MAPPING.COURIER_ID
            final String contactPerson = courierPincodeMappingDAO.getCourierContactPerson(mapping.getCourierId());
            pullback.setContactPerson(contactPerson);

            final int pullbackId = insertPullback(conn, pullback);
            logger.info("Scenario 1 (COURIER): Inserted pullback ID={} for printer detail ID={}", pullbackId, printer.getId());

        } else {
            // ── Scenario 2: Courier Pincode Mapping Does NOT Exist ──
            System.out.println("DEBUG [PrinterPullbackDAO]: Scenario 2 (ENGINEER) for printer ID=" + printer.getId()
                    + ", pincode=" + pincode + " — no courier mapping found");

            // Step 1: Insert into CLIENT_REQUEST
            final int newCallId = clientRequestDAO.getNextCallId(conn);

            final Date now = new Date();
            final ClientRequest clientRequest = new ClientRequest();
            clientRequest.setCallId(String.valueOf(newCallId));
            clientRequest.setCallDate(new SimpleDateFormat("dd-MM-yyyy").format(now));
            clientRequest.setCallTime(new SimpleDateFormat("hh:mm a").format(now));
            clientRequest.setClientId(String.valueOf(client.getId()));
            clientRequest.setCallDetails("PRINTER PULL BACK");
            clientRequest.setCallStatusName("FIELD VISIT REQUIRE");
            clientRequest.setContactNo(client.getMobileNo());
            clientRequest.setPSerial(printer.getExistingSerial());
            clientRequest.setPModel(printer.getExistingPModelId());
            clientRequest.setProblemId(100);
            clientRequest.setCallDone("NO");
            clientRequest.setCallStatus(13);
            clientRequest.setCallType(5);
            clientRequest.setCallTypeName("PULL BACK");
            clientRequest.setOnlineSupport(1);
            clientRequest.setVenId(0);
            clientRequest.setBrId(client.getBrId());
            clientRequest.setRemarks("Auto pull back call against replacement order# "+request.getId());

            // COORDINATOR = REPLACEMENT_REQUEST.REQUESTER_USER_ID
            if (request.getRequesterUserId() != null && !request.getRequesterUserId().isEmpty()) {
                clientRequest.setCoordinator(Integer.parseInt(request.getRequesterUserId()));
            }
            clientRequestDAO.insert(conn, clientRequest);
            logger.info("Scenario 2: Inserted CLIENT_REQUEST with CALL_ID={} for printer detail ID={}",
                    newCallId, printer.getId());

            // Step 2: Insert into REPLACEMENT_PULLBACK
            pullback.setCallId(newCallId);                              // Newly generated CALL_ID
            pullback.setCourierId(null);                                // No courier mapping
            pullback.setPickedBy("ENGINEER");                           // PICKED_BY = ENGINEER
            pullback.setCourierName(null);
            pullback.setContactPerson(null);
            pullback.setPullbackMode(null);

            final int pullbackId = insertPullback(conn, pullback);
            logger.info("Scenario 2 (ENGINEER): Inserted pullback ID={} for printer detail ID={}", pullbackId, printer.getId());
        }
    }

    private PrinterPullback mapResultSet(final ResultSet rs) throws SQLException {
        final PrinterPullback p = new PrinterPullback();
        p.setId(rs.getInt("ID"));
        p.setReplacementReqId(rs.getInt("REPLACEMENT_REQ_ID"));
        p.setCallId(rs.getObject("CALL_ID") != null ? rs.getInt("CALL_ID") : null);
        p.setClientDotId(rs.getObject("CLIENT_DOT_ID") != null ? rs.getInt("CLIENT_DOT_ID") : null);
        p.setLocation(rs.getString("LOCATION"));
        p.setPrinterModel(rs.getObject("P_MODEL") != null ? rs.getInt("P_MODEL") : null);
        p.setSerialNo(rs.getString("P_SERIAL_NO"));
        p.setPickedBy(rs.getString("PICKED_BY"));
        p.setUiStatus(rs.getObject("STATUS") != null ? rs.getString("STATUS") : null);
        p.setCourierId(rs.getObject("COURIER_ID") != null ? rs.getInt("COURIER_ID") : null);
        p.setCourierName(rs.getString("COURIER_NAME"));
        p.setConsignmentNo(rs.getString("CONSIGNMENT_NO"));
        p.setDispatchDate(rs.getDate("DISPATCH_DATE"));
        p.setArrivalDate(rs.getDate("ARRIVAL_DATE"));
        p.setReceipt(rs.getString("RECEIPT"));
        p.setDestinationBranch(rs.getString("DESTINATION_BRANCH"));
        p.setTransportMode(rs.getString("TRANSPORT_MODE"));
        p.setContactPerson(rs.getString("CONTACT_PERSON"));
        p.setContactNumber(rs.getString("CONTACT_NUMBER"));
        p.setComments(rs.getString("COMMENTS"));
        p.setPrinter(rs.getInt("PRINTER"));
        p.setPowerCable(rs.getInt("POWER_CABLE"));
        p.setLanCable(rs.getInt("LAN_CABLE"));
        p.setTray(rs.getInt("TRAY"));
        p.setEmptyCartridge(rs.getObject("EMPTY_CARTRIDGE") != null ? rs.getInt("EMPTY_CARTRIDGE") : null);
        p.setUnusedCartridge(rs.getObject("UNUSED_CARTRIDGE") != null ? rs.getInt("UNUSED_CARTRIDGE") : null);
        p.setPullbackMode(rs.getString("PULLBACK_MODE"));
        p.setReplacementPrinterDetailsId(rs.getObject("REPLACEMENT_PRINTER_DETAILS_ID") != null ? rs.getInt("REPLACEMENT_PRINTER_DETAILS_ID") : null);
        p.setPrinterQc(rs.getInt("PRINTER_QC"));
        p.setPowerCableQc(rs.getInt("POWER_CABLE_QC"));
        p.setLanCableQc(rs.getInt("LAN_CABLE_QC"));
        p.setTrayQc(rs.getInt("TRAY_QC"));
        p.setEmptyCartridgeQc(rs.getInt("EMPTY_CARTRIDGE_QC"));
        p.setUnusedCartridgeQc(rs.getInt("UNUSED_CARTRIDGE_QC"));
        p.setClientName(rs.getString("CLIENT_NAME"));
        p.setPrinterModelName(rs.getString("PRINTER_MODEL_NAME"));
        p.setReplacementReqNo(rs.getString("REPLACEMENT_REQ_NO"));
        return p;
    }
}
