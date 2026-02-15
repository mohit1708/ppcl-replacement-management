package com.ppcl.replacement.servlet;

import com.ppcl.replacement.dao.CourierLoginDAO;
import com.ppcl.replacement.model.PrinterPullback;
import com.ppcl.replacement.util.ValidateSesSecurity;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

/**
 * Displays assigned pullback pickups for the logged-in courier.
 * Requires a valid courier session; redirects to login if not authenticated.
 */
@WebServlet(urlPatterns = {"/CourierAssigned.do"})
public class CourierAssignedServlet extends BaseServlet {

    private final CourierLoginDAO courierLoginDAO = new CourierLoginDAO();

    @Override
    protected void doGet(final HttpServletRequest request, final HttpServletResponse response)
            throws ServletException, IOException {

        // redirect to login if courier session is not active
        if (!ValidateSesSecurity.isCourierSessionValid(request)) {
            response.sendRedirect(request.getContextPath() + "/CourierLoginOtp.do");
            return;
        }

        try {
            final int courierId = ValidateSesSecurity.getCourierId(request);
            final String courierName = ValidateSesSecurity.getCourierName(request);

            List<PrinterPullback> pullbacks = courierLoginDAO.getAssignedPullbacks(courierId);

            request.setAttribute("pullbacks", pullbacks);
            request.setAttribute("courierName", courierName);

            forwardToJsp(request, response, "courier/courierAssigned.jsp");

        } catch (final Exception e) {
            handleError(request, response, e);
        }
    }
}
