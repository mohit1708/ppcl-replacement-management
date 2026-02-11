package com.ppcl.replacement.servlet;

import com.ppcl.replacement.dao.ReplacementLetterDAO;
import com.ppcl.replacement.model.ReplacementLetterData;
import com.ppcl.replacement.util.DBConnectionPool;
import com.ppcl.replacement.util.DigitalSignUtil;
import com.ppcl.replacement.util.ReplacementLetterPdfGenerator;
import org.apache.pdfbox.io.MemoryUsageSetting;
import org.apache.pdfbox.multipdf.PDFMergerUtility;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.ByteArrayInputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@WebServlet("/am/replacementLetter")
public class AMReplacementLetter extends BaseServlet {

    private static final String SIGNED_LETTERS_DIR = "uploads/signed-letters";
    private static final String DEFAULT_MERGE_BASE_DIR = "/home/naruto/ppcl/agr";
    private static final String DEFAULT_SIGNED_OUTPUT_DIR = "/home/naruto/ppcl/signed_replacement_letter";

    private final ReplacementLetterDAO letterDAO = new ReplacementLetterDAO();

    @Override
    protected void doGet(final HttpServletRequest req, final HttpServletResponse resp) throws ServletException, IOException {
        final int requestId = getIntParameter(req, "requestId");

        final ReplacementLetterData letterData = letterDAO.getLetterData(requestId != 0 ? requestId : 1);
        req.setAttribute("letterData", letterData);
        req.setAttribute("mergedLinks", buildMergedLinks(letterData));

        forwardToJsp(req, resp, "am/replacementLetter.jsp");
    }

    @Override
    protected void doPost(final HttpServletRequest req, final HttpServletResponse resp) throws ServletException, IOException {
        final String action = req.getParameter("action");

        try {
            if ("applySignature".equals(action)) {
                applyDigitalSignature(req, resp);

            } else if ("sendEmail".equals(action)) {
                sendJsonSuccess(resp, "Email sent", null);

            } else if ("sendWhatsApp".equals(action)) {
                sendJsonSuccess(resp, "WhatsApp sent", null);

            } else if ("sendDispatch".equals(action)) {
                sendJsonSuccess(resp, "Dispatch notification sent", null);

            } else {
                resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Unknown action");
            }
        } catch (Exception e) {
            handleError(req, resp, e);
        }
    }

    private void applyDigitalSignature(final HttpServletRequest req, final HttpServletResponse resp) throws Exception {
        final int requestId = getIntParameter(req, "requestId");
        if (requestId == 0) {
            sendJsonError(resp, "Invalid request ID");
            return;
        }

        final String p12Path = getServletContext().getInitParameter("dsc.p12.path");
        final String p12Password = getServletContext().getInitParameter("dsc.p12.password");
        if (p12Path == null || p12Password == null) {
            sendJsonError(resp, "DSC certificate not configured");
            return;
        }

        final ReplacementLetterData letterData = letterDAO.getLetterData(requestId);
        if (letterData.getClient() == null) {
            sendJsonError(resp, "Replacement request not found");
            return;
        }
        if (letterData.isReplacementLetterGenerated()) {
            sendJsonError(resp, "Replacement letter already generated");
            return;
        }

        final String signerName = req.getSession().getAttribute("userName") != null
                ? req.getSession().getAttribute("userName").toString()
                : "System";
        final String signedAt = new SimpleDateFormat("dd-MMM-yyyy HH:mm").format(new Date());

        letterData.setSigned(true);
        letterData.setSignedBy(signerName);
        letterData.setSignedAt(signedAt);

        final byte[] unsignedPdf = ReplacementLetterPdfGenerator.generate(letterData);

        final byte[] signedPdf = DigitalSignUtil.signPdf(
                unsignedPdf,
                p12Path,
                p12Password.toCharArray(),
                "Replacement Letter Approved",
                "PPCL Office",
                true
        );

        final String appPath = getServletContext().getRealPath("");
        final String uploadPath = appPath + File.separator + SIGNED_LETTERS_DIR;
        final File uploadDir = new File(uploadPath);
        if (!uploadDir.exists()) {
            uploadDir.mkdirs();
        }

        final String mergeBaseDirParam = getServletContext().getInitParameter("dsc.merge.base.dir");
        final String mergeBaseDir = mergeBaseDirParam != null ? mergeBaseDirParam : DEFAULT_MERGE_BASE_DIR;
        final String signedOutputDirParam = getServletContext().getInitParameter("dsc.signed.output.dir");
        final String signedOutputDir = signedOutputDirParam != null ? signedOutputDirParam : DEFAULT_SIGNED_OUTPUT_DIR;

        final List<Map<String, String>> mergedLinks = new ArrayList<>();
        if (letterData.getPrinters() != null) {
            for (int i = 0; i < letterData.getPrinters().size(); i++) {
                String agreementNo = letterData.getPrinters().get(i).getAgreementNoMapped();
                String safeAgreementNo = sanitizeFilePart(agreementNo);
                if (safeAgreementNo.isEmpty()) {
                    continue;
                }
                Path existingPdf = Paths.get(mergeBaseDir, safeAgreementNo + ".pdf");
                if (!Files.exists(existingPdf)) {
                    continue;
                }
                String mergedName = "merged_" + safeAgreementNo + "_" + requestId + ".pdf";
                Path mergedPath = Paths.get(signedOutputDir, mergedName);
                if (!Files.exists(mergedPath)) {
                    mergePdfAtBottom(existingPdf.toString(), signedPdf, mergedPath);
                }
                Map<String, String> link = new HashMap<>();
                link.put("label", String.valueOf(i + 1));
                link.put("filePath", "signed-letter?file=" + URLEncoder.encode(mergedName, StandardCharsets.UTF_8.name()));
                mergedLinks.add(link);
            }
        }

        final String relativePath;
        if (!mergedLinks.isEmpty()) {
            relativePath = mergedLinks.get(0).get("filePath");
        } else {
            String fileName = "signed_" + requestId + ".pdf";
            Path fallbackPath = Paths.get(signedOutputDir, fileName);
            if (!Files.exists(fallbackPath)) {
                Files.write(fallbackPath, signedPdf);
            }
            relativePath = "signed-letter?file=" + URLEncoder.encode(fileName, StandardCharsets.UTF_8.name());
        }
        updateSignedLetterStatus(requestId, relativePath);

        final Map<String, Object> data = new HashMap<>();
        data.put("signedBy", signerName);
        data.put("signedAt", signedAt);
        data.put("filePath", relativePath);
        data.put("mergedLinks", mergedLinks);
        sendJsonSuccess(resp, "Digital signature applied successfully", data);
    }

