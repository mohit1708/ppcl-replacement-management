package com.ppcl.replacement.servlet;

import com.ppcl.replacement.util.JsonResponse;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;

public abstract class BaseServlet extends HttpServlet {

    protected static final String JSP_BASE = "/WEB-INF/jsp/replacement/";

    protected void forwardToJsp(HttpServletRequest request, HttpServletResponse response, String jspPath) throws ServletException, IOException {
        request.getRequestDispatcher(JSP_BASE + jspPath).forward(request, response);
    }

    protected void sendJsonSuccess(HttpServletResponse response, String message, Object data) throws IOException {
        JsonResponse.sendSuccess(response, message, data);
    }

    protected void sendJsonError(HttpServletResponse response, String message) throws IOException {
        JsonResponse.sendError(response, message);
    }

    protected boolean isAjaxRequest(HttpServletRequest request) {
        String requestedWith = request.getHeader("X-Requested-With");
        return "XMLHttpRequest".equals(requestedWith) || "true".equals(request.getParameter("ajax"));
    }

    protected void handleError(HttpServletRequest request, HttpServletResponse response, Exception e) throws ServletException, IOException {
        e.printStackTrace();
        if (isAjaxRequest(request)) {
            sendJsonError(response, e.getMessage());
        } else {
            request.setAttribute("error", e.getMessage());
            request.getRequestDispatcher("/error.jsp").forward(request, response);
        }
    }

    protected int getSessionUserId(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session == null) return 0;
        Object val = session.getAttribute("userId");
        if (val instanceof Number) return ((Number) val).intValue();
        if (val instanceof String) return Integer.parseInt((String) val);
        return 0;
    }

    protected int getIntParameter(HttpServletRequest request, String name) {
        String val = request.getParameter(name);
        if (val == null || val.trim().isEmpty()) {
            return 0;
        }
        try {
            return Integer.parseInt(val);
        } catch (NumberFormatException e) {
            return 0;
        }
    }
}
