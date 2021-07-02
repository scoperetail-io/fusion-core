package com.scoperetail.fusion.core.config.plugins.commons;

/*-
 * *****
 * fusion-core
 * -----
 * Copyright (C) 2018 - 2021 Scope Retail Systems Inc.
 * -----
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
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
