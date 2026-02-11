package com.ppcl.replacement.servlet;

import com.google.gson.Gson;
import com.ppcl.replacement.constants.AppConstants;
import com.ppcl.replacement.dao.CreditNoteDAO;
import com.ppcl.replacement.dao.TransitionWorkflowDao;
import com.ppcl.replacement.model.User;
import com.ppcl.replacement.util.DBConnectionPool;
import com.ppcl.replacement.util.FileUploadUtil;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.servlet.http.Part;
import java.io.IOException;
import java.sql.Connection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@WebServlet("/views/replacement/accounts/creditNoteApproval")
@MultipartConfig(
        fileSizeThreshold = 1024 * 1024,      // 1 MB
        maxFileSize = 1024 * 1024 * 10,       // 10 MB
        maxRequestSize = 1024 * 1024 * 15     // 15 MB
)
public class CreditNoteApprovalServlet extends BaseServlet {

    private final Gson gson = new Gson();
    private final TransitionWorkflowDao transitionWorkflowDao = new TransitionWorkflowDao();

    @Override
    protected void doGet(final HttpServletRequest request, final HttpServletResponse response)
            throws ServletException, IOException {
        try {
            final CreditNoteDAO dao = new CreditNoteDAO();
            final List<Map<String, Object>> creditNotes = dao.getPendingCreditNotes();
            final List<Map<String, Object>> approvedCreditNotes = dao.getApprovedCreditNotes();
            request.setAttribute("creditNotes", creditNotes);
            request.setAttribute("approvedCreditNotes", approvedCreditNotes);
        } catch (final Exception e) {
            e.printStackTrace();
            request.setAttribute("error", "Failed to load credit notes: " + e.getMessage());
        }

        forwardToJsp(request, response, "accounts/creditNoteApproval.jsp");
    }

    @Override
    protected void doPost(final HttpServletRequest request, final HttpServletResponse response)
            throws ServletException, IOException {

        final String action = request.getParameter("action");

        if ("approve".equals(action)) {
            approveCrediteNote(request, response);
        } else {
            response.sendRedirect(request.getContextPath() + "/views/replacement/accounts/creditNoteApproval");
        }
    }

    private void approveCrediteNote(final HttpServletRequest request, final HttpServletResponse response)
            throws ServletException, IOException {

        final int reqId = getIntParameter(request, "reqId");
        final String creditNoteNumber = request.getParameter("creditNoteNumber");
        final String comments = request.getParameter("comments");

        if (reqId == 0) {
            sendJsonError(response, "Request ID is required");
            return;
        }

        if (creditNoteNumber == null || creditNoteNumber.trim().isEmpty()) {
            sendJsonError(response, "Credit Note Number is required");
            return;
        }

        String documentPath = null;
        try {
            final Part filePart = request.getPart("creditNoteFile");
            if (filePart != null && filePart.getSize() > 0) {
                documentPath = FileUploadUtil.uploadFile(AppConstants.UPLOAD_BASE_CREDIT_NOTE, filePart, "creditnote");
            }
        } catch (final Exception e) {
            e.printStackTrace();
        }

        final HttpSession session = request.getSession();
        final User currentUser = (User) session.getAttribute("currentUser");

        final String commentText = "Credit Note approved with number: " + creditNoteNumber +
                (comments != null && !comments.isEmpty() ? ". Comments: " + comments : "");

        try (final Connection con = DBConnectionPool.getConnection()) {
            con.setAutoCommit(false);
            try {
                final CreditNoteDAO creditNoteDAO = new CreditNoteDAO();
                if (documentPath != null) {
                    creditNoteDAO.approveCreditNoteWithDocument(con, reqId, creditNoteNumber, documentPath, comments);
                } else {
                    creditNoteDAO.approveCreditNotesByRequestId(con, reqId, creditNoteNumber, comments);
                }

                transitionWorkflowDao.transitionFlow(con, reqId, commentText);

                con.commit();
                sendJsonSuccess(response, "Credit note approved successfully", null);
            } catch (final Exception e) {
                con.rollback();
                throw e;
            }
        } catch (final Exception e) {
            handleError(request, response, e);
        }
    }

}
