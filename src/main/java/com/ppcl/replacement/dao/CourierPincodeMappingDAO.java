package com.ppcl.replacement.dao;

import com.ppcl.replacement.model.Courier;
import com.ppcl.replacement.model.CourierPincodeMapping;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO for Courier Pincode Mapping operations
 */
public class CourierPincodeMappingDAO extends BaseDAO {

    // Status constants
    public static final int STATUS_ACTIVE = 1;
    public static final int STATUS_INACTIVE = 0;

    // Base columns for SELECT queries
    private static final String MAPPING_COLUMNS =
            "ID, COURIER_ID, COURIER_NAME, PINCODE, CITY, STATE, REGION, STATUS, " +
                    "CREATION_DATE_TIME, UPDATE_DATE_TIME, CREATED_BY, MODIFIED_BY";

    /**
     * Get all active couriers for dropdown
     */
    public List<Courier> getAllActiveCouriers() throws SQLException {
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        List<Courier> couriers = new ArrayList<>();

        try {
            conn = getConnection();
            String sql = "SELECT ID, NAME FROM COURIER_MASTER ORDER BY NAME";
            ps = conn.prepareStatement(sql);
            rs = ps.executeQuery();

            while (rs.next()) {
                couriers.add(mapResultSetToCourier(rs));
            }
        } finally {
            closeResources(conn, ps, rs);
        }
        return couriers;
    }

