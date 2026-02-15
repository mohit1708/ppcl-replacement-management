package com.ppcl.replacement.servlet;

import com.google.gson.Gson;
import com.ppcl.replacement.constants.AppConstants;
import com.ppcl.replacement.dao.*;
import com.ppcl.replacement.model.*;
import com.ppcl.replacement.util.DBConnectionPool;
import com.ppcl.replacement.util.FileUploadUtil;
import com.ppcl.replacement.util.JsonResponse;
import com.ppcl.replacement.util.ServletUtil;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.servlet.http.Part;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@WebServlet("/views/replacement/request")
@MultipartConfig(
        fileSizeThreshold = 1024 * 1024,      // 1 MB
        maxFileSize = 10 * 1024 * 1024,       // 10 MB
        maxRequestSize = 15 * 1024 * 1024     // 15 MB
)
public class ReplacementRequestServlet extends BaseServlet {

    private final Gson gson = new Gson();
    private final PrinterPullbackDAO printerPullbackDAO = new PrinterPullbackDAO();
    TransitionWorkflowDao transitionWorkflowDao = new TransitionWorkflowDao();

    @Override
    protected void doGet(final HttpServletRequest request, final HttpServletResponse response)
            throws ServletException, IOException {

        final String action = request.getParameter("action");
        final int userId = getSessionUserId(request);

        if (userId == 0) {

            if (isAjaxRequestInternal(request, action)) {
                sendJsonError(response, "Session expired. Please login again.");
            } else {
                response.sendRedirect(request.getContextPath() + "/login.jsp?error=session_expired");
            }
            return;
        }
        // Check if user is logged in
        int usrId = userId;
        try {
            if ("new".equals(action)) {
                showCreateForm(request, response);
            } else if ("prefill".equals(action)) {
                showPrefillForm(request, response);
            } else if ("myList".equals(action)) {
                showMyRequests(request, response);
            } else if ("view".equals(action)) {
                showViewRequest(request, response);
            } else if ("getClientBranches".equals(action)) {
                getClientBranches(request, response);
            } else if ("getBranchDetails".equals(action)) {
                getBranchDetails(request, response);
            } else if ("getClientLocations".equals(action)) {
                // locations are branches (CLIENT rows) under a logical CLIENT.CLIENT_ID
                getClientLocations(request, response);
            } else if ("getPrintersByLocations".equals(action)) {
                getPrintersByLocations(request, response);
            } else if ("getAllPrinterModels".equals(action)) {
                getAllPrinterModels(request, response);
            } else if ("getDetails".equals(action)) {
                getRequestDetails(request, response);
            } else if ("getBookingDetails".equals(action)) {
                getBookingDetails(request, response, usrId);
            } else if ("getCreditNoteDetails".equals(action)) {
                getCreditNoteDetails(request, response);
            } else if ("getPendingCreditNotes".equals(action)) {
                getPendingCreditNotes(request, response);
            } else if ("getCreditNotesForApproval".equals(action)) {
                getCreditNotesForApproval(request, response);
            } else if ("getAgreementDetails".equals(action)) {
                getAgreementDetails(request, response);
            } else if ("getCommentHistory".equals(action)) {
                getCommentHistory(request, response);
            } else {
                response.sendRedirect(request.getContextPath() + "/views/replacement/dashboard");
            }
        } catch (final Exception e) {
            e.printStackTrace();

            // Graceful error handling
            if (e.getMessage() != null &&
                    (e.getMessage().contains("Network Adapter") ||
                            e.getMessage().contains("connection"))) {

                // For AJAX requests, send JSON error
                if ("getClientBranches".equals(action) ||
                        "getBranchDetails".equals(action) ||
                        "getClientLocations".equals(action) ||
                        "getClientLocations".equals(action) ||
                        "getPrintersByLocations".equals(action) ||
                        "getAllPrinterModels".equals(action)) {
                    sendJsonError(response, "Database connection error: " + e.getMessage());
                } else {
                    request.setAttribute("error", "Database connection error");
                    request.setAttribute("errorDetails", e.getMessage());
                    request.getRequestDispatcher("/error.jsp").forward(request, response);
                }
            } else {
                sendJsonError(response, e.getMessage());
            }
        }
    }

    private void showMyRequests(final HttpServletRequest request, final HttpServletResponse response)
            throws Exception {

        final HttpSession session = request.getSession();

        int userId = getSessionUserId(request);
        String userRole = "AM";
        final UserDAO userDAO = new UserDAO();
        User user = null;
        if (userId > 0) {
            user = userDAO.getUserByUserId(userId);
            userRole = user.getRole();
            session.setAttribute("roleId", user.getRoleId());
            session.setAttribute("roleCode", user.getRole());
        }

        //(String) session.getAttribute("roleCode");

        try {
            final ReplacementRequestDAO dao = new ReplacementRequestDAO();
            final List<MyRequestRow> requests = dao.getMyPendingRequests(userId, userRole);
            request.setAttribute("requests", requests);
        } catch (final Exception e) {
            request.setAttribute("requests", new ArrayList<MyRequestRow>());
            request.setAttribute("dbError", "Could not connect to database");
        }

        forwardToJsp(request, response, "myRequests.jsp");
    }

    private void showViewRequest(final HttpServletRequest request, final HttpServletResponse response)
            throws Exception {
        final String idStr = request.getParameter("id");
        if (idStr == null || idStr.isEmpty()) {
            response.sendRedirect(request.getContextPath() + "/views/replacement/request?action=myList");
            return;
        }

        try {
            final int requestId = Integer.parseInt(idStr);
            final ReplacementRequestDAO dao = new ReplacementRequestDAO();
            final ReplacementRequest reqData = dao.getRequestById(requestId);

            if (reqData == null) {
                request.setAttribute("error", "Request not found");
                response.sendRedirect(request.getContextPath() + "/views/replacement/request?action=myList&error=notfound");
                return;
            }

            request.setAttribute("reqData", reqData);
        } catch (final NumberFormatException e) {
            response.sendRedirect(request.getContextPath() + "/views/replacement/request?action=myList&error=invalid");
            return;
        } catch (final Exception e) {
            e.printStackTrace();
            request.setAttribute("error", "Could not load request: " + e.getMessage());
        }

        forwardToJsp(request, response, "viewRequest.jsp");
    }

