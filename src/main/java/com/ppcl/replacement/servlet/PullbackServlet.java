package com.ppcl.replacement.servlet;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.ppcl.replacement.dao.PrinterPullbackDAO;
import com.ppcl.replacement.model.PrinterPullback;
import com.ppcl.replacement.util.FileUploadUtil;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.servlet.http.Part;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Date;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

@WebServlet(urlPatterns = {"/api/pullback", "/api/pullback/list", "/api/pullback/action"})
public class PullbackServlet extends HttpServlet {

    private static final String RECEIPT_UPLOAD_DIR_PARAM = "pullback.receipt.dir";
    private final Gson gson = new Gson();
    private final PrinterPullbackDAO pullbackDAO = new PrinterPullbackDAO();

    @Override
    protected void doGet(final HttpServletRequest request, final HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        final PrintWriter out = response.getWriter();
        final JsonObject responseJson = new JsonObject();

        if (!ensureAuthenticated(request, response, responseJson)) {
            out.print(gson.toJson(responseJson));
            out.flush();
            return;
        }

        final String path = request.getServletPath();

        try {
            if ("/api/pullback".equals(path)) {
                handleFetchByReqId(request, responseJson);
            } else if ("/api/pullback/list".equals(path)) {
                handleListWithFilters(request, responseJson);
            } else {
                responseJson.addProperty("status", "FAILURE");
                responseJson.addProperty("message", "Invalid endpoint for GET");
            }
        } catch (final Exception e) {
            e.printStackTrace();
            responseJson.addProperty("status", "FAILURE");
            responseJson.addProperty("message", "Error: " + e.getMessage());
        }

        out.print(gson.toJson(responseJson));
        out.flush();
    }

    @Override
    protected void doPost(final HttpServletRequest request, final HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        final PrintWriter out = response.getWriter();
        final JsonObject responseJson = new JsonObject();

        if (!ensureAuthenticated(request, response, responseJson)) {
            out.print(gson.toJson(responseJson));
            out.flush();
            return;
        }

        final String path = request.getServletPath();

        try {
            if ("/api/pullback".equals(path)) {
                handleCreateOrUpdate(request, responseJson);
            } else if ("/api/pullback/action".equals(path)) {
                handleAction(request, responseJson);
            } else {
                responseJson.addProperty("status", "FAILURE");
                responseJson.addProperty("message", "Invalid endpoint for POST");
            }
        } catch (final Exception e) {
            e.printStackTrace();
            responseJson.addProperty("status", "FAILURE");
            responseJson.addProperty("message", "Error: " + e.getMessage());
        }

        out.print(gson.toJson(responseJson));
        out.flush();
    }

    // GET /api/pullback?replacementReqId=123&pSerialNo=xyz
    private void handleFetchByReqId(final HttpServletRequest request, final JsonObject responseJson) throws Exception {
        final String reqIdParam = request.getParameter("replacementReqId");
        final String serialNo = request.getParameter("pSerialNo");

        if (reqIdParam == null || reqIdParam.isEmpty()) {
            responseJson.addProperty("status", "FAILURE");
            responseJson.addProperty("message", "replacementReqId is required");
            return;
        }

        final int reqId = Integer.parseInt(reqIdParam);
        final List<PrinterPullback> pullbacks = pullbackDAO.getPullbacksByReqId(reqId, serialNo);

        final JsonArray dataArray = new JsonArray();
        for (final PrinterPullback p : pullbacks) {
            dataArray.add(toJson(p));
        }

        responseJson.addProperty("status", "SUCCESS");
        responseJson.add("data", dataArray);
    }

    // GET /api/pullback/list?reqId=&status=&dateFrom=&dateTo=
    private void handleListWithFilters(final HttpServletRequest request, final JsonObject responseJson) throws Exception {
        final Integer reqId = parseIntOrNull(request.getParameter("reqId"));
        Integer status = null;
        final String statusParam = request.getParameter("status");
        if (statusParam != null && !statusParam.trim().isEmpty()) {
            status = pullbackDAO.getStatusCode(statusParam);
        }
        final Date dateFrom = parseDateOrNull(request.getParameter("dateFrom"));
        final Date dateTo = parseDateOrNull(request.getParameter("dateTo"));

        final List<PrinterPullback> pullbacks = pullbackDAO.getPullbacksWithFilters(reqId, status, dateFrom, dateTo);

        final JsonArray dataArray = new JsonArray();
        for (final PrinterPullback p : pullbacks) {
            dataArray.add(toJson(p));
        }

        responseJson.addProperty("status", "SUCCESS");
        responseJson.add("data", dataArray);
    }

