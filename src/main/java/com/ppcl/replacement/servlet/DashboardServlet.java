package com.ppcl.replacement.servlet;

import com.google.gson.Gson;
import com.ppcl.replacement.dao.DashboardDAO;
import com.ppcl.replacement.model.*;
import com.ppcl.replacement.util.JsonResponse;
import com.ppcl.replacement.model.DashboardSummary.CategorySummary;
import com.ppcl.replacement.model.DashboardSummary.DepartmentSummary;
import com.ppcl.replacement.model.DashboardSummary.OwnerSummary;
import com.ppcl.replacement.model.DashboardSummary.StageSummary;
import com.ppcl.replacement.util.DashboardUtil;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Servlet handling Dashboard operations
 * <p>
 * URL Patterns:
 * - /views/replacement/dashboard - Main dashboard page
 * - /views/replacement/dashboard/kpi - AJAX: Get KPI counts
 * - /views/replacement/dashboard/requests - AJAX: Get paginated requests
 * - /views/replacement/dashboard/printers - AJAX: Get printer details popup
 * - /views/replacement/dashboard/summary - AJAX: Get summary data (stage/owner/dept/category)
 * - /views/replacement/dashboard/export - Export data to CSV/Excel
 */
@WebServlet(urlPatterns = {
        "/views/replacement/dashboard",
        "/views/replacement/dashboard/kpi",
        "/views/replacement/dashboard/requests",
        "/views/replacement/dashboard/printers",
        "/views/replacement/dashboard/summary",
        "/views/replacement/dashboard/export",
        "/views/replacement/dashboard/filters",
        "/views/replacement/dashboard/events"
})
public class DashboardServlet extends BaseServlet {

    private static final long serialVersionUID = 1L;
    private static final int DEFAULT_PAGE_SIZE = 10;
    private static final int DEFAULT_EVENT_PAGE_SIZE = 25;

    private final Gson gson = new Gson();
    private final DashboardDAO dao = new DashboardDAO();

    @Override
    protected void doGet(final HttpServletRequest request, final HttpServletResponse response)
            throws ServletException, IOException {

        final String servletPath = request.getServletPath();

        // Get session info
        final HttpSession session = request.getSession(false);
        int userId = getSessionUserId(request);
        String userRole = session != null ? (String) session.getAttribute("userRole") : null;

        // Default values for development
        if (userRole == null) userRole = "ADMIN";

        try {
            if (servletPath.equals("/views/replacement/dashboard")) {
                showDashboard(request, response);
            } else if (servletPath.endsWith("/kpi")) {
                getKPICounts(request, response);
            } else if (servletPath.endsWith("/requests")) {
                getRequests(request, response);
            } else if (servletPath.endsWith("/printers")) {
                getPrinterDetails(request, response);
            } else if (servletPath.endsWith("/summary")) {
                getSummaryData(request, response);
            } else if (servletPath.endsWith("/export")) {
                exportData(request, response);
            } else if (servletPath.endsWith("/filters")) {
                getFilterOptions(request, response);
            } else if (servletPath.endsWith("/events")) {
                showEventTracking(request, response);
            } else {
                response.sendError(HttpServletResponse.SC_NOT_FOUND);
            }

        } catch (final Exception e) {
            e.printStackTrace();
            if (isAjaxRequest(request)) {
                sendJsonError(response, "Error: " + e.getMessage());
            } else {
                request.setAttribute("errorMessage", "System Error: " + e.getMessage());
                forwardToJsp(request, response, "dashboard/dashboard.jsp");
            }
        }
    }

    @Override
    protected void doPost(final HttpServletRequest request, final HttpServletResponse response)
            throws ServletException, IOException {
        // POST requests delegate to GET for filter submissions
        doGet(request, response);
    }

    // =====================================================
    // MAIN DASHBOARD PAGE
    // =====================================================

