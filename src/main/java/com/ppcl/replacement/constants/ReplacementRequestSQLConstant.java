package com.ppcl.replacement.constants;

/**
 * SQL constants for ReplacementRequestDAO
 */
public final class ReplacementRequestSQLConstant {

    private ReplacementRequestSQLConstant() {
        // Prevent instantiation
    }

    // ===================== SELECT QUERIES =====================

    public static final String GET_ALL_PRINTER_MODELS =
            "SELECT ID, MODEL_NAME FROM P_MODEL WHERE STATUS = 0 ORDER BY MODEL_NAME";

    public static final String GET_SERVICE_CALL_DETAILS =
            "SELECT cr.CALLID, cr.CLIENTID, cr.CALLBY, cr.CONTACTNO, " +
                    "       cr.PMODEL, cr.PSERIAL, cr.BRID, c.NAME AS CLIENTNAME " +
                    "FROM CLIENT_REQUEST cr " +
                    "LEFT JOIN CLIENT c ON c.CLIENT_ID = TO_CHAR(cr.CLIENT_ID) " +
                    "WHERE cr.CALLID = ?";

    public static final String NEXT_STAGE_ID_SQL = """
            SELECT next_id AS NEXT_STAGE_ID
            FROM (
              SELECT id,
                     LEAD(id) OVER (ORDER BY id) AS next_id
              FROM tat_master
            )
            WHERE id =?""";

    public static final String GET_CLIENT_CONTACT_FROM_SERVICE_CALL =
            "SELECT cr.CALLBY, cr.CONTACTNO, c.* " +
                    "FROM CLIENT_REQUEST cr " +
                    "JOIN CLIENT c ON c.CLIENTID = ? " +
                    "WHERE TO_CHAR(cr.CLIENTID) = ? " +
                    "  AND cr.CALLBY IS NOT NULL " +
                    "ORDER BY cr.CALLID DESC " +
                    "FETCH FIRST 1 ROW ONLY";

    public static final String GET_CLIENT_BY_CLIENT_ID =
            "SELECT * FROM CLIENT WHERE CLIENTID = ? AND ROWNUM = 1";

    public static final String CHECK_REQUEST_EDITABLE =
            "SELECT IS_EDITABLE FROM REPLACEMENT_REQUEST WHERE ID = ?";

    public static final String GET_USER_ACCOUNT_ID =
            "SELECT ID FROM USER_ACCOUNT WHERE USER_ID = ?";

    public static final String GET_CURRENT_STAGE =
            "SELECT CURRENT_STAGE FROM replacement_request WHERE ID = ?";

    public static final String GET_OWNER_ROLE_FOR_STAGE =
            "SELECT OWNER_ROLE FROM TAT_MASTER WHERE STAGE_CODE = ? AND STATUS = 1";

    public static final String GET_USER_FOR_OWNER_ROLE =
            "SELECT ua.USER_ID " +
                    "FROM USER_ACCOUNT ua " +
                    "LEFT JOIN DESIG d ON ua.DEPT_ID = d.ID " +
                    "WHERE d.NAME LIKE ? AND ROWNUM = 1";

    public static final String GET_NEXT_STAGE_OWNER_USER_ID =
            "SELECT CURRENT_OWNER_ID FROM REPLACEMENT_REQUEST WHERE ID = ?";

    public static final String NEXT_STAGE_ID = """
            SELECT next_id AS NEXT_STAGE_ID
            FROM (
              SELECT id,
                     LEAD(id) OVER (ORDER BY id) AS next_id
              FROM tat_master
            )
            WHERE id = ?""";

