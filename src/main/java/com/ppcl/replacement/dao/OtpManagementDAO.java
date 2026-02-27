package com.ppcl.replacement.dao;

import com.ppcl.replacement.model.OtpManagement;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * DAO for OTP Management operations.
 * Handles OTP generation, validation, and state management with proper database locking.
 * All lookups use PULLBACK_ID as the single link to REPLACEMENT_PULLBACK.
 */
public class OtpManagementDAO extends BaseDAO {

    private static final int MAX_ATTEMPTS = 5;
    private static final long BLOCK_DURATION_HOURS = 1;
    private static final long OTP_VALIDITY_MINUTES = 5;

    /**
     * Find existing OTP record by pullbackId.
     * Uses SELECT FOR UPDATE to prevent race conditions.
     * 
     * @param pullbackId the REPLACEMENT_PULLBACK.ID
     * @return OtpManagement object or null if not found
     * @throws SQLException if database error occurs
     */
    public OtpManagement findByPullbackIdForUpdate(Long pullbackId) throws SQLException {
        final String sql = """
                SELECT ID, PULLBACK_ID, MOBILE_NUMBER, OTP_VALUE, OTP_EXPIRY_TIME,
                       ATTEMPT_COUNT, BLOCK_UNTIL_TIME, OTP_VALIDATED_AT, WA_SEND_STATUS, CREATED_AT, UPDATED_AT
                FROM OTP_MANAGEMENT
                WHERE PULLBACK_ID = ?
                FOR UPDATE
                """;

        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            conn = getConnection();
            ps = conn.prepareStatement(sql);
            ps.setLong(1, pullbackId);
            
            rs = ps.executeQuery();
            
            if (rs.next()) {
                return mapRow(rs);
            }
            
            return null;
        } finally {
            if (rs != null) {
                try { rs.close(); } catch (SQLException e) { logger.error("Error closing ResultSet", e); }
            }
            if (ps != null) {
                try { ps.close(); } catch (SQLException e) { logger.error("Error closing PreparedStatement", e); }
            }
            if (conn != null) {
                try { conn.close(); } catch (SQLException e) { logger.error("Error closing Connection", e); }
            }
        }
    }

    /**
     * Insert a new OTP record.
     * 
     * @param otp the OTP management object
     * @return the generated ID
     * @throws SQLException if database error occurs
     */
    public Long insert(OtpManagement otp) throws SQLException {
        final String sql = """
                INSERT INTO OTP_MANAGEMENT (
                    PULLBACK_ID, MOBILE_NUMBER, OTP_VALUE, OTP_EXPIRY_TIME,
                    ATTEMPT_COUNT, BLOCK_UNTIL_TIME, WA_SEND_STATUS, CREATED_AT, UPDATED_AT
                ) VALUES (?, ?, ?, ?, ?, ?, ?, SYSTIMESTAMP, SYSTIMESTAMP)
                """;

        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            conn = getConnection();
            ps = conn.prepareStatement(sql, new String[]{"ID"});
            
            ps.setLong(1, otp.getPullbackId());
            ps.setString(2, otp.getMobileNumber());
            ps.setString(3, otp.getOtpValue());
            ps.setTimestamp(4, otp.getOtpExpiryTime());
            ps.setInt(5, otp.getAttemptCount() != null ? otp.getAttemptCount() : 1);
            ps.setTimestamp(6, otp.getBlockUntilTime());
            ps.setString(7, otp.getWaSendStatus());
            
            ps.executeUpdate();
            
            rs = ps.getGeneratedKeys();
            if (rs.next()) {
                return rs.getLong(1);
            }
            
            return null;
        } finally {
            closeResources(conn, ps, rs);
        }
    }

    /**
     * Update existing OTP record (for regeneration or blocking).
     * 
     * @param otp the OTP management object
     * @return true if update successful
     * @throws SQLException if database error occurs
     */
    public boolean update(OtpManagement otp) throws SQLException {
        final String sql = """
                UPDATE OTP_MANAGEMENT SET
                    MOBILE_NUMBER = ?,
                    OTP_VALUE = ?,
                    OTP_EXPIRY_TIME = ?,
                    ATTEMPT_COUNT = ?,
                    BLOCK_UNTIL_TIME = ?,
                    OTP_VALIDATED_AT = ?,
                    WA_SEND_STATUS = ?,
                    UPDATED_AT = SYSTIMESTAMP
                WHERE ID = ?
                """;

        Connection conn = null;
        PreparedStatement ps = null;

        try {
            conn = getConnection();
            ps = conn.prepareStatement(sql);
            
            ps.setString(1, otp.getMobileNumber());
            ps.setString(2, otp.getOtpValue());
            ps.setTimestamp(3, otp.getOtpExpiryTime());
            ps.setInt(4, otp.getAttemptCount());
            ps.setTimestamp(5, otp.getBlockUntilTime());
            ps.setTimestamp(6, otp.getOtpValidatedAt());
            ps.setString(7, otp.getWaSendStatus());
            ps.setLong(8, otp.getId());
            
            return ps.executeUpdate() > 0;
        } finally {
            closeResources(conn, ps, null);
        }
    }

    /**
     * Clear OTP after successful validation (reset state).
     * 
     * @param id the OTP record ID
     * @throws SQLException if database error occurs
     */
    public void clearOtpAfterValidation(Long id) throws SQLException {
        final String sql = """
                UPDATE OTP_MANAGEMENT SET
                    OTP_VALUE = NULL,
                    ATTEMPT_COUNT = 0,
                    BLOCK_UNTIL_TIME = NULL,
                    OTP_VALIDATED_AT = SYSTIMESTAMP,
                    UPDATED_AT = SYSTIMESTAMP
                WHERE ID = ?
                """;

        Connection conn = null;
        PreparedStatement ps = null;

        try {
            conn = getConnection();
            ps = conn.prepareStatement(sql);
            ps.setLong(1, id);
            ps.executeUpdate();
        } finally {
            closeResources(conn, ps, null);
        }
    }

    /**
     * Validate replacement pullback exists and fetch pullback ID + mobile number via CLIENT_DOT_ID.
     * All 3 params (callId, replacementRequestId, printerSerialNo) must match in REPLACEMENT_PULLBACK (AND condition).
     * REPLACEMENT_PULLBACK.CLIENT_DOT_ID -> CLIENT.MOBILE_NO
     * 
     * @param callId the call ID
     * @param replacementRequestId the replacement request ID
     * @param printerSerialNo the printer serial number
     * @return PullbackInfo with pullbackId and mobileNumber, or null if pullback not found
     * @throws SQLException if database error occurs
     */
    public PullbackInfo validatePullbackAndGetInfo(String callId, Integer replacementRequestId, String printerSerialNo) throws SQLException {
        final String sql = """
                SELECT rp.ID AS PULLBACK_ID, rp.CLIENT_DOT_ID, c.MOBILE_NO
                FROM REPLACEMENT_PULLBACK rp
                JOIN CLIENT c ON c.ID = rp.CLIENT_DOT_ID
                WHERE rp.CALL_ID = ? AND rp.REPLACEMENT_REQ_ID = ? AND rp.P_SERIAL_NO = ?
                """;

        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            conn = getConnection();
            ps = conn.prepareStatement(sql);
            ps.setString(1, callId);
            ps.setInt(2, replacementRequestId);
            ps.setString(3, printerSerialNo);
            
            rs = ps.executeQuery();
            if (rs.next()) {
                return new PullbackInfo(rs.getLong("PULLBACK_ID"), rs.getString("MOBILE_NO"), rs.getString("CLIENT_DOT_ID"));
            }
            return null;
        } finally {
            closeResources(conn, ps, rs);
        }
    }

    /**
     * Holds pullback ID, mobile number and client ID from REPLACEMENT_PULLBACK + CLIENT join.
     */
    public static class PullbackInfo {
        private final Long pullbackId;
        private final String mobileNumber;
        private final String clientId;

        public PullbackInfo(Long pullbackId, String mobileNumber, String clientId) {
            this.pullbackId = pullbackId;
            this.mobileNumber = mobileNumber;
            this.clientId = clientId;
        }

        public Long getPullbackId() {
            return pullbackId;
        }

        public String getMobileNumber() {
            return mobileNumber;
        }

        public String getClientId() {
            return clientId;
        }
    }


    /**
     * Map ResultSet to OtpManagement object.
     */
    private OtpManagement mapRow(ResultSet rs) throws SQLException {
        OtpManagement otp = new OtpManagement();
        otp.setId(rs.getLong("ID"));
        otp.setPullbackId(rs.getLong("PULLBACK_ID"));
        otp.setMobileNumber(rs.getString("MOBILE_NUMBER"));
        otp.setOtpValue(rs.getString("OTP_VALUE"));
        otp.setOtpExpiryTime(rs.getTimestamp("OTP_EXPIRY_TIME"));
        otp.setAttemptCount(rs.getInt("ATTEMPT_COUNT"));
        otp.setBlockUntilTime(rs.getTimestamp("BLOCK_UNTIL_TIME"));
        otp.setOtpValidatedAt(rs.getTimestamp("OTP_VALIDATED_AT"));
        otp.setWaSendStatus(rs.getString("WA_SEND_STATUS"));
        otp.setCreatedAt(rs.getTimestamp("CREATED_AT"));
        otp.setUpdatedAt(rs.getTimestamp("UPDATED_AT"));
        return otp;
    }

    /**
     * Get constants for OTP management.
     */
    public static int getMaxAttempts() {
        return MAX_ATTEMPTS;
    }

    public static long getBlockDurationHours() {
        return BLOCK_DURATION_HOURS;
    }

    public static long getOtpValidityMinutes() {
        return OTP_VALIDITY_MINUTES;
    }
}
