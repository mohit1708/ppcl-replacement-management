package com.ppcl.replacement.servlet;

import com.ppcl.replacement.dao.MenuDAO;
import com.ppcl.replacement.dao.UserDAO;
import com.ppcl.replacement.model.User;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.*;
import java.io.IOException;

public class LoginServlet extends HttpServlet {

    @Override
    protected void doPost(final HttpServletRequest request, final HttpServletResponse response)
            throws ServletException, IOException {

        String userIdParam = request.getParameter("userId");
        final String password = request.getParameter("password");

        try {
            final UserDAO userDAO = new UserDAO();
            final MenuDAO menuDAO = new MenuDAO();
            User user = null;
            if (userIdParam != null) userIdParam = userIdParam.trim();
            if (userIdParam != null && !userIdParam.isEmpty() && userIdParam.matches("\\d+")) {
                final int userId = Integer.parseInt(userIdParam);
                user = userDAO.getUserByUserId(userId);

            }
            if (user != null) {
                // Get user role
                final String role = user == null ? "AM" : user.getRole();

                // Create session
                final HttpSession session = request.getSession();
                session.setAttribute("userId", user.getId());
                session.setAttribute("userName", user.getName());
                session.setAttribute("userRole", role);
                session.setMaxInactiveInterval(30 * 60); // 30 minutes

                session.setAttribute("currentUser", user);

                // DB-driven menu: load accessible menu items for this user
                session.setAttribute("menuItems", menuDAO.getMenuItemsForUser(user.getId()));

                // Page-level flags still used outside the sidebar (e.g. register.jsp)
                session.setAttribute("isCRO", userDAO.isCRO(user.getId()));
                session.setAttribute("isAccountBillingUser", userDAO.isAccountBillingUser(user.getId()));

                // Redirect to dashboard
                response.sendRedirect(request.getContextPath() + "/views/replacement/dashboard");
            } else {
                response.sendRedirect(request.getContextPath() + "/login.jsp?error=invalid");
            }
        } catch (final Exception e) {
            e.printStackTrace();
            response.sendRedirect(request.getContextPath() + "/login.jsp?error=system");
        }
    }

    @Override
    protected void doGet(final HttpServletRequest request, final HttpServletResponse response)
            throws ServletException, IOException {
        response.sendRedirect(request.getContextPath() + "/login.jsp");
    }
}
