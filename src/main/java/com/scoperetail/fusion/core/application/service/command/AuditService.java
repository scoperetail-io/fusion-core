package com.scoperetail.fusion.core.application.service.command;

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
import org.apache.commons.lang3.StringUtils;
import com.scoperetail.fusion.config.Adapter.TransportType;
import com.scoperetail.fusion.core.adapter.out.messaging.jms.PosterOutboundJMSAdapter;
import com.scoperetail.fusion.core.application.port.in.command.AuditUseCase;
import com.scoperetail.fusion.core.application.port.in.command.HashServiceUseCase;
import com.scoperetail.fusion.core.common.JsonUtils;
import com.scoperetail.fusion.shared.kernel.common.annotation.UseCase;
import com.scoperetail.fusion.shared.kernel.events.DomainEvent;
import com.scoperetail.fusion.shared.kernel.events.DomainEvent.AuditType;
import com.scoperetail.fusion.shared.kernel.events.DomainEvent.Outcome;
import com.scoperetail.fusion.shared.kernel.events.DomainEvent.Result;
import lombok.AllArgsConstructor;

@UseCase
@AllArgsConstructor
public class AuditService implements AuditUseCase {
  private final HashServiceUseCase hashServiceUseCase;
  private final PosterOutboundJMSAdapter posterOutboundJMSAdapter;

  @Override
  public void createAudit(
      final String usecase,
      final Result result,
      final Outcome outcome,
      final TransportType transportType,
      final AuditType auditType,
      final String hashKeyJson,
      final String hashKey,
      final String payload,
      final String brokerId,
      final String queueName)
      throws Exception {
    final DomainEvent domainEvent =
        buildDomainEvent(
            usecase, result, outcome, transportType, auditType, hashKeyJson, hashKey, payload);
    posterOutboundJMSAdapter.post(brokerId, queueName, JsonUtils.marshal(Optional.of(domainEvent)));
  }

  @Override
  public void createAudit(
      final String usecase,
      final Result result,
      final Outcome outcome,
      final TransportType transportType,
      final AuditType auditType,
      final Object domainEntity,
      final String payload,
      final String brokerId,
      final String queueName)
      throws Exception {
    final String hashKeyJson = hashServiceUseCase.getHashKeyJson(usecase, domainEntity);
    final String hashKey = hashServiceUseCase.generateHash(hashKeyJson);
    createAudit(
        usecase,
        result,
        outcome,
        transportType,
        auditType,
        hashKeyJson,
        hashKey,
        payload,
        brokerId,
        queueName);
  }

  private DomainEvent buildDomainEvent(
      final String event,
      final Result result,
      final Outcome outcome,
      final TransportType transportType,
      final AuditType auditType,
      final String hashKeyJson,
      final String hashKey,
      final String payload)
      throws IOException {
    Map<String, String> keyMap = null;
    if (StringUtils.isNotBlank(hashKeyJson)) {
      keyMap = getkeyMap(hashKeyJson);
    }
    return DomainEvent.builder()
        .event(event)
        .eventId(hashKey)
        .transportType(transportType.name())
        .auditType(auditType)
        .result(result)
        .outcome(outcome)
        .keyMap(keyMap)
        .payload(payload)
        .build();
  }

  private Map<String, String> getkeyMap(final String keyJson) throws IOException {
    return JsonUtils.unmarshal(Optional.of(keyJson), Map.class.getCanonicalName());
  }
}
