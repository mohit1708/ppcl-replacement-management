package com.ppcl.replacement.servlet;

import com.ppcl.replacement.dao.CourierLoginDAO;
import com.ppcl.replacement.model.Courier;
import com.ppcl.replacement.util.EncryptionUtil;
import com.ppcl.replacement.util.WhatsAppMessageSender;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.util.List;
import java.util.Random;

/**
 * Admin-side servlet for managing courier login accounts.
 * Allows internal users to generate new logins and resend credentials via WhatsApp.
 *
 * URL patterns:
 *   /views/replacement/courier-login/page     – show management page
 *   /views/replacement/courier-login/list     – AJAX: get courier list
 *   /views/replacement/courier-login/generate – AJAX: generate new login
 *   /views/replacement/courier-login/resend   – AJAX: resend credentials
 */
@WebServlet(urlPatterns = {
        "/views/replacement/courier-login/page",
        "/views/replacement/courier-login/list",
        "/views/replacement/courier-login/generate",
        "/views/replacement/courier-login/resend"
})
public class CourierLoginManagementServlet extends BaseServlet {

    private static final String WHATSAPP_TEMPLATE_ID = "temp_3";

    private final CourierLoginDAO courierLoginDAO = new CourierLoginDAO();

    /* ---------- GET ---------- */

    @Override
    protected void doGet(final HttpServletRequest request, final HttpServletResponse response)
            throws ServletException, IOException {

        if (!isLoggedIn(request)) {
            request.setAttribute("error", "Access Denied. Please login first.");
            forwardToJsp(request, response, "accessDenied.jsp");
            return;
        }

        final String servletPath = request.getServletPath();

        try {
            if (servletPath.endsWith("/page")) {
                forwardToJsp(request, response, "courier/courierLoginManagement.jsp");
            } else if (servletPath.endsWith("/list")) {
                fetchCourierList(request, response);
            } else {
                response.sendError(HttpServletResponse.SC_NOT_FOUND);
            }
        } catch (final Exception e) {
            handleError(request, response, e);
        }
    }

    /* ---------- POST ---------- */

    @Override
    protected void doPost(final HttpServletRequest request, final HttpServletResponse response)
            throws ServletException, IOException {

        if (!isLoggedIn(request)) {
            sendJsonError(response, "Access Denied. Please login first.");
            return;
        }

        final String servletPath = request.getServletPath();

        try {
            if (servletPath.endsWith("/generate")) {
                generateLogin(request, response);
            } else if (servletPath.endsWith("/resend")) {
                resendLogin(request, response);
            } else {
                response.sendError(HttpServletResponse.SC_NOT_FOUND);
            }
        } catch (final Exception e) {
            handleError(request, response, e);
        }
    }

    /* ---- Fetch all couriers with login status (for admin table) ---- */

    private void fetchCourierList(final HttpServletRequest request, final HttpServletResponse response)
            throws Exception {
        final List<Courier> couriers = courierLoginDAO.getAllCouriersWithLoginStatus();
        sendJsonSuccess(response, "Couriers loaded", couriers);
    }

    /* ---- Generate new login: create passcode, save, send via WhatsApp ---- */

    private void generateLogin(final HttpServletRequest request, final HttpServletResponse response)
            throws Exception {

        final int courierId = getIntParameter(request, "courierId");
        if (courierId == 0) {
            sendJsonError(response, "Courier ID is required.");
            return;
        }

        final Courier courier = courierLoginDAO.getCourierById(courierId);
        if (courier == null) {
            sendJsonError(response, "Courier not found.");
            return;
        }

        if (courier.hasLoginCreated()) {
            sendJsonError(response, "Login already exists for this courier. Use 'Resend Login' instead.");
            return;
        }

        if (courier.getMobile() == null || courier.getMobile() == 0) {
            sendJsonError(response, "Courier mobile number is not available. Please update courier details first.");
            return;
        }

        // generate a random 4-digit passcode, encrypt and store
        final String passcode = generatePasscode();
        final String encryptedPassword = EncryptionUtil.encrypt(passcode);

        final boolean saved = courierLoginDAO.setPassword(courierId, encryptedPassword);
        if (!saved) {
            sendJsonError(response, "Failed to create login. Please try again.");
            return;
        }

        // send credentials via WhatsApp (prefixed with country code 91)
        final String mobile = "91" + courier.getMobile();

        WhatsAppMessageSender.sendCourierLoginMessage(
                mobile, WHATSAPP_TEMPLATE_ID, String.valueOf(courierId), passcode);

        sendJsonSuccess(response, "Login generated successfully. Credentials sent via WhatsApp to " + courier.getMobile(), null);
    }

    /* ---- Resend login: decrypt existing password and resend (does NOT overwrite) ---- */

    private void resendLogin(final HttpServletRequest request, final HttpServletResponse response)
            throws Exception {

        final int courierId = getIntParameter(request, "courierId");
        if (courierId == 0) {
            sendJsonError(response, "Courier ID is required.");
            return;
        }

        final Courier courier = courierLoginDAO.getCourierById(courierId);
        if (courier == null) {
            sendJsonError(response, "Courier not found.");
            return;
        }

        if (!courier.hasLoginCreated()) {
            sendJsonError(response, "Login not created yet. Please use Generate Login.");
            return;
        }

        // decrypt the existing password from DB and resend it — do NOT overwrite
        final String existingPassword = EncryptionUtil.decrypt(courier.getPassword());

        final String mobile = "91" + courier.getMobile();

        WhatsAppMessageSender.sendCourierLoginMessage(
                mobile, WHATSAPP_TEMPLATE_ID, String.valueOf(courierId), existingPassword);

        sendJsonSuccess(response, "Existing login details resent via WhatsApp to " + courier.getMobile(), null);
    }

    /* ---- Utility ---- */

    /** Generates a random 4-digit passcode. */
    private String generatePasscode() {
        final Random random = new Random();
        final int code = 1000 + random.nextInt(9000);
        return String.valueOf(code);
    }

    /** Checks if the current user has an active admin session. */
    private boolean isLoggedIn(final HttpServletRequest request) {
        final HttpSession session = request.getSession(false);
        return session != null && session.getAttribute("userId") != null;
    }
}