    /**
     * Show main dashboard page with all data
     */
    private void showDashboard(final HttpServletRequest request, final HttpServletResponse response)
            throws Exception {

        // Build filters from request parameters
        final DashboardFilters filters = buildFiltersFromRequest(request);

        // Get KPI counts (optimized - single query for total, pending, closed)
        final int[] kpiCounts = dao.getAllKpiCounts(filters);
        final int totalCount = kpiCounts[0];
        final int pendingCount = kpiCounts[1];
        final int closedCount = kpiCounts[2];
        final int tatBreachCount = dao.getTatBreachCount(filters); // Separate query (requires JOIN)

        // Build summary object
        final DashboardSummary summary = new DashboardSummary();
        summary.setTotalRequests(totalCount);
        summary.setPendingRequests(pendingCount);
        summary.setClosedRequests(closedCount);
        summary.setTatBreachCount(tatBreachCount);

        // Get aggregated summaries (getCurrentStageSummary groups by RR.CURRENT_STAGE - shows only current stage)
        final List<StageSummary> stageSummary = dao.getCurrentStageSummary(filters);
        final List<OwnerSummary> ownerSummary = dao.getOwnerSummary(filters);
        final List<DepartmentSummary> deptSummary = dao.getDepartmentSummary(filters);
        final List<CategorySummary> categorySummary = dao.getCategorySummary(filters);

        summary.setStageSummary(stageSummary);
        summary.setOwnerSummary(ownerSummary);
        summary.setDepartmentSummary(deptSummary);
        summary.setCategorySummary(categorySummary);

        // Get paginated requests
        final List<DashboardRequest> requests = dao.getReplacementRequests(filters);

        // Calculate pagination info
        final int totalPages = DashboardUtil.getTotalPages(totalCount, filters.getPageSize());
        final int startRecord = DashboardUtil.getStartRecord(filters.getPage(), filters.getPageSize());
        final int endRecord = DashboardUtil.getEndRecord(filters.getPage(), filters.getPageSize(), totalCount);

        // Get filter dropdown options (all date-filtered)
        final List<User> requesters = dao.getAllRequesters(filters);
        final List<User> accountManagers = dao.getAllAccountManagers(filters);
        final List<User> owners = dao.getAllOwners(filters);
        final List<Map<String, Object>> stages = dao.getStagesForRequests(filters);

        // Set request attributes
        request.setAttribute("summary", summary);
        request.setAttribute("requests", requests);
        request.setAttribute("filters", filters);

        // Pagination attributes
        request.setAttribute("totalCount", totalCount);
        request.setAttribute("totalPages", totalPages);
        request.setAttribute("currentPage", filters.getPage());
        request.setAttribute("pageSize", filters.getPageSize());
        request.setAttribute("startRecord", startRecord);
        request.setAttribute("endRecord", endRecord);

        // Filter dropdown options
        request.setAttribute("requesters", requesters);
        request.setAttribute("accountManagers", accountManagers);
        request.setAttribute("owners", owners);
        request.setAttribute("stages", stages);

        // Status options (static)
        request.setAttribute("statusOptions", getStatusOptions());

        // Set page title
        request.setAttribute("pageTitle", "Dashboard");

        // Forward to JSP
        forwardToJsp(request, response, "dashboard/dashboard.jsp");
    }

    // =====================================================
    // AJAX ENDPOINTS
    // =====================================================

    /**
     * Get KPI counts via AJAX
     */
    private void getKPICounts(final HttpServletRequest request, final HttpServletResponse response)
            throws Exception {

        final DashboardFilters filters = buildFiltersFromRequest(request);

        // Optimized: single query for total, pending, closed counts
        final int[] kpiCounts = dao.getAllKpiCounts(filters);

        final Map<String, Object> data = new HashMap<>();
        data.put("totalCount", kpiCounts[0]);
        data.put("pendingCount", kpiCounts[1]);
        data.put("closedCount", kpiCounts[2]);
        data.put("tatBreachCount", dao.getTatBreachCount(filters));
        data.put("pendingByStage", dao.getPendingCountsByStage(filters));

        sendJsonSuccess(response, "KPI counts retrieved", data);
    }

    /**
     * Get paginated requests via AJAX
     */
    private void getRequests(final HttpServletRequest request, final HttpServletResponse response)
            throws Exception {

        final DashboardFilters filters = buildFiltersFromRequest(request);

        final List<DashboardRequest> requests = dao.getReplacementRequests(filters);
        final int totalCount = dao.getTotalCount(filters);
        final int totalPages = DashboardUtil.getTotalPages(totalCount, filters.getPageSize());

        final Map<String, Object> data = new HashMap<>();
        data.put("requests", requests);
        data.put("totalCount", totalCount);
        data.put("totalPages", totalPages);
        data.put("currentPage", filters.getPage());
        data.put("pageSize", filters.getPageSize());

        sendJsonSuccess(response, "Requests retrieved", data);
    }

