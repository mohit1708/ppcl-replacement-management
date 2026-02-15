package com.ppcl.replacement.servlet;

import com.ppcl.replacement.dao.CourierLoginDAO;
import com.ppcl.replacement.model.Courier;
import com.ppcl.replacement.util.EncryptionUtil;
import com.ppcl.replacement.util.ValidateSesSecurity;
import com.ppcl.replacement.util.WhatsAppMessageSender;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.Timestamp;
import java.util.Random;

/**
 * Courier-facing servlet handling login, forgot-password (OTP flow),
 * password reset, first-login password change, remember-me and logout.
 *
 * URL patterns:
 *   /CourierLoginOtp.do      – login page & form submit
 *   /CourierForgotPassword.do – enter mobile to receive OTP
 *   /CourierVerifyOtp.do      – verify the OTP
 *   /CourierResetPassword.do  – set new password after OTP verified
 *   /CourierUpdatePassword.do – first-login mandatory password change
 *   /CourierLogout.do         – logout & clear cookies
 */
@WebServlet(urlPatterns = {
        "/CourierLoginOtp.do",
        "/CourierForgotPassword.do",
        "/CourierVerifyOtp.do",
        "/CourierResetPassword.do",
        "/CourierUpdatePassword.do",
        "/CourierLogout.do"
})
public class CourierLoginServlet extends BaseServlet {

    private static final String WHATSAPP_TEMPLATE_ID = "temp_3";
    private static final int MAX_OTP_ATTEMPTS = 3;
    private static final String REMEMBER_COOKIE = "courier_remember";
    private static final int REMEMBER_DAYS = 30;

    private final CourierLoginDAO courierLoginDAO = new CourierLoginDAO();

    /* ---------- GET requests ---------- */

    @Override
    protected void doGet(final HttpServletRequest request, final HttpServletResponse response)
            throws ServletException, IOException {

        final String servletPath = request.getServletPath();

        try {
            switch (servletPath) {
                case "/CourierLoginOtp.do" -> showLoginPage(request, response);
                case "/CourierForgotPassword.do" -> showForgotPasswordPage(request, response);
                case "/CourierVerifyOtp.do" -> showOtpVerificationPage(request, response);
                case "/CourierResetPassword.do" -> showResetPasswordPage(request, response);
                case "/CourierUpdatePassword.do" -> showUpdatePasswordPage(request, response);
                case "/CourierLogout.do" -> handleLogout(request, response);
                default -> response.sendError(HttpServletResponse.SC_NOT_FOUND);
            }
        } catch (final Exception e) {
            e.printStackTrace();
            request.setAttribute("error", "An unexpected error occurred. Please try again.");
            forwardToJsp(request, response, "courier/courierLogin.jsp");
        }
    }

    /* ---------- POST requests ---------- */

    @Override
    protected void doPost(final HttpServletRequest request, final HttpServletResponse response)
            throws ServletException, IOException {

        final String servletPath = request.getServletPath();

        try {
            switch (servletPath) {
                case "/CourierLoginOtp.do" -> handleLogin(request, response);
                case "/CourierForgotPassword.do" -> handleForgotPassword(request, response);
                case "/CourierVerifyOtp.do" -> handleOtpVerification(request, response);
                case "/CourierResetPassword.do" -> handleResetPassword(request, response);
                case "/CourierUpdatePassword.do" -> handleUpdatePassword(request, response);
                default -> response.sendError(HttpServletResponse.SC_NOT_FOUND);
            }
        } catch (final Exception e) {
            e.printStackTrace();
            request.setAttribute("error", "An unexpected error occurred. Please try again.");
            forwardToJsp(request, response, "courier/courierLogin.jsp");
        }
    }

    /* ================================================================
     *  LOGIN
     * ================================================================ */

    /** Shows login page; redirects if session is already active or remember-me cookie is valid. */
    private void showLoginPage(final HttpServletRequest request, final HttpServletResponse response)
            throws Exception {

        // already logged in → go straight to assigned pickups
        if (ValidateSesSecurity.isCourierSessionValid(request)) {
            response.sendRedirect(request.getContextPath() + "/CourierAssigned.do");
            return;
        }

        // try auto-login from remember-me cookie
        if (tryAutoLogin(request, response)) {
            return;
        }

        // pass encrypted courier ID if present (from WhatsApp link)
        final String encryptedCourierId = request.getParameter("courierId");
        if (encryptedCourierId != null && !encryptedCourierId.trim().isEmpty()) {
            request.setAttribute("encryptedCourierId", encryptedCourierId);
        }
        forwardToJsp(request, response, "courier/courierLogin.jsp");
    }

