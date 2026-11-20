package com.edumy.shared.time;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.ZoneId;

public final class TimeUtils {

    private static final String DEFAULT_TIME_ZONE = "Asia/Ho_Chi_Minh";

    private TimeUtils() {}

    public static ZoneId resolveZone(String timeZone) {
        return ZoneId.of(timeZone != null && !timeZone.isBlank() ? timeZone : DEFAULT_TIME_ZONE);
    }

    public static LocalDateTime now(String timeZone) {
        return LocalDateTime.now(resolveZone(timeZone));
    }

    public static LocalDateTime startOfCurrentMonth(String timeZone) {
        return YearMonth.now(resolveZone(timeZone)).atDay(1).atStartOfDay();
    }

    public static LocalDateTime startOfNextMonth(String timeZone) {
        return YearMonth.now(resolveZone(timeZone)).plusMonths(1).atDay(1).atStartOfDay();
    }

    public static Instant toInstant(LocalDateTime ldt, String timeZone) {
        if (ldt == null) return null;
        return ldt.atZone(resolveZone(timeZone)).toInstant();
    }
}
