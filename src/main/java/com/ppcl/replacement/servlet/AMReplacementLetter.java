package com.ppcl.replacement.servlet;

import com.ppcl.replacement.dao.ReplacementLetterDAO;
import com.ppcl.replacement.model.ReplacementLetterData;
import com.ppcl.replacement.model.ReplacementPrinter;
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
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoField;
import java.util.Locale;
import java.util.stream.Stream;

@WebServlet("/am/replacementLetter")
public class AMReplacementLetter extends BaseServlet {

    private static final DateTimeFormatter[] AGREEMENT_DATE_FORMATTERS = new DateTimeFormatter[] {
            DateTimeFormatter.ofPattern("yyyy-MM-dd"),
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"),
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.S"),
            new DateTimeFormatterBuilder()
                    .parseCaseInsensitive()
                    .appendPattern("dd-MMM-")
                    .appendValueReduced(ChronoField.YEAR, 2, 2, 2000)
                    .toFormatter(Locale.ENGLISH),
            new DateTimeFormatterBuilder()
                    .parseCaseInsensitive()
                    .appendPattern("dd-MMM-yyyy")
                    .toFormatter(Locale.ENGLISH),
            DateTimeFormatter.ofPattern("dd/MM/yyyy"),
            DateTimeFormatter.ofPattern("dd-MM-yyyy")
    };

    private final ReplacementLetterDAO letterDAO = new ReplacementLetterDAO();

    @Override
    protected void doGet(final HttpServletRequest req, final HttpServletResponse resp) throws ServletException, IOException {
        if (!ensureAuthenticated(req, resp)) {
            return;
        }
        final int requestId = getIntParameter(req, "requestId");

        final ReplacementLetterData letterData = letterDAO.getLetterData(requestId != 0 ? requestId : 1);
        req.setAttribute("letterData", letterData);
        req.setAttribute("mergedLinks", buildMergedLinks(letterData));

        forwardToJsp(req, resp, "am/replacementLetter.jsp");
    }

