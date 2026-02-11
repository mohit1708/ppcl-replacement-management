package com.ppcl.replacement.servlet;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.ppcl.replacement.dao.CourierPincodeMappingDAO;
import com.ppcl.replacement.model.Courier;
import com.ppcl.replacement.model.CourierPincodeMapping;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Servlet for Courier-Pincode Mapping operations
 */
@WebServlet(urlPatterns = {
        "/views/replacement/courier-pincode/page",
        "/views/replacement/courier-pincode/couriers",
        "/views/replacement/courier-pincode/mappings",
        "/views/replacement/courier-pincode/add",
        "/views/replacement/courier-pincode/upload",
        "/views/replacement/courier-pincode/edit",
        "/views/replacement/courier-pincode/toggleStatus",
        "/views/replacement/courier-pincode/export",
        "/views/replacement/courier-pincode/template",
        "/views/replacement/courier-pincode/getMappingById"
})
@MultipartConfig(
        fileSizeThreshold = 1024 * 1024,      // 1 MB
        maxFileSize = 1024 * 1024 * 10,        // 10 MB
        maxRequestSize = 1024 * 1024 * 15      // 15 MB
)
public class CourierPincodeMappingServlet extends HttpServlet {

    private Gson gson = new Gson();
    private CourierPincodeMappingDAO dao = new CourierPincodeMappingDAO();
    private static final String INDIA_POST_API = "https://api.postalpincode.in/pincode/";

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String servletPath = request.getServletPath();

        try {
            if (servletPath.endsWith("/page")) {
                // Forward to JSP page
                request.getRequestDispatcher(BaseServlet.JSP_BASE + "courier/pincodeMappingPage.jsp")
                        .forward(request, response);
            } else if (servletPath.endsWith("/couriers")) {
                getCouriers(request, response);
            } else if (servletPath.endsWith("/mappings")) {
                getMappings(request, response);
            } else if (servletPath.endsWith("/export")) {
                exportData(request, response);
            } else if (servletPath.endsWith("/template")) {
                downloadTemplate(request, response);
            } else if (servletPath.endsWith("/getMappingById")) {
                getMappingById(request, response);
            } else {
                response.sendError(HttpServletResponse.SC_NOT_FOUND);
            }
        } catch (Exception e) {
            e.printStackTrace();
            sendJsonError(response, "Error: " + e.getMessage());
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String servletPath = request.getServletPath();

        try {
            if (servletPath.endsWith("/add")) {
                addMappings(request, response);
            } else if (servletPath.endsWith("/upload")) {
                uploadMappings(request, response);
            } else if (servletPath.endsWith("/edit")) {
                editMapping(request, response);
            } else if (servletPath.endsWith("/toggleStatus")) {
                toggleStatus(request, response);
            } else {
                response.sendError(HttpServletResponse.SC_NOT_FOUND);
            }
        } catch (Exception e) {
            e.printStackTrace();
            sendJsonError(response, "Error: " + e.getMessage());
        }
    }

    /**
     * Get all active couriers for dropdown
     */
    private void getCouriers(HttpServletRequest request, HttpServletResponse response)
            throws Exception {
        List<Courier> couriers = dao.getAllActiveCouriers();
        sendJsonSuccess(response, "Couriers loaded", couriers);
    }

    /**
     * Get mappings with filters and pagination
     */
    private void getMappings(HttpServletRequest request, HttpServletResponse response)
            throws Exception {
        String courierName = request.getParameter("courierName");
        String pincode = request.getParameter("pincode");
        int page = parseIntOrDefault(request.getParameter("page"), 1);
        int pageSize = parseIntOrDefault(request.getParameter("pageSize"), 10);

        List<CourierPincodeMapping> mappings = dao.getMappings(courierName, pincode, page, pageSize);
        int totalCount = dao.countMappings(courierName, pincode);
        int totalPages = (int) Math.ceil((double) totalCount / pageSize);

        Map<String, Object> data = new HashMap<>();
        data.put("mappings", mappings);
        data.put("totalCount", totalCount);
        data.put("totalPages", totalPages);
        data.put("currentPage", page);
        data.put("pageSize", pageSize);

        sendJsonSuccess(response, "Mappings loaded", data);
    }

