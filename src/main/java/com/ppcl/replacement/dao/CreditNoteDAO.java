package com.ppcl.replacement.dao;

import com.ppcl.replacement.util.DBConnectionPool;

import java.sql.*;
import java.util.*;

public class CreditNoteDAO {

    public int insertCreditNote(final int replacementRequestId, final int printerDetailId, final String creditNoteNumber,
                                final String location, final String modelName, final String serialNo, final String issueDescription,
                                final Double agreementRate, final Double creditAmount, final String comments, final int createdBy) throws Exception {

        final String sql = "INSERT INTO CREDIT_NOTE (REPLACEMENT_REQUEST_ID, REPLACEMENT_PRINTER_DTL_ID, " +
                "CREDIT_NOTE_NUMBER, LOCATION, MODEL_NAME, SERIAL_NO, ISSUE_DESCRIPTION, " +
                "AGREEMENT_RATE, CREDIT_AMOUNT, COMMENTS, CREATED_BY) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (final Connection conn = DBConnectionPool.getConnection();
             final PreparedStatement ps = conn.prepareStatement(sql, new String[]{"ID"})) {

            ps.setInt(1, replacementRequestId);
            ps.setInt(2, printerDetailId);
            ps.setString(3, creditNoteNumber);
            ps.setString(4, location);
            ps.setString(5, modelName);
            ps.setString(6, serialNo);
            ps.setString(7, issueDescription);
            ps.setObject(8, agreementRate);
            ps.setObject(9, creditAmount);
            ps.setString(10, comments);
            ps.setInt(11, createdBy);

            ps.executeUpdate();

            try (final ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        }
        return -1;
    }

    public void insertCreditNotes(final int replacementRequestId, final List<Map<String, Object>> printerDetails,
                                  final String comments, final int createdBy) throws Exception {
        try (final Connection conn = DBConnectionPool.getConnection()) {
            insertCreditNotes(conn, replacementRequestId, printerDetails, comments, createdBy);
            conn.commit();
        }
    }

