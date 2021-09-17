package com.scoperetail.fusion.core.adapter.out.web.http.impl;

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

import static com.scoperetail.fusion.config.Adapter.TransportType.JMS;
import static com.scoperetail.fusion.config.Adapter.TransportType.REST;
import static com.scoperetail.fusion.shared.kernel.events.DomainEvent.AuditType.OUT;
import static com.scoperetail.fusion.shared.kernel.events.DomainEvent.Outcome.COMPLETE;
import static com.scoperetail.fusion.shared.kernel.events.DomainEvent.Outcome.OFFLINE_RETRY_START;
import static com.scoperetail.fusion.shared.kernel.events.DomainEvent.Outcome.ONLINE_RETRY_IN_PROGRESS;
import static com.scoperetail.fusion.shared.kernel.events.DomainEvent.Outcome.ONLINE_RETRY_START;
import static com.scoperetail.fusion.shared.kernel.events.DomainEvent.Result.FAILURE;
import static com.scoperetail.fusion.shared.kernel.events.DomainEvent.Result.SUCCESS;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import org.apache.http.impl.client.HttpClientBuilder;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.retry.support.RetrySynchronizationManager;
import org.springframework.stereotype.Component;
import org.springframework.web.client.DefaultResponseErrorHandler;
import org.springframework.web.client.RestTemplate;
import com.scoperetail.fusion.config.Adapter;
import com.scoperetail.fusion.config.Adapter.TransportType;
import com.scoperetail.fusion.config.AuditConfig;
import com.scoperetail.fusion.config.FusionConfig;
import com.scoperetail.fusion.core.adapter.out.web.http.PosterOutboundHttpAdapter;
import com.scoperetail.fusion.core.application.port.in.command.AuditUseCase;
import com.scoperetail.fusion.core.common.JsonUtils;
import com.scoperetail.fusion.core.common.LoggingInterceptor;
import com.scoperetail.fusion.core.common.PerformanceCounter;
import com.scoperetail.fusion.messaging.adapter.out.messaging.jms.MessageRouterSender;
import com.scoperetail.fusion.shared.kernel.events.DomainEvent.Outcome;
import com.scoperetail.fusion.shared.kernel.events.DomainEvent.Result;
import com.scoperetail.fusion.shared.kernel.events.DomainProperty;
import com.scoperetail.fusion.shared.kernel.messaging.jms.JMSEventWrapper;
import com.scoperetail.fusion.shared.kernel.web.request.HttpRequest;
import com.scoperetail.fusion.shared.kernel.web.request.HttpRequestWrapper;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@AllArgsConstructor
@Slf4j
public class PosterOutboundHttpAdapterImpl implements PosterOutboundHttpAdapter {
  private MessageRouterSender messageSender;
  private AuditUseCase auditUseCase;
  private FusionConfig fusionConfig;

  @Override
  public void post(
      final String usecase,
      final Set<DomainProperty> properties,
      final String hashKey,
      final Adapter adapter,
      final String url,
      final String requestBody,
      final Map<String, String> httpHeaders)
      throws Exception {
    final int retryCount = RetrySynchronizationManager.getContext().getRetryCount();
    Result result = FAILURE;
    Outcome outcome = retryCount == 0 ? ONLINE_RETRY_START : ONLINE_RETRY_IN_PROGRESS;
    try {
      post(adapter.getMethodType(), url, requestBody, httpHeaders);
      result = SUCCESS;
      outcome = COMPLETE;
    } finally {
      final HttpRequest httpRequest = buildHttpRequest(adapter, url, requestBody, httpHeaders);
      createAudit(
          usecase,
          result,
          outcome,
          REST,
          properties,
          hashKey,
          JsonUtils.marshal(Optional.of(httpRequest)));
    }
  }

  @Override
  public void post(final HttpRequest httpRequest) {
    post(
        httpRequest.getMethodType(),
        httpRequest.getUrl(),
        httpRequest.getRequestBody(),
        httpRequest.getHttpHeaders());
  }

