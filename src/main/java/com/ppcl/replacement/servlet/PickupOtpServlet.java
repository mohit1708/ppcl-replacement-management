package com.ppcl.replacement.servlet;

import com.google.gson.JsonObject;
import com.ppcl.replacement.util.OtpService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.stream.Collectors;

/**
 * Servlet for Pickup OTP operations.
 * Handles OTP generation and validation for pickup flows.
 * 
 * Endpoints:
 * - POST /pickup/otp/generate - Generate OTP
 * - POST /pickup/otp/validate - Validate OTP
 */
public class PickupOtpServlet extends HttpServlet {

    private final OtpService otpService = new OtpService();

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        
        PrintWriter out = response.getWriter();
        JsonObject responseJson = new JsonObject();

        String servletPath = request.getServletPath();

        try {
            if ("/pickup/otp/generate".equals(servletPath)) {
                handleGenerateOtp(request, responseJson);
            } else if ("/pickup/otp/validate".equals(servletPath)) {
                handleValidateOtp(request, responseJson);
            } else {
                responseJson.addProperty("status", "INVALID_REQUEST");
                responseJson.addProperty("message", "Invalid endpoint");
            }
        } catch (Exception e) {
            e.printStackTrace();
            responseJson.addProperty("status", "FAILURE");
            responseJson.addProperty("message", "Error: " + e.getMessage());
        }

        out.print(responseJson.toString());
        out.flush();
    }

    /**
     * Handle OTP generation request.
     * POST /pickup/otp/generate
     * 
     * Request body (JSON):
     * {
     *   "callId": "12345",
     *   "printerSerialNo": "ABC123",
     *   "replacementRequestId": 100,
     *   "pickupQty": "3",
     *   "orderedQty": "5"
     * }
     */
    private void handleGenerateOtp(HttpServletRequest request, JsonObject responseJson) throws IOException {
        // Read JSON request body
        String requestBody = readRequestBody(request);
        JsonObject requestJson = parseJson(requestBody);

        // Extract parameters
        String callId = getStringParameter(requestJson, "callId");
        String printerSerialNo = getStringParameter(requestJson, "printerSerialNo");
        Integer replacementRequestId = getIntegerParameter(requestJson, "replacementRequestId");
        String pickupQty = getStringParameter(requestJson, "pickupQty");
        String orderedQty = getStringParameter(requestJson, "orderedQty");

        // Validate required parameters
        if (callId == null || callId.trim().isEmpty()) {
            responseJson.addProperty("status", "INVALID_REQUEST");
            responseJson.addProperty("message", "callId is required");
            return;
        }

        if (printerSerialNo == null || printerSerialNo.trim().isEmpty()) {
            responseJson.addProperty("status", "INVALID_REQUEST");
            responseJson.addProperty("message", "printerSerialNo is required");
            return;
        }

        if (replacementRequestId == null) {
            responseJson.addProperty("status", "INVALID_REQUEST");
            responseJson.addProperty("message", "replacementRequestId is required");
            return;
        }

        if (pickupQty == null || pickupQty.trim().isEmpty()) {
            responseJson.addProperty("status", "INVALID_REQUEST");
            responseJson.addProperty("message", "pickupQty is required");
            return;
        }

        if (orderedQty == null || orderedQty.trim().isEmpty()) {
            responseJson.addProperty("status", "INVALID_REQUEST");
            responseJson.addProperty("message", "orderedQty is required");
            return;
        }

        // Generate OTP
        OtpService.GenerateOtpResult result = otpService.generateOtpForPickup(
            callId, printerSerialNo, replacementRequestId, pickupQty, orderedQty);

        responseJson.addProperty("status", result.getStatus());
        responseJson.addProperty("message", result.getMessage());
    }

    /**
     * Handle OTP validation request.
     * POST /pickup/otp/validate
     * 
     * Request body (JSON):
     * {
     *   "callId": "12345",
     *   "replacementRequestId": 100,
     *   "printerSerialNo": "ABC123",
     *   "otp": "1234",
     *   "pickupQty": "3",
     *   "orderedQty": "5"
     * }
     */
    private void handleValidateOtp(HttpServletRequest request, JsonObject responseJson) throws IOException {
        // Read JSON request body
        String requestBody = readRequestBody(request);
        JsonObject requestJson = parseJson(requestBody);

        // Extract parameters
        String callId = getStringParameter(requestJson, "callId");
        Integer replacementRequestId = getIntegerParameter(requestJson, "replacementRequestId");
        String printerSerialNo = getStringParameter(requestJson, "printerSerialNo");
        String otp = getStringParameter(requestJson, "otp");
        String pickupQty = getStringParameter(requestJson, "pickupQty");
        String orderedQty = getStringParameter(requestJson, "orderedQty");

        // Validate required parameters
        if (callId == null || callId.trim().isEmpty()) {
            responseJson.addProperty("status", "INVALID_REQUEST");
            responseJson.addProperty("message", "callId is required");
            return;
        }

        if (replacementRequestId == null) {
            responseJson.addProperty("status", "INVALID_REQUEST");
            responseJson.addProperty("message", "replacementRequestId is required");
            return;
        }

        if (printerSerialNo == null || printerSerialNo.trim().isEmpty()) {
            responseJson.addProperty("status", "INVALID_REQUEST");
            responseJson.addProperty("message", "printerSerialNo is required");
            return;
        }

        if (pickupQty == null || pickupQty.trim().isEmpty()) {
            responseJson.addProperty("status", "INVALID_REQUEST");
            responseJson.addProperty("message", "pickupQty is required");
            return;
        }

        if (orderedQty == null || orderedQty.trim().isEmpty()) {
            responseJson.addProperty("status", "INVALID_REQUEST");
            responseJson.addProperty("message", "orderedQty is required");
            return;
        }

        // otp is required only when pickupQty != orderedQty
        if (!(pickupQty.trim().equals(orderedQty.trim())) && (otp == null || otp.trim().isEmpty())) {
            responseJson.addProperty("status", "INVALID_REQUEST");
            responseJson.addProperty("message", "otp is required when pickupQty and orderedQty do not match");
            return;
        }

        // Validate OTP
        OtpService.ValidateOtpResult result = otpService.validateOtpForPickup(
            callId, replacementRequestId, printerSerialNo, otp, pickupQty, orderedQty);

        responseJson.addProperty("status", result.getStatus());
        responseJson.addProperty("message", result.getMessage());
    }

    /**
     * Read request body as string.
     */
    private String readRequestBody(HttpServletRequest request) throws IOException {
        try (BufferedReader reader = request.getReader()) {
            return reader.lines().collect(Collectors.joining("\n"));
        }
    }

    /**
     * Parse JSON string to JsonObject.
     */
    private JsonObject parseJson(String jsonString) {
        if (jsonString == null || jsonString.trim().isEmpty()) {
            return new JsonObject();
        }
        try {
            return com.google.gson.JsonParser.parseString(jsonString).getAsJsonObject();
        } catch (Exception e) {
            return new JsonObject();
        }
    }

    /**
     * Get string parameter from JsonObject.
     */
    private String getStringParameter(JsonObject json, String key) {
        if (json.has(key) && !json.get(key).isJsonNull()) {
            return json.get(key).getAsString();
        }
        return null;
    }

    /**
     * Get integer parameter from JsonObject.
     */
    private Integer getIntegerParameter(JsonObject json, String key) {
        if (json.has(key) && !json.get(key).isJsonNull()) {
            try {
                return json.get(key).getAsInt();
            } catch (Exception e) {
                return null;
            }
        }
        return null;
    }
}