    public void insertCreditNotes(final Connection conn, final int replacementRequestId, final List<Map<String, Object>> printerDetails,
                                  final String comments, final int createdBy) throws Exception {

        System.out.println("insertCreditNotes ======");
        final String sql = "INSERT INTO CREDIT_NOTE (REPLACEMENT_REQUEST_ID, REPLACEMENT_PRINTER_DTL_ID, " +
                "LOCATION, MODEL_NAME, SERIAL_NO, ISSUE_DESCRIPTION, AGREEMENT_RATE, CREDIT_AMOUNT, " +
                "COMMENTS, CREATED_BY) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (final PreparedStatement ps = conn.prepareStatement(sql)) {
            for (final Map<String, Object> printer : printerDetails) {
                ps.setInt(1, replacementRequestId);
                ps.setInt(2, ((Number) printer.get("printerDetailId")).intValue());
                ps.setString(3, (String) printer.get("location"));
                ps.setString(4, (String) printer.get("modelName"));
                ps.setString(5, (String) printer.get("serialNo"));
                ps.setString(6, (String) printer.get("issueDescription"));
                ps.setObject(7, printer.get("agreementRate"));
                ps.setObject(8, printer.get("creditAmount"));
                ps.setString(9, comments);
                ps.setInt(10, createdBy);
                ps.addBatch();
            }
            ps.executeBatch();
        }
    }

    public List<Map<String, Object>> getCreditNotesByRequestId(final int replacementRequestId) throws Exception {
        final List<Map<String, Object>> creditNotes = new ArrayList<>();

        final String sql = "SELECT cn.*, rpd.EXISTING_SERIAL, rpd.EXISTING_P_MODEL_ID, " +
                "pm.MODEL_NAME AS EXISTING_MODEL_NAME, c.BRANCH AS LOCATION_NAME " +
                "FROM CREDIT_NOTE cn " +
                "LEFT JOIN REPLACEMENT_PRINTER_DETAILS rpd ON cn.REPLACEMENT_PRINTER_DTL_ID = rpd.ID " +
                "LEFT JOIN P_MODEL pm ON rpd.EXISTING_P_MODEL_ID = pm.ID " +
                "LEFT JOIN CLIENT c ON rpd.CLIENT_DOT_ID = c.ID " +
                "WHERE cn.REPLACEMENT_REQUEST_ID = ? " +
                "ORDER BY cn.ID";

        try (final Connection conn = DBConnectionPool.getConnection();
             final PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, replacementRequestId);

            try (final ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    final Map<String, Object> cn = new HashMap<>();
                    cn.put("id", rs.getInt("ID"));
                    cn.put("replacementRequestId", rs.getInt("REPLACEMENT_REQUEST_ID"));
                    cn.put("printerDetailId", rs.getInt("REPLACEMENT_PRINTER_DTL_ID"));
                    cn.put("creditNoteNumber", rs.getString("CREDIT_NOTE_NUMBER"));
                    cn.put("location", rs.getString("LOCATION"));
                    cn.put("modelName", rs.getString("MODEL_NAME"));
                    cn.put("serialNo", rs.getString("SERIAL_NO"));
                    cn.put("issueDescription", rs.getString("ISSUE_DESCRIPTION"));
                    cn.put("agreementRate", rs.getDouble("AGREEMENT_RATE"));
                    cn.put("creditAmount", rs.getDouble("CREDIT_AMOUNT"));
                    cn.put("comments", rs.getString("COMMENTS"));
                    cn.put("status", rs.getString("STATUS"));
                    cn.put("createdBy", rs.getInt("CREATED_BY"));
                    cn.put("creationDateTime", rs.getTimestamp("CREATION_DATE_TIME"));
                    creditNotes.add(cn);
                }
            }
        }
        return creditNotes;
    }

    public void updateCreditNoteStatus(final int creditNoteId, final String status) throws Exception {
        final String sql = "UPDATE CREDIT_NOTE SET STATUS = ?, UPDATE_DATE_TIME = SYSTIMESTAMP WHERE ID = ?";

        try (final Connection conn = DBConnectionPool.getConnection();
             final PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, status);
            ps.setInt(2, creditNoteId);
            ps.executeUpdate();
        }
    }

    public void updateCreditNoteNumber(final int creditNoteId, final String creditNoteNumber) throws Exception {
        final String sql = "UPDATE CREDIT_NOTE SET CREDIT_NOTE_NUMBER = ?, UPDATE_DATE_TIME = SYSTIMESTAMP WHERE ID = ?";

        try (final Connection conn = DBConnectionPool.getConnection();
             final PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, creditNoteNumber);
            ps.setInt(2, creditNoteId);
            ps.executeUpdate();
        }
    }

    public List<Map<String, Object>> getPendingCreditNotes() throws Exception {

        final List<Map<String, Object>> creditNotes = new ArrayList<>();

        final String sql = """
                WITH latest_completed_flow AS (
                    SELECT fet.replacement_request_id,
                           fet.comments,
                           ROW_NUMBER() OVER (
                               PARTITION BY fet.replacement_request_id
                               ORDER BY fet.end_at DESC
                               ) AS rn
                    FROM RPLCE_FLOW_EVENT_TRACKING fet
                    WHERE fet.end_at IS NOT NULL
                )
                SELECT
                    cn.replacement_request_id,
                    c.name AS client_name,
                    cn.location,
                    COUNT(*) AS printer_count,
                    SUM(cn.credit_amount) AS total_amount,
                    MIN(cn.creation_date_time) AS creation_date_time,
                    ua.user_id AS requester_name,
                    lcf.comments AS am_comments
                FROM credit_note cn
                         JOIN replacement_request rr
                              ON cn.replacement_request_id = rr.id
                         JOIN client c
                              ON rr.client_dot_id_signing = c.id
                         LEFT JOIN user_account ua
                                   ON rr.requester_user_id = ua.id
                         LEFT JOIN latest_completed_flow lcf
                                   ON lcf.replacement_request_id = cn.replacement_request_id
                                       AND lcf.rn = 1
                WHERE cn.status = 'PENDING'
                GROUP BY
                    cn.replacement_request_id,
                    c.name,
                    cn.location,
                    ua.user_id,
                    lcf.comments
                ORDER BY MIN(cn.creation_date_time) DESC""";

        try (final Connection conn = DBConnectionPool.getConnection();
             final PreparedStatement ps = conn.prepareStatement(sql);
             final ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                final Map<String, Object> cn = new HashMap<>();

                cn.put("replacementRequestId", rs.getInt("REPLACEMENT_REQUEST_ID"));
                cn.put("clientName", rs.getString("CLIENT_NAME"));
                cn.put("location", rs.getString("LOCATION"));
                cn.put("printerCount", rs.getInt("PRINTER_COUNT"));
                cn.put("totalAmount", rs.getBigDecimal("TOTAL_AMOUNT")); // safer than double
                cn.put("creationDateTime", rs.getTimestamp("CREATION_DATE_TIME"));
                cn.put("requesterName", rs.getString("REQUESTER_NAME"));
                cn.put("amComments", rs.getString("AM_COMMENTS"));

                creditNotes.add(cn);
            }
        }

        return creditNotes;
    }


