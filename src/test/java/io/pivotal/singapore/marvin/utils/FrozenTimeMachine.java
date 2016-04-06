package io.pivotal.singapore.marvin.utils;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;

public class FrozenTimeMachine extends Clock {
    private Instant _instant = Instant.now();

    @Override
    public ZoneId getZone() {
        return ZoneId.of("UTC");
    }

    @Override
    public Clock withZone(ZoneId zone) {
        return null;
    }

    @Override
    public Instant instant() {
        return _instant;
    }
}
