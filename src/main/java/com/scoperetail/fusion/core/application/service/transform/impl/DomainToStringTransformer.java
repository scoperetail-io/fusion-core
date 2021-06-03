/* ScopeRetail (C)2021 */
package com.scoperetail.fusion.core.application.service.transform.impl;

import java.io.IOException;
import java.util.Map;
import java.util.Optional;
import org.springframework.stereotype.Component;
import com.scoperetail.fusion.core.application.service.transform.Transformer;
import com.scoperetail.fusion.core.common.JsonUtils;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class DomainToStringTransformer implements Transformer {

  @Override
  public String transform(final String event, final Map<String, Object> params, final String templateName) {
    Object object = params.get(DOMAIN_ENTITY);
	String result = object.toString();
    try {
      result = JsonUtils.marshal(Optional.ofNullable(object));
      log.trace("Event: {} transformed to String", event);
    } catch (final IOException e) {
      log.error("Unable to transform object: {}", object);
    }
    return result;
  }
}