    /**
     * Get mapping by ID for edit
     */
    private void getMappingById(HttpServletRequest request, HttpServletResponse response)
            throws Exception {
        String idStr = request.getParameter("id");

        if (idStr == null || idStr.trim().isEmpty()) {
            sendJsonError(response, "Mapping ID is required.");
            return;
        }

        int id;
        try {
            id = Integer.parseInt(idStr);
        } catch (NumberFormatException e) {
            sendJsonError(response, "Invalid mapping ID format.");
            return;
        }

        CourierPincodeMapping mapping = dao.getMappingById(id);

        if (mapping != null) {
            sendJsonSuccess(response, "Mapping loaded", mapping);
        } else {
            sendJsonError(response, "Mapping not found. It may have been deleted.");
        }
    }

    /**
     * Manual Add - comma separated pincodes
     */
    private void addMappings(HttpServletRequest request, HttpServletResponse response)
            throws Exception {
        String courierIdStr = request.getParameter("courierId");
        String pincodes = request.getParameter("pincodes");
        String currentUser = getCurrentUser(request);

        // Validate courier ID
        if (courierIdStr == null || courierIdStr.trim().isEmpty()) {
            sendJsonError(response, "Courier is required. Please select a courier from the dropdown.");
            return;
        }

        int courierId;
        try {
            courierId = Integer.parseInt(courierIdStr);
        } catch (NumberFormatException e) {
            sendJsonError(response, "Invalid courier ID format. Please select a valid courier.");
            return;
        }

        // Validate pincodes input
        if (pincodes == null || pincodes.trim().isEmpty()) {
            sendJsonError(response, "Pincodes are required. Please enter at least one pincode.");
            return;
        }

        // Get courier details
        Courier courier = dao.getCourierById(courierId);
        if (courier == null) {
            sendJsonError(response, "Selected courier not found. Please refresh and try again.");
            return;
        }

        // Parse comma-separated pincodes
        String[] pincodeArray = pincodes.split(",");
        Set<String> uniquePincodes = new LinkedHashSet<>();
        for (String p : pincodeArray) {
            String trimmed = p.trim();
            if (!trimmed.isEmpty()) {
                uniquePincodes.add(trimmed);
            }
        }

        if (uniquePincodes.isEmpty()) {
            sendJsonError(response, "No valid pincodes found. Please enter comma-separated 6-digit pincodes.");
            return;
        }

        List<CourierPincodeMapping> successList = new ArrayList<>();
        List<CourierPincodeMapping> failureList = new ArrayList<>();

        for (String pincode : uniquePincodes) {
            CourierPincodeMapping result = processAddPincode(pincode, courierId, courier.getName(), currentUser);
            if (result.isHasError()) {
                failureList.add(result);
            } else {
                successList.add(result);
            }
        }

        Map<String, Object> data = new HashMap<>();
        data.put("successCount", successList.size());
        data.put("failureCount", failureList.size());
        data.put("successList", successList);
        data.put("failureList", failureList);

        if (failureList.isEmpty()) {
            sendJsonSuccess(response, "All " + successList.size() + " pincodes added successfully", data);
        } else if (successList.isEmpty()) {
            sendJsonError(response, "All pincodes failed to add", data);
        } else {
            sendJsonPartialSuccess(response, successList.size() + " added, " +
                    failureList.size() + " failed", data);
        }
    }

