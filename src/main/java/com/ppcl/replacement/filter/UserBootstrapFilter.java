package com.ppcl.replacement.filter;

import com.ppcl.replacement.dao.UserDAO;
import com.ppcl.replacement.model.User;

import jakarta.servlet.*;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.sql.SQLException;

@WebFilter("/views/*")
public class UserBootstrapFilter implements Filter {

    private UserDAO userDAO;

    @Override
    public void init(FilterConfig filterConfig) {
        this.userDAO = new UserDAO();
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response,
                         FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest req  = (HttpServletRequest) request;
        HttpServletResponse resp = (HttpServletResponse) response;
        HttpSession session = req.getSession(false);

        // No session → block
        if (session == null) {
            resp.sendError(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        //  CASE 1: Logged in via YOUR LoginServlet
        if (session.getAttribute("userId") != null) {
            chain.doFilter(request, response);
            return;
        }

        //  CASE 2: Logged in via CLIENT app
        String usrId = (String) session.getAttribute("AcrmYUserNmK");
        if (usrId == null || usrId.trim().isEmpty()) {
            resp.sendError(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        long userId = Long.parseLong((String)session.getAttribute("AcrmYUsernID"));

        // Bootstrap only once
        User user = null;
        try {
            user = userDAO.getUserByUserName(usrId);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        if (user == null) {
            resp.sendError(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        // === SAME ATTRIBUTES AS LoginServlet ===
        session.setAttribute("userId", user.getId());
        session.setAttribute("userName", user.getName());
        session.setAttribute("userRole", user.getRole());
        session.setAttribute("currentUser", user);
        try {
        session.setAttribute("isCRO", userDAO.isCRO(user.getId()));
        session.setAttribute("isTLSupport", userDAO.isTLSupport(user.getId()));
        session.setAttribute("isRoleForCourierLoginValid", userDAO.isRoleForCourierLoginValid(user.getId()));
        session.setAttribute("isTLOrAbove", userDAO.isTLOrAbove(user.getId()));
        session.setAttribute("isTLLead", userDAO.isTLLead(user.getId()));
        session.setAttribute("isAMManager", userDAO.isAMManager(user.getId()));
        session.setAttribute("isAccountBillingUser", userDAO.isAccountBillingUser(user.getId()));
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        session.setMaxInactiveInterval(30 * 60);

        chain.doFilter(request, response);
    }

}
