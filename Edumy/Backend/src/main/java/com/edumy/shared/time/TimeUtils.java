package com.edumy.shared.time;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

public final class TimeUtils {

    private TimeUtils() {}

    public static Instant toInstant(LocalDateTime ldt, String timeZone) {
        if (ldt == null) return null;
        ZoneId zone = ZoneId.of(timeZone != null && !timeZone.isBlank() ? timeZone : "Asia/Ho_Chi_Minh");
        return ldt.atZone(zone).toInstant();
    }
}
