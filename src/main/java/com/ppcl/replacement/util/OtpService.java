package com.ppcl.replacement.util;

import com.ppcl.replacement.dao.OtpManagementDAO;
import com.ppcl.replacement.model.OtpManagement;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.Date;
import java.util.Random;

/**
 * Generic OTP Service for OTP generation and validation.
 * This service is reusable across multiple features (pickup, etc.).
 */
public class OtpService {

    private static final Random random = new Random();
    private final OtpManagementDAO otpDAO;

    public OtpService() {
        this.otpDAO = new OtpManagementDAO();
    }

    /**
     * Generate a 4-digit OTP.
     * 
     * @return 4-digit OTP string
     */
    public static String generateOtp() {
        StringBuilder otp = new StringBuilder();
        for (int i = 0; i < 4; i++) {
            otp.append(random.nextInt(10));
        }
        return otp.toString();
    }

    /**
     * Generate OTP for pickup.
     * If pickupQty == orderedQty, no OTP is needed (quantities match).
     * Otherwise, sends OTP via WhatsApp for confirmation.
     * 
     * @param callId the call ID
     * @param printerSerialNo the printer serial number
     * @param replacementRequestId the replacement request ID
     * @param pickupQty pickup qty entered by pickup executive
     * @param orderedQty unused cartridges qty ordered during pullback call
     * @return GenerateOtpResult with status and message
     */
    public GenerateOtpResult generateOtpForPickup(String callId, String printerSerialNo,
                                                    Integer replacementRequestId,
                                                    String pickupQty, String orderedQty) {
        try {
            // Validate all 3 params (callId, replacementRequestId, printerSerialNo) exist in REPLACEMENT_PULLBACK
            // and get pullback ID + mobile number via CLIENT_DOT_ID -> CLIENT.MOBILE_NO
            OtpManagementDAO.PullbackInfo pullbackInfo = otpDAO.validatePullbackAndGetInfo(callId, replacementRequestId, printerSerialNo);
            if (pullbackInfo == null || pullbackInfo.getMobileNumber() == null || pullbackInfo.getMobileNumber().trim().isEmpty()) {
                return new GenerateOtpResult("INVALID_REQUEST", 
                    "No valid replacement pullback found for the given callId, replacementRequestId and printerSerialNo, or mobile number not available");
            }

            // If pickupQty == orderedQty, no OTP needed
            if (pickupQty != null && orderedQty != null && pickupQty.trim().equals(orderedQty.trim())) {
                return new GenerateOtpResult("QTY_MATCH", "No OTP needed as quantities are matching");
            }

            String mobileNumber = "91" + pullbackInfo.getMobileNumber();
            String clientId = pullbackInfo.getClientId();
            Long pullbackId = pullbackInfo.getPullbackId();

            // Find existing OTP record by pullbackId with locking
            OtpManagement existingOtp = otpDAO.findByPullbackIdForUpdate(pullbackId);

            // Check if blocked
            if (existingOtp != null && existingOtp.getBlockUntilTime() != null) {
                Timestamp now = Timestamp.from(Instant.now());
                if (existingOtp.getBlockUntilTime().after(now)) {
                    return new GenerateOtpResult("OTP_BLOCKED", 
                        "OTP generation is blocked. Please try again after the block period.");
                }
            }

            // Check attempt count
            int attemptCount = (existingOtp != null && existingOtp.getAttemptCount() != null) 
                ? existingOtp.getAttemptCount() : 0;

            if (attemptCount >= OtpManagementDAO.getMaxAttempts()) {
                // Block for 1 hour
                Timestamp blockUntil = Timestamp.from(
                    Instant.now().plusSeconds(OtpManagementDAO.getBlockDurationHours() * 3600));
                
                if (existingOtp != null) {
                    existingOtp.setBlockUntilTime(blockUntil);
                    existingOtp.setUpdatedAt(Timestamp.from(Instant.now()));
                    otpDAO.update(existingOtp);
                } else {
                    // Create new record with block
                    OtpManagement newOtp = new OtpManagement();
                    newOtp.setPullbackId(pullbackId);
                    newOtp.setMobileNumber(mobileNumber);
                    newOtp.setOtpValue(""); // Empty OTP since blocked
                    newOtp.setOtpExpiryTime(Timestamp.from(Instant.now()));
                    newOtp.setAttemptCount(OtpManagementDAO.getMaxAttempts());
                    newOtp.setBlockUntilTime(blockUntil);
                    otpDAO.insert(newOtp);
                }
                
                return new GenerateOtpResult("OTP_BLOCKED", 
                    "Maximum OTP generation attempts exceeded. Blocked for 1 hour.");
            }

            // Generate new OTP
            String otpValue = generateOtp();
            Timestamp expiryTime = Timestamp.from(
                Instant.now().plusSeconds(OtpManagementDAO.getOtpValidityMinutes() * 60));

            if (existingOtp != null) {
                // Update existing record
                existingOtp.setMobileNumber(mobileNumber);
                existingOtp.setOtpValue(otpValue);
                existingOtp.setOtpExpiryTime(expiryTime);
                existingOtp.setAttemptCount(attemptCount + 1);
                existingOtp.setBlockUntilTime(null); // Clear block if any
                existingOtp.setOtpValidatedAt(null); // Clear previous validation
                existingOtp.setUpdatedAt(Timestamp.from(Instant.now()));
                otpDAO.update(existingOtp);
            } else {
                // Create new record
                OtpManagement newOtp = new OtpManagement();
                newOtp.setPullbackId(pullbackId);
                newOtp.setMobileNumber(mobileNumber);
                newOtp.setOtpValue(otpValue);
                newOtp.setOtpExpiryTime(expiryTime);
                newOtp.setAttemptCount(1);
                newOtp.setBlockUntilTime(null);
                otpDAO.insert(newOtp);
            }

            // Send OTP via WhatsApp using existing whatsapp class
            String templateName = "otp_verification";
            Date now = new Date();
            Connection con = DBConnectionPool.getConnection();
            try {
                WhatsAppMessageSender waba = new WhatsAppMessageSender();
                String sendResult = waba.sendCartPickupOTP(con, mobileNumber, templateName,
                        orderedQty, pickupQty, otpValue);

                // Log WhatsApp send status in OTP_MANAGEMENT
                OtpManagement otpRecord = otpDAO.findByPullbackIdForUpdate(pullbackId);
                if (otpRecord != null) {
                    otpRecord.setWaSendStatus(sendResult);
                    otpRecord.setUpdatedAt(Timestamp.from(Instant.now()));
                    otpDAO.update(otpRecord);
                }

                if (sendResult != null && sendResult.contains("submitted")) {
                    return new GenerateOtpResult("OTP_SENT", "OTP has been sent successfully");
                } else {
                    return new GenerateOtpResult("FAILURE", "Failed to send OTP: " + sendResult);
                }
            } finally {
                if (con != null) {
                    try { con.close(); } catch (Exception ignored) {}
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
            return new GenerateOtpResult("FAILURE", "Database error: " + e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            return new GenerateOtpResult("FAILURE", "Unexpected error: " + e.getMessage());
        }
    }

    /**
     * Validate OTP for pickup.
     * If pickupQty == orderedQty, no OTP validation needed (quantities match).
     * 
     * @param callId the call ID
     * @param replacementRequestId the replacement request ID
     * @param printerSerialNo the printer serial number
     * @param otp the OTP to validate
     * @param pickupQty pickup qty entered by pickup executive
     * @param orderedQty unused cartridges qty ordered during pullback call
     * @return ValidateOtpResult with status and message
     */
    public ValidateOtpResult validateOtpForPickup(String callId, Integer replacementRequestId,
                                                    String printerSerialNo, String otp,
                                                    String pickupQty, String orderedQty) {
        try {
            // Resolve pullbackId from REPLACEMENT_PULLBACK (all 3 must match)
            OtpManagementDAO.PullbackInfo pullbackInfo = otpDAO.validatePullbackAndGetInfo(callId, replacementRequestId, printerSerialNo);
            if (pullbackInfo == null) {
                return new ValidateOtpResult("INVALID_REQUEST", 
                    "No valid replacement pullback found for the given callId, replacementRequestId and printerSerialNo");
            }

            // If pickupQty == orderedQty, no OTP validation needed
            if (pickupQty != null && orderedQty != null && pickupQty.trim().equals(orderedQty.trim())) {
                return new ValidateOtpResult("QTY_MATCH", "No OTP needed as quantities are matching");
            }

            Long pullbackId = pullbackInfo.getPullbackId();

            // Find existing OTP record by pullbackId with locking
            OtpManagement existingOtp = otpDAO.findByPullbackIdForUpdate(pullbackId);

            if (existingOtp == null) {
                return new ValidateOtpResult("INVALID_REQUEST", 
                    "No OTP found. Please generate an OTP first.");
            }

            // Check if blocked
            if (existingOtp.getBlockUntilTime() != null) {
                Timestamp now = Timestamp.from(Instant.now());
                if (existingOtp.getBlockUntilTime().after(now)) {
                    return new ValidateOtpResult("OTP_BLOCKED", 
                        "OTP is currently blocked. Please try again after the block period.");
                }
            }

            // Check if OTP is expired
            Timestamp now = Timestamp.from(Instant.now());
            if (existingOtp.getOtpExpiryTime() != null && existingOtp.getOtpExpiryTime().before(now)) {
                return new ValidateOtpResult("OTP_EXPIRED", "OTP has expired");
            }

            // Check if already validated
            if (existingOtp.getOtpValidatedAt() != null) {
                return new ValidateOtpResult("INVALID_OTP", "OTP has already been validated");
            }

            // Validate OTP
            if (existingOtp.getOtpValue() == null || !existingOtp.getOtpValue().equals(otp)) {
                return new ValidateOtpResult("INVALID_OTP", "Incorrect OTP");
            }

            // Success - clear OTP and reset state
            otpDAO.clearOtpAfterValidation(existingOtp.getId());

            return new ValidateOtpResult("SUCCESS", "OTP validated successfully");

        } catch (SQLException e) {
            e.printStackTrace();
            return new ValidateOtpResult("FAILURE", "Database error: " + e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            return new ValidateOtpResult("FAILURE", "Unexpected error: " + e.getMessage());
        }
    }

    /**
     * Result class for OTP generation.
     */
    public static class GenerateOtpResult {
        private final String status;
        private final String message;

        public GenerateOtpResult(String status, String message) {
            this.status = status;
            this.message = message;
        }

        public String getStatus() {
            return status;
        }

        public String getMessage() {
            return message;
        }
    }

    /**
     * Result class for OTP validation.
     */
    public static class ValidateOtpResult {
        private final String status;
        private final String message;

        public ValidateOtpResult(String status, String message) {
            this.status = status;
            this.message = message;
        }

        public String getStatus() {
            return status;
        }

        public String getMessage() {
            return message;
        }
    }
}