//    public List<Map<String, Object>> getPendingCreditNotes() throws Exception {
//        List<Map<String, Object>> creditNotes = new ArrayList<>();
//
//        String sql = "SELECT cn.REPLACEMENT_REQUEST_ID, " +
//                "c.NAME AS CLIENT_NAME, cn.LOCATION, " +
//                "COUNT(*) AS PRINTER_COUNT, " +
//                "SUM(cn.CREDIT_AMOUNT) AS TOTAL_AMOUNT, " +
//                "MIN(cn.CREATION_DATE_TIME) AS CREATION_DATE_TIME, " +
//                "ua.NAME AS REQUESTER_NAME, " +
//                "(SELECT fet.COMMENTS FROM RPLCE_FLOW_EVENT_TRACKING fet " +
//                " WHERE fet.REPLACEMENT_REQUEST_ID = cn.REPLACEMENT_REQUEST_ID " +
//                " ORDER BY fet.CREATED_AT DESC FETCH FIRST 1 ROW ONLY) AS AM_COMMENTS " +
//                "FROM CREDIT_NOTE cn " +
//                "JOIN REPLACEMENT_REQUEST rr ON cn.REPLACEMENT_REQUEST_ID = rr.ID " +
//                "JOIN CLIENT c ON rr.CLIENT_DOT_ID_SIGNING = c.ID " +
//                "LEFT JOIN USER_ACCOUNT ua ON rr.REQUESTER_ID = ua.ID " +
//                "WHERE cn.STATUS = 'PENDING' " +
//                "GROUP BY cn.REPLACEMENT_REQUEST_ID, c.NAME, cn.LOCATION, ua.NAME, rr.CURRENT_STAGE, rr.ID " +
//                "ORDER BY MIN(cn.CREATION_DATE_TIME) DESC";
//
//        try (Connection conn = DBConnection.getConnection();
//             PreparedStatement ps = conn.prepareStatement(sql);
//             ResultSet rs = ps.executeQuery()) {
//
//            while (rs.next()) {
//                Map<String, Object> cn = new HashMap<>();
//                cn.put("replacementRequestId", rs.getInt("REPLACEMENT_REQUEST_ID"));
//                cn.put("clientName", rs.getString("CLIENT_NAME"));
//                cn.put("location", rs.getString("LOCATION"));
//                cn.put("printerCount", rs.getInt("PRINTER_COUNT"));
//                cn.put("totalAmount", rs.getDouble("TOTAL_AMOUNT"));
//                cn.put("creationDateTime", rs.getTimestamp("CREATION_DATE_TIME"));
//                cn.put("requesterName", rs.getString("REQUESTER_NAME"));
//                cn.put("amComments", rs.getString("AM_COMMENTS"));
//                creditNotes.add(cn);
//            }
//        }
//        return creditNotes;
//    }

    public List<Map<String, Object>> getApprovedCreditNotes() throws Exception {
        final List<Map<String, Object>> creditNotes = new ArrayList<>();

        final String sql = "SELECT cn.CREDIT_NOTE_NUMBER, cn.REPLACEMENT_REQUEST_ID, " +
                "c.NAME AS CLIENT_NAME, SUM(cn.CREDIT_AMOUNT) AS TOTAL_AMOUNT, " +
                "MAX(cn.UPDATE_DATE_TIME) AS ISSUED_DATE, cn.DOCUMENT_PATH " +
                "FROM CREDIT_NOTE cn " +
                "JOIN REPLACEMENT_REQUEST rr ON cn.REPLACEMENT_REQUEST_ID = rr.ID " +
                "JOIN CLIENT c ON rr.CLIENT_DOT_ID_SIGNING = c.ID " +
                "WHERE cn.STATUS = 'APPROVED' AND cn.CREDIT_NOTE_NUMBER IS NOT NULL " +
                "GROUP BY cn.CREDIT_NOTE_NUMBER, cn.REPLACEMENT_REQUEST_ID, c.NAME, cn.DOCUMENT_PATH " +
                "ORDER BY MAX(cn.UPDATE_DATE_TIME) DESC " +
                "FETCH FIRST 10 ROWS ONLY";

        try (final Connection conn = DBConnectionPool.getConnection();
             final PreparedStatement ps = conn.prepareStatement(sql);
             final ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                final Map<String, Object> cn = new HashMap<>();
                cn.put("creditNoteNumber", rs.getString("CREDIT_NOTE_NUMBER"));
                cn.put("replacementRequestId", rs.getInt("REPLACEMENT_REQUEST_ID"));
                cn.put("clientName", rs.getString("CLIENT_NAME"));
                cn.put("totalAmount", rs.getDouble("TOTAL_AMOUNT"));
                cn.put("issuedDate", rs.getTimestamp("ISSUED_DATE"));
                cn.put("documentPath", rs.getString("DOCUMENT_PATH"));
                creditNotes.add(cn);
            }
        }
        return creditNotes;
    }

    public void approveCreditNoteWithDocument(final Connection conn, final int replacementRequestId,
                                              final String creditNoteNumber, final String documentPath,
                                              final String comments) throws Exception {
        final String sql = "UPDATE CREDIT_NOTE SET STATUS = 'APPROVED', CREDIT_NOTE_NUMBER = ?, " +
                "DOCUMENT_PATH = ?, COMMENTS = COMMENTS || CHR(10) || ?, UPDATE_DATE_TIME = SYSTIMESTAMP " +
                "WHERE REPLACEMENT_REQUEST_ID = ? AND STATUS = 'PENDING'";

        try (final PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, creditNoteNumber);
            ps.setString(2, documentPath);
            ps.setString(3, comments != null ? comments : "");
            ps.setInt(4, replacementRequestId);
            ps.executeUpdate();
        }
    }

    public void approveCreditNote(final Connection conn, final int creditNoteId, final String creditNoteNumber,
                                  final Double creditAmount, final String comments) throws Exception {
        final String sql = "UPDATE CREDIT_NOTE SET STATUS = 'APPROVED', CREDIT_NOTE_NUMBER = ?, " +
                "CREDIT_AMOUNT = ?, COMMENTS = COMMENTS || CHR(10) || ?, UPDATE_DATE_TIME = SYSTIMESTAMP WHERE ID = ?";

        try (final PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, creditNoteNumber);
            ps.setObject(2, creditAmount);
            ps.setString(3, comments != null ? comments : "");
            ps.setInt(4, creditNoteId);
            ps.executeUpdate();
        }
    }

    public void approveCreditNotesByRequestId(final Connection conn, final int replacementRequestId,
                                              final String creditNoteNumber, final String comments) throws Exception {
        final String sql = "UPDATE CREDIT_NOTE SET STATUS = 'APPROVED', CREDIT_NOTE_NUMBER = ?, " +
                "COMMENTS = COMMENTS || CHR(10) || ?, UPDATE_DATE_TIME = SYSTIMESTAMP " +
                "WHERE REPLACEMENT_REQUEST_ID = ? AND STATUS = 'PENDING'";

        try (final PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, creditNoteNumber);
            ps.setString(2, comments != null ? comments : "");
            ps.setInt(3, replacementRequestId);
            ps.executeUpdate();
        }
    }
}