    /**
     * Bulk Upload - Excel file
     */
    private void uploadMappings(HttpServletRequest request, HttpServletResponse response)
            throws Exception {
        Part filePart = request.getPart("file");
        String courierIdStr = request.getParameter("courierId");
        String currentUser = getCurrentUser(request);

        // Validate courier ID
        if (courierIdStr == null || courierIdStr.trim().isEmpty()) {
            sendJsonError(response, "Courier is required. Please select a courier from the dropdown.");
            return;
        }

        int courierId;
        try {
            courierId = Integer.parseInt(courierIdStr);
        } catch (NumberFormatException e) {
            sendJsonError(response, "Invalid courier ID format. Please select a valid courier.");
            return;
        }

        // Validate file
        if (filePart == null || filePart.getSize() == 0) {
            sendJsonError(response, "File is required. Please select an Excel file (.xlsx) to upload.");
            return;
        }

        String fileName = getSubmittedFileName(filePart);
        if (fileName == null || (!fileName.endsWith(".xlsx") && !fileName.endsWith(".xls"))) {
            sendJsonError(response, "Invalid file format. Please upload an Excel file (.xlsx or .xls).");
            return;
        }

        // Get courier details
        Courier courier = dao.getCourierById(courierId);
        if (courier == null) {
            sendJsonError(response, "Selected courier not found. Please refresh and try again.");
            return;
        }

        // Parse Excel file
        Set<String> uniquePincodes = new LinkedHashSet<>();
        try (InputStream is = filePart.getInputStream();
             Workbook workbook = new XSSFWorkbook(is)) {

            Sheet sheet = workbook.getSheetAt(0);
            boolean isFirstRow = true;

            for (Row row : sheet) {
                if (isFirstRow) {
                    isFirstRow = false;
                    // Skip header row
                    continue;
                }
                Cell cell = row.getCell(0);
                if (cell != null) {
                    String pincode = getCellValueAsString(cell).trim();
                    if (!pincode.isEmpty()) {
                        uniquePincodes.add(pincode);
                    }
                }
            }
        }

        if (uniquePincodes.isEmpty()) {
            sendJsonError(response, "No valid pincodes found in the uploaded file. Please ensure the file contains 6-digit pincodes in the first column.");
            return;
        }

        List<CourierPincodeMapping> successList = new ArrayList<>();
        List<CourierPincodeMapping> failureList = new ArrayList<>();

        for (String pincode : uniquePincodes) {
            CourierPincodeMapping result = processAddPincode(pincode, courierId, courier.getName(), currentUser);
            if (result.isHasError()) {
                failureList.add(result);
            } else {
                successList.add(result);
            }
        }

        Map<String, Object> data = new HashMap<>();
        data.put("successCount", successList.size());
        data.put("failureCount", failureList.size());
        data.put("successList", successList);
        data.put("failureList", failureList);

        if (failureList.isEmpty()) {
            sendJsonSuccess(response, "All " + successList.size() + " pincodes uploaded successfully", data);
        } else if (successList.isEmpty()) {
            sendJsonError(response, "All pincodes failed", data);
        } else {
            sendJsonPartialSuccess(response, successList.size() + " success, " +
                    failureList.size() + " failed", data);
        }
    }

    /**
     * Process a single pincode for ADD operation
     */
    private CourierPincodeMapping processAddPincode(String pincode, int courierId,
                                                    String courierName, String currentUser) {
        CourierPincodeMapping result = new CourierPincodeMapping();
        result.setCourierId(courierId);
        result.setCourierName(courierName);
        result.setCreatedBy(currentUser);

        try {
            // Step 1: Validate format
            if (!isValidPincode(pincode)) {
                result.setHasError(true);
                result.setErrorMessage("Invalid pincode format: '" + pincode + "'. Must be exactly 6 digits.");
                return result;
            }

            // Set pincode after validation (it's an int in the model)
            result.setPincode(pincode);

            // Step 2: Check if active mapping exists
            CourierPincodeMapping existing = dao.getActiveMappingByPincode(pincode);

            if (existing != null) {
                // Case A: Active mapping exists
                result.setHasError(true);
                result.setErrorMessage("Mapping already exists for pincode " + pincode +
                        " with courier " + existing.getCourierName());
                result.setCity(existing.getCity());
                result.setState(existing.getState());
                result.setRegion(existing.getRegion());

                // If city/state/region is null, try to fetch from India Post API
                if (existing.getCity() == null || existing.getCity().isEmpty()) {
                    Map<String, String> locationData = fetchFromIndiaPostAPI(pincode);
                    if (locationData != null) {
                        dao.updateLocationDetails(existing.getId(),
                                locationData.get("city"),
                                locationData.get("state"),
                                locationData.get("region"));
                    }
                }
                return result;
            }

            // Case B & C: No active mapping - proceed with insert
            // Fetch location data from India Post API
            Map<String, String> locationData = fetchFromIndiaPostAPI(pincode);

            if (locationData != null) {
                result.setCity(locationData.get("city"));
                result.setState(locationData.get("state"));
                result.setRegion(locationData.get("region"));
            }

            int newId = dao.insertMapping(result);
            if (newId > 0) {
                result.setId(newId);
                result.setStatus("ACTIVE");
                result.setHasError(false);
            } else {
                result.setHasError(true);
                result.setErrorMessage("Failed to insert mapping");
            }

        } catch (Exception e) {
            e.printStackTrace();
            result.setHasError(true);
            result.setErrorMessage("Error: " + e.getMessage());
        }

        return result;
    }