    private void showCreateForm(final HttpServletRequest request, final HttpServletResponse response)
            throws Exception {
        // Access control: Only CRO(2), AM(4), AM Manager(261) can create requests
        if (!checkCreateRequestAccess(request, response)) {
            return;
        }
        loadCreateFormData(request);
        forwardToJsp(request, response, "createRequest.jsp");
    }

    /**
     * Show create form with prefilled data from URL parameters.
     * <p>
     * Example URL:
     * /views/replacement/request?action=prefill&clientId=123&serial=ABC123&pModelId=10&contactName=John%20Doe&contactPhone=9876543210&reasonId=1&tlId=5
     * <p>
     * Supported parameters:
     * - clientId: Client ID to preselect
     * - contactName: Contact person name
     * - contactEmail: Contact email address
     * - contactPhone: Contact phone number
     * - reasonId: Replacement reason ID
     * - comments: Initial comments
     * - tlId: Assigned TL ID
     *   GET  /views/replacement/request?action=prefill&clientId={clientId}&serial={serial}&pModelId={pModelId}&contactName={name}&contactEmail={email}&contactPhone={phone}&     █
     *       reasonId={reasonId}&comments={comments}&tlId={tlId}                                                                                                                 █
     */
    private void showPrefillForm(final HttpServletRequest request, final HttpServletResponse response)
            throws Exception {
        // Access control: Only CRO(2), AM(4), AM Manager(261) can create requests
        if (!checkCreateRequestAccess(request, response)) {
            return;
        }

        // Check for duplicate request by serial number before loading form
        final String serial = request.getParameter("serial");
        if (serial != null && !serial.trim().isEmpty()) {
            final ReplacementRequestDAO dao = new ReplacementRequestDAO();
            final Integer existingReqId = dao.checkDuplicateBySerial(serial.trim());
            if (existingReqId != null) {
                request.setAttribute("duplicateError",
                        "Replacement Request already exists for Serial: " + serial.trim()
                                + " (Request ID: " + existingReqId + ")");
            }
        }

        loadCreateFormData(request);

        // Resolve representative client ID for dropdown matching
        final String clientIdParam = request.getParameter("clientId");
        String resolvedClientId = clientIdParam;
        if (clientIdParam != null && !clientIdParam.trim().isEmpty() && clientIdParam.matches("\\d+")) {
            final ClientDAO clientDAO = new ClientDAO();
            final Integer repId = clientDAO.getRepresentativeClientId(Integer.parseInt(clientIdParam.trim()));
            if (repId != null) {
                resolvedClientId = String.valueOf(repId);
            }
        }

        // Set prefill attributes from URL parameters
        final Map<String, String> prefillData = new HashMap<>();
        prefillData.put("clientId", resolvedClientId);
        prefillData.put("contactName", request.getParameter("contactName"));
        prefillData.put("contactEmail", request.getParameter("contactEmail"));
        prefillData.put("contactPhone", request.getParameter("contactPhone"));
        prefillData.put("reasonId", request.getParameter("reasonId"));
        prefillData.put("comments", request.getParameter("comments"));
        prefillData.put("tlId", request.getParameter("tlId"));
        prefillData.put("signInBranchId", clientIdParam);
        prefillData.put("serial", serial);
        prefillData.put("pModelId", request.getParameter("pModelId"));

        request.setAttribute("prefillData", prefillData);
        request.setAttribute("isPrefill", true);

        forwardToJsp(request, response, "createRequest.jsp");
    }

    private void loadCreateFormData(final HttpServletRequest request) {
        try {
            final ClientDAO clientDAO = new ClientDAO();
            final PrinterDAO printerDAO = new PrinterDAO();
            final ReplacementRequestDAO reqDAO = new ReplacementRequestDAO();
            final UserDAO userDAO = new UserDAO();

            final List<Client> clients = clientDAO.getDistinctClients();
            final List<ReplacementReason> reasons = printerDAO.getAllActiveReasons();
            final List<User> tlLeads = userDAO.getAllTLLeads();

            request.setAttribute("clients", clients);
            request.setAttribute("reasons", reasons);
            request.setAttribute("tlLeads", tlLeads);

            final HttpSession session = request.getSession();
            final int userId = getSessionUserId(request);
            final List<MyRequestRow> requests = reqDAO.getRequestsAtServiceTLReview(userId);
            request.setAttribute("requests", requests);
        } catch (final Exception e) {
            e.printStackTrace();
            request.setAttribute("clients", new ArrayList<Client>());
            request.setAttribute("reasons", new ArrayList<ReplacementReason>());
            request.setAttribute("tlLeads", new ArrayList<User>());
            request.setAttribute("requests", new ArrayList<MyRequestRow>());
            request.setAttribute("dbError", "Could not connect to database: " + e.getMessage());
        }
    }

    private void getBookingDetails(final HttpServletRequest request, final HttpServletResponse response, Integer userId) throws Exception {
        final int reqId;
        try {
            reqId = ServletUtil.requireIntParam(request, "id");
        } catch (final IllegalArgumentException e) {
            sendJsonError(response, e.getMessage());
            return;
        }

        final PrinterBookingDAO dao = new PrinterBookingDAO();


        final Map<String, Object> result = dao.getPrinterDetails(reqId); // :contentReference[oaicite:2]{index=2}

        // Add signatories array that the JS expects
        final List<User> signatories = dao.getSignatories(userId);             // :contentReference[oaicite:3]{index=3}
        result.put("signatories", signatories);

        // IMPORTANT:
        // bookPrinterOrder.js calls _populateModal(data) assuming request/printers/signatories
        // so we must NOT wrap inside "data": {...}
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        final Map<String, Object> payload = new HashMap<>();
        payload.put("success", true);
        payload.put("message", "Replacement Request loaded");
        payload.putAll(result); // <-- flatten response

        response.getWriter().print(gson.toJson(payload));
    }

