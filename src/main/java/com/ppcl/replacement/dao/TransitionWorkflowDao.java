package com.ppcl.replacement.dao;

import com.ppcl.replacement.model.User;
import com.ppcl.replacement.util.DBConnectionPool;

import java.sql.*;
import java.util.Arrays;
import java.util.Map;

import static com.ppcl.replacement.constants.ReplacementRequestSQLConstant.GET_CURRENT_STAGE_USING_REQUEST_ID;

public class TransitionWorkflowDao extends BaseDAO {

    private final UserDAO userDAO = new UserDAO();
    private final ChatLogDao chatLogDao = new ChatLogDao();

    /**
     * Auto-transition to the next stage by discovering current stage and calculating next stage.
     * Delegates to transitionStage after gathering required parameters.
     */
    /**
     * Transitions the replacement request to the next stage in the workflow.
     *
     * @param con      the database connection
     * @param reqId    the ID of the replacement request
     * @param comments transition comments
     * @throws Exception if a database error occurs
     */
    public void transitionFlow(final Connection con, final int reqId, final String comments) throws Exception {

        final String sqlGetCurrentFlow =
                "SELECT CURRENT_STAGE, CURRENT_OWNER_ID " +
                        "FROM REPLACEMENT_REQUEST " +
                        "WHERE ID = ? ";

        final String sqlNextStage =
                "SELECT next_id AS next_stage_id " +
                        "FROM ( " +
                        "  SELECT id, LEAD(id) OVER (ORDER BY id) AS next_id " +
                        "  FROM tat_master " +
                        ") " +
                        "WHERE id = ?";

        final Integer currentStageId;
        final Integer currentOwnerUserId;

        try (final PreparedStatement ps = con.prepareStatement(sqlGetCurrentFlow)) {
            ps.setInt(1, reqId);
            try (final ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    throw new IllegalStateException("No open flow row found for reqId=" + reqId);
                }
                currentStageId = rs.getInt("CURRENT_STAGE");
                currentOwnerUserId = rs.getInt("CURRENT_OWNER_ID");
            }
        }

