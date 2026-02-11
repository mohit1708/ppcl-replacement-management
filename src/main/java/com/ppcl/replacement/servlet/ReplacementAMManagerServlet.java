package com.ppcl.replacement.servlet;

import com.google.gson.JsonObject;
import com.ppcl.replacement.dao.AgrDao;
import com.ppcl.replacement.dao.ReplacementAMManagerDAO;
import com.ppcl.replacement.dao.UserDAO;
import com.ppcl.replacement.model.*;
import com.ppcl.replacement.util.DBConnectionPool;
import com.ppcl.replacement.util.JsonResponse;
import com.ppcl.replacement.util.ServletUtil;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.util.*;

/**
 * Servlet for AM Manager operations
 */
@WebServlet(urlPatterns = {
        "/views/replacement/ammanager/requestList",
        "/views/replacement/ammanager/getFullRequest",
        "/views/replacement/ammanager/getPrinterDetails",
        "/views/replacement/ammanager/getPrinterHistory",
        "/views/replacement/ammanager/getCurrentCommercials",
        "/views/replacement/ammanager/getCommercialDetails",
        "/views/replacement/ammanager/getCommunicationLogs",
        "/views/replacement/ammanager/getHierarchyUsers",
        "/views/replacement/ammanager/getFilterData",
        "/views/replacement/ammanager/replyRequest",
        "/views/replacement/ammanager/rejectRequest",
        "/views/replacement/ammanager/forwardRequest",
        "/views/replacement/ammanager/approveRequest",
        "/views/replacement/ammanager/submitRequest",
        "/views/replacement/ammanager/updateRecommendation",
        "/views/replacement/ammanager/getPrinterModels",
        "/views/replacement/ammanager/getCommercialByRequest"
})
public class ReplacementAMManagerServlet extends BaseServlet {

    private final ReplacementAMManagerDAO dao = new ReplacementAMManagerDAO();
    private final UserDAO userDao = new UserDAO();

    @Override
    protected void doGet(final HttpServletRequest request, final HttpServletResponse response)
            throws ServletException, IOException {

        final String servletPath = request.getServletPath();
        final HttpSession session = request.getSession(false);
        final int userId = getSessionUserId(request);

        // Check if user is logged in
        if (userId == 0) {
            if (isAjaxRequest(request)) {
                JsonResponse.sendError(response, "Session expired. Please login again.");
            } else {
                response.sendRedirect(request.getContextPath() + "/login.jsp?error=session_expired");
            }
            return;
        }

        try {
            if (servletPath.endsWith("/requestList")) {
                showRequestList(request, response, userId);

            } else if (servletPath.endsWith("/getFullRequest")) {
                final int reqId = ServletUtil.requireIntParam(request, "reqId");
                getFullRequest(response, reqId);

            } else if (servletPath.endsWith("/getPrinterDetails")) {
                final int reqId = ServletUtil.requireIntParam(request, "reqId");
                getPrinterDetails(response, reqId);

            } else if (servletPath.endsWith("/getPrinterHistory")) {
                final int agrProdId = ServletUtil.requireIntParam(request, "agrProdId");
                getPrinterHistory(response, agrProdId);

            } else if (servletPath.endsWith("/getCurrentCommercials")) {
                final int reqId = ServletUtil.requireIntParam(request, "reqId");
                getCurrentCommercials(response, reqId);

            } else if (servletPath.endsWith("/getCommercialDetails")) {
                final int printerId = ServletUtil.requireIntParam(request, "printerId");
                getCommercialDetails(response, printerId);

            } else if (servletPath.endsWith("/getCommunicationLogs")) {
                final int reqId = ServletUtil.requireIntParam(request, "reqId");
                getCommunicationLogs(response, reqId);

            } else if (servletPath.endsWith("/getHierarchyUsers")) {
                getHierarchyUsers(response);

            } else if (servletPath.endsWith("/getFilterData")) {
                getFilterData(response);

            } else if (servletPath.endsWith("/getPrinterModels")) {
                getPrinterModels(response);

            } else if (servletPath.endsWith("/getCommercialByRequest")) {
                final int reqId = ServletUtil.requireIntParam(request, "reqId");
                getCommercialByRequest(response, reqId);

            } else {
                response.sendError(HttpServletResponse.SC_NOT_FOUND);
            }

        } catch (final IllegalArgumentException e) {
            JsonResponse.sendError(response, e.getMessage());
        } catch (final Exception e) {
            e.printStackTrace();
            handleError(request, response, e);
        }
    }

