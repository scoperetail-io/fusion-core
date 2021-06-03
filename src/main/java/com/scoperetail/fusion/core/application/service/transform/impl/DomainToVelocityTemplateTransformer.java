/* ScopeRetail (C)2021 */
package com.scoperetail.fusion.core.application.service.transform.impl;

import org.springframework.stereotype.Component;
import com.scoperetail.fusion.core.application.service.transform.AbstractTransformer;
import com.scoperetail.fusion.core.application.service.transform.template.engine.VelocityTemplateEngine;

@Component
public class DomainToVelocityTemplateTransformer extends AbstractTransformer {
  public DomainToVelocityTemplateTransformer(final VelocityTemplateEngine templateEngine) {
    super(templateEngine);
  }
}