    /** Validates mobile + password, creates session and sets remember-me cookie if checked. */
    private void handleLogin(final HttpServletRequest request, final HttpServletResponse response)
            throws Exception {

        final String mobileNumber = request.getParameter("mobileNumber");
        final String password = request.getParameter("password");
        final String encryptedCourierId = request.getParameter("encryptedCourierId");
        final String rememberMe = request.getParameter("rememberMe");

        // basic validation
        if (mobileNumber == null || mobileNumber.trim().isEmpty()) {
            request.setAttribute("error", "Mobile number is required.");
            request.setAttribute("encryptedCourierId", encryptedCourierId);
            forwardToJsp(request, response, "courier/courierLogin.jsp");
            return;
        }
        if (password == null || password.trim().isEmpty()) {
            request.setAttribute("error", "Password is required.");
            request.setAttribute("encryptedCourierId", encryptedCourierId);
            request.setAttribute("mobileNumber", mobileNumber);
            forwardToJsp(request, response, "courier/courierLogin.jsp");
            return;
        }

        final long mobile;
        try {
            mobile = Long.parseLong(mobileNumber.trim());
        } catch (final NumberFormatException e) {
            request.setAttribute("error", "Invalid mobile number format.");
            request.setAttribute("encryptedCourierId", encryptedCourierId);
            forwardToJsp(request, response, "courier/courierLogin.jsp");
            return;
        }

        final Courier courier = courierLoginDAO.getCourierByMobile(mobile);
        if (courier == null || !courier.hasLoginCreated()) {
            request.setAttribute("error", "Invalid mobile number. No account found.");
            request.setAttribute("encryptedCourierId", encryptedCourierId);
            forwardToJsp(request, response, "courier/courierLogin.jsp");
            return;
        }

        if (!courier.isAccountActive()) {
            request.setAttribute("error", "Your account is inactive. Please contact admin.");
            request.setAttribute("encryptedCourierId", encryptedCourierId);
            forwardToJsp(request, response, "courier/courierLogin.jsp");
            return;
        }

        // compare entered password with stored encrypted password
        String enteredPasswordTrimmed = password.trim();
        String storedPassword = courier.getPassword();

        System.out.println("[COURIER LOGIN] CourierId: " + courier.getId()
                + " | Name: " + courier.getName()
                + " | Mobile: " + courier.getMobile()
                + " | Match: " + EncryptionUtil.matches(enteredPasswordTrimmed, storedPassword));

        if (!EncryptionUtil.matches(enteredPasswordTrimmed, storedPassword)) {
            request.setAttribute("error", "Invalid password. Please try again.");
            request.setAttribute("encryptedCourierId", encryptedCourierId);
            request.setAttribute("mobileNumber", mobileNumber);
            forwardToJsp(request, response, "courier/courierLogin.jsp");
            return;
        }

        // update last login time
        courierLoginDAO.updateLastLoginTime(courier.getId());

        // resolve display name: NAME → CONTACT_PERSON → fallback
        String courierName = courier.getName();
        if (courierName == null || courierName.trim().isEmpty()) {
            courierName = courier.getContactPerson();
        }
        if (courierName == null || courierName.trim().isEmpty()) {
            courierName = "Courier #" + courier.getId();
        }

        // create courier session
        ValidateSesSecurity.createCourierSession(request, courier.getId(),
                courierName, String.valueOf(courier.getMobile()));

        System.out.println("[COURIER LOGIN] Session created - ID: " + courier.getId() + " | Name: " + courierName);

        // set remember-me cookie (30 days) if checkbox was checked
        if ("on".equals(rememberMe) || "true".equals(rememberMe)) {
            String token = courier.getId() + "|" + courier.getMobile();
            String encryptedToken = EncryptionUtil.encrypt(token);
            Cookie cookie = new Cookie(REMEMBER_COOKIE, encryptedToken);
            cookie.setMaxAge(REMEMBER_DAYS * 24 * 60 * 60);
            cookie.setPath("/");
            cookie.setHttpOnly(true);
            response.addCookie(cookie);
            System.out.println("[COURIER LOGIN] Remember-me cookie set for courier: " + courier.getId());
        }

        // first login → force password change; otherwise go to assigned pickups
        if (courier.isFirstLogin()) {
            response.sendRedirect(request.getContextPath() + "/CourierUpdatePassword.do");
        } else {
            response.sendRedirect(request.getContextPath() + "/CourierAssigned.do");
        }
    }