    public static final String GET_MY_PENDING_REQUESTS =
            """
                           SELECT DISTINCT r.ID, r.STATUS, r.CREATION_DATE_TIME AS CREATEDAT,  c.id as SIGNING_BRANCH,
                           r.IS_EDITABLE, r.SERVICE_CALL_ID,  
                           c.NAME AS CLIENTNAME, c.CLIENT_ID AS CLIENTID, 
                           c.BRANCH AS BRANCH, 
                           (SELECT COUNT(*) FROM REPLACEMENT_PRINTER_DETAILS pd WHERE pd.REPLACEMENT_REQUEST_ID = r.ID) AS PRINTERCOUNT,  
                           tm.STAGE_CODE AS CURRENTSTAGE, tm.DESCRIPTION AS CURRENTSTAGENAME,  
                           tm.TAT_DURATION, tm.TAT_DURATION_UNIT,  
                           ua.USER_ID AS OWNERUSERID,  
                           ua.USER_ID AS OWNERNAME,  
                           fet.START_AT AS STAGE_START, fet.END_AT AS STAGE_END ,
                           (SELECT COUNT(*) FROM REPLACEMENT_PRINTER_DETAILS pd WHERE pd.REPLACEMENT_REQUEST_ID = r.ID AND PRINTER_STAGE_ID=1 ) AS ALLOT_COUNT 
                    FROM REPLACEMENT_REQUEST r  
                    LEFT JOIN CLIENT c ON c.ID = r.CLIENT_DOT_ID_SIGNING  
                    LEFT JOIN TAT_MASTER tm ON tm.ID = r.CURRENT_STAGE  
                    LEFT JOIN USER_ACCOUNT ua ON ua.ID = r.CURRENT_OWNER_ID  
                    LEFT JOIN (SELECT fet1.* FROM RPLCE_FLOW_EVENT_TRACKING fet1  
                               WHERE fet1.ID = (SELECT MAX(fet2.ID) FROM RPLCE_FLOW_EVENT_TRACKING fet2  
                                                WHERE fet2.REPLACEMENT_REQUEST_ID = fet1.REPLACEMENT_REQUEST_ID)) fet  
                           ON fet.REPLACEMENT_REQUEST_ID = r.ID  
                    WHERE r.ID IN (SELECT REPLACEMENT_REQUEST_ID FROM RPLCE_FLOW_EVENT_TRACKING WHERE CURRENT_OWNER_USER_ID = ?)  
                       OR r.CURRENT_OWNER_ID = ?  
                    ORDER BY CASE WHEN r.STATUS IN ('OPEN', 'PENDING') THEN 0  
                                  WHEN r.STATUS = 'COMPLETED' THEN 1  
                                  ELSE 2 END, r.CREATION_DATE_TIME DESC
                    """;

    public static final String GET_REQUESTS_AT_SERVICE_TL_REVIEW =
            "SELECT r.ID, r.STATUS, r.CREATION_DATE_TIME AS CREATEDAT, " +
                    "       r.IS_EDITABLE, r.SERVICE_CALL_ID, " +
                    "       c.NAME AS CLIENTNAME, c.CLIENT_ID AS CLIENTID, " +
                    "       (SELECT COUNT(*) FROM REPLACEMENT_PRINTER_DETAILS pd WHERE pd.REPLACEMENT_REQUEST_ID = r.ID) AS PRINTERCOUNT, " +
                    "       tm.STAGE_CODE AS CURRENTSTAGE, tm.DESCRIPTION AS CURRENTSTAGENAME, " +
                    "       tm.TAT_DURATION, tm.TAT_DURATION_UNIT, " +
                    "       ua.USER_ID AS OWNERUSERID, " +
                    "       ua.USER_ID AS OWNERNAME, " +
                    "       fet.START_AT AS STAGE_START, fet.END_AT AS STAGE_END " +
                    "FROM REPLACEMENT_REQUEST r " +
                    "LEFT JOIN CLIENT c ON c.ID = r.CLIENT_DOT_ID_SIGNING " +
                    "LEFT JOIN TAT_MASTER tm ON tm.ID = r.CURRENT_STAGE " +
                    "LEFT JOIN USER_ACCOUNT ua ON ua.ID = r.CURRENT_OWNER_ID " +
                    "LEFT JOIN (SELECT fet1.* FROM RPLCE_FLOW_EVENT_TRACKING fet1 " +
                    "           WHERE fet1.ID = (SELECT MAX(fet2.ID) FROM RPLCE_FLOW_EVENT_TRACKING fet2 " +
                    "                            WHERE fet2.REPLACEMENT_REQUEST_ID = fet1.REPLACEMENT_REQUEST_ID)) fet " +
                    "       ON fet.REPLACEMENT_REQUEST_ID = r.ID " +
                    "WHERE r.STATUS IN ('OPEN', 'PENDING') " +
                    "  AND tm.STAGE_CODE = 'STG2_SERVICE_TL_REVIEW' " +
                    "ORDER BY r.CREATION_DATE_TIME DESC";