  private void post(
      final String methodType,
      final String url,
      final String requestBody,
      final Map<String, String> httpHeaders) {
    final HttpHeaders headers = new HttpHeaders();
    httpHeaders.entrySet().forEach(mapEntry -> headers.add(mapEntry.getKey(), mapEntry.getValue()));
    final HttpEntity<String> httpEntity = new HttpEntity<>(requestBody, headers);
    final HttpComponentsClientHttpRequestFactory clientHttpRequestFactory =
        new HttpComponentsClientHttpRequestFactory(HttpClientBuilder.create().build());
    final RestTemplate restTemplate = new RestTemplate(clientHttpRequestFactory);
    restTemplate.setErrorHandler(new DefaultResponseErrorHandler());
    restTemplate.setInterceptors(Collections.singletonList(new LoggingInterceptor()));
    final PerformanceCounter perfCounter =
        new PerformanceCounter("PosterOutboundHttpAdapterImpl::post");
    try {
      final ResponseEntity<String> exchange =
          restTemplate.exchange(url, HttpMethod.valueOf(methodType), httpEntity, String.class);
      log.trace(
          "REST request sent to URL: {} and Response received is: {} Time Taken: {}",
          url,
          exchange,
          perfCounter);
    } catch (final Exception e) {
      log.error(
          "An error occured while invoking REST endpoint \nURL: {} \nMethod:{} \nRequestBody:{} \nHeaders:{} \nTime Taken:{}",
          url,
          methodType,
          requestBody,
          headers,
          perfCounter);
      throw e;
    }
  }

  @Override
  public void recover(
      final RuntimeException exception,
      final String usecase,
      final Set<DomainProperty> properties,
      final String hashKey,
      final Adapter adapter,
      final String url,
      final String requestBody,
      final Map<String, String> httpHeaders)
      throws Exception {
    log.error(
        "On recover after retryPost failed. message: {}, Exception was: {} ",
        requestBody,
        exception.getMessage());
    Result result = FAILURE;
    final Outcome outcome = OFFLINE_RETRY_START;
    String payload = null;
    try {
      final HttpRequest httpRequest = buildHttpRequest(adapter, url, requestBody, httpHeaders);
      final HttpRequestWrapper httpRequestWrapper = buildHttpRequestWrapper(adapter, httpRequest);
      payload = JsonUtils.marshal(Optional.ofNullable(httpRequestWrapper));
      messageSender.send(adapter.getBoBrokerId(), adapter.getBoQueueName(), payload);
      result = SUCCESS;
      log.trace(
          "Sent Message to Broker Id:{}  Queue: {} Message: {}",
          adapter.getBoBrokerId(),
          adapter.getBoQueueName(),
          payload);
    } finally {
      final JMSEventWrapper jmsEvent = buildJmsEvent(adapter, payload);
      createAudit(
          usecase,
          result,
          outcome,
          JMS,
          properties,
          hashKey,
          JsonUtils.marshal(Optional.of(jmsEvent)));
    }
  }

  private HttpRequestWrapper buildHttpRequestWrapper(
      final Adapter adapter, final HttpRequest httpRequest) {
    return HttpRequestWrapper.builder()
        .httpRequest(httpRequest)
        .retryCustomizers(adapter.getRetryCustomizers())
        .build();
  }

  private HttpRequest buildHttpRequest(
      final Adapter adapter,
      final String url,
      final String requestBody,
      final Map<String, String> httpHeaders) {
    return HttpRequest.builder()
        .url(url)
        .methodType(adapter.getMethodType())
        .httpHeaders(httpHeaders)
        .requestBody(requestBody)
        .build();
  }

  private JMSEventWrapper buildJmsEvent(final Adapter adapter, final String payload) {
    return JMSEventWrapper.builder()
        .brokerId(adapter.getBoBrokerId())
        .queueName(adapter.getBoQueueName())
        .payload(payload)
        .build();
  }

  private void createAudit(
      final String usecase,
      final Result result,
      final Outcome outcome,
      final TransportType transportType,
      final Set<DomainProperty> properties,
      final String hashKey,
      final String payload)
      throws Exception {
    final AuditConfig auditConfig = fusionConfig.getAuditConfig();
    if (auditConfig != null && auditConfig.isEnabled()) {
      auditUseCase.createAudit(
          usecase,
          result,
          outcome,
          transportType,
          OUT,
          properties,
          hashKey,
          payload,
          auditConfig.getTargetBrokerId(),
          auditConfig.getTargetQueueName());
    }
  }
}