    /** Reads remember-me cookie, decrypts and validates, auto-creates session if valid. */
    private boolean tryAutoLogin(final HttpServletRequest request, final HttpServletResponse response)
            throws Exception {

        Cookie[] cookies = request.getCookies();
        if (cookies == null) return false;

        String token = null;
        for (Cookie c : cookies) {
            if (REMEMBER_COOKIE.equals(c.getName())) {
                token = c.getValue();
                break;
            }
        }
        if (token == null || token.isEmpty()) return false;

        try {
            String decrypted = EncryptionUtil.decrypt(token);
            String[] parts = decrypted.split("\\|");
            if (parts.length != 2) return false;

            int courierId = Integer.parseInt(parts[0]);
            long mobile = Long.parseLong(parts[1]);

            Courier courier = courierLoginDAO.getCourierById(courierId);
            if (courier == null || !courier.hasLoginCreated() || !courier.isAccountActive()) {
                clearRememberCookie(response);
                return false;
            }

            // verify mobile number still matches
            if (courier.getMobile() == null || courier.getMobile() != mobile) {
                clearRememberCookie(response);
                return false;
            }

            String courierName = courier.getName();
            if (courierName == null || courierName.trim().isEmpty()) {
                courierName = courier.getContactPerson();
            }
            if (courierName == null || courierName.trim().isEmpty()) {
                courierName = "Courier #" + courier.getId();
            }

            ValidateSesSecurity.createCourierSession(request, courier.getId(),
                    courierName, String.valueOf(courier.getMobile()));

            courierLoginDAO.updateLastLoginTime(courier.getId());

            System.out.println("[COURIER AUTO-LOGIN] Cookie login - ID: " + courier.getId() + " | Name: " + courierName);

            response.sendRedirect(request.getContextPath() + "/CourierAssigned.do");
            return true;

        } catch (final Exception e) {
            System.out.println("[COURIER AUTO-LOGIN] Invalid cookie, clearing: " + e.getMessage());
            clearRememberCookie(response);
            return false;
        }
    }

    /** Removes the remember-me cookie from the browser. */
    private void clearRememberCookie(final HttpServletResponse response) {
        Cookie cookie = new Cookie(REMEMBER_COOKIE, "");
        cookie.setMaxAge(0);
        cookie.setPath("/");
        cookie.setHttpOnly(true);
        response.addCookie(cookie);
    }

    /* ================================================================
     *  FORGOT PASSWORD – Step 1: enter mobile, send OTP via WhatsApp
     * ================================================================ */

    private void showForgotPasswordPage(final HttpServletRequest request, final HttpServletResponse response)
            throws Exception {
        forwardToJsp(request, response, "courier/courierForgotPassword.jsp");
    }

    private void handleForgotPassword(final HttpServletRequest request, final HttpServletResponse response)
            throws Exception {

        final String mobileNumber = request.getParameter("mobileNumber");

        if (mobileNumber == null || mobileNumber.trim().isEmpty()) {
            request.setAttribute("error", "Mobile number is required.");
            forwardToJsp(request, response, "courier/courierForgotPassword.jsp");
            return;
        }

        final long mobile;
        try {
            mobile = Long.parseLong(mobileNumber.trim());
        } catch (final NumberFormatException e) {
            request.setAttribute("error", "Invalid mobile number format.");
            forwardToJsp(request, response, "courier/courierForgotPassword.jsp");
            return;
        }

        final Courier courier = courierLoginDAO.getCourierByMobile(mobile);
        if (courier == null || !courier.hasLoginCreated()) {
            request.setAttribute("error", "No account found for this mobile number.");
            forwardToJsp(request, response, "courier/courierForgotPassword.jsp");
            return;
        }

        // generate 4-digit OTP, encrypt and save with 10-min expiry
        final String otp = generateOtp();
        final String encryptedOtp = EncryptionUtil.encrypt(otp);

        courierLoginDAO.saveOtp(courier.getId(), encryptedOtp);

        // send OTP via WhatsApp
        final String whatsappMobile = "91" + courier.getMobile();
        WhatsAppMessageSender.sendCourierOtpMessage(
                whatsappMobile, WHATSAPP_TEMPLATE_ID, String.valueOf(courier.getId()), otp);

        request.setAttribute("mobileNumber", mobileNumber);
        request.setAttribute("courierId", courier.getId());
        request.setAttribute("success", "OTP sent successfully to your mobile number.");
        forwardToJsp(request, response, "courier/courierVerifyOtp.jsp");
    }

