package com.ppcl.replacement.dao;

import com.ppcl.replacement.model.AgrProd;
import com.ppcl.replacement.model.ReplacementPrinterAgrDTO;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class AgrDao {

    public final String GET_AGR_BY_PRINTER_USING_REQUEST_ID_SQL = """
            SELECT
                rpd.ID                  AS RPD_ID,
                rpd.REPLACEMENT_REQUEST_ID,
                rpd.AGR_PROD_ID,
                rpd.EXISTING_P_MODEL_ID,
                rpd.EXISTING_SERIAL,
                rpd.PRINTER_TYPE,
                rpd.PAGE_COUNT,
                rpd.PAGE_COUNT_COMMENT,
                rpd.PRINTER_STAGE_ID,
                rpd.CONTINUE_EXISTING_COMMERCIAL,
                rpd.AM_COMMERCIAL_COMMENTS,
                ap.ID                   AS AP_ID,
                ap.AGR_ID,
                ap.AGR_NO,
                ap.PROD_ID,
                ap.RENT,
                ap.FREE_PRINTS,
                ap.A3_RATE_POST,
                ap.A4_RATE_POST,
                ap.A3_RATE,
                ap.A4_RATE,
                ap.FREE_SCAN,
                ap.SCAN_RATE,
                ap.SCAN_RATE_POST,
                ap.PAGE_COMMITED,
                ap.BLACK_CART_RATE,
                ap.CLR_CART_RATE,
                ap.CART_COMMITED,
                ap.BILLING_COMMITED,
                ap.COMMITMENT_PERIOD,
                ap.AMC,
                ap.PRINTER_COLOR,
                ap.CLIENT_BR_ID,
                ap.AGR_COMMERCE_TYPE,
                ap.DRUM_UNIT_FREE,
                ap.DRUM_UNIT_CHARGE,
                ap.AMC_TYPE,
                ap.FREE_A3_BLACK,
                ap.FREE_A4_COLOR,
                ap.FREE_A3_COLOR,
                ap.A3_RATE_POST_COLOR,
                ap.A4_RATE_POST_COLOR,
                ap.A4_RATE_COLOR,
                ap.A3_RATE_COLOR,
                ap.P_MODEL,
                c.BRANCH AS LOCATION,
                c.CITY
            FROM REPLACEMENT_PRINTER_DETAILS rpd
            JOIN AGR_PROD ap
              ON ap.ID = rpd.AGR_PROD_ID
            LEFT JOIN CLIENT c
              ON c.ID = rpd.CLIENT_DOT_ID
            WHERE ap.TERMINATED=0 and rpd.REPLACEMENT_REQUEST_ID = ?
            ORDER BY rpd.ID
            """;

    public List<AgrProd> fetchAgrProducts(final Connection conn, final long agrId) throws SQLException {

        final String sql =
                "SELECT ID, PROD_ID, RENT, FREE_PRINTS, A3_RATE_POST, A4_RATE_POST, " +
                        "A3_RATE, A4_RATE, FREE_SCAN, SCAN_RATE, SCAN_RATE_POST, PAGE_COMMITED, " +
                        "BLACK_CART_RATE, CLR_CART_RATE, CART_COMMITED, BILLING_COMMITED, " +
                        "COMMITMENT_PERIOD, AMC, PRINTER_COLOR, CLIENT_BR_ID, AGR_COMMERCE_TYPE, " +
                        //       "AGR_COMMERCE_PLAN, TAX_ID, TAX_INCLUSIVE, RATE_TYPE, " +
                        "DRUM_UNIT_FREE, DRUM_UNIT_CHARGE, AMC_TYPE, FREE_A3_BLACK, FREE_A4_COLOR, " +
                        "FREE_A3_COLOR, A3_RATE_POST_COLOR, A4_RATE_POST_COLOR, " +
                        "A4_RATE_COLOR, A3_RATE_COLOR " +
                        "FROM AGR_PROD WHERE AGR_ID = ? and TERMINATED=0 ORDER BY ID";

        final List<AgrProd> result = new ArrayList<>();

        try (final PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, agrId);

            try (final ResultSet rs = ps.executeQuery()) {

                while (rs.next()) {
                    final AgrProd ap = new AgrProd();

                    ap.setId(rs.getLong("ID"));
                    ap.setProdId(rs.getLong("PROD_ID"));
                    ap.setRent(rs.getLong("RENT"));
                    ap.setFreePrints(rs.getDouble("FREE_PRINTS"));

                    ap.setA3RatePost(rs.getLong("A3_RATE_POST"));
                    ap.setA4RatePost(rs.getDouble("A4_RATE_POST"));
                    ap.setA3Rate(rs.getLong("A3_RATE"));
                    ap.setA4Rate(rs.getDouble("A4_RATE"));

                    ap.setFreeScan(rs.getLong("FREE_SCAN"));
                    ap.setScanRate(rs.getLong("SCAN_RATE"));
                    ap.setScanRatePost(rs.getLong("SCAN_RATE_POST"));

                    ap.setPageCommited(rs.getLong("PAGE_COMMITED"));
                    ap.setBlackCartRate(rs.getLong("BLACK_CART_RATE"));
                    ap.setClrCartRate(rs.getLong("CLR_CART_RATE"));

                    ap.setCartCommited(rs.getLong("CART_COMMITED"));
                    ap.setBillingCommited(rs.getLong("BILLING_COMMITED"));
                    ap.setCommitmentPeriod(rs.getLong("COMMITMENT_PERIOD"));

                    ap.setAmc(rs.getLong("AMC"));
                    ap.setPrinterColor(rs.getLong("PRINTER_COLOR"));
                    ap.setClientBrId(rs.getLong("CLIENT_BR_ID"));
                    ap.setAgrCommerceType(rs.getLong("AGR_COMMERCE_TYPE"));

                    ap.setDrumUnitFree(rs.getLong("DRUM_UNIT_FREE"));
                    ap.setDrumUnitCharge(rs.getLong("DRUM_UNIT_CHARGE"));
                    ap.setAmcType(rs.getLong("AMC_TYPE"));

                    ap.setFreeA3Black(rs.getLong("FREE_A3_BLACK"));
                    ap.setFreeA4Color(rs.getLong("FREE_A4_COLOR"));
                    ap.setFreeA3Color(rs.getLong("FREE_A3_COLOR"));

                    ap.setA3RatePostColor(rs.getLong("A3_RATE_POST_COLOR"));
                    ap.setA4RatePostColor(rs.getLong("A4_RATE_POST_COLOR"));
                    ap.setA4RateColor(rs.getLong("A4_RATE_COLOR"));
                    ap.setA3RateColor(rs.getLong("A3_RATE_COLOR"));

                    result.add(ap);
                }
            }
        }

        return result;
    }

    public List<ReplacementPrinterAgrDTO> getCommercialUsingRequestId(
            final Connection conn,
            final long replacementRequestId
    ) throws SQLException {

        final List<ReplacementPrinterAgrDTO> result = new ArrayList<>();

        try (final PreparedStatement ps = conn.prepareStatement(GET_AGR_BY_PRINTER_USING_REQUEST_ID_SQL)) {

            ps.setLong(1, replacementRequestId);

            try (final ResultSet rs = ps.executeQuery()) {

                while (rs.next()) {

                    // --- AGR_PROD mapping ---
                    final AgrProd ap = new AgrProd();
                    ap.setId(rs.getLong("AP_ID"));
                    ap.setAgrId(rs.getLong("AGR_ID"));
                    ap.setAgrNo(rs.getString("AGR_NO"));
                    ap.setProdId(rs.getLong("PROD_ID"));
                    ap.setRent(rs.getLong("RENT"));
                    ap.setFreePrints(rs.getDouble("FREE_PRINTS"));
                    ap.setA3RatePost(rs.getLong("A3_RATE_POST"));
                    ap.setA4RatePost(rs.getDouble("A4_RATE_POST"));
                    ap.setA3Rate(rs.getLong("A3_RATE"));
                    ap.setA4Rate(rs.getDouble("A4_RATE"));
                    ap.setFreeScan(rs.getLong("FREE_SCAN"));
                    ap.setScanRate(rs.getLong("SCAN_RATE"));
                    ap.setScanRatePost(rs.getLong("SCAN_RATE_POST"));
                    ap.setPageCommited(rs.getLong("PAGE_COMMITED"));
                    ap.setBlackCartRate(rs.getLong("BLACK_CART_RATE"));
                    ap.setClrCartRate(rs.getLong("CLR_CART_RATE"));
                    ap.setCartCommited(rs.getLong("CART_COMMITED"));
                    ap.setBillingCommited(rs.getLong("BILLING_COMMITED"));
                    ap.setCommitmentPeriod(rs.getLong("COMMITMENT_PERIOD"));
                    ap.setAmc(rs.getLong("AMC"));
                    ap.setPrinterColor(rs.getLong("PRINTER_COLOR"));
                    ap.setClientBrId(rs.getLong("CLIENT_BR_ID"));
                    ap.setAgrCommerceType(rs.getLong("AGR_COMMERCE_TYPE"));
                    ap.setDrumUnitFree(rs.getLong("DRUM_UNIT_FREE"));
                    ap.setDrumUnitCharge(rs.getLong("DRUM_UNIT_CHARGE"));
                    ap.setAmcType(rs.getLong("AMC_TYPE"));
                    ap.setFreeA3Black(rs.getLong("FREE_A3_BLACK"));
                    ap.setFreeA4Color(rs.getLong("FREE_A4_COLOR"));
                    ap.setFreeA3Color(rs.getLong("FREE_A3_COLOR"));
                    ap.setA3RatePostColor(rs.getLong("A3_RATE_POST_COLOR"));
                    ap.setA4RatePostColor(rs.getLong("A4_RATE_POST_COLOR"));
                    ap.setA4RateColor(rs.getLong("A4_RATE_COLOR"));
                    ap.setA3RateColor(rs.getLong("A3_RATE_COLOR"));
                    ap.setpModel(rs.getLong("P_MODEL"));

                    // --- DTO mapping ---
                    final ReplacementPrinterAgrDTO dto = new ReplacementPrinterAgrDTO();
                    dto.setReplacementPrinterId(rs.getLong("RPD_ID"));
                    dto.setReplacementRequestId(rs.getLong("REPLACEMENT_REQUEST_ID"));
                    dto.setAgrProdId(rs.getLong("AGR_PROD_ID"));
                    dto.setExistingPModelId(rs.getLong("EXISTING_P_MODEL_ID"));
                    dto.setExistingSerial(rs.getString("EXISTING_SERIAL"));
                    dto.setPrinterType(rs.getString("PRINTER_TYPE"));
                    dto.setPageCount(rs.getLong("PAGE_COUNT"));
                    dto.setPageCountComment(rs.getString("PAGE_COUNT_COMMENT"));
                    dto.setPrinterStageId(rs.getLong("PRINTER_STAGE_ID"));
                    dto.setContinueExistingCommercial(rs.getString("CONTINUE_EXISTING_COMMERCIAL"));
                    dto.setAmCommercialComments(rs.getString("AM_COMMERCIAL_COMMENTS"));
                    dto.setLocation(rs.getString("LOCATION"));
                    dto.setCity(rs.getString("CITY"));
                    dto.setAgrProd(ap);

                    result.add(dto);
                }
            }
        }

        return result;
    }


    public List<AgrProd> fetchAgrProductsByReplacementRequest(
            final Connection conn,
            final long replacementRequestId
    ) throws SQLException {

        final String sql =
                "SELECT ap.ID, ap.AGR_ID, ap.AGR_NO, ap.PROD_ID, ap.RENT, ap.FREE_PRINTS, " +
                        "ap.A3_RATE_POST, ap.A4_RATE_POST, ap.A3_RATE, ap.A4_RATE, ap.FREE_SCAN, " +
                        "ap.SCAN_RATE, ap.SCAN_RATE_POST, ap.PAGE_COMMITED, ap.BLACK_CART_RATE, " +
                        "ap.CLR_CART_RATE, ap.CART_COMMITED, ap.BILLING_COMMITED, " +
                        "ap.COMMITMENT_PERIOD, ap.AMC, ap.PRINTER_COLOR, ap.CLIENT_BR_ID, " +
                        "ap.AGR_COMMERCE_TYPE, ap.TERMINATED, ap.OWNER, ap.SERIAL, " +
                        "ap.DRUM_UNIT_FREE, ap.DRUM_UNIT_CHARGE, ap.AMC_TYPE, " +
                        "ap.FREE_A3_BLACK, ap.FREE_A4_COLOR, ap.FREE_A3_COLOR, " +
                        "ap.A3_RATE_POST_COLOR, ap.A4_RATE_POST_COLOR, " +
                        "ap.A4_RATE_COLOR, ap.A3_RATE_COLOR, ap.P_MODEL " +
                        "FROM AGR_PROD ap " +
                        "JOIN REPLACEMENT_PRINTER_DETAILS rpd " +
                        "  ON rpd.AGR_PROD_ID = ap.ID " +
                        "WHERE ap.TERMINATED=0 and  rpd.REPLACEMENT_REQUEST_ID = ? " +
                        "ORDER BY ap.ID";

        final List<AgrProd> result = new ArrayList<>();

        try (final PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, replacementRequestId);

            try (final ResultSet rs = ps.executeQuery()) {

                while (rs.next()) {
                    final AgrProd ap = new AgrProd();

                    ap.setId(rs.getLong("ID"));
                    ap.setAgrId(rs.getLong("AGR_ID"));
                    ap.setAgrNo(rs.getString("AGR_NO"));
                    ap.setProdId(rs.getLong("PROD_ID"));

                    ap.setRent(rs.getLong("RENT"));
                    ap.setFreePrints(rs.getDouble("FREE_PRINTS"));

                    ap.setA3RatePost(rs.getLong("A3_RATE_POST"));
                    ap.setA4RatePost(rs.getDouble("A4_RATE_POST"));
                    ap.setA3Rate(rs.getLong("A3_RATE"));
                    ap.setA4Rate(rs.getDouble("A4_RATE"));

                    ap.setFreeScan(rs.getLong("FREE_SCAN"));
                    ap.setScanRate(rs.getLong("SCAN_RATE"));
                    ap.setScanRatePost(rs.getLong("SCAN_RATE_POST"));

                    ap.setPageCommited(rs.getLong("PAGE_COMMITED"));
                    ap.setBlackCartRate(rs.getLong("BLACK_CART_RATE"));
                    ap.setClrCartRate(rs.getLong("CLR_CART_RATE"));

                    ap.setCartCommited(rs.getLong("CART_COMMITED"));
                    ap.setBillingCommited(rs.getLong("BILLING_COMMITED"));
                    ap.setCommitmentPeriod(rs.getLong("COMMITMENT_PERIOD"));

                    ap.setAmc(rs.getLong("AMC"));
                    ap.setPrinterColor(rs.getLong("PRINTER_COLOR"));
                    ap.setClientBrId(rs.getLong("CLIENT_BR_ID"));
                    ap.setAgrCommerceType(rs.getLong("AGR_COMMERCE_TYPE"));

                    ap.setTerminated(rs.getLong("TERMINATED"));
                    ap.setOwner(rs.getLong("OWNER"));
                    ap.setSerial(rs.getString("SERIAL"));

                    ap.setDrumUnitFree(rs.getLong("DRUM_UNIT_FREE"));
                    ap.setDrumUnitCharge(rs.getLong("DRUM_UNIT_CHARGE"));
                    ap.setAmcType(rs.getLong("AMC_TYPE"));

                    ap.setFreeA3Black(rs.getLong("FREE_A3_BLACK"));
                    ap.setFreeA4Color(rs.getLong("FREE_A4_COLOR"));
                    ap.setFreeA3Color(rs.getLong("FREE_A3_COLOR"));

                    ap.setA3RatePostColor(rs.getLong("A3_RATE_POST_COLOR"));
                    ap.setA4RatePostColor(rs.getLong("A4_RATE_POST_COLOR"));
                    ap.setA4RateColor(rs.getLong("A4_RATE_COLOR"));
                    ap.setA3RateColor(rs.getLong("A3_RATE_COLOR"));

                    ap.setpModel(rs.getLong("P_MODEL"));

                    result.add(ap);
                }
            }
        }

        return result;
    }


}
