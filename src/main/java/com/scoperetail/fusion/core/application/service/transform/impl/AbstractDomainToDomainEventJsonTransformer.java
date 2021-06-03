/* ScopeRetail (C)2021 */
package com.scoperetail.fusion.core.application.service.transform.impl;

import java.io.IOException;
import java.util.Map;
import java.util.Optional;
import com.scoperetail.fusion.core.application.service.transform.AbstractTransformer;
import com.scoperetail.fusion.core.application.service.transform.template.engine.TemplateEngine;
import com.scoperetail.fusion.core.common.HashUtil;
import com.scoperetail.fusion.core.common.JsonUtils;
import com.scoperetail.fusion.shared.kernel.events.DomainEvent;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public abstract class AbstractDomainToDomainEventJsonTransformer extends AbstractTransformer {

  private static final String HASH_KEY_TEMPLATE = "hash_key";

  protected AbstractDomainToDomainEventJsonTransformer(final TemplateEngine templateEngine) {
    super(templateEngine);
  }

  @Override
  public String transform(final String event, final Map<String, Object> params,
      final String templateName) throws Exception {
    final Object domainEntity = params.get(DOMAIN_ENTITY);
    final String payload = JsonUtils.marshal(Optional.ofNullable(domainEntity));

    final String keyJson =
        templateEngine.generateTextFromTemplate(event, params, HASH_KEY_TEMPLATE);

    Map<String, String> keyMap = getkeyMap(keyJson);
    String keyHash = HashUtil.getHash(keyJson, HashUtil.SHA3_512);

    final DomainEvent domainEvent =
        DomainEvent.builder().eventId(keyHash).event(event).keyMap(keyMap).payload(payload).build();
    final String result = JsonUtils.marshal(Optional.ofNullable(domainEvent));
    log.trace("Event: {} transformed to domain entity", event);
    return result;
  }

  private final Map<String, String> getkeyMap(final String keyJson) throws IOException {
    final Map<String, String> keyMap =
        JsonUtils.unmarshal(Optional.of(keyJson), Map.class.getCanonicalName());
    log.trace("Key map: {}", keyMap);
    return keyMap;
  }
}