    @Override
    protected void doPost(final HttpServletRequest req, final HttpServletResponse resp) throws ServletException, IOException {
        if (!ensureAuthenticated(req, resp)) {
            return;
        }
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

        final String windowsCertAlias = getServletContext().getInitParameter("dsc.windows.cert.alias");
        final String windowsCertSubjectContains = getServletContext().getInitParameter("dsc.windows.cert.subject.contains");

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

        final byte[] signedPdf = DigitalSignUtil.signPdfWithWindowsCertificate(
                unsignedPdf,
                windowsCertAlias,
                windowsCertSubjectContains,
                "Replacement Letter Approved",
                "PPCL Office",
                true
        );

        final LocalDate currentDate = LocalDate.now();
        final Path mergeBaseDir = resolveConfiguredPath("dsc.merge.base.dir");
        final Path signedOutputBasePath = resolveConfiguredPath("dsc.signed.output.dir");
        final Path signedOutputDir = buildDatedPath(signedOutputBasePath, currentDate);
        Files.createDirectories(signedOutputDir);

        final List<Map<String, String>> mergedLinks = new ArrayList<>();
        if (letterData.getPrinters() != null) {
            for (int i = 0; i < letterData.getPrinters().size(); i++) {
                String agreementNo = letterData.getPrinters().get(i).getAgreementNoMapped();
                String safeAgreementNo = sanitizeFilePart(agreementNo);
                if (safeAgreementNo.isEmpty()) {
                    continue;
                }
                Path existingPdf = resolveSourcePdfPath(mergeBaseDir, letterData.getPrinters().get(i), safeAgreementNo);
                if (existingPdf == null || !Files.exists(existingPdf)) {
                    continue;
                }
                String mergedName = "merged_" + safeAgreementNo + "_" + requestId + ".pdf";
                Path mergedPath = signedOutputDir.resolve(mergedName);
                if (!Files.exists(mergedPath)) {
                    mergePdfAtBottom(existingPdf.toString(), signedPdf, mergedPath);
                }
                String relativeMergedPath = toRelativeFilePath(signedOutputBasePath, mergedPath);
                Map<String, String> link = new HashMap<>();
                link.put("label", String.valueOf(i + 1));
                link.put("filePath", "signed-letter?file=" + URLEncoder.encode(relativeMergedPath, StandardCharsets.UTF_8));
                mergedLinks.add(link);
            }
        }

        final String relativePath;
        if (!mergedLinks.isEmpty()) {
            relativePath = mergedLinks.get(0).get("filePath");
        } else {
            String fileName = "signed_" + requestId + ".pdf";
            Path fallbackPath = signedOutputDir.resolve(fileName);
            if (!Files.exists(fallbackPath)) {
                Files.write(fallbackPath, signedPdf);
            }
            String relativeFallbackPath = toRelativeFilePath(signedOutputBasePath, fallbackPath);
            relativePath = "signed-letter?file=" + URLEncoder.encode(relativeFallbackPath, StandardCharsets.UTF_8);
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

        final Path mergeBaseDir = resolveConfiguredPath("dsc.merge.base.dir");
        final Path signedOutputBasePath = resolveConfiguredPath("dsc.signed.output.dir");
        final LocalDate signedDateHint = parseSignedAtDate(letterData.getSignedAt());

        for (int i = 0; i < letterData.getPrinters().size(); i++) {
            String agreementNo = letterData.getPrinters().get(i).getAgreementNoMapped();
            String safeAgreementNo = sanitizeFilePart(agreementNo);
            if (safeAgreementNo.isEmpty()) {
                continue;
            }
            Path existingPdf = resolveSourcePdfPath(mergeBaseDir, letterData.getPrinters().get(i), safeAgreementNo);
            if (existingPdf == null || !Files.exists(existingPdf)) {
                continue;
            }
            String mergedName = "merged_" + safeAgreementNo + "_" + letterData.getRequestId() + ".pdf";
            Path mergedPath = resolveSignedFilePath(signedOutputBasePath, mergedName, signedDateHint);
            if (mergedPath == null || !Files.exists(mergedPath)) {
                continue;
            }
            String relativeMergedPath = toRelativeFilePath(signedOutputBasePath, mergedPath);
            Map<String, String> link = new HashMap<>();
            link.put("label", String.valueOf(i + 1));
            link.put("filePath", "signed-letter?file=" + encodeFileName(relativeMergedPath));
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

    private Path resolveSourcePdfPath(final Path mergeBaseDir,
                                      final ReplacementPrinter printer,
                                      final String safeAgreementNo) {
        if (safeAgreementNo == null || safeAgreementNo.isEmpty()) {
            return null;
        }

        final String fileName = safeAgreementNo + ".pdf";
        final LocalDate agreementDate = parseAgreementDate(printer != null ? printer.getAgreementDate() : null);
        if (agreementDate != null) {
            final Path datedPath = mergeBaseDir
                    .resolve(String.format("%04d", agreementDate.getYear()))
                    .resolve(String.format("%02d", agreementDate.getMonthValue()))
                    .resolve(String.format("%02d", agreementDate.getDayOfMonth()))
                    .resolve(fileName);
            if (Files.exists(datedPath)) {
                return datedPath;
            }
        }

        final Path legacyPath = mergeBaseDir.resolve(fileName);
        if (Files.exists(legacyPath)) {
            return legacyPath;
        }

        return null;
    }

    private LocalDate parseAgreementDate(final String rawAgreementDate) {
        if (rawAgreementDate == null || rawAgreementDate.trim().isEmpty()) {
            return null;
        }
        final String value = rawAgreementDate.trim();

        for (DateTimeFormatter formatter : AGREEMENT_DATE_FORMATTERS) {
            try {
                return LocalDate.parse(value, formatter);
            } catch (DateTimeParseException ignored) {
                // Try next format.
            }
        }

        if (value.length() >= 10) {
            final String firstTen = value.substring(0, 10);
            try {
                return LocalDate.parse(firstTen, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            } catch (DateTimeParseException ignored) {
                // Ignore.
            }
        }

        return null;
    }

    private LocalDate parseSignedAtDate(final String rawSignedAtDate) {
        if (rawSignedAtDate == null || rawSignedAtDate.trim().isEmpty()) {
            return null;
        }
        final String value = rawSignedAtDate.trim();
        final DateTimeFormatter formatter = new DateTimeFormatterBuilder()
                .parseCaseInsensitive()
                .appendPattern("dd-MMM-yyyy HH:mm")
                .toFormatter(Locale.ENGLISH);
        try {
            return LocalDateTime.parse(value, formatter).toLocalDate();
        } catch (DateTimeParseException ignored) {
            return null;
        }
    }

    private Path buildDatedPath(final Path basePath, final LocalDate date) {
        return basePath.resolve(String.format("%04d", date.getYear()))
                .resolve(String.format("%02d", date.getMonthValue()))
                .resolve(String.format("%02d", date.getDayOfMonth()));
    }

    private Path resolveSignedFilePath(final Path signedOutputBasePath,
                                       final String fileName,
                                       final LocalDate signedDateHint) {
        if (signedDateHint != null) {
            final Path datedPath = buildDatedPath(signedOutputBasePath, signedDateHint).resolve(fileName);
            if (Files.exists(datedPath)) {
                return datedPath;
            }
        }

        final Path legacyPath = signedOutputBasePath.resolve(fileName);
        if (Files.exists(legacyPath)) {
            return legacyPath;
        }

        try (Stream<Path> stream = Files.find(signedOutputBasePath, 6,
                (path, attrs) -> attrs.isRegularFile() && fileName.equals(path.getFileName().toString()))) {
            return stream.findFirst().orElse(null);
        } catch (IOException ignored) {
            return null;
        }
    }

    private String toRelativeFilePath(final Path basePath, final Path filePath) {
        final Path relative = basePath.relativize(filePath.toAbsolutePath().normalize());
        return relative.toString().replace(File.separatorChar, '/');
    }

    private Path resolveConfiguredPath(final String initParamName) {
        final String configuredValue = getServletContext().getInitParameter(initParamName);
        final Path appRootPath = getAppRootPath();

        if (configuredValue == null || configuredValue.trim().isEmpty()) {
            throw new IllegalStateException("Missing required servlet init-param: " + initParamName);
        }

        Path path = Paths.get(configuredValue.trim());
        if (!path.isAbsolute()) {
            path = appRootPath.resolve(path);
        }
        return path.toAbsolutePath().normalize();
    }

    private Path getAppRootPath() {
        final String appRoot = getServletContext().getRealPath("");
        if (appRoot == null || appRoot.trim().isEmpty()) {
            return Paths.get(System.getProperty("user.dir")).toAbsolutePath().normalize();
        }
        return Paths.get(appRoot).toAbsolutePath().normalize();
    }

    private boolean ensureAuthenticated(final HttpServletRequest req, final HttpServletResponse resp) throws IOException {
        final int userId = getSessionUserId(req);
        if (userId != 0) {
            return true;
        }

        if ("GET".equalsIgnoreCase(req.getMethod()) && !isAjaxRequest(req)) {
            resp.sendRedirect(req.getContextPath() + "/login.jsp?error=session_expired");
            return false;
        }

        sendJsonError(resp, "Session expired. Please login again.");
        return false;
    }
}