    /**
     * Edit a mapping
     */
    private void editMapping(HttpServletRequest request, HttpServletResponse response)
            throws Exception {
        String mappingIdStr = request.getParameter("mappingId");
        String courierIdStr = request.getParameter("courierId");
        String city = request.getParameter("city");
        String state = request.getParameter("state");
        String region = request.getParameter("region");
        String currentUser = getCurrentUser(request);

        // Validate mapping ID
        if (mappingIdStr == null || mappingIdStr.trim().isEmpty()) {
            sendJsonError(response, "Mapping ID is required.");
            return;
        }

        int mappingId;
        try {
            mappingId = Integer.parseInt(mappingIdStr);
        } catch (NumberFormatException e) {
            sendJsonError(response, "Invalid mapping ID format.");
            return;
        }

        // Validate courier ID
        if (courierIdStr == null || courierIdStr.trim().isEmpty()) {
            sendJsonError(response, "Courier is required. Please select a courier from the dropdown.");
            return;
        }

        int newCourierId;
        try {
            newCourierId = Integer.parseInt(courierIdStr);
        } catch (NumberFormatException e) {
            sendJsonError(response, "Invalid courier ID format. Please select a valid courier.");
            return;
        }

        // Get existing mapping
        CourierPincodeMapping existing = dao.getMappingById(mappingId);
        if (existing == null) {
            sendJsonError(response, "Mapping not found. It may have been deleted.");
            return;
        }

        // Get new courier details
        Courier newCourier = dao.getCourierById(newCourierId);
        if (newCourier == null) {
            sendJsonError(response, "Selected courier not found. Please refresh and try again.");
            return;
        }

        String pincodeStr = String.valueOf(existing.getPincode());

        // Validate pincode format
        if (!isValidPincode(pincodeStr)) {
            sendJsonError(response, "Invalid pincode format. Pincode must be exactly 6 digits.");
            return;
        }

        // If state is null, try to fetch from India Post API
        if (state == null || state.trim().isEmpty()) {
            Map<String, String> locationData = fetchFromIndiaPostAPI(pincodeStr);
            if (locationData != null) {
                city = locationData.get("city");
                state = locationData.get("state");
                region = locationData.get("region");
            }
        }

        // Deactivate existing mapping
        dao.deactivateMappingByPincode(pincodeStr, currentUser);

        // Create new mapping
        CourierPincodeMapping newMapping = new CourierPincodeMapping();
        newMapping.setCourierId(newCourierId);
        newMapping.setCourierName(newCourier.getName());
        newMapping.setPincode(existing.getPincode());
        newMapping.setCity(city);
        newMapping.setState(state);
        newMapping.setRegion(region);

        int newId = dao.insertMapping(newMapping);
        if (newId > 0) {
            newMapping.setId(newId);
            sendJsonSuccess(response, "Mapping updated successfully", newMapping);
        } else {
            sendJsonError(response, "Failed to update mapping");
        }
    }

