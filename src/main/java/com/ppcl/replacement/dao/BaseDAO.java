package com.ppcl.replacement.dao;

import com.ppcl.replacement.util.DBConnectionPool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;

public abstract class BaseDAO {
    protected final Logger logger = LoggerFactory.getLogger(getClass());

    /**
     * Gets a database connection from the connection pool.
     *
     * @return A {@link Connection} object
     * @throws SQLException if a database access error occurs
     */
    protected Connection getConnection() throws SQLException {
        return DBConnectionPool.getConnection();
    }

    /**
     * Safely closes the provided database resources (ResultSet, Statement, Connection).
     *
     * @param conn the {@link Connection} to close
     * @param ps   the {@link Statement} or {@link PreparedStatement} to close
     * @param rs   the {@link ResultSet} to close
     */
    protected void closeResources(final Connection conn, final Statement ps, final ResultSet rs) {
        if (rs != null) {
            try {
                rs.close();
            } catch (final SQLException e) {
                logger.error("Error closing ResultSet", e);
            }
        }
        if (ps != null) {
            try {
                ps.close();
            } catch (final SQLException e) {
                logger.error("Error closing Statement", e);
            }
        }
        if (conn != null) {
            try {
                conn.close();
            } catch (final SQLException e) {
                logger.error("Error closing Connection", e);
            }
        }
    }
}
