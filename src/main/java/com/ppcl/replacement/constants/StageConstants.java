package com.ppcl.replacement.constants;

import java.util.List;
import java.util.Arrays;

public class StageConstants {
    public static final String REQUEST_RAISED = "REQUEST_RAISED";
    public static final String SERVICE_TL_REVIEW = "SERVICE_TL_REVIEW";
    public static final String AM_MANAGER_REVIEW = "AM_MANAGER_REVIEW";
    public static final String QUOTATION_PENDING = "QUOTATION_PENDING";
    public static final String QUOTATION_SENT = "QUOTATION_SENT";
    public static final String PRINTER_BOOKING = "PRINTER_BOOKING";
    public static final String LETTER_GENERATION = "LETTER_GENERATION";
    public static final String INSTALLATION = "INSTALLATION";
    public static final String PULLBACK = "PULLBACK";
    public static final String QC_VERIFICATION = "QC_VERIFICATION";
    public static final String CREDIT_NOTE = "CREDIT_NOTE";
    public static final String CLOSED = "CLOSED";
    public static final String REJECTED = "REJECTED";

    // Example stage codes – align with what you actually save in REPLREQHDR.CURRENTSTAGE
    public static final List<String> STAGE_FLOW = Arrays.asList(
            "STG1_REQUEST_RAISED",
            "STG2_SERVICE_TL_REVIEW",
            "STG3_AM_MANAGER_REVIEW",
            "STG4_AM_COMMERCIAL",
            "STG5_AM_MANAGER_FINAL",
            "STG6_PRINTER_ORDER",
            "STG7_DISPATCH_LETTER",
            "STG8_INSTALLATION",
            "STG9_PULLBACK",
            "STG10_QC",
            "STG11_CREDIT_NOTE",
            "STG12_CLOSURE"
    );

}