    /**
     * After selecting a Client (value = representative CLIENT.ID), load all branches for that
     * logical client (CLIENT.CLIENT_ID).
     */
    private void getClientBranches(final HttpServletRequest request, final HttpServletResponse response)
            throws Exception {

        final int anyBranchId;
        try {
            anyBranchId = ServletUtil.requireIntParam(request, "clientId");
        } catch (final IllegalArgumentException e) {
            sendJsonError(response, e.getMessage());
            return;
        }

        final ClientDAO dao = new ClientDAO();
        final String logicalClientId = dao.getLogicalClientIdByBranchId(anyBranchId);
        if (logicalClientId == null) {
            sendJsonError(response, "Client not found");
            return;
        }

        final List<Client> branches = dao.getBranchesByLogicalClientId(logicalClientId);

        final List<Map<String, Object>> result = new ArrayList<>();
        for (final Client b : branches) {
            final Map<String, Object> m = new HashMap<>();
            m.put("id", b.getId());
            m.put("branch", b.getBranch());
            m.put("address", b.getAddress());
            m.put("city", b.getCity());
            m.put("state", b.getState());
            result.add(m);
        }

        sendJsonSuccess(response, "Branches loaded", result);
    }

    /**
     * When user selects Sign-In branch (CLIENT.ID), auto-fill contact details.
     */
    private void getBranchDetails(final HttpServletRequest request, final HttpServletResponse response)
            throws Exception {

        final int branchId;
        try {
            branchId = ServletUtil.requireIntParam(request, "branchId");
        } catch (final IllegalArgumentException e) {
            sendJsonError(response, e.getMessage());
            return;
        }

        final ClientDAO dao = new ClientDAO();
        final Client branch = dao.getClientById(branchId);
        if (branch == null) {
            sendJsonError(response, "Branch not found");
            return;
        }

        final Map<String, Object> result = new HashMap<>();
        result.put("contactPerson", branch.getContactPerson());
        result.put("mobileNo", branch.getMobileNo());
        result.put("emailId1", branch.getEmailId1());

        sendJsonSuccess(response, "Branch details loaded", result);
    }

    private void getClientLocations(final HttpServletRequest request, final HttpServletResponse response)
            throws Exception {

        final int anyBranchId;
        try {
            anyBranchId = ServletUtil.requireIntParam(request, "clientId");
        } catch (final IllegalArgumentException e) {
            sendJsonError(response, e.getMessage());
            return;
        }

        final ClientDAO dao = new ClientDAO();
        final String logicalClientId = dao.getLogicalClientIdByBranchId(anyBranchId);
        if (logicalClientId == null) {
            sendJsonError(response, "Client not found");
            return;
        }

        // Locations are branches under the same logical client id.
        final List<Client> locations = dao.getBranchesByLogicalClientId(logicalClientId);

        if (locations != null && !locations.isEmpty()) {
            final List<Map<String, Object>> result = new ArrayList<>();

            for (final Client loc : locations) {
                final Map<String, Object> locMap = new HashMap<>();
                locMap.put("id", loc.getId());
                locMap.put("branch", loc.getBranch());
                locMap.put("address", loc.getAddress());
                locMap.put("city", loc.getCity());
                locMap.put("state", loc.getState());
                result.add(locMap);
            }

            sendJsonSuccess(response, "Locations loaded", result);
        } else {
            sendJsonError(response, "No locations found");
        }
    }

    private void getPrintersByLocations(final HttpServletRequest request, final HttpServletResponse response)
            throws Exception {

        final String locationIdsJson = request.getParameter("locationIds");
        final int[] locationIdsArray = gson.fromJson(locationIdsJson, int[].class);
        final List<Integer> locationIds = new ArrayList<>();
        for (final int id : locationIdsArray) {
            locationIds.add(id);
        }

        final PrinterDAO dao = new PrinterDAO();
        final List<PrinterDetail> printers = dao.getPrintersByLocations(locationIds);

        if (printers != null) {
            final List<Map<String, Object>> result = new ArrayList<>();

            for (final PrinterDetail p : printers) {
                final Map<String, Object> printerMap = new HashMap<>();
                printerMap.put("agrProdId", p.getAgrProdId());
                printerMap.put("pModelId", p.getPModelId());
                printerMap.put("serial", p.getSerial());
                printerMap.put("modelName", p.getModelName());
                printerMap.put("clientBrId", p.getClientBrId());
                // Include existing request ID for duplicate detection
                if (p.getExistingRequestId() != null) {
                    printerMap.put("existingRequestId", p.getExistingRequestId());
                }
                result.add(printerMap);
            }

            sendJsonSuccess(response, "Printers loaded", result);
        } else {
            sendJsonError(response, "No printers found");
        }
    }

    private void getCommentHistory(final HttpServletRequest request, final HttpServletResponse response)
            throws Exception {

        final String reqIdStr = request.getParameter("id");
        if (reqIdStr == null || reqIdStr.isEmpty()) {
            sendJsonError(response, "Request ID is required");
            return;
        }

        final int reqId = Integer.parseInt(reqIdStr);
        final ReplacementRequestDAO dao = new ReplacementRequestDAO();
        final List<Map<String, Object>> comments = dao.getCommentHistory(reqId);

        final Map<String, Object> result = new HashMap<>();
        result.put("comments", comments);
        sendJsonSuccess(response, "Comment history loaded", result);
    }

    private void getAllPrinterModels(final HttpServletRequest request, final HttpServletResponse response)
            throws Exception {

        final ReplacementRequestDAO dao = new ReplacementRequestDAO();
        final List<PrinterModel> models = dao.getAllPrinterModels();

        final List<Map<String, Object>> result = new ArrayList<>();
        for (final PrinterModel m : models) {
            final Map<String, Object> modelMap = new HashMap<>();
            modelMap.put("id", m.getId());
            modelMap.put("modelName", m.getModelName());
            result.add(modelMap);
        }

        sendJsonSuccess(response, "Printer models loaded", result);
    }