    public static final String GET_REQUEST_BY_ID = """
            SELECT rr.ID, rr.REPLACEMENT_TYPE, rr.REPLACEMENT_REASON_ID,
                   rr.CLIENT_DOT_ID_SIGNING, rr.SOURCE, rr.CURRENT_STAGE,
                   rr.REQUESTER_USER_ID, rr.SERVICE_CALL_ID, rr.CREATION_DATE_TIME, rr.UPDATE_DATE_TIME,
                   rr.CURRENT_OWNER_ID,
                   rn.NAME AS REASON_NAME,
                   c.NAME AS CLIENT_NAME,
                   tm.STAGE_CODE AS STAGE_CODE,
                   u.user_id AS REQUESTER_NAME,
                   d.name AS OWNER_ROLE
            FROM REPLACEMENT_REQUEST rr
            LEFT JOIN REPLACEMENT_REASON rn ON rr.REPLACEMENT_REASON_ID = rn.ID
            LEFT JOIN CLIENT c ON c.ID = rr.CLIENT_DOT_ID_SIGNING
            LEFT JOIN TAT_MASTER tm ON tm.ID = rr.CURRENT_STAGE
            LEFT JOIN USER_ACCOUNT u ON u.ID = rr.REQUESTER_USER_ID
            LEFT JOIN USER_ACCOUNT owner ON owner.ID = rr.CURRENT_OWNER_ID
            LEFT JOIN EMP emp ON emp.id = owner.EMP_ID
            LEFT JOIN DESIG d on d.id = emp.DESIGNATION
            WHERE rr.ID = ?""";

    public static final String GET_PRINTERS_BY_REQUEST_ID = """
            SELECT rpd.ID, rpd.CLIENT_DOT_ID, rpd.EXISTING_P_MODEL_ID, rpd.EXISTING_SERIAL,
                   rpd.NEW_P_MODEL_SELECTED_ID, rpd.NEW_P_MODEL_SELECTED_TEXT,
                   rpd.CONTACT_PERSON_NAME, rpd.CONTACT_PERSON_NUMBER, rpd.CONTACT_PERSON_EMAIL,
                   c.NAME AS LOCATION_NAME, c.CITY,
                   pm1.MODEL_NAME AS EXISTING_MODEL_NAME,
                   pm2.MODEL_NAME AS RECOMMENDED_MODEL_NAME
            FROM REPLACEMENT_PRINTER_DETAILS rpd
            LEFT JOIN CLIENT c ON c.ID = rpd.CLIENT_DOT_ID
            LEFT JOIN P_MODEL pm1 ON pm1.ID = rpd.EXISTING_P_MODEL_ID
            LEFT JOIN P_MODEL pm2 ON pm2.ID = rpd.NEW_P_MODEL_SELECTED_ID
            WHERE rpd.REPLACEMENT_REQUEST_ID = ?""";

    public static final String GET_REQUEST_DETAILS_PAYLOAD =
            "SELECT r.ID, r.REPLACEMENT_TYPE, r.REPLACEMENT_REASON_ID, r.STATUS, " +
                    "       r.IS_EDITABLE, r.SERVICE_CALL_ID, r.CLIENT_DOT_ID_SIGNING, " +
                    "       c.NAME AS CLIENT_NAME, c.CLIENT_ID, c.BRANCH AS SIGN_IN_BRANCH, c.CITY AS SIGN_IN_CITY, " +
                    "       tm.STAGE_CODE, tm.DESCRIPTION AS STAGE_NAME, " +
                    "       tm.TAT_DURATION, tm.TAT_DURATION_UNIT, " +
                    "       fet.START_AT AS STAGE_START, " +
                    "       SYSTIMESTAMP AS CURRENT_TIME " +
                    "FROM REPLACEMENT_REQUEST r " +
                    "LEFT JOIN CLIENT c ON c.ID = r.CLIENT_DOT_ID_SIGNING " +
                    "LEFT JOIN TAT_MASTER tm ON tm.ID = r.CURRENT_STAGE " +
                    "LEFT JOIN (SELECT fet1.* FROM RPLCE_FLOW_EVENT_TRACKING fet1 " +
                    "           WHERE fet1.ID = (SELECT MAX(fet2.ID) FROM RPLCE_FLOW_EVENT_TRACKING fet2 " +
                    "                            WHERE fet2.REPLACEMENT_REQUEST_ID = fet1.REPLACEMENT_REQUEST_ID " +
                    "                              AND fet2.REPLACEMENT_REQUEST_ID = ?)) fet " +
                    "       ON fet.REPLACEMENT_REQUEST_ID = r.ID " +
                    "WHERE r.ID = ?";

