package com.ppcl.replacement.dao;

import com.ppcl.replacement.model.*;
import com.ppcl.replacement.util.DBConnectionPool;

import java.sql.*;
import java.util.*;

public class PrinterDAO {

    /**
     * Get all active replacement reasons
     */
    public List<ReplacementReason> getAllActiveReasons() throws Exception {
        final List<ReplacementReason> list = new ArrayList<>();
        final String sql =
                "SELECT ID, NAME " +
                        "FROM REPLACEMENT_REASON " +
                        "WHERE STATUS = 1 " +
                        "ORDER BY NAME";

        try (final Connection con = DBConnectionPool.getConnection();
             final PreparedStatement ps = con.prepareStatement(sql);
             final ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                final ReplacementReason r = new ReplacementReason();
                r.setId(rs.getInt("ID"));
                r.setName(rs.getString("NAME"));
                list.add(r);
            }
        }

        return list;
    }

    /**
     * Get printers for specific location (CLIENT.ID)
     */
    public List<PrinterDetail> getPrintersByLocation(final int clientBrId) throws Exception {
        final List<PrinterDetail> list = new ArrayList<>();

        final String sql =
                "SELECT ap.ID AS AGRPRODID, ap.P_MODEL AS P_MODEL, ap.SERIAL AS SERIAL, " +
                        "       pm.MODEL_NAME AS MODEL_NAME, ap.CLIENT_BR_ID AS CLIENT_BR_ID " +
                        "FROM AGR_PROD ap " +
                        "JOIN P_MODEL pm ON ap.P_MODEL = pm.ID " +
                        "WHERE ap.CLIENT_BR_ID = ? " +
                        "  AND NVL(ap.TERMINATED, 0) = 0 " +
                        "ORDER BY pm.MODEL_NAME, ap.SERIAL";


        try (final Connection con = DBConnectionPool.getConnection();
             final PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, clientBrId);

            try (final ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    final PrinterDetail p = new PrinterDetail();
                    p.setAgrProdId(rs.getInt("AGRPRODID"));
                    p.setPModelId(rs.getInt("P_MODEL"));
                    p.setSerial(rs.getString("SERIAL"));
                    p.setModelName(rs.getString("MODEL_NAME"));
                    p.setClientBrId(rs.getInt("CLIENT_BR_ID"));
                    list.add(p);
                }
            }
        }

        return list;
    }

    /**
     * Get printers for multiple locations with duplicate request check
     */
    public List<PrinterDetail> getPrintersByLocations(final List<Integer> locationIds) throws Exception {
        if (locationIds == null || locationIds.isEmpty()) {
            return new ArrayList<>();
        }

        final List<PrinterDetail> list = new ArrayList<>();

        final StringBuilder sql = new StringBuilder();
        sql.append("SELECT ap.ID AS AGRPRODID, ap.P_MODEL AS P_MODEL, ap.SERIAL AS SERIAL, ");
        sql.append("       pm.MODEL_NAME AS MODEL_NAME, ap.CLIENT_BR_ID AS CLIENT_BR_ID, ");
        sql.append("       (SELECT rpd.REPLACEMENT_REQUEST_ID ");
        sql.append("        FROM REPLACEMENT_PRINTER_DETAILS rpd ");
        sql.append("        JOIN REPLACEMENT_REQUEST rr ON rr.ID = rpd.REPLACEMENT_REQUEST_ID ");
        sql.append("        WHERE rpd.CLIENT_DOT_ID = ap.CLIENT_BR_ID ");
        sql.append("          AND rpd.EXISTING_SERIAL = ap.SERIAL ");
        sql.append("          AND rpd.EXISTING_P_MODEL_ID = ap.P_MODEL ");
        sql.append("          AND rr.STATUS NOT IN ('CLOSED', 'REJECTED') ");
        sql.append("          AND ROWNUM = 1) AS EXISTING_REQUEST_ID ");
        sql.append("FROM AGR_PROD ap ");
        sql.append("JOIN P_MODEL pm ON ap.P_MODEL = pm.ID ");
        sql.append("WHERE NVL(ap.TERMINATED, 0) = 0 ");
        sql.append("  AND ap.CLIENT_BR_ID IN (");

        for (int i = 0; i < locationIds.size(); i++) {
            sql.append("?");
            if (i < locationIds.size() - 1) {
                sql.append(",");
            }
        }
        sql.append(") ORDER BY ap.CLIENT_BR_ID, pm.MODEL_NAME, ap.SERIAL");

        try (final Connection con = DBConnectionPool.getConnection();
             final PreparedStatement ps = con.prepareStatement(sql.toString())) {

            for (int i = 0; i < locationIds.size(); i++) {
                ps.setInt(i + 1, locationIds.get(i));
            }

            try (final ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    final PrinterDetail p = new PrinterDetail();
                    p.setAgrProdId(rs.getInt("AGRPRODID"));
                    p.setPModelId(rs.getInt("P_MODEL"));
                    p.setSerial(rs.getString("SERIAL"));
                    p.setModelName(rs.getString("MODEL_NAME"));
                    p.setClientBrId(rs.getInt("CLIENT_BR_ID"));

                    // Set existing request ID if printer is already in a replacement request
                    final int existingReqId = rs.getInt("EXISTING_REQUEST_ID");
                    if (!rs.wasNull() && existingReqId > 0) {
                        p.setExistingRequestId(existingReqId);
                    }

                    list.add(p);
                }
            }
        }

        return list;
    }
}
