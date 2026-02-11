package com.ppcl.replacement.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class TatDao extends BaseDAO {

    /**
     * Gets the ID of a stage from its string code.
     *
     * @param con       the database connection
     * @param stageCode the code of the stage (e.g., STG1_AM_REVIEW)
     * @return the stage ID, or 1 as a default fallback
     * @throws Exception if a database error occurs
     */
    public int getStageIdByCode(final Connection con, final String stageCode) throws Exception {
        final String sql = "SELECT ID FROM TAT_MASTER WHERE STAGE_CODE = ? AND STATUS = 1";
        try (final PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, stageCode);
            try (final ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("ID");
                }
            }
        }
        return 1;
    }

}