    public static final String GET_REQUEST_PRINTERS_INTERNAL =
            "SELECT rpd.ID, rpd.EXISTING_SERIAL, rpd.NEW_P_MODEL_SELECTED_ID, " +
                    "       rpd.NEW_P_MODEL_SELECTED_TEXT, rpd.CLIENT_DOT_ID, " +
                    "       rpd.CONTACT_PERSON_NAME, rpd.CONTACT_PERSON_NUMBER, rpd.CONTACT_PERSON_EMAIL, " +
                    "       c.BRANCH AS LOCATION, c.CITY, " +
                    "       pm.MODEL_NAME AS EXISTING_MODEL_NAME " +
                    "FROM REPLACEMENT_PRINTER_DETAILS rpd " +
                    "LEFT JOIN CLIENT c ON c.ID = rpd.CLIENT_DOT_ID " +
                    "LEFT JOIN P_MODEL pm ON pm.ID = rpd.EXISTING_P_MODEL_ID " +
                    "WHERE rpd.REPLACEMENT_REQUEST_ID = ?";

    public static final String GET_REQUEST_CONTACT_INTERNAL =
            "SELECT CONTACT_PERSON_NAME, CONTACT_PERSON_NUMBER, CONTACT_PERSON_EMAIL " +
                    "FROM REPLACEMENT_PRINTER_DETAILS " +
                    "WHERE REPLACEMENT_REQUEST_ID = ? AND ROWNUM = 1";

    public static final String GET_REPLACEMENT_REASONS =
            "SELECT ID, NAME FROM REPLACEMENT_REASON WHERE STATUS = 1 ORDER BY NAME";

    // ===================== INSERT QUERIES =====================

    public static final String INSERT_REPLACEMENT_REQUEST =
            "INSERT INTO REPLACEMENT_REQUEST " +
                    "  (REPLACEMENT_REASON_ID, CLIENT_DOT_ID_SIGNING, REPLACEMENT_TYPE, " +
                    "   REQUESTER_USER_ID, SERVICE_CALL_ID, IS_EDITABLE, CURRENT_STAGE, " +
                    "   CURRENT_OWNER_ID, SOURCE, STATUS, CONTACT_PERSON_NAME, CONTACT_PERSON_NUMBER, CONTACT_PERSON_EMAIL) " +
                    "VALUES " +
                    "  (?, ?, ?, ?, ?, 1, ?, ?, ?, 'OPEN',?, ?, ?)";

    public static final String INSERT_REPLACEMENT_PRINTER_DETAILS =
            "INSERT INTO REPLACEMENT_PRINTER_DETAILS " +
                    "  (REPLACEMENT_REQUEST_ID, AGR_PROD_ID, EXISTING_P_MODEL_ID, CLIENT_DOT_ID, " +
                    "   CONTACT_PERSON_NAME, CONTACT_PERSON_NUMBER, CONTACT_PERSON_EMAIL, " +
                    "   EXISTING_SERIAL, NEW_P_MODEL_SELECTED_ID, NEW_P_MODEL_SELECTED_TEXT, " +
                    "   NEW_P_MODEL_SOURCE,PRINTER_STAGE_ID) " +
                    "VALUES " +
                    "  (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?,?)";

    public static final String INSERT_COMMENT =
            "INSERT INTO RPLCE_COMMENTS_TRACKING (REPLACEMENT_REQUEST_ID, COMMENT_BY_USER_ID, COMMENTS) " +
                    "VALUES (?, (SELECT ID FROM USER_ACCOUNT WHERE USER_ID = ?), ?)";

    public static final String ADD_COMMENT =
            "INSERT INTO RPLCE_COMMENTS_TRACKING (ID, REPLACEMENT_REQUEST_ID, COMMENT_TEXT, COMMENTED_BY, COMMENT_AT) " +
                    "VALUES ((SELECT NVL(MAX(ID),0)+1 FROM RPLCE_COMMENTS_TRACKING), ?, ?, ?, SYSTIMESTAMP)";

