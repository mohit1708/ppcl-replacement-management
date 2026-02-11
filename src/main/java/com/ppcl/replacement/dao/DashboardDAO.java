package com.ppcl.replacement.dao;

import com.ppcl.replacement.model.*;
import com.ppcl.replacement.model.DashboardSummary.CategorySummary;
import com.ppcl.replacement.model.DashboardSummary.DepartmentSummary;
import com.ppcl.replacement.model.DashboardSummary.OwnerSummary;
import com.ppcl.replacement.model.DashboardSummary.StageSummary;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * DAO for Dashboard operations
 * Handles all dashboard KPIs, summaries, and detailed request queries
 * Uses REPLACEMENT_REQUEST as the primary table
 */
public class DashboardDAO extends BaseDAO {

    // TAT is stored by the scheduler in RPLCE_FLOW_EVENT_TRACKING.TAT_PERCENTAGE
    // We just read it directly from the database - no calculation needed.

    // =====================================================
    // KPI COUNT METHODS
    // =====================================================

    /**
     * Get all KPI counts in a single query (optimized - reduces DB round trips)
     * Returns: [totalCount, pendingCount, closedCount]
     */
    public int[] getAllKpiCounts(DashboardFilters filters) throws Exception {
        int[] counts = new int[3]; // total, pending, closed

        StringBuilder sql = new StringBuilder();
        sql.append("SELECT ");
        sql.append("  COUNT(*) AS TOTAL_COUNT, ");
        sql.append("  SUM(CASE WHEN RR.STATUS IN ('PENDING', 'OPEN') THEN 1 ELSE 0 END) AS PENDING_COUNT, ");
        sql.append("  SUM(CASE WHEN RR.STATUS = 'COMPLETED' THEN 1 ELSE 0 END) AS CLOSED_COUNT ");
        sql.append("FROM REPLACEMENT_REQUEST RR ");

        // Add JOINs if department filter is needed
        if (filters.getDepartmentId() != null) {
            sql.append("JOIN USER_ACCOUNT UA ON RR.CURRENT_OWNER_ID = UA.ID ");
        }

        sql.append("WHERE 1=1 ");

        List<Object> params = new ArrayList<>();
        appendDateFilter(sql, params, filters);
        appendCommonFilters(sql, params, filters);

        // Department filter
        if (filters.getDepartmentId() != null) {
            sql.append("AND UA.DEPT_ID = ? ");
            params.add(filters.getDepartmentId());
        }

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {

            setParameters(ps, params);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                counts[0] = rs.getInt("TOTAL_COUNT");
                counts[1] = rs.getInt("PENDING_COUNT");
                counts[2] = rs.getInt("CLOSED_COUNT");
            }
        }