    /* ================================================================
     *  FORGOT PASSWORD – Step 2: verify OTP
     * ================================================================ */

    private void showOtpVerificationPage(final HttpServletRequest request, final HttpServletResponse response)
            throws Exception {
        forwardToJsp(request, response, "courier/courierVerifyOtp.jsp");
    }

    private void handleOtpVerification(final HttpServletRequest request, final HttpServletResponse response)
            throws Exception {

        final String courierIdStr = request.getParameter("courierId");
        final String otpEntered = request.getParameter("otp");
        final String mobileNumber = request.getParameter("mobileNumber");

        if (courierIdStr == null || otpEntered == null || otpEntered.trim().isEmpty()) {
            request.setAttribute("error", "OTP is required.");
            request.setAttribute("mobileNumber", mobileNumber);
            request.setAttribute("courierId", courierIdStr);
            forwardToJsp(request, response, "courier/courierVerifyOtp.jsp");
            return;
        }

        final int courierId;
        try {
            courierId = Integer.parseInt(courierIdStr.trim());
        } catch (final NumberFormatException e) {
            request.setAttribute("error", "Invalid session. Please restart the forgot password process.");
            forwardToJsp(request, response, "courier/courierForgotPassword.jsp");
            return;
        }

        final Courier courier = courierLoginDAO.getCourierById(courierId);
        if (courier == null) {
            request.setAttribute("error", "Session expired. Please restart the forgot password process.");
            forwardToJsp(request, response, "courier/courierForgotPassword.jsp");
            return;
        }

        // check max attempts
        if (courier.getOtpAttemptCount() >= MAX_OTP_ATTEMPTS) {
            request.setAttribute("error", "Maximum OTP attempts exceeded. Please request a new OTP.");
            forwardToJsp(request, response, "courier/courierForgotPassword.jsp");
            return;
        }

        // check expiry (compare Java time to DB time; both set from Java clock)
        final Timestamp now = new Timestamp(System.currentTimeMillis());
        System.out.println("[OTP DEBUG] Java current time : " + now);
        System.out.println("[OTP DEBUG] DB OTP expiry time: " + courier.getOtpExpiryTime());
        System.out.println("[OTP DEBUG] DB OTP gen time   : " + courier.getOtpGeneratedTime());

        if (courier.getOtpExpiryTime() != null) {
            if (now.after(courier.getOtpExpiryTime())) {
                System.out.println("[OTP DEBUG] OTP EXPIRED - now is after expiry");
                request.setAttribute("error", "OTP has expired. Please request a new OTP.");
                forwardToJsp(request, response, "courier/courierForgotPassword.jsp");
                return;
            } else {
                System.out.println("[OTP DEBUG] OTP still valid");
            }
        }

        if (courier.getOtp() == null) {
            request.setAttribute("error", "No OTP found. Please request a new OTP.");
            forwardToJsp(request, response, "courier/courierForgotPassword.jsp");
            return;
        }

        // match entered OTP with stored encrypted OTP
        if (!EncryptionUtil.matches(otpEntered.trim(), courier.getOtp())) {
            courierLoginDAO.incrementOtpAttempts(courierId);
            final int remainingAttempts = MAX_OTP_ATTEMPTS - (courier.getOtpAttemptCount() + 1);

            if (remainingAttempts <= 0) {
                request.setAttribute("error", "Maximum OTP attempts exceeded. Please request a new OTP.");
                forwardToJsp(request, response, "courier/courierForgotPassword.jsp");
            } else {
                request.setAttribute("error", "Invalid OTP. " + remainingAttempts + " attempt(s) remaining.");
                request.setAttribute("mobileNumber", mobileNumber);
                request.setAttribute("courierId", courierId);
                forwardToJsp(request, response, "courier/courierVerifyOtp.jsp");
            }
            return;
        }

        // OTP verified → proceed to reset password page
        request.setAttribute("courierId", courierId);
        request.setAttribute("mobileNumber", mobileNumber);
        request.setAttribute("otpVerified", "true");
        forwardToJsp(request, response, "courier/courierResetPassword.jsp");
    }

    /* ================================================================
     *  FORGOT PASSWORD – Step 3: set new password
     * ================================================================ */

    private void showResetPasswordPage(final HttpServletRequest request, final HttpServletResponse response)
            throws Exception {
        forwardToJsp(request, response, "courier/courierResetPassword.jsp");
    }