    public static final String INSERT_AUDIT_LOG =
            "INSERT INTO REPL_AUDIT_LOG " +
                    "  (ID, REQID, PRINTERID, ACTION, ACTORUSERID, ACTORROLE, DETAILS, CREATEDAT) " +
                    "VALUES " +
                    "  (SEQ_REPLAUDITLOG.NEXTVAL, ?, ?, ?, ?, ?, ?, SYSTIMESTAMP)";

    // ===================== UPDATE QUERIES =====================

    public static final String UPDATE_REPLACEMENT_REQUEST_HEADER =
            "UPDATE REPLACEMENT_REQUEST " +
                    "SET REPLACEMENT_TYPE = ?, REPLACEMENT_REASON_ID = ?, CLIENT_DOT_ID_SIGNING = ?, UPDATE_DATE_TIME = SYSTIMESTAMP " +
                    "WHERE ID = ?";

    public static final String UPDATE_PRINTER_CONTACT_DETAILS =
            "UPDATE REPLACEMENT_PRINTER_DETAILS " +
                    "SET CONTACT_PERSON_NAME = ?, CONTACT_PERSON_NUMBER = ?, CONTACT_PERSON_EMAIL = ?, " +
                    "    UPDATE_DATE_TIME = SYSTIMESTAMP " +
                    "WHERE REPLACEMENT_REQUEST_ID = ?";

    public static final String UPDATE_PRINTER_MODEL_SELECTION =
            "UPDATE REPLACEMENT_PRINTER_DETAILS " +
                    "SET NEW_P_MODEL_SELECTED_ID = ?, NEW_P_MODEL_SELECTED_TEXT = ?, " +
                    "    NEW_P_MODEL_SOURCE = ?, UPDATE_DATE_TIME = SYSTIMESTAMP " +
                    "WHERE ID = ?";

    public static final String GET_REQUEST_AT_SERVICE_TL_STATUS = "SELECT r.ID, r.STATUS, r.CREATION_DATE_TIME AS CREATEDAT, " +
            "       r.IS_EDITABLE, r.SERVICE_CALL_ID, " +
            "       c.NAME AS CLIENTNAME, c.CLIENT_ID AS CLIENTID, c.BRANCH AS BRANCH, " +
            "       (SELECT COUNT(*) FROM REPLACEMENT_PRINTER_DETAILS pd WHERE pd.REPLACEMENT_REQUEST_ID = r.ID) AS PRINTERCOUNT, " +
            "       tm.STAGE_CODE AS CURRENTSTAGE, tm.DESCRIPTION AS CURRENTSTAGENAME, " +
            "       tm.TAT_DURATION, tm.TAT_DURATION_UNIT, " +
            "       ua.USER_ID AS OWNERUSERID, " +
            "       ua.USER_ID AS OWNERNAME, " +
            "       fet.START_AT AS STAGE_START, fet.END_AT AS STAGE_END " +
            "FROM REPLACEMENT_REQUEST r " +
            "LEFT JOIN CLIENT c ON c.ID = r.CLIENT_DOT_ID_SIGNING " +
            "LEFT JOIN TAT_MASTER tm ON tm.ID = r.CURRENT_STAGE " +
            "LEFT JOIN USER_ACCOUNT ua ON ua.ID = r.CURRENT_OWNER_ID " +
            "LEFT JOIN (SELECT fet1.* FROM RPLCE_FLOW_EVENT_TRACKING fet1 " +
            "           WHERE fet1.ID = (SELECT MAX(fet2.ID) FROM RPLCE_FLOW_EVENT_TRACKING fet2 " +
            "                            WHERE fet2.REPLACEMENT_REQUEST_ID = fet1.REPLACEMENT_REQUEST_ID)) fet " +
            "       ON fet.REPLACEMENT_REQUEST_ID = r.ID " +
            "WHERE r.STATUS IN ('OPEN', 'PENDING') " +
            "  AND tm.STAGE_CODE = 'STG2_SERVICE_TL_REVIEW' " +
            "ORDER BY r.CREATION_DATE_TIME DESC";

    public static final String GET_CURRENT_STAGE_USING_REQUEST_ID = "SELECT CURRENT_STAGE FROM replacement_request WHERE ID = ?";