    /**
     * Toggle status (soft delete)
     */
    private void toggleStatus(HttpServletRequest request, HttpServletResponse response)
            throws Exception {
        String mappingIdStr = request.getParameter("mappingId");
        String newStatus = request.getParameter("newStatus");
        String currentUser = getCurrentUser(request);

        // Validate mapping ID
        if (mappingIdStr == null || mappingIdStr.trim().isEmpty()) {
            sendJsonError(response, "Mapping ID is required.");
            return;
        }

        int mappingId;
        try {
            mappingId = Integer.parseInt(mappingIdStr);
        } catch (NumberFormatException e) {
            sendJsonError(response, "Invalid mapping ID format.");
            return;
        }

        // Validate status
        if (newStatus == null || newStatus.trim().isEmpty()) {
            sendJsonError(response, "Status is required.");
            return;
        }

        if (!"ACTIVE".equals(newStatus) && !"INACTIVE".equals(newStatus)) {
            sendJsonError(response, "Invalid status value. Must be 'ACTIVE' or 'INACTIVE'.");
            return;
        }

        boolean updated = dao.updateMappingStatus(mappingId, newStatus, currentUser);
        if (updated) {
            String msg = "ACTIVE".equals(newStatus) ? "Mapping enabled successfully" : "Mapping disabled successfully";
            sendJsonSuccess(response, msg, null);
        } else {
            sendJsonError(response, "Failed to update status. Mapping may not exist.");
        }
    }

    /**
     * Export data to CSV or PDF
     */
    private void exportData(HttpServletRequest request, HttpServletResponse response)
            throws Exception {
        String format = request.getParameter("format");
        String courierName = request.getParameter("courierName");
        String pincode = request.getParameter("pincode");

        List<CourierPincodeMapping> mappings = dao.getAllMappingsForExport(courierName, pincode);

        if ("csv".equalsIgnoreCase(format)) {
            exportToCsv(response, mappings);
        } else if ("pdf".equalsIgnoreCase(format)) {
            exportToPdf(response, mappings);
        } else {
            sendJsonError(response, "Invalid export format. Use 'csv' or 'pdf'.");
        }
    }

    /**
     * Export to CSV
     */
    private void exportToCsv(HttpServletResponse response, List<CourierPincodeMapping> mappings)
            throws IOException {
        String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        response.setContentType("text/csv");
        response.setHeader("Content-Disposition",
                "attachment; filename=courier_pincode_mappings_" + timestamp + ".csv");

        PrintWriter writer = response.getWriter();

        // Header
        writer.println("Courier Name,Pincode,City,State,Region,Status,Created At,Created By,Modified By");

        // Data
        SimpleDateFormat sdf = new SimpleDateFormat("dd-MMM-yyyy HH:mm");
        for (CourierPincodeMapping m : mappings) {
            writer.println(String.format("\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\"",
                    escapeCSV(m.getCourierName()),
                    String.valueOf(m.getPincode()),
                    escapeCSV(m.getCity()),
                    escapeCSV(m.getState()),
                    escapeCSV(m.getRegion()),
                    m.getStatus(),
                    m.getCreatedAt() != null ? sdf.format(m.getCreatedAt()) : "",
                    escapeCSV(m.getCreatedBy()),
                    escapeCSV(m.getModifiedBy())));
        }
        writer.flush();
    }

