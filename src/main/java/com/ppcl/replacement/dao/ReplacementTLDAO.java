package com.ppcl.replacement.dao;

import com.ppcl.replacement.model.*;
import com.ppcl.replacement.util.DBConnectionPool;

import java.sql.*;
import java.util.*;

import static com.ppcl.replacement.constants.MessageConstant.formatDefaultTLRecommendation;

/**
 * DAO class for Service TL operations
 * Uses correct table names: REPLACEMENT_REQUEST, REPLACEMENT_PRINTER_DETAILS, TAT_MASTER
 */
public class ReplacementTLDAO extends BaseDAO {

    private final TransitionWorkflowDao transitionWorkflowDao = new TransitionWorkflowDao();
    private final UserDAO userDao = new UserDAO();
    private final TatDao tatDao = new TatDao();

    private final String GET_ALL_TL_PENDING_LIST = """
                     SELECT r.ID, r.STATUS, r.CREATION_DATE_TIME, r.IS_EDITABLE,
                           c.NAME AS CLIENT_NAME, c.CLIENT_ID,
                            req_ua.USER_ID AS REQUESTER_NAME,
                                                               req_ua.USER_ID AS ACCOUNT_MANAGER,
                                                                tm.STAGE_CODE, tm.STAGE_CODE AS STAGE_NAME,
                                                                 (SELECT COUNT(*) FROM REPLACEMENT_PRINTER_DETAILS pd
                                                                  WHERE pd.REPLACEMENT_REQUEST_ID = r.ID) AS PRINTER_COUNT
                     FROM REPLACEMENT_REQUEST r
                      LEFT JOIN CLIENT c ON c.ID = r.CLIENT_DOT_ID_SIGNING
                     LEFT JOIN USER_ACCOUNT req_ua ON req_ua.ID = r.REQUESTER_USER_ID
            LEFT JOIN USER_ACCOUNT owner_ua ON owner_ua.ID = r.CURRENT_OWNER_ID
            LEFT JOIN TAT_MASTER tm ON tm.ID = r.CURRENT_STAGE
            WHERE r.STATUS IN ('OPEN', 'PENDING')
              AND tm.STAGE_CODE = 'STG2_SERVICE_TL_REVIEW'
            ORDER BY r.CREATION_DATE_TIME DESC""";

