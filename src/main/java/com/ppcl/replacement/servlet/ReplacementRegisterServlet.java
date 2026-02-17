package com.ppcl.replacement.servlet;

import com.ppcl.replacement.constants.AppConstants;
import com.ppcl.replacement.dao.CreditNoteDAO;
import com.ppcl.replacement.dao.RegisterDAO;
import com.ppcl.replacement.model.RegisterRequestRow;
import com.ppcl.replacement.util.FileUploadUtil;
import com.ppcl.replacement.util.JsonResponse;
import com.ppcl.replacement.util.ServletUtil;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Servlet for Replacement Register - Central register with signed letter management
 */
@WebServlet("/views/replacement/register")
@MultipartConfig(
        fileSizeThreshold = 1024 * 1024,      // 1 MB
        maxFileSize = 25 * 1024 * 1024,       // 25 MB
        maxRequestSize = 30 * 1024 * 1024     // 30 MB
)
public class ReplacementRegisterServlet extends HttpServlet {

    private static final String UPLOAD_DIR = "uploads/signed-letters";
    private static final String TEMP_UPLOAD_DIR = "uploads/temp-signed-letters";
    private final RegisterDAO dao = new RegisterDAO();

    // Temporary storage for uploaded documents before freezing (requestId -> TempDocInfo)
    private static final Map<Integer, TempDocInfo> tempDocuments = new ConcurrentHashMap<>();

    private static class TempDocInfo {
        String fileName;
        String filePath;
        long fileSize;
        Date uploadDate;

        TempDocInfo(final String fileName, final String filePath, final long fileSize) {
            this.fileName = fileName;
            this.filePath = filePath;
            this.fileSize = fileSize;
            this.uploadDate = new Date();
        }
    }

    @Override
    protected void doGet(final HttpServletRequest request, final HttpServletResponse response)
            throws ServletException, IOException {

        final String action = ServletUtil.optionalParam(request, "action");

        try {
            if ("getDetails".equals(action)) {
                getRequestDetails(request, response);

            } else if ("download".equals(action)) {
                downloadSignedLetter(request, response);

            } else if ("getClients".equals(action)) {
                getClientsForFilter(response);

            } else if ("getTempDocInfo".equals(action)) {
                getTempDocInfo(request, response);

            } else if ("previewTemp".equals(action)) {
                serveTempDocument(request, response, false);

            } else if ("downloadTemp".equals(action)) {
                serveTempDocument(request, response, true);

            } else {
                showRegisterPage(request, response);
            }

        } catch (final IllegalArgumentException e) {
            JsonResponse.sendError(response, e.getMessage());
        } catch (final Exception e) {
            e.printStackTrace();
            handleError(request, response, action, e);
        }
    }

    @Override
    protected void doPost(final HttpServletRequest request, final HttpServletResponse response)
            throws ServletException, IOException {

        final String action = ServletUtil.optionalParam(request, "action");

        try {
            if ("uploadSigned".equals(action)) {
                uploadSignedLetter(request, response);

            } else if ("uploadTemp".equals(action)) {
                uploadTempDocument(request, response);

            } else if ("replaceTemp".equals(action)) {
                replaceTempDocument(request, response);

            } else if ("freeze".equals(action)) {
                freezeRecord(request, response);

            } else {
                JsonResponse.sendError(response, "Invalid action");
            }

        } catch (final IllegalArgumentException e) {
            JsonResponse.sendError(response, e.getMessage());
        } catch (final Exception e) {
            e.printStackTrace();
            JsonResponse.sendError(response, e.getMessage());
        }
    }

    private void showRegisterPage(final HttpServletRequest request, final HttpServletResponse response)
            throws Exception {

        final String clientFilter = ServletUtil.optionalParam(request, "client");
        final String statusFilter = ServletUtil.optionalParam(request, "status");
        final String fromDate = ServletUtil.optionalParam(request, "fromDate");
        final String toDate = ServletUtil.optionalParam(request, "toDate");

        final List<RegisterRequestRow> requests = dao.getRegisterRequests(clientFilter, statusFilter, fromDate, toDate);
        request.setAttribute("requests", requests);

        final List<String[]> clients = dao.getDistinctClients();
        request.setAttribute("clients", clients);

        request.setAttribute("filterClient", clientFilter);
        request.setAttribute("filterStatus", statusFilter);
        request.setAttribute("filterFromDate", fromDate);
        request.setAttribute("filterToDate", toDate);

        request.getRequestDispatcher(BaseServlet.JSP_BASE + "register.jsp").forward(request, response);
    }

    private void getRequestDetails(final HttpServletRequest request, final HttpServletResponse response)
            throws Exception {

        final int requestId = ServletUtil.requireIntParam(request, "id");
        final RegisterRequestRow row = dao.getRequestWithPrinters(requestId);

        if (row == null) {
            JsonResponse.sendError(response, "Request not found");
            return;
        }

        JsonResponse.sendSuccess(response, "Request details loaded", row);
    }

