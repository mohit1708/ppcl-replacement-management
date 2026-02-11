package com.ppcl.replacement.servlet;

import com.ppcl.replacement.dao.ReplacementTLDAO;
import com.ppcl.replacement.dao.UserDAO;
import com.ppcl.replacement.model.*;
import com.ppcl.replacement.util.JsonResponse;
import com.ppcl.replacement.util.ServletUtil;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.util.*;

/**
 * Servlet for Service TL operations
 * Uses UserDAO.getTLLeadList() to get TL info
 */
@WebServlet(urlPatterns = {
        "/views/replacement/tl/requestList",
        "/views/replacement/tl/getPrinterDetails",
        "/views/replacement/tl/getPrinterHistory",
        "/views/replacement/tl/getFilterData",
        "/views/replacement/tl/updateRecommendation",
        "/views/replacement/tl/action"
})
public class ReplacementTLServlet extends BaseServlet {

    private final ReplacementTLDAO dao = new ReplacementTLDAO();
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
                sendJsonError(response, "Session expired. Please login again.");
            } else {
                response.sendRedirect(request.getContextPath() + "/login.jsp?error=session_expired");
            }
            return;
        }

        try {
            if (servletPath.endsWith("/requestList")) {
                showRequestList(request, response, userId);

            } else if (servletPath.endsWith("/getPrinterDetails")) {
                final int reqId = ServletUtil.requireIntParam(request, "reqId");
                getPrinterDetails(response, reqId);

            } else if (servletPath.endsWith("/getPrinterHistory")) {
                final String serial = ServletUtil.requireParam(request, "serial");
                getPrinterHistory(response, serial);

            } else if (servletPath.endsWith("/getFilterData")) {
                getFilterData(response);

            } else {
                response.sendError(HttpServletResponse.SC_NOT_FOUND);
            }

        } catch (final IllegalArgumentException e) {
            sendJsonError(response, e.getMessage());
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
            sendJsonError(response, "Session expired. Please login again.");
            return;
        }

        try {
            if (servletPath.endsWith("/updateRecommendation")) {
                updateRecommendation(request, response, userId);

            } else if (servletPath.endsWith("/action")) {
                takeAction(request, response, userId);

            } else {
                response.sendError(HttpServletResponse.SC_NOT_FOUND);
            }

        } catch (final IllegalArgumentException e) {
            sendJsonError(response, e.getMessage());
        } catch (final Exception e) {
            e.printStackTrace();
            sendJsonError(response, "Error: " + e.getMessage());
        }
    }

    /**
     * Show TL request list page
     */
    private void showRequestList(final HttpServletRequest request, final HttpServletResponse response,
                                 final int userId) throws Exception {

        List<RequestDetailRow> requests = new ArrayList<>();
        List<RequestDetailRow> completedRequests = new ArrayList<>();
        String errorMessage = null;
        User tlInfo = null;

        try {
            final int loggedInUserAccountId = userId;

            // Get TL info using UserDAO.getTLLeadList()
            tlInfo = userDao.getTLLeadList();

            if (tlInfo != null) {
                // Use logged-in user's account ID for pending/completed queries
                // so forwarded requests appear correctly for the new owner
                requests = dao.getPendingRequestsForTL(loggedInUserAccountId);
                request.setAttribute("tlInfo", tlInfo);

                completedRequests = dao.getCompletedRequestsForTL(loggedInUserAccountId);

                // Get reporting hierarchy for "Forward to Authority" dropdown
                final List<User> reportingHierarchy = Collections.singletonList(userDao.getReportingManager(loggedInUserAccountId));
                request.setAttribute("reportingHierarchy", reportingHierarchy);
            } else {
                errorMessage = "TL user not found in the system";
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

        forwardToJsp(request, response, "tl/requestList.jsp");
    }

    /**
     * AJAX: Get printer details for a request
     */
    private void getPrinterDetails(final HttpServletResponse response, final int reqId) throws Exception {
        final Map<String, Object> data = dao.getPrinterDetails(reqId);
        sendJsonSuccess(response, "Printer details loaded", data);
    }

    /**
     * AJAX: Get printer history (service calls) for a specific serial number
     */
    private void getPrinterHistory(final HttpServletResponse response, final String serial) throws Exception {
        final Map<String, Object> history = dao.getPrinterHistoryBySerial(serial);
        sendJsonSuccess(response, "Printer history loaded", history);
    }

    /**
     * AJAX: Get filter data (AM Managers, Requesters, Printer Models)
     */
    private void getFilterData(final HttpServletResponse response) throws Exception {
        final Map<String, Object> data = dao.getFilterData();
        sendJsonSuccess(response, "Filter data loaded", data);
    }

    /**
     * AJAX: Update TL recommendation for a printer
     */
    private void updateRecommendation(final HttpServletRequest request, final HttpServletResponse response,
                                      final int userId) throws Exception {

        final int printerId = ServletUtil.requireIntParam(request, "printerId");
        final int reqId = ServletUtil.requireIntParam(request, "reqId");
        final Integer newModelId = ServletUtil.optionalIntParam(request, "newModelId");
        final String newModelText = ServletUtil.optionalParam(request, "newModelText");
        final String printerType = ServletUtil.optionalParam(request, "printerType");
        final String comments = ServletUtil.optionalParam(request, "comments");

        dao.updateTLRecommendation(printerId, reqId, newModelId, newModelText, printerType, comments, userId);
        sendJsonSuccess(response, "Recommendation updated successfully", null);
    }

    /**
     * AJAX: TL takes action (Approve/Reject/Forward)
     */
    private void takeAction(final HttpServletRequest request, final HttpServletResponse response,
                            final int userId) throws Exception {

        final int reqId = ServletUtil.requireIntParam(request, "reqId");
        final String actionType = ServletUtil.requireParam(request, "actionType");
        final String comments = ServletUtil.optionalParam(request, "comments");
        final Integer forwardToUserId = ServletUtil.optionalIntParam(request, "forwardTo");

        dao.takeAction(reqId, userId, actionType, comments, forwardToUserId);

        final String message = switch (actionType) {
            case "APPROVE" -> "Request approved and forwarded to AM Manager";
            case "REJECT" -> "Request rejected";
            case "FORWARD" -> "Request forwarded to higher authority";
            default -> "Action completed successfully";
        };

        sendJsonSuccess(response, message, null);
    }

    protected void handleError(final HttpServletRequest request, final HttpServletResponse response,
                               final Exception e) throws ServletException, IOException {

        if (isAjaxRequest(request)) {
            sendJsonError(response, "Error: " + e.getMessage());
        } else {
            request.setAttribute("errorMessage", "System Error: " + e.getMessage());
            request.setAttribute("hasError", true);
            request.setAttribute("requests", new ArrayList<>());
            forwardToJsp(request, response, "tl/requestList.jsp");
        }
    }
}
