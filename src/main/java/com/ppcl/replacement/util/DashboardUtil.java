package com.ppcl.replacement.util;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Date;
import java.util.Locale;

/**
 * Utility class for Dashboard operations
 * Contains helper methods for date parsing, TAT status calculation, and formatting
 */
public class DashboardUtil {

    // Date format patterns
    public static final String DATE_FORMAT_INPUT = "yyyy-MM-dd";
    public static final String DATE_FORMAT_DISPLAY = "dd MMM yyyy";
    public static final String DATE_FORMAT_DISPLAY_SHORT = "dd-MMM-yy";
    public static final String DATETIME_FORMAT_DISPLAY = "dd MMM yyyy HH:mm";
    public static final String TIMESTAMP_FORMAT_DB = "yyyy-MM-dd HH:mm:ss.SSS";

    // TAT Status constants
    public static final String TAT_STATUS_WITHIN = "Within TAT";
    public static final String TAT_STATUS_BEYOND = "Beyond TAT";
    public static final BigDecimal TAT_THRESHOLD = BigDecimal.valueOf(100);

    // CSS Badge classes
    public static final String BADGE_SUCCESS = "badge-success";
    public static final String BADGE_DANGER = "badge-danger";
    public static final String BADGE_WARNING = "badge-warning";
    public static final String BADGE_INFO = "badge-info";
    public static final String BADGE_SECONDARY = "badge-secondary";
    public static final String BADGE_PRIMARY = "badge-primary";

    // =====================================================
    // TAT STATUS METHODS
    // =====================================================

    /**
     * Get TAT status text from percentage
     *
     * @param percentage TAT percentage value
     * @return "Within TAT" if < 100, "Beyond TAT" if >= 100
     */
    public static String getTatStatus(final BigDecimal percentage) {
        if (percentage == null) {
            return TAT_STATUS_WITHIN; // Default to within if not calculated
        }
        return percentage.compareTo(TAT_THRESHOLD) >= 0 ? TAT_STATUS_BEYOND : TAT_STATUS_WITHIN;
    }

    /**
     * Get TAT status text from integer percentage
     */
    public static String getTatStatus(final int percentage) {
        return getTatStatus(BigDecimal.valueOf(percentage));
    }

    /**
     * Check if TAT is breached
     */
    public static boolean isTatBreached(final BigDecimal percentage) {
        return percentage != null && percentage.compareTo(TAT_THRESHOLD) >= 0;
    }

    /**
     * Get CSS class for TAT status badge
     */
    public static String getTatBadgeClass(final BigDecimal percentage) {
        return isTatBreached(percentage) ? BADGE_DANGER : BADGE_SUCCESS;
    }

    /**
     * Get progress bar color based on TAT percentage
     *
     * @param percentage TAT percentage
     * @return Bootstrap color class (success, warning, danger)
     */
    public static String getProgressBarColor(final BigDecimal percentage) {
        if (percentage == null) {
            return "success";
        }
        final int pct = percentage.intValue();
        if (pct < 50) {
            return "success";
        } else if (pct < 80) {
            return "info";
        } else if (pct < 100) {
            return "warning";
        } else {
            return "danger";
        }
    }

    /**
     * Calculate TAT percentage display value (capped at 100 for progress bar)
     */
    public static int getTatProgressValue(final BigDecimal percentage) {
        if (percentage == null) {
            return 0;
        }
        return Math.min(percentage.intValue(), 100);
    }

    // =====================================================
    // STATUS BADGE METHODS
    // =====================================================

    /**
     * Get CSS badge class for request status
     */
    public static String getStatusBadgeClass(final String status) {
        if (status == null || status.isEmpty()) {
            return BADGE_SECONDARY;
        }

        switch (status.toUpperCase()) {
            case "OPEN":
                return BADGE_INFO;
            case "PENDING":
                return BADGE_WARNING;
            case "COMPLETED":
                return BADGE_SUCCESS;
            case "REJECTED":
                return BADGE_DANGER;
            default:
                return BADGE_SECONDARY;
        }
    }

    /**
     * Get friendly display name for status
     */
    public static String getStatusDisplayName(final String status) {
        if (status == null || status.isEmpty()) {
            return "Unknown";
        }

        switch (status.toUpperCase()) {
            case "OPEN":
                return "Open";
            case "PENDING":
                return "Pending Approval";
            case "COMPLETED":
                return "Completed";
            case "REJECTED":
                return "Rejected";
            default:
                return status;
        }
    }

    // =====================================================
    // DATE METHODS
    // =====================================================

    /**
     * Parse date string to Date object
     */
    public static Date parseDate(final String dateStr, final String pattern) {
        if (dateStr == null || dateStr.isEmpty()) {
            return null;
        }
        try {
            final SimpleDateFormat sdf = new SimpleDateFormat(pattern);
            return sdf.parse(dateStr);
        } catch (final ParseException e) {
            return null;
        }
    }

