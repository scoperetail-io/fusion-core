package com.scoperetail.fusion.core.config.plugins.commons;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import com.scoperetail.commons.time.util.DateTimeZoneUtil;

public final class TimeZoneCustomizer {
  private static final String GMT = "GMT";
  private static final String DATE_FORMAT = "yyyy-MM-dd HH:mm:ss.SSS";

  public TimeZoneCustomizer() {}

  public String toTimeZone(final String timezone, final String dateTime) {
    ZoneId fromZone = ZoneId.of(GMT);
    ZoneId toZone = ZoneId.of(timezone.trim());
    DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern(DATE_FORMAT);
    LocalDateTime result = DateTimeZoneUtil
        .toLocalDateTime(LocalDateTime.parse(dateTime, dateTimeFormatter), fromZone, toZone);
    return result.format(dateTimeFormatter);
  }
}