    public static final String GET_REPLACEMENT_REQUESTER_NAME = """
            SELECT UA.USER_ID as USER_NAME FROM REPLACEMENT_REQUEST RR, USER_ACCOUNT UA WHERE RR.ID =? AND RR.REQUESTER_USER_ID =UA.ID
            """;
    public static final String GET_REPLACEMENT_REQUEST_USING_REQUEST_ID = """
            
                     SELECT rr.ID, rr.REPLACEMENT_TYPE, rr.REPLACEMENT_REASON_ID,
                   rr.CLIENT_DOT_ID_SIGNING, rr.SOURCE, rr.CURRENT_STAGE,
                   rr.REQUESTER_USER_ID, rr.SERVICE_CALL_ID, rr.CREATION_DATE_TIME, rr.UPDATE_DATE_TIME,
                   rr.CURRENT_OWNER_ID,
                   rn.NAME AS REASON_NAME,
                   c.NAME AS CLIENT_NAME,
                   tm.STAGE_CODE AS STAGE_CODE,
                   u.user_id AS REQUESTER_NAME,
                   d.name AS OWNER_ROLE
            FROM REPLACEMENT_REQUEST rr
            LEFT JOIN REPLACEMENT_REASON rn ON rr.REPLACEMENT_REASON_ID = rn.ID
            LEFT JOIN CLIENT c ON c.ID = rr.CLIENT_DOT_ID_SIGNING
            LEFT JOIN TAT_MASTER tm ON tm.ID = rr.CURRENT_STAGE
            LEFT JOIN USER_ACCOUNT u ON u.ID = rr.REQUESTER_USER_ID
            LEFT JOIN USER_ACCOUNT owner ON owner.ID = rr.CURRENT_OWNER_ID
            LEFT JOIN EMP emp ON emp.id = owner.EMP_ID
            LEFT JOIN DESIG d on d.id = emp.DESIGNATION
            WHERE rr.ID =?""";

    public static final String GET_ALL_PRINTER_USING_REQUEST_ID = """
            SELECT rpd.ID, rpd.CLIENT_DOT_ID, rpd.EXISTING_P_MODEL_ID, rpd.EXISTING_SERIAL,
                   rpd.NEW_P_MODEL_SELECTED_ID, rpd.NEW_P_MODEL_SELECTED_TEXT,
                   rpd.CONTACT_PERSON_NAME, rpd.CONTACT_PERSON_NUMBER, rpd.CONTACT_PERSON_EMAIL,
                   c.NAME AS LOCATION_NAME, c.CITY,
                   pm1.MODEL_NAME AS EXISTING_MODEL_NAME,
                   pm2.MODEL_NAME AS RECOMMENDED_MODEL_NAME
            FROM REPLACEMENT_PRINTER_DETAILS rpd
            LEFT JOIN CLIENT c ON c.ID = rpd.CLIENT_DOT_ID
            LEFT JOIN P_MODEL pm1 ON pm1.ID = rpd.EXISTING_P_MODEL_ID
            LEFT JOIN P_MODEL pm2 ON pm2.ID = rpd.NEW_P_MODEL_SELECTED_ID
            WHERE rpd.REPLACEMENT_REQUEST_ID = ?
            """;

    public static final String GET_ALL_REPLACEMENT_REASON = "SELECT ID, NAME FROM REPLACEMENT_REASON WHERE STATUS = 1 ORDER BY NAME";

    public static final String GET_CONTACT_DETAILS_FROM_PRINTER =
            "SELECT CONTACT_PERSON_NAME, CONTACT_PERSON_NUMBER, CONTACT_PERSON_EMAIL " +
                    "FROM REPLACEMENT_PRINTER_DETAILS " +
                    "WHERE REPLACEMENT_REQUEST_ID = ? AND ROWNUM = 1";

    public static final String GET_ALL_PRINTER_USING_REQUEST_ID_PAYLOAD =
            "SELECT rpd.ID, rpd.EXISTING_SERIAL, rpd.NEW_P_MODEL_SELECTED_ID, " +
                    "       COALESCE(npm.MODEL_NAME, rpd.NEW_P_MODEL_SELECTED_TEXT) AS NEW_P_MODEL_SELECTED_TEXT, " +
                    "       rpd.CLIENT_DOT_ID, " +
                    "       rpd.CONTACT_PERSON_NAME, rpd.CONTACT_PERSON_NUMBER, rpd.CONTACT_PERSON_EMAIL, " +
                    "       c.BRANCH AS LOCATION, c.CITY, " +
                    "       pm.MODEL_NAME AS EXISTING_MODEL_NAME " +
                    "FROM REPLACEMENT_PRINTER_DETAILS rpd " +
                    "LEFT JOIN CLIENT c ON c.ID = rpd.CLIENT_DOT_ID " +
                    "LEFT JOIN P_MODEL pm ON pm.ID = rpd.EXISTING_P_MODEL_ID " +
                    "LEFT JOIN P_MODEL npm ON npm.ID = rpd.NEW_P_MODEL_SELECTED_ID " +
                    "WHERE rpd.REPLACEMENT_REQUEST_ID = ?";

