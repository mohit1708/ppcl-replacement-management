package com.ppcl.replacement.dao;

import com.ppcl.replacement.model.ClientRequest;
import com.ppcl.replacement.util.DBConnectionPool;

import java.sql.*;
import java.util.*;

public class ClientRequestDAO {

    public ClientRequest getByCallId(final String callId) throws Exception {
        final String sql = "SELECT * FROM CLIENT_REQUEST WHERE CALL_ID = ?";

        try (final Connection conn = DBConnectionPool.getConnection();
             final PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, callId);

            try (final ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
        }
        return null;
    }

    public List<ClientRequest> getByClientId(final String clientId) throws Exception {
        final List<ClientRequest> results = new ArrayList<>();
        final String sql = "SELECT * FROM CLIENT_REQUEST WHERE CLIENT_ID = ? ORDER BY CALL_DATE DESC, CALL_TIME DESC";

        try (final Connection conn = DBConnectionPool.getConnection();
             final PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, clientId);

            try (final ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    results.add(mapRow(rs));
                }
            }
        }
        return results;
    }

    public List<ClientRequest> getByStatus(final int callStatus) throws Exception {
        final List<ClientRequest> results = new ArrayList<>();
        final String sql = "SELECT * FROM CLIENT_REQUEST WHERE CALL_STATUS = ? ORDER BY CALL_DATE DESC, CALL_TIME DESC";

        try (final Connection conn = DBConnectionPool.getConnection();
             final PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, callStatus);

            try (final ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    results.add(mapRow(rs));
                }
            }
        }
        return results;
    }

    public void insert(final ClientRequest req) throws Exception {
        try (final Connection conn = DBConnectionPool.getConnection()) {
            insert(conn, req);
        }
    }

    /**
     * Insert a CLIENT_REQUEST using a shared connection (for transaction management).
     *
     * @param conn the database connection (caller manages lifecycle)
     * @param req  the client request to insert
     * @throws Exception if a database error occurs
     */
    public void insert(final Connection conn, final ClientRequest req) throws Exception {
        final String sql = "INSERT INTO CLIENT_REQUEST (CALL_ID, CALL_DATE, CALL_TIME, CLIENT_ID, CALL_DETAILS, " +
                "EXECODE, CALL_DONE, REMARKS, CALL_BY, CONTACT_NO, P_SERIAL, P_MODEL, CALL_STATUS, " +
                "BR_ID, CALL_TYPE, ONLINE_SUPPORT, PROBLEM_ID, CALL_TYPE_NAME, CALL_STATUS_NAME, " +
                "COORDINATOR, VEN_ID) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (final PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, req.getCallId());
            ps.setString(2, req.getCallDate());
            ps.setString(3, req.getCallTime());
            ps.setString(4, req.getClientId());
            ps.setString(5, req.getCallDetails());
            ps.setObject(6, req.getExeCode());
            ps.setString(7, req.getCallDone());
            ps.setString(8, req.getRemarks());
            ps.setString(9, req.getCallBy());
            ps.setString(10, req.getContactNo());
            ps.setString(11, req.getPSerial());
            ps.setObject(12, req.getPModel());
            ps.setObject(13, req.getCallStatus());
            ps.setObject(14, req.getBrId());
            ps.setObject(15, req.getCallType());
            ps.setObject(16, req.getOnlineSupport());
            ps.setObject(17, req.getProblemId());
            ps.setString(18, req.getCallTypeName());
            ps.setString(19, req.getCallStatusName());
            ps.setObject(20, req.getCoordinator());
            ps.setObject(21, req.getVenId());

            ps.executeUpdate();
        }
    }

    /**
     * Get the next CALL_ID (MAX + 1) using a shared connection.
     *
     * @param conn the database connection
     * @return the next CALL_ID
     * @throws Exception if a database error occurs
     */
    public int getNextCallId(final Connection conn) throws Exception {
        final String sql = "SELECT NVL(MAX(CALL_ID), 0) + 1 FROM CLIENT_REQUEST";

        try (final PreparedStatement ps = conn.prepareStatement(sql);
             final ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                return rs.getInt(1);
            }
        }
        return 1;
    }

    public void update(final ClientRequest req) throws Exception {
        final String sql = "UPDATE CLIENT_REQUEST SET CALL_DATE = ?, CALL_TIME = ?, CLIENT_ID = ?, " +
                "CALL_DETAILS = ?, EXECODE = ?, CALL_DONE = ?, REMARKS = ?, CALL_BY = ?, " +
                "CONTACT_NO = ?, P_SERIAL = ?, P_MODEL = ?, CALL_STATUS = ?, BR_ID = ?, " +
                "CALL_TYPE = ?, ONLINE_SUPPORT = ?, PROBLEM_ID = ?, CALL_TYPE_NAME = ?, " +
                "CALL_STATUS_NAME = ?, COORDINATOR = ?, VEN_ID = ? WHERE CALL_ID = ?";

        try (final Connection conn = DBConnectionPool.getConnection();
             final PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, req.getCallDate());
            ps.setString(2, req.getCallTime());
            ps.setString(3, req.getClientId());
            ps.setString(4, req.getCallDetails());
            ps.setObject(5, req.getExeCode());
            ps.setString(6, req.getCallDone());
            ps.setString(7, req.getRemarks());
            ps.setString(8, req.getCallBy());
            ps.setString(9, req.getContactNo());
            ps.setString(10, req.getPSerial());
            ps.setObject(11, req.getPModel());
            ps.setObject(12, req.getCallStatus());
            ps.setObject(13, req.getBrId());
            ps.setObject(14, req.getCallType());
            ps.setObject(15, req.getOnlineSupport());
            ps.setObject(16, req.getProblemId());
            ps.setString(17, req.getCallTypeName());
            ps.setString(18, req.getCallStatusName());
            ps.setObject(19, req.getCoordinator());
            ps.setObject(20, req.getVenId());
            ps.setString(21, req.getCallId());

            ps.executeUpdate();
        }
    }

    public void updateStatus(final String callId, final int callStatus, final String callStatusName) throws Exception {
        final String sql = "UPDATE CLIENT_REQUEST SET CALL_STATUS = ?, CALL_STATUS_NAME = ? WHERE CALL_ID = ?";

        try (final Connection conn = DBConnectionPool.getConnection();
             final PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, callStatus);
            ps.setString(2, callStatusName);
            ps.setString(3, callId);
            ps.executeUpdate();
        }
    }

    public void updateRemarks(final String callId, final String remarks) throws Exception {
        final String sql = "UPDATE CLIENT_REQUEST SET REMARKS = ? WHERE CALL_ID = ?";

        try (final Connection conn = DBConnectionPool.getConnection();
             final PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, remarks);
            ps.setString(2, callId);
            ps.executeUpdate();
        }
    }

    private ClientRequest mapRow(final ResultSet rs) throws SQLException {
        final ClientRequest req = new ClientRequest();
        req.setCallId(rs.getString("CALL_ID"));
        req.setCallDate(rs.getString("CALL_DATE"));
        req.setCallTime(rs.getString("CALL_TIME"));
        req.setClientId(rs.getString("CLIENT_ID"));
        req.setCallDetails(rs.getString("CALL_DETAILS"));
        req.setExeCode((Integer) rs.getObject("EXECODE"));
        req.setCallDone(rs.getString("CALL_DONE"));
        req.setRemarks(rs.getString("REMARKS"));
        req.setCallBy(rs.getString("CALL_BY"));
        req.setContactNo(rs.getString("CONTACT_NO"));
        req.setPSerial(rs.getString("P_SERIAL"));
        req.setPModel((Integer) rs.getObject("P_MODEL"));
        req.setCallStatus((Integer) rs.getObject("CALL_STATUS"));
        req.setBrId((Integer) rs.getObject("BR_ID"));
        req.setCallType((Integer) rs.getObject("CALL_TYPE"));
        req.setOnlineSupport((Integer) rs.getObject("ONLINE_SUPPORT"));
        req.setProblemId((Integer) rs.getObject("PROBLEM_ID"));
        req.setCallTypeName(rs.getString("CALL_TYPE_NAME"));
        req.setCallStatusName(rs.getString("CALL_STATUS_NAME"));
        req.setCoordinator((Integer) rs.getObject("COORDINATOR"));
        req.setVenId((Integer) rs.getObject("VEN_ID"));
        return req;
    }
}
