package com.ppcl.replacement.servlet;

import com.ppcl.replacement.dao.PrinterBookingDAO;
import com.ppcl.replacement.dao.PrinterPullbackDAO;
import com.ppcl.replacement.dao.ReplacementRequestDAO;
import com.ppcl.replacement.dao.UserDAO;
import com.ppcl.replacement.model.*;
import com.ppcl.replacement.util.JsonResponse;
import com.ppcl.replacement.util.ServletUtil;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.util.*;

@WebServlet(urlPatterns = {
        "/views/replacement/am/printerBooking",
        "/views/replacement/am/bookPrinterOrder"
})
public class PrinterBookingServlet extends HttpServlet {

    private final PrinterBookingDAO dao = new PrinterBookingDAO();
    private final PrinterPullbackDAO printerPullbackDAO = new PrinterPullbackDAO();
    private final ReplacementRequestDAO replacementRequestDAO = new ReplacementRequestDAO();
    private final UserDAO userDAO = new UserDAO();

    @Override
    protected void doGet(final HttpServletRequest request, final HttpServletResponse response)
            throws ServletException, IOException {

        final String servletPath = request.getServletPath();
        final Integer userId = (Integer) request.getSession().getAttribute("userId");

        try {
            if (servletPath.endsWith("/printerBooking")) {
                showPrinterBooking(request, response, userId);
            } else {
                response.sendError(HttpServletResponse.SC_NOT_FOUND);
            }
        } catch (final IllegalArgumentException e) {
            request.setAttribute("errorMessage", e.getMessage());
            request.getRequestDispatcher(BaseServlet.JSP_BASE + "am/printerBooking.jsp").forward(request, response);
        } catch (final Exception e) {
            e.printStackTrace();
            request.setAttribute("errorMessage", "Error: " + e.getMessage());
            request.getRequestDispatcher(BaseServlet.JSP_BASE + "am/printerBooking.jsp").forward(request, response);
        }
    }

    @Override
    protected void doPost(final HttpServletRequest request, final HttpServletResponse response)
            throws ServletException, IOException {

        final String servletPath = request.getServletPath();

        try {
            if (servletPath.endsWith("/bookPrinterOrder")) {
                bookPrinterOrder(request, response);
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

    private void showPrinterBooking(final HttpServletRequest request, final HttpServletResponse response,
                                    final Integer userId) throws Exception {

        final String reqIdParam = request.getParameter("reqId");

        // If no reqId, show list of pending printer booking requests
        if (reqIdParam == null || reqIdParam.isEmpty()) {
            final List<Map<String, Object>> pendingRequests = dao.getPendingPrinterBookingRequests();
            request.setAttribute("pendingRequests", pendingRequests);
            request.setAttribute("showList", true);
            request.getRequestDispatcher(BaseServlet.JSP_BASE + "am/printerBookingList.jsp").forward(request, response);
            return;
        }

        final int reqId = Integer.parseInt(reqIdParam);

        final ReplacementRequest replacementRequest = replacementRequestDAO.getRequestById(reqId);
        if (replacementRequest == null) {
            throw new IllegalArgumentException("Replacement request not found");
        }

        final Map<String, Object> printerData = dao.getPrinterDetails(reqId);
        final List<User> signatories = dao.getSignatories(userId);

        final List<?> printers = (List<?>) printerData.get("printers");
        final int totalPrinters = printers != null ? printers.size() : 0;

        request.setAttribute("replacementRequest", replacementRequest);
        request.setAttribute("printerData", printerData);
        request.setAttribute("signatories", signatories);
        request.setAttribute("totalPrinters", totalPrinters);

        request.getRequestDispatcher(BaseServlet.JSP_BASE + "am/printerBooking.jsp").forward(request, response);
    }

    private void bookPrinterOrder(final HttpServletRequest request, final HttpServletResponse response)
            throws Exception {

        final int requestId = ServletUtil.requireIntParam(request, "requestId");
        final int signatoryId = ServletUtil.requireIntParam(request, "signatoryId");

        final HttpSession session = request.getSession();
        final User user = (User) session.getAttribute("currentUser");

        if (user == null) {
            throw new IllegalArgumentException("Session expired. Please login again.");
        }

        final int empId = userDAO.getEmpIdUsingUserId(user.getId());
        final String orderBy = user.getName();

        final List<OrderItemSubmission> orderItems = parseOrderItems(request);

        final int orderId = dao.bookPrinterOrderSimple(requestId, orderBy, user, empId, String.valueOf(signatoryId), null);

        // After successful printer booking, process printer pullback
        processPrinterPullbackAfterBooking(requestId);

        final Map<String, Object> data = new HashMap<>();
        data.put("orderId", "PO-" + orderId);
        JsonResponse.sendSuccess(response, "Printer order booked successfully. Order ID: PO-" + orderId, data);
    }

    /**
     * Invokes the printer pullback processing after a successful printer booking.
     * This is a separate method so that pullback logic remains modular and can be
     * reused or modified independently of the booking logic.
     *
     * @param requestId the replacement request ID
     */
    private void processPrinterPullbackAfterBooking(final int requestId) {
        try {
            printerPullbackDAO.processPrinterPullbackAfterBooking(requestId);
        } catch (final Exception e) {
            // Log the error but do not fail the booking response — pullback is a post-booking step
            e.printStackTrace();
            System.err.println("WARNING [PrinterBookingServlet]: Printer pullback processing failed for request ID: "
                    + requestId + ". Error: " + e.getMessage());
        }
    }

    private List<OrderItemSubmission> parseOrderItems(final HttpServletRequest request) {
        final List<OrderItemSubmission> items = new ArrayList<>();

        final Enumeration<String> paramNames = request.getParameterNames();
        final Map<Integer, OrderItemSubmission> itemMap = new TreeMap<>();

        while (paramNames.hasMoreElements()) {
            final String paramName = paramNames.nextElement();

            if (paramName.startsWith("items[") && paramName.endsWith("]")) {
                final String[] parts = paramName.split("\\[|\\]");
                final int index = Integer.parseInt(parts[1]);
                final String field = parts[2].substring(1);
                final String value = request.getParameter(paramName);

                final OrderItemSubmission item = itemMap.computeIfAbsent(index, k -> new OrderItemSubmission());

                switch (field) {
                    case "locationId" -> item.setLocationId(Integer.parseInt(value));
                    case "printerModelId" -> item.setPrinterModelId(Integer.parseInt(value));
                    case "quantity" -> item.setQuantity(Integer.parseInt(value));
                    case "printerType" -> item.setPrinterType(value);
                    case "dispatchBranchId" -> item.setDispatchBranchId(Integer.parseInt(value));
                    case "deliveryDate" -> item.setDeliveryDate(value);
                    case "printerPrice" -> item.setPrinterPrice(Double.parseDouble(value));
                    case "contactPerson" -> item.setContactPerson(value);
                    case "contactNumber" -> item.setContactNumber(value);
                    case "cartridgePickup" -> item.setCartridgePickup(value);
                    case "pickupQuantity" -> item.setPickupQuantity(Integer.parseInt(value));
                    case "sendCartridge" -> item.setSendCartridge(value);
                    case "cartridgeQuantity" -> item.setCartridgeQuantity(Integer.parseInt(value));
                    case "installation" -> item.setInstallation(value);
                }
            }
        }

        items.addAll(itemMap.values());
        return items;
    }
}