    // POST /api/pullback - Create or Update
    private void handleCreateOrUpdate(final HttpServletRequest request, final JsonObject responseJson) throws Exception {
        final boolean multipart = isMultipartRequest(request);
        final JsonObject body = multipart ? new JsonObject() : readJsonBody(request);

        final int replacementReqId = getIntField(request, body, multipart, "replacementReqId", 0);
        final String pSerialNo = getStringField(request, body, multipart, "pSerialNo");

        if (replacementReqId == 0) {
            responseJson.addProperty("status", "FAILURE");
            responseJson.addProperty("message", "replacementReqId is required");
            return;
        }

        if (pSerialNo == null || pSerialNo.trim().isEmpty()) {
            responseJson.addProperty("status", "FAILURE");
            responseJson.addProperty("message", "pSerialNo is required");
            return;
        }

        if (!pullbackDAO.isValidReplacementRequest(replacementReqId)) {
            responseJson.addProperty("status", "FAILURE");
            responseJson.addProperty("message", "Invalid replacementReqId: No replacement request found with ID " + replacementReqId);
            return;
        }

        final String pullbackMode = getStringField(request, body, multipart, "pullbackMode");
        final String uploadedReceiptPath = multipart ? uploadReceiptIfPresent(request) : null;
        final String effectiveReceipt = uploadedReceiptPath != null
                ? uploadedReceiptPath
                : getStringField(request, body, multipart, "receipt");

        // Validate COURIER mode fields
        if ("COURIER".equalsIgnoreCase(pullbackMode)) {
            final String missing = validateCourierFields(request, body, multipart, effectiveReceipt);
            if (missing != null) {
                responseJson.addProperty("status", "FAILURE");
                responseJson.addProperty("message", "When pullbackMode is COURIER, the following fields are mandatory: " + missing);
                return;
            }
        }

        // Validate TRANSPORT mode fields
        if ("TRANSPORT".equalsIgnoreCase(pullbackMode)) {
            final String transportMode = getStringField(request, body, multipart, "transportMode");
            if (transportMode != null && !transportMode.isEmpty() &&
                    !transportMode.equalsIgnoreCase("BUS") &&
                    !transportMode.equalsIgnoreCase("TRAIN") &&
                    !transportMode.equalsIgnoreCase("TRANSPORT")) {
                responseJson.addProperty("status", "FAILURE");
                responseJson.addProperty("message", "transportMode must be one of: BUS, TRAIN, TRANSPORT");
                return;
            }
            final String missing = validateTransportFields(request, body, multipart, effectiveReceipt);
            if (missing != null) {
                responseJson.addProperty("status", "FAILURE");
                responseJson.addProperty("message", "When pullbackMode is TRANSPORT, the following fields are mandatory: " + missing);
                return;
            }
        }

        final PrinterPullback pullback = multipart ? mapFromRequest(request, uploadedReceiptPath) : mapFromJson(body);
        pullback.setReplacementReqId(replacementReqId);
        pullback.setSerialNo(pSerialNo);
        pullback.setStatus(3);

        final Integer existingId = pullbackDAO.getExistingPullbackId(replacementReqId, pSerialNo);

        if (existingId != null) {
            // Fetch existing record and preserve fields not provided in the request
            final PrinterPullback existing = pullbackDAO.getPullbackByIdForUpdate(existingId);
            if (existing != null) {
                if (pullback.getCallId() == null) pullback.setCallId(existing.getCallId());
                if (pullback.getClientDotId() == null) pullback.setClientDotId(existing.getClientDotId());
                if (pullback.getLocation() == null) pullback.setLocation(existing.getLocation());
                if (pullback.getPrinterModel() == null) pullback.setPrinterModel(existing.getPrinterModel());
                if (pullback.getEmptyCartridge() == null) pullback.setEmptyCartridge(existing.getEmptyCartridge());
                if (pullback.getUnusedCartridge() == null) pullback.setUnusedCartridge(existing.getUnusedCartridge());
                if (pullback.getReplacementPrinterDetailsId() == null) pullback.setReplacementPrinterDetailsId(existing.getReplacementPrinterDetailsId());
                if (pullback.getReceipt() == null) pullback.setReceipt(existing.getReceipt());
            }
            pullback.setId(existingId);
            pullbackDAO.updatePullback(pullback);
            responseJson.addProperty("status", "SUCCESS");
            responseJson.addProperty("message", "Pullback record updated successfully");
            responseJson.addProperty("id", existingId);
        } else {
            final int newId = pullbackDAO.insertPullback(pullback);
            responseJson.addProperty("status", "SUCCESS");
            responseJson.addProperty("message", "Pullback record created successfully");
            responseJson.addProperty("id", newId);
        }
    }