    /**
     * Get printer details for a request via AJAX
     */
    private void getPrinterDetails(final HttpServletRequest request, final HttpServletResponse response)
            throws Exception {

        final String reqIdStr = request.getParameter("reqId");
        if (reqIdStr == null || reqIdStr.isEmpty()) {
            sendJsonError(response, "Request ID is required");
            return;
        }

        final int reqId = Integer.parseInt(reqIdStr);
        final List<PrinterDetailRow> printers = dao.getPrinterDetails(reqId);

        sendJsonSuccess(response, "Printer details retrieved", printers);
    }

    /**
     * Get summary data via AJAX
     */
    private void getSummaryData(final HttpServletRequest request, final HttpServletResponse response)
            throws Exception {

        final DashboardFilters filters = buildFiltersFromRequest(request);
        final String type = request.getParameter("type"); // stage, owner, dept, category

        final Object data;
        switch (type != null ? type : "all") {
            case "stage":
                data = dao.getStageSummary(filters);
                break;
            case "owner":
                data = dao.getOwnerSummary(filters);
                break;
            case "dept":
                data = dao.getDepartmentSummary(filters);
                break;
            case "category":
                data = dao.getCategorySummary(filters);
                break;
            default:
                final Map<String, Object> allData = new HashMap<>();
                allData.put("stageSummary", dao.getStageSummary(filters));
                allData.put("ownerSummary", dao.getOwnerSummary(filters));
                allData.put("departmentSummary", dao.getDepartmentSummary(filters));
                allData.put("categorySummary", dao.getCategorySummary(filters));
                data = allData;
        }

        sendJsonSuccess(response, "Summary data retrieved", data);
    }

    /**
     * Get filter dropdown options via AJAX
     * Supports type parameter: requesters, accountManagers, owners
     * Returns a flat array for Select2 AJAX compatibility
     */
    private void getFilterOptions(final HttpServletRequest request, final HttpServletResponse response)
            throws Exception {

        final DashboardFilters filters = buildFiltersFromRequest(request);
        final String type = request.getParameter("type");

        Object data;

        if ("requesters".equals(type)) {
            data = dao.getAllRequesters(filters);
        } else if ("accountManagers".equals(type)) {
            data = dao.getAllAccountManagers(filters);
        } else if ("owners".equals(type)) {
            data = dao.getAllOwners(filters);
        } else {
            // Default: return all filter options (date-filtered)
            final Map<String, Object> allData = new HashMap<>();
            allData.put("requesters", dao.getAllRequesters(filters));
            allData.put("accountManagers", dao.getAllAccountManagers(filters));
            allData.put("owners", dao.getAllOwners(filters));
            allData.put("stages", dao.getStagesForRequests(filters));
            allData.put("statuses", getStatusOptions());
            data = allData;
        }

        sendJsonSuccess(response, "Filter options retrieved", data);
    }

    /**
     * Export data to CSV/Excel
     */
    private void exportData(final HttpServletRequest request, final HttpServletResponse response)
            throws Exception {

        final DashboardFilters filters = buildFiltersFromRequest(request);
        filters.setPage(1);
        filters.setPageSize(10000); // Get all records for export

        // Parameters reserved for future format/type support
        // String format = request.getParameter("format");
        // String type = request.getParameter("type"); // requests, summary

        final List<DashboardRequest> requests = dao.getReplacementRequests(filters);

        // Set CSV response headers
        response.setContentType("text/csv");
        response.setHeader("Content-Disposition",
                "attachment; filename=replacement_requests_" + System.currentTimeMillis() + ".csv");

        final PrintWriter out = response.getWriter();

        // Write CSV header
        out.println("Request ID,Client Name,Requester,Account Manager,Status,Current Owner,Owner Department,Stage,Request Date,TAT Status,Printers");

        // Write data rows
        for (final DashboardRequest req : requests) {
            final String requesterDisplay = req.getRequesterName() != null ? req.getRequesterName() :
                    (req.getRequesterUserIdStr() != null ? req.getRequesterUserIdStr() : "");
            final String amDisplay = req.getAccountManagerName() != null ? req.getAccountManagerName() :
                    (req.getAccountManagerUserId() != null ? req.getAccountManagerUserId() : "");
            final String ownerDisplay = req.getCurrentOwnerName() != null ? req.getCurrentOwnerName() :
                    (req.getCurrentOwnerUserId() != null ? req.getCurrentOwnerUserId() : "");
            final String row = escapeCSV(req.getDisplayId()) + "," +
                    escapeCSV(req.getClientName()) + "," +
                    escapeCSV(requesterDisplay) + "," +
                    escapeCSV(amDisplay) + "," +
                    escapeCSV(req.getStatus()) + "," +
                    escapeCSV(ownerDisplay) + "," +
                    escapeCSV(req.getCurrentOwnerDepartment()) + "," +
                    escapeCSV(req.getCurrentStageDescription()) + "," +
                    escapeCSV(DashboardUtil.formatDisplayDate(req.getCreationDateTime())) + "," +
                    escapeCSV(req.getTatStatus()) + "," +
                    req.getPrinterCount();
            out.println(row);
        }

        out.flush();
    }

