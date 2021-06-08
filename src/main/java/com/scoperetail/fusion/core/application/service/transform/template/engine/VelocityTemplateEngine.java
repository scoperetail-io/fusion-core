package com.scoperetail.fusion.core.application.service.transform.template.engine;

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

import static java.io.File.separator;
import java.io.StringWriter;
import java.util.Map;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@AllArgsConstructor
@Slf4j
public class VelocityTemplateEngine implements TemplateEngine {
  private static final String USECASES = "usecases";
  private static final String VM = "vm";
  private static final String VM_EXTENSION = ".vm";
  private final VelocityEngine velocityEngine;

  @Override
  public String generateTextFromTemplate(String event, Map<String, Object> params,
      String templateName) {
    final String path = USECASES + separator + event + separator + TEMPLATES + separator + VM
        + separator + templateName + VM_EXTENSION;
    final Template template = velocityEngine.getTemplate(StringUtils.cleanPath(path));
    final VelocityContext context = new VelocityContext();
    params.forEach(context::put);
    final StringWriter writer = new StringWriter();
    template.merge(context, writer);
    final String text = writer.toString();
    log.trace("Generated text for \nEvent: {} \nTemplate: {} \nText: {}", event, templateName,
        text);
    return text;
  }

}
