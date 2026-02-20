package com.ppcl.replacement.model;

import java.time.LocalDate;
import java.util.Map;
import java.util.Set;

public record HolidayCalendar(
            Set<LocalDate> fullDayHolidays,
            Map<LocalDate, WorkingWindow> halfDayHolidays
    ) {}