    private void uploadSignedLetter(final HttpServletRequest request, final HttpServletResponse response)
            throws Exception {

        final int requestId = ServletUtil.requireIntParam(request, "id");

        final Part filePart = request.getPart("signedFile");
        if (filePart == null || filePart.getSize() == 0) {
            JsonResponse.sendError(response, "No file uploaded");
            return;
        }

        final String fileName = getFileName(filePart);
        if (!fileName.toLowerCase().endsWith(".pdf")) {
            JsonResponse.sendError(response, "Only PDF files are allowed");
            return;
        }

        final CreditNoteDAO creditNoteDAO = new CreditNoteDAO();
        try (final java.sql.Connection con = com.ppcl.replacement.util.DBConnectionPool.getConnection()) {
            if (creditNoteDAO.hasPendingCreditNotes(con, requestId)) {
                JsonResponse.sendError(response, "Cannot upload: Credit notes are still pending for some locations. Please complete all credit note approvals first.");
                return;
            }
        }

        final String relativePath = FileUploadUtil.uploadFile(AppConstants.UPLOAD_BASE_REPLACEMENT_REGISTER, filePart, "signed_" + requestId);

        // Atomically check lock status and upload
        final boolean success = dao.checkAndUploadSignedLetter(requestId, relativePath);
        if (!success) {
            JsonResponse.sendError(response, "This request is already locked with a signed letter");
            return;
        }

        final Map<String, Object> data = new HashMap<>();
        data.put("letterRef", "R-" + requestId);
        JsonResponse.sendSuccess(response, "Signed letter uploaded successfully. Record is now locked.", data);
    }