    private void handleResetPassword(final HttpServletRequest request, final HttpServletResponse response)
            throws Exception {

        final String courierIdStr = request.getParameter("courierId");
        final String newPassword = request.getParameter("newPassword");
        final String confirmPassword = request.getParameter("confirmPassword");

        if (courierIdStr == null || courierIdStr.trim().isEmpty()) {
            request.setAttribute("error", "Invalid session. Please restart the forgot password process.");
            forwardToJsp(request, response, "courier/courierForgotPassword.jsp");
            return;
        }

        if (newPassword == null || newPassword.trim().isEmpty()) {
            request.setAttribute("error", "New password is required.");
            request.setAttribute("courierId", courierIdStr);
            forwardToJsp(request, response, "courier/courierResetPassword.jsp");
            return;
        }

        if (!newPassword.equals(confirmPassword)) {
            request.setAttribute("error", "Passwords do not match.");
            request.setAttribute("courierId", courierIdStr);
            forwardToJsp(request, response, "courier/courierResetPassword.jsp");
            return;
        }

        final int courierId;
        try {
            courierId = Integer.parseInt(courierIdStr.trim());
        } catch (final NumberFormatException e) {
            request.setAttribute("error", "Invalid session. Please restart the forgot password process.");
            forwardToJsp(request, response, "courier/courierForgotPassword.jsp");
            return;
        }

        final String encryptedPassword = EncryptionUtil.encrypt(newPassword.trim());

        final boolean updated = courierLoginDAO.updatePassword(courierId, encryptedPassword);
        if (!updated) {
            request.setAttribute("error", "Failed to update password. Please try again.");
            request.setAttribute("courierId", courierIdStr);
            forwardToJsp(request, response, "courier/courierResetPassword.jsp");
            return;
        }

        // clear OTP data after successful reset
        courierLoginDAO.clearOtp(courierId);

        request.setAttribute("success", "Password reset successfully. Please login with your new password.");
        forwardToJsp(request, response, "courier/courierLogin.jsp");
    }

    /* ================================================================
     *  FIRST-LOGIN PASSWORD UPDATE
     * ================================================================ */

    private void showUpdatePasswordPage(final HttpServletRequest request, final HttpServletResponse response)
            throws Exception {
        if (!ValidateSesSecurity.isCourierSessionValid(request)) {
            response.sendRedirect(request.getContextPath() + "/CourierLoginOtp.do");
            return;
        }
        forwardToJsp(request, response, "courier/courierUpdatePassword.jsp");
    }

    private void handleUpdatePassword(final HttpServletRequest request, final HttpServletResponse response)
            throws Exception {

        if (!ValidateSesSecurity.isCourierSessionValid(request)) {
            response.sendRedirect(request.getContextPath() + "/CourierLoginOtp.do");
            return;
        }

        final String newPassword = request.getParameter("newPassword");
        final String confirmPassword = request.getParameter("confirmPassword");

        if (newPassword == null || newPassword.trim().isEmpty()) {
            request.setAttribute("error", "New password is required.");
            forwardToJsp(request, response, "courier/courierUpdatePassword.jsp");
            return;
        }

        if (!newPassword.equals(confirmPassword)) {
            request.setAttribute("error", "Passwords do not match.");
            forwardToJsp(request, response, "courier/courierUpdatePassword.jsp");
            return;
        }

        final int courierId = ValidateSesSecurity.getCourierId(request);
        if (courierId == 0) {
            response.sendRedirect(request.getContextPath() + "/CourierLoginOtp.do");
            return;
        }

        final String encryptedPassword = EncryptionUtil.encrypt(newPassword.trim());
        courierLoginDAO.updatePassword(courierId, encryptedPassword);

        response.sendRedirect(request.getContextPath() + "/CourierAssigned.do");
    }

    /* ================================================================
     *  LOGOUT
     * ================================================================ */

    private void handleLogout(final HttpServletRequest request, final HttpServletResponse response)
            throws IOException {
        clearRememberCookie(response);
        ValidateSesSecurity.invalidateCourierSession(request);
        response.sendRedirect(request.getContextPath() + "/CourierLoginOtp.do");
    }

    /* ---- Utility ---- */

    /** Generates a random 4-digit OTP. */
    private String generateOtp() {
        final Random random = new Random();
        final int otp = 1000 + random.nextInt(9000);
        return String.valueOf(otp);
    }
}