    /**
     * Get courier by ID
     */
    public Courier getCourierById(int courierId) throws SQLException {
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            conn = getConnection();
            String sql = "SELECT ID, NAME FROM COURIER_MASTER WHERE ID = ?";
            ps = conn.prepareStatement(sql);
            ps.setInt(1, courierId);
            rs = ps.executeQuery();

            if (rs.next()) {
                return mapResultSetToCourier(rs);
            }
        } finally {
            closeResources(conn, ps, rs);
        }
        return null;
    }

    /**
     * Check if an active mapping exists for given pincode
     */
    public CourierPincodeMapping getActiveMappingByPincode(String pincode) throws SQLException {
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            conn = getConnection();
            String sql = "SELECT " + MAPPING_COLUMNS +
                    " FROM COURIER_PINCODE_MAPPING WHERE PINCODE = ? AND STATUS = ?";
            ps = conn.prepareStatement(sql);
            ps.setInt(1, Integer.parseInt(pincode));
            ps.setInt(2, STATUS_ACTIVE);
            rs = ps.executeQuery();

            if (rs.next()) {
                return mapResultSetToMapping(rs);
            }
        } finally {
            closeResources(conn, ps, rs);
        }
        return null;
    }

    /**
     * Check if any mapping exists (active or inactive) for given pincode
     */
    public boolean hasAnyMappingForPincode(String pincode) throws SQLException {
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            conn = getConnection();
            String sql = "SELECT COUNT(*) FROM COURIER_PINCODE_MAPPING WHERE PINCODE = ?";
            ps = conn.prepareStatement(sql);
            ps.setInt(1, Integer.parseInt(pincode));
            rs = ps.executeQuery();

            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        } finally {
            closeResources(conn, ps, rs);
        }
        return false;
    }

    /**
     * Insert a new mapping
     */
    public int insertMapping(CourierPincodeMapping mapping) throws SQLException {
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            conn = getConnection();
            String sql = "INSERT INTO COURIER_PINCODE_MAPPING (COURIER_ID, COURIER_NAME, PINCODE, " +
                    "CITY, STATE, REGION, STATUS, CREATION_DATE_TIME, UPDATE_DATE_TIME, CREATED_BY) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, SYSTIMESTAMP, SYSTIMESTAMP, ?)";
            ps = conn.prepareStatement(sql, new String[]{"ID"});
            ps.setInt(1, mapping.getCourierId());
            ps.setString(2, mapping.getCourierName());
            ps.setInt(3, mapping.getPincode());
            ps.setString(4, mapping.getCity());
            ps.setString(5, mapping.getState());
            ps.setString(6, mapping.getRegion());
            ps.setInt(7, STATUS_ACTIVE);
            ps.setString(8, mapping.getCreatedBy());

            if (ps.executeUpdate() > 0) {
                rs = ps.getGeneratedKeys();
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        } finally {
            closeResources(conn, ps, rs);
        }
        return -1;
    }

    /**
     * Update mapping status (for soft delete / toggle)
     */
    public boolean updateMappingStatus(int id, String newStatus, String modifiedBy) throws SQLException {
        Connection conn = null;
        PreparedStatement ps = null;

        try {
            conn = getConnection();
            int statusCode = "ACTIVE".equalsIgnoreCase(newStatus) ? STATUS_ACTIVE : STATUS_INACTIVE;
            String sql = "UPDATE COURIER_PINCODE_MAPPING SET STATUS = ?, UPDATE_DATE_TIME = SYSTIMESTAMP, " +
                    "MODIFIED_BY = ? WHERE ID = ?";
            ps = conn.prepareStatement(sql);
            ps.setInt(1, statusCode);
            ps.setString(2, modifiedBy);
            ps.setInt(3, id);
            return ps.executeUpdate() > 0;
        } finally {
            closeResources(conn, ps, null);
        }
    }

    /**
     * Deactivate existing active mapping for a pincode (used in edit flow)
     */
    public boolean deactivateMappingByPincode(String pincode, String modifiedBy) throws SQLException {
        Connection conn = null;
        PreparedStatement ps = null;

        try {
            conn = getConnection();
            String sql = "UPDATE COURIER_PINCODE_MAPPING SET STATUS = ?, UPDATE_DATE_TIME = SYSTIMESTAMP, " +
                    "MODIFIED_BY = ? WHERE PINCODE = ? AND STATUS = ?";
            ps = conn.prepareStatement(sql);
            ps.setInt(1, STATUS_INACTIVE);
            ps.setString(2, modifiedBy);
            ps.setInt(3, Integer.parseInt(pincode));
            ps.setInt(4, STATUS_ACTIVE);
            return ps.executeUpdate() > 0;
        } finally {
            closeResources(conn, ps, null);
        }
    }

    /**
     * Update city/state/region for an existing mapping
     */
    public boolean updateLocationDetails(int id, String city, String state, String region) throws SQLException {
        Connection conn = null;
        PreparedStatement ps = null;

        try {
            conn = getConnection();
            String sql = "UPDATE COURIER_PINCODE_MAPPING SET CITY = ?, STATE = ?, REGION = ?, " +
                    "UPDATE_DATE_TIME = SYSTIMESTAMP WHERE ID = ?";
            ps = conn.prepareStatement(sql);
            ps.setString(1, city);
            ps.setString(2, state);
            ps.setString(3, region);
            ps.setInt(4, id);
            return ps.executeUpdate() > 0;
        } finally {
            closeResources(conn, ps, null);
        }
    }

    /**
     * Get mapping by ID
     */
    public CourierPincodeMapping getMappingById(int id) throws SQLException {
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            conn = getConnection();
            String sql = "SELECT " + MAPPING_COLUMNS + " FROM COURIER_PINCODE_MAPPING WHERE ID = ?";
            ps = conn.prepareStatement(sql);
            ps.setInt(1, id);
            rs = ps.executeQuery();

            if (rs.next()) {
                return mapResultSetToMapping(rs);
            }
        } finally {
            closeResources(conn, ps, rs);
        }
        return null;
    }

    /**
     * Get all active mappings with optional filters and pagination
     */
    public List<CourierPincodeMapping> getMappings(String courierName, String pincode,
                                                   int page, int pageSize) throws SQLException {
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        List<CourierPincodeMapping> mappings = new ArrayList<>();

        try {
            conn = getConnection();
            List<Object> params = new ArrayList<>();
            String whereClause = buildFilterWhereClause(courierName, pincode, params);

            StringBuilder sql = new StringBuilder();
            sql.append("SELECT * FROM (");
            sql.append("  SELECT m.*, ROWNUM AS rn FROM (");
            sql.append("    SELECT ").append(MAPPING_COLUMNS);
            sql.append("    FROM COURIER_PINCODE_MAPPING WHERE STATUS = ? ").append(whereClause);
            sql.append("    ORDER BY CREATION_DATE_TIME DESC");
            sql.append("  ) m WHERE ROWNUM <= ?");
            sql.append(") WHERE rn > ?");

            // Add status as first param, then pagination params at end
            params.add(0, STATUS_ACTIVE);
            params.add(page * pageSize);      // endRow
            params.add((page - 1) * pageSize); // startRow

            ps = conn.prepareStatement(sql.toString());
            setParameters(ps, params);

            rs = ps.executeQuery();
            while (rs.next()) {
                mappings.add(mapResultSetToMapping(rs));
            }
        } finally {
            closeResources(conn, ps, rs);
        }
        return mappings;
    }

    /**
     * Count total mappings with filters (for pagination)
     */
    public int countMappings(String courierName, String pincode) throws SQLException {
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            conn = getConnection();
            List<Object> params = new ArrayList<>();
            params.add(STATUS_ACTIVE);
            String whereClause = buildFilterWhereClause(courierName, pincode, params);

            String sql = "SELECT COUNT(*) FROM COURIER_PINCODE_MAPPING WHERE STATUS = ? " + whereClause;

            ps = conn.prepareStatement(sql);
            setParameters(ps, params);

            rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getInt(1);
            }
        } finally {
            closeResources(conn, ps, rs);
        }
        return 0;
    }

    /**
     * Get all active mappings for export (no pagination)
     */
    public List<CourierPincodeMapping> getAllMappingsForExport(String courierName, String pincode)
            throws SQLException {
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        List<CourierPincodeMapping> mappings = new ArrayList<>();

        try {
            conn = getConnection();
            List<Object> params = new ArrayList<>();
            params.add(STATUS_ACTIVE);
            String whereClause = buildFilterWhereClause(courierName, pincode, params);

            String sql = "SELECT " + MAPPING_COLUMNS +
                    " FROM COURIER_PINCODE_MAPPING WHERE STATUS = ? " + whereClause +
                    " ORDER BY COURIER_NAME, PINCODE";

            ps = conn.prepareStatement(sql);
            setParameters(ps, params);

            rs = ps.executeQuery();
            while (rs.next()) {
                mappings.add(mapResultSetToMapping(rs));
            }
        } finally {
            closeResources(conn, ps, rs);
        }
        return mappings;
    }

    /**
     * Get CONTACT_PERSON from COURIER_MASTER by courier ID.
     *
     * @param courierId the COURIER_MASTER.ID
     * @return the contact person name, or null if not found
     */
    public String getCourierContactPerson(final int courierId) throws SQLException {
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            conn = getConnection();
            final String sql = "SELECT CONTACT_PERSON FROM COURIER_MASTER WHERE ID = ?";
            ps = conn.prepareStatement(sql);
            ps.setInt(1, courierId);
            rs = ps.executeQuery();

            if (rs.next()) {
                return rs.getString("CONTACT_PERSON");
            }
        } finally {
            closeResources(conn, ps, rs);
        }
        return null;
    }

    // ==================== Helper Methods ====================

    /**
     * Build WHERE clause for courierName and pincode filters
     */
    private String buildFilterWhereClause(String courierName, String pincode, List<Object> params) {
        StringBuilder where = new StringBuilder();

        if (courierName != null && !courierName.trim().isEmpty()) {
            where.append("AND UPPER(COURIER_NAME) LIKE UPPER(?) ");
            params.add("%" + courierName.trim() + "%");
        }
        if (pincode != null && !pincode.trim().isEmpty()) {
            where.append("AND TO_CHAR(PINCODE) LIKE ? ");
            params.add("%" + pincode.trim() + "%");
        }

        return where.toString();
    }

    /**
     * Set parameters on PreparedStatement
     */
    private void setParameters(PreparedStatement ps, List<Object> params) throws SQLException {
        for (int i = 0; i < params.size(); i++) {
            Object param = params.get(i);
            if (param instanceof String) {
                ps.setString(i + 1, (String) param);
            } else if (param instanceof Integer) {
                ps.setInt(i + 1, (Integer) param);
            }
        }
    }

    /**
     * Map ResultSet to CourierPincodeMapping
     */
    private CourierPincodeMapping mapResultSetToMapping(ResultSet rs) throws SQLException {
        CourierPincodeMapping m = new CourierPincodeMapping();
        m.setId(rs.getInt("ID"));
        m.setCourierId(rs.getInt("COURIER_ID"));
        m.setCourierName(rs.getString("COURIER_NAME"));
        m.setPincode(rs.getInt("PINCODE"));
        m.setCity(rs.getString("CITY"));
        m.setState(rs.getString("STATE"));
        m.setRegion(rs.getString("REGION"));
        m.setStatusCode(rs.getInt("STATUS"));
        m.setCreationDateTime(rs.getTimestamp("CREATION_DATE_TIME"));
        m.setUpdateDateTime(rs.getTimestamp("UPDATE_DATE_TIME"));
        m.setCreatedBy(rs.getString("CREATED_BY"));
        m.setModifiedBy(rs.getString("MODIFIED_BY"));
        return m;
    }

    /**
     * Map ResultSet to Courier
     */
    private Courier mapResultSetToCourier(ResultSet rs) throws SQLException {
        Courier c = new Courier();
        c.setId(rs.getInt("ID"));
        c.setName(rs.getString("NAME"));
        return c;
    }
}
