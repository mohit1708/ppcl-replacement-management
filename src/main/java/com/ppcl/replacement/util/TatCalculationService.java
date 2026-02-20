package com.ppcl.replacement.util;

import com.ppcl.replacement.model.HolidayCalendar;
import com.ppcl.replacement.model.WorkingWindow;

import java.sql.*;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

public class TatCalculationService {

    private static final Logger LOGGER = Logger.getLogger(TatCalculationService.class.getName());

    private static final DateTimeFormatter DB_DATE_FORMAT =
            new DateTimeFormatterBuilder()
                    .appendPattern("[uuuu-MM-dd][dd-MM-uuuu][d-M-uuuu]")
                    .toFormatter(Locale.ROOT);

    private static final DateTimeFormatter DB_TIME_FORMAT =
            DateTimeFormatter.ofPattern("H:mm[:ss]", Locale.ROOT);

    /**
     * Loads holidays from ATTND_HOLIDAYS table.
     * TYPE = 1 → Holiday entries only.
     * Skips rows with bad/unparseable data instead of aborting the entire load.
     */
    public static HolidayCalendar loadHolidayCalendar(Connection con) throws SQLException {

        Set<LocalDate> fullDayHolidays = new HashSet<>();
        Map<LocalDate, WorkingWindow> halfDayHolidays = new HashMap<>();

        String sql = """
                SELECT HOLIDAY_DATE, HOLIDAY_TYPE, IN_TIME, OUT_TIME
                FROM ATTND_HOLIDAYS
                WHERE TYPE = 1
                """;

        try (PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                try {
                    String dateStr = rs.getString("HOLIDAY_DATE");
                    if (dateStr == null || dateStr.trim().isEmpty()) {
                        continue;
                    }

                    LocalDate date = LocalDate.parse(dateStr.trim(), DB_DATE_FORMAT);
                    int holidayType = rs.getInt("HOLIDAY_TYPE");

                    if (holidayType == 1) {
                        fullDayHolidays.add(date);

                    } else if (holidayType == 2) {
                        String inTimeStr = rs.getString("IN_TIME");
                        String outTimeStr = rs.getString("OUT_TIME");

                        if (inTimeStr == null || inTimeStr.trim().isEmpty()
                                || outTimeStr == null || outTimeStr.trim().isEmpty()) {
                            // Missing half-day times — treat as full-day holiday (conservative)
                            fullDayHolidays.add(date);
                            LOGGER.warning("Half-day holiday " + dateStr + " missing IN_TIME/OUT_TIME, treating as full-day");
                            continue;
                        }

                        LocalTime in = LocalTime.parse(inTimeStr.trim(), DB_TIME_FORMAT);
                        LocalTime out = LocalTime.parse(outTimeStr.trim(), DB_TIME_FORMAT);

                        if (!in.isBefore(out)) {
                            // Invalid window (in >= out) — treat as full-day holiday
                            fullDayHolidays.add(date);
                            LOGGER.warning("Half-day holiday " + dateStr + " has invalid time window (" + inTimeStr + " - " + outTimeStr + "), treating as full-day");
                            continue;
                        }

                        halfDayHolidays.put(date, new WorkingWindow(in, out));
                    }
                } catch (DateTimeParseException e) {
                    LOGGER.log(Level.WARNING, "Skipping holiday row with unparseable date/time", e);
                }
            }
        }

        return new HolidayCalendar(fullDayHolidays, halfDayHolidays);
    }

    /**
     * Calculate working minutes excluding Sundays & holidays.
     * Delegates to DateUtil.
     */
    public static long workingMinutesBetween(
            Date start,
            Date end,
            ZoneId zone,
            HolidayCalendar holidayCalendar) {
        return DateUtil.workingMinutesBetween(start, end, zone, holidayCalendar);
    }

    /**
     * Calculate TAT percentage. Delegates to DateUtil.
     */
    public static double calculateTatPercentage(
            Date stageStart,
            Date now,
            int tatDuration,
            String tatUnit,
            HolidayCalendar calendar) {
        return DateUtil.calculateTatPercentage(stageStart, now, tatDuration, tatUnit, calendar);
    }

    /**
     * TAT Status. Delegates to DateUtil.
     */
    public static String getTatStatus(double percentage) {
        return DateUtil.getTatStatus(percentage);
    }

}