    private void mergePdfAtBottom(final String existingPdfPath, final byte[] signedPdf, final Path outputPath) throws IOException {
        PDFMergerUtility merger = new PDFMergerUtility();
        merger.addSource(new File(existingPdfPath));
        try (InputStream signedStream = new ByteArrayInputStream(signedPdf)) {
            merger.addSource(signedStream);
            merger.setDestinationFileName(outputPath.toString());
            merger.mergeDocuments(MemoryUsageSetting.setupMainMemoryOnly());
        } catch (FileNotFoundException e) {
            Files.write(outputPath, signedPdf);
        }
    }

    private List<Map<String, String>> buildMergedLinks(final ReplacementLetterData letterData) {
        final List<Map<String, String>> mergedLinks = new ArrayList<>();
        if (letterData == null || letterData.getPrinters() == null) {
            return mergedLinks;
        }

        final String mergeBaseDirParam = getServletContext().getInitParameter("dsc.merge.base.dir");
        final String mergeBaseDir = mergeBaseDirParam != null ? mergeBaseDirParam : DEFAULT_MERGE_BASE_DIR;
        final String signedOutputDirParam = getServletContext().getInitParameter("dsc.signed.output.dir");
        final String signedOutputDir = signedOutputDirParam != null ? signedOutputDirParam : DEFAULT_SIGNED_OUTPUT_DIR;

        for (int i = 0; i < letterData.getPrinters().size(); i++) {
            String agreementNo = letterData.getPrinters().get(i).getAgreementNoMapped();
            String safeAgreementNo = sanitizeFilePart(agreementNo);
            if (safeAgreementNo.isEmpty()) {
                continue;
            }
            Path existingPdf = Paths.get(mergeBaseDir, safeAgreementNo + ".pdf");
            if (!Files.exists(existingPdf)) {
                continue;
            }
            String mergedName = "merged_" + safeAgreementNo + "_" + letterData.getRequestId() + ".pdf";
            Path mergedPath = Paths.get(signedOutputDir, mergedName);
            if (!Files.exists(mergedPath)) {
                continue;
            }
            Map<String, String> link = new HashMap<>();
            link.put("label", String.valueOf(i + 1));
            link.put("filePath", "signed-letter?file=" + encodeFileName(mergedName));
            mergedLinks.add(link);
        }
        return mergedLinks;
    }

    private void updateSignedLetterStatus(final int requestId, final String filePath) throws Exception {
        final String sql = """
                UPDATE REPLACEMENT_REQUEST
                SET SIGNED_LETTER_PATH = ?,
                    SIGNED_UPLOAD_DATE = SYSTIMESTAMP,
                    REPLACEMENT_LETTER_GENERATED = 1,
                    IS_EDITABLE = 0,
                    STATUS = 'COMPLETED',
                    UPDATE_DATE_TIME = SYSTIMESTAMP
                WHERE ID = ?
                """;

        try (final Connection con = DBConnectionPool.getConnection();
             final PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, filePath);
            ps.setInt(2, requestId);
            ps.executeUpdate();
        }
    }

    private String encodeFileName(final String fileName) {
        try {
            return URLEncoder.encode(fileName, StandardCharsets.UTF_8.name());
        } catch (IOException e) {
            return fileName;
        }
    }

    private String sanitizeFilePart(final String input) {
        if (input == null) {
            return "";
        }
        return input.replaceAll("[^A-Za-z0-9_-]", "");
    }
}
