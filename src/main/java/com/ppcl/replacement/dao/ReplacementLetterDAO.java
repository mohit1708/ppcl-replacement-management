package com.ppcl.replacement.dao;

import com.ppcl.replacement.model.Client;
import com.ppcl.replacement.model.ReplacementLetterData;
import com.ppcl.replacement.model.ReplacementPrinter;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class ReplacementLetterDAO extends BaseDAO {

    public ReplacementLetterData getLetterData(final int requestId) {
        final ReplacementLetterData data = new ReplacementLetterData();
        data.setRequestId(requestId);
        data.setLetterDate(new SimpleDateFormat("dd-MMM-yyyy").format(new Date()));
        data.setSigned(false);

        Connection conn = null;
        final PreparedStatement ps = null;
        final ResultSet rs = null;

        try {
            conn = getConnection();

            // Fetch request and client info
            fetchRequestAndClient(conn, requestId, data);

            // Fetch printer details
            final List<ReplacementPrinter> printers = fetchPrinterDetails(conn, requestId);
            data.setPrinters(printers);

            // Extract unique locations
            final List<String> locations = new ArrayList<>();
            for (final ReplacementPrinter p : printers) {
                final String loc = p.getLocation();
                if (loc != null && !locations.contains(loc)) {
                    locations.add(loc);
                }
            }
            data.setLocations(locations);

            // Cartridges - placeholder (no cartridge table defined yet)
            data.setCartridges(new ArrayList<>());

        } catch (final SQLException e) {
            e.printStackTrace();
        } finally {
            closeResources(conn, ps, rs);
        }

        return data;
    }

    private void fetchRequestAndClient(final Connection conn, final int requestId, final ReplacementLetterData data) throws SQLException {
        final String sql = """
                SELECT rr.ID, rr.STATUS, rr.REPLACEMENT_TYPE, rr.CREATION_DATE_TIME,
                       rr.SIGNED_LETTER_PATH, rr.SIGNED_UPLOAD_DATE, rr.REPLACEMENT_LETTER_GENERATED,
                       c.ID as CLIENT_ID, c.CLIENT_ID as CLIENT_CODE, c.NAME, c.BRANCH, 
                       c.ADDRESS, c.CITY, c.STATE, c.PINCODE, 
                       c.CONTACT_PERSON, c.MOBILE_NO, c.EMAIL_ID1, c.EMAIL_ID2
                FROM REPLACEMENT_REQUEST rr
                JOIN CLIENT c ON rr.CLIENT_DOT_ID_SIGNING = c.ID
                WHERE rr.ID = ?
                """;

        try (final PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, requestId);
            try (final ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    data.setRefNo("R-" + (10000 + rs.getInt("ID")));

                    final Client client = new Client();
                    client.setId(rs.getInt("CLIENT_ID"));
                    client.setClientId(rs.getString("CLIENT_CODE"));
                    client.setName(rs.getString("NAME"));
                    client.setBranch(rs.getString("BRANCH"));
                    client.setAddress(rs.getString("ADDRESS"));
                    client.setCity(rs.getString("CITY"));
                    client.setState(rs.getString("STATE"));
                    client.setPincode(rs.getString("PINCODE"));
                    client.setContactPerson(rs.getString("CONTACT_PERSON"));
                    client.setMobileNo(rs.getString("MOBILE_NO"));
                    client.setEmailId1(rs.getString("EMAIL_ID1"));
                    client.setEmailId2(rs.getString("EMAIL_ID2"));
                    data.setClient(client);

                    final String signedPath = rs.getString("SIGNED_LETTER_PATH");
                    if (signedPath != null && !signedPath.isEmpty()) {
                        data.setSigned(true);
                        data.setSignedLetterPath(signedPath);
                        final java.sql.Timestamp signedDate = rs.getTimestamp("SIGNED_UPLOAD_DATE");
                        if (signedDate != null) {
                            data.setSignedAt(new SimpleDateFormat("dd-MMM-yyyy HH:mm").format(signedDate));
                        }
                    }
                    data.setReplacementLetterGenerated(rs.getInt("REPLACEMENT_LETTER_GENERATED") == 1);
                }
            }
        }
    }

    private List<ReplacementPrinter> fetchPrinterDetails(final Connection conn, final int requestId) throws SQLException {
        final List<ReplacementPrinter> printers = new ArrayList<>();

        final String sql = """
                SELECT rpd.ID, rpd.REPLACEMENT_REQUEST_ID, rpd.AGR_PROD_ID, rpd.CLIENT_DOT_ID,
                       rpd.CONTACT_PERSON_NAME, rpd.CONTACT_PERSON_NUMBER, rpd.CONTACT_PERSON_EMAIL,
                       rpd.EXISTING_SERIAL, rpd.NEW_P_MODEL_SELECTED_ID, rpd.NEW_P_MODEL_SELECTED_TEXT,
                       rpd.NEW_P_MODEL_SOURCE, rpd.RECOMMENDED_COMMENTS,
                       pm_old.MODEL_NAME as EXISTING_MODEL_NAME,
                       pm_new.MODEL_NAME as NEW_MODEL_NAME,
                       ap.AGR_NO as AGREEMENT_NO,
                       c.BRANCH as LOCATION, c.CITY,
                       psm.stage as PRINTER_STAGE,
                       poia.ID as ALLOT_ID,
                       poia.P_SERIAL as ALLOT_SERIAL,
                       rpd.PRINTER_TYPE as PRINTER_TYPE,
                       a.AGR_DATE as AGREEMENT_DATE
                FROM REPLACEMENT_PRINTER_DETAILS rpd
                LEFT JOIN P_MODEL pm_old ON rpd.EXISTING_P_MODEL_ID = pm_old.ID
                LEFT JOIN P_MODEL pm_new ON rpd.NEW_P_MODEL_SELECTED_ID = pm_new.ID
                LEFT JOIN AGR_PROD ap ON rpd.AGR_PROD_ID = ap.ID
                LEFT JOIN AGR a on a.id=ap.AGR_ID
                LEFT JOIN CLIENT c ON rpd.CLIENT_DOT_ID = c.ID
                LEFT JOIN PRINTER_ORDER_ITEM poi ON rpd.printer_order_item_id=poi.id
                LEFT JOIN  printer_order_item_allot poia ON poia.item_id=poi.id
                LEFT JOIN PRINTER_STAGE_MASTER psm on psm.id=rpd.printer_stage_id
                WHERE rpd.REPLACEMENT_REQUEST_ID = ?
                and ap.serial=rpd.EXISTING_SERIAL
                ORDER BY rpd.ID
                """;

        try (final PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, requestId);
            try (final ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    final ReplacementPrinter p = new ReplacementPrinter();
                    p.setId(rs.getInt("ID"));
                    p.setReqId(rs.getInt("REPLACEMENT_REQUEST_ID"));
                    p.setClientBrId(rs.getInt("CLIENT_DOT_ID"));
                    p.setAgrProdId(rs.getInt("AGR_PROD_ID"));
                    p.setContactPerson(rs.getString("CONTACT_PERSON_NAME"));
                    p.setContactNumber(rs.getString("CONTACT_PERSON_NUMBER"));
                    p.setContactEmail(rs.getString("CONTACT_PERSON_EMAIL"));
                    p.setExistingSerial(rs.getString("EXISTING_SERIAL"));
                    p.setExistingModelName(rs.getString("EXISTING_MODEL_NAME"));

                    // New model - could be from dropdown (ID) or manual text
                    final String newModelName = rs.getString("NEW_MODEL_NAME");
                    final String newModelText = rs.getString("NEW_P_MODEL_SELECTED_TEXT");
                    p.setNewModelName(newModelName != null ? newModelName : newModelText);
                    p.setNewPModelSource(rs.getString("NEW_P_MODEL_SOURCE"));

                    p.setAgreementNoMapped(rs.getString("AGREEMENT_NO"));
                    p.setLocation(rs.getString("LOCATION"));
                    p.setCity(rs.getString("CITY"));
                    p.setRecommendedModelText(rs.getString("RECOMMENDED_COMMENTS"));
                    p.setNewSerial(rs.getString("ALLOT_SERIAL"));
                    p.setNew("NEW".equalsIgnoreCase(rs.getString("PRINTER_TYPE")));
                    p.setPrinterStage(rs.getString("PRINTER_STAGE"));
                    p.setAgreementDate(rs.getString("AGREEMENT_DATE"));
                    printers.add(p);
                }
            }
        }

        return printers;
    }
}