    @Override
    protected void doPost(final HttpServletRequest request, final HttpServletResponse response)
            throws ServletException, IOException {

        final String servletPath = request.getServletPath();
        final HttpSession session = request.getSession(false);
        final int userId = getSessionUserId(request);

        // Check if user is logged in
        if (userId == 0) {
            JsonResponse.sendError(response, "Session expired. Please login again.");
            return;
        }

        try {
            if (servletPath.endsWith("/replyRequest")) {
                replyRequest(request, response, userId);

            } else if (servletPath.endsWith("/rejectRequest")) {
                rejectRequest(request, response, userId);

            } else if (servletPath.endsWith("/forwardRequest")) {
                forwardRequest(request, response, userId);

            } else if (servletPath.endsWith("/approveRequest")) {
                approveRequest(request, response, userId);

            } else if (servletPath.endsWith("/submitRequest")) {
                submitRequest(request, response, userId);

            } else if (servletPath.endsWith("/updateRecommendation")) {
                updateRecommendation(request, response, userId);

            } else {
                response.sendError(HttpServletResponse.SC_NOT_FOUND);
            }

        } catch (final IllegalArgumentException e) {
            JsonResponse.sendError(response, e.getMessage());
        } catch (final Exception e) {
            e.printStackTrace();
            JsonResponse.sendError(response, "Error: " + e.getMessage());
        }
    }

    private void showRequestList(final HttpServletRequest request, final HttpServletResponse response,
                                 final int userId) throws Exception {

        List<RequestDetailRow> requests = new ArrayList<>();
        List<RequestDetailRow> completedRequests = new ArrayList<>();
        String errorMessage = null;
        User amManagerInfo = null;
        System.out.print("userId " + userId);

        try {
            final int usrId = userId;
            amManagerInfo = userDao.getUserByUserId(usrId);

            final String dateFrom = ServletUtil.optionalParam(request, "dateFrom");
            final String dateTo = ServletUtil.optionalParam(request, "dateTo");
            final String requester = ServletUtil.optionalParam(request, "requester");
            final String am = ServletUtil.optionalParam(request, "am");
            final String status = ServletUtil.optionalParam(request, "status");

            // Filter by status: if APPROVED, only show completed; if PENDING, only show pending; otherwise show both
            if ("APPROVED".equals(status)) {
                // Only show approved/completed requests
                requests = new ArrayList<>();
                completedRequests = dao.getCompletedRequestsForAMManager(dateFrom, dateTo, requester, am);
            } else if ("PENDING".equals(status)) {
                // Only show pending requests
                requests = dao.getPendingRequestsForAMManager(dateFrom, dateTo, requester, am, status);
                completedRequests = new ArrayList<>();
            } else {
                // Show both
                requests = dao.getPendingRequestsForAMManager(dateFrom, dateTo, requester, am, status);
                completedRequests = dao.getCompletedRequestsForAMManager(dateFrom, dateTo, requester, am);
            }

            final List<Map<String, Object>> requesters = dao.getAllRequesters();
            final List<Map<String, Object>> accountManagers = dao.getAllAccountManagers();

            request.setAttribute("requesters", requesters);
            request.setAttribute("accountManagers", accountManagers);
            request.setAttribute("amManagerInfo", amManagerInfo);

            if (amManagerInfo != null) {
                final List<User> reportingHierarchy = Collections.singletonList(userDao.getReportingManager(usrId));
                request.setAttribute("reportingHierarchy", reportingHierarchy);
            }

        } catch (final Exception e) {
            e.printStackTrace();
            errorMessage = "Unable to load requests: " + e.getMessage();
            requests = new ArrayList<>();
            completedRequests = new ArrayList<>();
        }

        request.setAttribute("requests", requests);
        request.setAttribute("completedRequests", completedRequests);
        request.setAttribute("completedCount", completedRequests.size());
        request.setAttribute("errorMessage", errorMessage);
        request.setAttribute("hasError", errorMessage != null);
        request.setAttribute("pendingCount", requests.size());

        forwardToJsp(request, response, "ammanager/requestList.jsp");
    }

