package com.ppcl.replacement.dao;

import com.ppcl.replacement.model.*;
import com.ppcl.replacement.util.DateUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.*;

import static com.ppcl.replacement.constants.MessageConstant.NEW_REPLACEMENT_REQUEST_MESSAGE;
import static com.ppcl.replacement.constants.MessageConstant.formatDefaultRequestCreated;
import static com.ppcl.replacement.constants.ReplacementRequestSQLConstant.*;
import static com.ppcl.replacement.constants.StageConstants.STAGE_FLOW;

public class ReplacementRequestDAO extends BaseDAO {

    private static final Logger log = LoggerFactory.getLogger(ReplacementRequestDAO.class);
    private final UserDAO userDao = new UserDAO();
    private final ChatLogDao chatLogDao = new ChatLogDao();
    private final TransitionWorkflowDao transitionWorkflowDao = new TransitionWorkflowDao();
    private final TatDao tatDao = new TatDao();

    /**
     * Get all printer models from P_MODEL table.
     *
     * @return a list of {@link PrinterModel} objects
     * @throws Exception if a database error occurs
     */
    public List<PrinterModel> getAllPrinterModels() throws Exception {
        final List<PrinterModel> list = new ArrayList<>();

        try (final Connection con = getConnection();
             final PreparedStatement ps = con.prepareStatement(GET_ALL_PRINTER_MODELS);
             final ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                final PrinterModel m = new PrinterModel();
                m.setId(rs.getInt("ID"));
                m.setModelName(rs.getString("MODEL_NAME"));
                list.add(m);
            }
        }
        return list;
    }

    /**
     * Get service call details by CALLID.
     *
     * @param callId the ID of the service call
     * @return a {@link ServiceCall} object, or {@code null} if not found
     * @throws Exception if a database error occurs
     */
    public ServiceCall getServiceCallDetails(final int callId) throws Exception {
        try (final Connection con = getConnection();
             final PreparedStatement ps = con.prepareStatement(GET_SERVICE_CALL_DETAILS)) {
            ps.setInt(1, callId);
            try (final ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    final ServiceCall sc = new ServiceCall();
                    sc.setCallId(rs.getInt("CALLID"));
                    sc.setClientId(rs.getString("CLIENTID"));
                    sc.setClientName(rs.getString("CLIENTNAME"));
                    sc.setCallBy(rs.getString("CALLBY"));
                    sc.setContactNo(rs.getString("CONTACTNO"));
                    sc.setPModel(rs.getString("PMODEL"));
                    sc.setPSerial(rs.getString("PSERIAL"));
                    sc.setBrId(rs.getInt("BRID"));
                    return sc;
                }
            }
        }
        return null;
    }

    /**
     * Get client contact from recent service call (for Service Team).
     *
     * @param clientId the ID of the client
     * @return a {@link Client} object with contact details
     * @throws Exception if a database error occurs
     */
    public Client getClientContactFromServiceCall(final String clientId) throws Exception {
        try (final Connection con = getConnection();
             final PreparedStatement ps = con.prepareStatement(GET_CLIENT_CONTACT_FROM_SERVICE_CALL)) {
            ps.setString(1, clientId);
            ps.setString(2, clientId);
            try (final ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    final Client c = new Client();
                    c.setClientId(clientId);
                    c.setName(rs.getString("NAME"));
                    c.setContactPerson(rs.getString("CALLBY")); // From service call
                    c.setMobileNo(rs.getString("CONTACTNO"));    // From service call
                    c.setEmailId1(rs.getString("EMAILID1"));
                    return c;
                }
            }
        }

        // Fallback to client master
        return getClientByClientId(clientId);
    }

    /**
     * Get client details (for AM).
     *
     * @param clientId the ID of the client
     * @return a {@link Client} object, or {@code null} if not found
     * @throws Exception if a database error occurs
     */
    public Client getClientByClientId(final String clientId) throws Exception {
        try (final Connection con = getConnection();
             final PreparedStatement ps = con.prepareStatement(GET_CLIENT_BY_CLIENT_ID)) {
            ps.setString(1, clientId);
            try (final ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    final Client c = new Client();
                    c.setClientId(rs.getString("CLIENTID"));
                    c.setName(rs.getString("NAME"));
                    c.setContactPerson(rs.getString("CONTACTPERSON"));
                    c.setMobileNo(rs.getString("MOBILENO"));
                    c.setEmailId1(rs.getString("EMAILID1"));
                    return c;
                }
            }
        }
        return null;
    }

    /**
     * Create replacement request - inserts into:
     * 1. REPLACEMENT_REQUEST (header)
     * 2. REPLACEMENT_PRINTER_DETAILS (printer details)
     * 3. RPLCE_FLOW_EVENT_TRACKING (flow event tracking)
     */
    public int createReplacementRequest(final ReplacementRequest req, final int userId, final String userRole) throws Exception {
        Connection con = null;
        int reqId = 0;

        try {
            con = getConnection();
            con.setAutoCommit(false);

            // Get initial stage ID from TAT_MASTER
            //As soon it will be submitted it will be assigned to TL for recommendation
            final int initialStageId = tatDao.getStageIdByCode(con, "STG1_REQUEST_RAISED");
            final int nextStageId = nextStageFromId(initialStageId);
            //User nextStageUser = userDao.getNextStageOwnerUsingStageID(con, nextStageId);

            // Get requester user account ID
            int requesterUserAccountId = userId;

            // Get sign-in branch ID (CLIENT_DOT_ID_SIGNING)
            final int signInBranchId = Integer.parseInt(req.getClientId());

            // Insert into REPLACEMENT_REQUEST table
            try (final PreparedStatement ps = con.prepareStatement(INSERT_REPLACEMENT_REQUEST, new String[]{"ID"})) {
                ps.setInt(1, req.getReasonId());
                ps.setInt(2, signInBranchId);
                ps.setString(3, req.getReplacementType());
                ps.setInt(4, requesterUserAccountId);

                if (req.getServiceCallId() != null && req.getServiceCallId() > 0) {
                    ps.setInt(5, req.getServiceCallId());
                } else {
                    ps.setNull(5, Types.INTEGER);
                }

                ps.setInt(6, nextStageId);
                ps.setInt(7, req.getTlLeadId()); // Initially owner is requester
                ps.setString(8, req.getSource() != null ? req.getSource() : "DIRECT");
                ps.setString(9, req.getContactPerson());
                ps.setString(10, req.getContactNumber());
                ps.setString(11, req.getContactEmail());

                ps.executeUpdate();

                try (final ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        reqId = rs.getInt(1);
                    }
                }
            }

            // Insert into REPLACEMENT_PRINTER_DETAILS table
            try (final PreparedStatement ps = con.prepareStatement(INSERT_REPLACEMENT_PRINTER_DETAILS)) {
                for (final ReplacementPrinter p : req.getPrinters()) {
                    ps.setInt(1, reqId);
                    ps.setInt(2, p.getAgrProdId());
                    ps.setInt(3, p.getExistingPModelId());
                    ps.setInt(4, p.getClientBrId());
                    ps.setString(5, p.getContactPerson());
                    ps.setString(6, p.getContactNumber());
                    ps.setString(7, p.getContactEmail());
                    ps.setString(8, p.getExistingSerial());

                    // Handle new model selection (dropdown vs manual)
                    if (p.getNewPModelSelectedId() != null && p.getNewPModelSelectedId() > 0) {
                        ps.setInt(9, p.getNewPModelSelectedId());
                        ps.setNull(10, Types.VARCHAR);
                        ps.setString(11, "AUTO");
                    } else if (p.getNewModelText() != null && !p.getNewModelText().isEmpty()) {
                        ps.setNull(9, Types.INTEGER);
                        ps.setString(10, p.getNewModelText());
                        ps.setString(11, "MANUAL");
                    } else {
                        ps.setNull(9, Types.INTEGER);
                        ps.setNull(10, Types.VARCHAR);
                        ps.setString(11, "MANUAL");
                    }
                    ps.setInt(12,0);
                    ps.addBatch();
                }
                ps.executeBatch();
            }
            final Client client = new ClientDAO().getClientById(signInBranchId);

            // Use default system-generated comment if user didn't provide any
            String effectiveComment = req.getComments();
            if (effectiveComment == null || effectiveComment.trim().isEmpty()) {
                effectiveComment = formatDefaultRequestCreated(client != null ? client.getName() : "Unknown");
            }

            transitionWorkflowDao.openFlowEventTracking(con, reqId, initialStageId, requesterUserAccountId, effectiveComment);
            transitionWorkflowDao.transitionWorkflow(con, reqId, initialStageId, requesterUserAccountId, nextStageId, req.getTlLeadId(), effectiveComment, null);

            final User toUser = userDao.getUserByUserId(req.getTlLeadId());
            final User fromUser = userDao.getUserByUserId(requesterUserAccountId);
            final String message = NEW_REPLACEMENT_REQUEST_MESSAGE.formatted(client.getName());
            chatLogDao.insertChatLog(con, fromUser.getName(), toUser.getName(), message, 1, null);
            con.commit();

        } catch (final Exception e) {
            if (con != null) con.rollback();
            throw e;
        }

        return reqId;
    }

    /**
     * Update replacement request - updates:
     * 1. REPLACEMENT_REQUEST (header) - replacement type, reason, sign-in location
     * 2. REPLACEMENT_PRINTER_DETAILS - contact info, new model selection
     * 3. RPLCE_COMMENTS_TRACKING - adds comment if provided
     */
    public void updateReplacementRequest(final int reqId, final String replacementType, final int reasonId,
                                         final int signInBranchId, final String comments, final String contactName,
                                         final String contactNumber, final String contactEmail,
                                         final List<Map<String, Object>> printers, final int userId) throws Exception {
        Connection con = null;

        System.out.print("reasonId " + reasonId);

        try {
            con = getConnection();
            con.setAutoCommit(false);

            // Check if request is editable
            try (final PreparedStatement ps = con.prepareStatement(CHECK_REQUEST_EDITABLE)) {
                ps.setInt(1, reqId);
                try (final ResultSet rs = ps.executeQuery()) {
                    if (rs.next() && rs.getInt("IS_EDITABLE") != 1) {
                        throw new Exception("Request is not editable");
                    }
                }
            }

            // Update REPLACEMENT_REQUEST header (including sign-in location)
            try (final PreparedStatement ps = con.prepareStatement(UPDATE_REPLACEMENT_REQUEST_HEADER)) {
                ps.setString(1, replacementType);
                ps.setInt(2, reasonId);
                ps.setInt(3, signInBranchId);
                ps.setInt(4, reqId);
                ps.executeUpdate();
            }

            // Update contact details for all printers in this request
            try (final PreparedStatement ps = con.prepareStatement(UPDATE_PRINTER_CONTACT_DETAILS)) {
                ps.setString(1, contactName);
                ps.setString(2, contactNumber);
                ps.setString(3, contactEmail);
                ps.setInt(4, reqId);
                ps.executeUpdate();
            }

            // Update each printer's new model selection
            if (printers != null && !printers.isEmpty()) {
                try (final PreparedStatement ps = con.prepareStatement(UPDATE_PRINTER_MODEL_SELECTION)) {
                    for (final Map<String, Object> p : printers) {
                        final int printerId = ((Number) p.get("id")).intValue();
                        final Object newModelIdObj = p.get("newModelId");
                        final String newModelText = (String) p.get("newModelText");

                        if (newModelIdObj != null && !newModelIdObj.toString().isEmpty()) {
                            final int newModelId = Integer.parseInt(newModelIdObj.toString());
                            ps.setInt(1, newModelId);
                            ps.setNull(2, Types.VARCHAR);
                            ps.setString(3, "AUTO");
                        } else if (newModelText != null && !newModelText.trim().isEmpty()) {
                            ps.setNull(1, Types.INTEGER);
                            ps.setString(2, newModelText.trim());
                            ps.setString(3, "MANUAL");
                        } else {
                            ps.setNull(1, Types.INTEGER);
                            ps.setNull(2, Types.VARCHAR);
                            ps.setString(3, "MANUAL");
                        }
                        ps.setInt(4, printerId);
                        ps.addBatch();
                    }
                    ps.executeBatch();
                }
            }

            // Add comment if provided
            if (comments != null && !comments.trim().isEmpty()) {
                insertComment(con, reqId, userId, comments);
            }

            con.commit();
        } catch (final Exception e) {
            if (con != null) con.rollback();
            throw e;
        }
    }

    /**
     * Insert comment into RPLCE_COMMENTS_TRACKING
     */
    private void insertComment(final Connection con, final int reqId, final int userId, final String comment) throws Exception {
        try (final PreparedStatement ps = con.prepareStatement(INSERT_COMMENT)) {
            ps.setInt(1, reqId);
            ps.setString(2, String.valueOf(userId));
            ps.setString(3, comment);
            ps.executeUpdate();
        }
    }


    /**
     * Get my pending requests with enhanced display including TAT calculation.
     * TAT is calculated from RPLCE_FLOW_EVENT_TRACKING (actual time = END_AT - START_AT or SYSTIMESTAMP - START_AT)
     * and TAT_MASTER (TAT_DURATION and TAT_DURATION_UNIT for each stage).
     *
     * @param userId   the user account ID
     * @param userRole the role of the user
     * @return a list of {@link MyRequestRow} objects
     * @throws Exception if a database error occurs
     */
    public List<MyRequestRow> getMyPendingRequests(final int userId, final String userRole) throws Exception {
        final List<MyRequestRow> list = new ArrayList<>();

        try (final Connection con = getConnection();
             final PreparedStatement ps = con.prepareStatement(GET_MY_PENDING_REQUESTS)) {

            ps.setInt(1, userId);
            ps.setInt(2, userId);

            try (final ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    final MyRequestRow r = new MyRequestRow();
                    r.setId(rs.getInt("ID"));
                    r.setClientId(rs.getString("CLIENTID"));
                    r.setSigningBranch(rs.getString("SIGNING_BRANCH"));
                    r.setBranch(rs.getString("BRANCH"));
                    r.setClientName(rs.getString("CLIENTNAME"));
                    r.setPrinterCount(rs.getInt("PRINTERCOUNT"));
                    r.setCurrentStage(rs.getString("CURRENTSTAGE"));
                    r.setCurrentStageName(rs.getString("CURRENTSTAGENAME"));
                    r.setCurrentOwnerUserId(rs.getString("OWNERUSERID"));
                    r.setStageOwnerName(rs.getString("OWNERNAME"));
                    r.setCreatedAt(rs.getTimestamp("CREATEDAT"));
                    r.setEditable(rs.getInt("IS_EDITABLE") == 1);
                    r.setStatus(rs.getString("STATUS"));
                    r.setAllotedPrinterCount(rs.getInt("ALLOT_COUNT"));
                    final Integer svcCallId = rs.getInt("SERVICE_CALL_ID");
                    r.setServiceCallId(rs.wasNull() ? null : svcCallId);

                    // TAT Calculation using DateUtil (working hours: Mon-Fri, 9-17)
                    final int tatDuration = rs.getInt("TAT_DURATION");
                    final String tatUnit = rs.getString("TAT_DURATION_UNIT");
                    final Timestamp stageStart = rs.getTimestamp("STAGE_START");
                    final Timestamp stageEnd = rs.getTimestamp("STAGE_END");

                    if (tatDuration > 0 && stageStart != null) {
                        final java.util.Date endTime = (stageEnd != null) ? stageEnd : new java.util.Date();
                        final double percentage = DateUtil.calculateTatPercentage(stageStart, endTime, tatDuration, tatUnit);

                        final String unit = (tatUnit != null) ? tatUnit : "DAYS";
                        final long tatDurationMinutes = "HOURS".equalsIgnoreCase(unit)
                                ? tatDuration * 60L
                                : tatDuration * 8L * 60L;
                        final long actualMinutes = DateUtil.workingMinutesBetween(
                                stageStart, endTime, java.time.ZoneId.systemDefault());

                        r.setTatDurationMinutes(tatDurationMinutes);
                        r.setTatActualMinutes(actualMinutes);
                        r.setTatUnit(tatUnit);
                        r.setTatPercentage(percentage);
                        r.setTatStatus(DateUtil.getTatStatus(percentage));
                    }

                    list.add(r);
                }
            }
        }

        return list;
    }

    /**
     * Get requests pending at Service TL Review stage (STG2_SERVICE_TL_REVIEW).
     * Used for embedded view in createRequest.jsp.
     *
     * @param userId the user account ID
     * @return a list of {@link MyRequestRow} objects
     * @throws Exception if a database error occurs
     */
    public List<MyRequestRow> getRequestsAtServiceTLReview(final int userId) throws Exception {
        final List<MyRequestRow> list = new ArrayList<>();

        try (final Connection con = getConnection();
             final PreparedStatement ps = con.prepareStatement(GET_REQUEST_AT_SERVICE_TL_STATUS)) {

            try (final ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    final MyRequestRow r = new MyRequestRow();
                    r.setId(rs.getInt("ID"));
                    r.setClientId(rs.getString("CLIENTID"));
                    r.setClientName(rs.getString("CLIENTNAME"));
                    r.setBranch(rs.getString("BRANCH"));
                    r.setPrinterCount(rs.getInt("PRINTERCOUNT"));
                    r.setCurrentStage(rs.getString("CURRENTSTAGE"));
                    r.setCurrentStageName(rs.getString("CURRENTSTAGENAME"));
                    r.setCurrentOwnerUserId(rs.getString("OWNERUSERID"));
                    r.setStageOwnerName(rs.getString("OWNERNAME"));
                    r.setCreatedAt(rs.getTimestamp("CREATEDAT"));
                    r.setEditable(rs.getInt("IS_EDITABLE") == 1);
                    r.setStatus(rs.getString("STATUS"));

                    final Integer svcCallId = rs.getInt("SERVICE_CALL_ID");
                    r.setServiceCallId(rs.wasNull() ? null : svcCallId);

                    // TAT Calculation
                    final int tatDuration = rs.getInt("TAT_DURATION");
                    final String tatUnit = rs.getString("TAT_DURATION_UNIT");
                    final Timestamp stageStart = rs.getTimestamp("STAGE_START");
                    final Timestamp stageEnd = rs.getTimestamp("STAGE_END");

                    if (tatDuration > 0 && stageStart != null) {
                        final long tatDurationMinutes;
                        if ("HOURS".equalsIgnoreCase(tatUnit)) {
                            tatDurationMinutes = tatDuration * 60L;
                        } else {
                            tatDurationMinutes = tatDuration * 24L * 60L;
                        }

                        final long endTime = (stageEnd != null) ? stageEnd.getTime() : System.currentTimeMillis();
                        final long actualMinutes = (endTime - stageStart.getTime()) / (1000 * 60);
                        final double percentage = (tatDurationMinutes > 0) ? (actualMinutes * 100.0 / tatDurationMinutes) : 0;

                        final String tatStatus;
                        if (percentage >= 100) {
                            tatStatus = "BREACH";
                        } else if (percentage >= 80) {
                            tatStatus = "WARNING";
                        } else {
                            tatStatus = "WITHIN";
                        }

                        r.setTatDurationMinutes(tatDurationMinutes);
                        r.setTatActualMinutes(actualMinutes);
                        r.setTatUnit(tatUnit);
                        r.setTatPercentage(percentage);
                        r.setTatStatus(tatStatus);
                    }

                    list.add(r);
                }
            }
        }

        return list;
    }


    private Integer nextStageFromId(final int stageId) throws Exception {
        try (final Connection con = getConnection();
             final PreparedStatement ps = con.prepareStatement(NEXT_STAGE_ID_SQL)) {
            ps.setInt(1, stageId);
            try (final ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("NEXT_STAGE_ID");
                }
            }
        }
        return null;
    }

    public String getNextStageCode(final String currentStage) {
        if (currentStage == null) return null;
        final int idx = STAGE_FLOW.indexOf(currentStage);
        if (idx == -1 || idx == STAGE_FLOW.size() - 1) {
            return null;
        }
        return STAGE_FLOW.get(idx + 1);
    }


    public String getReplacementRequestRequesterName(int reqId) throws Exception {
        try (final Connection con = getConnection();
             final PreparedStatement ps = con.prepareStatement(GET_REPLACEMENT_REQUESTER_NAME)) {
            ps.setInt(1, reqId);
            try (final ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("USER_NAME");
                }
            }
        }
        return null;
    }

    public String getUserForOwnerRole(final String ownerRole) throws Exception {
        if (ownerRole == null) return null;

        final String desigName;
        switch (ownerRole) {
            case "SERVICE_TL":
                desigName = "TEAM LEADER SERVICE";
                break;
            case "CRO_TL":
                desigName = "TEAM LEADER CRO";
                break;
            case "AM":
                desigName = "ACCOUNT MANAGER";
                break;
            case "AM_MANAGER":
                desigName = "MANAGER CRO OPERATIONS";
                break;
            default:
                desigName = ownerRole;
        }

        final String sql =
                "SELECT ua.USER_ID " +
                        "FROM USER_ACCOUNT ua " +
                        "LEFT JOIN DESIG d ON ua.DEPT_ID = d.ID " +
                        "WHERE d.NAME LIKE ? AND ROWNUM = 1";

        try (final Connection con = getConnection();
             final PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, "%" + desigName + "%");
            try (final ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("USER_ID");
                }
            }
        }
        return null;
    }

    public Integer getNextStageOwnerUserId(final int reqId) throws Exception {

        final String ownerSql = "SELECT CURRENT_OWNER_ID FROM REPLACEMENT_REQUEST WHERE ID = ?";
        try (final Connection con = getConnection();
             final PreparedStatement ps = con.prepareStatement(ownerSql)) {
            ps.setInt(1, reqId);
            try (final ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("CURRENT_OWNER_ID");
                }
            }
        }
        return null;

    }

    public void insertReminderChatLog(final int reqId, final Integer fromUser, final Integer toUser) throws Exception {
        Connection con = null;
        try {
            con = getConnection();
            User tUser = userDao.getUserByUserId(toUser);
            User fUser = userDao.getUserByUserId(fromUser);
            chatLogDao.insertChatLog(con, fUser.getName(), tUser.getName(),
                    "Reminder: Replacement request ID " + reqId + " is pending for your action.", 11, null);

        } catch (Exception e) {
            log.error("Error while inserting Reminder");
        }
    }

    private void insertAuditLog(final Connection con, final int reqId, final Integer printerId, final String action,
                                final String actorUserId, final String actorRole, final String details) throws Exception {
        final String sql =
                "INSERT INTO REPL_AUDIT_LOG " +
                        "  (ID, REQID, PRINTERID, ACTION, ACTORUSERID, ACTORROLE, DETAILS, CREATEDAT) " +
                        "VALUES " +
                        "  (SEQ_REPLAUDITLOG.NEXTVAL, ?, ?, ?, ?, ?, ?, SYSTIMESTAMP)";

        try (final PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, reqId);
            if (printerId != null) {
                ps.setInt(2, printerId);
            } else {
                ps.setNull(2, Types.INTEGER);
            }
            ps.setString(3, action);
            ps.setString(4, actorUserId);
            ps.setString(5, actorRole);
            ps.setString(6, details);
            ps.executeUpdate();
        }
    }

    /**
     * Book printer order (STG6_PRINTER_ORDER -> next stage).
     *
     * @param reqId        the ID of the replacement request
     * @param orderRef     the order reference number
     * @param deliveryDate the expected delivery date
     * @param comments     the transition comments
     * @param userId       the user account ID who performed the action
     * @throws Exception if a database error occurs
     */
    public void bookPrinterOrder(final int reqId, final String orderRef, final String deliveryDate,
                                 final String comments, final int userId) throws Exception {
        final int toStageId;
        try (final Connection con = getConnection()) {
            toStageId = tatDao.getStageIdByCode(con, "STG7_DISPATCH_LETTER");
        }
        final int toUserId = userId;
        transitionWorkflowDao.transitionStage(reqId, toStageId, toUserId, comments);
    }

    /**
     * Get replacement request by ID with all details.
     *
     * @param requestId the ID of the replacement request
     * @return a {@link ReplacementRequest} object, or {@code null} if not found
     * @throws Exception if a database error occurs
     */
    public ReplacementRequest getRequestById(final int requestId) throws Exception {
        ReplacementRequest req = null;

        try (final Connection con = getConnection();
             final PreparedStatement ps = con.prepareStatement(GET_REPLACEMENT_REQUEST_USING_REQUEST_ID)) {
            ps.setInt(1, requestId);
            try (final ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    req = new ReplacementRequest();
                    req.setId(rs.getInt("ID"));
                    req.setReplacementType(rs.getString("REPLACEMENT_TYPE"));
                    req.setReasonId(rs.getInt("REPLACEMENT_REASON_ID"));
                    req.setReasonName(rs.getString("REASON_NAME"));
                    req.setClientId(rs.getString("CLIENT_DOT_ID_SIGNING"));
                    req.setClientName(rs.getString("CLIENT_NAME"));
                    req.setSource(rs.getString("SOURCE"));
                    req.setCurrentStage(rs.getString("STAGE_CODE"));
                    req.setRequesterUserId(rs.getString("REQUESTER_USER_ID"));
                    req.setRequesterName(rs.getString("REQUESTER_NAME"));
                    req.setCurrentOwnerRole(rs.getString("OWNER_ROLE"));
                    req.setServiceCallId(rs.getObject("SERVICE_CALL_ID") != null ?
                            rs.getInt("SERVICE_CALL_ID") : null);
                    req.setCreatedAt(rs.getTimestamp("CREATION_DATE_TIME"));
                    req.setUpdatedAt(rs.getTimestamp("UPDATE_DATE_TIME"));
                }
            }
        }

        if (req != null) {
            req.setPrinters(getPrintersByRequestId(requestId));
        }

        return req;
    }

    /**
     * Get printers for a specific replacement request.
     *
     * @param requestId the ID of the replacement request
     * @return a list of {@link ReplacementPrinter} objects
     * @throws Exception if a database error occurs
     */
    private List<ReplacementPrinter> getPrintersByRequestId(final int requestId) throws Exception {
        final List<ReplacementPrinter> printers = new ArrayList<>();

        try (final Connection con = getConnection();
             final PreparedStatement ps = con.prepareStatement(GET_ALL_PRINTER_USING_REQUEST_ID)) {
            ps.setInt(1, requestId);
            try (final ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    final ReplacementPrinter p = new ReplacementPrinter();
                    p.setId(rs.getInt("ID"));
                    p.setClientBrId(rs.getInt("CLIENT_DOT_ID"));
                    p.setLocation(rs.getString("LOCATION_NAME"));
                    p.setCity(rs.getString("CITY"));
                    p.setExistingPModelId(rs.getInt("EXISTING_P_MODEL_ID"));
                    p.setExistingModelName(rs.getString("EXISTING_MODEL_NAME"));
                    p.setExistingSerial(rs.getString("EXISTING_SERIAL"));
                    p.setRecommendedModelName(rs.getString("RECOMMENDED_MODEL_NAME"));
                    p.setRecommendedModelText(rs.getString("NEW_P_MODEL_SELECTED_TEXT"));
                    p.setContactPerson(rs.getString("CONTACT_PERSON_NAME"));
                    p.setContactNumber(rs.getString("CONTACT_PERSON_NUMBER"));
                    printers.add(p);
                }
            }
        }

        return printers;
    }

    /**
     * Add a comment to the request tracking.
     *
     * @param reqId   the ID of the replacement request
     * @param userId  the user account ID
     * @param comment the comment text
     * @throws Exception if a database error occurs
     */
    public void addComment(final int reqId, final int userId, final String comment) throws Exception {
        try (final Connection con = getConnection()) {
            addComment(con, reqId, userId, comment);
        }
    }

    /**
     * Add a comment to the request tracking (using shared connection for atomicity).
     *
     * @param con     the database connection
     * @param reqId   the ID of the replacement request
     * @param userId  the user account ID
     * @param comment the comment text
     * @throws Exception if a database error occurs
     */
    public void addComment(final Connection con, final int reqId, final int userId, final String comment) throws Exception {
        try (final PreparedStatement ps = con.prepareStatement(INSERT_COMMENT_INTO_CMT_TRACKING_SQL)) {
            ps.setInt(1, reqId);
            ps.setString(2, comment);
            ps.setString(3, String.valueOf(userId));
            ps.executeUpdate();
        }
    }

    /**
     * Get request details payload for edit modal with TAT info.
     * Returns map with: request, tat, printers, contact, reasons.
     *
     * @param reqId the ID of the replacement request
     * @return a map containing the request details payload
     * @throws Exception if a database error occurs
     */
    public Map<String, Object> getRequestDetailsPayload(final int reqId) throws Exception {
        final Map<String, Object> result = new HashMap<>();

        try (final Connection con = getConnection();
             final PreparedStatement ps = con.prepareStatement(GET_REPLACEMENT_REQUEST_USING_REQUEST_ID_PAYLOAD)) {

            ps.setInt(1, reqId);
            ps.setInt(2, reqId);

            try (final ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    final Map<String, Object> reqInfo = new HashMap<>();
                    reqInfo.put("id", rs.getInt("ID"));
                    reqInfo.put("replacementType", rs.getString("REPLACEMENT_TYPE"));
                    reqInfo.put("reasonId", rs.getInt("REPLACEMENT_REASON_ID"));
                    reqInfo.put("status", rs.getString("STATUS"));
                    reqInfo.put("clientName", rs.getString("CLIENT_NAME"));
                    reqInfo.put("clientId", rs.getString("CLIENT_ID"));
                    reqInfo.put("currentStage", rs.getString("STAGE_CODE"));
                    reqInfo.put("currentStageName", rs.getString("STAGE_NAME"));
                    reqInfo.put("editable", rs.getInt("IS_EDITABLE") == 1);

                    reqInfo.put("signInBranchId", rs.getInt("CLIENT_DOT_ID_SIGNING"));
                    final String signInBranch = rs.getString("SIGN_IN_BRANCH");
                    final String signInCity = rs.getString("SIGN_IN_CITY");
                    reqInfo.put("signInLocation",
                            (signInBranch != null ? signInBranch : "") +
                                    (signInCity != null ? ", " + signInCity : ""));

                    result.put("request", reqInfo);

                    final Map<String, Object> tatInfo = new HashMap<>();
                    final Timestamp stageStart = rs.getTimestamp("STAGE_START");
                    final Timestamp currentTime = rs.getTimestamp("CURRENT_TIME");
                    final int tatDuration = rs.getInt("TAT_DURATION");
                    final String tatUnit = rs.getString("TAT_DURATION_UNIT");

                    if (stageStart != null) {
                        final java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("dd-MMM-yyyy HH:mm");
                        tatInfo.put("stageStartTime", sdf.format(stageStart));
                        tatInfo.put("currentTime", currentTime != null ? sdf.format(currentTime) : sdf.format(new java.util.Date()));
                        tatInfo.put("tatDuration", tatDuration);
                        tatInfo.put("tatUnit", tatUnit != null ? tatUnit : "DAYS");

                        final double percentage = com.ppcl.replacement.util.DateUtil.calculateTatPercentage(
                                stageStart, currentTime, tatDuration, tatUnit);
                        tatInfo.put("percentage", percentage);
                    }
                    result.put("tat", tatInfo);

                    result.put("printers", getRequestPrintersInternal(con, reqId));
                    result.put("contact", getRequestContactInternal(con, reqId));
                    result.put("reasons", getReplacementReasonsInternal(con));

                    // Add requester name
                    final String requesterName = getReplacementRequestRequesterName(reqId);
                    reqInfo.put("requesterName", requesterName);

                    result.put("success", true);
                } else {
                    result.put("success", false);
                    result.put("message", "Request not found");
                }
            }
        }

        return result;
    }

    /**
     * Get printers for request details (internal method using shared connection)
     */
    private List<Map<String, Object>> getRequestPrintersInternal(final Connection con, final int reqId) throws Exception {
        final List<Map<String, Object>> printers = new ArrayList<>();


        try (final PreparedStatement ps = con.prepareStatement(GET_ALL_PRINTER_USING_REQUEST_ID_PAYLOAD)) {
            ps.setInt(1, reqId);

            try (final ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    final Map<String, Object> p = new HashMap<>();
                    p.put("id", rs.getInt("ID"));
                    p.put("existingSerial", rs.getString("EXISTING_SERIAL"));
                    p.put("existingModelName", rs.getString("EXISTING_MODEL_NAME"));
                    p.put("location", rs.getString("LOCATION") + ", " + rs.getString("CITY"));
                    p.put("newModelId", rs.getInt("NEW_P_MODEL_SELECTED_ID"));
                    p.put("newModelText", rs.getString("NEW_P_MODEL_SELECTED_TEXT"));
                    p.put("contactName", rs.getString("CONTACT_PERSON_NAME"));
                    p.put("contactNumber", rs.getString("CONTACT_PERSON_NUMBER"));
                    p.put("contactEmail", rs.getString("CONTACT_PERSON_EMAIL"));
                    printers.add(p);
                }
            }
        }

        return printers;
    }

    /**
     * Get contact details from first printer in request (internal method)
     */
    private Map<String, Object> getRequestContactInternal(final Connection con, final int reqId) throws Exception {
        final Map<String, Object> contact = new HashMap<>();

        try (final PreparedStatement ps = con.prepareStatement(GET_CONTACT_DETAILS_FROM_PRINTER)) {
            ps.setInt(1, reqId);

            try (final ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    contact.put("contactName", rs.getString("CONTACT_PERSON_NAME"));
                    contact.put("contactNumber", rs.getString("CONTACT_PERSON_NUMBER"));
                    contact.put("contactEmail", rs.getString("CONTACT_PERSON_EMAIL"));
                }
            }
        }

        return contact;
    }

    /**
     * Get all active replacement reasons (internal method)
     */
    private List<Map<String, Object>> getReplacementReasonsInternal(final Connection con) throws Exception {
        final List<Map<String, Object>> reasons = new ArrayList<>();

        try (final PreparedStatement ps = con.prepareStatement(GET_ALL_REPLACEMENT_REASON);
             final ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                final Map<String, Object> r = new HashMap<>();
                r.put("id", rs.getInt("ID"));
                r.put("name", rs.getString("NAME"));
                reasons.add(r);
            }
        }

        return reasons;
    }

    /**
     * Check if a duplicate replacement request exists for the given printer.
     * Returns the existing request ID if duplicate found, otherwise null.
     *
     * @param clientBrId       The CLIENT.ID (location/branch)
     * @param existingSerial   The printer serial number
     * @param existingPModelId The printer model ID
     * @return Existing request ID if duplicate found, null otherwise
     */
    public Integer checkDuplicatePrinterRequest(final int clientBrId, final String existingSerial, final int existingPModelId) throws Exception {
        try (final Connection con = getConnection();
             final PreparedStatement ps = con.prepareStatement(CHECK_DUPLICATE_PRINTER_REQUEST)) {

            ps.setInt(1, clientBrId);
            ps.setString(2, existingSerial);
            ps.setInt(3, existingPModelId);

            try (final ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("REPLACEMENT_REQUEST_ID");
                }
            }
        }
        return null;
    }

    /**
     * Check all printers for duplicates before creating request.
     * Returns a map of serial -> existing request ID for any duplicates found.
     */
    public Map<String, Integer> checkDuplicatesForPrinters(final List<ReplacementPrinter> printers) throws Exception {
        final Map<String, Integer> duplicates = new LinkedHashMap<>();

        for (final ReplacementPrinter p : printers) {
            final Integer existingReqId = checkDuplicatePrinterRequest(
                    p.getClientBrId(),
                    p.getExistingSerial(),
                    p.getExistingPModelId()
            );
            if (existingReqId != null) {
                duplicates.put(p.getExistingSerial(), existingReqId);
            }
        }

        return duplicates;
    }

    public Integer checkDuplicateBySerial(final String serial) throws Exception {
        try (final Connection con = getConnection();
             final PreparedStatement ps = con.prepareStatement(CHECK_DUPLICATE_BY_SERIAL)) {
            ps.setString(1, serial.trim());
            try (final ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("REPLACEMENT_REQUEST_ID");
                }
            }
        }
        return null;
    }

    /**
     * Close a replacement request - sets status to CANCELLED and adds event tracking entry.
     */
    public boolean closeRequest(final int reqId, final String reason, final int userId) throws Exception {
        try (final Connection con = getConnection()) {
            con.setAutoCommit(false);

            transitionWorkflowDao.transitionCancelled(con, reqId, userId, reason);

            con.commit();
            return true;

        } catch (final Exception e) {
            throw e;
        }
    }

    public void updateOrderInReplacementRequest(
            final Connection conn, final int reqId, final int orderId) throws SQLException {

        final String sql =
                "UPDATE REPLACEMENT_REQUEST " +
                        "SET PRINTER_ORDER_ID = ? " +
                        "WHERE ID = ?";

        try (final PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, orderId);
            ps.setInt(2, reqId);

            if (ps.executeUpdate() != 1) {
                throw new SQLException("REPLACEMENT_REQUEST not updated, reqId=" + reqId);
            }
        }
    }


    /**
     * Update PRINTER_ORDER_ITEM_ID in REPLACEMENT_PRINTER_DETAILS.
     * Matches by NEW_P_MODEL_SELECTED_ID (the new printer model being ordered).
     * Uses row numbering to pair printers with order items in order of ID.
     */
    public void updateOrderItemsInReplacementPrinter(
            final Connection conn, final int reqId, final int orderId) throws SQLException {

        final String sql = """
                UPDATE REPLACEMENT_PRINTER_DETAILS rpd
                SET rpd.PRINTER_ORDER_ITEM_ID = (
                    SELECT poi_id
                    FROM (
                        SELECT
                            poi.ID AS poi_id,
                            poi.P_MODEL,
                            ROW_NUMBER() OVER (
                                PARTITION BY poi.P_MODEL
                                ORDER BY poi.ID
                            ) rn
                        FROM PRINTER_ORDER_ITEM poi
                        WHERE poi.ORDER_ID = ?
                    ) poi_rn
                    WHERE poi_rn.P_MODEL = rpd.NEW_P_MODEL_SELECTED_ID
                      AND poi_rn.rn = (
                          SELECT rpd_rn
                          FROM (
                              SELECT
                                  rpd2.ID,
                                  ROW_NUMBER() OVER (
                                      PARTITION BY rpd2.NEW_P_MODEL_SELECTED_ID
                                      ORDER BY rpd2.ID
                                  ) rpd_rn
                              FROM REPLACEMENT_PRINTER_DETAILS rpd2
                              WHERE rpd2.REPLACEMENT_REQUEST_ID = ?
                          ) rpd_rn_tbl
                          WHERE rpd_rn_tbl.ID = rpd.ID
                      )
                )
                WHERE rpd.REPLACEMENT_REQUEST_ID = ?
                """;

        try (final PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, orderId);
            ps.setInt(2, reqId);
            ps.setInt(3, reqId);

            final int updated = ps.executeUpdate();
            if (updated == 0) {
                throw new SQLException("No replacement printer rows updated for reqId=" + reqId);
            }
        }
    }

    public List<ReplacementRequest> getRequestsByRole(final String role) throws SQLException {
        final List<ReplacementRequest> requests = new ArrayList<>();
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            conn = getConnection();
            // In the new schema we should use CURRENT_OWNER_ROLE from REPL_REQ_HDR
            final String sql = "SELECT * FROM REPL_REQ_HDR WHERE CURRENT_OWNER_ROLE = ? ORDER BY CREATED_AT DESC";
            ps = conn.prepareStatement(sql);
            ps.setString(1, role);
            rs = ps.executeQuery();

            while (rs.next()) {
                final ReplacementRequest req = new ReplacementRequest();
                req.setId(rs.getInt("ID"));
                req.setCurrentStage(rs.getString("CURRENT_STAGE"));
                req.setCurrentOwnerRole(rs.getString("CURRENT_OWNER_ROLE"));
                req.setCreatedAt(rs.getTimestamp("CREATED_AT"));
                // Add more fields as needed
                requests.add(req);
            }
        } finally {
            closeResources(conn, ps, rs);
        }

        return requests;
    }

    public void updateRequestStage(final int reqId, final String newStage, final String newOwnerRole,
                                   final int userId, final String action, final String comment) throws SQLException {
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            conn = getConnection();
            conn.setAutoCommit(false);

            // Close current event
            final String sqlCloseEvent = "UPDATE REPL_OWNER_EVENT SET END_AT = SYSTIMESTAMP, ACTION = ? " +
                    "WHERE REQ_ID = ? AND END_AT IS NULL";
            ps = conn.prepareStatement(sqlCloseEvent);
            ps.setString(1, action);
            ps.setInt(2, reqId);
            ps.executeUpdate();
            ps.close();

            // Create new event
            final String sqlNewEvent = "INSERT INTO REPL_OWNER_EVENT (ID, REQ_ID, STAGE_CODE, OWNER_ROLE, START_AT) " +
                    "VALUES (SEQ_REPL_OWNER_EVENT.NEXTVAL, ?, ?, ?, SYSTIMESTAMP)";
            ps = conn.prepareStatement(sqlNewEvent, new String[]{"ID"});
            ps.setInt(1, reqId);
            ps.setString(2, newStage);
            ps.setString(3, newOwnerRole);
            ps.executeUpdate();

            rs = ps.getGeneratedKeys();
            int newEventId = 0;
            if (rs.next()) {
                newEventId = rs.getInt(1);
            }
            ps.close();
            rs.close();

            // Update header
            final String sqlUpdateHdr = "UPDATE REPL_REQ_HDR SET CURRENT_STAGE = ?, CURRENT_OWNER_ROLE = ?, " +
                    "CURRENT_EVENT_ID = ?, UPDATED_AT = SYSTIMESTAMP WHERE ID = ?";
            ps = conn.prepareStatement(sqlUpdateHdr);
            ps.setString(1, newStage);
            ps.setString(2, newOwnerRole);
            ps.setInt(3, newEventId);
            ps.setInt(4, reqId);
            ps.executeUpdate();
            ps.close();

            // Add comment if provided
//            if (comment != null && !comment.trim().isEmpty()) {
//                addComment(conn, reqId, userId, comment);
//            }
//
//            // Audit log
//            insertAuditLog(conn, reqId, null, action, userId, "", comment != null ? comment : action + " by " + userId);

            conn.commit();
        } catch (final SQLException e) {
            if (conn != null) {
                conn.rollback();
            }
            throw e;
        } finally {
            if (conn != null) {
                conn.setAutoCommit(true);
            }
            closeResources(conn, ps, rs);
        }
    }

    /**
     * Get comment history for a replacement request.
     *
     * @param requestId the ID of the replacement request
     * @return a list of maps containing comment details
     * @throws Exception if a database error occurs
     */
    public List<Map<String, Object>> getCommentHistory(final int requestId) throws Exception {
        List<Map<String, Object>> comments = new ArrayList<>();

        final String sql = """
                    SELECT r.COMMENTS, ua.ID, ua.USER_ID AS USER_NAME, r.START_AT,
                       tm.DESCRIPTION AS STAGE_NAME
                FROM RPLCE_FLOW_EVENT_TRACKING r
                         LEFT JOIN USER_ACCOUNT ua ON r.CURRENT_OWNER_USER_ID = ua.ID
                         LEFT JOIN TAT_MASTER tm ON r.CURRENT_STAGE_ID = tm.ID
                WHERE r.REPLACEMENT_REQUEST_ID = ?
                ORDER BY r.START_AT DESC
                """;

        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, requestId);

            try (final ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    final Map<String, Object> comment = new HashMap<>();
                    comment.put("comments", rs.getString("COMMENTS"));
                    comment.put("userId", rs.getString("ID"));
                    comment.put("userName", rs.getString("USER_NAME"));
                    comment.put("createdAt", rs.getTimestamp("START_AT"));
                    comment.put("stageName", rs.getString("STAGE_NAME"));
                    comments.add(comment);
                }
            }
        }
        return comments;
    }

}
