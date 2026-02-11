package com.ppcl.replacement.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class ChatLogDao {

    /**
     * Inserts a record into the CHAT_LOG table.
     *
     * @param con      the database connection
     * @param fromUser the sender's name or user ID
     * @param toUser   the recipient's name or user ID
     * @param message  the chat message
     * @param msgType  the type of message
     * @param replyId  the ID of the message being replied to, or {@code null}
     * @throws SQLException if a database access error occurs
     */
    public void insertChatLog(final Connection con,
                              final String fromUser,
                              final String toUser,
                              final String message,
                              final int msgType,
                              final Integer replyId) throws SQLException {

        final String sql =
                "INSERT INTO CHAT_LOG " +
                        "  (ID, FROM_USER, TO_USER, MSG_DATE, MSG_TIME, MESSAGE, MSG_TYPE, REPLY_ID) " +
                        "VALUES " +
                        "  (CHAT_LOG_SEQ.NEXTVAL, ?, ?, " +
                        "   TO_CHAR(SYSTIMESTAMP, 'DD-MM-YYYY'), " +
                        "   TO_CHAR(SYSTIMESTAMP, 'FMHH:MI AM', 'NLS_DATE_LANGUAGE=ENGLISH'), " +
                        "   ?, ?, ?)";

        try (final PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, fromUser);
            ps.setString(2, toUser);
            ps.setString(3, message);
            ps.setInt(4, msgType);

            if (replyId != null) ps.setInt(5, replyId);
            else ps.setNull(5, java.sql.Types.INTEGER);

            ps.executeUpdate();
        }
    }


}
