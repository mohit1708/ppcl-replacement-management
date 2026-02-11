package com.ppcl.replacement.constants;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class MessageConstant {

    public static final String NEW_REPLACEMENT_REQUEST_MESSAGE = "New replacement Request has been raised for Client %s";
    public static final String COMMENT_FOR_PRINTER_BOOK_STAGE = "Book Printer has been raised for Client %s and PO Order %s";

    // Default system-generated comments when user does not provide comments
    public static final String DEFAULT_COMMENT_REQUEST_CREATED = "System Generated: Request Created for Client %s";
    public static final String DEFAULT_COMMENT_TL_RECOMMENDATION = "System Generated: Recommendation Provided for Client %s";
    public static final String DEFAULT_COMMENT_AM_COMMERCIAL_APPROVED = "System Generated: Commercials Approved for Client %s";
    public static final String DEFAULT_COMMENT_PRINTER_BOOKED = "System Generated: Printer has been booked for Client %s on date %s";

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd-MMM-yyyy");

    public static String formatDefaultRequestCreated(final String clientName) {
        return String.format(DEFAULT_COMMENT_REQUEST_CREATED, clientName != null ? clientName : "Unknown");
    }

    public static String formatDefaultTLRecommendation(final String clientName) {
        return String.format(DEFAULT_COMMENT_TL_RECOMMENDATION, clientName != null ? clientName : "Unknown");
    }

    public static String formatDefaultAMCommercialApproved(final String clientName) {
        return String.format(DEFAULT_COMMENT_AM_COMMERCIAL_APPROVED, clientName != null ? clientName : "Unknown");
    }

    public static String formatDefaultPrinterBooked(final String clientName) {
        final String date = LocalDate.now().format(DATE_FORMATTER);
        return String.format(DEFAULT_COMMENT_PRINTER_BOOKED, clientName != null ? clientName : "Unknown", date);
    }
}