    // =====================================================
    // EVENT TRACKING VIEW (Drill-down from Summaries)
    // =====================================================

    /**
     * Show Event Tracking detail page
     * This is the drill-down view from Stage/Owner/Department summaries
     * Shows RPLCE_FLOW_EVENT_TRACKING data with filters
     */
    private void showEventTracking(final HttpServletRequest request, final HttpServletResponse response)
            throws ServletException, IOException {

        try {
            final DashboardFilters filters = buildFiltersFromRequest(request);

            // Default to larger page size for events if not explicitly set
            if (request.getParameter("pageSize") == null) {
                filters.setPageSize(DEFAULT_EVENT_PAGE_SIZE);
            }

            // Get event tracking data
            final List<EventTrackingRow> events = dao.getEventTrackingData(filters);
            final int totalCount = dao.getEventTrackingCount(filters);

            // Calculate pagination info
            final int totalPages = DashboardUtil.getTotalPages(totalCount, filters.getPageSize());
            final int startRecord = DashboardUtil.getStartRecord(filters.getPage(), filters.getPageSize());
            final int endRecord = DashboardUtil.getEndRecord(filters.getPage(), filters.getPageSize(), totalCount);

            // Get filter dropdown options (with current filters for date awareness)
            final List<DashboardSummary.StageSummary> stages = dao.getStageSummary(filters);
            final List<DashboardSummary.OwnerSummary> owners = dao.getOwnerSummary(filters);
            final List<Map<String, Object>> departments = dao.getAllDepartments();

            // Get summaries for overview cards
            final List<DashboardSummary.StageSummary> stageSummary = dao.getCurrentStageSummary(filters);
            final List<DashboardSummary.OwnerSummary> ownerSummary = dao.getOwnerSummary(filters);
            final List<DashboardSummary.DepartmentSummary> departmentSummary = dao.getDepartmentSummary(filters);

            // Set request attributes
            request.setAttribute("events", events);
            request.setAttribute("totalCount", totalCount);
            request.setAttribute("filters", filters);
            request.setAttribute("stages", stages);
            request.setAttribute("owners", owners);
            request.setAttribute("departments", departments);
            request.setAttribute("stageSummary", stageSummary);
            request.setAttribute("ownerSummary", ownerSummary);
            request.setAttribute("departmentSummary", departmentSummary);

            // Pagination attributes
            request.setAttribute("totalPages", totalPages);
            request.setAttribute("currentPage", filters.getPage());
            request.setAttribute("pageSize", filters.getPageSize());
            request.setAttribute("startRecord", startRecord);
            request.setAttribute("endRecord", endRecord);

            // Get filter display names for tags
            if (filters.getStageId() != null) {
                request.setAttribute("stageName", dao.getStageName(filters.getStageId()));
            }
            if (filters.getCurrentOwnerId() != null) {
                request.setAttribute("ownerName", dao.getOwnerName(filters.getCurrentOwnerId()));
            }
            if (filters.getDepartmentId() != null) {
                request.setAttribute("departmentName", dao.getDepartmentName(filters.getDepartmentId()));
            }

            // Forward to event tracking JSP
            forwardToJsp(request, response, "dashboard/event-tracking.jsp");

        } catch (final Exception e) {
            log("Error in showEventTracking: " + e.getMessage(), e);
            request.setAttribute("errorMessage", "Error loading event tracking: " + e.getMessage());
            forwardToJsp(request, response, "dashboard/event-tracking.jsp");
        }
    }