    private void downloadSignedLetter(final HttpServletRequest request, final HttpServletResponse response)
            throws Exception {

        final int requestId = ServletUtil.requireIntParam(request, "id");
        final RegisterRequestRow row = dao.getRequestWithPrinters(requestId);

        if (row == null || row.getSignedLetterPath() == null) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND, "Signed letter not found");
            return;
        }

        final String appPath = request.getServletContext().getRealPath("");
        final Path filePath = Paths.get(appPath, row.getSignedLetterPath());

        if (!Files.exists(filePath)) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND, "File not found");
            return;
        }

        response.setContentType("application/pdf");
        response.setHeader("Content-Disposition", "attachment; filename=\"signed_letter_R-" + requestId + ".pdf\"");
        response.setContentLength((int) Files.size(filePath));

        Files.copy(filePath, response.getOutputStream());
        response.getOutputStream().flush();
    }

    private void getClientsForFilter(final HttpServletResponse response) throws Exception {
        final List<String[]> clients = dao.getDistinctClients();

        final List<Map<String, String>> clientList = new ArrayList<>();
        for (final String[] client : clients) {
            final Map<String, String> c = new HashMap<>();
            c.put("id", client[0]);
            c.put("name", client[1]);
            clientList.add(c);
        }

        JsonResponse.sendSuccess(response, "Clients loaded", clientList);
    }

    private void uploadTempDocument(final HttpServletRequest request, final HttpServletResponse response)
            throws Exception {

        final int requestId = ServletUtil.requireIntParam(request, "id");

        final Part filePart = request.getPart("signedFile");
        if (filePart == null || filePart.getSize() == 0) {
            JsonResponse.sendError(response, "No file uploaded");
            return;
        }

        final String originalFileName = getFileName(filePart);
        if (!originalFileName.toLowerCase().endsWith(".pdf")) {
            JsonResponse.sendError(response, "Only PDF files are allowed");
            return;
        }

        final String appPath = request.getServletContext().getRealPath("");
        final String uploadPath = appPath + File.separator + TEMP_UPLOAD_DIR;
        final File uploadDir = new File(uploadPath);
        if (!uploadDir.exists()) {
            uploadDir.mkdirs();
        }

        // Delete previous temp file if exists
        final TempDocInfo prevInfo = tempDocuments.get(requestId);
        if (prevInfo != null) {
            new File(prevInfo.filePath).delete();
        }

        final String newFileName = "temp_" + requestId + "_" + System.currentTimeMillis() + ".pdf";
        final String filePath = uploadPath + File.separator + newFileName;

        filePart.write(filePath);

        final TempDocInfo tempInfo = new TempDocInfo(originalFileName, filePath, filePart.getSize());
        tempDocuments.put(requestId, tempInfo);

        final Map<String, Object> data = new HashMap<>();
        data.put("fileName", originalFileName);
        data.put("fileSize", filePart.getSize());
        data.put("uploadDate", new SimpleDateFormat("dd-MMM-yyyy HH:mm").format(tempInfo.uploadDate));
        JsonResponse.sendSuccess(response, "Document uploaded successfully", data);
    }

    private void replaceTempDocument(final HttpServletRequest request, final HttpServletResponse response)
            throws Exception {

        final int requestId = ServletUtil.requireIntParam(request, "id");

        final Part filePart = request.getPart("signedFile");
        if (filePart == null || filePart.getSize() == 0) {
            JsonResponse.sendError(response, "No file uploaded");
            return;
        }

        final String originalFileName = getFileName(filePart);
        if (!originalFileName.toLowerCase().endsWith(".pdf")) {
            JsonResponse.sendError(response, "Only PDF files are allowed");
            return;
        }

        final String appPath = request.getServletContext().getRealPath("");
        final String uploadPath = appPath + File.separator + TEMP_UPLOAD_DIR;
        final File uploadDir = new File(uploadPath);
        if (!uploadDir.exists()) {
            uploadDir.mkdirs();
        }

        // Delete previous temp file
        final TempDocInfo prevInfo = tempDocuments.get(requestId);
        if (prevInfo != null) {
            new File(prevInfo.filePath).delete();
        }

        final String newFileName = "temp_" + requestId + "_" + System.currentTimeMillis() + ".pdf";
        final String filePath = uploadPath + File.separator + newFileName;

        filePart.write(filePath);

        final TempDocInfo tempInfo = new TempDocInfo(originalFileName, filePath, filePart.getSize());
        tempDocuments.put(requestId, tempInfo);

        final Map<String, Object> data = new HashMap<>();
        data.put("fileName", originalFileName);
        data.put("fileSize", filePart.getSize());
        JsonResponse.sendSuccess(response, "Document replaced successfully", data);
    }

    private void getTempDocInfo(final HttpServletRequest request, final HttpServletResponse response)
            throws Exception {

        final int requestId = ServletUtil.requireIntParam(request, "id");
        final TempDocInfo tempInfo = tempDocuments.get(requestId);

        if (tempInfo == null) {
            JsonResponse.sendError(response, "No temporary document found");
            return;
        }

        final Map<String, Object> data = new HashMap<>();
        data.put("fileName", tempInfo.fileName);
        data.put("fileSize", tempInfo.fileSize);
        data.put("uploadDate", new SimpleDateFormat("dd-MMM-yyyy HH:mm").format(tempInfo.uploadDate));
        JsonResponse.sendSuccess(response, "Document info loaded", data);
    }

    private void serveTempDocument(final HttpServletRequest request, final HttpServletResponse response, final boolean asDownload)
            throws Exception {

        final int requestId = ServletUtil.requireIntParam(request, "id");
        final TempDocInfo tempInfo = tempDocuments.get(requestId);

        if (tempInfo == null) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND, "Temporary document not found");
            return;
        }

        final Path filePath = Paths.get(tempInfo.filePath);
        if (!Files.exists(filePath)) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND, "File not found");
            return;
        }

        response.setContentType("application/pdf");
        if (asDownload) {
            response.setHeader("Content-Disposition", "attachment; filename=\"" + tempInfo.fileName + "\"");
        } else {
            response.setHeader("Content-Disposition", "inline; filename=\"" + tempInfo.fileName + "\"");
        }
        response.setContentLength((int) Files.size(filePath));

        Files.copy(filePath, response.getOutputStream());
        response.getOutputStream().flush();
    }

    private void freezeRecord(final HttpServletRequest request, final HttpServletResponse response)
            throws Exception {

        final int requestId = ServletUtil.requireIntParam(request, "id");
        final TempDocInfo tempInfo = tempDocuments.get(requestId);

        if (tempInfo == null) {
            JsonResponse.sendError(response, "No document uploaded. Please upload a document first.");
            return;
        }

        final CreditNoteDAO creditNoteDAO = new CreditNoteDAO();
        try (final java.sql.Connection con = com.ppcl.replacement.util.DBConnectionPool.getConnection()) {
            if (creditNoteDAO.hasPendingCreditNotes(con, requestId)) {
                JsonResponse.sendError(response, "Cannot freeze: Credit notes are still pending for some locations. Please complete all credit note approvals first.");
                return;
            }
        }

        final String relativePath = FileUploadUtil.uploadFromPath(AppConstants.UPLOAD_BASE_REPLACEMENT_REGISTER, tempInfo.filePath, "signed_" + requestId);

        // Atomically check lock status and upload
        final boolean success = dao.checkAndUploadSignedLetter(requestId, relativePath);
        if (!success) {
            JsonResponse.sendError(response, "This request is already locked");
            return;
        }

        // Clean up temp file
        new File(tempInfo.filePath).delete();
        tempDocuments.remove(requestId);

        final Map<String, Object> data = new HashMap<>();
        data.put("letterRef", "R-" + requestId);
        JsonResponse.sendSuccess(response, "Record frozen successfully", data);
    }

    private String getFileName(final Part part) {
        final String contentDisposition = part.getHeader("content-disposition");
        for (final String token : contentDisposition.split(";")) {
            if (token.trim().startsWith("filename")) {
                return token.substring(token.indexOf('=') + 1).trim().replace("\"", "");
            }
        }
        return "unknown.pdf";
    }

    private void handleError(final HttpServletRequest request, final HttpServletResponse response,
                             final String action, final Exception e) throws ServletException, IOException {

        if ("getDetails".equals(action) || "getClients".equals(action) || "getTempDocInfo".equals(action)) {
            JsonResponse.sendError(response, e.getMessage());
        } else {
            request.setAttribute("error", "Error: " + e.getMessage());
            request.getRequestDispatcher(BaseServlet.JSP_BASE + "register.jsp").forward(request, response);
        }
    }
}
