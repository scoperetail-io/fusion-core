package com.scoperetail.fusion.core.application.service.transform.impl;

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

  public String getHashKey(final String event, final Map<String, Object> params, final String templateName) {
    final String keyJson = templateEngine.generateTextFromTemplate(event, params, HASH_KEY_TEMPLATE);
    return HashUtil.getHash(keyJson, HashUtil.SHA3_512);
  }

  private Map<String, String> getkeyMap(final String keyJson) throws IOException {
    final Map<String, String> keyMap =
        JsonUtils.unmarshal(Optional.of(keyJson), Map.class.getCanonicalName());
    log.trace("Key map: {}", keyMap);
    return keyMap;
  }
}
