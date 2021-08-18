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
import javax.annotation.PostConstruct;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.view.freemarker.FreeMarkerConfigurer;
import freemarker.template.Template;
import freemarker.template.TemplateExceptionHandler;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@AllArgsConstructor
@Slf4j
public class FreemarkerTemplateEngine implements TemplateEngine {

  private static final String FTL = "ftl";
  private static final String FTL_EXTENSION = ".ftl";

  private final FreeMarkerConfigurer freeMarkerConfigurer;

  @PostConstruct
  private void init() {
    freeMarkerConfigurer.getConfiguration().setNumberFormat("computer");
    freeMarkerConfigurer
        .getConfiguration()
        .setTemplateExceptionHandler(TemplateExceptionHandler.IGNORE_HANDLER);
  }

  @Override
  public String generateTextFromTemplate(
      final String event, final Map<String, Object> params, final String templateName) {
    final String path =
        event + separator + TEMPLATES + separator + FTL + separator + templateName + FTL_EXTENSION;

    try {
      final Template template =
          freeMarkerConfigurer.getConfiguration().getTemplate(StringUtils.cleanPath(path));
      final StringWriter writer = new StringWriter();
      template.process(params, writer);
      final String text = writer.toString();
      log.trace(
          "Generated text for \nEvent: {} \nTemplate: {} \nText: {}", event, templateName, text);
      return text;
    } catch (final Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public String getTemplateDirBasePath(final String event) {
    return "usecases" + separator + event + separator + TEMPLATES + separator + FTL;
  }

  @Override
  public String getTemplateFileExtension() {
    return FTL_EXTENSION;
  }
}
