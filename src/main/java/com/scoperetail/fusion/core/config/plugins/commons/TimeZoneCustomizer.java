package com.scoperetail.fusion.core.config.plugins.commons;

/*-
 * *****
 * fusion-core
 * -----
 * Copyright (C) 2018 - 2021 Scope Retail Systems Inc.
 * -----
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * =====
 */

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