    @Override
    protected void doPost(final HttpServletRequest request, final HttpServletResponse response)
            throws ServletException, IOException {

        final String action = request.getParameter("action");
        final int userId = getSessionUserId(request);

        // Check if user is logged in
        if (userId == 0) {
            sendJsonError(response, "Session expired. Please login again.");
            return;
        }

        try {
            if (ServletUtil.isJsonRequest(request)) {
                handleJsonPost(request, response);
                return;
            }

            if ("create".equals(action)) {
                createRequest(request, response);
            } else if ("remind".equals(action)) {
                sendReminder(request, response);
            } else if ("quotationSent".equals(action)) {
                handleStageAction(request, response, 4, 5, false);
            } else if ("approvalReceived".equals(action)) {
                handleStageAction(request, response, 5, 6, true);
            } else if ("bookOrder".equals(action)) {
                handleBookOrder(request, response);
            } else if ("submitCreditNote".equals(action)) {
                submitCreditNote(request, response);
            } else if ("creditNoteNoAction".equals(action)) {
                creditNoteNoAction(request, response);
            } else if ("forwardCreditNote".equals(action)) {
                forwardCreditNote(request, response);
            } else if ("approveCreditNote".equals(action)) {
                approveCreditNote(request, response);
            }
        } catch (final Exception e) {
            e.printStackTrace();
            sendJsonError(response, e.getMessage());
        }
    }

    private void handleJsonPost(final HttpServletRequest request, final HttpServletResponse response)
            throws Exception {

        final String body = ServletUtil.readRequestBody(request);

        @SuppressWarnings("unchecked") final Map<String, Object> jsonData = gson.fromJson(body, Map.class);
        final String action = (String) jsonData.get("action");

        if ("update".equals(action)) {
            updateRequest(jsonData, request, response);
        } else if ("closeRequest".equals(action)) {
            closeRequest(jsonData, request, response);
        } else {
            sendJsonError(response, "Unknown action: " + action);
        }
    }

    private void closeRequest(final Map<String, Object> jsonData, final HttpServletRequest request,
                              final HttpServletResponse response) throws Exception {
        final HttpSession session = request.getSession();
        final int userId = getSessionUserId(request);

        final Object reqIdObj = jsonData.get("reqId");
        final int reqId = Integer.parseInt(String.valueOf(reqIdObj));
        final String reason = (String) jsonData.get("reason");

        if (reason == null || reason.trim().isEmpty()) {
            sendJsonError(response, "Reason is required");
            return;
        }

        final ReplacementRequestDAO dao = new ReplacementRequestDAO();
        final boolean success = dao.closeRequest(reqId, reason, userId);

        if (success) {
            sendJsonSuccess(response, "Request closed successfully", null);
        } else {
            sendJsonError(response, "Failed to close request");
        }
    }

    private void updateRequest(final Map<String, Object> jsonData, final HttpServletRequest request,
                               final HttpServletResponse response) throws Exception {

        final HttpSession session = request.getSession();
        final int userId = getSessionUserId(request);
        // Extract data from JSON
        final Object reqIdObj = jsonData.get("reqId");
        final int reqId = Integer.parseInt(String.valueOf(reqIdObj));

        //int reqId = ((Integer) jsonData.get("reqId")).intValue();

        final String replacementType = (String) jsonData.get("replacementType");
        // int reasonId = ((Integer) jsonData.get("reasonId")).intValue();

        final Object reasonIdObj = jsonData.get("reasonId");
        final int reasonId = Integer.parseInt(String.valueOf(reasonIdObj));

        // Sign-In Branch ID (location change support)
        final Object signInBranchIdObj = jsonData.get("signInBranchId");
        final int signInBranchId = signInBranchIdObj != null && !signInBranchIdObj.toString().isEmpty()
                ? Integer.parseInt(String.valueOf(signInBranchIdObj)) : 0;

        final String comments = (String) jsonData.get("comments");
        final String contactName = (String) jsonData.get("contactName");
        final String contactNumber = (String) jsonData.get("contactNumber");
        final String contactEmail = (String) jsonData.get("contactEmail");

        @SuppressWarnings("unchecked") final List<Map<String, Object>> printers = (List<Map<String, Object>>) jsonData.get("printers");

        // Update in database
        final ReplacementRequestDAO dao = new ReplacementRequestDAO();
        dao.updateReplacementRequest(reqId, replacementType, reasonId, signInBranchId, comments,
                contactName, contactNumber, contactEmail, printers, userId);

        sendJsonSuccess(response, "Request updated successfully", null);
    }