    private void getFullRequest(final HttpServletResponse response, final int reqId) throws Exception {
        final Map<String, Object> data = dao.getFullRequestDetails(reqId);
        JsonResponse.sendSuccess(response, "Full request details loaded", data);
    }

    private void getPrinterDetails(final HttpServletResponse response, final int reqId) throws Exception {
        final Map<String, Object> data = dao.getPrinterDetails(reqId);
        JsonResponse.sendSuccess(response, "Printer details loaded", data);
    }

    private void getPrinterHistory(final HttpServletResponse response, final int agrProdId) throws Exception {
        final Map<String, Object> history = dao.getPrinterHistory(agrProdId);
        JsonResponse.sendSuccess(response, "Printer history loaded", history);
    }

    private void getCurrentCommercials(final HttpServletResponse response, final int reqId) throws Exception {
        final List<Map<String, Object>> commercials = dao.getCurrentCommercials(reqId);
        JsonResponse.sendSuccess(response, "Current commercials loaded", commercials);
    }

    private void getCommercialDetails(final HttpServletResponse response, final int printerId) throws Exception {
        final Map<String, Object> details = dao.getCommercialDetails(printerId);
        JsonResponse.sendSuccess(response, "Commercial details loaded", details);
    }

    private void getCommunicationLogs(final HttpServletResponse response, final int reqId) throws Exception {
        final List<Map<String, Object>> logs = dao.getCommunicationLogs(reqId);
        JsonResponse.sendSuccess(response, "Communication logs loaded", logs);
    }

    private void getHierarchyUsers(final HttpServletResponse response) throws Exception {
        final List<Map<String, String>> users = dao.getHierarchyUsers();
        JsonResponse.sendSuccess(response, "Hierarchy users loaded", users);
    }

    private void getFilterData(final HttpServletResponse response) throws Exception {
        final Map<String, Object> data = new HashMap<>();
        data.put("requesters", dao.getAllRequesters());
        data.put("accountManagers", dao.getAllAccountManagers());
        JsonResponse.sendSuccess(response, "Filter data loaded", data);
    }

    private void getPrinterModels(final HttpServletResponse response) throws Exception {
        final List<Map<String, Object>> models = dao.getAllPrinterModels();
        JsonResponse.sendSuccess(response, "Printer models loaded", models);
    }

    private void replyRequest(final HttpServletRequest request, final HttpServletResponse response,
                              final int userId) throws Exception {
        final int reqId = ServletUtil.requireIntParam(request, "reqId");
        final String comments = ServletUtil.optionalParam(request, "commercialComments");
        final String replaceExisting = ServletUtil.optionalParam(request, "replaceExisting");

        dao.replyRequest(reqId, userId, comments, replaceExisting);
        JsonResponse.sendSuccess(response, "Reply sent to AM successfully", null);
    }

    private void rejectRequest(final HttpServletRequest request, final HttpServletResponse response,
                               final int userId) throws Exception {
        final int reqId = ServletUtil.requireIntParam(request, "reqId");
        final String rejectionReason = ServletUtil.optionalParam(request, "rejectionReason");
        final String comments = ServletUtil.optionalParam(request, "comments");

        dao.rejectRequest(reqId, userId, rejectionReason, comments);
        JsonResponse.sendSuccess(response, "Request rejected successfully", null);
    }

