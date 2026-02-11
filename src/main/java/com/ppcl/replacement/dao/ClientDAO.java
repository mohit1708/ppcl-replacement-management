package com.ppcl.replacement.dao;

import com.ppcl.replacement.model.Client;
import com.ppcl.replacement.util.DBConnectionPool;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class ClientDAO {

    /**
     * Client dropdown should be unique. We group by CLIENT.CLIENT_ID (logical code) and NAME,
     * and we return a representative branch-row ID (MIN(ID)) so UI can send an ID (as you requested).
     */
    private static final String SQL_DISTINCT_CLIENTS =
            "SELECT MIN(ID) AS ID, CLIENT_ID, NAME " +
                    "FROM CLIENT " +
                    "GROUP BY CLIENT_ID, NAME " +
                    "ORDER BY NAME";

    private static final String SQL_LOGICAL_CLIENT_ID_BY_BRANCH_ID =
            "SELECT CLIENT_ID FROM CLIENT WHERE ID = ?";

    private static final String SQL_BRANCHES_BY_LOGICAL_CLIENT_ID =
            "SELECT ID, CLIENT_ID, NAME, BRANCH, ADDRESS, CITY, STATE, PINCODE, " +
                    "       CONTACT_PERSON, MOBILE_NO, EMAIL_ID1, EMAIL_ID2 " +
                    "FROM CLIENT " +
                    "WHERE CLIENT_ID = ? " +
                    "ORDER BY BRANCH";


    private static final String SQL_REPRESENTATIVE_CLIENT_ID =
            "SELECT MIN(c2.ID) AS REP_ID " +
                    "FROM CLIENT c1 " +
                    "JOIN CLIENT c2 ON c2.CLIENT_ID = c1.CLIENT_ID AND c2.NAME = c1.NAME " +
                    "WHERE c1.ID = ?";

    private static final String SQL_CLIENT_BY_ID =
            "SELECT ID, CLIENT_ID, NAME, BRANCH, ADDRESS, CITY, STATE, PINCODE, " +
                    "       CONTACT_PERSON, MOBILE_NO, EMAIL_ID1, EMAIL_ID2 " +
                    "FROM CLIENT " +
                    "WHERE ID = ?";

    public List<Client> getDistinctClients() throws Exception {
        final List<Client> list = new ArrayList<>();

        try (final Connection con = DBConnectionPool.getConnection();
             final PreparedStatement ps = con.prepareStatement(SQL_DISTINCT_CLIENTS);
             final ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                final Client c = new Client();
                c.setId(rs.getInt("ID")); // representative branch-row id
                c.setClientId(rs.getString("CLIENT_ID"));
                c.setName(rs.getString("NAME"));
                list.add(c);
            }
        }

        return list;
    }

    /**
     * Given any CLIENT.ID (branch row id), returns its logical CLIENT.CLIENT_ID.
     */
    public String getLogicalClientIdByBranchId(final int branchId) throws Exception {
        try (final Connection con = DBConnectionPool.getConnection();
             final PreparedStatement ps = con.prepareStatement(SQL_LOGICAL_CLIENT_ID_BY_BRANCH_ID)) {

            ps.setInt(1, branchId);

            try (final ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("CLIENT_ID");
                }
            }
        }
        return null;
    }

    /**
     * Returns all branches (CLIENT rows) under the given logical CLIENT_ID.
     * This is used for:
     * - Client Sign-In Location dropdown
     * - Locations selection cards
     */
    public List<Client> getBranchesByLogicalClientId(final String logicalClientId) throws Exception {
        final List<Client> list = new ArrayList<>();

        try (final Connection con = DBConnectionPool.getConnection();
             final PreparedStatement ps = con.prepareStatement(SQL_BRANCHES_BY_LOGICAL_CLIENT_ID)) {

            ps.setString(1, logicalClientId);

            try (final ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    final Client c = new Client();
                    c.setId(rs.getInt("ID"));
                    c.setClientId(rs.getString("CLIENT_ID"));
                    c.setName(rs.getString("NAME"));
                    c.setBranch(rs.getString("BRANCH"));
                    c.setAddress(rs.getString("ADDRESS"));
                    c.setCity(rs.getString("CITY"));
                    c.setState(rs.getString("STATE"));
                    c.setPincode(rs.getString("PINCODE"));
                    c.setContactPerson(rs.getString("CONTACT_PERSON"));
                    c.setMobileNo(rs.getString("MOBILE_NO"));
                    c.setEmailId1(rs.getString("EMAIL_ID1"));
                    c.setEmailId2(rs.getString("EMAIL_ID2"));
                    list.add(c);
                }
            }
        }

        return list;
    }

    /**
     * Returns a single branch row by CLIENT.ID.
     */
    public Client getClientById(final int branchId) throws Exception {
        try (final Connection con = DBConnectionPool.getConnection();
             final PreparedStatement ps = con.prepareStatement(SQL_CLIENT_BY_ID)) {

            ps.setInt(1, branchId);

            try (final ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    final Client c = new Client();
                    c.setId(rs.getInt("ID"));
                    c.setClientId(rs.getString("CLIENT_ID"));
                    c.setName(rs.getString("NAME"));
                    c.setBranch(rs.getString("BRANCH"));
                    c.setAddress(rs.getString("ADDRESS"));
                    c.setCity(rs.getString("CITY"));
                    c.setState(rs.getString("STATE"));
                    c.setPincode(rs.getString("PINCODE"));
                    c.setContactPerson(rs.getString("CONTACT_PERSON"));
                    c.setMobileNo(rs.getString("MOBILE_NO"));
                    c.setEmailId1(rs.getString("EMAIL_ID1"));
                    c.setEmailId2(rs.getString("EMAIL_ID2"));
                    return c;
                }
            }
        }

        return null;
    }

    public Integer getRepresentativeClientId(final int anyBranchId) throws Exception {
        try (final Connection con = DBConnectionPool.getConnection();
             final PreparedStatement ps = con.prepareStatement(SQL_REPRESENTATIVE_CLIENT_ID)) {
            ps.setInt(1, anyBranchId);
            try (final ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("REP_ID");
                }
            }
        }
        return null;
    }
}