    private void createRequest(final HttpServletRequest request, final HttpServletResponse response)
            throws Exception {

        final HttpSession session = request.getSession();
        final int userId = getSessionUserId(request);
        final String userRole = (String) session.getAttribute("roleCode");

        // Get form parameters
        final String signInBranchId = request.getParameter("signInBranchId"); // CLIENT.ID for signing location
        final String replacementType = request.getParameter("replacementType");
        final int reasonId = Integer.parseInt(request.getParameter("reasonId"));
        final String comments = request.getParameter("comments");
        final int tlLeadId = Integer.parseInt(request.getParameter("tlLeadId"));

        final String clientSiginContactName = request.getParameter("clientContactName");
        final String clientSiginContactNumber = request.getParameter("clientContactNumber");
        final String clientSiginContactEmail = request.getParameter("clientContactEmail");
        // Parse selected printers
        final List<ReplacementPrinter> printers = new ArrayList<>();
        final Map<String, String[]> params = request.getParameterMap();


        for (final String key : params.keySet()) {
            if (key.matches("printers\\[.*\\]\\.selected")) {
                final String printerKey = key.substring(9, key.indexOf("].selected"));

                final String clientBrIdStr = request.getParameter("printers[" + printerKey + "].clientBrId");
                final String agrProdIdStr = request.getParameter("printers[" + printerKey + "].agrProdId");
                final String pModelIdStr = request.getParameter("printers[" + printerKey + "].pModelId");
                final String serial = request.getParameter("printers[" + printerKey + "].serial");
                final String newModelIdStr = request.getParameter("printers[" + printerKey + "].newModelId");
                final String newModelText = request.getParameter("printers[" + printerKey + "].newModelText");

                if (clientBrIdStr != null && agrProdIdStr != null) {
                    final ReplacementPrinter printer = new ReplacementPrinter();
                    final int printerClientBrId = Integer.parseInt(clientBrIdStr);
                    printer.setClientBrId(printerClientBrId);
                    printer.setAgrProdId(Integer.parseInt(agrProdIdStr));
                    printer.setExistingPModelId(Integer.parseInt(pModelIdStr));
                    printer.setExistingSerial(serial);

                    // Handle new model selection (dropdown vs manual)
                    if (newModelIdStr != null && !newModelIdStr.isEmpty()) {
                        printer.setNewPModelSelectedId(Integer.parseInt(newModelIdStr));
                        printer.setNewPModelSource("AUTO");
                    } else if (newModelText != null && !newModelText.trim().isEmpty()) {
                        printer.setNewModelText(newModelText.trim());
                        printer.setNewPModelSource("MANUAL");
                    } else {
                        printer.setNewPModelSource("MANUAL");
                    }

                    // Fetch contact info from the printer's specific CLIENT location
                    final ClientDAO clientDAO = new ClientDAO();
                    final Client printerClient = clientDAO.getClientById(printerClientBrId);
                    if (printerClient != null) {
                        printer.setContactPerson(printerClient.getContactPerson());
                        printer.setContactNumber(printerClient.getMobileNo());
                        printer.setContactEmail(printerClient.getEmailId1());
                    } else {
                        // Fallback to form contact if client not found
                        printer.setContactPerson(request.getParameter("clientContactName"));
                        printer.setContactNumber(request.getParameter("clientContactNumber"));
                        printer.setContactEmail(request.getParameter("clientContactEmail"));
                    }

                    printers.add(printer);
                }
            }
        }

        if (printers.isEmpty()) {
            response.sendRedirect(request.getContextPath() +
                    "/views/replacement/request?action=new&error=no_printers_selected");
            return;
        }

        final ReplacementRequestDAO dao = new ReplacementRequestDAO();

        // Check for duplicate requests before creating
        final Map<String, Integer> duplicates = dao.checkDuplicatesForPrinters(printers);
        if (!duplicates.isEmpty()) {
            // Build error message with all duplicates
            final StringBuilder errorMsg = new StringBuilder("Duplicate Request Exists - ");
            for (final Map.Entry<String, Integer> entry : duplicates.entrySet()) {
                errorMsg.append("Serial: ").append(entry.getKey())
                        .append(" already in Request ID: ").append(entry.getValue()).append("; ");
            }
            // Forward back to form with error - preserves form data
            request.setAttribute("duplicateError", errorMsg.toString());
            request.setAttribute("duplicatePrinterSerials", duplicates.keySet());

            // Store submitted form data to repopulate
            final Map<String, Object> formData = new HashMap<>();
            formData.put("clientId", request.getParameter("clientId"));
            formData.put("signInBranchId", signInBranchId);
            formData.put("replacementType", replacementType);
            formData.put("reasonId", reasonId);
            formData.put("tlLeadId", tlLeadId);
            formData.put("comments", comments);
            formData.put("contactName", clientSiginContactName);
            formData.put("contactNumber", clientSiginContactNumber);
            formData.put("contactEmail", clientSiginContactEmail);

            // Store printer data for repopulation
            final List<Map<String, Object>> printerData = new ArrayList<>();
            for (final ReplacementPrinter p : printers) {
                final Map<String, Object> pd = new HashMap<>();
                pd.put("clientBrId", p.getClientBrId());
                pd.put("agrProdId", p.getAgrProdId());
                pd.put("pModelId", p.getExistingPModelId());
                pd.put("serial", p.getExistingSerial());
                pd.put("newModelId", p.getNewPModelSelectedId());
                pd.put("newModelText", p.getNewModelText());
                pd.put("isDuplicate", duplicates.containsKey(p.getExistingSerial()));
                printerData.add(pd);
            }
            formData.put("printers", printerData);

            request.setAttribute("savedFormData", gson.toJson(formData));

            loadCreateFormData(request);
            forwardToJsp(request, response, "createRequest.jsp");
            return;
        }

        // Create request object
        final ReplacementRequest req = new ReplacementRequest();
        req.setClientId(signInBranchId); // CLIENT_DOT_ID_SIGNING
        req.setReplacementType(replacementType);
        req.setReasonId(reasonId);
        req.setTlLeadId(tlLeadId);
        req.setSource("DIRECT"); // Source is DIRECT for form submissions
        req.setPrinters(printers);
        req.setComments(comments);
        req.setContactPerson(clientSiginContactName);
        req.setContactNumber(clientSiginContactNumber);
        req.setContactEmail(clientSiginContactEmail);

        final int reqId = dao.createReplacementRequest(req, userId, userRole);

        response.sendRedirect(request.getContextPath() +
                "/views/replacement/request?action=myList&success=created&id=" + reqId);
    }

    private void sendReminder(final HttpServletRequest request, final HttpServletResponse response)
            throws Exception {

        final HttpSession session = request.getSession();
        final int userId = getSessionUserId(request);
        final int reqId = Integer.parseInt(request.getParameter("reqId"));
        final User user = (User) session.getAttribute("currentUser");
        final ReplacementRequestDAO dao = new ReplacementRequestDAO();
        final Integer nextOwner = dao.getNextStageOwnerUserId(reqId);

        if (nextOwner == null) {
            sendJsonError(response, "Next stage owner not found");
            return;
        }

        dao.insertReminderChatLog(reqId, user.getId(), nextOwner);
        sendJsonSuccess(response, "Reminder sent successfully", null);
    }

    @Override
    protected void sendJsonSuccess(final HttpServletResponse response, final String message, final Object data)
            throws IOException {
        JsonResponse.sendSuccess(response, message, data);
    }

    @Override
    protected void sendJsonError(final HttpServletResponse response, final String message)
            throws IOException {
        JsonResponse.sendError(response, message);
    }

