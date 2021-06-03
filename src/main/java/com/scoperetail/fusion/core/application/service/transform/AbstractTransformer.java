package com.scoperetail.fusion.core.application.service.transform;

import java.util.Map;
import com.scoperetail.fusion.core.application.service.transform.template.engine.TemplateEngine;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public abstract class AbstractTransformer implements Transformer {

  protected TemplateEngine templateEngine;

  @Override
  public String transform(final String event, final Map<String, Object> params,
      final String template) throws Exception{
    return templateEngine.generateTextFromTemplate(event, params, template);
  }
}