    // POST /api/pullback/action - markReceived, verifyCartridge, triggerCreditNote
    private void handleAction(final HttpServletRequest request, final JsonObject responseJson) throws Exception {
        final String action = request.getParameter("action");
        final String pullbackIdStr = request.getParameter("pullbackId");

        if (pullbackIdStr == null || pullbackIdStr.trim().isEmpty()) {
            responseJson.addProperty("status", "FAILURE");
            responseJson.addProperty("message", "pullbackId is required");
            return;
        }

        final int pullbackId = Integer.parseInt(pullbackIdStr);
        final boolean success;

        switch (action) {
            case "markReceived":
                success = pullbackDAO.updateStatusOnly(pullbackId, 4);
                setActionResponse(responseJson, success, "Printer marked as received");
                break;

            case "verifyCartridge":
                success = handleVerifyCartridge(pullbackId, request, responseJson);
                if (success) {
                    responseJson.addProperty("status", "SUCCESS");
                    responseJson.addProperty("message", "Verification saved successfully");
                }
                break;

            case "receivedByInventory":
                success = pullbackDAO.updateStatusOnly(pullbackId, 5);
                setActionResponse(responseJson, success, "Marked as received by inventory");
                break;

            case "updateQc":
                final int printerQc = parseIntOrDefault(request.getParameter("printerQc"), 0);
                final int powerCableQc = parseIntOrDefault(request.getParameter("powerCableQc"), 0);
                final int lanCableQc = parseIntOrDefault(request.getParameter("lanCableQc"), 0);
                final int trayQc = parseIntOrDefault(request.getParameter("trayQc"), 0);
                final int emptyCartridgeQc = parseIntOrDefault(request.getParameter("emptyCartridgeQc"), 0);
                final int unusedCartridgeQc = parseIntOrDefault(request.getParameter("unusedCartridgeQc"), 0);
                final String qcCondition = request.getParameter("qcCondition");
                final String qcDamageDetails = request.getParameter("qcDamageDetails");
                final String qcComments = request.getParameter("qcComments");
                final String qcComment = buildVerificationComment(qcCondition, qcDamageDetails, qcComments);
                final int qcStatus = 6;
                success = pullbackDAO.updateQc(pullbackId, printerQc, powerCableQc, lanCableQc,
                        trayQc, emptyCartridgeQc, unusedCartridgeQc, qcStatus, qcComment);
                setActionResponse(responseJson, success, "QC updated successfully");
                break;

            case "triggerCreditNote":
                success = handleTriggerCreditNote(pullbackId, request);
                setActionResponse(responseJson, success, "Credit note workflow initiated");
                break;

            default:
                responseJson.addProperty("status", "FAILURE");
                responseJson.addProperty("message", "Invalid action: " + action);
        }
    }

