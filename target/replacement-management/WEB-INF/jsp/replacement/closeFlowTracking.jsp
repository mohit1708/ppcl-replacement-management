<%@ page import="java.sql.*" %>
<%@ page import="com.ppcl.replacement.util.DBConnectionPool" %>
<%@ page contentType="application/json;charset=UTF-8" %>
<%
    int reqId = 0;
    String json = "";
    try {
        reqId = Integer.parseInt(request.getParameter("id"));
    } catch (Exception e) {
        json = "{\"success\":false,\"message\":\"Invalid request ID\"}";
        out.print(json);
        return;
    }

    Connection conn = null;
    PreparedStatement ps = null;
    try {
        conn = DBConnectionPool.getConnection();
        String sql = "UPDATE RPLCE_FLOW_EVENT_TRACKING SET END_AT = SYSTIMESTAMP " +
                     "WHERE REPLACEMENT_REQUEST_ID = ? AND CURRENT_STAGE_ID = 13 AND END_AT IS NULL";
        ps = conn.prepareStatement(sql);
        ps.setInt(1, reqId);
        int rows = ps.executeUpdate();
        conn.commit();
        json = "{\"success\":true,\"rowsUpdated\":" + rows + "}";
    } catch (Exception e) {
        if (conn != null) try { conn.rollback(); } catch (Exception ex) {}
        json = "{\"success\":false,\"message\":\"" + e.getMessage().replace("\"", "'") + "\"}";
    } finally {
        if (ps != null) try { ps.close(); } catch (Exception e) {}
        if (conn != null) try { conn.close(); } catch (Exception e) {}
    }
    out.print(json);
%>