    /**
     * Export to PDF using Apache PDFBox
     */
    private void exportToPdf(HttpServletResponse response, List<CourierPincodeMapping> mappings)
            throws Exception {
        String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        response.setContentType("application/pdf");
        response.setHeader("Content-Disposition",
                "attachment; filename=courier_pincode_mappings_" + timestamp + ".pdf");

        try (PDDocument document = new PDDocument()) {
            // PDFBox 2.0.x uses static font constants
            PDType1Font fontBold = PDType1Font.HELVETICA_BOLD;
            PDType1Font fontRegular = PDType1Font.HELVETICA;

            // Column widths - adjusted for 9 columns
            float[] colWidths = {90, 50, 60, 60, 60, 50, 65, 60, 60};
            float tableWidth = 0;
            for (float w : colWidths) tableWidth += w;

            String[] headers = {"Courier Name", "Pincode", "City", "State", "Region", "Status", "Created At", "Created By", "Modified By"};
            SimpleDateFormat sdf = new SimpleDateFormat("dd-MMM-yyyy");

            int rowsPerPage = 30;
            int totalRows = mappings.size();
            int totalPages = (int) Math.ceil((double) totalRows / rowsPerPage);
            if (totalPages == 0) totalPages = 1;

            for (int pageNum = 0; pageNum < totalPages; pageNum++) {
                PDPage page = new PDPage(PDRectangle.A4);
                document.addPage(page);

                float pageWidth = page.getMediaBox().getWidth();
                float pageHeight = page.getMediaBox().getHeight();
                float margin = 40;
                float startX = margin;
                float startY = pageHeight - margin;
                float rowHeight = 18;

                try (PDPageContentStream cs = new PDPageContentStream(document, page)) {

                    // Title
                    cs.beginText();
                    cs.setFont(fontBold, 16);
                    cs.newLineAtOffset((pageWidth - 180) / 2, startY);
                    cs.showText("Courier Pincode Mappings");
                    cs.endText();

                    // Generated date
                    cs.beginText();
                    cs.setFont(fontRegular, 9);
                    cs.newLineAtOffset(margin, startY - 20);
                    cs.showText("Generated on: " + new SimpleDateFormat("dd-MMM-yyyy HH:mm:ss").format(new Date()));
                    cs.endText();

                    float tableStartY = startY - 45;

                    // Draw header row background
                    cs.setNonStrokingColor(51, 51, 51);
                    cs.addRect(startX, tableStartY - rowHeight, tableWidth, rowHeight);
                    cs.fill();

                    // Header text
                    cs.setNonStrokingColor(255, 255, 255);
                    float xPos = startX + 3;
                    for (int i = 0; i < headers.length; i++) {
                        cs.beginText();
                        cs.setFont(fontBold, 8);
                        cs.newLineAtOffset(xPos, tableStartY - 12);
                        cs.showText(headers[i]);
                        cs.endText();
                        xPos += colWidths[i];
                    }

                    // Data rows
                    cs.setNonStrokingColor(0, 0, 0);
                    int startRow = pageNum * rowsPerPage;
                    int endRow = Math.min(startRow + rowsPerPage, totalRows);

                    float currentY = tableStartY - rowHeight;
                    for (int i = startRow; i < endRow; i++) {
                        CourierPincodeMapping m = mappings.get(i);
                        currentY -= rowHeight;

                        // Draw row border
                        cs.setStrokingColor(200, 200, 200);
                        cs.addRect(startX, currentY, tableWidth, rowHeight);
                        cs.stroke();

                        // Row data
                        String[] rowData = {
                                truncateText(m.getCourierName(), 15),
                                String.valueOf(m.getPincode()),
                                truncateText(m.getCity(), 10),
                                truncateText(m.getState(), 10),
                                truncateText(m.getRegion(), 10),
                                m.getStatus(),
                                m.getCreatedAt() != null ? sdf.format(m.getCreatedAt()) : "",
                                truncateText(m.getCreatedBy(), 10),
                                truncateText(m.getModifiedBy(), 10)
                        };

                        xPos = startX + 3;
                        cs.setNonStrokingColor(0, 0, 0);
                        for (int j = 0; j < rowData.length; j++) {
                            cs.beginText();
                            cs.setFont(fontRegular, 7);
                            cs.newLineAtOffset(xPos, currentY + 5);
                            cs.showText(rowData[j]);
                            cs.endText();
                            xPos += colWidths[j];
                        }
                    }

                    // Page number
                    cs.beginText();
                    cs.setFont(fontRegular, 9);
                    cs.newLineAtOffset(pageWidth / 2 - 30, 25);
                    cs.showText("Page " + (pageNum + 1) + " of " + totalPages);
                    cs.endText();
                }
            }

            document.save(response.getOutputStream());
        }
    }

    /**
     * Truncate text for PDF table cells
     */
    private String truncateText(String text, int maxLength) {
        if (text == null) return "";
        if (text.length() <= maxLength) return text;
        return text.substring(0, maxLength - 2) + "..";
    }

    /**
     * Download sample Excel template
     */
    private void downloadTemplate(HttpServletRequest request, HttpServletResponse response)
            throws Exception {
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setHeader("Content-Disposition", "attachment; filename=pincode_upload_template.xlsx");

        try (Workbook workbook = new XSSFWorkbook();
             OutputStream out = response.getOutputStream()) {

            Sheet sheet = workbook.createSheet("Pincodes");

            // Header row
            Row headerRow = sheet.createRow(0);
            CellStyle headerStyle = workbook.createCellStyle();
            org.apache.poi.ss.usermodel.Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerStyle.setFont(headerFont);
            headerStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

            Cell headerCell = headerRow.createCell(0);
            headerCell.setCellValue("Pincode");
            headerCell.setCellStyle(headerStyle);

            // Auto-size column
            sheet.setColumnWidth(0, 4000);

            workbook.write(out);
        }
    }

