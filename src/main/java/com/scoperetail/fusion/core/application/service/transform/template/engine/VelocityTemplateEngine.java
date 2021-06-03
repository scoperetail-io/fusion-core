package com.scoperetail.fusion.core.application.service.transform.template.engine;

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