    private void forwardRequest(final HttpServletRequest request, final HttpServletResponse response,
                                final int userId) throws Exception {
        final int reqId = ServletUtil.requireIntParam(request, "reqId");
        final Integer forwardToUserId = ServletUtil.optionalIntParam(request, "forwardTo");
        final String targetRole = ServletUtil.optionalParam(request, "targetRole");
        final String comments = ServletUtil.optionalParam(request, "comments");

        dao.forwardRequest(reqId, userId, forwardToUserId, targetRole, comments);
        JsonResponse.sendSuccess(response, "Request forwarded successfully", null);
    }

    private void approveRequest(final HttpServletRequest request, final HttpServletResponse response,
                                final int userId) throws Exception {

        final JsonObject requestBody = ServletUtil.readJson(request, JsonObject.class);
        final int reqId = requestBody.get("reqId").getAsInt();
        final String overallComments = requestBody.has("overallComments")
                ? requestBody.get("overallComments").getAsString() : "";

        try (final java.sql.Connection conn = DBConnectionPool.getConnection()) {
            conn.setAutoCommit(false);
            try {
                // Process commercial decisions (Continue with Existing Commercial)
                if (requestBody.has("printerDecisions") && requestBody.get("printerDecisions").isJsonArray()) {
                    for (final var element : requestBody.getAsJsonArray("printerDecisions")) {
                        final JsonObject dec = element.getAsJsonObject();
                        final int printerId = dec.get("printerId").getAsInt();
                        final String continueExistingStr = dec.has("continueExisting")
                                ? dec.get("continueExisting").getAsString() : "no";
                        final boolean continueExisting = "yes".equalsIgnoreCase(continueExistingStr);
                        final String comments = dec.has("comments") ? dec.get("comments").getAsString() : "";

                        dao.updateCommercialDecision(conn, printerId, reqId, continueExisting, comments);
                    }
                }

                // Process model recommendations (if any model changes)
                if (requestBody.has("printerRecommendations") && requestBody.get("printerRecommendations").isJsonArray()) {
                    for (final var element : requestBody.getAsJsonArray("printerRecommendations")) {
                        final JsonObject rec = element.getAsJsonObject();
                        final int printerId = rec.get("printerId").getAsInt();
                        final Integer newModelId = rec.has("newModelId") && !rec.get("newModelId").isJsonNull()
                                ? rec.get("newModelId").getAsInt() : null;
                        final String newModelText = rec.has("newModelText") ? rec.get("newModelText").getAsString() : null;
                        final String comments = rec.has("comments") ? rec.get("comments").getAsString() : "Updated by AM Manager";

                        dao.updateRecommendation(conn, printerId, reqId, newModelId, newModelText, comments, userId);
                    }
                }

                dao.approveRequest(conn, reqId, userId, overallComments);
                conn.commit();
            } catch (final Exception e) {
                conn.rollback();
                throw e;
            }
        }

        JsonResponse.sendSuccess(response, "Commercial terms approved. Account Manager has been notified.", null);
    }

    private void submitRequest(final HttpServletRequest request, final HttpServletResponse response,
                               final int userId) throws Exception {
        final int reqId = ServletUtil.requireIntParam(request, "reqId");
        final String comments = ServletUtil.optionalParam(request, "comments");

        dao.submitRequest(reqId, userId, comments);
        JsonResponse.sendSuccess(response, "Request submitted to AM successfully", null);
    }

    private void updateRecommendation(final HttpServletRequest request, final HttpServletResponse response,
                                      final int userId) throws Exception {
        final int printerId = ServletUtil.requireIntParam(request, "printerId");
        final int reqId = ServletUtil.requireIntParam(request, "reqId");
        final Integer newModelId = ServletUtil.optionalIntParam(request, "newModelId");
        final String newModelText = ServletUtil.optionalParam(request, "newModelText");
        final String comments = ServletUtil.optionalParam(request, "comments");

        dao.updateRecommendation(printerId, reqId, newModelId, newModelText, comments, userId);
        JsonResponse.sendSuccess(response, "Recommendation updated successfully", null);
    }

