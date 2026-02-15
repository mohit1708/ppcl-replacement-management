package com.ppcl.replacement.dao;

import com.ppcl.replacement.model.User;
import com.ppcl.replacement.util.DBConnectionPool;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;

public class UserDAO extends BaseDAO {

    // Designation IDs for access control
    public static final int DESIG_CRO = 2;
    public static final int DESIG_AM = 4;
    public static final int DESIG_AM_MANAGER = 261;

    // Predefined access levels for common use cases
    public static final Set<Integer> ACCESS_CREATE_REQUEST = Set.of(DESIG_CRO, DESIG_AM, DESIG_AM_MANAGER);
    public static final Set<Integer> ACCESS_AM_MODULE = Set.of(DESIG_CRO, DESIG_AM);
    public static final Set<Integer> ACCESS_AM_MANAGER_MODULE = Set.of(DESIG_AM_MANAGER);

    private static final String SQL_CHECK_USER_DESIGNATION = """
            SELECT 1 FROM USER_ACCOUNT ua
            JOIN EMP e ON ua.EMP_ID = e.ID
            JOIN DESIG d ON e.DESIGNATION = d.ID
            WHERE ua.ID = ? AND e.DESIGNATION IN (%s)
            """;

    private static final String SQL_GET_USER_DESIGNATION_ID = """
            SELECT e.DESIGNATION
            FROM USER_ACCOUNT ua
            JOIN EMP e ON ua.EMP_ID = e.ID
            WHERE ua.ID = ?
            """;

    private static final String SQL_GET_EFFECTIVE_AM_USER_ID = """
            SELECT
              CASE
                WHEN rr.source = 'DIRECT' THEN rr.requester_user_id
                ELSE ca.user_id
              END AS id
            FROM replacement_request rr
            LEFT JOIN client_access ca
              ON rr.source <> 'DIRECT'
             AND ca.client_id = rr.client_dot_id_signing
            WHERE rr.id = ?
            """;

    private final String GET_CRO_MANAGER_SQL = """
            SELECT mgr_ua.*
            FROM (
                SELECT
                    e.ID AS MANAGER_EMP_ID,
                    LEVEL AS HIER_LEVEL
                FROM EMP e
                JOIN DESIG d
                    ON d.ID = e.DESIGNATION
                START WITH e.ID = (
                    SELECT ua.EMP_ID
                    FROM USER_ACCOUNT ua
                    WHERE ua.id = ?
                )
                CONNECT BY NOCYCLE PRIOR e.REPORTING_TO = e.ID
            ) mgr
            JOIN USER_ACCOUNT mgr_ua
                ON mgr_ua.EMP_ID = mgr.MANAGER_EMP_ID
            WHERE mgr.MANAGER_EMP_ID IS NOT NULL
              AND EXISTS (
                  SELECT 1
                  FROM DESIG d
                  JOIN EMP e ON e.DESIGNATION = d.ID
                  WHERE e.ID = mgr.MANAGER_EMP_ID
                    AND d.NAME = 'MANAGER CRO'
              )
            FETCH FIRST 1 ROW ONLY
            """;

    private final String GET_CRO_MANAGER_USING_REQUEST_SQL = """
            WITH effective_am AS (
              SELECT
                CASE
                  WHEN rr.source = 'DIRECT' THEN rr.requester_user_id
                  ELSE ca.user_id
                END AS am_user_id
              FROM replacement_request rr
              LEFT JOIN client_access ca
                ON rr.source <> 'DIRECT'
               AND ca.client_id = rr.client_dot_id_signing
              WHERE rr.id = ?
              FETCH FIRST 1 ROW ONLY
            ),
            mgr_chain AS (
              SELECT
                e.ID AS manager_emp_id,
                LEVEL AS hier_level
              FROM emp e
              START WITH e.ID = (
                SELECT ua.emp_id
                FROM user_account ua
                JOIN effective_am ea
                  ON ua.id = ea.am_user_id
              )
              CONNECT BY NOCYCLE PRIOR e.reporting_to = e.id
            )
            SELECT mgr_ua.id AS cro_manager_user_account_id
            FROM mgr_chain mgr
            JOIN user_account mgr_ua
              ON mgr_ua.emp_id = mgr.manager_emp_id
            JOIN emp e2
              ON e2.id = mgr.manager_emp_id
            JOIN desig d2
              ON d2.id = e2.designation
            WHERE d2.name = 'MANAGER CRO'
            ORDER BY mgr.hier_level
            FETCH FIRST 1 ROW ONLY""";

    private final String GET_USER_USING_USERID_SQL = """
            SELECT ua.ID, ua.EMP_ID, ua.USER_ID, ua.USER_ID AS NAME, ua.MOBILE, ua.EMAIL,
                                    ua.BR_ID, ua.DEPT_ID, ua.DEPT ,d.id AS ROLE_ID,d.name AS ROLE
                                    FROM USER_ACCOUNT ua , emp e , desig d
                                    WHERE ua.id = ? and ua.emp_id=e.id and e.designation = d.id""";

    private final String GET_USER_USING_USERNAME_SQL = """
            SELECT ua.ID, ua.EMP_ID, ua.USER_ID, ua.USER_ID AS NAME, ua.MOBILE, ua.EMAIL,
                                    ua.BR_ID, ua.DEPT_ID, ua.DEPT ,d.id AS ROLE_ID,d.name AS ROLE
                                    FROM USER_ACCOUNT ua , emp e , desig d
                                    WHERE ua.user_id = ? and ua.emp_id=e.id and e.designation = d.id""";

