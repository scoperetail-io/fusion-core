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
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.util.ContentCachingRequestWrapper;
import com.scoperetail.fusion.config.Adapter.TransportType;
import com.scoperetail.fusion.config.AuditConfig;
import com.scoperetail.fusion.config.FusionConfig;
import com.scoperetail.fusion.core.adapter.out.messaging.jms.PosterOutboundJMSAdapter;
import com.scoperetail.fusion.core.application.port.in.command.AuditUseCase;
import com.scoperetail.fusion.core.application.port.in.command.HashServiceUseCase;
import com.scoperetail.fusion.core.common.JsonUtils;
import com.scoperetail.fusion.shared.kernel.common.annotation.UseCase;
import com.scoperetail.fusion.shared.kernel.events.DomainEvent;
import com.scoperetail.fusion.shared.kernel.events.DomainEvent.AuditType;
import com.scoperetail.fusion.shared.kernel.web.request.HttpRequest;
import lombok.AllArgsConstructor;

@UseCase
@AllArgsConstructor
public class AuditService implements AuditUseCase {
  private final FusionConfig fusionConfig;
  private final HashServiceUseCase hashServiceUseCase;
  private final PosterOutboundJMSAdapter posterOutboundJMSAdapter;

  @Override
  public void createAudit(final String event, final Object domainEntity) throws Exception {
    final String hashKeyJson = hashServiceUseCase.getHashKeyJson(event, domainEntity);
    final String hashKey = hashServiceUseCase.generateHash(hashKeyJson);
    final HttpRequest httpRequest = buildHttpRequest();
    final DomainEvent domainEvent = buildDomainEvent(event, hashKeyJson, hashKey, httpRequest);
    final AuditConfig auditConfig = fusionConfig.getAuditConfig();
    posterOutboundJMSAdapter.post(
        auditConfig.getBrokerId(),
        auditConfig.getQueueName(),
        JsonUtils.marshal(Optional.of(domainEvent)));
  }

  private DomainEvent buildDomainEvent(
      final String event,
      final String hashKeyJson,
      final String hashKey,
      final HttpRequest httpRequest)
      throws IOException {
    return DomainEvent.builder()
        .event(event)
        .auditType(AuditType.IN)
        .eventId(hashKey)
        .keyMap(getkeyMap(hashKeyJson))
        .transportType(TransportType.REST.name())
        .payload(JsonUtils.marshal(Optional.of(httpRequest)))
        .build();
  }

  private HttpRequest buildHttpRequest() {
    final ServletRequestAttributes currentRequestAttributes =
        (ServletRequestAttributes) RequestContextHolder.currentRequestAttributes();
    final ContentCachingRequestWrapper requestWrapper =
        (ContentCachingRequestWrapper) currentRequestAttributes.getRequest();

    final Map<String, String> queryParamsByNameMap =
        Collections.list(requestWrapper.getParameterNames())
            .stream()
            .collect(Collectors.toMap(paramName -> paramName, requestWrapper::getParameter));

    final Map<String, String> headersByNameMap =
        Collections.list(requestWrapper.getHeaderNames())
            .stream()
            .collect(Collectors.toMap(headerName -> headerName, requestWrapper::getHeader));

    return HttpRequest.builder()
        .url(requestWrapper.getRequestURL().toString())
        .queryParams(queryParamsByNameMap)
        .httpHeaders(headersByNameMap)
        .methodType(requestWrapper.getMethod())
        .requestBody(new String(requestWrapper.getContentAsByteArray()))
        .build();
  }

  private Map<String, String> getkeyMap(final String keyJson) throws IOException {
    return JsonUtils.unmarshal(Optional.of(keyJson), Map.class.getCanonicalName());
  }
}
