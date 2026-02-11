package com.ppcl.replacement.scheduler;

import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;
import java.util.logging.Logger;

/**
 * Listener to start/stop the TAT Percentage Scheduler when the application starts/stops.
 */
@WebListener
public class TatSchedulerListener implements ServletContextListener {

    private static final Logger LOGGER = Logger.getLogger(TatSchedulerListener.class.getName());

    @Override
    public void contextInitialized(final ServletContextEvent sce) {
        LOGGER.info("Application starting - initializing TAT Percentage Scheduler");
        TatPercentageScheduler.getInstance().start();
    }

    @Override
    public void contextDestroyed(final ServletContextEvent sce) {
        LOGGER.info("Application stopping - shutting down TAT Percentage Scheduler");
        TatPercentageScheduler.getInstance().stop();
    }
}
