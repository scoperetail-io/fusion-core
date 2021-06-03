package com.scoperetail.fusion.core.config.plugins.commons;

import java.util.Collections;
import java.util.Map;
import org.apache.velocity.tools.generic.DateTool;

public final class DateToolCustomizer {
  private static final String DATE_TOOL = "DATE_TOOL";

  private DateToolCustomizer() {}

  public static Map<String, Object> getDateParams() {
    return Collections.singletonMap(DATE_TOOL, new DateTool());
  }
}