    /**
     * Get request details for edit modal with TAT info
     */
    private void getRequestDetails(final HttpServletRequest request, final HttpServletResponse response)
            throws Exception {

        try {
            final int reqId = ServletUtil.requireIntParam(request, "id");
            final ReplacementRequestDAO dao = new ReplacementRequestDAO();
            final Map<String, Object> result = dao.getRequestDetailsPayload(reqId);

            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");
            response.getWriter().print(gson.toJson(result));
        } catch (final IllegalArgumentException e) {
            sendJsonError(response, e.getMessage());
        } catch (final Exception e) {
            e.printStackTrace();
            final Map<String, Object> result = new HashMap<>();
            result.put("success", false);
            result.put("message", "Error: " + e.getMessage());
            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");
            response.getWriter().print(gson.toJson(result));
        }
    }

    /**
     * Handle stage transition for AM actions (Quotation Sent, Approval Received)
     */
    private void handleStageAction(final HttpServletRequest request, final HttpServletResponse response,
                                   final int fromStage, final int toStage, final boolean requireUpload) throws Exception {

        final HttpSession session = request.getSession();
        final User user = (User) session.getAttribute("currentUser");
        final int reqId = Integer.parseInt(request.getParameter("reqId"));
        final String comments = request.getParameter("comments");

        if (requireUpload && isMultipart(request)) {
            final Part filePart = request.getPart("quotationFile");
            if (filePart != null && filePart.getSize() > 0) {
                FileUploadUtil.uploadFile(
                        getServletContext().getInitParameter("quotation.dir"),
                        filePart,
                        "quotation_req_" + reqId
                );
            }
        }

        transitionWorkflowDao.transitionStage(reqId, toStage, user.getId(), comments);

        sendJsonSuccess(response, "Stage updated successfully", null);
    }

    /**
     * Handle Book Order action (STG6_PRINTER_ORDER)
     */
    private void handleBookOrder(final HttpServletRequest request, final HttpServletResponse response) throws Exception {

        final HttpSession session = request.getSession();
        final int userId = getSessionUserId(request);
        final int reqId = Integer.parseInt(request.getParameter("reqId"));
        final String orderRef = request.getParameter("orderRef");
        final String deliveryDate = request.getParameter("deliveryDate");
        final String comments = request.getParameter("comments");

        final ReplacementRequestDAO dao = new ReplacementRequestDAO();
        dao.bookPrinterOrder(reqId, orderRef, deliveryDate, comments, userId);

        // After successful printer booking, process printer pullback
        processPrinterPullbackAfterBooking(reqId);

        sendJsonSuccess(response, "Printer order booked successfully", null);
    }

    private boolean isMultipart(final HttpServletRequest request) {
        final String contentType = request.getContentType();
        return contentType != null && contentType.toLowerCase().startsWith("multipart/");
    }

    /**
     * Invokes the printer pullback processing after a successful printer booking.
     * Separated into its own method so pullback logic remains modular and reusable.
     *
     * @param requestId the replacement request ID
     */
    private void processPrinterPullbackAfterBooking(final int requestId) {
        try {
            printerPullbackDAO.processPrinterPullbackAfterBooking(requestId);
        } catch (final Exception e) {
            e.printStackTrace();
            System.err.println("WARNING [ReplacementRequestServlet]: Printer pullback processing failed for request ID: "
                    + requestId + ". Error: " + e.getMessage());
        }
    }

    /**
     * Get credit note details for STG11_CREDIT_NOTE stage
     */
    private void getCreditNoteDetails(final HttpServletRequest request, final HttpServletResponse response)
            throws Exception {

        final int reqId;
        try {
            reqId = ServletUtil.requireIntParam(request, "id");
        } catch (final IllegalArgumentException e) {
            sendJsonError(response, e.getMessage());
            return;
        }

        final PrinterBookingDAO dao = new PrinterBookingDAO();

        // Get request and printer details (reusing existing method)
        final Map<String, Object> result = dao.getPrinterDetails(reqId);

        // Transform printers for credit note display
        @SuppressWarnings("unchecked") final List<Map<String, Object>> printers = (List<Map<String, Object>>) result.get("printers");
        final List<Map<String, Object>> creditNotePrinters = new ArrayList<>();

        if (printers != null) {
            for (final Map<String, Object> p : printers) {
                final Map<String, Object> cnp = new HashMap<>();
                cnp.put("location", p.get("location"));
                cnp.put("modelName", p.get("existingModel") != null ? p.get("existingModel") : p.get("newModelName"));
                cnp.put("serialNo", p.get("existingSerial"));
                cnp.put("issueDescription", "Replacement");
                // Default agreement rate - can be fetched from commercial agreement if available
                cnp.put("agreementRate", 5000);
                creditNotePrinters.add(cnp);
            }
        }

        final Map<String, Object> payload = new HashMap<>();
        payload.put("success", true);
        payload.put("request", result.get("request"));
        payload.put("printers", creditNotePrinters);

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().print(gson.toJson(payload));
    }

