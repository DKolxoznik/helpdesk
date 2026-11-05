package ru.tushkov.helpdesk.support;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.concurrent.atomic.AtomicReference;

public final class MutableClock extends Clock {

    private final AtomicReference<Instant> current = new AtomicReference<>();
    private final ZoneId zone = ZoneOffset.UTC;

    public MutableClock(Instant initial) {
        current.set(initial);
    }

    public void set(Instant instant) {
        current.set(instant);
    }

    public void plus(Duration duration) {
        current.updateAndGet(value -> value.plus(duration));
    }

    @Override
    public ZoneId getZone() {
        return zone;
    }

    @Override
    public Clock withZone(ZoneId zone) {
        return this;
    }

    @Override
    public Instant instant() {
        return current.get();
    }
}