    /**
     * Fetch location data from India Post API
     */
    private Map<String, String> fetchFromIndiaPostAPI(String pincode) {
        try {
            URL url = new URL(INDIA_POST_API + pincode);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(1000);
            conn.setReadTimeout(1000);

            conn.setRequestProperty("User-Agent", "Mozilla/5.0");
            conn.setRequestProperty("Accept", "application/json");

            if (conn.getResponseCode() == 200) {
                BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    sb.append(line);
                }
                reader.close();

                // Parse JSON response
                String jsonResponse = sb.toString();
                // Response format: [{"Message":"Success","Status":"Success","PostOffice":[{...}]}]

                JsonParser parser = new JsonParser();
                com.google.gson.JsonArray arr = parser.parse(jsonResponse).getAsJsonArray();
                JsonObject obj = arr.get(0).getAsJsonObject();

                if ("Success".equals(obj.get("Status").getAsString())) {
                    com.google.gson.JsonArray postOffices = obj.getAsJsonArray("PostOffice");
                    if (postOffices != null && postOffices.size() > 0) {
                        JsonObject po = postOffices.get(0).getAsJsonObject();

                        Map<String, String> result = new HashMap<>();
                        result.put("city", getJsonString(po, "District"));
                        result.put("state", getJsonString(po, "State"));
                        result.put("region", getJsonString(po, "Division"));
                        return result;
                    }
                }
            }
        } catch (Exception e) {
        }
        return null;
    }

    private String getJsonString(JsonObject obj, String key) {
        return obj.has(key) && !obj.get(key).isJsonNull() ? obj.get(key).getAsString() : null;
    }

    /**
     * Validate pincode format (6 digits)
     */
    private boolean isValidPincode(String pincode) {
        return pincode != null && pincode.matches("\\d{6}");
    }

    /**
     * Get current user from session
     */
    private String getCurrentUser(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session != null && session.getAttribute("userId") != null) {
            return String.valueOf(session.getAttribute("userId"));
        }
        return "SYSTEM";
    }

    /**
     * Get cell value as String
     */
    private String getCellValueAsString(Cell cell) {
        if (cell == null) return "";
        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue();
            case NUMERIC:
                // Handle as integer (no decimals for pincode)
                return String.valueOf((long) cell.getNumericCellValue());
            default:
                return "";
        }
    }

    /**
     * Get submitted file name from Part
     */
    private String getSubmittedFileName(Part part) {
        if (part == null) return null;
        for (String content : part.getHeader("content-disposition").split(";")) {
            if (content.trim().startsWith("filename")) {
                return content.substring(content.indexOf('=') + 1).trim().replace("\"", "");
            }
        }
        return null;
    }

    /**
     * Escape CSV special characters
     */
    private String escapeCSV(String value) {
        if (value == null) return "";
        return value.replace("\"", "\"\"");
    }

    private int parseIntOrDefault(String value, int defaultValue) {
        try {
            return Integer.parseInt(value);
        } catch (Exception e) {
            return defaultValue;
        }
    }

    // ==================== JSON Response Helpers ====================

    private void sendJsonSuccess(HttpServletResponse response, String message, Object data)
            throws IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("message", message);
        result.put("data", data);

        PrintWriter out = response.getWriter();
        out.print(gson.toJson(result));
        out.flush();
    }

    private void sendJsonError(HttpServletResponse response, String message) throws IOException {
        sendJsonError(response, message, null);
    }

    private void sendJsonError(HttpServletResponse response, String message, Object data)
            throws IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        Map<String, Object> result = new HashMap<>();
        result.put("success", false);
        result.put("message", message);
        result.put("data", data);

        PrintWriter out = response.getWriter();
        out.print(gson.toJson(result));
        out.flush();
    }

    private void sendJsonPartialSuccess(HttpServletResponse response, String message, Object data)
            throws IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("partial", true);
        result.put("message", message);
        result.put("data", data);

        PrintWriter out = response.getWriter();
        out.print(gson.toJson(result));
        out.flush();
    }
}
