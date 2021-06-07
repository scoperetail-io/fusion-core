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
