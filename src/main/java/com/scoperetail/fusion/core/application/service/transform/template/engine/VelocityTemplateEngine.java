package com.scoperetail.fusion.core.application.service.transform.template.engine;

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
  public String generateTextFromTemplate(
      final String event, final Map<String, Object> params, final String templateName) {
    final String path =
        USECASES
            + separator
            + event
            + separator
            + TEMPLATES
            + separator
            + VM
            + separator
            + templateName
            + VM_EXTENSION;
    final Template template = velocityEngine.getTemplate(StringUtils.cleanPath(path));
    final VelocityContext context = new VelocityContext();
    params.forEach(context::put);
    final StringWriter writer = new StringWriter();
    template.merge(context, writer);
    final String text = writer.toString();
    log.trace(
        "Generated text for \nEvent: {} \nTemplate: {} \nText: {}", event, templateName, text);
    return text;
  }

  @Override
  public String getTemplateDirBasePath(final String event) {
    return USECASES + separator + event + separator + TEMPLATES + separator + VM;
  }

  @Override
  public String getTemplateFileExtension() {
    return VM;
  }
}