    private boolean handleVerifyCartridge(final int pullbackId, final HttpServletRequest request,
                                          final JsonObject responseJson) throws Exception {
        final int printer = "1".equals(request.getParameter("printerReceived")) ? 1 : 0;
        final int powerCable = "1".equals(request.getParameter("powerCableReceived")) ? 1 : 0;
        final int lanCable = "1".equals(request.getParameter("lanCableReceived")) ? 1 : 0;
        final int tray = "1".equals(request.getParameter("trayReceived")) ? 1 : 0;
        final int actualEmpty = parseIntOrDefault(request.getParameter("actualEmptyCartridges"), 0);
        final int actualUnused = parseIntOrDefault(request.getParameter("actualUnusedCartridges"), 0);
        final String condition = request.getParameter("condition");
        final String damageDetails = request.getParameter("damageDetails");
        final String verificationComments = request.getParameter("verificationComments");

        final int status = "DAMAGED".equals(condition) ? 3 : 2;
        final String comments = buildVerificationComment(condition, damageDetails, verificationComments);

        final boolean success = pullbackDAO.updateVerification(pullbackId, printer, powerCable, lanCable,
                tray, actualEmpty, actualUnused, status, comments);

        if (success) {
            responseJson.addProperty("condition", condition);
        } else {
            responseJson.addProperty("status", "FAILURE");
            responseJson.addProperty("message", "Record not found");
        }
        return success;
    }

    private boolean handleTriggerCreditNote(final int pullbackId, final HttpServletRequest request) throws Exception {
        final String damageType = request.getParameter("damageType");
        final String creditAmountStr = request.getParameter("creditAmount");
        final String creditNoteReason = request.getParameter("creditNoteReason");

        final String creditInfo = String.format("[%s] %s (Amount: %s)",
                damageType, creditNoteReason,
                creditAmountStr != null && !creditAmountStr.isEmpty() ? "₹" + creditAmountStr : "TBD");

        return pullbackDAO.triggerCreditNote(pullbackId, creditInfo);
    }

    private void setActionResponse(final JsonObject responseJson, final boolean success, final String successMessage) {
        if (success) {
            responseJson.addProperty("status", "SUCCESS");
            responseJson.addProperty("message", successMessage);
        } else {
            responseJson.addProperty("status", "FAILURE");
            responseJson.addProperty("message", "Record not found");
        }
    }

    private String validateCourierFields(final HttpServletRequest request, final JsonObject body,
                                         final boolean multipart, final String receiptValue) {
        final StringBuilder missing = new StringBuilder();
        if (isBlankField(request, body, multipart, "courierName")) missing.append("courierName, ");
        if (isBlankField(request, body, multipart, "consignmentNo")) missing.append("consignmentNo, ");
        if (isBlankField(request, body, multipart, "dispatchDate")) missing.append("dispatchDate, ");
        if (isBlankField(request, body, multipart, "arrivalDate")) missing.append("arrivalDate, ");
        if (receiptValue == null || receiptValue.trim().isEmpty()) missing.append("receipt, ");
        if (isBlankField(request, body, multipart, "destinationBranch")) missing.append("destinationBranch, ");
        return missing.length() > 0 ? missing.substring(0, missing.length() - 2) : null;
    }

    private String validateTransportFields(final HttpServletRequest request, final JsonObject body,
                                           final boolean multipart, final String receiptValue) {
        final StringBuilder missing = new StringBuilder();
        if (isBlankField(request, body, multipart, "transportMode")) missing.append("transportMode, ");
        if (receiptValue == null || receiptValue.trim().isEmpty()) missing.append("receipt, ");
        if (isBlankField(request, body, multipart, "dispatchDate")) missing.append("dispatchDate, ");
        if (isBlankField(request, body, multipart, "arrivalDate")) missing.append("arrivalDate, ");
        if (isBlankField(request, body, multipart, "contactPerson")) missing.append("contactPerson, ");
        if (isBlankField(request, body, multipart, "contactNumber")) missing.append("contactNumber, ");
        if (isBlankField(request, body, multipart, "comments")) missing.append("comments, ");
        return missing.length() > 0 ? missing.substring(0, missing.length() - 2) : null;
    }

    private boolean isBlankField(final HttpServletRequest request, final JsonObject body,
                                 final boolean multipart, final String key) {
        final String val = getStringField(request, body, multipart, key);
        return val == null || val.trim().isEmpty();
    }

