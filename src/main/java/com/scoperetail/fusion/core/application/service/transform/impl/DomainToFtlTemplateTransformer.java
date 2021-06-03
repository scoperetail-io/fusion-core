/* ScopeRetail (C)2021 */
package com.scoperetail.fusion.core.application.service.transform.impl;

import org.springframework.stereotype.Component;
import com.scoperetail.fusion.core.application.service.transform.AbstractTransformer;
import com.scoperetail.fusion.core.application.service.transform.template.engine.FreemarkerTemplateEngine;

@Component
public class DomainToFtlTemplateTransformer extends AbstractTransformer {

  public DomainToFtlTemplateTransformer(FreemarkerTemplateEngine templateEngine) {
    super(templateEngine);
  }
}
