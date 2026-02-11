package com.ppcl.replacement.servlet;

import com.google.gson.Gson;
import com.ppcl.replacement.dao.PrinterPullbackDAO;
import com.ppcl.replacement.model.PrinterPullback;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@WebServlet(urlPatterns = {
        "/views/replacement/logistics/pullbackManagement",
        "/views/wm/markReceived"
})
public class PrinterPullbackServlet extends HttpServlet {

    private final Gson gson = new Gson();
    private final PrinterPullbackDAO dao = new PrinterPullbackDAO();

    @Override
    protected void doGet(final HttpServletRequest request, final HttpServletResponse response)
            throws ServletException, IOException {

        final String servletPath = request.getServletPath();

        try {
            if (servletPath.endsWith("/pullbackManagement")) {
                showPullbackList(request, response);
            } else {
                response.sendError(HttpServletResponse.SC_NOT_FOUND);
            }
        } catch (final Exception e) {
            e.printStackTrace();
            request.setAttribute("errorMessage", "Error: " + e.getMessage());

            request.getRequestDispatcher(BaseServlet.JSP_BASE + "logistics/pullbackManagement.jsp").forward(request, response);
        }
    }

    @Override
    protected void doPost(final HttpServletRequest request, final HttpServletResponse response)
            throws ServletException, IOException {

        final String servletPath = request.getServletPath();

        try {
            if (servletPath.endsWith("/markReceived")) {
                markAsReceived(request, response);
            } else {
                response.sendError(HttpServletResponse.SC_NOT_FOUND);
            }
        } catch (final Exception e) {
            e.printStackTrace();
            sendJsonError(response, "Error: " + e.getMessage());
        }
    }

    private void showPullbackList(final HttpServletRequest request, final HttpServletResponse response)
            throws Exception {

        final List<PrinterPullback> pullbacks = dao.getPendingPullbacks();
        final int pendingCount = dao.getPendingCount();

        request.setAttribute("pullbacks", pullbacks);
        request.setAttribute("pendingCount", pendingCount);

        request.getRequestDispatcher(BaseServlet.JSP_BASE + "logistics/pullbackManagement.jsp").forward(request, response);
    }

    private void markAsReceived(final HttpServletRequest request, final HttpServletResponse response)
            throws Exception {

        final String idStr = request.getParameter("id");
        if (idStr == null || idStr.isEmpty()) {
            throw new Exception("Pullback ID is required");
        }

        final int id = Integer.parseInt(idStr);
        final String comments = request.getParameter("comments");
        final int printer = "1".equals(request.getParameter("printer")) ? 1 : 0;
        final int powerCable = "1".equals(request.getParameter("powerCable")) ? 1 : 0;
        final int lanCable = "1".equals(request.getParameter("lanCable")) ? 1 : 0;
        final int tray = "1".equals(request.getParameter("tray")) ? 1 : 0;

        Integer emptyCartridge = null;
        Integer unusedCartridge = null;
        final String emptyStr = request.getParameter("emptyCartridge");
        final String unusedStr = request.getParameter("unusedCartridge");
        if (emptyStr != null && !emptyStr.isEmpty()) {
            emptyCartridge = Integer.parseInt(emptyStr);
        }
        if (unusedStr != null && !unusedStr.isEmpty()) {
            unusedCartridge = Integer.parseInt(unusedStr);
        }

        dao.markAsReceived(id, comments, printer, powerCable, lanCable, tray, emptyCartridge, unusedCartridge);

        sendJsonSuccess(response, "Printer marked as received successfully", null);
    }

    private void sendJsonSuccess(final HttpServletResponse response, final String message, final Object data)
            throws IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        final Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("message", message);
        result.put("data", data);

        final PrintWriter out = response.getWriter();
        out.print(gson.toJson(result));
        out.flush();
    }

    private void sendJsonError(final HttpServletResponse response, final String message)
            throws IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        final Map<String, Object> result = new HashMap<>();
        result.put("success", false);
        result.put("message", message);

        final PrintWriter out = response.getWriter();
        out.print(gson.toJson(result));
        out.flush();
    }
}