    private String buildVerificationComment(final String condition, final String damageDetails, final String comments) {
        final StringBuilder sb = new StringBuilder();
        sb.append("[VERIFIED: ").append(condition).append("]");
        if (damageDetails != null && !damageDetails.trim().isEmpty()) {
            sb.append(" Damage: ").append(damageDetails);
        }
        if (comments != null && !comments.trim().isEmpty()) {
            sb.append(" Notes: ").append(comments);
        }
        return sb.toString();
    }

    private PrinterPullback mapFromJson(final JsonObject body) {
        final PrinterPullback p = new PrinterPullback();
        p.setCallId(getIntOrNull(body, "callId"));
        p.setClientDotId(getIntOrNull(body, "clientDotId"));
        p.setLocation(getStringOrNull(body, "location"));
        p.setPrinterModel(getIntOrNull(body, "pModel"));
        p.setPickedBy(getStringOrNull(body, "pickedBy"));
        p.setStatus(getIntOrNull(body, "status"));
        p.setCourierId(getIntOrNull(body, "courierId"));
        p.setCourierName(getStringOrNull(body, "courierName"));
        p.setConsignmentNo(getStringOrNull(body, "consignmentNo"));
        p.setDispatchDate(parseDateFromString(getStringOrNull(body, "dispatchDate")));
        p.setArrivalDate(parseDateFromString(getStringOrNull(body, "arrivalDate")));
        p.setReceipt(getStringOrNull(body, "receipt"));
        p.setDestinationBranch(getStringOrNull(body, "destinationBranch"));
        p.setTransportMode(getStringOrNull(body, "transportMode"));
        p.setContactPerson(getStringOrNull(body, "contactPerson"));
        p.setContactNumber(getStringOrNull(body, "contactNumber"));
        p.setComments(getStringOrNull(body, "comments"));
        p.setPrinter(getIntOrDefault(body, "printer", 0));
        p.setPowerCable(getIntOrDefault(body, "powerCable", 0));
        p.setLanCable(getIntOrDefault(body, "lanCable", 0));
        p.setTray(getIntOrDefault(body, "tray", 0));
        p.setEmptyCartridge(getIntOrNull(body, "emptyCartridge"));
        p.setUnusedCartridge(getIntOrNull(body, "unusedCartridge"));
        p.setPullbackMode(getStringOrNull(body, "pullbackMode"));
        return p;
    }

    private PrinterPullback mapFromRequest(final HttpServletRequest request, final String uploadedReceiptPath) {
        final PrinterPullback p = new PrinterPullback();
        p.setCallId(parseIntOrNull(request.getParameter("callId")));
        p.setClientDotId(parseIntOrNull(request.getParameter("clientDotId")));
        p.setLocation(trimToNull(request.getParameter("location")));
        p.setPrinterModel(parseIntOrNull(request.getParameter("pModel")));
        p.setPickedBy(trimToNull(request.getParameter("pickedBy")));
        p.setStatus(parseIntOrNull(request.getParameter("status")));
        p.setCourierId(parseIntOrNull(request.getParameter("courierId")));
        p.setCourierName(trimToNull(request.getParameter("courierName")));
        p.setConsignmentNo(trimToNull(request.getParameter("consignmentNo")));
        p.setDispatchDate(parseDateFromString(request.getParameter("dispatchDate")));
        p.setArrivalDate(parseDateFromString(request.getParameter("arrivalDate")));
        p.setReceipt(uploadedReceiptPath != null ? uploadedReceiptPath : trimToNull(request.getParameter("receipt")));
        p.setDestinationBranch(trimToNull(request.getParameter("destinationBranch")));
        p.setTransportMode(trimToNull(request.getParameter("transportMode")));
        p.setContactPerson(trimToNull(request.getParameter("contactPerson")));
        p.setContactNumber(trimToNull(request.getParameter("contactNumber")));
        p.setComments(trimToNull(request.getParameter("comments")));
        p.setPrinter(parseIntOrDefault(request.getParameter("printer"), 0));
        p.setPowerCable(parseIntOrDefault(request.getParameter("powerCable"), 0));
        p.setLanCable(parseIntOrDefault(request.getParameter("lanCable"), 0));
        p.setTray(parseIntOrDefault(request.getParameter("tray"), 0));
        p.setEmptyCartridge(parseIntOrNull(request.getParameter("emptyCartridge")));
        p.setUnusedCartridge(parseIntOrNull(request.getParameter("unusedCartridge")));
        p.setPullbackMode(trimToNull(request.getParameter("pullbackMode")));
        return p;
    }

