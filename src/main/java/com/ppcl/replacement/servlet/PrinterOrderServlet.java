package com.ppcl.replacement.servlet;

import com.ppcl.replacement.dao.PrinterBookingDAO;
import com.ppcl.replacement.dao.PrinterPullbackDAO;
import com.ppcl.replacement.dao.TransitionWorkflowDao;
import com.ppcl.replacement.dao.UserDAO;
import com.ppcl.replacement.model.User;
import com.ppcl.replacement.util.JsonResponse;
import com.ppcl.replacement.util.ServletUtil;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Servlet for handling Printer Order booking from myRequests.jsp
 * Uses PrinterBookingDAO with PRINTER_ORDER and PRINTER_ORDER_ITEM tables (01_ddl.sql)
 */
@WebServlet("/views/replacement/myRequests")
public class PrinterOrderServlet extends HttpServlet {

    private final PrinterBookingDAO printerBookingDAO = new PrinterBookingDAO();
    private final PrinterPullbackDAO printerPullbackDAO = new PrinterPullbackDAO();
    private final UserDAO userDAO = new UserDAO();
    private final TransitionWorkflowDao transitionWorkflowDao = new TransitionWorkflowDao();

    @Override
    protected void doPost(final HttpServletRequest request, final HttpServletResponse response)
            throws ServletException, IOException {

        final String action = ServletUtil.optionalParam(request, "action");

        try {
            if ("bookOrder".equals(action)) {
                handleBookOrder(request, response);
            } else if ("approvalReceived".equals(action)) {
                handleApprovalReceived(request, response);
            } else {
                JsonResponse.sendError(response, "Unknown action: " + action);
            }
        } catch (final IllegalArgumentException e) {
            JsonResponse.sendError(response, e.getMessage());
        } catch (final Exception e) {
            e.printStackTrace();
            JsonResponse.sendError(response, "Error: " + e.getMessage());
        }
    }

    /**
     * Handle Book Printer Order action (STG6_PRINTER_ORDER)
     */
    private void handleBookOrder(final HttpServletRequest request, final HttpServletResponse response)
            throws Exception {

        final HttpSession session = request.getSession();
        final User user = (User) session.getAttribute("currentUser");
        final Object userIdObj = session.getAttribute("userId");
        final int userId = (userIdObj instanceof Number) ? ((Number) userIdObj).intValue() : 0;

        if (user == null && userId == 0) {
            throw new IllegalArgumentException("Session expired. Please login again.");
        }

        final int reqId = ServletUtil.requireIntParam(request, "reqId");
        final String signatory = ServletUtil.optionalParam(request, "signatory");
        final String orderItemsJson = ServletUtil.optionalParam(request, "orderItems");

        final String orderBy = user != null ? user.getName() : String.valueOf(userId);
        final int empId = userDAO.getEmpIdUsingUserId(user.getId());

        final int orderId = printerBookingDAO.bookPrinterOrderSimple(
                reqId, orderBy, user, empId, signatory, orderItemsJson
        );

        // After successful printer booking, process printer pullback
        processPrinterPullbackAfterBooking(reqId);

        final Map<String, Object> data = new HashMap<>();
        data.put("orderId", "PO-" + orderId);
        JsonResponse.sendSuccess(response, "Printer order booked successfully", data);
    }

    /**
     * Invokes the printer pullback processing after a successful printer booking.
     * Separated into its own method so pullback logic remains modular and reusable.
     *
     * @param requestId the replacement request ID
     */
    private void processPrinterPullbackAfterBooking(final int requestId) {
        try {
            printerPullbackDAO.processPrinterPullbackAfterBooking(requestId);
        } catch (final Exception e) {
            // Log the error but do not fail the booking response — pullback is a post-booking step
            e.printStackTrace();
            System.err.println("WARNING [PrinterOrderServlet]: Printer pullback processing failed for request ID: "
                    + requestId + ". Error: " + e.getMessage());
        }
    }

    /**
     * Handle Approval Received action (STG5_AM_MANAGER_FINAL → STG6_PRINTER_ORDER)
     */
    private void handleApprovalReceived(final HttpServletRequest request, final HttpServletResponse response)
            throws Exception {

        final HttpSession session = request.getSession();
        final User user = (User) session.getAttribute("currentUser");

        if (user == null) {
            throw new IllegalArgumentException("Session expired. Please login again.");
        }

        final int reqId = ServletUtil.requireIntParam(request, "reqId");
        final String comments = ServletUtil.optionalParam(request, "comments");

        // Transition from stage 5 (STG5_AM_MANAGER_FINAL) to stage 6 (STG6_PRINTER_ORDER)
        transitionWorkflowDao.transitionStage(reqId, 6, user.getId(), comments);

        JsonResponse.sendSuccess(response, "Approval received marked successfully", null);
    }
}
