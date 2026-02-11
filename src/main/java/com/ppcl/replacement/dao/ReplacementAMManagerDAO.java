package com.ppcl.replacement.dao;

import com.ppcl.replacement.model.*;
import com.ppcl.replacement.util.DBConnectionPool;

import java.sql.*;
import java.util.*;

import static com.ppcl.replacement.constants.MessageConstant.formatDefaultAMCommercialApproved;

/**
 * DAO for AM Manager operations
 * Uses tables: REPLACEMENT_REQUEST, REPLACEMENT_PRINTER_DETAILS, TAT_MASTER,
 * RPLCE_FLOW_EVENT_TRACKING, AGR, AGR_PROD, CLIENT, USER_ACCOUNT, P_MODEL
 */
public class ReplacementAMManagerDAO extends BaseDAO {

    private static final String AM_MANAGER_STAGE = "STG3_AM_MANAGER_REVIEW";
    private final TransitionWorkflowDao transitionWorkflowDao = new TransitionWorkflowDao();
    private final UserDAO userDao = new UserDAO();
    private final TatDao tatDao = new TatDao();

    /**
     * Get pending requests for AM Manager stage.
     *
     * @param dateFrom  optional start date filter (YYYY-MM-DD)
     * @param dateTo    optional end date filter (YYYY-MM-DD)
     * @param requester optional requester name filter
     * @param am        optional account manager name filter
     * @param status    optional status filter
     * @return a list of {@link RequestDetailRow} objects
     * @throws Exception if a database error occurs
     */
    public List<RequestDetailRow> getPendingRequestsForAMManager(final String dateFrom, final String dateTo,
                                                                 final String requester, final String am,
                                                                 final String status) throws Exception {
        final List<RequestDetailRow> list = new ArrayList<>();

        final StringBuilder sql = new StringBuilder();
        sql.append("SELECT r.ID, r.STATUS, r.CREATION_DATE_TIME, r.IS_EDITABLE, ");
        sql.append("c.NAME AS CLIENT_NAME, c.CLIENT_ID, ");
        sql.append("req_ua.USER_ID AS REQUESTER_NAME, ");
        sql.append("owner_ua.USER_ID AS ACCOUNT_MANAGER, ");
        sql.append("(SELECT sign_ua.USER_ID FROM CLIENT_ACCESS ca JOIN USER_ACCOUNT sign_ua ON sign_ua.ID = ca.USER_ID WHERE ca.CLIENT_ID = r.CLIENT_DOT_ID_SIGNING FETCH FIRST 1 ROW ONLY) AS SIGN_IN_USER_ID, ");
        sql.append("tm.STAGE_CODE, tm.DESCRIPTION AS STAGE_NAME, ");
        sql.append("(SELECT COUNT(*) FROM REPLACEMENT_PRINTER_DETAILS pd WHERE pd.REPLACEMENT_REQUEST_ID = r.ID) AS PRINTER_COUNT, ");
        sql.append("fet.START_AT AS LAST_ACTION_DATE, ");
        sql.append("(SELECT ua2.USER_ID FROM USER_ACCOUNT ua2 WHERE ua2.ID = fet.CURRENT_OWNER_USER_ID) AS LAST_ACTION_BY, ");
        sql.append("ROUND(SYSDATE - CAST(fet.START_AT AS DATE)) AS STAGE_TAT_DAYS, ");
        sql.append("ROUND(SYSDATE - CAST(r.CREATION_DATE_TIME AS DATE)) AS OVERALL_TAT_DAYS, ");
        sql.append("NVL(tm.TAT_DURATION, 20) AS TARGET_TAT_DAYS ");
        sql.append("FROM REPLACEMENT_REQUEST r ");
        sql.append("LEFT JOIN CLIENT c ON c.ID = r.CLIENT_DOT_ID_SIGNING ");
        sql.append("LEFT JOIN USER_ACCOUNT req_ua ON req_ua.ID = r.REQUESTER_USER_ID ");
        sql.append("LEFT JOIN USER_ACCOUNT owner_ua ON owner_ua.ID = r.CURRENT_OWNER_ID ");
        sql.append("LEFT JOIN TAT_MASTER tm ON tm.ID = r.CURRENT_STAGE ");
        sql.append("LEFT JOIN (SELECT fet1.* FROM RPLCE_FLOW_EVENT_TRACKING fet1 ");
        sql.append("           WHERE fet1.END_AT IS NULL) fet ");
        sql.append("       ON fet.REPLACEMENT_REQUEST_ID = r.ID ");
        sql.append("WHERE r.STATUS IN ('OPEN', 'PENDING') ");
        sql.append("  AND tm.STAGE_CODE = '").append(AM_MANAGER_STAGE).append("' ");

        final List<Object> params = new ArrayList<>();

        if (dateFrom != null && !dateFrom.isEmpty()) {
            sql.append("AND r.CREATION_DATE_TIME >= TO_DATE(?, 'YYYY-MM-DD') ");
            params.add(dateFrom);
        }
        if (dateTo != null && !dateTo.isEmpty()) {
            sql.append("AND r.CREATION_DATE_TIME <= TO_DATE(?, 'YYYY-MM-DD') + 1 ");
            params.add(dateTo);
        }
        if (requester != null && !requester.isEmpty()) {
            sql.append("AND req_ua.USER_ID = ? ");
            params.add(requester);
        }
        if (am != null && !am.isEmpty()) {
            sql.append("AND EXISTS (SELECT 1 FROM CLIENT_ACCESS ca JOIN USER_ACCOUNT sign_ua ON sign_ua.ID = ca.USER_ID WHERE ca.CLIENT_ID = r.CLIENT_DOT_ID_SIGNING AND sign_ua.USER_ID = ?) ");
            params.add(am);
        }

        sql.append("ORDER BY r.CREATION_DATE_TIME DESC");

        try (final Connection conn = getConnection();
             final PreparedStatement ps = conn.prepareStatement(sql.toString())) {

            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }

            final ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                final RequestDetailRow row = new RequestDetailRow();
                row.setId(rs.getInt("ID"));
                row.setClientId(rs.getString("CLIENT_ID"));
                row.setClientName(rs.getString("CLIENT_NAME"));
                row.setRequesterName(rs.getString("REQUESTER_NAME"));
                row.setAccountManager(rs.getString("ACCOUNT_MANAGER"));
                row.setSignInUserId(rs.getString("SIGN_IN_USER_ID"));
                row.setPrinterCount(rs.getInt("PRINTER_COUNT"));
                row.setCreatedAt(rs.getTimestamp("CREATION_DATE_TIME"));
                row.setCurrentStage(rs.getString("STAGE_CODE"));
                row.setCurrentStageName(rs.getString("STAGE_NAME"));
                row.setEditable(rs.getInt("IS_EDITABLE") == 1);
                row.setStatus(rs.getString("STATUS"));
                row.setLastActionDate(rs.getTimestamp("LAST_ACTION_DATE"));
                row.setLastActionBy(rs.getString("LAST_ACTION_BY"));
                row.setStageTatDays(rs.getInt("STAGE_TAT_DAYS"));
                row.setOverallTatDays(rs.getInt("OVERALL_TAT_DAYS"));
                row.setTargetTatDays(rs.getInt("TARGET_TAT_DAYS"));
                list.add(row);
            }
        }
        return list;
    }

    /**
     * Get full request details including printers and agreements.
     *
     * @param reqId the ID of the replacement request
     * @return a {@link Map} containing request details, printers, and agreements
     * @throws Exception if a database error occurs
     */
    public Map<String, Object> getFullRequestDetails(final int reqId) throws Exception {
        final Map<String, Object> result = new HashMap<>();

        final String reqSql =
                "SELECT r.ID, r.STATUS, r.REPLACEMENT_TYPE, " +
                        "       c.NAME AS CLIENT_NAME, c.CLIENT_ID, c.CITY, c.BRANCH, " +
                        "       req_ua.USER_ID AS REQUESTER_NAME, " +
                        "       owner_ua.USER_ID AS ACCOUNT_MANAGER, " +
                        "       tm.STAGE_CODE, tm.DESCRIPTION AS STAGE_NAME, " +
                        "       rr.NAME AS REASON_NAME, " +
                        "       (SELECT fet.COMMENTS FROM RPLCE_FLOW_EVENT_TRACKING fet " +
                        "        WHERE fet.REPLACEMENT_REQUEST_ID = r.ID " +
                        "        ORDER BY fet.ID DESC FETCH FIRST 1 ROW ONLY) AS TL_COMMENTS " +
                        "FROM REPLACEMENT_REQUEST r " +
                        "LEFT JOIN CLIENT c ON c.ID = r.CLIENT_DOT_ID_SIGNING " +
                        "LEFT JOIN USER_ACCOUNT req_ua ON req_ua.ID = r.REQUESTER_USER_ID " +
                        "LEFT JOIN USER_ACCOUNT owner_ua ON owner_ua.ID = r.CURRENT_OWNER_ID " +
                        "LEFT JOIN TAT_MASTER tm ON tm.ID = r.CURRENT_STAGE " +
                        "LEFT JOIN REPLACEMENT_REASON rr ON rr.ID = r.REPLACEMENT_REASON_ID " +
                        "WHERE r.ID = ?";

        try (final Connection conn = getConnection();
             final PreparedStatement ps = conn.prepareStatement(reqSql)) {

            ps.setInt(1, reqId);
            final ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                final Map<String, Object> reqInfo = new HashMap<>();
                reqInfo.put("id", rs.getInt("ID"));
                reqInfo.put("clientName", rs.getString("CLIENT_NAME"));
                reqInfo.put("clientId", rs.getString("CLIENT_ID"));
                reqInfo.put("city", rs.getString("CITY"));
                reqInfo.put("branch", rs.getString("BRANCH"));
                reqInfo.put("requesterName", rs.getString("REQUESTER_NAME"));
                reqInfo.put("requesterRole", "TL");
                reqInfo.put("currentStage", rs.getString("STAGE_CODE"));
                reqInfo.put("stageName", rs.getString("STAGE_NAME"));
                reqInfo.put("reasonName", rs.getString("REASON_NAME"));
                reqInfo.put("tlComments", rs.getString("TL_COMMENTS"));
                result.put("request", reqInfo);
            }
        }

        result.put("printers", getPrintersList(reqId));
        return result;
    }

    /**
     * Get printer details for a request (with agreement info)
     */
    public Map<String, Object> getPrinterDetails(final int reqId) throws Exception {
        final Map<String, Object> result = new HashMap<>();
        result.put("printers", getPrintersList(reqId));
        return result;
    }

    private List<Map<String, Object>> getPrintersList(final int reqId) throws Exception {
        final List<Map<String, Object>> printers = new ArrayList<>();

        final String sql =
                "SELECT rpd.ID, rpd.AGR_PROD_ID, rpd.EXISTING_SERIAL, " +
                        "       rpd.NEW_P_MODEL_SELECTED_ID, rpd.NEW_P_MODEL_SELECTED_TEXT, " +
                        "       rpd.NEW_P_MODEL_SOURCE, rpd.RECOMMENDED_COMMENTS, " +
                        "       pm.MODEL_NAME AS EXISTING_MODEL, " +
                        "       new_pm.MODEL_NAME AS NEW_MODEL_NAME, " +
                        "       c.BRANCH AS LOCATION, c.CITY, " +
                        "       ap.RENT AS RENTAL_AMOUNT, ap.A4_RATE AS BLACK_RATE, ap.A4_RATE_COLOR AS COLOR_RATE, " +
                        "       a.AGR_NO AS AGREEMENT_NO " +
                        "FROM REPLACEMENT_PRINTER_DETAILS rpd " +
                        "LEFT JOIN P_MODEL pm ON pm.ID = rpd.EXISTING_P_MODEL_ID " +
                        "LEFT JOIN P_MODEL new_pm ON new_pm.ID = rpd.NEW_P_MODEL_SELECTED_ID " +
                        "LEFT JOIN CLIENT c ON c.ID = rpd.CLIENT_DOT_ID " +
                        "LEFT JOIN AGR_PROD ap ON ap.ID = rpd.AGR_PROD_ID " +
                        "LEFT JOIN AGR a ON a.ID = ap.AGR_ID " +
                        "WHERE rpd.REPLACEMENT_REQUEST_ID = ?";

        try (final Connection conn = getConnection();
             final PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, reqId);
            final ResultSet rs = ps.executeQuery();

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
                printer.put("rentalAmount", rs.getBigDecimal("RENTAL_AMOUNT"));
                printer.put("blackRate", rs.getBigDecimal("BLACK_RATE"));
                printer.put("colorRate", rs.getBigDecimal("COLOR_RATE"));
                printer.put("agreementNo", rs.getString("AGREEMENT_NO"));
                printer.put("printerType", "NEW");
                printers.add(printer);
            }
        }

        return printers;
    }

    /**
     * Get printer history including billing and page counts from AGR_PROD
     */
    public Map<String, Object> getPrinterHistory(final int agrProdId) throws Exception {
        final Map<String, Object> history = new HashMap<>();

        try (final Connection conn = getConnection()) {

            final String sql =
                    "SELECT ap.ID, ap.SERIAL, ap.RENT, ap.A4_RATE, ap.A4_RATE_COLOR, " +
                            "       ap.FREE_PRINTS, ap.PAGE_COMMITED, ap.COMMITMENT_PERIOD, " +
                            "       ap.AMC, ap.AMC_TYPE, ap.PRINTER_COLOR, ap.AGR_COMMERCE_TYPE, " +
                            "       pm.MODEL_NAME, pm.COLOR AS IS_COLOR_PRINTER, " +
                            "       a.AGR_NO, a.AGR_DATE, a.EXPIRY_DATE, " +
                            "       c.NAME AS CLIENT_NAME, c.CITY, c.BRANCH AS LOCATION " +
                            "FROM AGR_PROD ap " +
                            "LEFT JOIN P_MODEL pm ON pm.ID = ap.P_MODEL " +
                            "LEFT JOIN AGR a ON a.ID = ap.AGR_ID " +
                            "LEFT JOIN CLIENT c ON c.ID = ap.CLIENT_BR_ID " +
                            "WHERE ap.ID = ?";

            try (final PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, agrProdId);
                final ResultSet rs = ps.executeQuery();

                if (rs.next()) {
                    final Map<String, Object> agreement = new HashMap<>();
                    agreement.put("agreementNo", rs.getString("AGR_NO"));
                    agreement.put("startDate", rs.getString("AGR_DATE"));
                    agreement.put("endDate", rs.getString("EXPIRY_DATE"));
                    agreement.put("billingType", "Per Page");
                    agreement.put("monoRate", rs.getBigDecimal("A4_RATE"));
                    agreement.put("colorRate", rs.getBigDecimal("A4_RATE_COLOR"));
                    agreement.put("rent", rs.getBigDecimal("RENT"));
                    agreement.put("freePrints", rs.getInt("FREE_PRINTS"));
                    agreement.put("commitmentPeriod", rs.getInt("COMMITMENT_PERIOD"));
                    agreement.put("amc", rs.getBigDecimal("AMC"));
                    agreement.put("location", rs.getString("LOCATION"));
                    agreement.put("productName", rs.getString("MODEL_NAME"));
                    agreement.put("serialNumber", rs.getString("SERIAL"));

                    // AMC Type text
                    final int amcType = rs.getInt("AMC_TYPE");
                    agreement.put("amcType", getAmcTypeText(amcType));

                    // Color printer flag
                    final int isColorPrinter = rs.getInt("IS_COLOR_PRINTER");
                    final int printerColor = rs.getInt("PRINTER_COLOR");
                    agreement.put("colorPrinter", isColorPrinter == 1 || printerColor == 1);

                    // Commercial type
                    final int commerceType = rs.getInt("AGR_COMMERCE_TYPE");
                    agreement.put("commercialType", getCommercialTypeText(commerceType));

                    // Tax defaults
                    agreement.put("taxPercent", 18);
                    agreement.put("taxInclusive", false);

                    history.put("agreement", agreement);

                    final Map<String, Object> printer = new HashMap<>();
                    printer.put("serial", rs.getString("SERIAL"));
                    printer.put("modelName", rs.getString("MODEL_NAME"));
                    printer.put("clientName", rs.getString("CLIENT_NAME"));
                    printer.put("city", rs.getString("CITY"));
                    printer.put("location", rs.getString("LOCATION"));
                    history.put("printer", printer);
                }
            }
        }

        return history;
    }

    /**
     * Get current commercials for request printers
     */
    public List<Map<String, Object>> getCurrentCommercials(final int reqId) throws Exception {
        final List<Map<String, Object>> commercials = new ArrayList<>();

        final String sql =
                "SELECT rpd.ID, rpd.EXISTING_SERIAL, " +
                        "       pm.MODEL_NAME, pm.COLOR AS IS_COLOR_PRINTER, " +
                        "       ap.AGR_NO AS AGREEMENT_NO, " +
                        "       ap.A4_RATE AS BLACK_RATE, ap.A4_RATE_COLOR AS COLOR_RATE, " +
                        "       ap.RENT, ap.FREE_PRINTS, ap.COMMITMENT_PERIOD, " +
                        "       ap.AMC, ap.AMC_TYPE, ap.PRINTER_COLOR, ap.AGR_COMMERCE_TYPE, " +
                        "       c.BRANCH AS LOCATION, c.NAME AS CLIENT_NAME " +
                        "FROM REPLACEMENT_PRINTER_DETAILS rpd " +
                        "LEFT JOIN P_MODEL pm ON pm.ID = rpd.EXISTING_P_MODEL_ID " +
                        "LEFT JOIN AGR_PROD ap ON ap.ID = rpd.AGR_PROD_ID " +
                        "LEFT JOIN CLIENT c ON c.ID = ap.CLIENT_BR_ID " +
                        "WHERE rpd.REPLACEMENT_REQUEST_ID = ?";

        try (final Connection conn = getConnection();
             final PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, reqId);
            final ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                final Map<String, Object> commercial = new HashMap<>();
                commercial.put("id", rs.getInt("ID"));
                commercial.put("serial", rs.getString("EXISTING_SERIAL"));
                commercial.put("productName", rs.getString("MODEL_NAME"));
                commercial.put("agreementNo", rs.getString("AGREEMENT_NO"));
                commercial.put("a4Rate", rs.getBigDecimal("BLACK_RATE"));
                commercial.put("a4RateColor", rs.getBigDecimal("COLOR_RATE"));
                commercial.put("rent", rs.getBigDecimal("RENT"));
                commercial.put("freePrints", rs.getInt("FREE_PRINTS"));
                commercial.put("commitmentPeriod", rs.getInt("COMMITMENT_PERIOD"));
                commercial.put("amc", rs.getBigDecimal("AMC"));
                commercial.put("location", rs.getString("LOCATION"));
                commercial.put("clientName", rs.getString("CLIENT_NAME"));

                // Determine AMC Type text
                final int amcType = rs.getInt("AMC_TYPE");
                commercial.put("amcType", getAmcTypeText(amcType));

                // Determine if color printer (from P_MODEL.COLOR or AGR_PROD.PRINTER_COLOR)
                final int isColorPrinter = rs.getInt("IS_COLOR_PRINTER");
                final int printerColor = rs.getInt("PRINTER_COLOR");
                commercial.put("colorPrinter", isColorPrinter == 1 || printerColor == 1);

                // Commercial type from AGR_COMMERCE_TYPE
                final int commerceType = rs.getInt("AGR_COMMERCE_TYPE");
                commercial.put("commercialType", getCommercialTypeText(commerceType));

                // Tax defaults (can be enhanced if tax columns exist)
                commercial.put("taxPercent", 18);
                commercial.put("taxInclusive", false);

                commercials.add(commercial);
            }
        }

        return commercials;
    }

    private String getAmcTypeText(final int amcType) {
        switch (amcType) {
            case 1:
                return "Comprehensive AMC";
            case 2:
                return "Non-Comprehensive AMC";
            case 3:
                return "On-Call AMC";
            default:
                return "No AMC";
        }
    }

    private String getCommercialTypeText(final int commerceType) {
        switch (commerceType) {
            case 1:
                return "RENTAL";
            case 2:
                return "PER PRINT";
            case 3:
                return "RENTAL + PER PRINT";
            case 4:
                return "AMC";
            case 5:
                return "OUTRIGHT";
            default:
                return "STANDARD";
        }
    }

    /**
     * Get commercial details for a specific printer
     */
    public Map<String, Object> getCommercialDetails(final int printerId) throws Exception {
        final Map<String, Object> commercial = new HashMap<>();
        System.out.println("getCommercialDetails called " + printerId);
        final String sql =
                "SELECT rpd.ID, rpd.EXISTING_SERIAL, rpd.RECOMMENDED_COMMENTS, " +
                        "       ap.A4_RATE, ap.A4_RATE_COLOR, ap.RENT " +
                        "FROM REPLACEMENT_PRINTER_DETAILS rpd " +
                        "LEFT JOIN AGR_PROD ap ON ap.ID = rpd.AGR_PROD_ID " +
                        "WHERE rpd.ID = ?";

        try (final Connection conn = getConnection();
             final PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, printerId);
            final ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                commercial.put("id", rs.getInt("ID"));
                commercial.put("serial", rs.getString("EXISTING_SERIAL"));
                commercial.put("blackRate", rs.getBigDecimal("A4_RATE"));
                commercial.put("colorRate", rs.getBigDecimal("A4_RATE_COLOR"));
                commercial.put("rental", rs.getBigDecimal("RENT"));
                commercial.put("comments", rs.getString("RECOMMENDED_COMMENTS"));
            }
        }

        return commercial;
    }

    /**
     * Reply to request with commercial comments
     */
    public void replyRequest(final int reqId, final int userId, final String comments, final String replaceExisting) throws Exception {
        try (final Connection conn = getConnection()) {
            conn.setAutoCommit(false);
            try {
                final int userAccountId = userId;

                final String sql = "INSERT INTO RPLCE_COMMENTS_TRACKING (SENDER_ID, RECEIVER_ID, MESSAGE) " +
                        "VALUES (?, ?, ?)";
                try (final PreparedStatement ps = conn.prepareStatement(sql)) {
                    ps.setInt(1, userAccountId);
                    ps.setInt(2, userAccountId);
                    ps.setString(3, "REPLY: " + (replaceExisting != null ? "Replace Existing: " + replaceExisting + ". " : "") + comments);
                    ps.executeUpdate();
                }

                final String updateSql = "UPDATE REPLACEMENT_REQUEST SET UPDATE_DATE_TIME = SYSTIMESTAMP WHERE ID = ?";
                try (final PreparedStatement ps = conn.prepareStatement(updateSql)) {
                    ps.setInt(1, reqId);
                    ps.executeUpdate();
                }

                conn.commit();
            } catch (final Exception e) {
                conn.rollback();
                throw e;
            }
        }
    }

    /**
     * Reject request with reason and comments
     */
    public void rejectRequest(final int reqId, final int userId, final String rejectionReason, final String comments) throws Exception {

        try (final Connection conn = getConnection()) {
            conn.setAutoCommit(false);
            try {
                transitionWorkflowDao.rejectTransitionStage(conn, reqId, comments);

                conn.commit();
            } catch (final Exception e) {
                conn.rollback();
                throw e;
            }
        }
    }

    /**
     * Forward request to another user
     */
    public void forwardRequest(final int reqId, final int userId, final Integer forwardToUserId,
                               final String targetRole, final String comments) throws Exception {

        try (final Connection conn = getConnection()) {
            conn.setAutoCommit(false);
            try {
                transitionWorkflowDao.forwardTransitionWorkflow(conn, reqId, userId, forwardToUserId, comments);
                conn.commit();
            } catch (final Exception e) {
                conn.rollback();
                throw e;
            }
        }
    }

    /**
     * Approve request and move to next stage (AM Commercial)
     */
    public void approveRequest(final int reqId, final int userId, final String overallComments) throws Exception {
        try (final Connection conn = getConnection()) {
            conn.setAutoCommit(false);
            try {
                approveRequest(conn, reqId, userId, overallComments);
                conn.commit();
            } catch (final Exception e) {
                conn.rollback();
                throw e;
            }
        }
    }

    /**
     * Approve request and move to next stage (using shared connection for atomicity)
     */
    public void approveRequest(final Connection conn, final int reqId, final int userId, final String overallComments) throws Exception {
        // Use default system-generated comment if user didn't provide any
        String effectiveComment = overallComments;
        if (effectiveComment == null || effectiveComment.trim().isEmpty()) {
            final String clientName = getClientNameByRequestId(conn, reqId);
            effectiveComment = formatDefaultAMCommercialApproved(clientName);
        }

        final int nextStageId = tatDao.getStageIdByCode(conn, "STG4_AM_COMMERCIAL");
        final int nextStageOwnerId = userDao.getEffectiveCROIdByRequestId(conn, reqId);
        transitionWorkflowDao.transitionStage(conn, reqId, nextStageId, nextStageOwnerId, effectiveComment);
    }

    /**
     * Submit request to AM
     */
    public void submitRequest(final int reqId, final int userId, final String comments) throws Exception {
        approveRequest(reqId, userId, comments);
    }

    /**
     * Get communication logs for a request
     */
    public List<Map<String, Object>> getCommunicationLogs(final int reqId) throws Exception {
        final List<Map<String, Object>> logs = new ArrayList<>();

        final String sql =
                "SELECT fet.ID, fet.START_AT, fet.END_AT, fet.COMMENTS, " +
                        "       ua.USER_ID AS ACTOR_NAME, " +
                        "       tm.STAGE_CODE AS ACTION_TYPE " +
                        "FROM RPLCE_FLOW_EVENT_TRACKING fet " +
                        "LEFT JOIN USER_ACCOUNT ua ON ua.ID = fet.CURRENT_OWNER_USER_ID " +
                        "LEFT JOIN TAT_MASTER tm ON tm.ID = fet.CURRENT_STAGE_ID " +
                        "WHERE fet.REPLACEMENT_REQUEST_ID = ? " +
                        "ORDER BY fet.ID DESC";

        try (final Connection conn = getConnection();
             final PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, reqId);
            final ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                final Map<String, Object> log = new HashMap<>();
                log.put("id", rs.getInt("ID"));
                log.put("actionType", rs.getString("ACTION_TYPE"));
                log.put("actorName", rs.getString("ACTOR_NAME"));
                log.put("comments", rs.getString("COMMENTS"));
                log.put("createdAt", rs.getTimestamp("START_AT"));
                log.put("endAt", rs.getTimestamp("END_AT"));
                logs.add(log);
            }
        }

        return logs;
    }

    /**
     * Get hierarchy users for forwarding
     */
    public List<Map<String, String>> getHierarchyUsers() throws Exception {
        final List<Map<String, String>> users = new ArrayList<>();

        final String sql =
                "SELECT ua.ID, ua.USER_ID, e.NAME, d.NAME AS ROLE " +
                        "FROM USER_ACCOUNT ua " +
                        "LEFT JOIN EMP e ON e.ID = ua.EMP_ID " +
                        "LEFT JOIN DESIG d ON d.ID = e.DESIGNATION " +
                        "WHERE e.ISACTIVE = 1 " +
                        "ORDER BY d.HIERARCHY_LEVEL, e.NAME";

        try (final Connection conn = getConnection();
             final PreparedStatement ps = conn.prepareStatement(sql);
             final ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                final Map<String, String> user = new HashMap<>();
                user.put("userId", rs.getString("USER_ID"));
                user.put("name", rs.getString("NAME"));
                user.put("role", rs.getString("ROLE"));
                users.add(user);
            }
        }

        return users;
    }

    /**
     * Get all requesters for filter dropdown
     */
    public List<Map<String, Object>> getAllRequesters() throws Exception {
        final List<Map<String, Object>> requesters = new ArrayList<>();

        final String sql =
                "SELECT DISTINCT ua.ID, ua.USER_ID, e.NAME " +
                        "FROM USER_ACCOUNT ua " +
                        "LEFT JOIN EMP e ON e.ID = ua.EMP_ID " +
                        "JOIN REPLACEMENT_REQUEST r ON ua.ID = r.REQUESTER_USER_ID " +
                        "ORDER BY e.NAME";

        try (final Connection conn = getConnection();
             final PreparedStatement ps = conn.prepareStatement(sql);
             final ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                final Map<String, Object> m = new HashMap<>();
                m.put("id", rs.getInt("ID"));
                m.put("userId", rs.getString("USER_ID"));
                m.put("name", rs.getString("NAME") != null ? rs.getString("NAME") : rs.getString("USER_ID"));
                requesters.add(m);
            }
        }

        return requesters;
    }

    /**
     * Get all account managers (sign-in users from CLIENT_ACCESS) for filter dropdown
     */
    public List<Map<String, Object>> getAllAccountManagers() throws Exception {
        final List<Map<String, Object>> managers = new ArrayList<>();

        final String sql =
                "SELECT DISTINCT ua.ID, ua.USER_ID, e.NAME " +
                        "FROM USER_ACCOUNT ua " +
                        "LEFT JOIN EMP e ON e.ID = ua.EMP_ID " +
                        "JOIN CLIENT_ACCESS ca ON ca.USER_ID = ua.ID " +
                        "JOIN REPLACEMENT_REQUEST r ON r.CLIENT_DOT_ID_SIGNING = ca.CLIENT_ID " +
                        "ORDER BY e.NAME";

        try (final Connection conn = getConnection();
             final PreparedStatement ps = conn.prepareStatement(sql);
             final ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                final Map<String, Object> m = new HashMap<>();
                m.put("id", rs.getInt("ID"));
                m.put("userId", rs.getString("USER_ID"));
                m.put("name", rs.getString("NAME") != null ? rs.getString("NAME") : rs.getString("USER_ID"));
                managers.add(m);
            }
        }

        return managers;
    }

    /**
     * Update AM Manager recommendation for a printer.
     *
     * @param printerId    the ID of the printer detail record
     * @param reqId        the ID of the replacement request
     * @param newModelId   the ID of the new printer model, or {@code null}
     * @param newModelText the text description of the new model if not in master, or {@code null}
     * @param comments     recommendation comments
     * @param userId       the ID of the user performing the update
     * @throws Exception if a database error occurs
     */
    public void updateRecommendation(final int printerId, final int reqId, final Integer newModelId,
                                     final String newModelText, final String comments, final int userId) throws Exception {
        try (final Connection conn = getConnection()) {
            updateRecommendation(conn, printerId, reqId, newModelId, newModelText, comments, userId);
        }
    }

    /**
     * Update AM Manager recommendation for a printer using a shared connection.
     *
     * @param conn         the database connection
     * @param printerId    the ID of the printer detail record
     * @param reqId        the ID of the replacement request
     * @param newModelId   the ID of the new printer model, or {@code null}
     * @param newModelText the text description of the new model if not in master, or {@code null}
     * @param comments     recommendation comments
     * @param userId       the ID of the user performing the update
     * @throws Exception if a database error occurs
     */
    public void updateRecommendation(final Connection conn, final int printerId, final int reqId, final Integer newModelId,
                                     final String newModelText, final String comments, final int userId) throws Exception {

        final String sql =
                "UPDATE REPLACEMENT_PRINTER_DETAILS " +
                        "SET NEW_P_MODEL_SELECTED_ID = ?, " +
                        "    NEW_P_MODEL_SELECTED_TEXT = ?, " +
                        "    NEW_P_MODEL_SOURCE = ?, " +
                        "    RECOMMENDED_COMMENTS = ?, " +
                        "    UPDATE_DATE_TIME = SYSTIMESTAMP " +
                        "WHERE ID = ? AND REPLACEMENT_REQUEST_ID = ?";

        try (final PreparedStatement ps = conn.prepareStatement(sql)) {
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

            ps.setString(4, comments);
            ps.setInt(5, printerId);
            ps.setInt(6, reqId);
            ps.executeUpdate();
        }
    }

    /**
     * Update commercial decision for a printer (Continue with existing or new terms).
     *
     * @param conn             the database connection
     * @param printerId        the ID of the printer detail record
     * @param reqId            the ID of the replacement request
     * @param continueExisting whether to continue with existing commercial terms
     * @param comments         commercial decision comments
     * @throws Exception if a database error occurs
     */
    public void updateCommercialDecision(final Connection conn, final int printerId, final int reqId,
                                         final boolean continueExisting, final String comments) throws Exception {

        final String sql =
                "UPDATE REPLACEMENT_PRINTER_DETAILS " +
                        "SET CONTINUE_EXISTING_COMMERCIAL = ?, " +
                        "    AM_COMMERCIAL_COMMENTS = ?, " +
                        "    UPDATE_DATE_TIME = SYSTIMESTAMP " +
                        "WHERE ID = ? AND REPLACEMENT_REQUEST_ID = ?";

        try (final PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, continueExisting ? "Y" : "N");
            ps.setString(2, comments);
            ps.setInt(3, printerId);
            ps.setInt(4, reqId);
            ps.executeUpdate();
        }
    }

    /**
     * Get all active printer models for dropdown selection.
     *
     * @return a list of maps containing model ID, name, and color
     * @throws Exception if a database error occurs
     */
    public List<Map<String, Object>> getAllPrinterModels() throws Exception {
        final List<Map<String, Object>> models = new ArrayList<>();

        final String sql = "SELECT ID, MODEL_NAME, COLOR FROM P_MODEL WHERE STATUS = 0 ORDER BY MODEL_NAME";

        try (final Connection conn = getConnection();
             final PreparedStatement ps = conn.prepareStatement(sql);
             final ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                final Map<String, Object> m = new HashMap<>();
                m.put("id", rs.getInt("ID"));
                m.put("modelName", rs.getString("MODEL_NAME"));
                m.put("color", rs.getString("COLOR"));
                models.add(m);
            }
        }

        return models;
    }

    /**
     * Get completed requests for which AM Manager has taken action.
     * These are requests that passed through AM Manager stage (STG3_AM_MANAGER_REVIEW) and end_at is not null.
     */
    public List<RequestDetailRow> getCompletedRequestsForAMManager() throws Exception {
        return getCompletedRequestsForAMManager(null, null, null, null);
    }

    /**
     * Get completed requests for which AM Manager has taken action with filters.
     * These are requests that passed through AM Manager stage (STG3_AM_MANAGER_REVIEW) and end_at is not null.
     */
    public List<RequestDetailRow> getCompletedRequestsForAMManager(final String dateFrom, final String dateTo,
                                                                   final String requester, final String am) throws Exception {
        final List<RequestDetailRow> requests = new ArrayList<>();

        final StringBuilder sql = new StringBuilder();
        sql.append("SELECT r.ID, r.STATUS, r.CREATION_DATE_TIME, r.IS_EDITABLE, ");
        sql.append("c.NAME AS CLIENT_NAME, c.CLIENT_ID, ");
        sql.append("req_ua.USER_ID AS REQUESTER_NAME, ");
        sql.append("owner_ua.USER_ID AS STAGE_OWNER_NAME, ");
        sql.append("tm.STAGE_CODE, tm.DESCRIPTION AS STAGE_NAME, ");
        sql.append("(SELECT COUNT(*) FROM REPLACEMENT_PRINTER_DETAILS pd ");
        sql.append(" WHERE pd.REPLACEMENT_REQUEST_ID = r.ID) AS PRINTER_COUNT, ");
        sql.append("fet.END_AT AS AM_MANAGER_COMPLETED_AT ");
        sql.append("FROM REPLACEMENT_REQUEST r ");
        sql.append("INNER JOIN RPLCE_FLOW_EVENT_TRACKING fet ON fet.REPLACEMENT_REQUEST_ID = r.ID ");
        sql.append("LEFT JOIN CLIENT c ON c.ID = r.CLIENT_DOT_ID_SIGNING ");
        sql.append("LEFT JOIN USER_ACCOUNT req_ua ON req_ua.ID = r.REQUESTER_USER_ID ");
        sql.append("LEFT JOIN USER_ACCOUNT owner_ua ON owner_ua.ID = r.CURRENT_OWNER_ID ");
        sql.append("LEFT JOIN TAT_MASTER tm ON tm.ID = r.CURRENT_STAGE ");
        sql.append("INNER JOIN TAT_MASTER fet_tm ON fet_tm.ID = fet.CURRENT_STAGE_ID ");
        sql.append("WHERE fet_tm.STAGE_CODE = 'STG3_AM_MANAGER_REVIEW' ");
        sql.append("AND fet.END_AT IS NOT NULL ");
        sql.append("AND r.STATUS NOT IN ('REJECTED', 'CLOSED','CANCELLED','COMPLETED') ");

        final List<Object> params = new ArrayList<>();

        if (dateFrom != null && !dateFrom.isEmpty()) {
            sql.append("AND r.CREATION_DATE_TIME >= TO_DATE(?, 'YYYY-MM-DD') ");
            params.add(dateFrom);
        }
        if (dateTo != null && !dateTo.isEmpty()) {
            sql.append("AND r.CREATION_DATE_TIME <= TO_DATE(?, 'YYYY-MM-DD') + 1 ");
            params.add(dateTo);
        }
        if (requester != null && !requester.isEmpty()) {
            sql.append("AND req_ua.USER_ID = ? ");
            params.add(requester);
        }
        if (am != null && !am.isEmpty()) {
            sql.append("AND EXISTS (SELECT 1 FROM CLIENT_ACCESS ca JOIN USER_ACCOUNT sign_ua ON sign_ua.ID = ca.USER_ID WHERE ca.CLIENT_ID = r.CLIENT_DOT_ID_SIGNING AND sign_ua.USER_ID = ?) ");
            params.add(am);
        }

        sql.append("ORDER BY fet.END_AT DESC");

        try (final Connection con = getConnection();
             final PreparedStatement ps = con.prepareStatement(sql.toString())) {

            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }

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
     * Get client name by request ID (for default comments)
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