        Integer nextStageId = null;
        try (final PreparedStatement ps = con.prepareStatement(sqlNextStage)) {
            ps.setInt(1, currentStageId);
            try (final ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    final int v = rs.getInt("NEXT_STAGE_ID");
                    nextStageId = rs.wasNull() ? null : v;
                }
            }
        }

        // Last stage → nothing more to route
        if (nextStageId == null) {
            return;
        }

        final User nextOwner = resolveNextOwner(con, nextStageId);

        System.out.println("reqId :" + reqId + "  nextStageId : " + nextStageId + " nextOwner.getId():{}  " + nextOwner.toString());
        transitionStage(con, reqId, nextStageId, nextOwner.getId(), comments);


        final String fromUser = userDAO.getUserByUserId(currentOwnerUserId).getName();
        final String toUser = nextOwner.getName();
        final String msg = "Request #" + reqId + " moved to your queue (Stage " + nextStageId + ").";


        new ChatLogDao().insertChatLog(con, fromUser, toUser, msg, 11, null);

    }


    public void rejectTransitionStage(final Connection con, final int reqId, final String comments) throws Exception {

        // Close current event tracking

        final String closeSql =
                "UPDATE RPLCE_FLOW_EVENT_TRACKING " +
                        "SET END_AT = SYSTIMESTAMP, COMMENTS = ? " +
                        "WHERE REPLACEMENT_REQUEST_ID = ? AND END_AT IS NULL";

        try (final PreparedStatement ps = con.prepareStatement(closeSql)) {
            ps.setString(1, "REJECTED: " + comments);
            ps.setInt(2, reqId);
            ps.executeUpdate();
        }

        // Update request status
        final String sql =
                "UPDATE REPLACEMENT_REQUEST " +
                        "SET STATUS = 'REJECTED', UPDATE_DATE_TIME = SYSTIMESTAMP " +
                        "WHERE ID = ?";

        try (final PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, reqId);
            ps.executeUpdate();
        }
    }


    /**
     * Stub: implement your routing rule.
     * Example: stage -> role -> pick user. Or use request data to decide.
     */
    private User resolveNextOwner(final Connection con, final int nextStageId) throws SQLException {
        return userDAO.getNextStageOwnerUsingStageID(con, nextStageId);
        //throw new UnsupportedOperationException("Implement resolveNextOwner(reqId, nextStageId)");
    }


    public void forwardTransitionWorkflow(final Connection con, final int reqId, final int fromUserId,
                                           final Integer toUserId, final String comments) throws Exception {

        // Update current owner
        final String sql =
                "UPDATE REPLACEMENT_REQUEST " +
                        "SET CURRENT_OWNER_ID = ?, UPDATE_DATE_TIME = SYSTIMESTAMP " +
                        "WHERE ID = ?";

        try (final PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, toUserId);
            ps.setInt(2, reqId);
            ps.executeUpdate();
        }
        Integer stageId  = getCurrentStage(con, reqId);
        transitionWorkflow(con, reqId, stageId, fromUserId, stageId, toUserId, comments, null);

        Map<Integer,String> userMap = userDAO.getUsernamesByIds(Arrays.asList(fromUserId, toUserId));

        chatLogDao.insertChatLog(con, userMap.get(fromUserId), userMap.get(toUserId), comments, 1, null);
    }

    private int closeOpenFlowEvent(final Connection con, final int reqId, final int stageId,
                                   final int ownerUserId, final String closeComments) throws SQLException {

        final String sql =
                "UPDATE RPLCE_FLOW_EVENT_TRACKING " +
                        "   SET END_AT = SYSTIMESTAMP, " +
                        "       COMMENTS = CASE WHEN ? IS NOT NULL THEN ? ELSE COMMENTS END " +
                        " WHERE REPLACEMENT_REQUEST_ID = ? " +
                        "   AND CURRENT_STAGE_ID = ? " +
                        "   AND CURRENT_OWNER_USER_ID = ? " +
                        "   AND END_AT IS NULL";

        try (final PreparedStatement ps = con.prepareStatement(sql)) {
            // used twice in CASE
            if (closeComments != null) {
                ps.setString(1, closeComments);
                ps.setString(2, closeComments);
            } else {
                ps.setNull(1, java.sql.Types.VARCHAR);
                ps.setNull(2, java.sql.Types.VARCHAR);
            }

            ps.setInt(3, reqId);
            ps.setInt(4, stageId);
            ps.setInt(5, ownerUserId);

            return ps.executeUpdate(); // should be 1 if an open row existed
        }
    }

    /**
     * Insert into RPLCE_FLOW_EVENT_TRACKING
     */
    public void openFlowEventTracking(final Connection con, final int reqId, final int stageId,
                                      final int ownerUserId, final String comments) throws Exception {
        final String sql =
                "INSERT INTO RPLCE_FLOW_EVENT_TRACKING " +
                        "  (REPLACEMENT_REQUEST_ID, CURRENT_STAGE_ID, CURRENT_OWNER_USER_ID,COMMENTS) " +
                        "VALUES " +
                        "  (?, ?, ?, ?)";

        try (final PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, reqId);
            ps.setInt(2, stageId);
            ps.setInt(3, ownerUserId);

            if (comments != null) {
                ps.setString(4, comments);
            } else {
                ps.setNull(4, Types.VARCHAR);
            }
            ps.executeUpdate();
        }
    }

    public void transitionWorkflow(final Connection con,
                                   final int reqId,
                                   final int currentStageId,
                                   final int currentOwnerUserId,
                                   final int nextStageId,
                                   final int nextOwnerUserId,
                                   final String currentComments,
                                   final String nextComments) throws Exception {

        final boolean oldAutoCommit = con.getAutoCommit();
        con.setAutoCommit(false);

        try {
            final int updated = closeOpenFlowEvent(con, reqId, currentStageId, currentOwnerUserId, currentComments);

            if (updated == 0) {
                throw new IllegalStateException(
                        "No open tracking row found to close for reqId=" + reqId +
                                ", stageId=" + currentStageId +
                                ", ownerUserId=" + currentOwnerUserId
                );
            }

            openFlowEventTracking(con, reqId, nextStageId, nextOwnerUserId, nextComments);

            con.commit();
        } catch (final Exception e) {
            con.rollback();
            throw e;
        } finally {
            con.setAutoCommit(oldAutoCommit);
        }
    }

    /**
     * Transition request from one stage to the next.
     * Opens its own connection and manages the transaction.
     *
     * @param reqId            the ID of the replacement request
     * @param toStage          the destination stage ID
     * @param nextStageOwnerId the ID of the new owner
     * @param comments         transition comments
     * @throws Exception if a database error occurs
     */
    public void transitionStage(final int reqId, final int toStage,
                                final int nextStageOwnerId, final String comments) throws Exception {
        try (final Connection con = getConnection()) {
            con.setAutoCommit(false);
            try {
                transitionStage(con, reqId, toStage, nextStageOwnerId, comments);
                con.commit();
            } catch (final Exception e) {
                con.rollback();
                throw e;
            }
        }
    }

    /**
     * Core transition logic - closes current flow, updates request, and opens new flow.
     * Uses provided connection (caller manages transaction).
     *
     * @param con              the database connection
     * @param reqId            the ID of the replacement request
     * @param toStage          the destination stage ID
     * @param nextStageOwnerId the ID of the new owner
     * @param comments         transition comments
     * @throws Exception if a database error occurs
     */
    public void transitionStage(final Connection con, final int reqId, final int toStage,
                                final int nextStageOwnerId, final String comments) throws Exception {

        // Close current flow event
        final String closeSql =
                "UPDATE RPLCE_FLOW_EVENT_TRACKING " +
                        "SET END_AT = SYSTIMESTAMP, COMMENTS = ? " +
                        "WHERE REPLACEMENT_REQUEST_ID = ? AND END_AT IS NULL";

        try (final PreparedStatement ps = con.prepareStatement(closeSql)) {
            ps.setString(1, comments);
            ps.setInt(2, reqId);
            ps.executeUpdate();
        }

        // Update request to next stage
        final String updateSql =
                "UPDATE REPLACEMENT_REQUEST " +
                        "SET CURRENT_STAGE = ?, CURRENT_OWNER_ID = ?, IS_EDITABLE = 0, " +
                        "    UPDATE_DATE_TIME = SYSTIMESTAMP " +
                        "WHERE ID = ?";

        try (final PreparedStatement ps = con.prepareStatement(updateSql)) {
            ps.setInt(1, toStage);
            ps.setInt(2, nextStageOwnerId);
            ps.setInt(3, reqId);
            ps.executeUpdate();
        }

        // Create new event tracking
        final String insertSql =
                "INSERT INTO RPLCE_FLOW_EVENT_TRACKING " +
                        "(REPLACEMENT_REQUEST_ID, CURRENT_STAGE_ID, CURRENT_OWNER_USER_ID, START_AT) " +
                        "VALUES (?, ?, ?, SYSTIMESTAMP)";

        try (final PreparedStatement ps = con.prepareStatement(insertSql)) {
            ps.setInt(1, reqId);
            ps.setInt(2, toStage);
            ps.setInt(3, nextStageOwnerId);
            ps.executeUpdate();
        }
    }


    /**
     * Cancels a replacement request and closes the open flow event.
     *
     * @param con      the database connection
     * @param reqId    the ID of the replacement request
     * @param userId   the ID of the user performing the cancellation
     * @param comments cancellation comments
     * @throws Exception if a database error occurs
     */
    public void transitionCancelled(final Connection con, final int reqId, final int userId, final String comments) throws Exception {

        // Close current flow event
        final String closeSql =
                "UPDATE RPLCE_FLOW_EVENT_TRACKING " +
                        "SET END_AT = SYSTIMESTAMP, COMMENTS = ? " +
                        "WHERE REPLACEMENT_REQUEST_ID = ? AND END_AT IS NULL";

        try (final PreparedStatement ps = con.prepareStatement(closeSql)) {
            ps.setString(1, comments);
            ps.setInt(2, reqId);
            ps.executeUpdate();
        }

        // Update REPLACEMENT_REQUEST status to CANCELLED
        final String updateSql = "UPDATE REPLACEMENT_REQUEST SET STATUS = 'CANCELLED', " +
                "IS_EDITABLE = 0, UPDATE_DATE_TIME = SYSTIMESTAMP WHERE ID = ?";
        try (final PreparedStatement ps = con.prepareStatement(updateSql)) {
            ps.setInt(1, reqId);
            ps.executeUpdate();
        }

        final int currentStage = getCurrentStage(con, reqId);
        // Create new event tracking
        final String insertSql =
                "INSERT INTO RPLCE_FLOW_EVENT_TRACKING " +
                        "(REPLACEMENT_REQUEST_ID, CURRENT_STAGE_ID, CURRENT_OWNER_USER_ID, START_AT,END_AT, COMMENTS) " +
                        "VALUES (?, ?, ?, SYSTIMESTAMP,SYSTIMESTAMP, ? )";

        try (final PreparedStatement ps = con.prepareStatement(insertSql)) {
            ps.setInt(1, reqId);
            ps.setInt(2, currentStage);
            ps.setInt(3, userId);
            ps.setString(4, comments);
            ps.executeUpdate();
        }
    }


    /**
     * Gets the current stage ID of a replacement request.
     *
     * @param con   the database connection
     * @param reqId the ID of the replacement request
     * @return the stage ID, or {@code null} if not found
     * @throws Exception if a database error occurs
     */
    public Integer getCurrentStage(final Connection con, final int reqId) throws Exception {
        try (final PreparedStatement ps =
                     con.prepareStatement(GET_CURRENT_STAGE_USING_REQUEST_ID)) {

            ps.setInt(1, reqId);
            try (final ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("CURRENT_STAGE");
                }
            }
        }
        return null;
    }

}
