package com.ppcl.replacement.dao;

import com.ppcl.replacement.model.Courier;
import com.ppcl.replacement.model.PrinterPullback;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO for courier login, password, OTP and assigned pullback operations.
 * All queries run against COURIER_MASTER (login columns added via ALTER TABLE).
 */
public class CourierLoginDAO extends BaseDAO {

    /* ---- SQL: courier list for admin page ---- */

    private static final String SQL_GET_ALL_COURIERS = """
            SELECT ID, NAME, CONTACT_PERSON, MOB, EMAIL, PASSWORD, FIRST_LOGIN_FLAG, ACCOUNT_STATUS
            FROM COURIER_MASTER
            ORDER BY NAME
            """;

    /* ---- SQL: courier lookup by ID / mobile ---- */

    private static final String SQL_GET_COURIER_BY_ID = """
            SELECT ID, NAME, CONTACT_PERSON, ADDRESS, MOB, EMAIL, CITY_ID, WEBSITE,
                   PASSWORD, FIRST_LOGIN_FLAG, OTP, OTP_GENERATED_TIME,
                   OTP_EXPIRY_TIME, OTP_ATTEMPT_COUNT, LAST_LOGIN_TIME, ACCOUNT_STATUS
            FROM COURIER_MASTER WHERE ID = ?
            """;

    private static final String SQL_GET_COURIER_BY_MOBILE = """
            SELECT ID, NAME, CONTACT_PERSON, ADDRESS, MOB, EMAIL, CITY_ID, WEBSITE,
                   PASSWORD, FIRST_LOGIN_FLAG, OTP, OTP_GENERATED_TIME,
                   OTP_EXPIRY_TIME, OTP_ATTEMPT_COUNT, LAST_LOGIN_TIME, ACCOUNT_STATUS
            FROM COURIER_MASTER WHERE MOB = ?
            """;

    /* ---- SQL: password / login management ---- */

    private static final String SQL_SET_PASSWORD = """
            UPDATE COURIER_MASTER
            SET PASSWORD = ?, FIRST_LOGIN_FLAG = 'Y', ACCOUNT_STATUS = 'A'
            WHERE ID = ?
            """;

    private static final String SQL_UPDATE_PASSWORD = """
            UPDATE COURIER_MASTER
            SET PASSWORD = ?, FIRST_LOGIN_FLAG = 'N'
            WHERE ID = ?
            """;

    private static final String SQL_UPDATE_LAST_LOGIN = """
            UPDATE COURIER_MASTER
            SET LAST_LOGIN_TIME = ?
            WHERE ID = ?
            """;

    /* ---- SQL: OTP (timestamps set from Java to avoid clock mismatch with remote DB) ---- */

    private static final String SQL_SAVE_OTP = """
            UPDATE COURIER_MASTER
            SET OTP = ?, OTP_GENERATED_TIME = ?, OTP_EXPIRY_TIME = ?,
                OTP_ATTEMPT_COUNT = 0
            WHERE ID = ?
            """;

    private static final String SQL_INCREMENT_OTP_ATTEMPTS = """
            UPDATE COURIER_MASTER
            SET OTP_ATTEMPT_COUNT = OTP_ATTEMPT_COUNT + 1
            WHERE ID = ?
            """;

    private static final String SQL_CLEAR_OTP = """
            UPDATE COURIER_MASTER
            SET OTP = NULL, OTP_GENERATED_TIME = NULL,
                OTP_EXPIRY_TIME = NULL, OTP_ATTEMPT_COUNT = 0
            WHERE ID = ?
            """;

    /* ---- SQL: assigned pullbacks (joins CLIENT for contact person, sorted by location) ---- */

    private static final String SQL_GET_ASSIGNED_PULLBACKS = """
            SELECT rp.ID, rp.LOCATION, rp.P_SERIAL_NO, rp.DISPATCH_DATE,
                   rp.EMPTY_CARTRIDGE, rp.UNUSED_CARTRIDGE,
                   rp.CLIENT_DOT_ID,
                   pm.MODEL_NAME AS PRINTER_MODEL_NAME,
                   cl.CONTACT_PERSON AS CLIENT_CONTACT_PERSON,
                   cl.NAME AS CLIENT_NAME,
                   cl.ADDRESS AS CLIENT_ADDRESS,
                   cl.MOBILE_NO AS CLIENT_CONTACT_NUMBER
            FROM REPLACEMENT_PULLBACK rp
            LEFT JOIN P_MODEL pm ON pm.ID = rp.P_MODEL
            LEFT JOIN CLIENT cl ON cl.ID = rp.CLIENT_DOT_ID
            WHERE rp.PICKED_BY = 'VENDOR'
              AND rp.PULLBACK_MODE = 'COURIER'
              AND rp.COURIER_ID = ?
            ORDER BY rp.LOCATION ASC
            """;

