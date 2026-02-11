package com.ppcl.replacement.dao;

import com.ppcl.replacement.model.RegisterRequestRow;
import com.ppcl.replacement.model.ReplacementPrinter;
import com.ppcl.replacement.util.DBConnectionPool;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO for Replacement Register - Central register with signed letter management
 */
public class RegisterDAO {

    /**
     * Get all replacement requests for the register with filters
     *
     * @param clientFilter - Filter by client (optional)
     * @param statusFilter - Filter by status: "pending" or "signed" (optional)
     * @param fromDate     - Filter by start date (optional)
     * @param toDate       - Filter by end date (optional)
     */
    public List<RegisterRequestRow> getRegisterRequests(final String clientFilter, final String statusFilter,
                                                        final String fromDate, final String toDate) throws Exception {
        final List<RegisterRequestRow> list = new ArrayList<>();

        final StringBuilder sql = new StringBuilder();
        sql.append("SELECT r.ID, r.STATUS, r.CREATION_DATE_TIME, r.IS_EDITABLE, ");
        sql.append("       r.SIGNED_LETTER_PATH, r.SIGNED_UPLOAD_DATE, ");
        sql.append("       c.NAME AS CLIENTNAME, c.CLIENT_ID AS CLIENTID, ");
        sql.append("       (SELECT COUNT(*) FROM REPLACEMENT_PRINTER_DETAILS pd WHERE pd.REPLACEMENT_REQUEST_ID = r.ID) AS PRINTERCOUNT, ");
        sql.append("       tm.STAGE_CODE AS CURRENTSTAGE, tm.DESCRIPTION AS CURRENTSTAGENAME ");
        sql.append("FROM REPLACEMENT_REQUEST r ");
        sql.append("LEFT JOIN CLIENT c ON c.ID = r.CLIENT_DOT_ID_SIGNING ");
        sql.append("LEFT JOIN TAT_MASTER tm ON tm.ID = r.CURRENT_STAGE ");
        sql.append("WHERE 1=1 ");

        final List<Object> params = new ArrayList<>();

        // Apply filters
        if (clientFilter != null && !clientFilter.trim().isEmpty()) {
            sql.append("AND c.CLIENT_ID = ? ");
            params.add(clientFilter.trim());
        }

        if (statusFilter != null && !statusFilter.trim().isEmpty()) {
            if ("signed".equalsIgnoreCase(statusFilter)) {
                sql.append("AND r.SIGNED_LETTER_PATH IS NOT NULL ");
            } else if ("pending".equalsIgnoreCase(statusFilter)) {
                sql.append("AND r.SIGNED_LETTER_PATH IS NULL ");
            }
        }

        if (fromDate != null && !fromDate.trim().isEmpty()) {
            sql.append("AND r.CREATION_DATE_TIME >= TO_DATE(?, 'YYYY-MM-DD') ");
            params.add(fromDate.trim());
        }

        if (toDate != null && !toDate.trim().isEmpty()) {
            sql.append("AND r.CREATION_DATE_TIME < TO_DATE(?, 'YYYY-MM-DD') + 1 ");
            params.add(toDate.trim());
        }

        sql.append("ORDER BY r.CREATION_DATE_TIME DESC");

        try (final Connection con = DBConnectionPool.getConnection();
             final PreparedStatement ps = con.prepareStatement(sql.toString())) {

            int idx = 1;
            for (final Object param : params) {
                ps.setObject(idx++, param);
            }

            try (final ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    final RegisterRequestRow row = new RegisterRequestRow();
                    row.setId(rs.getInt("ID"));
                    row.setLetterRef("R-" + rs.getInt("ID")); // Generate letter ref from ID
                    row.setClientId(rs.getString("CLIENTID"));
                    row.setClientName(rs.getString("CLIENTNAME"));
                    row.setPrinterCount(rs.getInt("PRINTERCOUNT"));
                    row.setGenerationDate(rs.getTimestamp("CREATION_DATE_TIME"));
                    row.setCurrentStage(rs.getString("CURRENTSTAGE"));
                    row.setCurrentStageName(rs.getString("CURRENTSTAGENAME"));
                    row.setStatus(rs.getString("STATUS"));
                    row.setSignedLetterPath(rs.getString("SIGNED_LETTER_PATH"));
                    row.setSignedUploadDate(rs.getTimestamp("SIGNED_UPLOAD_DATE"));
                    row.setLocked(row.getSignedLetterPath() != null && !row.getSignedLetterPath().isEmpty());

                    list.add(row);
                }
            }
        }

