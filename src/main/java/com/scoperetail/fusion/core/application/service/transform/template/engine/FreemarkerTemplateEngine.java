package com.scoperetail.fusion.core.application.service.transform.template.engine;

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
    freeMarkerConfigurer.getConfiguration()
        .setTemplateExceptionHandler(TemplateExceptionHandler.IGNORE_HANDLER);
  }

  @Override
  public String generateTextFromTemplate(final String event, final Map<String, Object> params,
      final String templateName) {
    final String path = event + separator + TEMPLATES + separator + FTL + separator
        + templateName + FTL_EXTENSION;

    try {
      final Template template =
          freeMarkerConfigurer.getConfiguration().getTemplate(StringUtils.cleanPath(path));
      final StringWriter writer = new StringWriter();
      template.process(params, writer);
      final String text = writer.toString();
      log.trace("Generated text for \nEvent: {} \nTemplate: {} \nText: {}", event, templateName,
          text);
      return text;
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

}
