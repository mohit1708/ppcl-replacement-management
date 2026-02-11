package com.ppcl.replacement.servlet;

import com.ppcl.replacement.constants.StageConstants;
import com.ppcl.replacement.dao.ReplacementRequestDAO;
import com.ppcl.replacement.model.ReplacementRequest;
import com.ppcl.replacement.util.JsonResponse;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.util.List;

public class AMActionServlet extends BaseServlet {

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
            final String overallComment = request.getParameter("overallComment");

            final ReplacementRequestDAO dao = new ReplacementRequestDAO();

            if ("review".equals(action)) {
                // Approve commercial review - move to next stage
                dao.updateRequestStage(reqId, StageConstants.QUOTATION_PENDING, "LOGISTICS",
                        userId, "APPROVE", "Commercial review approved: " + overallComment);

                response.sendRedirect(request.getContextPath() + "/views/replacement/am/action?action=list&success=approved");

            } else if ("reject".equals(action)) {
                // Reject commercial review
                dao.updateRequestStage(reqId, StageConstants.REJECTED, "SERVICE_TL",
                        userId, "REJECT", "Commercial review rejected: " + overallComment);

                response.sendRedirect(request.getContextPath() + "/views/replacement/am/action?action=list&success=rejected");
            }

        } catch (final Exception e) {
            e.printStackTrace();
            response.sendRedirect(request.getContextPath() + "/views/replacement/am/action?action=list&error=failed");
        }
    }

    private void showPendingList(final HttpServletRequest request, final HttpServletResponse response)
            throws Exception {

        final ReplacementRequestDAO dao = new ReplacementRequestDAO();
        final List<ReplacementRequest> pendingRequests = dao.getRequestsByRole("AM_MANAGER");

        request.setAttribute("pendingRequests", pendingRequests);
        forwardToJsp(request, response, "am/list.jsp");
    }

    private void getRequestDetails(final HttpServletRequest request, final HttpServletResponse response)
            throws Exception {

        final int reqId = Integer.parseInt(request.getParameter("requestId"));
        final ReplacementRequestDAO dao = new ReplacementRequestDAO();
        final ReplacementRequest req = dao.getRequestById(reqId);

        sendJsonSuccess(response, "Success", req);
    }
}