        return list;
    }

    /**
     * Get all replacement requests for the register (no filters)
     */
    public List<RegisterRequestRow> getAllRegisterRequests() throws Exception {
        return getRegisterRequests(null, null, null, null);
    }

    /**
     * Get request details with printers for view modal
     */
    public RegisterRequestRow getRequestWithPrinters(final int requestId) throws Exception {
        RegisterRequestRow row = null;

        final String sql = """
                SELECT r.ID, r.STATUS, r.CREATION_DATE_TIME, r.IS_EDITABLE,
                       r.SIGNED_LETTER_PATH, r.SIGNED_UPLOAD_DATE,
                       c.NAME AS CLIENTNAME, c.CLIENT_ID AS CLIENTID,
                       tm.STAGE_CODE AS CURRENTSTAGE, tm.DESCRIPTION AS CURRENTSTAGENAME
                FROM REPLACEMENT_REQUEST r
                LEFT JOIN CLIENT c ON c.ID = r.CLIENT_DOT_ID_SIGNING
                LEFT JOIN TAT_MASTER tm ON tm.ID = r.CURRENT_STAGE
                WHERE r.ID = ?
                """;

        try (final Connection con = DBConnectionPool.getConnection();
             final PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, requestId);

            try (final ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    row = new RegisterRequestRow();
                    row.setId(rs.getInt("ID"));
                    row.setLetterRef("R-" + rs.getInt("ID"));
                    row.setClientId(rs.getString("CLIENTID"));
                    row.setClientName(rs.getString("CLIENTNAME"));
                    row.setGenerationDate(rs.getTimestamp("CREATION_DATE_TIME"));
                    row.setCurrentStage(rs.getString("CURRENTSTAGE"));
                    row.setCurrentStageName(rs.getString("CURRENTSTAGENAME"));
                    row.setStatus(rs.getString("STATUS"));
                    row.setSignedLetterPath(rs.getString("SIGNED_LETTER_PATH"));
                    row.setSignedUploadDate(rs.getTimestamp("SIGNED_UPLOAD_DATE"));
                    row.setLocked(row.getSignedLetterPath() != null && !row.getSignedLetterPath().isEmpty());
                }
            }
        }

        if (row != null) {
            row.setPrinters(getPrintersByRequestId(requestId));
            row.setPrinterCount(row.getPrinters() != null ? row.getPrinters().size() : 0);
        }

        return row;
    }

    /**
     * Get printers for a specific replacement request with delivery info from PRINTER_ORDER_ITEM_ALLOT
     */
    private List<ReplacementPrinter> getPrintersByRequestId(final int requestId) throws Exception {
        final List<ReplacementPrinter> printers = new ArrayList<>();

        // First, get the ORDER_ID from the workflow event tracking comments
        final Integer orderId = getOrderIdForRequest(requestId);

        final String sql = """
                SELECT rpd.ID, rpd.CLIENT_DOT_ID, rpd.EXISTING_P_MODEL_ID, rpd.EXISTING_SERIAL,
                       rpd.NEW_P_MODEL_SELECTED_ID, rpd.NEW_P_MODEL_SELECTED_TEXT,
                       rpd.CONTACT_PERSON_NAME, rpd.CONTACT_PERSON_NUMBER, rpd.CONTACT_PERSON_EMAIL,
                       c.NAME AS LOCATION_NAME, c.CITY,
                       pm1.MODEL_NAME AS EXISTING_MODEL_NAME,
                       pm2.MODEL_NAME AS NEW_MODEL_NAME,
                       poia.P_SERIAL AS NEW_SERIAL,
                       poia.DELIVERY_STATUS,
                       poia.DELIVERED_DATE,
                       pm3.MODEL_NAME AS ALLOT_MODEL_NAME
                FROM REPLACEMENT_PRINTER_DETAILS rpd
                LEFT JOIN CLIENT c ON c.ID = rpd.CLIENT_DOT_ID
                LEFT JOIN P_MODEL pm1 ON pm1.ID = rpd.EXISTING_P_MODEL_ID
                LEFT JOIN P_MODEL pm2 ON pm2.ID = rpd.NEW_P_MODEL_SELECTED_ID
                LEFT JOIN PRINTER_ORDER_ITEM_ALLOT poia ON poia.ORDER_ID = ? AND poia.CLIENT_ID = rpd.CLIENT_DOT_ID
                LEFT JOIN P_MODEL pm3 ON pm3.ID = poia.P_MODEL
                WHERE rpd.REPLACEMENT_REQUEST_ID = ?
                """;

        try (final Connection con = DBConnectionPool.getConnection();
             final PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setObject(1, orderId);
            ps.setInt(2, requestId);

            try (final ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    final ReplacementPrinter p = new ReplacementPrinter();
                    p.setId(rs.getInt("ID"));
                    p.setClientBrId(rs.getInt("CLIENT_DOT_ID"));
                    p.setLocation(rs.getString("LOCATION_NAME"));
                    p.setCity(rs.getString("CITY"));
                    p.setExistingPModelId(rs.getInt("EXISTING_P_MODEL_ID"));
                    p.setExistingModelName(rs.getString("EXISTING_MODEL_NAME"));
                    p.setExistingSerial(rs.getString("EXISTING_SERIAL"));

                    final String newModelName = rs.getString("NEW_MODEL_NAME");
                    if (newModelName != null) {
                        p.setRecommendedModelName(newModelName);
                    } else {
                        p.setRecommendedModelName(rs.getString("NEW_P_MODEL_SELECTED_TEXT"));
                    }

                    // New serial from PRINTER_ORDER_ITEM_ALLOT
                    p.setNewSerial(rs.getString("NEW_SERIAL"));
                    p.setDeliveryStatus(rs.getString("DELIVERY_STATUS"));
                    p.setDeliveredDate(rs.getString("DELIVERED_DATE"));

                    // Use allocated model name if available
                    final String allotModelName = rs.getString("ALLOT_MODEL_NAME");
                    if (allotModelName != null) {
                        p.setNewModelName(allotModelName);
                    }

                    p.setContactPerson(rs.getString("CONTACT_PERSON_NAME"));
                    p.setContactNumber(rs.getString("CONTACT_PERSON_NUMBER"));
                    printers.add(p);
                }
            }
        }

        return printers;
    }

    /**
     * Get ORDER_ID from workflow event tracking comments for a replacement request
     * The comment format is: "Printer Order booked for client [X]. Order ID: [Y]"
     */
    private Integer getOrderIdForRequest(final int requestId) throws Exception {
        final String sql = """
                SELECT COMMENTS FROM RPLCE_FLOW_EVENT_TRACKING
                WHERE REPLACEMENT_REQUEST_ID = ?
                AND COMMENTS LIKE '%Order ID:%'
                ORDER BY START_AT DESC
                FETCH FIRST 1 ROWS ONLY
                """;

        try (final Connection con = DBConnectionPool.getConnection();
             final PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, requestId);

            try (final ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    final String comments = rs.getString("COMMENTS");
                    if (comments != null && comments.contains("Order ID:")) {
                        // Extract the order ID from the comment
                        String orderIdStr = comments.substring(comments.indexOf("Order ID:") + 9).trim();
                        // Remove any trailing text
                        final int spaceIdx = orderIdStr.indexOf(' ');
                        if (spaceIdx > 0) {
                            orderIdStr = orderIdStr.substring(0, spaceIdx);
                        }
                        try {
                            return Integer.parseInt(orderIdStr.trim());
                        } catch (final NumberFormatException e) {
                            return null;
                        }
                    }
                }
            }
        }
        return null;
    }

    /**
     * Upload signed letter and lock the request
     *
     * @param requestId - The replacement request ID
     * @param filePath  - Path to the uploaded signed letter PDF
     */
    public void uploadSignedLetterAndLock(final int requestId, final String filePath) throws Exception {
        try (final Connection con = DBConnectionPool.getConnection()) {
            uploadSignedLetterAndLock(con, requestId, filePath);
        }
    }

    /**
     * Upload signed letter and lock the request (using shared connection for atomicity)
     */
    public void uploadSignedLetterAndLock(final Connection con, final int requestId, final String filePath) throws Exception {
        final String sql = """
                UPDATE REPLACEMENT_REQUEST
                SET SIGNED_LETTER_PATH = ?,
                    SIGNED_UPLOAD_DATE = SYSTIMESTAMP,
                    IS_EDITABLE = 0,
                    STATUS = 'COMPLETED',
                    UPDATE_DATE_TIME = SYSTIMESTAMP
                WHERE ID = ?
                """;

        try (final PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, filePath);
            ps.setInt(2, requestId);
            ps.executeUpdate();
        }
    }

    /**
     * Check if a request is locked (has signed letter uploaded)
     */
    public boolean isRequestLocked(final int requestId) throws Exception {
        try (final Connection con = DBConnectionPool.getConnection()) {
            return isRequestLocked(con, requestId);
        }
    }

    /**
     * Check if a request is locked (using shared connection for atomicity)
     */
    public boolean isRequestLocked(final Connection con, final int requestId) throws Exception {
        final String sql = "SELECT SIGNED_LETTER_PATH FROM REPLACEMENT_REQUEST WHERE ID = ? FOR UPDATE";

        try (final PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, requestId);

            try (final ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    final String path = rs.getString("SIGNED_LETTER_PATH");
                    return path != null && !path.isEmpty();
                }
            }
        }
        return false;
    }

    /**
     * Atomically check if request is locked and upload signed letter
     * Returns true if upload was successful, false if already locked
     */
    public boolean checkAndUploadSignedLetter(final int requestId, final String filePath) throws Exception {
        try (final Connection con = DBConnectionPool.getConnection()) {
            con.setAutoCommit(false);
            try {
                if (isRequestLocked(con, requestId)) {
                    con.rollback();
                    return false;
                }
                uploadSignedLetterAndLock(con, requestId, filePath);
                con.commit();
                return true;
            } catch (final Exception e) {
                con.rollback();
                throw e;
            }
        }
    }

    /**
     * Get distinct clients for filter dropdown
     */
    public List<String[]> getDistinctClients() throws Exception {
        final List<String[]> clients = new ArrayList<>();

        final String sql = """
                SELECT DISTINCT c.CLIENT_ID, c.NAME
                FROM CLIENT c
                INNER JOIN REPLACEMENT_REQUEST r ON r.CLIENT_DOT_ID_SIGNING = c.ID
                ORDER BY c.NAME
                """;

        try (final Connection con = DBConnectionPool.getConnection();
             final PreparedStatement ps = con.prepareStatement(sql);
             final ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                clients.add(new String[]{rs.getString("CLIENT_ID"), rs.getString("NAME")});
            }
        }

        return clients;
    }
}