    /**
     * Parse input date string (yyyy-MM-dd)
     */
    public static Date parseInputDate(final String dateStr) {
        return parseDate(dateStr, DATE_FORMAT_INPUT);
    }

    /**
     * Format date for display
     */
    public static String formatDate(final Date date, final String pattern) {
        if (date == null) {
            return "";
        }
        final SimpleDateFormat sdf = new SimpleDateFormat(pattern);
        return sdf.format(date);
    }

    /**
     * Format date for display (dd MMM yyyy)
     */
    public static String formatDisplayDate(final Date date) {
        return formatDate(date, DATE_FORMAT_DISPLAY);
    }

    /**
     * Format datetime for display (dd MMM yyyy HH:mm)
     */
    public static String formatDisplayDateTime(final Date date) {
        return formatDate(date, DATETIME_FORMAT_DISPLAY);
    }

    /**
     * Get default date range start (10 days ago)
     */
    public static String getDefaultFromDate() {
        final LocalDate fromDate = LocalDate.now().minusDays(10);
        return fromDate.format(DateTimeFormatter.ofPattern(DATE_FORMAT_INPUT));
    }

    /**
     * Get default date range end (today)
     */
    public static String getDefaultToDate() {
        return LocalDate.now().format(DateTimeFormatter.ofPattern(DATE_FORMAT_INPUT));
    }

    /**
     * Validate date string format
     */
    public static boolean isValidDate(final String dateStr) {
        if (dateStr == null || dateStr.isEmpty()) {
            return false;
        }
        try {
            LocalDate.parse(dateStr, DateTimeFormatter.ofPattern(DATE_FORMAT_INPUT));
            return true;
        } catch (final DateTimeParseException e) {
            return false;
        }
    }

    // =====================================================
    // NUMBER FORMATTING METHODS
    // =====================================================

    /**
     * Format number with Indian number system
     */
    public static String formatIndianNumber(final int number) {
        final NumberFormat nf = NumberFormat.getInstance(new Locale("en", "IN"));
        return nf.format(number);
    }

    /**
     * Format number with comma separators
     */
    public static String formatNumber(final int number) {
        return NumberFormat.getInstance().format(number);
    }

    /**
     * Format percentage with one decimal place
     */
    public static String formatPercentage(final BigDecimal percentage) {
        if (percentage == null) {
            return "0%";
        }
        return String.format("%.1f%%", percentage.doubleValue());
    }

    // =====================================================
    // STRING UTILITY METHODS
    // =====================================================

    /**
     * Safe null check for string
     */
    public static String nvl(final String value, final String defaultValue) {
        return (value == null || value.isEmpty()) ? defaultValue : value;
    }

    /**
     * Truncate string to max length with ellipsis
     */
    public static String truncate(final String str, final int maxLength) {
        if (str == null || str.length() <= maxLength) {
            return str;
        }
        return str.substring(0, maxLength - 3) + "...";
    }

    /**
     * Generate display ID for replacement request
     */
    public static String generateDisplayId(final int id, final Date creationDate) {
        if (creationDate == null) {
            return "REQ-" + String.format("%04d", id);
        }
        final SimpleDateFormat yearFormat = new SimpleDateFormat("yyyy");
        final String year = yearFormat.format(creationDate);
        return String.format("REQ-%s-%04d", year, id);
    }

    // =====================================================
    // PAGINATION METHODS
    // =====================================================

    /**
     * Calculate total pages
     */
    public static int getTotalPages(final int totalCount, final int pageSize) {
        if (pageSize <= 0) {
            return 0;
        }
        return (int) Math.ceil((double) totalCount / pageSize);
    }

    /**
     * Validate and adjust page number
     */
    public static int validatePage(final int page, final int totalPages) {
        if (page < 1) {
            return 1;
        }
        if (totalPages > 0 && page > totalPages) {
            return totalPages;
        }
        return page;
    }

    /**
     * Get start record number for display
     */
    public static int getStartRecord(final int page, final int pageSize) {
        return ((page - 1) * pageSize) + 1;
    }

    /**
     * Get end record number for display
     */
    public static int getEndRecord(final int page, final int pageSize, final int totalCount) {
        final int end = page * pageSize;
        return Math.min(end, totalCount);
    }

    // =====================================================
    // SECURITY METHODS
    // =====================================================

    /**
     * Sanitize string for HTML output (basic XSS prevention)
     */
    public static String escapeHtml(final String input) {
        if (input == null) {
            return "";
        }
        return input.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#x27;");
    }

    /**
     * Parse integer safely
     */
    public static Integer parseIntSafe(final String value) {
        if (value == null || value.isEmpty()) {
            return null;
        }
        try {
            return Integer.parseInt(value.trim());
        } catch (final NumberFormatException e) {
            return null;
        }
    }

    /**
     * Parse integer with default value
     */
    public static int parseIntWithDefault(final String value, final int defaultValue) {
        final Integer parsed = parseIntSafe(value);
        return parsed != null ? parsed : defaultValue;
    }
}