    /**
     * Get pending requests for a specific Service TL.
     *
     * @param tlUserId the user ID of the Service TL
     * @return a list of {@link RequestDetailRow} objects
     * @throws Exception if a database error occurs
     */
    public List<RequestDetailRow> getPendingRequestsForTL(final int tlUserId) throws Exception {
        final List<RequestDetailRow> requests = new ArrayList<>();

        final String sql =
                "SELECT r.ID, r.STATUS, r.CREATION_DATE_TIME, r.IS_EDITABLE, " +
                        "       c.NAME AS CLIENT_NAME, c.CLIENT_ID, " +
                        "       req_ua.USER_ID AS REQUESTER_NAME, " +
                        "       owner_ua.USER_ID AS ACCOUNT_MANAGER, " +
                        "       tm.STAGE_CODE, tm.DESCRIPTION AS STAGE_NAME, " +
                        "       (SELECT COUNT(*) FROM REPLACEMENT_PRINTER_DETAILS pd WHERE pd.REPLACEMENT_REQUEST_ID = r.ID) AS PRINTER_COUNT, " +
                        "       fet.START_AT AS STAGE_START " +
                        "FROM REPLACEMENT_REQUEST r " +
                        "LEFT JOIN CLIENT c ON c.ID = r.CLIENT_DOT_ID_SIGNING " +
                        "LEFT JOIN USER_ACCOUNT req_ua ON req_ua.ID = r.REQUESTER_USER_ID " +
                        "LEFT JOIN USER_ACCOUNT owner_ua ON owner_ua.ID = r.CURRENT_OWNER_ID " +
                        "LEFT JOIN TAT_MASTER tm ON tm.ID = r.CURRENT_STAGE " +
                        "LEFT JOIN (SELECT fet1.* FROM RPLCE_FLOW_EVENT_TRACKING fet1 " +
                        "           WHERE fet1.ID = (SELECT MAX(fet2.ID) FROM RPLCE_FLOW_EVENT_TRACKING fet2 " +
                        "                            WHERE fet2.REPLACEMENT_REQUEST_ID = fet1.REPLACEMENT_REQUEST_ID)) fet " +
                        "       ON fet.REPLACEMENT_REQUEST_ID = r.ID " +
                        "WHERE r.STATUS IN ('OPEN', 'PENDING') " +
                        "  AND r.CURRENT_OWNER_ID = ? " +
                        "  AND tm.STAGE_CODE = 'STG2_SERVICE_TL_REVIEW' " +
                        "ORDER BY r.CREATION_DATE_TIME DESC";

        try (final Connection con = getConnection();
             final PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, tlUserId);

            try (final ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    final RequestDetailRow row = new RequestDetailRow();
                    row.setId(rs.getInt("ID"));
                    row.setClientId(rs.getString("CLIENT_ID"));
                    row.setClientName(rs.getString("CLIENT_NAME"));
                    row.setRequesterName(rs.getString("REQUESTER_NAME"));
                    row.setAccountManager(rs.getString("ACCOUNT_MANAGER"));
                    row.setPrinterCount(rs.getInt("PRINTER_COUNT"));
                    row.setCreatedAt(rs.getTimestamp("CREATION_DATE_TIME"));
                    row.setCurrentStage(rs.getString("STAGE_CODE"));
                    row.setCurrentStageName(rs.getString("STAGE_NAME"));
                    row.setEditable(rs.getInt("IS_EDITABLE") == 1);
                    row.setStatus(rs.getString("STATUS"));

                    requests.add(row);
                }
            }
        }

        return requests;
    }

    /**
     * Get all pending requests for the Service TL stage (for administrative/managerial view).
     *
     * @return a list of {@link RequestDetailRow} objects
     * @throws Exception if a database error occurs
     */
    public List<RequestDetailRow> getAllPendingRequestsForTLStage() throws Exception {
        final List<RequestDetailRow> requests = new ArrayList<>();


        try (final Connection con = getConnection();
             final PreparedStatement ps = con.prepareStatement(GET_ALL_TL_PENDING_LIST);
             final ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                final RequestDetailRow row = new RequestDetailRow();
                row.setId(rs.getInt("ID"));
                row.setClientId(rs.getString("CLIENT_ID"));
                row.setClientName(rs.getString("CLIENT_NAME"));
                row.setRequesterName(rs.getString("REQUESTER_NAME"));
                row.setAccountManager(rs.getString("ACCOUNT_MANAGER"));
                row.setPrinterCount(rs.getInt("PRINTER_COUNT"));
                row.setCreatedAt(rs.getTimestamp("CREATION_DATE_TIME"));
                row.setCurrentStage(rs.getString("STAGE_CODE"));
                row.setCurrentStageName(rs.getString("STAGE_NAME"));
                row.setEditable(rs.getInt("IS_EDITABLE") == 1);
                row.setStatus(rs.getString("STATUS"));

                requests.add(row);
            }
        }

        return requests;
    }

    /**
     * Get printer details for a replacement request, including history and available models.
     *
     * @param reqId the ID of the replacement request
     * @return a map containing request, printers, models, and history
     * @throws Exception if a database error occurs
     */
    public Map<String, Object> getPrinterDetails(final int reqId) throws Exception {
        final Map<String, Object> result = new HashMap<>();

        // Get request info
        final String reqSql =
                "SELECT r.ID, r.STATUS, r.REPLACEMENT_TYPE, " +
                        "       c.NAME AS CLIENT_NAME, c.CLIENT_ID, " +
                        "       req_ua.USER_ID AS REQUESTER_NAME, " +
                        "       owner_ua.USER_ID AS ACCOUNT_MANAGER, " +
                        "       tm.STAGE_CODE, tm.DESCRIPTION AS STAGE_NAME, " +
                        "       rr.NAME AS REASON_NAME " +
                        "FROM REPLACEMENT_REQUEST r " +
                        "LEFT JOIN CLIENT c ON c.ID = r.CLIENT_DOT_ID_SIGNING " +
                        "LEFT JOIN USER_ACCOUNT req_ua ON req_ua.ID = r.REQUESTER_USER_ID " +
                        "LEFT JOIN USER_ACCOUNT owner_ua ON owner_ua.ID = r.CURRENT_OWNER_ID " +
                        "LEFT JOIN TAT_MASTER tm ON tm.ID = r.CURRENT_STAGE " +
                        "LEFT JOIN REPLACEMENT_REASON rr ON rr.ID = r.REPLACEMENT_REASON_ID " +
                        "WHERE r.ID = ?";

        final Map<String, Object> requestInfo = new HashMap<>();

        try (final Connection con = getConnection();
             final PreparedStatement ps = con.prepareStatement(reqSql)) {

            ps.setInt(1, reqId);

            try (final ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    requestInfo.put("id", rs.getInt("ID"));
                    requestInfo.put("status", rs.getString("STATUS"));
                    requestInfo.put("replacementType", rs.getString("REPLACEMENT_TYPE"));
                    requestInfo.put("requester", rs.getString("REQUESTER_NAME"));
                    requestInfo.put("accountManager", rs.getString("ACCOUNT_MANAGER"));
                    requestInfo.put("currentStage", rs.getString("STAGE_CODE"));
                    requestInfo.put("stageName", rs.getString("STAGE_NAME"));
                    requestInfo.put("clientName", rs.getString("CLIENT_NAME"));
                    requestInfo.put("clientId", rs.getString("CLIENT_ID"));
                    requestInfo.put("reasonName", rs.getString("REASON_NAME"));
                }
            }
        }

        result.put("request", requestInfo);

        // Get printers
        final String printerSql =
                "SELECT rpd.ID, rpd.AGR_PROD_ID, rpd.EXISTING_SERIAL, " +
                        "       rpd.NEW_P_MODEL_SELECTED_ID, rpd.NEW_P_MODEL_SELECTED_TEXT, " +
                        "       rpd.NEW_P_MODEL_SOURCE, rpd.RECOMMENDED_COMMENTS, " +
                        "       pm.MODEL_NAME AS EXISTING_MODEL, " +
                        "       new_pm.MODEL_NAME AS NEW_MODEL_NAME, " +
                        "       c.BRANCH AS LOCATION, c.CITY " +
                        "FROM REPLACEMENT_PRINTER_DETAILS rpd " +
                        "LEFT JOIN P_MODEL pm ON pm.ID = rpd.EXISTING_P_MODEL_ID " +
                        "LEFT JOIN P_MODEL new_pm ON new_pm.ID = rpd.NEW_P_MODEL_SELECTED_ID " +
                        "LEFT JOIN CLIENT c ON c.ID = rpd.CLIENT_DOT_ID " +
                        "WHERE rpd.REPLACEMENT_REQUEST_ID = ?";

        final List<Map<String, Object>> printers = new ArrayList<>();

        try (final Connection con = getConnection();
             final PreparedStatement ps = con.prepareStatement(printerSql)) {

            ps.setInt(1, reqId);

            try (final ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    final Map<String, Object> printer = new HashMap<>();
                    printer.put("id", rs.getInt("ID"));
                    printer.put("agrProdId", rs.getInt("AGR_PROD_ID"));
                    printer.put("serial", rs.getString("EXISTING_SERIAL"));
                    printer.put("existingModel", rs.getString("EXISTING_MODEL"));
                    printer.put("newModelId", rs.getInt("NEW_P_MODEL_SELECTED_ID"));
                    printer.put("newModelText", rs.getString("NEW_P_MODEL_SELECTED_TEXT"));
                    printer.put("newModelName", rs.getString("NEW_MODEL_NAME"));
                    printer.put("newModelSource", rs.getString("NEW_P_MODEL_SOURCE"));
                    printer.put("comments", rs.getString("RECOMMENDED_COMMENTS"));
                    printer.put("city", rs.getString("CITY"));
                    printer.put("location", rs.getString("LOCATION"));

                    printers.add(printer);
                }
            }
        }

        result.put("printers", printers);

        return result;
    }

    /**
     * Update Service TL's recommendation for a printer.
     *
     * @param printerId    the ID of the printer detail record
     * @param reqId        the ID of the replacement request
     * @param newModelId   the ID of the recommended printer model, or {@code null}
     * @param newModelText the text description of the model if not in master, or {@code null}
     * @param printerType  the type of printer (e.g., Mono/Color)
     * @param comments     recommendation comments
     * @param userId       the user ID performing the action
     * @throws Exception if a database error occurs
     */
    public void updateTLRecommendation(final int printerId, final int reqId, final Integer newModelId,
                                       final String newModelText, final String printerType, final String comments, final int userId) throws Exception {

        final String sql =
                "UPDATE REPLACEMENT_PRINTER_DETAILS " +
                        "SET NEW_P_MODEL_SELECTED_ID = ?, " +
                        "    NEW_P_MODEL_SELECTED_TEXT = ?, " +
                        "    NEW_P_MODEL_SOURCE = ?, " +
                        "    PRINTER_TYPE = ?, " +
                        "    RECOMMENDED_COMMENTS = ?, " +
                        "    UPDATE_DATE_TIME = SYSTIMESTAMP " +
                        "WHERE ID = ? AND REPLACEMENT_REQUEST_ID = ?";

        try (final Connection con = getConnection();
             final PreparedStatement ps = con.prepareStatement(sql)) {

            if (newModelId != null && newModelId > 0) {
                ps.setInt(1, newModelId);
                ps.setNull(2, Types.VARCHAR);
                ps.setString(3, "AUTO");
            } else if (newModelText != null && !newModelText.isEmpty()) {
                ps.setNull(1, Types.INTEGER);
                ps.setString(2, newModelText);
                ps.setString(3, "MANUAL");
            } else {
                ps.setNull(1, Types.INTEGER);
                ps.setNull(2, Types.VARCHAR);
                ps.setString(3, "MANUAL");
            }

            ps.setString(4, printerType);
            ps.setString(5, comments);
            ps.setInt(6, printerId);
            ps.setInt(7, reqId);

            ps.executeUpdate();
        }
    }

    /**
     * Process Service TL's action on a request (Approve/Reject/Forward).
     *
     * @param reqId           the ID of the replacement request
     * @param userId          the user ID performing the action
     * @param actionType      the type of action (e.g., APPROVE, REJECT, FORWARD)
     * @param comments        action comments
     * @param forwardToUserId the user ID to forward to (required if actionType is FORWARD)
     * @throws Exception if a database error occurs
     */
    public void takeAction(final int reqId, final int userId, final String actionType,
                           final String comments, final Integer forwardToUserId) throws Exception {

        System.out.println("userId " + userId);
        try (final Connection con = getConnection()) {
            con.setAutoCommit(false);
            try {
                // Use default system-generated comment if user didn't provide any
                String effectiveComment = comments;
                if (effectiveComment == null || effectiveComment.trim().isEmpty()) {
                    final String clientName = getClientNameByRequestId(con, reqId);
                    effectiveComment = formatDefaultTLRecommendation(clientName);
                }

                if ("APPROVE".equals(actionType)) {
                    final int toStage = tatDao.getStageIdByCode(con, "STG3_AM_MANAGER_REVIEW");
                    final int toUserAccountId = userDao.getAMManagerUsingRequestId(con, reqId);

                    transitionWorkflowDao.transitionStage(con, reqId, toStage, toUserAccountId, effectiveComment);

                } else if ("REJECT".equals(actionType)) {
                    transitionWorkflowDao.rejectTransitionStage(con, reqId, effectiveComment);

                } else if ("FORWARD".equals(actionType)) {
                    transitionWorkflowDao.forwardTransitionWorkflow(con, reqId, userId, forwardToUserId, effectiveComment);

                }

                con.commit();
            } catch (final Exception e) {
                con.rollback();
                throw e;
            }
        }
    }

    /**
     * Get all AM Managers for filter dropdown
     * Uses OWNER_ROLE from TAT_MASTER which references DESIG.ID
     */
    public List<Map<String, Object>> getAllAMManagers() throws Exception {
        final List<Map<String, Object>> managers = new ArrayList<>();

        // Get users whose designation matches OWNER_ROLE for AM Manager stages
        final String sql =
                "SELECT DISTINCT ua.ID, ua.USER_ID, ua.USER_ID AS NAME " +
                        "FROM USER_ACCOUNT ua " +
                        "JOIN EMP e ON ua.EMP_ID = e.ID " +
                        "WHERE e.DESIGNATION IN (" +
                        "  SELECT DISTINCT OWNER_ROLE FROM TAT_MASTER " +
                        "  WHERE STAGE_CODE IN ('STG3_AM_MANAGER_REVIEW', 'STG5_AM_MANAGER_FINAL')" +
                        ") " +
                        "ORDER BY ua.USER_ID";

        try (final Connection con = getConnection();
             final PreparedStatement ps = con.prepareStatement(sql);
             final ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                final Map<String, Object> m = new HashMap<>();
                m.put("id", rs.getInt("ID"));
                m.put("userId", rs.getString("USER_ID"));
                m.put("name", rs.getString("NAME"));
                managers.add(m);
            }
        }

        return managers;
    }

    /**
     * Get all requesters for filter dropdown
     */
    public List<Map<String, Object>> getAllRequesters() throws Exception {
        final List<Map<String, Object>> requesters = new ArrayList<>();

        final String sql =
                "SELECT DISTINCT ua.ID, ua.USER_ID, ua.USER_ID AS NAME " +
                        "FROM USER_ACCOUNT ua " +
                        "JOIN REPLACEMENT_REQUEST r ON ua.ID = r.REQUESTER_USER_ID " +
                        "ORDER BY ua.USER_ID";

        try (final Connection con = getConnection();
             final PreparedStatement ps = con.prepareStatement(sql);
             final ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                final Map<String, Object> m = new HashMap<>();
                m.put("id", rs.getInt("ID"));
                m.put("userId", rs.getString("USER_ID"));
                m.put("name", rs.getString("NAME"));
                requesters.add(m);
            }
        }

        return requesters;
    }

    /**
     * Get all active printer models for dropdown selection.
     *
     * @return a list of maps containing model ID, name, and color
     * @throws Exception if a database error occurs
     */
    public List<Map<String, Object>> getAllPrinterModels() throws Exception {
        final List<Map<String, Object>> models = new ArrayList<>();

        final String sql = "SELECT ID, MODEL_NAME FROM P_MODEL WHERE STATUS = 0 ORDER BY MODEL_NAME";

        try (final Connection con = getConnection();
             final PreparedStatement ps = con.prepareStatement(sql);
             final ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                final Map<String, Object> m = new HashMap<>();
                m.put("id", rs.getInt("ID"));
                m.put("modelName", rs.getString("MODEL_NAME"));
                models.add(m);
            }
        }

        return models;
    }

    /**
     * Get all filter data for TL screen (AM Managers, Requesters, Printer Models)
     */
    public Map<String, Object> getFilterData() throws Exception {
        final Map<String, Object> data = new HashMap<>();
        data.put("amManagers", getAllAMManagers());
        data.put("requesters", getAllRequesters());
        data.put("printerModels", getAllPrinterModels());
        return data;
    }

    /**
     * Get completed requests for which this TL has provided recommendation.
     * These are requests that this TL reviewed (CURRENT_OWNER_USER_ID = tlUserId) and completed (END_AT IS NOT NULL).
     */
    public List<RequestDetailRow> getCompletedRequestsForTL(final int tlUserId) throws Exception {
        final List<RequestDetailRow> requests = new ArrayList<>();

        final String sql = """
                SELECT r.ID, r.STATUS, r.CREATION_DATE_TIME, r.IS_EDITABLE,
                       c.NAME AS CLIENT_NAME, c.CLIENT_ID,
                       req_ua.USER_ID AS REQUESTER_NAME,
                       owner_ua.USER_ID AS STAGE_OWNER_NAME,
                       tm.STAGE_CODE, tm.DESCRIPTION AS STAGE_NAME,
                       (SELECT COUNT(*) FROM REPLACEMENT_PRINTER_DETAILS pd 
                        WHERE pd.REPLACEMENT_REQUEST_ID = r.ID) AS PRINTER_COUNT,
                       fet.END_AT AS TL_COMPLETED_AT
                FROM REPLACEMENT_REQUEST r
                INNER JOIN RPLCE_FLOW_EVENT_TRACKING fet ON fet.REPLACEMENT_REQUEST_ID = r.ID
                LEFT JOIN CLIENT c ON c.ID = r.CLIENT_DOT_ID_SIGNING
                LEFT JOIN USER_ACCOUNT req_ua ON req_ua.ID = r.REQUESTER_USER_ID
                LEFT JOIN USER_ACCOUNT owner_ua ON owner_ua.ID = r.CURRENT_OWNER_ID
                LEFT JOIN TAT_MASTER tm ON tm.ID = r.CURRENT_STAGE
                WHERE fet.CURRENT_STAGE_ID = 2
                  AND fet.CURRENT_OWNER_USER_ID = ?
                  AND fet.END_AT IS NOT NULL
                  AND r.STATUS != 'CLOSED'
                ORDER BY fet.END_AT DESC
                """;

        try (final Connection con = getConnection();
             final PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, tlUserId);
            try (final ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    final RequestDetailRow row = new RequestDetailRow();
                    row.setId(rs.getInt("ID"));
                    row.setClientId(rs.getString("CLIENT_ID"));
                    row.setClientName(rs.getString("CLIENT_NAME"));
                    row.setRequesterName(rs.getString("REQUESTER_NAME"));
                    row.setStageOwnerName(rs.getString("STAGE_OWNER_NAME"));
                    row.setPrinterCount(rs.getInt("PRINTER_COUNT"));
                    row.setCreatedAt(rs.getTimestamp("CREATION_DATE_TIME"));
                    row.setCurrentStage(rs.getString("STAGE_CODE"));
                    row.setCurrentStageName(rs.getString("STAGE_NAME"));
                    row.setEditable(rs.getInt("IS_EDITABLE") == 1);
                    row.setStatus(rs.getString("STATUS"));

                    requests.add(row);
                }
            }
        }

        return requests;
    }

    /**
     * Get printer history (service calls) for a specific serial number
     */
    public Map<String, Object> getPrinterHistoryBySerial(final String serial) throws Exception {
        final Map<String, Object> history = new HashMap<>();

        final String historySql =
                "SELECT cr.CALL_ID, " +
                        "       cr.CALL_DATE, " +
                        "       cr.CALL_DETAILS, " +
                        "       cr.CALL_STATUS_NAME, " +
                        "       cr.P_SERIAL " +
                        "FROM CLIENT_REQUEST cr " +
                        "WHERE TRIM(UPPER(cr.P_SERIAL)) = TRIM(UPPER(?)) " +
                        "  AND REGEXP_LIKE(cr.CALL_DATE, '^\\s*\\d{2}-\\d{2}-\\d{4}\\s*$') " +
                        "  AND TO_DATE(TRIM(cr.CALL_DATE), 'DD-MM-YYYY') >= ADD_MONTHS(TRUNC(SYSDATE), -6) " +
                        "ORDER BY TO_DATE(TRIM(cr.CALL_DATE), 'DD-MM-YYYY') DESC";

        final List<Map<String, Object>> serviceCalls = new ArrayList<>();

        try (final Connection con = getConnection();
             final PreparedStatement ps = con.prepareStatement(historySql)) {

            ps.setString(1, serial);

            try (final ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    final Map<String, Object> call = new HashMap<>();
                    call.put("callId", rs.getString("CALL_ID"));
                    call.put("callDate", rs.getString("CALL_DATE"));
                    call.put("callDetails", rs.getString("CALL_DETAILS"));
                    call.put("callStatusName", rs.getString("CALL_STATUS_NAME"));
                    call.put("serial", rs.getString("P_SERIAL"));
                    serviceCalls.add(call);
                }
            }
        }

        history.put("serviceCalls", serviceCalls);
        history.put("serviceCallsCount", serviceCalls.size());
        history.put("serial", serial);
        return history;
    }

    /**
     * Get the client name associated with a replacement request (for default comments).
     *
     * @param con   the database connection
     * @param reqId the ID of the replacement request
     * @return the name of the client
     * @throws SQLException if a database error occurs
     */
    private String getClientNameByRequestId(final Connection con, final int reqId) throws SQLException {
        final String sql = "SELECT c.NAME FROM REPLACEMENT_REQUEST r " +
                "LEFT JOIN CLIENT c ON c.ID = r.CLIENT_DOT_ID_SIGNING " +
                "WHERE r.ID = ?";

        try (final PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, reqId);
            try (final ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("NAME");
                }
            }
        }
        return null;
    }
}