    public static final String GET_REPLACEMENT_REQUEST_USING_REQUEST_ID_PAYLOAD =
            "SELECT r.ID, r.REPLACEMENT_TYPE, r.REPLACEMENT_REASON_ID, r.STATUS, " +
                    "       r.IS_EDITABLE, r.SERVICE_CALL_ID, r.CLIENT_DOT_ID_SIGNING, " +
                    "       c.NAME AS CLIENT_NAME, c.CLIENT_ID, c.BRANCH AS SIGN_IN_BRANCH, c.CITY AS SIGN_IN_CITY, " +
                    "       tm.STAGE_CODE, tm.DESCRIPTION AS STAGE_NAME, " +
                    "       tm.TAT_DURATION, tm.TAT_DURATION_UNIT, " +
                    "       fet.START_AT AS STAGE_START, " +
                    "       SYSTIMESTAMP AS CURRENT_TIME " +
                    "FROM REPLACEMENT_REQUEST r " +
                    "LEFT JOIN CLIENT c ON c.ID = r.CLIENT_DOT_ID_SIGNING " +
                    "LEFT JOIN TAT_MASTER tm ON tm.ID = r.CURRENT_STAGE " +
                    "LEFT JOIN (SELECT fet1.* FROM RPLCE_FLOW_EVENT_TRACKING fet1 " +
                    "           WHERE fet1.ID = (SELECT MAX(fet2.ID) FROM RPLCE_FLOW_EVENT_TRACKING fet2 " +
                    "                            WHERE fet2.REPLACEMENT_REQUEST_ID = fet1.REPLACEMENT_REQUEST_ID " +
                    "                              AND fet2.REPLACEMENT_REQUEST_ID = ?)) fet " +
                    "       ON fet.REPLACEMENT_REQUEST_ID = r.ID " +
                    "WHERE r.ID = ?";

    public static final String INSERT_COMMENT_INTO_CMT_TRACKING_SQL = "INSERT INTO RPLCE_COMMENTS_TRACKING (ID, REPLACEMENT_REQUEST_ID, COMMENT_TEXT, COMMENTED_BY, COMMENT_AT) " +
            "VALUES ((SELECT NVL(MAX(ID),0)+1 FROM RPLCE_COMMENTS_TRACKING), ?, ?, ?, SYSTIMESTAMP)";

    // Check for duplicate replacement request by CLIENT_DOT_ID, EXISTING_SERIAL, EXISTING_P_MODEL_ID
    // Only check for requests that are not CLOSED or CANCELLED
    public static final String CHECK_DUPLICATE_PRINTER_REQUEST =
            "SELECT rpd.REPLACEMENT_REQUEST_ID " +
                    "FROM REPLACEMENT_PRINTER_DETAILS rpd " +
                    "JOIN REPLACEMENT_REQUEST rr ON rr.ID = rpd.REPLACEMENT_REQUEST_ID " +
                    "WHERE rpd.CLIENT_DOT_ID = ? " +
                    "  AND rpd.EXISTING_SERIAL = ? " +
                    "  AND rpd.EXISTING_P_MODEL_ID = ? " +
                    "  AND rr.STATUS NOT IN ('CLOSED', 'REJECTED','CANCELLED') " +
                    "  AND ROWNUM = 1";

    public static final String CHECK_DUPLICATE_BY_SERIAL =
            "SELECT rpd.REPLACEMENT_REQUEST_ID, rpd.EXISTING_SERIAL " +
                    "FROM REPLACEMENT_PRINTER_DETAILS rpd " +
                    "JOIN REPLACEMENT_REQUEST rr ON rr.ID = rpd.REPLACEMENT_REQUEST_ID " +
                    "WHERE rpd.EXISTING_SERIAL = ? " +
                    "  AND rr.STATUS NOT IN ('CLOSED', 'REJECTED') " +
                    "  AND ROWNUM = 1";

}
