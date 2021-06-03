package com.scoperetail.fusion.core.application.service.transform.template.engine;

import java.util.Map;

public interface TemplateEngine {
  String TEMPLATES = "TEMPLATES";

  String generateTextFromTemplate(String event, Map<String, Object> params, String templateName);

}