    /* ========== Admin: courier list with login status ========== */

    public List<Courier> getAllCouriersWithLoginStatus() throws SQLException {
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        final List<Courier> couriers = new ArrayList<>();

        try {
            conn = getConnection();
            ps = conn.prepareStatement(SQL_GET_ALL_COURIERS);
            rs = ps.executeQuery();

            while (rs.next()) {
                final Courier c = new Courier();
                c.setId(rs.getInt("ID"));
                c.setName(rs.getString("NAME"));
                c.setContactPerson(rs.getString("CONTACT_PERSON"));
                c.setMobile(rs.getLong("MOB"));
                c.setEmail(rs.getString("EMAIL"));
                c.setPassword(rs.getString("PASSWORD"));
                c.setFirstLoginFlag(rs.getString("FIRST_LOGIN_FLAG"));
                c.setAccountStatus(rs.getString("ACCOUNT_STATUS"));
                c.setStatus(c.hasLoginCreated() ? "LOGIN_CREATED" : "NO_LOGIN");
                couriers.add(c);
            }
        } finally {
            closeResources(conn, ps, rs);
        }
        return couriers;
    }

    /* ========== Courier lookup ========== */

    public Courier getCourierById(final int courierId) throws SQLException {
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            conn = getConnection();
            ps = conn.prepareStatement(SQL_GET_COURIER_BY_ID);
            ps.setInt(1, courierId);
            rs = ps.executeQuery();

            if (rs.next()) {
                return mapFullCourier(rs);
            }
        } finally {
            closeResources(conn, ps, rs);
        }
        return null;
    }

    public Courier getCourierByMobile(final long mobileNumber) throws SQLException {
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            conn = getConnection();
            ps = conn.prepareStatement(SQL_GET_COURIER_BY_MOBILE);
            ps.setLong(1, mobileNumber);
            rs = ps.executeQuery();

            if (rs.next()) {
                return mapFullCourier(rs);
            }
        } finally {
            closeResources(conn, ps, rs);
        }
        return null;
    }

    /* ========== Password & login management ========== */

    /** Sets initial password when admin generates a new login. */
    public boolean setPassword(final int courierId, final String encryptedPassword) throws SQLException {
        Connection conn = null;
        PreparedStatement ps = null;

        try {
            conn = getConnection();
            ps = conn.prepareStatement(SQL_SET_PASSWORD);
            ps.setString(1, encryptedPassword);
            ps.setInt(2, courierId);
            return ps.executeUpdate() > 0;
        } finally {
            closeResources(conn, ps, null);
        }
    }

    /** Updates password (first-login change or forgot-password reset). */
    public boolean updatePassword(final int courierId, final String encryptedPassword) throws SQLException {
        Connection conn = null;
        PreparedStatement ps = null;

        try {
            conn = getConnection();
            ps = conn.prepareStatement(SQL_UPDATE_PASSWORD);
            ps.setString(1, encryptedPassword);
            ps.setInt(2, courierId);
            return ps.executeUpdate() > 0;
        } finally {
            closeResources(conn, ps, null);
        }
    }

    public boolean updateLastLoginTime(final int courierId) throws SQLException {
        Connection conn = null;
        PreparedStatement ps = null;

        try {
            conn = getConnection();
            ps = conn.prepareStatement(SQL_UPDATE_LAST_LOGIN);
            ps.setTimestamp(1, new Timestamp(System.currentTimeMillis()));
            ps.setInt(2, courierId);
            return ps.executeUpdate() > 0;
        } finally {
            closeResources(conn, ps, null);
        }
    }

    /* ========== OTP management ========== */

    /** Saves encrypted OTP with 10-minute expiry (timestamps from Java clock). */
    public boolean saveOtp(final int courierId, final String encryptedOtp) throws SQLException {
        Connection conn = null;
        PreparedStatement ps = null;

        final Timestamp now = new Timestamp(System.currentTimeMillis());
        final Timestamp expiry = new Timestamp(System.currentTimeMillis() + (10 * 60 * 1000));

        System.out.println("[OTP SAVE] CourierId: " + courierId + " | Generated: " + now + " | Expiry: " + expiry);

        try {
            conn = getConnection();
            ps = conn.prepareStatement(SQL_SAVE_OTP);
            ps.setString(1, encryptedOtp);
            ps.setTimestamp(2, now);
            ps.setTimestamp(3, expiry);
            ps.setInt(4, courierId);
            return ps.executeUpdate() > 0;
        } finally {
            closeResources(conn, ps, null);
        }
    }

    public boolean incrementOtpAttempts(final int courierId) throws SQLException {
        Connection conn = null;
        PreparedStatement ps = null;

        try {
            conn = getConnection();
            ps = conn.prepareStatement(SQL_INCREMENT_OTP_ATTEMPTS);
            ps.setInt(1, courierId);
            return ps.executeUpdate() > 0;
        } finally {
            closeResources(conn, ps, null);
        }
    }

    public boolean clearOtp(final int courierId) throws SQLException {
        Connection conn = null;
        PreparedStatement ps = null;

        try {
            conn = getConnection();
            ps = conn.prepareStatement(SQL_CLEAR_OTP);
            ps.setInt(1, courierId);
            return ps.executeUpdate() > 0;
        } finally {
            closeResources(conn, ps, null);
        }
    }

    /* ========== Assigned pullbacks for courier portal ========== */

    public List<PrinterPullback> getAssignedPullbacks(final int courierId) throws SQLException {
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        final List<PrinterPullback> pullbacks = new ArrayList<>();

        try {
            conn = getConnection();
            ps = conn.prepareStatement(SQL_GET_ASSIGNED_PULLBACKS);
            ps.setInt(1, courierId);
            rs = ps.executeQuery();

            while (rs.next()) {
                pullbacks.add(mapPullback(rs));
            }
        } finally {
            closeResources(conn, ps, rs);
        }
        return pullbacks;
    }

    /* ========== ResultSet mapping helpers ========== */

    private Courier mapFullCourier(final ResultSet rs) throws SQLException {
        final Courier c = new Courier();
        c.setId(rs.getInt("ID"));
        c.setName(rs.getString("NAME"));
        c.setContactPerson(rs.getString("CONTACT_PERSON"));
        c.setAddress(rs.getString("ADDRESS"));
        c.setMobile(rs.getLong("MOB"));
        c.setEmail(rs.getString("EMAIL"));
        c.setCityId(rs.getInt("CITY_ID"));
        c.setWebsite(rs.getString("WEBSITE"));
        c.setPassword(rs.getString("PASSWORD"));
        c.setFirstLoginFlag(rs.getString("FIRST_LOGIN_FLAG"));
        c.setOtp(rs.getString("OTP"));
        c.setOtpGeneratedTime(rs.getTimestamp("OTP_GENERATED_TIME"));
        c.setOtpExpiryTime(rs.getTimestamp("OTP_EXPIRY_TIME"));
        c.setOtpAttemptCount(rs.getInt("OTP_ATTEMPT_COUNT"));
        c.setLastLoginTime(rs.getTimestamp("LAST_LOGIN_TIME"));
        c.setAccountStatus(rs.getString("ACCOUNT_STATUS"));
        return c;
    }

    private PrinterPullback mapPullback(final ResultSet rs) throws SQLException {
        final PrinterPullback pb = new PrinterPullback();
        pb.setId(rs.getInt("ID"));
        pb.setLocation(rs.getString("LOCATION"));
        pb.setSerialNo(rs.getString("P_SERIAL_NO"));
        pb.setDispatchDate(rs.getDate("DISPATCH_DATE"));
        pb.setPrinterModelName(rs.getString("PRINTER_MODEL_NAME"));
        pb.setContactPerson(rs.getString("CLIENT_CONTACT_PERSON"));
        pb.setClientName(rs.getString("CLIENT_NAME"));
        pb.setClientAddress(rs.getString("CLIENT_ADDRESS"));
        pb.setClientContactNumber(rs.getString("CLIENT_CONTACT_NUMBER"));

        final int emptyCart = rs.getInt("EMPTY_CARTRIDGE");
        pb.setEmptyCartridge(rs.wasNull() ? null : emptyCart);
        final int unusedCart = rs.getInt("UNUSED_CARTRIDGE");
        pb.setUnusedCartridge(rs.wasNull() ? null : unusedCart);

        pb.setClientDotId(rs.getInt("CLIENT_DOT_ID"));
        return pb;
    }
}