    // =====================================================
    // HELPER METHODS
    // =====================================================

    /**
     * Build DashboardFilters from request parameters
     */
    private DashboardFilters buildFiltersFromRequest(final HttpServletRequest request) {
        final DashboardFilters filters = new DashboardFilters();

        // Date range
        final String fromDate = request.getParameter("fromDate");
        final String toDate = request.getParameter("toDate");

        if (fromDate != null && !fromDate.isEmpty()) {
            filters.setFromDate(fromDate);
        }
        if (toDate != null && !toDate.isEmpty()) {
            filters.setToDate(toDate);
        }

        // Apply default dates if not provided
        filters.applyDefaultDates();

        // Other filters
        filters.setRequesterId(DashboardUtil.parseIntSafe(request.getParameter("requesterId")));
        filters.setAccountManagerId(DashboardUtil.parseIntSafe(request.getParameter("accountManagerId")));
        filters.setCurrentOwnerId(DashboardUtil.parseIntSafe(request.getParameter("ownerId")));
        filters.setStageId(DashboardUtil.parseIntSafe(request.getParameter("stageId")));
        filters.setStatus(request.getParameter("status"));
        filters.setTatFilter(request.getParameter("tatFilter"));
        filters.setDepartmentId(DashboardUtil.parseIntSafe(request.getParameter("departmentId")));
        filters.setCategoryId(DashboardUtil.parseIntSafe(request.getParameter("categoryId")));
        filters.setEventRequestId(DashboardUtil.parseIntSafe(request.getParameter("eventRequestId")));

        // Lookup names for display in filter tags
        try {
            if (filters.getDepartmentId() != null) {
                filters.setDepartmentName(dao.getDepartmentName(filters.getDepartmentId()));
            }
            if (filters.getCategoryId() != null) {
                filters.setCategoryName(dao.getCategoryName(filters.getCategoryId()));
            }
        } catch (final Exception e) {
            // Log but don't fail - names are just for display
            log("Error looking up filter names: " + e.getMessage());
        }

        // View type (for KPI drill-down)
        filters.setView(request.getParameter("view"));

        // Pagination
        filters.setPage(DashboardUtil.parseIntWithDefault(request.getParameter("page"), 1));
        filters.setPageSize(DashboardUtil.parseIntWithDefault(request.getParameter("pageSize"), DEFAULT_PAGE_SIZE));

        // Sorting
        final String sortColumn = request.getParameter("sortColumn");
        final String sortDirection = request.getParameter("sortDirection");
        if (sortColumn != null && !sortColumn.isEmpty()) {
            filters.setSortColumn(sortColumn);
        }
        if (sortDirection != null && !sortDirection.isEmpty()) {
            filters.setSortDirection(sortDirection);
        }

        return filters;
    }

    /**
     * Get status options for dropdown
     */
    private List<Map<String, String>> getStatusOptions() {
        final List<Map<String, String>> options = new ArrayList<>();

        final String[][] statuses = {
                {"OPEN", "Open"},
                {"PENDING", "Pending"},
                {"COMPLETED", "Completed"},
                {"REJECTED", "Rejected"}
        };

        for (final String[] status : statuses) {
            final Map<String, String> opt = new HashMap<>();
            opt.put("value", status[0]);
            opt.put("label", status[1]);
            options.add(opt);
        }

        return options;
    }

    @Override
    protected boolean isAjaxRequest(final HttpServletRequest request) {
        return "XMLHttpRequest".equals(request.getHeader("X-Requested-With"));
    }

    @Override
    protected void sendJsonSuccess(final HttpServletResponse response, final String message, final Object data)
            throws IOException {
        JsonResponse.sendSuccess(response, message, data);
    }

    @Override
    protected void sendJsonError(final HttpServletResponse response, final String message)
            throws IOException {
        JsonResponse.sendError(response, message);
    }

    /**
     * Escape CSV field value
     */
    private String escapeCSV(final String value) {
        if (value == null) {
            return "";
        }
        // If contains comma, newline, or quote, wrap in quotes and escape existing quotes
        if (value.contains(",") || value.contains("\n") || value.contains("\"")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }
}
