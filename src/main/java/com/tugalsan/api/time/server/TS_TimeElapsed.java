package com.tugalsan.api.time.server;

import java.time.Duration;
import java.time.Instant;

public class TS_TimeElapsed {

    private TS_TimeElapsed() {
        start = Instant.now();
    }
    public Instant start;

    public static TS_TimeElapsed of() {
        return new TS_TimeElapsed();
    }

    public TS_TimeElapsed restart() {
        start = Instant.now();
        return this;
    }

    public Duration elapsed_now() {
        return Duration.between(start, Instant.now());
    }
}