    protected void handleError(final HttpServletRequest request, final HttpServletResponse response,
                               final Exception e) throws ServletException, IOException {

        if (isAjaxRequest(request)) {
            sendJsonError(response, "Error: " + e.getMessage());
        } else {
            request.setAttribute("errorMessage", "System Error: " + e.getMessage());
            request.setAttribute("hasError", true);
            request.setAttribute("requests", new ArrayList<>());
            request.setAttribute("pendingCount", 0);
            forwardToJsp(request, response, "ammanager/requestList.jsp");
        }
    }

    /**
     * Get commercial details per printer using AgrDao.getCommercialUsingRequestId
     */
    private void getCommercialByRequest(final HttpServletResponse response, final int reqId) throws Exception {
        final AgrDao agrDao = new AgrDao();
        try (final java.sql.Connection conn = DBConnectionPool.getConnection()) {
            final List<ReplacementPrinterAgrDTO> commercials = agrDao.getCommercialUsingRequestId(conn, reqId);
            final List<Map<String, Object>> result = new ArrayList<>();
            for (final ReplacementPrinterAgrDTO dto : commercials) {
                final Map<String, Object> item = new HashMap<>();

                // Printer details from DTO
                item.put("printerId", dto.getReplacementPrinterId());
                item.put("serial", dto.getExistingSerial());
                item.put("printerType", dto.getPrinterType());
                item.put("continueExistingCommercial", dto.getContinueExistingCommercial());
                item.put("amCommercialComments", dto.getAmCommercialComments());

                // Agreement details from AgrProd
                final AgrProd ap = dto.getAgrProd();
                if (ap != null) {
                    item.put("agrProdId", ap.getId());
                    item.put("agrNo", ap.getAgrNo());
                    item.put("rent", ap.getRent());
                    item.put("freePrints", ap.getFreePrints());
                    item.put("a4Rate", ap.getA4Rate());
                    item.put("a3Rate", ap.getA3Rate());
                    item.put("a4RatePost", ap.getA4RatePost());
                    item.put("a3RatePost", ap.getA3RatePost());
                    item.put("a4RateColor", ap.getA4RateColor());
                    item.put("a3RateColor", ap.getA3RateColor());
                    item.put("a4RatePostColor", ap.getA4RatePostColor());
                    item.put("a3RatePostColor", ap.getA3RatePostColor());
                    item.put("freeScan", ap.getFreeScan());
                    item.put("scanRate", ap.getScanRate());
                    item.put("scanRatePost", ap.getScanRatePost());
                    item.put("amc", ap.getAmc());
                    item.put("amcType", ap.getAmcType());
                    item.put("pageCommited", ap.getPageCommited());
                    item.put("billingCommited", ap.getBillingCommited());
                    item.put("commitmentPeriod", ap.getCommitmentPeriod());
                    item.put("printerColor", ap.getPrinterColor());
                    item.put("drumUnitFree", ap.getDrumUnitFree());
                    item.put("drumUnitCharge", ap.getDrumUnitCharge());
                    item.put("blackCartRate", ap.getBlackCartRate());
                    item.put("clrCartRate", ap.getClrCartRate());
                    item.put("cartCommited", ap.getCartCommited());
                    item.put("freeA3Black", ap.getFreeA3Black());
                    item.put("freeA4Color", ap.getFreeA4Color());
                    item.put("freeA3Color", ap.getFreeA3Color());
                    item.put("agrCommerceType", ap.getAgrCommerceType());
                }

                item.put("location", dto.getLocation());
                item.put("city", dto.getCity());

                result.add(item);
            }

            JsonResponse.sendSuccess(response, "Commercial details loaded", result);
        }
    }
}