    private JsonObject toJson(final PrinterPullback p) {
        final JsonObject json = new JsonObject();
        json.addProperty("id", p.getId());
        json.addProperty("replacementReqId", p.getReplacementReqId());
        json.addProperty("callId", p.getCallId());
        json.addProperty("clientDotId", p.getClientDotId());
        json.addProperty("location", p.getLocation());
        json.addProperty("pModel", p.getPrinterModel());
        json.addProperty("pSerialNo", p.getSerialNo());
        json.addProperty("pickedBy", p.getPickedBy());
        json.addProperty("status", p.getStatus());
        json.addProperty("uiStatus", p.getUiStatus());
        json.addProperty("courierId", p.getCourierId());
        json.addProperty("courierName", p.getCourierName());
        json.addProperty("consignmentNo", p.getConsignmentNo());
        json.addProperty("dispatchDate", p.getDispatchDate() != null ? p.getDispatchDate().toString() : null);
        json.addProperty("arrivalDate", p.getArrivalDate() != null ? p.getArrivalDate().toString() : null);
        json.addProperty("receipt", p.getReceipt());
        json.addProperty("destinationBranch", p.getDestinationBranch());
        json.addProperty("transportMode", p.getTransportMode());
        json.addProperty("contactPerson", p.getContactPerson());
        json.addProperty("contactNumber", p.getContactNumber());
        json.addProperty("comments", p.getComments());
        json.addProperty("printer", p.getPrinter());
        json.addProperty("powerCable", p.getPowerCable());
        json.addProperty("lanCable", p.getLanCable());
        json.addProperty("tray", p.getTray());
        json.addProperty("emptyCartridge", p.getEmptyCartridge());
        json.addProperty("unusedCartridge", p.getUnusedCartridge());
        json.addProperty("pullbackMode", p.getPullbackMode());
        json.addProperty("printerQc", p.getPrinterQc());
        json.addProperty("powerCableQc", p.getPowerCableQc());
        json.addProperty("lanCableQc", p.getLanCableQc());
        json.addProperty("trayQc", p.getTrayQc());
        json.addProperty("emptyCartridgeQc", p.getEmptyCartridgeQc());
        json.addProperty("unusedCartridgeQc", p.getUnusedCartridgeQc());
        json.addProperty("clientName", p.getClientName());
        json.addProperty("printerModelName", p.getPrinterModelName());
        return json;
    }

    private JsonObject readJsonBody(final HttpServletRequest request) throws IOException {
        final StringBuilder sb = new StringBuilder();
        final BufferedReader reader = request.getReader();
        String line;
        while ((line = reader.readLine()) != null) {
            sb.append(line);
        }
        return JsonParser.parseString(sb.toString()).getAsJsonObject();
    }

    private boolean isMultipartRequest(final HttpServletRequest request) {
        final String contentType = request.getContentType();
        return contentType != null && contentType.toLowerCase(Locale.ROOT).startsWith("multipart/");
    }

    private String getStringField(final HttpServletRequest request, final JsonObject body,
                                  final boolean multipart, final String key) {
        if (multipart) {
            return trimToNull(request.getParameter(key));
        }
        return getStringOrNull(body, key);
    }

    private int getIntField(final HttpServletRequest request, final JsonObject body,
                            final boolean multipart, final String key, final int defaultVal) {
        if (multipart) {
            return parseIntOrDefault(request.getParameter(key), defaultVal);
        }
        return getIntOrDefault(body, key, defaultVal);
    }