    private final String GET_AM_MANAGER_USING_REQ_ID = """
            
            SELECT mgr_ua.id AS manager_user_account_id
            FROM (
                     SELECT e.id AS emp_id
                     FROM replacement_request rr
                              JOIN user_account ua
                                   ON ua.id = CASE
                                                  WHEN rr.source = 'DIRECT'
                                                      THEN rr.requester_user_id
                                                  ELSE (
                                                      SELECT ca.user_id
                                                      FROM client_access ca
                                                      WHERE ca.client_id = rr.client_dot_id_signing
                                                          FETCH FIRST 1 ROW ONLY
                                                  )
                                       END
                              JOIN emp e
                                   ON e.id = ua.emp_id
                     WHERE rr.id = ?
                 ) start_emp
                     JOIN emp mgr
                          ON 1 = 1
                     JOIN user_account mgr_ua
                          ON mgr_ua.emp_id = mgr.id
            WHERE mgr.designation = 87
            START WITH mgr.id = start_emp.emp_id
            CONNECT BY NOCYCLE PRIOR mgr.reporting_to = mgr.id
                FETCH FIRST 1 ROW ONLY
            """;
//    private final String GET_AM_MANAGER_USING_REQ_ID= """
//            SELECT mgr_ua.id AS MANAGER_USER_ACCOUNT_ID
//            FROM replacement_request rr
//            JOIN user_account ua
//              ON ua.id = CASE
//                           WHEN rr.source = 'DIRECT'
//                             THEN rr.requester_user_id
//                           ELSE (
//                             SELECT ca.user_id
//                             FROM client_access ca
//                             WHERE ca.client_id = rr.client_dot_id_signing
//                             FETCH FIRST 1 ROW ONLY
//                           )
//                         END
//            JOIN emp e
//              ON e.id = ua.emp_id
//            JOIN user_account mgr_ua
//              ON mgr_ua.emp_id = e.reporting_to
//            WHERE rr.id = ?
//            """;

