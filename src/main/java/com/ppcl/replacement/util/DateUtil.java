package com.ppcl.replacement.util;

import java.text.SimpleDateFormat;
import java.time.*;
import java.time.temporal.ChronoUnit;
import java.util.Date;

public class DateUtil {
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("dd-MMM-yyyy");
    private static final SimpleDateFormat DATETIME_FORMAT = new SimpleDateFormat("dd-MMM-yyyy HH:mm:ss");

    private static final LocalTime WORK_START = LocalTime.of(9, 0);
    private static final LocalTime WORK_END = LocalTime.of(17, 0);

    public static String formatDate(final Date date) {
        if (date == null) return "";
        return DATE_FORMAT.format(date);
    }

    public static String formatDateTime(final Date date) {
        if (date == null) return "";
        return DATETIME_FORMAT.format(date);
    }

    public static String getCurrentDate() {
        return DATE_FORMAT.format(new Date());
    }

    public static String getCurrentDateTime() {
        return DATETIME_FORMAT.format(new Date());
    }

    /**
     * Calculate working minutes between two dates.
     * Working hours: Mon-Fri, 09:00-17:00 (8 hours/day)
     */
    public static long workingMinutesBetween(final Date start, final Date end, final ZoneId zone) {
        if (start == null || end == null) return 0;

        final Instant s = start.toInstant();
        final Instant e = end.toInstant();
        if (e.isBefore(s)) return 0;

        final LocalDateTime startDt = LocalDateTime.ofInstant(s, zone);
        final LocalDateTime endDt = LocalDateTime.ofInstant(e, zone);

        long minutes = 0;
        LocalDate d = startDt.toLocalDate();

        while (!d.isAfter(endDt.toLocalDate())) {
            final DayOfWeek dow = d.getDayOfWeek();
            final boolean isWorkingDay = dow != DayOfWeek.SATURDAY && dow != DayOfWeek.SUNDAY;

            if (isWorkingDay) {
                final LocalDateTime dayStart = LocalDateTime.of(d, WORK_START);
                final LocalDateTime dayEnd = LocalDateTime.of(d, WORK_END);

                final LocalDateTime from = startDt.isAfter(dayStart) ? startDt : dayStart;
                final LocalDateTime to = endDt.isBefore(dayEnd) ? endDt : dayEnd;

                if (to.isAfter(from)) {
                    minutes += ChronoUnit.MINUTES.between(from, to);
                }
            }
            d = d.plusDays(1);
        }
        return minutes;
    }

    /**
     * Calculate TAT percentage based on working hours.
     *
     * @param stageStart  When stage started
     * @param currentTime Current time
     * @param tatDuration TAT duration value
     * @param tatUnit     TAT unit (HOURS or DAYS, default DAYS = 8 working hours)
     */
    public static double calculateTatPercentage(final Date stageStart, final Date currentTime, final int tatDuration, final String tatUnit) {
        if (stageStart == null || tatDuration <= 0) return 0.0;

        final Date now = currentTime != null ? currentTime : new Date();
        final ZoneId zone = ZoneId.systemDefault();

        final long actualMinutes = workingMinutesBetween(stageStart, now, zone);

        final String unit = tatUnit != null ? tatUnit : "DAYS";
        final long tatMinutes = "HOURS".equalsIgnoreCase(unit)
                ? tatDuration * 60L
                : tatDuration * 8L * 60L;  // 1 day = 8 working hours

        return tatMinutes > 0 ? (actualMinutes * 100.0 / tatMinutes) : 0.0;
    }

    /**
     * Get TAT status based on percentage.
     */
    public static String getTatStatus(final double percentage) {
        if (percentage >= 100) return "BREACH";
        if (percentage >= 80) return "WARNING";
        return "WITHIN";
    }
}
