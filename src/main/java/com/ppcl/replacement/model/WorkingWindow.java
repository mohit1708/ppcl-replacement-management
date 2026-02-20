package com.ppcl.replacement.model;

import java.time.LocalTime;

public record WorkingWindow(
            LocalTime start,
            LocalTime end
    ) {}