    /**
     * Submit credit note and forward to billing
     */
    private void submitCreditNote(final HttpServletRequest request, final HttpServletResponse response)
            throws Exception {

        final int reqId;
        try {
            reqId = ServletUtil.requireIntParam(request, "reqId");
        } catch (final IllegalArgumentException e) {
            sendJsonError(response, e.getMessage());
            return;
        }

        final HttpSession session = request.getSession();
        final User currentUser = (User) session.getAttribute("currentUser");
        final int userId = currentUser.getId();
        final String comments = ServletUtil.optionalParam(request, "comments");

        // JS sends locationData (savedLocations object with amounts per location)
        final String locationDataJson = ServletUtil.optionalParam(request, "locationData");
        // Also check for printers param (legacy/alternative format)
        final String printersJson = ServletUtil.optionalParam(request, "printers");

        final String commentText = (comments != null && !comments.trim().isEmpty())
                ? "Credit Note submitted: " + comments
                : "Credit Note submitted and forwarded to Billing";

        try (final java.sql.Connection con = DBConnectionPool.getConnection()) {
            con.setAutoCommit(false);
            try {
                final CreditNoteDAO creditNoteDAO = new CreditNoteDAO();

                // Handle locationData format from JS (savedLocations object)
                if (locationDataJson != null && !locationDataJson.isEmpty()) {
                    // Get printer details from database
                    final PrinterBookingDAO printerBookingDAO = new PrinterBookingDAO();
                    final Map<String, Object> printerData = printerBookingDAO.getPrinterDetails(reqId);
                    @SuppressWarnings("unchecked") final List<Map<String, Object>> dbPrinters = (List<Map<String, Object>>) printerData.get("printers");

                    // Parse locationData to get amounts
                    @SuppressWarnings("unchecked") final Map<String, Map<String, Object>> savedLocations = gson.fromJson(locationDataJson,
                            new com.google.gson.reflect.TypeToken<Map<String, Map<String, Object>>>() {
                            }.getType());

                    // Build printer list with credit amounts from saved locations
                    final List<Map<String, Object>> printersWithAmounts = new ArrayList<>();
                    if (dbPrinters != null) {
                        int printerIndex = 0;
                        for (final Map<String, Object> p : dbPrinters) {
                            final Map<String, Object> creditPrinter = new HashMap<>();
                            creditPrinter.put("printerDetailId", p.get("id"));
                            creditPrinter.put("location", p.get("location") + ", " + p.get("city"));
                            creditPrinter.put("modelName", p.get("existingModel"));
                            creditPrinter.put("serialNo", p.get("serial"));
                            creditPrinter.put("issueDescription", "Replacement");
                            creditPrinter.put("agreementRate", p.get("agreementRate"));

                            // Try to find credit amount from savedLocations
                            double creditAmount = 0;
                            for (final Map.Entry<String, Map<String, Object>> entry : savedLocations.entrySet()) {
                                final Map<String, Object> locData = entry.getValue();
                                @SuppressWarnings("unchecked") final List<Number> amounts = (List<Number>) locData.get("amounts");
                                if (amounts != null && printerIndex < amounts.size()) {
                                    creditAmount = amounts.get(printerIndex).doubleValue();
                                    break;
                                }
                            }
                            creditPrinter.put("creditAmount", creditAmount);
                            printersWithAmounts.add(creditPrinter);
                            printerIndex++;
                        }
                    }

                    if (!printersWithAmounts.isEmpty()) {
                        creditNoteDAO.insertCreditNotes(con, reqId, printersWithAmounts, comments, userId);
                    }
                }
                // Handle printers param (direct printer list)
                else if (printersJson != null && !printersJson.isEmpty()) {
                    final List<Map<String, Object>> printers = gson.fromJson(printersJson,
                            new com.google.gson.reflect.TypeToken<List<Map<String, Object>>>() {
                            }.getType());
                    creditNoteDAO.insertCreditNotes(con, reqId, printers, comments, userId);
                }

                // Move to next stage using transitionFlow
                transitionWorkflowDao.transitionFlow(con, reqId, commentText);

                con.commit();
            } catch (final Exception e) {
                con.rollback();
                throw e;
            }
        }

        final Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("message", "Credit note forwarded to Billing successfully");

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().print(gson.toJson(result));

    }

    /**
     * Mark credit note as no action required
     */
    private void creditNoteNoAction(final HttpServletRequest request, final HttpServletResponse response)
            throws Exception {

        final int reqId;
        final String reason;
        try {
            reqId = ServletUtil.requireIntParam(request, "reqId");
            reason = ServletUtil.requireParam(request, "reason");
        } catch (final IllegalArgumentException e) {
            sendJsonError(response, e.getMessage());
            return;
        }

        final int userId = getSessionUserId(request);

        final String commentText = "Credit Note - No Action Required. Reason: " + reason;

        try (final java.sql.Connection con = DBConnectionPool.getConnection()) {
            con.setAutoCommit(false);
            try {
                // Add comment for no action
                final ReplacementRequestDAO dao = new ReplacementRequestDAO();
                dao.addComment(con, reqId, userId, commentText);

                // Move to next stage using transitionFlow
                transitionWorkflowDao.transitionFlow(con, reqId, commentText);

                con.commit();
            } catch (final Exception e) {
                con.rollback();
                throw e;
            }
        }

        final Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("message", "Request marked as No Action Required");

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().print(gson.toJson(result));
    }

    /**
     * Forward credit note to higher authority
     */
    private void forwardCreditNote(final HttpServletRequest request, final HttpServletResponse response)
            throws Exception {

        final int reqId;
        final String manager;
        try {
            reqId = ServletUtil.requireIntParam(request, "reqId");
            manager = ServletUtil.requireParam(request, "manager");
        } catch (final IllegalArgumentException e) {
            sendJsonError(response, e.getMessage());
            return;
        }

        final HttpSession session = request.getSession();

        final int userId = getSessionUserId(request);
        final String comments = ServletUtil.optionalParam(request, "comments");

        // Add comment for forwarding
        final ReplacementRequestDAO dao = new ReplacementRequestDAO();
        final String commentText = "Credit Note forwarded to " + manager + ". Comment: " + (comments != null ? comments : "");
        dao.addComment(reqId, userId, commentText);

        // Note: In a real implementation, you would also update the owner/assignee

        final Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("message", "Request forwarded successfully");

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().print(gson.toJson(result));
    }

    private void getPendingCreditNotes(final HttpServletRequest request, final HttpServletResponse response)
            throws Exception {

        final CreditNoteDAO dao = new CreditNoteDAO();
        final List<Map<String, Object>> creditNotes = dao.getPendingCreditNotes();

        final Map<String, Object> payload = new HashMap<>();
        payload.put("success", true);
        payload.put("creditNotes", creditNotes);

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().print(gson.toJson(payload));
    }