    private String trimToNull(final String value) {
        if (value == null) {
            return null;
        }
        final String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private String uploadReceiptIfPresent(final HttpServletRequest request) throws Exception {
        final Part receiptPart = request.getPart("receipt");
        if (receiptPart == null || receiptPart.getSize() == 0) {
            return null;
        }

        validateReceiptFile(receiptPart);
        final String uploadBaseDir = trimToNull(getServletContext().getInitParameter(RECEIPT_UPLOAD_DIR_PARAM));
        if (uploadBaseDir == null) {
            throw new IllegalStateException("Missing web.xml context-param: " + RECEIPT_UPLOAD_DIR_PARAM);
        }

        return FileUploadUtil.uploadFile(uploadBaseDir, receiptPart, "pullback_receipt");
    }

    private void validateReceiptFile(final Part receiptPart) {
        final String contentType = receiptPart.getContentType();
        final String normalizedContentType = contentType != null ? contentType.toLowerCase(Locale.ROOT) : "";
        final String submittedFileName = receiptPart.getSubmittedFileName();
        final String extension = getFileExtension(submittedFileName);

        final boolean pdfMime = "application/pdf".equals(normalizedContentType);
        final boolean imageMime = normalizedContentType.startsWith("image/");
        final boolean validMime = pdfMime || imageMime;
        final boolean validExtension = ".pdf".equals(extension)
                || ".png".equals(extension)
                || ".jpg".equals(extension)
                || ".jpeg".equals(extension)
                || ".gif".equals(extension)
                || ".bmp".equals(extension)
                || ".webp".equals(extension)
                || ".tif".equals(extension)
                || ".tiff".equals(extension);

        if (!validMime && !validExtension) {
            throw new IllegalArgumentException("receipt must be a PDF or image file");
        }
    }

    private String getFileExtension(final String fileName) {
        if (fileName == null || fileName.trim().isEmpty()) {
            return "";
        }
        final int dotIndex = fileName.lastIndexOf('.');
        if (dotIndex < 0 || dotIndex == fileName.length() - 1) {
            return "";
        }
        return fileName.substring(dotIndex).toLowerCase(Locale.ROOT);
    }

    private Integer getIntOrNull(final JsonObject json, final String key) {
        return json.has(key) && !json.get(key).isJsonNull() ? json.get(key).getAsInt() : null;
    }

    private int getIntOrDefault(final JsonObject json, final String key, final int defaultVal) {
        return json.has(key) && !json.get(key).isJsonNull() ? json.get(key).getAsInt() : defaultVal;
    }

    private String getStringOrNull(final JsonObject json, final String key) {
        return json.has(key) && !json.get(key).isJsonNull() ? json.get(key).getAsString() : null;
    }

    private Integer parseIntOrNull(final String value) {
        if (value == null || value.trim().isEmpty()) return null;
        try {
            return Integer.parseInt(value);
        } catch (final NumberFormatException e) {
            return null;
        }
    }

    private int parseIntOrDefault(final String value, final int defaultVal) {
        if (value == null || value.trim().isEmpty()) return defaultVal;
        try {
            return Integer.parseInt(value);
        } catch (final NumberFormatException e) {
            return defaultVal;
        }
    }

    private Date parseDateOrNull(final String value) {
        if (value == null || value.trim().isEmpty()) return null;
        try {
            return Date.valueOf(value);
        } catch (final IllegalArgumentException e) {
            return null;
        }
    }

    private java.util.Date parseDateFromString(final String dateStr) {
        if (dateStr == null || dateStr.isEmpty()) return null;
        try {
            return new SimpleDateFormat("yyyy-MM-dd").parse(dateStr);
        } catch (final ParseException e) {
            return null;
        }
    }

    private boolean ensureAuthenticated(final HttpServletRequest request,
                                        final HttpServletResponse response,
                                        final JsonObject responseJson) {
        final HttpSession session = request.getSession(false);
        final Object userId = session != null ? session.getAttribute("userId") : null;
        if (userId != null) {
            return true;
        }

        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        responseJson.addProperty("status", "FAILURE");
        responseJson.addProperty("message", "Session expired. Please login again.");
        return false;
    }
}