    /**
     * Gets the sign-in user (Account Manager) for a specific replacement request.
     *
     * @param replacementRequestId the ID of the replacement request
     * @return the {@link User} object, or {@code null} if not found
     * @throws SQLException if a database access error occurs
     */
    public User getSignInUserByRequestId(final Integer replacementRequestId) throws SQLException {

        final String sql =
                """
                        SELECT ca.USER_ID 
                        FROM REPLACEMENT_REQUEST rr 
                        JOIN CLIENT_ACCESS ca 
                          ON rr.CLIENT_DOT_ID_SIGNING = ca.CLIENT_ID 
                        WHERE rr.ID = ?""";

        try (final Connection conn = getConnection();
             final PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, replacementRequestId);

            try (final ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    final int userId = rs.getInt("USER_ID");
                    return getUserByUserId(userId); // reuse existing DAO
                }
            }
        }

        return null;
    }


    /**
     * Gets a user by their user account ID.
     *
     * @param userId the user account ID
     * @return the {@link User} object, or {@code null} if not found
     * @throws SQLException if a database access error occurs
     */
    public User getUserByUserId(final Integer userId) throws SQLException {
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        User user = null;

        try {
            conn = getConnection();

            ps = conn.prepareStatement(GET_USER_USING_USERID_SQL);
            ps.setInt(1, userId);
            rs = ps.executeQuery();

            if (rs.next()) {
                user = new User();
                user.setId(rs.getInt("ID"));
                user.setEmpId(rs.getInt("EMP_ID"));
                user.setUserId(rs.getString("USER_ID"));
                user.setName(rs.getString("NAME"));
                user.setMobile(rs.getString("MOBILE"));
                user.setEmail(rs.getString("EMAIL"));
                user.setBrId(rs.getInt("BR_ID"));
                user.setDeptId(rs.getInt("DEPT_ID"));
                user.setDept(rs.getString("DEPT"));
                user.setRoleId(rs.getInt("ROLE_ID"));
                user.setRole(rs.getString("ROLE"));
            }
        } finally {
            closeResources(conn, ps, rs);
        }

        return user;
    }

    /**
     * Gets a user by their user account ID.
     *
     * @param userId the user account ID
     * @return the {@link User} object, or {@code null} if not found
     * @throws SQLException if a database access error occurs
     */
    public User getUserByUserName(final String userName) throws SQLException {
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        User user = null;

        try {
            conn = getConnection();

            ps = conn.prepareStatement(GET_USER_USING_USERNAME_SQL);
            ps.setString(1, userName);
            rs = ps.executeQuery();

            if (rs.next()) {
                user = new User();
                user.setId(rs.getInt("ID"));
                user.setEmpId(rs.getInt("EMP_ID"));
                user.setUserId(rs.getString("USER_ID"));
                user.setName(rs.getString("NAME"));
                user.setMobile(rs.getString("MOBILE"));
                user.setEmail(rs.getString("EMAIL"));
                user.setBrId(rs.getInt("BR_ID"));
                user.setDeptId(rs.getInt("DEPT_ID"));
                user.setDept(rs.getString("DEPT"));
                user.setRoleId(rs.getInt("ROLE_ID"));
                user.setRole(rs.getString("ROLE"));
            }
        } finally {
            closeResources(conn, ps, rs);
        }

        return user;
    }

    /**
     * Gets the default owner for Service TL Review stage.
     *
     * @return a {@link User} who is a Service TL
     * @throws SQLException if a database access error occurs
     */
    public User getTLLeadList() throws SQLException {
        return getNextStageOwnerUsingStage("STG2_SERVICE_TL_REVIEW");
    }

    /**
     * Gets the default owner for AM Manager Review stage.
     *
     * @return a {@link User} who is an AM Manager
     * @throws SQLException if a database access error occurs
     */
    public User getAMManagerInfo() throws SQLException {
        return getNextStageOwnerUsingStage("STG3_AM_MANAGER_REVIEW");
    }

    /**
     * Get all TL Leads for dropdown selection.
     *
     * @return list of users who can be assigned as TL for replacement requests
     * @throws SQLException if a database access error occurs
     */
    public List<User> getAllTLLeads() throws SQLException {
        final List<User> tlLeads = new ArrayList<>();
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            conn = getConnection();
            final String sql = """
                    SELECT ua.ID, ua.EMP_ID, ua.USER_ID, ua.USER_ID AS NAME, ua.MOBILE, ua.EMAIL, 
                           ua.BR_ID, ua.DEPT_ID, ua.DEPT  
                    FROM USER_ACCOUNT ua, DESIG des, EMP e, TAT_MASTER tat
                    WHERE ua.EMP_ID = e.ID 
                      AND e.DESIGNATION = des.ID 
                      AND des.ID = tat.OWNER_ROLE 
                      AND tat.STAGE_CODE = 'STG2_SERVICE_TL_REVIEW'
                    ORDER BY ua.USER_ID
                    """;
            ps = conn.prepareStatement(sql);
            rs = ps.executeQuery();

            while (rs.next()) {
                final User user = new User();
                user.setId(rs.getInt("ID"));
                user.setEmpId(rs.getInt("EMP_ID"));
                user.setUserId(rs.getString("USER_ID"));
                user.setName(rs.getString("NAME"));
                user.setMobile(rs.getString("MOBILE"));
                user.setEmail(rs.getString("EMAIL"));
                user.setBrId(rs.getInt("BR_ID"));
                user.setDeptId(rs.getInt("DEPT_ID"));
                user.setDept(rs.getString("DEPT"));
                tlLeads.add(user);
            }
        } finally {
            closeResources(conn, ps, rs);
        }

        return tlLeads;
    }

    /**
     * Gets the default owner for a specific workflow stage.
     *
     * @param stageName the code of the workflow stage
     * @return the {@link User} assigned to that stage, or {@code null}
     * @throws SQLException if a database access error occurs
     */
    public User getNextStageOwnerUsingStage(final String stageName) throws SQLException {
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        User user = null;

        try {
            conn = getConnection();
            final String sql = """
                    select ua.ID, ua.EMP_ID, ua.USER_ID, ua.USER_ID AS NAME, ua.MOBILE, ua.EMAIL, 
                                        ua.BR_ID, ua.DEPT_ID, ua.DEPT  from user_Account ua, desig des, emp e ,tat_master tat
                                        where ua.emp_id = e.id and e.designation = des.id and des.id=tat.owner_role and tat.stage_code = ? """;
            ps = conn.prepareStatement(sql);
            ps.setString(1, stageName);
            rs = ps.executeQuery();

            if (rs.next()) {
                user = new User();
                user.setId(rs.getInt("ID"));
                user.setEmpId(rs.getInt("EMP_ID"));
                user.setUserId(rs.getString("USER_ID"));
                user.setName(rs.getString("NAME"));
                user.setMobile(rs.getString("MOBILE"));
                user.setEmail(rs.getString("EMAIL"));
                user.setBrId(rs.getInt("BR_ID"));
                user.setDeptId(rs.getInt("DEPT_ID"));
                user.setDept(rs.getString("DEPT"));
            }
        } finally {
            closeResources(conn, ps, rs);
        }

        return user;
    }

    public User getNextStageOwnerUsingStageID(final Connection con, final int stageId) throws SQLException {
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        User user = null;

        try {
            conn = (conn != null) ? conn : getConnection();
            final String sql = """
                    select ua.ID, ua.EMP_ID, ua.USER_ID, ua.USER_ID AS NAME, ua.MOBILE, ua.EMAIL,
                                        ua.BR_ID, ua.DEPT_ID, ua.DEPT  from user_Account ua, desig des, emp e ,tat_master tat
                                        where ua.emp_id = e.id and e.designation = des.id and des.id=tat.owner_role and tat.id = ? """;
            ps = conn.prepareStatement(sql);
            ps.setInt(1, stageId);
            rs = ps.executeQuery();

            if (rs.next()) {
                user = new User();
                user.setId(rs.getInt("ID"));
                user.setEmpId(rs.getInt("EMP_ID"));
                user.setUserId(rs.getString("USER_ID"));
                user.setName(rs.getString("NAME"));
                user.setMobile(rs.getString("MOBILE"));
                user.setEmail(rs.getString("EMAIL"));
                user.setBrId(rs.getInt("BR_ID"));
                user.setDeptId(rs.getInt("DEPT_ID"));
                user.setDept(rs.getString("DEPT"));
            }
        } finally {
            closeResources(conn, ps, rs);
        }

        return user;
    }

    public User getNextStageOwnerUsingDesig(final int desigId) throws SQLException {
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        User user = null;

        try {
            conn = getConnection();
            final String sql = """
                    select ua.ID, ua.EMP_ID, ua.USER_ID, ua.USER_ID AS NAME, ua.MOBILE, ua.EMAIL, 
                    ua.BR_ID, ua.DEPT_ID, ua.DEPT  from user_Account ua, desig des, emp e 
                    where ua.emp_id = e.id and e.designation = des.id and des.id=? """;
            ps = conn.prepareStatement(sql);
            ps.setInt(1, desigId);
            rs = ps.executeQuery();

            if (rs.next()) {
                user = new User();
                user.setId(rs.getInt("ID"));
                user.setEmpId(rs.getInt("EMP_ID"));
                user.setUserId(rs.getString("USER_ID"));
                user.setName(rs.getString("NAME"));
                user.setMobile(rs.getString("MOBILE"));
                user.setEmail(rs.getString("EMAIL"));
                user.setBrId(rs.getInt("BR_ID"));
                user.setDeptId(rs.getInt("DEPT_ID"));
                user.setDept(rs.getString("DEPT"));
            }
        } finally {
            closeResources(conn, ps, rs);
        }

        return user;
    }

    /**
     * Gets the role name of a user.
     *
     * @param userId the user account ID (string format)
     * @return the role name, or {@code null} if not found
     * @throws SQLException if a database access error occurs
     */
    public String getUserRole(final String userId) throws SQLException {
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        String role = "AM"; // Default

        try {
            conn = getConnection();
            // Get role from DEPT_ID which references DESIG table
            final String sql = "SELECT d.NAME AS DESIG_NAME FROM USER_ACCOUNT ua " +
                    "LEFT JOIN DESIG d ON ua.DEPT_ID = d.ID " +
                    "WHERE ua.USER_ID = ?";
            ps = conn.prepareStatement(sql);
            ps.setString(1, userId);
            rs = ps.executeQuery();

            if (rs.next()) {
                final String desigName = rs.getString("DESIG_NAME");
                if (desigName != null) {
                    if (desigName.contains("CRO")) role = "CRO";
                    else if (desigName.contains("SERVICE") || desigName.contains("SUPPORT")) role = "SERVICE_TL";
                    else if (desigName.contains("MANAGER") || desigName.contains("OPERATIONS")) role = "AM_MANAGER";
                    else if (desigName.contains("ACCOUNT")) role = "AM";
                }
            }
        } finally {
            closeResources(conn, ps, rs);
        }

        return role;
    }

    public User getCROManager(final Connection conn, final int userId) throws SQLException {


        try (final PreparedStatement ps = conn.prepareStatement(GET_CRO_MANAGER_SQL)) {

            ps.setInt(1, userId);   // USER_ACCOUNT.USER_ID (username)

            try (final ResultSet rs = ps.executeQuery()) {

                if (rs.next()) {
                    final User user = new User();
                    user.setId(rs.getInt("ID"));
                    user.setEmpId(rs.getInt("EMP_ID"));
                    user.setUserId(rs.getString("USER_ID"));
                    user.setName(rs.getString("NAME"));
                    user.setMobile(rs.getString("MOBILE"));
                    user.setEmail(rs.getString("EMAIL"));
                    user.setBrId(rs.getInt("BR_ID"));
                    user.setDeptId(rs.getInt("DEPT_ID"));
                    user.setDept(rs.getString("DEPT"));
                    user.setRole(rs.getString("ROLE"));
                    return user;
                }
            }
        }
        return null; // No Manager CRO found
    }

    public Integer getEffectiveCROIdByRequestId(final Connection conn, final Integer requestId) throws SQLException {
        try (final PreparedStatement ps = conn.prepareStatement(SQL_GET_EFFECTIVE_AM_USER_ID)) {
            ps.setInt(1, requestId);

            try (final ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return 0;
                }

                final Integer id = rs.getInt("id"); // could be null if data missing
                return id;
            }
        }
    }

    public Integer getCROManagerUsingRequest(final Connection conn, final Integer reqId) throws SQLException {
        try (final PreparedStatement ps = conn.prepareStatement(GET_CRO_MANAGER_USING_REQUEST_SQL)) {
            ps.setInt(1, reqId);

            try (final ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return 0;
                }
                final Integer id = rs.getInt("CRO_MANAGER_USER_ACCOUNT_ID"); // could be null if data missing
                return id;
            }
        }
    }


    /**
     * Gets the user account ID (integer) for a given string user ID.
     *
     * @param con    the database connection
     * @param userId the user ID (string login ID)
     * @return the integer ID from USER_ACCOUNT table, or 0 if not found
     * @throws Exception if a database error occurs
     */
    public int getUserAccountId(final Connection con, final String userId) throws Exception {
        final String sql = "SELECT ID FROM USER_ACCOUNT WHERE USER_ID = ?";
        try (final PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, userId);
            try (final ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("ID");
                }
            }
        }
        return 1;
    }

    public User getUserfromName(final String userId) throws Exception {
        try (final Connection con = getConnection()) {
            return getUserByUserId(getUserAccountId(con, userId));
        }
    }

    public int getEmpIdUsingUserId(final int userId) throws Exception {
        final String sql = "SELECT E.ID as ID FROM USER_ACCOUNT UA , EMP E WHERE UA.EMP_ID= E.ID AND UA.ID=?";
        try (final Connection con = DBConnectionPool.getConnection(); final PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (final ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("ID");
                }
            }
        }
        return 1;
    }

    /**
     * Get reporting hierarchy (managers) for a given user based on EMP.REPORTING_TO chain.
     * Returns list of managers up the hierarchy with their designation info.
     *
     * @param userAccountId - USER_ACCOUNT.ID of the current user
     * @return List of User objects representing the reporting hierarchy (immediate manager first)
     */
    public List<User> getReportingHierarchy(final int userAccountId) throws SQLException {
        final List<User> hierarchy = new ArrayList<>();

        System.out.println("userAccountId is "+userAccountId);
        final String sql = """
                SELECT
                    mgr_ua.ID,
                    mgr_ua.EMP_ID,
                    mgr_ua.USER_ID,
                    mgr_e.NAME AS NAME,
                    mgr_ua.MOBILE,
                    mgr_ua.EMAIL,
                    mgr_ua.BR_ID,
                    mgr_ua.DEPT_ID,
                    mgr_ua.DEPT,
                    mgr_d.ID AS ROLE_ID,
                    mgr_d.NAME AS ROLE,
                    mgr_d.HIERARCHY_LEVEL,
                    mgr.HIER_LEVEL
                FROM (
                         SELECT
                             e.ID AS MANAGER_EMP_ID,
                             LEVEL AS HIER_LEVEL
                         FROM EMP e
                         START WITH e.ID = (
                             SELECT ua.EMP_ID
                             FROM USER_ACCOUNT ua
                             WHERE ua.ID = ?
                         )
                         CONNECT BY NOCYCLE PRIOR e.REPORTING_TO = e.ID
                     ) mgr
                         JOIN EMP mgr_e ON mgr_e.ID = mgr.MANAGER_EMP_ID
                         JOIN USER_ACCOUNT mgr_ua ON mgr_ua.EMP_ID = mgr.MANAGER_EMP_ID
                         LEFT JOIN DESIG mgr_d ON mgr_d.ID = mgr_e.DESIGNATION
                WHERE mgr.HIER_LEVEL > 1
                ORDER BY mgr.HIER_LEVEL ASC
                """;

        try (final Connection conn = getConnection();
             final PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, userAccountId);

            try (final ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    final User user = new User();
                    user.setId(rs.getInt("ID"));
                    user.setEmpId(rs.getInt("EMP_ID"));
                    user.setUserId(rs.getString("USER_ID"));
                    user.setName(rs.getString("NAME"));
                    user.setMobile(rs.getString("MOBILE"));
                    user.setEmail(rs.getString("EMAIL"));
                    user.setBrId(rs.getInt("BR_ID"));
                    user.setDeptId(rs.getInt("DEPT_ID"));
                    user.setDept(rs.getString("DEPT"));
                    user.setRoleId(rs.getInt("ROLE_ID"));
                    user.setRole(rs.getString("ROLE"));
                    System.out.println("user si "+ user);
                    hierarchy.add(user);
                }
            }
        }
        System.out.println("hierarchy "+hierarchy.size());
        return hierarchy;
    }

    /**
     * Get reporting hierarchy for a user identified by USER_ACCOUNT.USER_ID (string).
     * Convenience method that converts string userId to int.
     *
     * @param userId - USER_ACCOUNT.USER_ID (string username)
     * @return List of User objects representing the reporting hierarchy
     */
    public List<User> getReportingHierarchyByUserId(final String userId) throws SQLException {
        if (userId == null || userId.trim().isEmpty()) {
            return new ArrayList<>();
        }

        // First get the USER_ACCOUNT.ID from USER_ID
        final String sql = "SELECT ID FROM USER_ACCOUNT WHERE USER_ID = ?";
        int userAccountId = 0;

        try (final Connection conn = getConnection();
             final PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, userId);
            try (final ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    userAccountId = rs.getInt("ID");
                }
            }
        }

        if (userAccountId == 0) {
            return new ArrayList<>();
        }

        return getReportingHierarchy(userAccountId);
    }

    /**
     * Get all users from the same department as the given user.
     *
     * @param userAccountId - USER_ACCOUNT.ID of the current user
     * @return List of User objects in the same department
     */
    public List<User> getUsersFromSameDept(final int userAccountId) throws SQLException {
        final List<User> users = new ArrayList<>();

        final String sql = """
                SELECT 
                    ua.ID, ua.EMP_ID, ua.USER_ID, e.NAME AS NAME, 
                    ua.MOBILE, ua.EMAIL, ua.BR_ID, ua.DEPT_ID, ua.DEPT,
                    d.ID AS ROLE_ID, d.NAME AS ROLE
                FROM USER_ACCOUNT ua
                JOIN EMP e ON ua.EMP_ID = e.ID
                LEFT JOIN DESIG d ON e.DESIGNATION = d.ID
                WHERE ua.DEPT_ID = (
                    SELECT DEPT_ID FROM USER_ACCOUNT WHERE ID = ?
                )
                AND ua.STATUS = 1
                ORDER BY e.NAME
                """;

        try (final Connection conn = getConnection();
             final PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, userAccountId);

            try (final ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    final User user = new User();
                    user.setId(rs.getInt("ID"));
                    user.setEmpId(rs.getInt("EMP_ID"));
                    user.setUserId(rs.getString("USER_ID"));
                    user.setName(rs.getString("NAME"));
                    user.setMobile(rs.getString("MOBILE"));
                    user.setEmail(rs.getString("EMAIL"));
                    user.setBrId(rs.getInt("BR_ID"));
                    user.setDeptId(rs.getInt("DEPT_ID"));
                    user.setDept(rs.getString("DEPT"));
                    user.setRoleId(rs.getInt("ROLE_ID"));
                    user.setRole(rs.getString("ROLE"));
                    users.add(user);
                }
            }
        }

        return users;
    }

    /**
     * Get all users from same department by DEPT_ID directly.
     *
     * @param deptId - DEPT.ID
     * @return List of User objects in the department
     */
    public List<User> getUsersByDeptId(final int deptId) throws SQLException {
        final List<User> users = new ArrayList<>();

        final String sql = """
                SELECT 
                    ua.ID, ua.EMP_ID, ua.USER_ID, e.NAME AS NAME, 
                    ua.MOBILE, ua.EMAIL, ua.BR_ID, ua.DEPT_ID, ua.DEPT,
                    d.ID AS ROLE_ID, d.NAME AS ROLE
                FROM USER_ACCOUNT ua
                JOIN EMP e ON ua.EMP_ID = e.ID
                LEFT JOIN DESIG d ON e.DESIGNATION = d.ID
                WHERE ua.DEPT_ID = ?
                AND ua.STATUS = 1
                ORDER BY e.NAME
                """;

        try (final Connection conn = getConnection();
             final PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, deptId);

            try (final ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    final User user = new User();
                    user.setId(rs.getInt("ID"));
                    user.setEmpId(rs.getInt("EMP_ID"));
                    user.setUserId(rs.getString("USER_ID"));
                    user.setName(rs.getString("NAME"));
                    user.setMobile(rs.getString("MOBILE"));
                    user.setEmail(rs.getString("EMAIL"));
                    user.setBrId(rs.getInt("BR_ID"));
                    user.setDeptId(rs.getInt("DEPT_ID"));
                    user.setDept(rs.getString("DEPT"));
                    user.setRoleId(rs.getInt("ROLE_ID"));
                    user.setRole(rs.getString("ROLE"));
                    users.add(user);
                }
            }
        }

        return users;
    }

    /**
     * Get manager and above hierarchy for a user using EMP.REPORTING_TO.
     * This returns only managers (excludes the user themselves).
     *
     * @param userAccountId - USER_ACCOUNT.ID
     * @return List of managers ordered by hierarchy level (immediate manager first)
     */
    public List<User> getManagersAbove(final int userAccountId) throws SQLException {
        return getReportingHierarchy(userAccountId);
    }

    /**
     * Get all users by stage name from TAT_MASTER.
     * Uses OWNER_ROLE (DESIG.ID) to find users with that designation.
     *
     * @param stageCode - TAT_MASTER.STAGE_CODE (e.g., 'STG2_SERVICE_TL_REVIEW')
     * @return List of users with the designation matching the stage's owner role
     */
    public List<User> getUsersByStageCode(final String stageCode) throws SQLException {
        final List<User> users = new ArrayList<>();

        final String sql = """
                SELECT 
                    ua.ID, ua.EMP_ID, ua.USER_ID, e.NAME AS NAME, 
                    ua.MOBILE, ua.EMAIL, ua.BR_ID, ua.DEPT_ID, ua.DEPT,
                    d.ID AS ROLE_ID, d.NAME AS ROLE
                FROM USER_ACCOUNT ua
                JOIN EMP e ON ua.EMP_ID = e.ID
                JOIN DESIG d ON e.DESIGNATION = d.ID
                WHERE d.ID = (
                    SELECT OWNER_ROLE FROM TAT_MASTER WHERE STAGE_CODE = ? AND STATUS = 1
                )
                AND ua.STATUS = 1
                ORDER BY e.NAME
                """;

        try (final Connection conn = getConnection();
             final PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, stageCode);

            try (final ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    users.add(mapUserFromResultSet(rs));
                }
            }
        }

        return users;
    }

    /**
     * Gets the AM Manager for a specific replacement request.
     *
     * @param conn  the database connection
     * @param reqId the ID of the replacement request
     * @return the user account ID of the AM Manager
     * @throws SQLException if a database access error occurs
     */
    public Integer getAMManagerUsingRequestId(final Connection conn, final Integer reqId) throws SQLException {
        try (final PreparedStatement ps = conn.prepareStatement(GET_AM_MANAGER_USING_REQ_ID)) {
            ps.setInt(1, reqId);

            try (final ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return 0;
                }

                final Integer id = rs.getInt("MANAGER_USER_ACCOUNT_ID"); // could be null if data missing
                return id;
            }
        }
    }

    /**
     * Get all users by stage ID from TAT_MASTER.
     * Uses OWNER_ROLE (DESIG.ID) to find users with that designation.
     *
     * @param stageId - TAT_MASTER.ID
     * @return List of users with the designation matching the stage's owner role
     */
    public List<User> getUsersByStageId(final int stageId) throws SQLException {
        final List<User> users = new ArrayList<>();

        final String sql = """
                SELECT 
                    ua.ID, ua.EMP_ID, ua.USER_ID, e.NAME AS NAME, 
                    ua.MOBILE, ua.EMAIL, ua.BR_ID, ua.DEPT_ID, ua.DEPT,
                    d.ID AS ROLE_ID, d.NAME AS ROLE
                FROM USER_ACCOUNT ua
                JOIN EMP e ON ua.EMP_ID = e.ID
                JOIN DESIG d ON e.DESIGNATION = d.ID
                WHERE d.ID = (
                    SELECT OWNER_ROLE FROM TAT_MASTER WHERE ID = ? AND STATUS = 1
                )
                AND ua.STATUS = 1
                ORDER BY e.NAME
                """;

        try (final Connection conn = getConnection();
             final PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, stageId);

            try (final ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    users.add(mapUserFromResultSet(rs));
                }
            }
        }

        return users;
    }

    /**
     * Get all managers from given user up to the top of hierarchy.
     * Traverses EMP.REPORTING_TO chain until no more managers exist.
     *
     * @param userAccountId - USER_ACCOUNT.ID
     * @return List of all managers ordered by hierarchy (immediate manager first, top-most last)
     */
    public List<User> getAllManagersToTop(final int userAccountId) throws SQLException {
        final List<User> managers = new ArrayList<>();

        final String sql = """
                SELECT 
                    mgr_ua.ID, mgr_ua.EMP_ID, mgr_ua.USER_ID, mgr_e.NAME AS NAME,
                    mgr_ua.MOBILE, mgr_ua.EMAIL, mgr_ua.BR_ID, mgr_ua.DEPT_ID, mgr_ua.DEPT,
                    mgr_d.ID AS ROLE_ID, mgr_d.NAME AS ROLE, mgr.HIER_LEVEL
                FROM (
                    SELECT 
                        e.ID AS MANAGER_EMP_ID,
                        LEVEL AS HIER_LEVEL
                    FROM EMP e
                    START WITH e.ID = (
                        SELECT EMP_ID FROM USER_ACCOUNT WHERE ID = ?
                    )
                    CONNECT BY NOCYCLE PRIOR e.REPORTING_TO = e.ID
                ) mgr
                JOIN EMP mgr_e ON mgr_e.ID = mgr.MANAGER_EMP_ID
                JOIN USER_ACCOUNT mgr_ua ON mgr_ua.EMP_ID = mgr.MANAGER_EMP_ID
                LEFT JOIN DESIG mgr_d ON mgr_d.ID = mgr_e.DESIGNATION
                WHERE mgr.HIER_LEVEL > 1
                ORDER BY mgr.HIER_LEVEL ASC
                """;

        try (final Connection conn = getConnection();
             final PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, userAccountId);

            try (final ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    managers.add(mapUserFromResultSet(rs));
                }
            }
        }

        return managers;
    }

    private User mapUserFromResultSet(final ResultSet rs) throws SQLException {
        final User user = new User();
        user.setId(rs.getInt("ID"));
        user.setEmpId(rs.getInt("EMP_ID"));
        user.setUserId(rs.getString("USER_ID"));
        user.setName(rs.getString("NAME"));
        user.setMobile(rs.getString("MOBILE"));
        user.setEmail(rs.getString("EMAIL"));
        user.setBrId(rs.getInt("BR_ID"));
        user.setDeptId(rs.getInt("DEPT_ID"));
        user.setDept(rs.getString("DEPT"));
        user.setRoleId(rs.getInt("ROLE_ID"));
        user.setRole(rs.getString("ROLE"));
        return user;
    }

    public User getReportingManager(final int userAccountId) throws SQLException {
        final String sql = """
        SELECT
            mgr_ua.ID,
            mgr_ua.EMP_ID,
            mgr_ua.USER_ID,
            mgr_e.NAME AS NAME,
            mgr_ua.MOBILE,
            mgr_ua.EMAIL,
            mgr_ua.BR_ID,
            mgr_ua.DEPT_ID,
            mgr_ua.DEPT,
            mgr_d.ID AS ROLE_ID,
            mgr_d.NAME AS ROLE,
            mgr_d.HIERARCHY_LEVEL
        FROM USER_ACCOUNT ua
        JOIN EMP e
          ON e.ID = ua.EMP_ID
        JOIN EMP mgr_e
          ON mgr_e.ID = e.REPORTING_TO
        LEFT JOIN USER_ACCOUNT mgr_ua
          ON mgr_ua.EMP_ID = e.REPORTING_TO
        LEFT JOIN DESIG mgr_d
          ON mgr_d.ID = mgr_e.DESIGNATION
        WHERE ua.ID = ?
        """;

        try (final Connection conn = getConnection();
             final PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, userAccountId);

            try (final ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    final User user = new User();
                    user.setId(rs.getInt("ID"));
                    user.setEmpId(rs.getInt("EMP_ID"));
                    user.setUserId(rs.getString("USER_ID"));
                    user.setName(rs.getString("NAME"));
                    user.setMobile(rs.getString("MOBILE"));
                    user.setEmail(rs.getString("EMAIL"));
                    user.setBrId(rs.getInt("BR_ID"));
                    user.setDeptId(rs.getInt("DEPT_ID"));
                    user.setDept(rs.getString("DEPT"));
                    user.setRoleId(rs.getInt("ROLE_ID"));
                    user.setRole(rs.getString("ROLE"));

                    return user;
                }
            }
        }
        return null;
    }

    /**
     * Check if user has access based on allowed designation IDs.
     * Generic method for role-based access control.
     *
     * @param userAccountId         USER_ACCOUNT.ID
     * @param allowedDesignationIds Set of allowed DESIG.ID values
     * @return true if user's designation is in the allowed set
     */
    /**
     * Checks if a user has any of the specified designation IDs.
     *
     * @param userAccountId          the user account ID
     * @param allowedDesignationIds  a set of allowed designation IDs
     * @return {@code true} if the user has one of the allowed designations, {@code false} otherwise
     * @throws SQLException if a database access error occurs
     */
    public boolean hasAccess(final int userAccountId, final Set<Integer> allowedDesignationIds) throws SQLException {
        if (allowedDesignationIds == null || allowedDesignationIds.isEmpty()) {
            return false;
        }

        final String placeholders = allowedDesignationIds.stream()
                .map(String::valueOf)
                .collect(Collectors.joining(","));

        final String sql = String.format(SQL_CHECK_USER_DESIGNATION, placeholders);

        try (final Connection conn = getConnection();
             final PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userAccountId);
            try (final ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    /**
     * Check if user has access using varargs for designation IDs.
     *
     * @param userAccountId  USER_ACCOUNT.ID
     * @param designationIds allowed DESIG.ID values
     * @return true if user's designation matches any of the provided IDs
     */
    public boolean hasAccess(final int userAccountId, final Integer... designationIds) throws SQLException {
        return hasAccess(userAccountId, Arrays.stream(designationIds).collect(Collectors.toSet()));
    }

    /**
     * Check if user can create replacement requests (CRO, AM, AM Manager).
     */
    public boolean canCreateRequest(final int userAccountId) throws SQLException {
        return hasAccess(userAccountId, ACCESS_CREATE_REQUEST);
    }

    /**
     * Check if user can access AM module (CRO, AM only).
     */
    public boolean canAccessAMModule(final int userAccountId) throws SQLException {
        return hasAccess(userAccountId, ACCESS_AM_MODULE);
    }

    /**
     * Check if user can access AM Manager module.
     */
    public boolean canAccessAMManagerModule(final int userAccountId) throws SQLException {
        return hasAccess(userAccountId, ACCESS_AM_MANAGER_MODULE);
    }

    /**
     * Get user's designation ID.
     *
     * @param userAccountId USER_ACCOUNT.ID
     * @return DESIG.ID or null if not found
     */
    public Integer getUserDesignationId(final int userAccountId) throws SQLException {
        try (final Connection conn = getConnection();
             final PreparedStatement ps = conn.prepareStatement(SQL_GET_USER_DESIGNATION_ID)) {
            ps.setInt(1, userAccountId);
            try (final ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt("DESIGNATION") : null;
            }
        }
    }

    /**
     * Checks if a user has the CRO designation.
     *
     * @param userId the user account ID
     * @return {@code true} if the user is a CRO, {@code false} otherwise
     * @throws SQLException if a database access error occurs
     */
    public boolean isTLSupport(final int userId) throws SQLException {

        final String sql = """
        SELECT 1
        FROM USER_ACCOUNT ua
        JOIN EMP e ON e.ID = ua.EMP_ID
        WHERE ua.ID = ?
          AND e.DESIGNATION IN (261)
        """;

        try (final Connection conn = getConnection();
             final PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, userId);

            try (final ResultSet rs = ps.executeQuery()) {
                return rs.next(); // true if at least one row exists
            }
        }
    }

    /**
     * Checks if a user has the CRO designation.
     *
     * @param userId the user account ID
     * @return {@code true} if the user is a CRO, {@code false} otherwise
     * @throws SQLException if a database access error occurs
     */
    public boolean isCRO(final int userId) throws SQLException {

        final String sql = """
        SELECT 1
        FROM USER_ACCOUNT ua
        JOIN EMP e ON e.ID = ua.EMP_ID
        WHERE ua.ID = ?
          AND e.DESIGNATION IN (2, 4)
        """;

        try (final Connection conn = getConnection();
             final PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, userId);

            try (final ResultSet rs = ps.executeQuery()) {
                return rs.next(); // true if at least one row exists
            }
        }
    }

    /**
     * Checks if a user has the Team Leader Service designation.
     *
     * @param userId the user account ID
     * @return {@code true} if the user is a Team Leader Service, {@code false} otherwise
     * @throws SQLException if a database access error occurs
     */
    public boolean isTLLead(final int userId) throws SQLException {

        final String sql = """
        SELECT 1
        FROM USER_ACCOUNT ua
        JOIN EMP e ON e.ID = ua.EMP_ID
        WHERE ua.ID = ?
          AND e.DESIGNATION IN (62)
        """;

        try (Connection conn = getConnection();
             final PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, userId);

            try (final ResultSet rs = ps.executeQuery()) {
                return rs.next(); // true if at least one row exists
            }
        }
    }


    /**
     * Checks if a user has the Manager CRO Operations designation.
     *
     * @param userId the user account ID
     * @return {@code true} if the user is an AM Manager, {@code false} otherwise
     * @throws SQLException if a database access error occurs
     */
    public boolean isAMManager(final int userId) throws SQLException {

        final String sql = """
        SELECT 1
        FROM USER_ACCOUNT ua
        JOIN EMP e ON e.ID = ua.EMP_ID
        WHERE ua.ID = ?
          AND e.DESIGNATION IN (87)
        """;

        try (final Connection conn = getConnection();
             final PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, userId);

            try (final ResultSet rs = ps.executeQuery()) {
                return rs.next(); // true if at least one row exists
            }
        }
    }

    /**
     * Checks if a user has the Account Billing designation.
     *
     * @param userId the user account ID
     * @return {@code true} if the user is an Account Billing user, {@code false} otherwise
     * @throws SQLException if a database access error occurs
     */
    public boolean isAccountBillingUser(final int userId) throws SQLException {

        final String sql = """
        SELECT 1
        FROM USER_ACCOUNT ua
        JOIN EMP e ON e.ID = ua.EMP_ID
        WHERE ua.ID = ?
          AND e.DESIGNATION IN (202)
        """;

        try (final Connection conn = getConnection();
             final PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, userId);

            try (final ResultSet rs = ps.executeQuery()) {
                return rs.next(); // true if at least one row exists
            }
        }
    }

    public Map<Integer, String> getUsernamesByIds(List<Integer> ids) throws Exception {

        if (ids == null || ids.isEmpty()) {
            return Collections.emptyMap();
        }

        Map<Integer, String> result = new HashMap<>();

        // Build ?,?,? dynamically
        String placeholders = ids.stream()
                .map(id -> "?")
                .collect(Collectors.joining(","));

        String sql = """
        SELECT ID, USER_ID
        FROM USER_ACCOUNT
        WHERE ID IN (%s)
        """.formatted(placeholders);

        try (Connection conn = DBConnectionPool.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            // Bind values
            for (int i = 0; i < ids.size(); i++) {
                ps.setInt(i + 1, ids.get(i));
            }

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    result.put(
                            rs.getInt("ID"),
                            rs.getString("USER_ID")
                    );
                }
            }
        }

        return result;
    }


    public boolean isTLOrAbove(final int userId) throws SQLException {

        final String sql = """
        SELECT 1
        FROM (
            SELECT e.ID
            FROM EMP e
            START WITH e.DESIGNATION = 62
            CONNECT BY NOCYCLE PRIOR e.REPORTING_TO = e.ID
        )
        WHERE ID = (
            SELECT ua.EMP_ID
            FROM USER_ACCOUNT ua
            WHERE ua.ID = ?
        )
        FETCH FIRST 1 ROW ONLY
        """;

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, userId);

            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    public boolean isRoleForCourierLoginValid(final int userId) throws SQLException {

        final String sql = """
        SELECT 1
        FROM USER_ACCOUNT ua
        JOIN EMP e ON e.ID = ua.EMP_ID
        WHERE ua.ID = ?
          AND (
                e.ID IN (
                    SELECT h.ID
                    FROM EMP h
                    START WITH h.DESIGNATION IN (87,62)
                    CONNECT BY NOCYCLE PRIOR h.REPORTING_TO = h.ID
                )
                OR UPPER(ua.DEPT) = 'LOGISTICS'
          )
        FETCH FIRST 1 ROW ONLY
        """;

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, userId);

            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }


}