    private void getCreditNotesForApproval(final HttpServletRequest request, final HttpServletResponse response)
            throws Exception {

        final int reqId;
        try {
            reqId = ServletUtil.requireIntParam(request, "id");
        } catch (final IllegalArgumentException e) {
            sendJsonError(response, e.getMessage());
            return;
        }

        final CreditNoteDAO creditNoteDAO = new CreditNoteDAO();
        final List<Map<String, Object>> creditNotes = creditNoteDAO.getCreditNotesByRequestId(reqId);

        final PrinterBookingDAO printerBookingDAO = new PrinterBookingDAO();
        final Map<String, Object> requestData = printerBookingDAO.getPrinterDetails(reqId);

        final Map<String, Object> payload = new HashMap<>();
        payload.put("success", true);
        payload.put("request", requestData.get("request"));
        payload.put("creditNotes", creditNotes);

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().print(gson.toJson(payload));
    }

    private void approveCreditNote(final HttpServletRequest request, final HttpServletResponse response)
            throws Exception {

        final int reqId;
        final String creditNoteNumber;
        try {
            reqId = ServletUtil.requireIntParam(request, "reqId");
            creditNoteNumber = ServletUtil.requireParam(request, "creditNoteNumber");
        } catch (final IllegalArgumentException e) {
            sendJsonError(response, e.getMessage());
            return;
        }

        final HttpSession session = request.getSession();
        final User currentUser = (User) session.getAttribute("currentUser");
        final String comments = ServletUtil.optionalParam(request, "comments");
        final String location = ServletUtil.optionalParam(request, "location");

        final String commentText = "Credit Note approved with number: " + creditNoteNumber +
                (comments != null && !comments.isEmpty() ? ". Comments: " + comments : "");

        try (final java.sql.Connection con = DBConnectionPool.getConnection()) {
            con.setAutoCommit(false);
            try {
                final CreditNoteDAO creditNoteDAO = new CreditNoteDAO();
                creditNoteDAO.approveCreditNotesByRequestId(con, reqId, location, creditNoteNumber, comments);

                if (!creditNoteDAO.hasPendingCreditNotes(con, reqId)) {
                    transitionWorkflowDao.transitionFlow(con, reqId, commentText);
                }

                con.commit();
            } catch (final Exception e) {
                con.rollback();
                throw e;
            }
        }

        final Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("message", "Credit note approved successfully");

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().print(gson.toJson(result));
    }

    /**
     * Get agreement details for printers in a replacement request
     */
    private void getAgreementDetails(final HttpServletRequest request, final HttpServletResponse response)
            throws Exception {

        final int reqId;
        try {
            reqId = ServletUtil.requireIntParam(request, "id");
        } catch (final IllegalArgumentException e) {
            sendJsonError(response, e.getMessage());
            return;
        }

        final Map<String, Object> result = new HashMap<>();

        try (final java.sql.Connection con = DBConnectionPool.getConnection()) {
            final AgrDao agrDao = new AgrDao();
            final List<AgrProd> agrProducts = agrDao.fetchAgrProductsByReplacementRequest(con, reqId);

            // Convert to JSON-friendly format with printer details
            final List<Map<String, Object>> agreements = new ArrayList<>();
            for (final AgrProd ap : agrProducts) {
                final Map<String, Object> agr = new HashMap<>();
                agr.put("id", ap.getId());
                agr.put("agrNo", ap.getAgrNo());
                agr.put("serial", ap.getSerial());
                agr.put("rent", ap.getRent());
                agr.put("freePrints", ap.getFreePrints());
                agr.put("a4Rate", ap.getA4Rate());
                agr.put("a3Rate", ap.getA3Rate());
                agr.put("a4RatePost", ap.getA4RatePost());
                agr.put("a3RatePost", ap.getA3RatePost());
                agr.put("a4RateColor", ap.getA4RateColor());
                agr.put("a3RateColor", ap.getA3RateColor());
                agr.put("a4RatePostColor", ap.getA4RatePostColor());
                agr.put("a3RatePostColor", ap.getA3RatePostColor());
                agr.put("freeScan", ap.getFreeScan());
                agr.put("scanRate", ap.getScanRate());
                agr.put("amc", ap.getAmc());
                agr.put("amcType", ap.getAmcType());
                agr.put("pageCommited", ap.getPageCommited());
                agr.put("billingCommited", ap.getBillingCommited());
                agr.put("commitmentPeriod", ap.getCommitmentPeriod());
                agr.put("printerColor", ap.getPrinterColor());
                agr.put("pModel", ap.getpModel());
                agreements.add(agr);
            }

            result.put("success", true);
            result.put("data", agreements);
        } catch (final Exception e) {
            e.printStackTrace();
            result.put("success", false);
            result.put("message", "Error fetching agreement details: " + e.getMessage());
        }

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().print(gson.toJson(result));
    }

    /**
     * Check if logged-in user can create replacement requests.
     * Only designations 2 (CRO), 4 (AM), 261 (AM Manager) are allowed.
     *
     * @return true if access granted, false if denied (response already sent)
     */
    private boolean checkCreateRequestAccess(final HttpServletRequest request, final HttpServletResponse response)
            throws Exception {
        final int userId = getSessionUserId(request);
        if (userId == 0) {
            response.sendRedirect(request.getContextPath() + "/views/replacement/dashboard?error=unauthorized");
            return false;
        }

        final UserDAO userDAO = new UserDAO();

        if (!userDAO.canCreateRequest(userId)) {
            request.setAttribute("error", "Access Denied");
            request.setAttribute("errorDetails", "You do not have permission to create replacement requests.");
            forwardToJsp(request, response, "accessDenied.jsp");
            return false;
        }

        return true;
    }

    private boolean isAjaxRequestInternal(final HttpServletRequest request, final String action) {
        if ("XMLHttpRequest".equals(request.getHeader("X-Requested-With"))) {
            return true;
        }
        // Actions that return JSON are AJAX requests
        return action != null && (
                action.equals("getClientBranches") ||
                        action.equals("getBranchDetails") ||
                        action.equals("getClientLocations") ||
                        action.equals("getPrintersByLocations") ||
                        action.equals("getAllPrinterModels") ||
                        action.equals("getDetails") ||
                        action.equals("getBookingDetails") ||
                        action.equals("getCreditNoteDetails") ||
                        action.equals("getPendingCreditNotes") ||
                        action.equals("getCreditNotesForApproval") ||
                        action.equals("getAgreementDetails") ||
                        action.equals("getCommentHistory")
        );
    }
}