        return counts;
    }

    /**
     * Get total count of replacement requests with filters
     */
    public int getTotalCount(DashboardFilters filters) throws Exception {
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT COUNT(*) FROM REPLACEMENT_REQUEST RR WHERE 1=1 ");

        List<Object> params = new ArrayList<>();
        appendDateFilter(sql, params, filters);
        appendCommonFilters(sql, params, filters);

        return executeCountQuery(sql.toString(), params);
    }

    /**
     * Get pending requests count (STATUS='PENDING' or 'OPEN')
     */
    public int getPendingCount(DashboardFilters filters) throws Exception {
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT COUNT(*) FROM REPLACEMENT_REQUEST RR WHERE RR.STATUS IN ('PENDING', 'OPEN') ");

        List<Object> params = new ArrayList<>();
        appendDateFilter(sql, params, filters);
        appendCommonFilters(sql, params, filters);

        return executeCountQuery(sql.toString(), params);
    }

    /**
     * Get closed/completed requests count
     */
    public int getClosedCount(DashboardFilters filters) throws Exception {
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT COUNT(*) FROM REPLACEMENT_REQUEST RR WHERE RR.STATUS = 'COMPLETED' ");

        List<Object> params = new ArrayList<>();
        appendDateFilter(sql, params, filters);
        appendCommonFilters(sql, params, filters);

        return executeCountQuery(sql.toString(), params);
    }

    /**
     * Get TAT breach count (TAT_PERCENTAGE >= 100 from current stage event tracking)
     */
    public int getTatBreachCount(DashboardFilters filters) throws Exception {
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT COUNT(*) FROM REPLACEMENT_REQUEST RR ");
        sql.append("JOIN RPLCE_FLOW_EVENT_TRACKING FET_CURR ON FET_CURR.REPLACEMENT_REQUEST_ID = RR.ID ");
        sql.append("  AND FET_CURR.CURRENT_STAGE_ID = RR.CURRENT_STAGE ");
        sql.append("  AND FET_CURR.ID = (SELECT MAX(FET2.ID) FROM RPLCE_FLOW_EVENT_TRACKING FET2 WHERE FET2.REPLACEMENT_REQUEST_ID = RR.ID AND FET2.CURRENT_STAGE_ID = RR.CURRENT_STAGE) ");

        // Add JOINs if department filter is needed
        if (filters.getDepartmentId() != null) {
            sql.append("JOIN USER_ACCOUNT UA ON RR.CURRENT_OWNER_ID = UA.ID ");
        }

        sql.append("WHERE NVL(FET_CURR.TAT_PERCENTAGE, 0) >= 100 ");

        List<Object> params = new ArrayList<>();
        appendDateFilter(sql, params, filters);
        appendCommonFilters(sql, params, filters);

        // Department filter
        if (filters.getDepartmentId() != null) {
            sql.append("AND UA.DEPT_ID = ? ");
            params.add(filters.getDepartmentId());
        }

        return executeCountQuery(sql.toString(), params);
    }

    /**
     * Get pending counts by stage breakdown
     */
    public Map<String, Integer> getPendingCountsByStage(DashboardFilters filters) throws Exception {
        Map<String, Integer> counts = new HashMap<>();

        StringBuilder sql = new StringBuilder();
        sql.append("SELECT TM.STAGE_CODE, COUNT(*) AS CNT FROM REPLACEMENT_REQUEST RR ");
        sql.append("JOIN TAT_MASTER TM ON RR.CURRENT_STAGE = TM.ID ");
        sql.append("WHERE RR.STATUS IN ('PENDING', 'OPEN') ");

        List<Object> params = new ArrayList<>();
        appendDateFilter(sql, params, filters);
        appendCommonFilters(sql, params, filters);
        sql.append("GROUP BY TM.STAGE_CODE");

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {

            setParameters(ps, params);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                counts.put(rs.getString("STAGE_CODE"), rs.getInt("CNT"));
            }
        }

        return counts;
    }

    // =====================================================
    // DETAILED REQUEST LIST METHODS
    // =====================================================

    /**
     * Get paginated replacement requests with all details
     */
    public List<DashboardRequest> getReplacementRequests(DashboardFilters filters) throws Exception {
        List<DashboardRequest> list = new ArrayList<>();

        String sql = "SELECT * FROM ( " +
                "  SELECT ROWNUM AS RN, A.* FROM ( " +
                buildMainQuery(filters) +
                "  ) A WHERE ROWNUM <= ? " +
                ") WHERE RN > ? ";

        List<Object> params = buildQueryParams(filters);
        params.add(filters.getOffset() + filters.getPageSize()); // Upper limit
        params.add(filters.getOffset()); // Offset

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            setParameters(ps, params);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                list.add(mapDashboardRequest(rs));
            }
        }

        return list;
    }

    /**
     * Build main query for replacement requests
     */
    private String buildMainQuery(DashboardFilters filters) {
        StringBuilder sql = new StringBuilder();

        sql.append("SELECT ");
        sql.append("  RR.ID, RR.STATUS, RR.SOURCE, RR.SERVICE_CALL_ID, ");
        sql.append("  RR.REPLACEMENT_REASON_ID, RR.REPLACEMENT_TYPE, ");
        sql.append("  RR.CREATION_DATE_TIME, RR.UPDATE_DATE_TIME, ");

        // Client info
        sql.append("  RR.CLIENT_DOT_ID_SIGNING, C.NAME AS CLIENT_NAME, C.CITY AS CLIENT_CITY, C.BRANCH AS CLIENT_BRANCH, ");

        // Current owner info
        sql.append("  RR.CURRENT_OWNER_ID, UA_OWNER.USER_ID AS OWNER_USER_ID, ");
        sql.append("  E_OWNER.NAME AS OWNER_NAME, D.NAME AS OWNER_DEPARTMENT, ");

        // Current stage info
        sql.append("  RR.CURRENT_STAGE, TM.STAGE_CODE, TM.DESCRIPTION AS STAGE_DESCRIPTION, ");

        // Requester info
        sql.append("  RR.REQUESTER_USER_ID, UA_REQ.USER_ID AS REQUESTER_USER_ID_STR, ");
        sql.append("  E_REQ.NAME AS REQUESTER_NAME, ");

        // Account Manager (conditional: if SERVICE_CALL_ID present, use COORDINATOR from CLIENT_REQUEST -> USER_ACCOUNT; else use Requester)
        sql.append("  CASE WHEN RR.SERVICE_CALL_ID IS NOT NULL THEN UA_AM.USER_ID ");
        sql.append("       ELSE UA_REQ.USER_ID END AS ACCOUNT_MANAGER_USER_ID, ");
        sql.append("  CASE WHEN RR.SERVICE_CALL_ID IS NOT NULL THEN NVL(E_AM.NAME, UA_AM.USER_ID) ");
        sql.append("       ELSE NVL(E_REQ.NAME, UA_REQ.USER_ID) END AS ACCOUNT_MANAGER_NAME, ");

        // Replacement reason
        sql.append("  RRSN.NAME AS REASON_NAME, ");

        // TAT info - directly from event tracking (populated by scheduler)
        sql.append("  NVL(FET_CURR.TAT_PERCENTAGE, 0) AS TAT_PERCENTAGE, ");

        // Printer count
        sql.append("  (SELECT COUNT(*) FROM REPLACEMENT_PRINTER_DETAILS RPD WHERE RPD.REPLACEMENT_REQUEST_ID = RR.ID) AS PRINTER_COUNT ");

        // FROM clause with JOINs
        sql.append("FROM REPLACEMENT_REQUEST RR ");

        // Client join
        sql.append("LEFT JOIN CLIENT C ON RR.CLIENT_DOT_ID_SIGNING = C.ID ");

        // Current owner join chain: USER_ACCOUNT -> EMP -> DEPARTMENT
        sql.append("LEFT JOIN USER_ACCOUNT UA_OWNER ON RR.CURRENT_OWNER_ID = UA_OWNER.ID ");
        sql.append("LEFT JOIN EMP E_OWNER ON UA_OWNER.EMP_ID = E_OWNER.ID ");
        sql.append("LEFT JOIN DEPARTMENT D ON E_OWNER.DEPARTMENT = D.ID ");

        // Stage join
        sql.append("LEFT JOIN TAT_MASTER TM ON RR.CURRENT_STAGE = TM.ID ");

        // Requester join
        sql.append("LEFT JOIN USER_ACCOUNT UA_REQ ON RR.REQUESTER_USER_ID = UA_REQ.ID ");
        sql.append("LEFT JOIN EMP E_REQ ON UA_REQ.EMP_ID = E_REQ.ID ");

        // Service call join for Account Manager (COORDINATOR -> USER_ACCOUNT.ID -> USER_ID)
        sql.append("LEFT JOIN CLIENT_REQUEST CR ON RR.SERVICE_CALL_ID = CR.CALL_ID ");
        sql.append("LEFT JOIN USER_ACCOUNT UA_AM ON CR.COORDINATOR = UA_AM.ID ");
        sql.append("LEFT JOIN EMP E_AM ON UA_AM.EMP_ID = E_AM.ID ");

        // Replacement reason join
        sql.append("LEFT JOIN REPLACEMENT_REASON RRSN ON RR.REPLACEMENT_REASON_ID = RRSN.ID ");

        // TAT join - get latest event tracking row for the request's current stage
        sql.append("LEFT JOIN RPLCE_FLOW_EVENT_TRACKING FET_CURR ON FET_CURR.REPLACEMENT_REQUEST_ID = RR.ID ");
        sql.append("  AND FET_CURR.CURRENT_STAGE_ID = RR.CURRENT_STAGE ");
        sql.append("  AND FET_CURR.ID = (SELECT MAX(FET2.ID) FROM RPLCE_FLOW_EVENT_TRACKING FET2 WHERE FET2.REPLACEMENT_REQUEST_ID = RR.ID AND FET2.CURRENT_STAGE_ID = RR.CURRENT_STAGE) ");

        // WHERE clause
        sql.append("WHERE 1=1 ");

        // Add filter conditions
        if (filters.getFromDate() != null && !filters.getFromDate().isEmpty()) {
            sql.append("AND RR.CREATION_DATE_TIME >= TO_TIMESTAMP(?, 'YYYY-MM-DD HH24:MI:SS') ");
        }
        if (filters.getToDate() != null && !filters.getToDate().isEmpty()) {
            sql.append("AND RR.CREATION_DATE_TIME < TO_TIMESTAMP(?, 'YYYY-MM-DD HH24:MI:SS') + INTERVAL '1' DAY ");
        }

        // View-specific filters
        if (filters.getView() != null) {
            switch (filters.getView()) {
                case "pending":
                    sql.append("AND RR.STATUS IN ('PENDING', 'OPEN') ");
                    break;
                case "tat-breach":
                    sql.append("AND NVL(FET_CURR.TAT_PERCENTAGE, 0) >= 100 ");
                    break;
                case "closed":
                    sql.append("AND RR.STATUS = 'COMPLETED' ");
                    break;
            }
        }

        // Additional filters
        if (filters.getRequesterId() != null) {
            sql.append("AND RR.REQUESTER_USER_ID = ? ");
        }
        if (filters.getAccountManagerId() != null) {
            // buildMainQuery already has LEFT JOIN CLIENT_REQUEST CR, so use CR.COORDINATOR directly
            sql.append("AND ((RR.SERVICE_CALL_ID IS NOT NULL AND CR.COORDINATOR = ?) ");
            sql.append(" OR (RR.SERVICE_CALL_ID IS NULL AND RR.REQUESTER_USER_ID = ?)) ");
        }
        if (filters.getCurrentOwnerId() != null) {
            sql.append("AND RR.CURRENT_OWNER_ID = ? ");
        }
        if (filters.getStageId() != null) {
            sql.append("AND RR.CURRENT_STAGE = ? ");
        }
        if (filters.getStatus() != null && !filters.getStatus().isEmpty()) {
            sql.append("AND RR.STATUS = ? ");
        }
        if (filters.getTatFilter() != null) {
            if ("within".equalsIgnoreCase(filters.getTatFilter())) {
                sql.append("AND NVL(FET_CURR.TAT_PERCENTAGE, 0) < 100 ");
            } else if ("beyond".equalsIgnoreCase(filters.getTatFilter())) {
                sql.append("AND NVL(FET_CURR.TAT_PERCENTAGE, 0) >= 100 ");
            }
        }
        if (filters.getDepartmentId() != null) {
            sql.append("AND E_OWNER.DEPARTMENT = ? ");
        }
        if (filters.getCategoryId() != null) {
            sql.append("AND RR.REPLACEMENT_REASON_ID = ? ");
        }

        // ORDER BY
        sql.append("ORDER BY ");
        sql.append(sanitizeSortColumn(filters.getSortColumn()));
        sql.append(" ");
        sql.append("DESC".equalsIgnoreCase(filters.getSortDirection()) ? "DESC" : "ASC");

        return sql.toString();
    }

    /**
     * Build query parameters list
     */
    private List<Object> buildQueryParams(DashboardFilters filters) {
        List<Object> params = new ArrayList<>();

        if (filters.getFromDate() != null && !filters.getFromDate().isEmpty()) {
            params.add(filters.getFromDate() + " 00:00:00");
        }
        if (filters.getToDate() != null && !filters.getToDate().isEmpty()) {
            params.add(filters.getToDate() + " 00:00:00");
        }
        if (filters.getRequesterId() != null) {
            params.add(filters.getRequesterId());
        }
        if (filters.getAccountManagerId() != null) {
            params.add(filters.getAccountManagerId()); // for CR.COORDINATOR = ?
            params.add(filters.getAccountManagerId()); // for RR.REQUESTER_USER_ID = ?
        }
        if (filters.getCurrentOwnerId() != null) {
            params.add(filters.getCurrentOwnerId());
        }
        if (filters.getStageId() != null) {
            params.add(filters.getStageId());
        }
        if (filters.getStatus() != null && !filters.getStatus().isEmpty()) {
            params.add(filters.getStatus());
        }
        if (filters.getDepartmentId() != null) {
            params.add(filters.getDepartmentId());
        }
        if (filters.getCategoryId() != null) {
            params.add(filters.getCategoryId());
        }

        return params;
    }

    /**
     * Map ResultSet to DashboardRequest object
     */
    private DashboardRequest mapDashboardRequest(ResultSet rs) throws SQLException {
        DashboardRequest req = new DashboardRequest();

        req.setId(rs.getInt("ID"));
        req.setStatus(rs.getString("STATUS"));
        req.setSource(rs.getString("SOURCE"));
        req.setServiceCallId(rs.getObject("SERVICE_CALL_ID") != null ? rs.getInt("SERVICE_CALL_ID") : null);
        req.setReasonId(rs.getInt("REPLACEMENT_REASON_ID"));
        req.setReplacementType(rs.getString("REPLACEMENT_TYPE"));
        req.setCreationDateTime(rs.getTimestamp("CREATION_DATE_TIME"));
        req.setUpdateDateTime(rs.getTimestamp("UPDATE_DATE_TIME"));

        // Client info
        req.setClientId(rs.getInt("CLIENT_DOT_ID_SIGNING"));
        req.setClientName(rs.getString("CLIENT_NAME"));
        req.setClientCity(rs.getString("CLIENT_CITY"));
        req.setClientBranch(rs.getString("CLIENT_BRANCH"));

        // Owner info
        req.setCurrentOwnerId(rs.getInt("CURRENT_OWNER_ID"));
        req.setCurrentOwnerUserId(rs.getString("OWNER_USER_ID"));
        req.setCurrentOwnerName(rs.getString("OWNER_NAME"));
        req.setCurrentOwnerDepartment(rs.getString("OWNER_DEPARTMENT"));

        // Stage info
        req.setCurrentStageId(rs.getInt("CURRENT_STAGE"));
        req.setCurrentStageCode(rs.getString("STAGE_CODE"));
        req.setCurrentStageDescription(rs.getString("STAGE_DESCRIPTION"));

        // Requester info
        req.setRequesterUserId(rs.getInt("REQUESTER_USER_ID"));
        req.setRequesterUserIdStr(rs.getString("REQUESTER_USER_ID_STR"));
        req.setRequesterName(rs.getString("REQUESTER_NAME"));

        // Account Manager
        req.setAccountManagerUserId(rs.getString("ACCOUNT_MANAGER_USER_ID"));
        req.setAccountManagerName(rs.getString("ACCOUNT_MANAGER_NAME"));

        // Reason
        req.setReasonName(rs.getString("REASON_NAME"));

        // TAT
        BigDecimal tatPct = rs.getBigDecimal("TAT_PERCENTAGE");
        req.setTatPercentage(tatPct);

        // Printer count
        req.setPrinterCount(rs.getInt("PRINTER_COUNT"));

        return req;
    }

    // =====================================================
    // AGGREGATED VIEW METHODS
    // =====================================================

    /**
     * Get stage-wise summary with TAT breakdown
     * Uses RPLCE_FLOW_EVENT_TRACKING for per-stage TAT (not just current stage)
     * - Within TAT: TAT_PERCENTAGE IS NULL OR TAT_PERCENTAGE < 100
     * - Beyond TAT: TAT_PERCENTAGE >= 100
     */
    public List<StageSummary> getStageSummary(DashboardFilters filters) throws Exception {
        List<StageSummary> list = new ArrayList<>();

        StringBuilder sql = new StringBuilder();
        sql.append("SELECT TM.ID AS STAGE_ID, TM.STAGE_CODE, TM.DESCRIPTION AS STAGE_DESC, ");
        sql.append("  SUM(CASE WHEN NVL(FET.TAT_PERCENTAGE, 0) < 100 THEN 1 ELSE 0 END) AS WITHIN_TAT, ");
        sql.append("  SUM(CASE WHEN NVL(FET.TAT_PERCENTAGE, 0) >= 100 THEN 1 ELSE 0 END) AS BEYOND_TAT, ");
        sql.append("  COUNT(*) AS TOTAL ");
        sql.append("FROM RPLCE_FLOW_EVENT_TRACKING FET ");
        sql.append("JOIN REPLACEMENT_REQUEST RR ON FET.REPLACEMENT_REQUEST_ID = RR.ID ");
        sql.append("JOIN TAT_MASTER TM ON FET.CURRENT_STAGE_ID = TM.ID ");
        sql.append("WHERE FET.CURRENT_STAGE_ID IS NOT NULL ");

        List<Object> params = new ArrayList<>();
        appendDateFilterForEventTracking(sql, params, filters);
        sql.append("GROUP BY TM.ID, TM.STAGE_CODE, TM.DESCRIPTION ");
        sql.append("ORDER BY TM.ID");

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {

            setParameters(ps, params);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                StageSummary ss = new StageSummary();
                ss.setStageId(rs.getInt("STAGE_ID"));
                ss.setStageCode(rs.getString("STAGE_CODE"));
                ss.setStageDescription(rs.getString("STAGE_DESC"));
                ss.setWithinTatCount(rs.getInt("WITHIN_TAT"));
                ss.setBeyondTatCount(rs.getInt("BEYOND_TAT"));
                ss.setTotalCount(rs.getInt("TOTAL"));
                list.add(ss);
            }
        }

        return list;
    }

    /**
     * Get Current Stage Wise summary for main dashboard
     * Groups REPLACEMENT_REQUEST by CURRENT_STAGE, TAT from current active event tracking (END_AT IS NULL)
     * - Within TAT: TAT_PERCENTAGE IS NULL OR TAT_PERCENTAGE < 100
     * - Beyond TAT: TAT_PERCENTAGE >= 100
     */
    public List<StageSummary> getCurrentStageSummary(DashboardFilters filters) throws Exception {
        List<StageSummary> list = new ArrayList<>();

        StringBuilder sql = new StringBuilder();
        sql.append("SELECT TM.ID AS STAGE_ID, TM.STAGE_CODE, TM.DESCRIPTION AS STAGE_DESC, ");
        sql.append("  SUM(CASE WHEN NVL(FET_CURR.TAT_PERCENTAGE, 0) < 100 THEN 1 ELSE 0 END) AS WITHIN_TAT, ");
        sql.append("  SUM(CASE WHEN NVL(FET_CURR.TAT_PERCENTAGE, 0) >= 100 THEN 1 ELSE 0 END) AS BEYOND_TAT, ");
        sql.append("  COUNT(*) AS TOTAL ");
        sql.append("FROM REPLACEMENT_REQUEST RR ");
        sql.append("JOIN TAT_MASTER TM ON RR.CURRENT_STAGE = TM.ID ");
        sql.append("LEFT JOIN RPLCE_FLOW_EVENT_TRACKING FET_CURR ON FET_CURR.REPLACEMENT_REQUEST_ID = RR.ID ");
        sql.append("  AND FET_CURR.CURRENT_STAGE_ID = RR.CURRENT_STAGE ");
        sql.append("  AND FET_CURR.ID = (SELECT MAX(FET2.ID) FROM RPLCE_FLOW_EVENT_TRACKING FET2 WHERE FET2.REPLACEMENT_REQUEST_ID = RR.ID AND FET2.CURRENT_STAGE_ID = RR.CURRENT_STAGE) ");
        sql.append("WHERE RR.CURRENT_STAGE IS NOT NULL ");

        List<Object> params = new ArrayList<>();
        appendDateFilter(sql, params, filters);
        appendCommonFilters(sql, params, filters);
        sql.append("GROUP BY TM.ID, TM.STAGE_CODE, TM.DESCRIPTION ");
        sql.append("ORDER BY TM.ID");

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {

            setParameters(ps, params);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                StageSummary ss = new StageSummary();
                ss.setStageId(rs.getInt("STAGE_ID"));
                ss.setStageCode(rs.getString("STAGE_CODE"));
                ss.setStageDescription(rs.getString("STAGE_DESC"));
                ss.setWithinTatCount(rs.getInt("WITHIN_TAT"));
                ss.setBeyondTatCount(rs.getInt("BEYOND_TAT"));
                ss.setTotalCount(rs.getInt("TOTAL"));
                list.add(ss);
            }
        }

        return list;
    }

    /**
     * Get owner-wise summary with TAT breakdown
     * Uses RPLCE_FLOW_EVENT_TRACKING for per-owner TAT (not just current owner)
     * - Within TAT: TAT_PERCENTAGE IS NULL OR TAT_PERCENTAGE < 100
     * - Beyond TAT: TAT_PERCENTAGE >= 100
     */
    public List<OwnerSummary> getOwnerSummary(DashboardFilters filters) throws Exception {
        List<OwnerSummary> list = new ArrayList<>();

        StringBuilder sql = new StringBuilder();
        sql.append("SELECT UA.ID AS OWNER_ID, UA.USER_ID AS OWNER_USER_ID, ");
        sql.append("  NVL(E.NAME, UA.USER_ID) AS OWNER_NAME, ");
        sql.append("  SUM(CASE WHEN NVL(FET.TAT_PERCENTAGE, 0) < 100 THEN 1 ELSE 0 END) AS WITHIN_TAT, ");
        sql.append("  SUM(CASE WHEN NVL(FET.TAT_PERCENTAGE, 0) >= 100 THEN 1 ELSE 0 END) AS BEYOND_TAT, ");
        sql.append("  COUNT(*) AS TOTAL ");
        sql.append("FROM RPLCE_FLOW_EVENT_TRACKING FET ");
        sql.append("JOIN REPLACEMENT_REQUEST RR ON FET.REPLACEMENT_REQUEST_ID = RR.ID ");
        sql.append("JOIN TAT_MASTER TM ON FET.CURRENT_STAGE_ID = TM.ID ");
        sql.append("JOIN USER_ACCOUNT UA ON FET.CURRENT_OWNER_USER_ID = UA.ID ");
        sql.append("LEFT JOIN EMP E ON UA.EMP_ID = E.ID ");
        sql.append("WHERE FET.CURRENT_OWNER_USER_ID IS NOT NULL ");

        List<Object> params = new ArrayList<>();
        appendDateFilterForEventTracking(sql, params, filters);
        sql.append("GROUP BY UA.ID, UA.USER_ID, E.NAME ");
        sql.append("ORDER BY TOTAL DESC");

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {

            setParameters(ps, params);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                OwnerSummary os = new OwnerSummary();
                os.setOwnerId(rs.getInt("OWNER_ID"));
                os.setOwnerUserId(rs.getString("OWNER_USER_ID"));
                os.setOwnerName(rs.getString("OWNER_NAME"));
                os.setWithinTatCount(rs.getInt("WITHIN_TAT"));
                os.setBeyondTatCount(rs.getInt("BEYOND_TAT"));
                os.setTotalCount(rs.getInt("TOTAL"));
                list.add(os);
            }
        }

        return list;
    }

    /**
     * Get department-wise summary with TAT breakdown
     * Uses RPLCE_FLOW_EVENT_TRACKING for per-department TAT (not just current owner's dept)
     * Joins: FET -> USER_ACCOUNT (owner) -> EMP -> DEPARTMENT
     * - Within TAT: TAT_PERCENTAGE IS NULL OR TAT_PERCENTAGE < 100
     * - Beyond TAT: TAT_PERCENTAGE >= 100
     */
    public List<DepartmentSummary> getDepartmentSummary(DashboardFilters filters) throws Exception {
        List<DepartmentSummary> list = new ArrayList<>();

        StringBuilder sql = new StringBuilder();
        sql.append("SELECT NVL(UA.DEPT_ID, 0) AS DEPT_ID, NVL(UA.DEPT, 'Unknown') AS DEPT_NAME, ");
        sql.append("  SUM(CASE WHEN NVL(FET.TAT_PERCENTAGE, 0) < 100 THEN 1 ELSE 0 END) AS WITHIN_TAT, ");
        sql.append("  SUM(CASE WHEN NVL(FET.TAT_PERCENTAGE, 0) >= 100 THEN 1 ELSE 0 END) AS BEYOND_TAT, ");
        sql.append("  COUNT(*) AS TOTAL ");
        sql.append("FROM RPLCE_FLOW_EVENT_TRACKING FET ");
        sql.append("JOIN REPLACEMENT_REQUEST RR ON FET.REPLACEMENT_REQUEST_ID = RR.ID ");
        sql.append("JOIN TAT_MASTER TM ON FET.CURRENT_STAGE_ID = TM.ID ");
        sql.append("JOIN USER_ACCOUNT UA ON FET.CURRENT_OWNER_USER_ID = UA.ID ");
        sql.append("WHERE FET.CURRENT_OWNER_USER_ID IS NOT NULL ");

        List<Object> params = new ArrayList<>();
        appendDateFilterForEventTracking(sql, params, filters);
        sql.append("GROUP BY UA.DEPT_ID, UA.DEPT ");
        sql.append("ORDER BY TOTAL DESC");

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {

            setParameters(ps, params);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                DepartmentSummary ds = new DepartmentSummary();
                ds.setDepartmentId(rs.getInt("DEPT_ID"));
                ds.setDepartmentName(rs.getString("DEPT_NAME"));
                ds.setWithinTatCount(rs.getInt("WITHIN_TAT"));
                ds.setBeyondTatCount(rs.getInt("BEYOND_TAT"));
                ds.setTotalCount(rs.getInt("TOTAL"));
                list.add(ds);
            }
        }

        return list;
    }

    /**
     * Get category (replacement reason) summary for pie chart
     */
    public List<CategorySummary> getCategorySummary(DashboardFilters filters) throws Exception {
        List<CategorySummary> list = new ArrayList<>();
        int totalCount = 0;

        StringBuilder sql = new StringBuilder();
        sql.append("SELECT RR_RSN.ID AS CATEGORY_ID, RR_RSN.NAME AS CATEGORY_NAME, ");
        sql.append("  COUNT(*) AS CNT ");
        sql.append("FROM REPLACEMENT_REQUEST RR ");
        sql.append("JOIN REPLACEMENT_REASON RR_RSN ON RR.REPLACEMENT_REASON_ID = RR_RSN.ID ");
        sql.append("WHERE 1=1 ");

        List<Object> params = new ArrayList<>();
        appendDateFilter(sql, params, filters);
        sql.append("GROUP BY RR_RSN.ID, RR_RSN.NAME ");
        sql.append("ORDER BY CNT DESC");

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {

            setParameters(ps, params);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                CategorySummary cs = new CategorySummary();
                cs.setCategoryId(rs.getInt("CATEGORY_ID"));
                cs.setCategoryName(rs.getString("CATEGORY_NAME"));
                cs.setCount(rs.getInt("CNT"));
                totalCount += cs.getCount();
                list.add(cs);
            }
        }

        // Calculate percentages
        for (CategorySummary cs : list) {
            if (totalCount > 0) {
                BigDecimal pct = BigDecimal.valueOf(cs.getCount() * 100.0 / totalCount)
                        .setScale(1, RoundingMode.HALF_UP);
                cs.setPercentage(pct);
            } else {
                cs.setPercentage(BigDecimal.ZERO);
            }
        }

        return list;
    }

    // =====================================================
    // PRINTER DETAILS POPUP
    // =====================================================

    /**
     * Get printer details for a specific replacement request
     */
    public List<PrinterDetailRow> getPrinterDetails(int replacementRequestId) throws Exception {
        List<PrinterDetailRow> list = new ArrayList<>();

        String sql = "SELECT RPD.ID, RPD.REPLACEMENT_REQUEST_ID, " +
                "  RPD.EXISTING_P_MODEL_ID, PM.MODEL_NAME AS EXISTING_MODEL_NAME, " +
                "  RPD.EXISTING_SERIAL, " +
                "  RPD.NEW_P_MODEL_SELECTED_ID, " +
                "  NVL(PM_NEW.MODEL_NAME, RPD.NEW_P_MODEL_SELECTED_TEXT) AS NEW_MODEL_TEXT, " +
                "  RPD.NEW_P_MODEL_SOURCE, " +
                "  RPD.CLIENT_DOT_ID, C.CITY AS CLIENT_CITY, C.BRANCH AS CLIENT_BRANCH, " +
                "  RPD.CONTACT_PERSON_NAME, RPD.CONTACT_PERSON_NUMBER, RPD.CONTACT_PERSON_EMAIL, " +
                "  RPD.RECOMMENDED_COMMENTS " +
                "FROM REPLACEMENT_PRINTER_DETAILS RPD " +
                "LEFT JOIN P_MODEL PM ON RPD.EXISTING_P_MODEL_ID = PM.ID " +
                "LEFT JOIN P_MODEL PM_NEW ON RPD.NEW_P_MODEL_SELECTED_ID = PM_NEW.ID " +
                "LEFT JOIN CLIENT C ON RPD.CLIENT_DOT_ID = C.ID " +
                "WHERE RPD.REPLACEMENT_REQUEST_ID = ? " +
                "ORDER BY RPD.ID";

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, replacementRequestId);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                PrinterDetailRow row = new PrinterDetailRow();
                row.setId(rs.getInt("ID"));
                row.setReplacementRequestId(rs.getInt("REPLACEMENT_REQUEST_ID"));
                row.setExistingModelId(rs.getInt("EXISTING_P_MODEL_ID"));
                row.setExistingModelName(rs.getString("EXISTING_MODEL_NAME"));
                row.setExistingSerial(rs.getString("EXISTING_SERIAL"));
                row.setNewModelSelectedId(rs.getObject("NEW_P_MODEL_SELECTED_ID") != null ?
                        rs.getInt("NEW_P_MODEL_SELECTED_ID") : null);
                row.setNewModelSelectedText(rs.getString("NEW_MODEL_TEXT"));
                row.setNewModelSource(rs.getString("NEW_P_MODEL_SOURCE"));
                row.setClientDotId(rs.getInt("CLIENT_DOT_ID"));
                row.setClientCity(rs.getString("CLIENT_CITY"));
                row.setClientBranch(rs.getString("CLIENT_BRANCH"));
                row.setContactPersonName(rs.getString("CONTACT_PERSON_NAME"));
                row.setContactPersonNumber(rs.getString("CONTACT_PERSON_NUMBER"));
                row.setContactPersonEmail(rs.getString("CONTACT_PERSON_EMAIL"));
                row.setRecommendedComments(rs.getString("RECOMMENDED_COMMENTS"));
                list.add(row);
            }
        }

        return list;
    }

    // =====================================================
    // DROPDOWN DATA METHODS
    // =====================================================

    /**
     * Get all requesters for filter dropdown
     */
    public List<User> getAllRequesters(DashboardFilters filters) throws Exception {
        List<User> list = new ArrayList<>();

        StringBuilder sql = new StringBuilder();
        sql.append("SELECT DISTINCT UA.ID, UA.USER_ID, NVL(E.NAME, UA.USER_ID) AS NAME ");
        sql.append("FROM REPLACEMENT_REQUEST RR ");
        sql.append("JOIN USER_ACCOUNT UA ON RR.REQUESTER_USER_ID = UA.ID ");
        sql.append("LEFT JOIN EMP E ON UA.EMP_ID = E.ID ");
        sql.append("WHERE 1=1 ");

        List<Object> params = new ArrayList<>();
        appendDateFilter(sql, params, filters);
        sql.append("ORDER BY NAME");

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {

            setParameters(ps, params);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                User user = new User();
                user.setId(rs.getInt("ID"));
                user.setUserId(rs.getString("USER_ID"));
                user.setName(rs.getString("NAME"));
                list.add(user);
            }
        }

        return list;
    }

    /**
     * Get all current owners for filter dropdown
     */
    public List<User> getAllOwners(DashboardFilters filters) throws Exception {
        List<User> list = new ArrayList<>();

        StringBuilder sql = new StringBuilder();
        sql.append("SELECT DISTINCT UA.ID, UA.USER_ID, NVL(E.NAME, UA.USER_ID) AS NAME ");
        sql.append("FROM REPLACEMENT_REQUEST RR ");
        sql.append("JOIN USER_ACCOUNT UA ON RR.CURRENT_OWNER_ID = UA.ID ");
        sql.append("LEFT JOIN EMP E ON UA.EMP_ID = E.ID ");
        sql.append("WHERE 1=1 ");

        List<Object> params = new ArrayList<>();
        appendDateFilter(sql, params, filters);
        sql.append("ORDER BY NAME");

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {

            setParameters(ps, params);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                User user = new User();
                user.setId(rs.getInt("ID"));
                user.setUserId(rs.getString("USER_ID"));
                user.setName(rs.getString("NAME"));
                list.add(user);
            }
        }

        return list;
    }

    /**
     * Get all account managers for filter dropdown.
     * Account Manager = COORDINATOR from CLIENT_REQUEST (when SERVICE_CALL_ID is set),
     *                    or REQUESTER_USER_ID (when SERVICE_CALL_ID is NULL).
     */
    public List<User> getAllAccountManagers(DashboardFilters filters) throws Exception {
        List<User> list = new ArrayList<>();

        StringBuilder sql = new StringBuilder();
        sql.append("SELECT DISTINCT UA.ID, UA.USER_ID, NVL(E.NAME, UA.USER_ID) AS NAME FROM ( ");
        // Branch 1: COORDINATOR from CLIENT_REQUEST when SERVICE_CALL_ID is present
        sql.append("  SELECT CR.COORDINATOR AS UA_ID FROM REPLACEMENT_REQUEST RR ");
        sql.append("  JOIN CLIENT_REQUEST CR ON RR.SERVICE_CALL_ID = CR.CALL_ID ");
        sql.append("  WHERE RR.SERVICE_CALL_ID IS NOT NULL ");
        List<Object> params = new ArrayList<>();
        appendDateFilter(sql, params, filters);
        sql.append("  UNION ");
        // Branch 2: REQUESTER_USER_ID when SERVICE_CALL_ID is NULL
        sql.append("  SELECT RR.REQUESTER_USER_ID AS UA_ID FROM REPLACEMENT_REQUEST RR ");
        sql.append("  WHERE RR.SERVICE_CALL_ID IS NULL ");
        appendDateFilter(sql, params, filters);
        sql.append(") AM_IDS ");
        sql.append("JOIN USER_ACCOUNT UA ON AM_IDS.UA_ID = UA.ID ");
        sql.append("LEFT JOIN EMP E ON UA.EMP_ID = E.ID ");
        sql.append("ORDER BY NAME");

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {

            setParameters(ps, params);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                User user = new User();
                user.setId(rs.getInt("ID"));
                user.setUserId(rs.getString("USER_ID"));
                user.setName(rs.getString("NAME"));
                list.add(user);
            }
        }

        return list;
    }

    /**
     * Search requesters with pagination for Select2 AJAX
     * Sorted by name, filtered by search term
     */
    public List<User> searchRequesters(String search, int limit, int page) throws Exception {
        List<User> list = new ArrayList<>();
        int offset = (page - 1) * limit;

        StringBuilder sql = new StringBuilder();
        sql.append("SELECT * FROM (");
        sql.append("  SELECT ROWNUM AS RN, A.* FROM (");
        sql.append("    SELECT DISTINCT UA.ID, UA.USER_ID, UA.USER_ID AS NAME ");
        sql.append("    FROM REPLACEMENT_REQUEST RR ");
        sql.append("    JOIN USER_ACCOUNT UA ON RR.REQUESTER_USER_ID = UA.ID ");
        sql.append("    WHERE 1=1 ");

        List<Object> params = new ArrayList<>();
        if (search != null && !search.trim().isEmpty()) {
            sql.append("    AND UPPER(UA.USER_ID) LIKE UPPER(?) ");
            params.add("%" + search.trim() + "%");
        }
        sql.append("    ORDER BY UA.USER_ID");
        sql.append("  ) A WHERE ROWNUM <= ?");
        sql.append(") WHERE RN > ?");

        params.add(offset + limit);
        params.add(offset);

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {

            setParameters(ps, params);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                User user = new User();
                user.setId(rs.getInt("ID"));
                user.setUserId(rs.getString("USER_ID"));
                user.setName(rs.getString("NAME"));
                list.add(user);
            }
        }

        return list;
    }

    /**
     * Search owners with pagination for Select2 AJAX
     * Sorted by name, filtered by search term
     */
    public List<User> searchOwners(String search, int limit, int page) throws Exception {
        List<User> list = new ArrayList<>();
        int offset = (page - 1) * limit;

        StringBuilder sql = new StringBuilder();
        sql.append("SELECT * FROM (");
        sql.append("  SELECT ROWNUM AS RN, A.* FROM (");
        sql.append("    SELECT DISTINCT UA.ID, UA.USER_ID, UA.USER_ID AS NAME ");
        sql.append("    FROM REPLACEMENT_REQUEST RR ");
        sql.append("    JOIN USER_ACCOUNT UA ON RR.CURRENT_OWNER_ID = UA.ID ");
        sql.append("    WHERE 1=1 ");

        List<Object> params = new ArrayList<>();
        if (search != null && !search.trim().isEmpty()) {
            sql.append("    AND UPPER(UA.USER_ID) LIKE UPPER(?) ");
            params.add("%" + search.trim() + "%");
        }
        sql.append("    ORDER BY UA.USER_ID");
        sql.append("  ) A WHERE ROWNUM <= ?");
        sql.append(") WHERE RN > ?");

        params.add(offset + limit);
        params.add(offset);

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {

            setParameters(ps, params);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                User user = new User();
                user.setId(rs.getInt("ID"));
                user.setUserId(rs.getString("USER_ID"));
                user.setName(rs.getString("NAME"));
                list.add(user);
            }
        }

        return list;
    }

    /**
     * Search account managers with pagination for Select2 AJAX
     * Account Manager logic:
     * - If SOURCE = 'SERVICE_CALL': get from CLIENT_REQUEST.CALL_BY via REPLACEMENT_REQUEST.SERVICE_CALL_ID
     * - Otherwise: use REQUESTER_USER_ID
     */
    public List<User> searchAccountManagers(String search, int limit, int page) throws Exception {
        List<User> list = new ArrayList<>();
        int offset = (page - 1) * limit;

        StringBuilder sql = new StringBuilder();
        sql.append("SELECT * FROM (");
        sql.append("  SELECT ROWNUM AS RN, A.* FROM (");
        sql.append("    SELECT DISTINCT ID, USER_ID, USER_ID AS NAME FROM ( ");
        // Union of both sources for account managers
        // Source 1: SERVICE_CALL_ID present -> CLIENT_REQUEST.COORDINATOR -> USER_ACCOUNT.ID
        sql.append("      SELECT DISTINCT UA2.ID, UA2.USER_ID ");
        sql.append("      FROM REPLACEMENT_REQUEST RR ");
        sql.append("      JOIN CLIENT_REQUEST CR ON RR.SERVICE_CALL_ID = CR.CALL_ID ");
        sql.append("      JOIN USER_ACCOUNT UA2 ON CR.COORDINATOR = UA2.ID ");
        sql.append("      WHERE RR.SERVICE_CALL_ID IS NOT NULL ");
        sql.append("      UNION ");
        // Source 2: SERVICE_CALL_ID is NULL -> use Requester (REQUESTER_USER_ID -> USER_ACCOUNT.ID)
        sql.append("      SELECT DISTINCT UA3.ID, UA3.USER_ID ");
        sql.append("      FROM REPLACEMENT_REQUEST RR2 ");
        sql.append("      JOIN USER_ACCOUNT UA3 ON RR2.REQUESTER_USER_ID = UA3.ID ");
        sql.append("      WHERE RR2.SERVICE_CALL_ID IS NULL ");
        sql.append("    ) ");
        sql.append("    WHERE 1=1 ");

        List<Object> params = new ArrayList<>();
        if (search != null && !search.trim().isEmpty()) {
            sql.append("    AND UPPER(USER_ID) LIKE UPPER(?) ");
            params.add("%" + search.trim() + "%");
        }
        sql.append("    ORDER BY USER_ID");
        sql.append("  ) A WHERE ROWNUM <= ?");
        sql.append(") WHERE RN > ?");

        params.add(offset + limit);
        params.add(offset);

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {

            setParameters(ps, params);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                User user = new User();
                user.setId(rs.getInt("ID"));
                user.setUserId(rs.getString("USER_ID"));
                user.setName(rs.getString("NAME"));
                list.add(user);
            }
        }

        return list;
    }

    /**
     * Get all stages for filter dropdown
     */
    public List<Map<String, Object>> getAllStages() throws Exception {
        List<Map<String, Object>> list = new ArrayList<>();

        String sql = "SELECT ID, STAGE_CODE, DESCRIPTION FROM TAT_MASTER WHERE STATUS = 1 ORDER BY ID";

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                Map<String, Object> stage = new HashMap<>();
                stage.put("id", rs.getInt("ID"));
                stage.put("code", rs.getString("STAGE_CODE"));
                stage.put("description", rs.getString("DESCRIPTION"));
                list.add(stage);
            }
        }

        return list;
    }

    /**
     * Get stages that have replacement requests in the given date range.
     * Returns only stages relevant to the current filter context.
     */
    public List<Map<String, Object>> getStagesForRequests(DashboardFilters filters) throws Exception {
        List<Map<String, Object>> list = new ArrayList<>();

        StringBuilder sql = new StringBuilder();
        sql.append("SELECT DISTINCT TM.ID, TM.STAGE_CODE, TM.DESCRIPTION ");
        sql.append("FROM TAT_MASTER TM ");
        sql.append("JOIN REPLACEMENT_REQUEST RR ON RR.CURRENT_STAGE = TM.ID ");
        sql.append("WHERE TM.STATUS = 1 ");

        List<Object> params = new ArrayList<>();
        appendDateFilter(sql, params, filters);
        sql.append("ORDER BY TM.ID");

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {

            setParameters(ps, params);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                Map<String, Object> stage = new HashMap<>();
                stage.put("id", rs.getInt("ID"));
                stage.put("code", rs.getString("STAGE_CODE"));
                stage.put("description", rs.getString("DESCRIPTION"));
                list.add(stage);
            }
        }

        return list;
    }

    /**
     * Get department name by ID
     */
    public String getDepartmentName(int departmentId) throws Exception {
        String sql = "SELECT NAME FROM DEPT WHERE ID = ?";

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, departmentId);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                return rs.getString("NAME");
            }
        }
        return null;
    }

    /**
     * Get category (replacement reason) name by ID
     */
    public String getCategoryName(int categoryId) throws Exception {
        String sql = "SELECT NAME FROM REPLACEMENT_REASON WHERE ID = ?";

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, categoryId);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                return rs.getString("NAME");
            }
        }
        return null;
    }

    /**
     * Get stage name by ID
     */
    public String getStageName(int stageId) throws Exception {
        String sql = "SELECT DESCRIPTION FROM TAT_MASTER WHERE ID = ?";

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, stageId);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                return rs.getString("DESCRIPTION");
            }
        }
        return null;
    }

    /**
     * Get owner name by user account ID
     */
    public String getOwnerName(int ownerId) throws Exception {
        String sql = "SELECT NVL(E.NAME, UA.USER_ID) AS OWNER_NAME " +
                "FROM USER_ACCOUNT UA LEFT JOIN EMP E ON UA.EMP_ID = E.ID " +
                "WHERE UA.ID = ?";

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, ownerId);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                return rs.getString("OWNER_NAME");
            }
        }
        return null;
    }

    // =====================================================
    // HELPER METHODS
    // =====================================================

    /**
     * Append date filter to SQL (for REPLACEMENT_REQUEST based queries)
     */
    private void appendDateFilter(StringBuilder sql, List<Object> params, DashboardFilters filters) {
        if (filters.getFromDate() != null && !filters.getFromDate().isEmpty()) {
            sql.append("AND RR.CREATION_DATE_TIME >= TO_TIMESTAMP(?, 'YYYY-MM-DD HH24:MI:SS') ");
            params.add(filters.getFromDate() + " 00:00:00");
        }
        if (filters.getToDate() != null && !filters.getToDate().isEmpty()) {
            sql.append("AND RR.CREATION_DATE_TIME < TO_TIMESTAMP(?, 'YYYY-MM-DD HH24:MI:SS') + INTERVAL '1' DAY ");
            params.add(filters.getToDate() + " 00:00:00");
        }
    }

    /**
     * Append date filter for event tracking queries (filter by START_AT)
     */
    private void appendDateFilterForEventTracking(StringBuilder sql, List<Object> params, DashboardFilters filters) {
        if (filters.getFromDate() != null && !filters.getFromDate().isEmpty()) {
            sql.append("AND FET.START_AT >= TO_TIMESTAMP(?, 'YYYY-MM-DD HH24:MI:SS') ");
            params.add(filters.getFromDate() + " 00:00:00");
        }
        if (filters.getToDate() != null && !filters.getToDate().isEmpty()) {
            sql.append("AND FET.START_AT < TO_TIMESTAMP(?, 'YYYY-MM-DD HH24:MI:SS') + INTERVAL '1' DAY ");
            params.add(filters.getToDate() + " 00:00:00");
        }
    }

    /**
     * Append common filters to SQL
     */
    private void appendCommonFilters(StringBuilder sql, List<Object> params, DashboardFilters filters) {
        if (filters.getRequesterId() != null) {
            sql.append("AND RR.REQUESTER_USER_ID = ? ");
            params.add(filters.getRequesterId());
        }
        if (filters.getAccountManagerId() != null) {
            // Account Manager: if SERVICE_CALL_ID is present use COORDINATOR from CLIENT_REQUEST, else use REQUESTER_USER_ID
            sql.append("AND ((RR.SERVICE_CALL_ID IS NOT NULL AND EXISTS ");
            sql.append("  (SELECT 1 FROM CLIENT_REQUEST CR2 WHERE CR2.CALL_ID = RR.SERVICE_CALL_ID AND CR2.COORDINATOR = ?)) ");
            sql.append(" OR (RR.SERVICE_CALL_ID IS NULL AND RR.REQUESTER_USER_ID = ?)) ");
            params.add(filters.getAccountManagerId());
            params.add(filters.getAccountManagerId());
        }
        if (filters.getCurrentOwnerId() != null) {
            sql.append("AND RR.CURRENT_OWNER_ID = ? ");
            params.add(filters.getCurrentOwnerId());
        }
        if (filters.getStageId() != null) {
            sql.append("AND RR.CURRENT_STAGE = ? ");
            params.add(filters.getStageId());
        }
        if (filters.getStatus() != null && !filters.getStatus().isEmpty()) {
            sql.append("AND RR.STATUS = ? ");
            params.add(filters.getStatus());
        }
        if (filters.getCategoryId() != null) {
            sql.append("AND RR.REPLACEMENT_REASON_ID = ? ");
            params.add(filters.getCategoryId());
        }
    }

    /**
     * Execute count query
     */
    private int executeCountQuery(String sql, List<Object> params) throws Exception {
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            setParameters(ps, params);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                return rs.getInt(1);
            }
        }
        return 0;
    }

    /**
     * Set parameters on PreparedStatement
     */
    private void setParameters(PreparedStatement ps, List<Object> params) throws SQLException {
        for (int i = 0; i < params.size(); i++) {
            ps.setObject(i + 1, params.get(i));
        }
    }

    /**
     * Sanitize sort column to prevent SQL injection
     */
    private String sanitizeSortColumn(String column) {
        if (column == null || column.isEmpty()) {
            return "RR.CREATION_DATE_TIME";
        }

        // Whitelist of allowed sort columns
        Map<String, String> allowedColumns = new HashMap<>();
        allowedColumns.put("id", "RR.ID");
        allowedColumns.put("client", "CLIENT_NAME");
        allowedColumns.put("status", "RR.STATUS");
        allowedColumns.put("owner", "OWNER_NAME");
        allowedColumns.put("account", "ACCOUNT_MANAGER_USER_ID");
        allowedColumns.put("date", "RR.CREATION_DATE_TIME");
        allowedColumns.put("tat", "NVL(FET_CURR.TAT_PERCENTAGE, 0)");
        allowedColumns.put("CREATION_DATE_TIME", "RR.CREATION_DATE_TIME");

        return allowedColumns.getOrDefault(column.toLowerCase(), "RR.CREATION_DATE_TIME");
    }

    // =====================================================
    // EVENT TRACKING METHODS (for drill-down view)
    // =====================================================

    /**
     * Get event tracking data with filters
     * This is used for the Event Tracking detail view (drill-down from summaries)
     */
    public List<EventTrackingRow> getEventTrackingData(DashboardFilters filters) throws Exception {
        List<EventTrackingRow> list = new ArrayList<>();

        StringBuilder sql = new StringBuilder();
        sql.append("SELECT ");
        sql.append("  FET.ID, FET.REPLACEMENT_REQUEST_ID, ");
        sql.append("  FET.CURRENT_STAGE_ID, TM.STAGE_CODE, TM.DESCRIPTION AS STAGE_DESC, ");
        sql.append("  FET.CURRENT_OWNER_USER_ID, UA.USER_ID AS OWNER_USER_ID, ");
        sql.append("  UA.USER_ID AS OWNER_NAME, ");
        sql.append("  NVL(UA.DEPT_ID, 0) AS DEPT_ID, NVL(UA.DEPT, 'Unknown') AS DEPT_NAME, ");
        sql.append("  FET.START_AT, FET.END_AT, FET.COMMENTS, NVL(FET.TAT_PERCENTAGE, 0) AS TAT_PERCENTAGE, ");
        sql.append("  C.NAME AS CLIENT_NAME, C.CITY AS CLIENT_CITY, ");
        sql.append("  RR.STATUS AS REQUEST_STATUS, ");
        sql.append("  RRSN.NAME AS REASON_NAME ");
        sql.append("FROM RPLCE_FLOW_EVENT_TRACKING FET ");
        sql.append("JOIN REPLACEMENT_REQUEST RR ON FET.REPLACEMENT_REQUEST_ID = RR.ID ");
        sql.append("JOIN TAT_MASTER TM ON FET.CURRENT_STAGE_ID = TM.ID ");
        sql.append("JOIN USER_ACCOUNT UA ON FET.CURRENT_OWNER_USER_ID = UA.ID ");
        sql.append("LEFT JOIN CLIENT C ON RR.CLIENT_DOT_ID_SIGNING = C.ID ");
        sql.append("LEFT JOIN REPLACEMENT_REASON RRSN ON RR.REPLACEMENT_REASON_ID = RRSN.ID ");
        sql.append("WHERE 1=1 ");

        List<Object> params = new ArrayList<>();

        // Apply filters
        if (filters.getEventRequestId() != null) {
            sql.append("AND FET.REPLACEMENT_REQUEST_ID = ? ");
            params.add(filters.getEventRequestId());
        }
        if (filters.getStageId() != null) {
            sql.append("AND FET.CURRENT_STAGE_ID = ? ");
            params.add(filters.getStageId());
        }
        if (filters.getCurrentOwnerId() != null) {
            sql.append("AND FET.CURRENT_OWNER_USER_ID = ? ");
            params.add(filters.getCurrentOwnerId());
        }
        if (filters.getDepartmentId() != null) {
            sql.append("AND UA.DEPT_ID = ? ");
            params.add(filters.getDepartmentId());
        }
        if (filters.getTatFilter() != null) {
            if ("within".equalsIgnoreCase(filters.getTatFilter())) {
                sql.append("AND NVL(FET.TAT_PERCENTAGE, 0) < 100 ");
            } else if ("beyond".equalsIgnoreCase(filters.getTatFilter())) {
                sql.append("AND NVL(FET.TAT_PERCENTAGE, 0) >= 100 ");
            }
        }

        // Date filters on START_AT
        if (filters.getFromDate() != null && !filters.getFromDate().isEmpty()) {
            sql.append("AND FET.START_AT >= TO_TIMESTAMP(?, 'YYYY-MM-DD HH24:MI:SS') ");
            params.add(filters.getFromDate() + " 00:00:00");
        }
        if (filters.getToDate() != null && !filters.getToDate().isEmpty()) {
            sql.append("AND FET.START_AT < TO_TIMESTAMP(?, 'YYYY-MM-DD HH24:MI:SS') + INTERVAL '1' DAY ");
            params.add(filters.getToDate() + " 00:00:00");
        }

        // Order by
        sql.append("ORDER BY FET.START_AT DESC ");

        // Server-side pagination
        sql.append("OFFSET ? ROWS FETCH NEXT ? ROWS ONLY");
        params.add(filters.getOffset());
        params.add(filters.getPageSize());

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {

            setParameters(ps, params);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                EventTrackingRow row = new EventTrackingRow();
                row.setId(rs.getInt("ID"));
                row.setReplacementRequestId(rs.getInt("REPLACEMENT_REQUEST_ID"));
                row.setStageId(rs.getInt("CURRENT_STAGE_ID"));
                row.setStageCode(rs.getString("STAGE_CODE"));
                row.setStageDescription(rs.getString("STAGE_DESC"));
                row.setOwnerId(rs.getInt("CURRENT_OWNER_USER_ID"));
                row.setOwnerUserId(rs.getString("OWNER_USER_ID"));
                row.setOwnerName(rs.getString("OWNER_NAME"));
                row.setDepartmentId(rs.getInt("DEPT_ID"));
                row.setDepartmentName(rs.getString("DEPT_NAME"));
                row.setStartAt(rs.getTimestamp("START_AT"));
                row.setEndAt(rs.getTimestamp("END_AT"));
                row.setComments(rs.getString("COMMENTS"));

                BigDecimal tatPct = rs.getBigDecimal("TAT_PERCENTAGE");
                row.setTatPercentage(tatPct != null ? tatPct.doubleValue() : null);

                row.setClientName(rs.getString("CLIENT_NAME"));
                row.setClientCity(rs.getString("CLIENT_CITY"));
                row.setRequestStatus(rs.getString("REQUEST_STATUS"));
                row.setReasonName(rs.getString("REASON_NAME"));

                list.add(row);
            }
        }

        return list;
    }

    /**
     * Get count of event tracking records with filters
     */
    public int getEventTrackingCount(DashboardFilters filters) throws Exception {
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT COUNT(*) ");
        sql.append("FROM RPLCE_FLOW_EVENT_TRACKING FET ");
        sql.append("JOIN REPLACEMENT_REQUEST RR ON FET.REPLACEMENT_REQUEST_ID = RR.ID ");
        sql.append("JOIN TAT_MASTER TM ON FET.CURRENT_STAGE_ID = TM.ID ");
        sql.append("JOIN USER_ACCOUNT UA ON FET.CURRENT_OWNER_USER_ID = UA.ID ");
        sql.append("WHERE 1=1 ");

        List<Object> params = new ArrayList<>();

        if (filters.getEventRequestId() != null) {
            sql.append("AND FET.REPLACEMENT_REQUEST_ID = ? ");
            params.add(filters.getEventRequestId());
        }
        if (filters.getStageId() != null) {
            sql.append("AND FET.CURRENT_STAGE_ID = ? ");
            params.add(filters.getStageId());
        }
        if (filters.getCurrentOwnerId() != null) {
            sql.append("AND FET.CURRENT_OWNER_USER_ID = ? ");
            params.add(filters.getCurrentOwnerId());
        }
        if (filters.getDepartmentId() != null) {
            sql.append("AND UA.DEPT_ID = ? ");
            params.add(filters.getDepartmentId());
        }
        if (filters.getTatFilter() != null) {
            if ("within".equalsIgnoreCase(filters.getTatFilter())) {
                sql.append("AND NVL(FET.TAT_PERCENTAGE, 0) < 100 ");
            } else if ("beyond".equalsIgnoreCase(filters.getTatFilter())) {
                sql.append("AND NVL(FET.TAT_PERCENTAGE, 0) >= 100 ");
            }
        }
        if (filters.getFromDate() != null && !filters.getFromDate().isEmpty()) {
            sql.append("AND FET.START_AT >= TO_TIMESTAMP(?, 'YYYY-MM-DD HH24:MI:SS') ");
            params.add(filters.getFromDate() + " 00:00:00");
        }
        if (filters.getToDate() != null && !filters.getToDate().isEmpty()) {
            sql.append("AND FET.START_AT < TO_TIMESTAMP(?, 'YYYY-MM-DD HH24:MI:SS') + INTERVAL '1' DAY ");
            params.add(filters.getToDate() + " 00:00:00");
        }

        return executeCountQuery(sql.toString(), params);
    }

    /**
     * Get all departments for filter dropdown
     */
    public List<Map<String, Object>> getAllDepartments() throws Exception {
        List<Map<String, Object>> list = new ArrayList<>();

        // Get distinct departments from USER_ACCOUNT table
        String sql = "SELECT DISTINCT NVL(DEPT_ID, 0) AS ID, NVL(DEPT, 'Unknown') AS NAME " +
                "FROM USER_ACCOUNT WHERE DEPT IS NOT NULL ORDER BY NAME";

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                Map<String, Object> dept = new HashMap<>();
                dept.put("id", rs.getInt("ID"));
                dept.put("name", rs.getString("NAME"));
                list.add(dept);
            }
        }

        return list;
    }
}

