package com.ppcl.replacement.dao;

import com.ppcl.replacement.model.*;
import com.ppcl.replacement.util.DBConnectionPool;

import java.sql.*;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.Date;

import static com.ppcl.replacement.constants.MessageConstant.formatDefaultPrinterBooked;

/**
 * DAO for Printer Booking operations using PRINTER_ORDER and PRINTER_ORDER_ITEM tables (01_ddl.sql)
 */
public class PrinterBookingDAO {

    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");
    private static final SimpleDateFormat TIME_FORMAT = new SimpleDateFormat("HH:mm:ss");
    private static final ZoneId INDIA_TZ = ZoneId.of("Asia/Kolkata");
    private static final DateTimeFormatter DELIVERY_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private final ReplacementRequestDAO replacementRequestDAO = new ReplacementRequestDAO();
    private final TransitionWorkflowDao transitionWorkflowDao = new TransitionWorkflowDao();
    private final TatDao tatDao = new TatDao();

    /**
     * Get next ID for a table (no sequences, use MAX+1)
     */
    private int getNextId(final Connection conn, final String tableName) throws Exception {
        final String sql = "SELECT NVL(MAX(ID), 0) + 1 FROM " + tableName;
        try (final Statement stmt = conn.createStatement();
             final ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) {
                return rs.getInt(1);
            }
        }
        return 1;
    }

    /**
     * Get printer details for booking form
     */
    public Map<String, Object> getPrinterDetails(final int reqId) throws Exception {
        final Map<String, Object> result = new HashMap<>();

        // Get request info from REPLACEMENT_REQUEST
        final String reqSql =
                "SELECT r.ID, r.STATUS, r.REPLACEMENT_TYPE, " +
                        "       c.NAME AS CLIENT_NAME, c.ID AS CLIENT_ID, " +
                        "       req_ua.USER_ID AS REQUESTER_NAME, " +
                        "       owner_ua.USER_ID AS ACCOUNT_MANAGER, " +
                        "       tm.STAGE_CODE, tm.DESCRIPTION AS STAGE_NAME, " +
                        "       rr.NAME AS REASON_NAME " +
                        "FROM REPLACEMENT_REQUEST r " +
                        "LEFT JOIN CLIENT c ON c.ID = r.CLIENT_DOT_ID_SIGNING " +
                        "LEFT JOIN USER_ACCOUNT req_ua ON req_ua.ID = r.REQUESTER_USER_ID " +
                        "LEFT JOIN USER_ACCOUNT owner_ua ON owner_ua.ID = r.CURRENT_OWNER_ID " +
                        "LEFT JOIN TAT_MASTER tm ON tm.ID = r.CURRENT_STAGE " +
                        "LEFT JOIN REPLACEMENT_REASON rr ON rr.ID = r.REPLACEMENT_REASON_ID " +
                        "WHERE r.ID = ?";

        final Map<String, Object> requestInfo = new HashMap<>();

        try (final Connection con = DBConnectionPool.getConnection();
             final PreparedStatement ps = con.prepareStatement(reqSql)) {

            ps.setInt(1, reqId);

            try (final ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    requestInfo.put("id", rs.getInt("ID"));
                    requestInfo.put("status", rs.getString("STATUS"));
                    requestInfo.put("replacementType", rs.getString("REPLACEMENT_TYPE"));
                    requestInfo.put("requester", rs.getString("REQUESTER_NAME"));
                    requestInfo.put("accountManager", rs.getString("ACCOUNT_MANAGER"));
                    requestInfo.put("currentStage", rs.getString("STAGE_CODE"));
                    requestInfo.put("stageName", rs.getString("STAGE_NAME"));
                    requestInfo.put("clientName", rs.getString("CLIENT_NAME"));
                    requestInfo.put("clientId", rs.getInt("CLIENT_ID"));
                    requestInfo.put("reasonName", rs.getString("REASON_NAME"));
                }
            }
        }

        result.put("request", requestInfo);

        // Get printers from REPLACEMENT_PRINTER_DETAILS
        final String printerSql =
                "SELECT rpd.ID, rpd.AGR_PROD_ID, rpd.EXISTING_SERIAL, " +
                        "       rpd.EXISTING_P_MODEL_ID, rpd.NEW_P_MODEL_SELECTED_ID, rpd.NEW_P_MODEL_SELECTED_TEXT, " +
                        "       rpd.NEW_P_MODEL_SOURCE, rpd.RECOMMENDED_COMMENTS, " +
                        "       rpd.CONTACT_PERSON_NAME, rpd.CONTACT_PERSON_NUMBER, " +
                        "       pm.MODEL_NAME AS EXISTING_MODEL, " +
                        "       new_pm.MODEL_NAME AS NEW_MODEL_NAME, " +
                        "       c.BRANCH AS LOCATION, c.CITY, c.ID AS CLIENT_BR_ID " +
                        "FROM REPLACEMENT_PRINTER_DETAILS rpd " +
                        "LEFT JOIN P_MODEL pm ON pm.ID = rpd.EXISTING_P_MODEL_ID " +
                        "LEFT JOIN P_MODEL new_pm ON new_pm.ID = rpd.NEW_P_MODEL_SELECTED_ID " +
                        "LEFT JOIN CLIENT c ON c.ID = rpd.CLIENT_DOT_ID " +
                        "WHERE rpd.REPLACEMENT_REQUEST_ID = ?";

        final List<Map<String, Object>> printers = new ArrayList<>();

        try (final Connection con = DBConnectionPool.getConnection();
             final PreparedStatement ps = con.prepareStatement(printerSql)) {

            ps.setInt(1, reqId);

            try (final ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    final Map<String, Object> printer = new HashMap<>();
                    printer.put("id", rs.getInt("ID"));
                    printer.put("agrProdId", rs.getInt("AGR_PROD_ID"));
                    printer.put("serial", rs.getString("EXISTING_SERIAL"));
                    printer.put("existingModel", rs.getString("EXISTING_MODEL"));
                    printer.put("existingModelName", rs.getString("EXISTING_MODEL"));

                    // Use NEW_P_MODEL_SELECTED_ID if available, otherwise fallback to EXISTING_P_MODEL_ID
                    int newModelId = rs.getInt("NEW_P_MODEL_SELECTED_ID");
                    if (rs.wasNull() || newModelId == 0) {
                        newModelId = rs.getInt("EXISTING_P_MODEL_ID");
                    }
                    printer.put("newModelId", newModelId);

                    printer.put("newModelText", rs.getString("NEW_P_MODEL_SELECTED_TEXT"));
                    printer.put("newModelName", rs.getString("NEW_MODEL_NAME"));
                    printer.put("newModelSource", rs.getString("NEW_P_MODEL_SOURCE"));
                    printer.put("comments", rs.getString("RECOMMENDED_COMMENTS"));
                    printer.put("city", rs.getString("CITY"));
                    printer.put("location", rs.getString("LOCATION"));
                    printer.put("contactName", rs.getString("CONTACT_PERSON_NAME"));
                    printer.put("contactNumber", rs.getString("CONTACT_PERSON_NUMBER"));
                    printer.put("clientBrId", rs.getInt("CLIENT_BR_ID"));

                    printers.add(printer);
                }
            }
        }

        result.put("printers", printers);

        return result;
    }

    /**
     * Get replacement request details
     */
    public ReplacementRequest getReplacementRequest(final int reqId) throws Exception {
        final ReplacementRequest replacementRequest = replacementRequestDAO.getRequestById(reqId);

        final List<OrderItem> orderItems = new ArrayList<>();

        for (final ReplacementPrinter details : replacementRequest.getPrinters()) {
            final OrderItem orderItem = new OrderItem();

            orderItem.setLocationName(details.getLocation());
            orderItem.setSuggestedBranchId(details.getClientBrId());

            // If OrderItem expects a String label, replace this with your mapping.
            orderItem.setPrinterType(String.valueOf(details.getPrinterType()));

            orderItem.setQuantity(1);
            orderItem.setContactPerson(details.getContactPerson());
            orderItem.setContactNumber(details.getContactNumber());

            // Use recommended model as "new model"
            final Integer modelId = details.getRecommendedPModelId();
            String modelName = details.getRecommendedModelName();

            if (modelName == null || modelName.isBlank()) {
                // fallback order if name is missing
                if (details.getRecommendedModelText() != null && !details.getRecommendedModelText().isBlank()) {
                    modelName = details.getRecommendedModelText();
                } else if (details.getNewModelText() != null && !details.getNewModelText().isBlank()) {
                    modelName = details.getNewModelText();
                } else {
                    modelName = details.getExistingModelName();
                }
            }

            orderItem.setPrinterModelId(modelId != null ? modelId : 0);  // 0 => text-only
            orderItem.setNewPrinterModel(modelName);

            // Current date + 7 days (India TZ)
            orderItem.setDefaultDeliveryDate(
                    LocalDate.now(INDIA_TZ).plusDays(7).format(DELIVERY_FMT)
            );

            orderItems.add(orderItem);
        }
        return replacementRequest;
    }

    public List<OrderItem> getOrderItem(final int reqId) throws Exception {
        final ReplacementRequest replacementRequest = replacementRequestDAO.getRequestById(reqId);

        final List<OrderItem> orderItems = new ArrayList<>();

        for (final ReplacementPrinter details : replacementRequest.getPrinters()) {
            final OrderItem orderItem = new OrderItem();

            orderItem.setLocationName(details.getLocation());
            orderItem.setSuggestedBranchId(details.getClientBrId());

            // If OrderItem expects a String label, replace this with your mapping.
            orderItem.setPrinterType(String.valueOf(details.getPrinterType()));

            orderItem.setQuantity(1);
            orderItem.setContactPerson(details.getContactPerson());
            orderItem.setContactNumber(details.getContactNumber());

            // Use recommended model as "new model"
            final Integer modelId = details.getRecommendedPModelId();
            String modelName = details.getRecommendedModelName();

            if (modelName == null || modelName.isBlank()) {
                // fallback order if name is missing
                if (details.getRecommendedModelText() != null && !details.getRecommendedModelText().isBlank()) {
                    modelName = details.getRecommendedModelText();
                } else if (details.getNewModelText() != null && !details.getNewModelText().isBlank()) {
                    modelName = details.getNewModelText();
                } else {
                    modelName = details.getExistingModelName();
                }
            }

            orderItem.setPrinterModelId(modelId != null ? modelId : 0);  // 0 => text-only
            orderItem.setNewPrinterModel(modelName);

            // Current date + 7 days (India TZ)
            orderItem.setDefaultDeliveryDate(
                    LocalDate.now(INDIA_TZ).plusDays(7).format(DELIVERY_FMT)
            );

            orderItems.add(orderItem);
        }
        return orderItems;
    }

    /**
     * Get signatories (employees with designation hierarchy <= 3)
     */
    public List<User> getSignatories(Integer userId) throws Exception {
        final List<User> signatories = new ArrayList<>();

        final String sql = """
        SELECT
            ua.ID        AS USER_ID,
            ua.USER_ID   AS USER_NAME,
            d.NAME       AS DESIGNATION
        FROM (
            SELECT
                e.ID,
                LEVEL AS LVL
            FROM EMP e
            WHERE e.ISACTIVE = 1
            START WITH e.ID = (
                SELECT ua.EMP_ID
                FROM USER_ACCOUNT ua
                WHERE ua.ID = ?
            )
            CONNECT BY NOCYCLE PRIOR e.REPORTING_TO = e.ID
        ) mgr
        JOIN USER_ACCOUNT ua
            ON ua.EMP_ID = mgr.ID
        LEFT JOIN DESIG d
            ON d.ID = (
                SELECT e.DESIGNATION
                FROM EMP e
                WHERE e.ID = mgr.ID
            )
        WHERE mgr.LVL > 1
        ORDER BY mgr.LVL
        """;

        try (Connection conn = DBConnectionPool.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, userId);

            try (ResultSet rs = stmt.executeQuery()) { // Important
                while (rs.next()) {
                    User user = new User();
                    user.setId(rs.getInt("USER_ID"));
                    user.setName(rs.getString("USER_NAME"));
                    user.setRole(rs.getString("DESIGNATION"));
                    signatories.add(user);
                }
            }
        }

        return signatories;
    }

    /**
     * Book printer order - inserts into PRINTER_ORDER and PRINTER_ORDER_ITEM tables
     *
     * @param requestId  Replacement request ID
     * @param clientId   Client ID (number)
     * @param orderBy    User who placed the order
     * @param brId       Branch ID
     * @param empId      Employee ID
     * @param comments   Order comments
     * @param orderItems List of order items
     * @return Generated order ID
     */
    public int bookPrinterOrder(final int requestId, final int clientId, final String orderBy,
                                final int brId, final int brIdDisp, final int empId,
                                final String comments, final List<OrderItemSubmission> orderItems, final User user) throws Exception {

        try (final Connection conn = DBConnectionPool.getConnection()) {
            conn.setAutoCommit(false);
            try {
                final int orderId = bookPrinterOrder(conn, requestId, clientId, orderBy, brId, brIdDisp, empId, comments, orderItems, user);
                conn.commit();
                return orderId;
            } catch (final Exception e) {
                conn.rollback();
                throw e;
            }
        }
    }

    /**
     * Book printer order using shared connection for atomicity
     */
    public int bookPrinterOrder(final Connection conn, final int requestId, final int clientId, final String orderBy,
                                final int brId, final int brIdDisp, final int empId,
                                final String comments, final List<OrderItemSubmission> orderItems, final User user) throws Exception {

        final Date now = new Date();
        final String orderDate = DATE_FORMAT.format(now);
        final String orderTime = TIME_FORMAT.format(now);

        final int orderId = getNextId(conn, "PRINTER_ORDER");

        final String orderSql = "INSERT INTO PRINTER_ORDER " +
                "(ID, ORDER_DATE, ORDER_TIME, ORDER_BY, CLIENT_ID, STATUS, COMMENTS, BR_ID, BR_ID_DISP, EMP_ID) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (final PreparedStatement ps = conn.prepareStatement(orderSql)) {
            ps.setInt(1, orderId);
            ps.setString(2, orderDate);
            ps.setString(3, orderTime);
            ps.setString(4, orderBy);
            ps.setString(5, String.valueOf(clientId));
            ps.setInt(6, 1);
            ps.setString(7, truncate(comments, 128));
            ps.setInt(8, brId);
            ps.setInt(9, brIdDisp);
            ps.setInt(10, empId);
            ps.executeUpdate();
        }

        if (orderItems != null && !orderItems.isEmpty()) {
            insertOrderItems(conn, orderId, clientId, orderItems);
        }
        replacementRequestDAO.updateOrderInReplacementRequest(conn, requestId, orderId);
        replacementRequestDAO.updateOrderItemsInReplacementPrinter(conn, requestId, orderId);

        final int nextStageID = tatDao.getStageIdByCode(conn, "STG7_DISPATCH_LETTER");

        // Use system-generated default comment with client name and date
        final String clientName = getClientNameByRequestId(conn, requestId);
        final String comment = formatDefaultPrinterBooked(clientName);
        transitionWorkflowDao.transitionStage(conn, requestId, nextStageID, user.getId(), comment);

        return orderId;
    }

    /**
     * Insert order items into PRINTER_ORDER_ITEM table.
     * Each consolidated item (with qty > 1) is expanded into multiple rows (1 per printer).
     * Pickup and Cartridge quantities are distributed equally with first row getting remainder.
     * Example: 10 pickup for 3 printers → 4, 3, 3
     */
    private void insertOrderItems(final Connection conn, final int orderId, final int clientId,
                                  final List<OrderItemSubmission> orderItems) throws Exception {

        final String itemSql =
                "INSERT INTO PRINTER_ORDER_ITEM " +
                        "(ID, P_MODEL, QTY, CLIENT_ID, TARGET_DELIVERY_DATE, STATUS, " +
                        " PRINTER_TYPE, ORDER_ID, ORDER_COMMENT, CART_ORDER_REQ, " +
                        " INSTALL_SCHEDULE, CONTACT_PERSON, CONTACT_NUMBER, CONTRACT_TYPE, ACCESSORIES, " +
                        " CART_PICKUP_REQ, CART_PICKUP_QUANITY, CART_SEND_REQ, CART_SENT_QUANITY, PRINTER_PRICE) " +
                        "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        int itemId = getNextId(conn, "PRINTER_ORDER_ITEM");

        try (final PreparedStatement ps = conn.prepareStatement(itemSql)) {

            for (final OrderItemSubmission item : orderItems) {
                System.out.println("item is  ==>" + item.toString());

                int qty = item.getQuantity();
                if (qty <= 0) qty = 1;

                // Distribute pickup and cartridge quantities across rows
                final int[] pickupDistribution = distributeQuantity(item.getPickupQuantity(), qty);
                final int[] cartridgeDistribution = distributeQuantity(item.getCartridgeQuantity(), qty);

                // Create one row per printer
                for (int i = 0; i < qty; i++) {
                    int idx = 1;
                    ps.setInt(idx++, itemId++);
                    ps.setInt(idx++, item.getPrinterModelId());
                    ps.setInt(idx++, 1); // QTY = 1 per row
                    ps.setInt(idx++, clientId);

                    ps.setString(idx++, truncate(item.getDeliveryDate(), 50)); // VARCHAR2(50)

                    ps.setInt(idx++, 1); // STATUS
                    ps.setInt(idx++, parsePrinterType(item.getPrinterType()));
                    ps.setInt(idx++, orderId);
                    ps.setString(idx++, truncate(item.getComments(), 128));

                    ps.setInt(idx++, "yes".equalsIgnoreCase(item.getCartridgePickup()) ? 1 : 0); // CART_ORDER_REQ
                    ps.setInt(idx++, "yes".equalsIgnoreCase(item.getInstallation()) ? 1 : 0);

                    ps.setString(idx++, truncate(item.getContactPerson(), 50));
                    ps.setString(idx++, truncate(item.getContactNumber(), 50));

                    ps.setInt(idx++, 1);      // CONTRACT_TYPE
                    ps.setString(idx++, null); // ACCESSORIES

                    ps.setInt(idx++, "yes".equalsIgnoreCase(item.getCartridgePickup()) ? 1 : 0); // CART_PICKUP_REQ
                    ps.setInt(idx++, pickupDistribution[i]);

                    ps.setInt(idx++, "yes".equalsIgnoreCase(item.getSendCartridge()) ? 1 : 0);   // CART_SEND_REQ
                    ps.setInt(idx++, cartridgeDistribution[i]);

                    // PRINTER_PRICE NUMBER
                    if (item.getPrinterPrice() == 0.0) {
                        ps.setNull(idx++, java.sql.Types.NUMERIC);
                    } else {
                        ps.setBigDecimal(idx++, java.math.BigDecimal.valueOf(item.getPrinterPrice()));
                    }

                    ps.addBatch();
                }
            }

            ps.executeBatch();
        }
    }

    /**
     * Distribute a total quantity across n rows.
     * First row gets the full quantity, others get 0.
     * Example: distributeQuantity(10, 3) → [10, 0, 0]
     */
    private int[] distributeQuantity(final int total, final int count) {
        if (count <= 0) return new int[0];

        final int[] result = new int[count];
        result[0] = total; // First printer gets all
        for (int i = 1; i < count; i++) {
            result[i] = 0; // Others get 0
        }
        return result;
    }


    /**
     * Simplified book order method for myRequests.jsp
     */
    public int bookPrinterOrderSimple(final int requestId, final String orderBy, final User user, final int empId,
                                      final String signatoryId, final String orderItemsJson) throws Exception {

        // Get client signing ID (numeric) from replacement request
        final int clientId = getClientSigningId(requestId);
        final int brId = 1; // Default branch
        final int brIdDisp = parseIntSafe(signatoryId, 1);

        final String comments = "Replacement Request ID: " + requestId + ", Signatory: " + signatoryId;

        // Parse order items from JSON
        final List<OrderItemSubmission> orderItems = parseOrderItemsJson(orderItemsJson);

        return bookPrinterOrder(requestId, clientId, orderBy, brId, brIdDisp, empId, comments, orderItems, user);
    }

    /**
     * Get CLIENT_DOT_ID_SIGNING (numeric client ID) from replacement request
     */
    private int getClientSigningId(final int requestId) throws Exception {
        final String sql = "SELECT CLIENT_DOT_ID_SIGNING FROM REPLACEMENT_REQUEST WHERE ID = ?";

        try (final Connection conn = DBConnectionPool.getConnection();
             final PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, requestId);
            try (final ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("CLIENT_DOT_ID_SIGNING");
                }
            }
        }

        throw new Exception("Replacement request not found: " + requestId);
    }

    /**
     * Parse order items from JSON string
     */
    private List<OrderItemSubmission> parseOrderItemsJson(final String json) {
        final List<OrderItemSubmission> items = new ArrayList<>();

        if (json == null || json.trim().isEmpty()) {
            return items;
        }

        // Simple JSON array parsing
        String content = json.trim();
        if (content.startsWith("[") && content.endsWith("]")) {
            content = content.substring(1, content.length() - 1);
        }

        if (content.isEmpty()) {
            return items;
        }

        // Split by "},{"
        final String[] itemStrings = content.split("\\},\\s*\\{");

        for (String itemStr : itemStrings) {
            itemStr = itemStr.replace("{", "").replace("}", "").trim();
            if (itemStr.isEmpty()) continue;

            final OrderItemSubmission item = new OrderItemSubmission();
            item.setPrinterModelId(extractIntValue(itemStr, "pModel", 0));
            item.setQuantity(extractIntValue(itemStr, "qty", 1));
            item.setDeliveryDate(extractStringValue(itemStr, "deliveryDate"));
            item.setPrinterType(extractStringValue(itemStr, "printerType"));
            item.setContactPerson(extractStringValue(itemStr, "contactPerson"));
            item.setContactNumber(extractStringValue(itemStr, "contactNumber"));
            item.setCartridgePickup(extractStringValue(itemStr, "cartridgePickup"));
            item.setInstallation(extractStringValue(itemStr, "installation"));
            item.setComments(extractStringValue(itemStr, "comments"));
            item.setCartridgePickup(extractStringValue(itemStr, "cartridgePickup"));
            item.setPickupQuantity(extractIntValue(itemStr, "pickupQty", 0));
            item.setPrinterPrice(extractDoubleValue(itemStr, "printerPrice", 0.0));
// JSON has "cartridgeSend" but POJO field is sendCartridge
            item.setSendCartridge(extractStringValue(itemStr, "cartridgeSend"));
            item.setCartridgeQuantity(extractIntValue(itemStr, "cartQty", 0));


            items.add(item);
        }

        return items;
    }


    // Helper methods
    private String truncate(final String value, final int maxLength) {
        if (value == null) return null;
        return value.length() > maxLength ? value.substring(0, maxLength) : value;
    }

    private int parsePrinterType(final String type) {
        if (type == null) return 1;
        try {
            return Integer.parseInt(type);
        } catch (final NumberFormatException e) {
            return 1; // Default
        }
    }

    private int parseClientId(final String clientId) {
        if (clientId == null || clientId.trim().isEmpty()) {
            return 0;
        }
        try {
            return Integer.parseInt(clientId.trim());
        } catch (final NumberFormatException e) {
            return 0;
        }
    }

    private int parseIntSafe(final String value, final int defaultValue) {
        if (value == null || value.trim().isEmpty()) {
            return defaultValue;
        }
        try {
            return Integer.parseInt(value.trim());
        } catch (final NumberFormatException e) {
            return defaultValue;
        }
    }

    private String extractStringValue(final String json, final String key) {
        if (json == null || key == null) return null;

        final String pattern = "\"" + java.util.regex.Pattern.quote(key) + "\"\\s*:\\s*\"([^\"]*)\"";
        final java.util.regex.Matcher m = java.util.regex.Pattern.compile(pattern).matcher(json);
        return m.find() ? m.group(1) : null;
    }


    private int extractIntValue(final String json, final String key, final int defaultValue) {
        if (json == null || key == null) return defaultValue;

        // matches: "key":10  OR  "key":"10"
        final String pattern = "\"" + java.util.regex.Pattern.quote(key) + "\"\\s*:\\s*\"?(\\d+)\"?";
        final java.util.regex.Matcher m = java.util.regex.Pattern.compile(pattern).matcher(json);

        if (m.find()) {
            try {
                return Integer.parseInt(m.group(1));
            } catch (final Exception e) {
                return defaultValue;
            }
        }
        return defaultValue;
    }

    private double extractDoubleValue(final String json, final String key, final double defaultValue) {
        if (json == null || key == null) return defaultValue;

        // matches: "key":1110  OR  "key":"1110"  OR decimals
        final String pattern = "\"" + java.util.regex.Pattern.quote(key) + "\"\\s*:\\s*\"?([0-9]+(?:\\.[0-9]+)?)\"?";
        final java.util.regex.Matcher m = java.util.regex.Pattern.compile(pattern).matcher(json);

        if (m.find()) {
            try {
                return Double.parseDouble(m.group(1));
            } catch (final Exception e) {
                return defaultValue;
            }
        }
        return defaultValue;
    }

    /**
     * Get all requests pending for Printer Booking (STG6_PRINTER_ORDER stage)
     */
    public List<Map<String, Object>> getPendingPrinterBookingRequests() throws Exception {
        final List<Map<String, Object>> requests = new ArrayList<>();

        final String sql = """
                SELECT r.ID, r.STATUS, r.REPLACEMENT_TYPE, r.CREATED_AT,
                       c.NAME AS CLIENT_NAME, c.ID AS CLIENT_ID,
                       req_ua.USER_ID AS REQUESTER_NAME, req_ua.NAME AS REQUESTER_FULL_NAME,
                       tm.STAGE_CODE, tm.DESCRIPTION AS STAGE_NAME,
                       (SELECT COUNT(*) FROM REPLACEMENT_PRINTER_DETAILS WHERE REPLACEMENT_REQUEST_ID = r.ID) AS PRINTER_COUNT
                FROM REPLACEMENT_REQUEST r
                LEFT JOIN CLIENT c ON c.ID = r.CLIENT_DOT_ID_SIGNING
                LEFT JOIN USER_ACCOUNT req_ua ON req_ua.ID = r.REQUESTER_USER_ID
                LEFT JOIN TAT_MASTER tm ON tm.ID = r.CURRENT_STAGE
                WHERE tm.STAGE_CODE = 'STG6_PRINTER_ORDER'
                  AND r.STATUS = 'ACTIVE'
                ORDER BY r.CREATED_AT DESC
                """;

        try (final Connection conn = DBConnectionPool.getConnection();
             final PreparedStatement ps = conn.prepareStatement(sql);
             final ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                final Map<String, Object> req = new HashMap<>();
                req.put("id", rs.getInt("ID"));
                req.put("status", rs.getString("STATUS"));
                req.put("replacementType", rs.getString("REPLACEMENT_TYPE"));
                req.put("clientName", rs.getString("CLIENT_NAME"));
                req.put("clientId", rs.getInt("CLIENT_ID"));
                req.put("requesterName", rs.getString("REQUESTER_FULL_NAME"));
                req.put("requesterId", rs.getString("REQUESTER_NAME"));
                req.put("stageCode", rs.getString("STAGE_CODE"));
                req.put("stageName", rs.getString("STAGE_NAME"));
                req.put("printerCount", rs.getInt("PRINTER_COUNT"));
                req.put("createdAt", rs.getTimestamp("CREATED_AT"));
                requests.add(req);
            }
        }

        return requests;
    }

    /**
     * Get client name by request ID (for default comments)
     */
    private String getClientNameByRequestId(final Connection con, final int reqId) throws SQLException {
        final String sql = "SELECT c.NAME FROM REPLACEMENT_REQUEST r " +
                "LEFT JOIN CLIENT c ON c.ID = r.CLIENT_DOT_ID_SIGNING " +
                "WHERE r.ID = ?";

        try (final PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, reqId);
            try (final ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("NAME");
                }
            }
        }
        return null;
    }
}
