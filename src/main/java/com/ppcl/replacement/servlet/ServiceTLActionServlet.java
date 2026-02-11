package com.ppcl.replacement.servlet;

import com.ppcl.replacement.constants.StageConstants;
import com.ppcl.replacement.dao.ReplacementRequestDAO;
import com.ppcl.replacement.model.ReplacementRequest;
import com.ppcl.replacement.util.JsonResponse;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.util.List;

public class ServiceTLActionServlet extends BaseServlet {

    @Override
    protected void doGet(final HttpServletRequest request, final HttpServletResponse response)
            throws ServletException, IOException {

        final String action = request.getParameter("action");

        try {
            if ("list".equals(action)) {
                showPendingList(request, response);
            } else if ("getDetails".equals(action)) {
                getRequestDetails(request, response);
            } else {
                response.sendRedirect(request.getContextPath() + "/views/replacement/dashboard");
            }
        } catch (final Exception e) {
            handleError(request, response, e);
        }
    }

    @Override
    protected void doPost(final HttpServletRequest request, final HttpServletResponse response)
            throws ServletException, IOException {

        final String action = request.getParameter("action");
        final HttpSession session = request.getSession();
        final int userId = getSessionUserId(request);

        try {
            final int reqId = Integer.parseInt(request.getParameter("requestId"));
            final String comment = request.getParameter("comment");

            final ReplacementRequestDAO dao = new ReplacementRequestDAO();

            if ("approve".equals(action)) {
                // Move to AM Manager Review
                dao.updateRequestStage(reqId, StageConstants.AM_MANAGER_REVIEW, "AM_MANAGER",
                        userId, "APPROVE", "Approved by Service TL: " + comment);

                response.sendRedirect(request.getContextPath() + "/views/replacement/servicetl/action?action=list&success=approved");

            } else if ("reject".equals(action)) {
                // Reject request
                dao.updateRequestStage(reqId, StageConstants.REJECTED, "CRO",
                        userId, "REJECT", comment);

                response.sendRedirect(request.getContextPath() + "/views/replacement/servicetl/action?action=list&success=rejected");

            } else if ("forward".equals(action)) {
                final String forwardToRole = request.getParameter("forwardToRole");
                dao.updateRequestStage(reqId, StageConstants.SERVICE_TL_REVIEW, forwardToRole,
                        userId, "FORWARD", comment);

                response.sendRedirect(request.getContextPath() + "/views/replacement/servicetl/action?action=list&success=forwarded");
            }

        } catch (final Exception e) {
            e.printStackTrace();
            response.sendRedirect(request.getContextPath() + "/views/replacement/servicetl/action?action=list&error=failed");
        }
    }

    private void showPendingList(final HttpServletRequest request, final HttpServletResponse response)
            throws Exception {

        final ReplacementRequestDAO dao = new ReplacementRequestDAO();
        final List<ReplacementRequest> pendingRequests = dao.getRequestsByRole("SERVICE_TL");

        request.setAttribute("pendingRequests", pendingRequests);
        forwardToJsp(request, response, "servicetl/list.jsp");
    }

    private void getRequestDetails(final HttpServletRequest request, final HttpServletResponse response)
            throws Exception {

        final int reqId = Integer.parseInt(request.getParameter("requestId"));
        final ReplacementRequestDAO dao = new ReplacementRequestDAO();
        final ReplacementRequest req = dao.getRequestById(reqId);

        sendJsonSuccess(response, "Success", req);
    }
}
