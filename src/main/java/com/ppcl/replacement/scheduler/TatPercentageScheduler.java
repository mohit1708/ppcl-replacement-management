package com.ppcl.replacement.scheduler;

import com.ppcl.replacement.util.DBConnectionPool;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.time.*;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Scheduler to update TAT percentage in RPLCE_FLOW_EVENT_TRACKING table every 30 minutes.
 * <p>
 * Logic:
 * - Only update records where END_AT is NULL (ongoing stages)
 * - If END_AT is not null and TAT_PERCENTAGE is not null, ignore
 * - TAT is calculated based on working hours (Mon-Fri, 09:00-17:00)
 * - 1 day = 8 working hours
 */
public class TatPercentageScheduler {

    private static final Logger LOGGER = Logger.getLogger(TatPercentageScheduler.class.getName());
    private static final ZoneId INDIA_TZ = ZoneId.of("Asia/Kolkata");
    private static final LocalTime WORK_START = LocalTime.of(9, 0);
    private static final LocalTime WORK_END = LocalTime.of(17, 0);

    private ScheduledExecutorService scheduler;
    private static TatPercentageScheduler instance;

    private TatPercentageScheduler() {
    }

    public static synchronized TatPercentageScheduler getInstance() {
        if (instance == null) {
            instance = new TatPercentageScheduler();
        }
        return instance;
    }

    /**
     * Start the scheduler to run every 30 minutes
     */
    public void start() {
        if (scheduler != null && !scheduler.isShutdown()) {
            LOGGER.info("TAT Percentage Scheduler is already running");
            return;
        }

        scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
            final Thread t = new Thread(r, "TatPercentageScheduler");
            t.setDaemon(true);
            return t;
        });

        // Run immediately, then every 30 minutes
        scheduler.scheduleAtFixedRate(this::updateTatPercentages, 0, 30, TimeUnit.MINUTES);
        LOGGER.info("TAT Percentage Scheduler started - runs every 30 minutes");
    }

    /**
     * Stop the scheduler
     */
    public void stop() {
        if (scheduler != null && !scheduler.isShutdown()) {
            scheduler.shutdown();
            try {
                if (!scheduler.awaitTermination(10, TimeUnit.SECONDS)) {
                    scheduler.shutdownNow();
                }
            } catch (final InterruptedException e) {
                scheduler.shutdownNow();
                Thread.currentThread().interrupt();
            }
            LOGGER.info("TAT Percentage Scheduler stopped");
        }
    }

    /**
     * Update TAT percentages for all open events (END_AT is null)
     */
    private void updateTatPercentages() {
        LOGGER.info("Starting TAT percentage update...");
        int updatedCount = 0;
        int errorCount = 0;

        // Update TAT% for:
        // 1. Records where END_AT IS NULL (ongoing stages) - always update
        // 2. Records where END_AT IS NOT NULL but TAT_PERCENTAGE IS NULL (completed but not calculated)
        final String selectSql = """
                SELECT e.ID, e.START_AT, e.END_AT, e.CURRENT_STAGE_ID, 
                       t.TAT_DURATION, t.TAT_DURATION_UNIT
                FROM RPLCE_FLOW_EVENT_TRACKING e
                JOIN TAT_MASTER t ON e.CURRENT_STAGE_ID = t.ID
                WHERE e.END_AT IS NULL 
                   OR (e.END_AT IS NOT NULL AND e.TAT_PERCENTAGE IS NULL)
                """;

        final String updateSql = """
                UPDATE RPLCE_FLOW_EVENT_TRACKING 
                SET TAT_PERCENTAGE = ? 
                WHERE ID = ?
                """;

        try (final Connection con = DBConnectionPool.getConnection()) {
            con.setAutoCommit(false);

            try (final PreparedStatement selectPs = con.prepareStatement(selectSql);
                 final PreparedStatement updatePs = con.prepareStatement(updateSql);
                 final ResultSet rs = selectPs.executeQuery()) {

                final LocalDateTime now = LocalDateTime.now(INDIA_TZ);

                while (rs.next()) {
                    try {
                        final int eventId = rs.getInt("ID");
                        final Timestamp startAt = rs.getTimestamp("START_AT");
                        final Timestamp endAt = rs.getTimestamp("END_AT");
                        final int tatDuration = rs.getInt("TAT_DURATION");
                        final String tatUnit = rs.getString("TAT_DURATION_UNIT");

                        if (startAt == null) {
                            continue;
                        }

                        final LocalDateTime startTime = startAt.toInstant()
                                .atZone(INDIA_TZ)
                                .toLocalDateTime();

                        // Use END_AT if available (completed stage), otherwise use current time
                        final LocalDateTime endTime = (endAt != null)
                                ? endAt.toInstant().atZone(INDIA_TZ).toLocalDateTime()
                                : now;

                        // Calculate working minutes elapsed
                        final long actualMinutes = calculateWorkingMinutes(startTime, endTime);

                        // Calculate TAT in minutes (1 day = 8 hours)
                        final String unit = (tatUnit != null) ? tatUnit : "DAYS";
                        final long tatMinutes = "HOURS".equalsIgnoreCase(unit)
                                ? tatDuration * 60L
                                : tatDuration * 8L * 60L;

                        // Calculate percentage
                        double percentage = tatMinutes > 0
                                ? (actualMinutes * 100.0 / tatMinutes)
                                : 0.0;

                        // Round to 2 decimal places
                        percentage = Math.round(percentage * 100.0) / 100.0;

                        // Update the record
                        updatePs.setDouble(1, percentage);
                        updatePs.setInt(2, eventId);
                        updatePs.executeUpdate();
                        updatedCount++;

                    } catch (final Exception e) {
                        LOGGER.log(Level.WARNING, "Error updating event ID: " + rs.getInt("ID"), e);
                        errorCount++;
                    }
                }

                con.commit();
                LOGGER.info("TAT percentage update completed. Updated: " + updatedCount + ", Errors: " + errorCount);

            } catch (final Exception e) {
                con.rollback();
                throw e;
            }

        } catch (final Exception e) {
            LOGGER.log(Level.SEVERE, "Error during TAT percentage update", e);
        }
    }

    /**
     * Calculate working minutes between two LocalDateTime instances.
     * Working hours: Mon-Fri, 09:00-17:00 (8 hours/day)
     */
    private long calculateWorkingMinutes(final LocalDateTime start, final LocalDateTime end) {
        if (start == null || end == null || end.isBefore(start)) {
            return 0;
        }

        long minutes = 0;
        LocalDate currentDate = start.toLocalDate();
        final LocalDate endDate = end.toLocalDate();

        while (!currentDate.isAfter(endDate)) {
            final DayOfWeek dow = currentDate.getDayOfWeek();
            final boolean isWorkingDay = dow != DayOfWeek.SATURDAY && dow != DayOfWeek.SUNDAY;

            if (isWorkingDay) {
                final LocalDateTime dayStart = LocalDateTime.of(currentDate, WORK_START);
                final LocalDateTime dayEnd = LocalDateTime.of(currentDate, WORK_END);

                final LocalDateTime from = start.isAfter(dayStart) ? start : dayStart;
                final LocalDateTime to = end.isBefore(dayEnd) ? end : dayEnd;

                if (to.isAfter(from)) {
                    minutes += ChronoUnit.MINUTES.between(from, to);
                }
            }
            currentDate = currentDate.plusDays(1);
        }

        return minutes;
    }
}
