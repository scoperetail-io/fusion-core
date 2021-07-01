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

import static com.scoperetail.fusion.messaging.application.port.in.UsecaseResult.FAILURE;
import static com.scoperetail.fusion.messaging.application.port.in.UsecaseResult.SUCCESS;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import org.apache.commons.collections.MapUtils;
import com.scoperetail.fusion.core.application.port.in.command.create.PosterUseCase;
import com.scoperetail.fusion.core.application.port.out.jms.PosterOutboundJmsPort;
import com.scoperetail.fusion.core.application.port.out.mail.PosterOutboundMailPort;
import com.scoperetail.fusion.core.application.port.out.web.PosterOutboundWebPort;
import com.scoperetail.fusion.core.application.service.transform.Transformer;
import com.scoperetail.fusion.core.application.service.transform.impl.DomainToDomainEventJsonFtlTransformer;
import com.scoperetail.fusion.core.application.service.transform.impl.DomainToDomainEventJsonVelocityTransformer;
import com.scoperetail.fusion.core.application.service.transform.impl.DomainToFtlTemplateTransformer;
import com.scoperetail.fusion.core.application.service.transform.impl.DomainToStringTransformer;
import com.scoperetail.fusion.core.application.service.transform.impl.DomainToVelocityTemplateTransformer;
import com.scoperetail.fusion.core.common.JsonUtils;
import com.scoperetail.fusion.messaging.application.port.in.UsecaseResult;
import com.scoperetail.fusion.messaging.config.Adapter;
import com.scoperetail.fusion.messaging.config.Adapter.TransformationType;
import com.scoperetail.fusion.messaging.config.Adapter.TransportType;
import com.scoperetail.fusion.messaging.config.Config;
import com.scoperetail.fusion.messaging.config.FusionConfig;
import com.scoperetail.fusion.messaging.config.MailHost;
import com.scoperetail.fusion.messaging.config.UseCaseConfig;
import com.scoperetail.fusion.shared.kernel.common.annotation.UseCase;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@UseCase
@AllArgsConstructor
@Slf4j
class PosterService implements PosterUseCase {

  private final PosterOutboundJmsPort posterOutboundJmsPort;

  private final PosterOutboundWebPort posterOutboundWebPort;

  private final DomainToDomainEventJsonVelocityTransformer
      domainToDomainEventJsonVelocityTransformer;

  private final DomainToDomainEventJsonFtlTransformer domainToDomainEventJsonFtlTransformer;

  private final DomainToFtlTemplateTransformer domainToFtlTemplateTransformer;

  private final DomainToVelocityTemplateTransformer domainToVelocityTemplateTransformer;

  private final DomainToStringTransformer domainToStringTransformer;

  private final FusionConfig fusionConfig;

  private final PosterOutboundMailPort posterOutboundMailPort;

  @Override
  public void post(final String event, final Object domainEntity, final boolean isValid)
      throws Exception {
    handleEvent(event, domainEntity, isValid);
  }

  private void handleEvent(final String event, final Object domainEntity, final boolean isValid)
      throws Exception {
    final Optional<UseCaseConfig> optUseCase =
        fusionConfig.getUsecases().stream().filter(u -> u.getName().equals(event)).findFirst();
    if (optUseCase.isPresent()) {
      final UseCaseConfig useCase = optUseCase.get();
      final String activeConfig = useCase.getActiveConfig();
      final Optional<Config> optConfig =
          useCase.getConfigs().stream().filter(c -> activeConfig.equals(c.getName())).findFirst();
      if (optConfig.isPresent()) {
        final Config config = optConfig.get();
        final UsecaseResult usecaseResult = isValid ? SUCCESS : FAILURE;
        final List<Adapter> adapters =
            config
                .getAdapters()
                .stream()
                .filter(
                    c ->
                        c.getAdapterType().equals(Adapter.AdapterType.OUTBOUND)
                            && c.getUsecaseResult().equals(usecaseResult))
                .collect(Collectors.toList());
        for (final Adapter adapter : adapters) {
          log.trace("Notifying outbound adapter: {}", adapter);
          final Transformer transformer = getTransformer(adapter.getTransformationType());
          final TransportType trasnportType = adapter.getTrasnportType();
          switch (trasnportType) {
            case JMS:
              notifyJms(event, domainEntity, adapter, transformer);
              break;
            case REST:
              notifyRest(event, domainEntity, adapter, transformer);
              break;
            case MAIL:
              notifyMail(event, domainEntity, adapter, transformer);
              break;
            default:
              log.error(
                  "Invalid adapter transport type: {} for adapter: {}", trasnportType, adapter);
          }
        }
      }
    }
  }

  private Transformer getTransformer(final TransformationType transformationType) {
    Transformer transformer;
    switch (transformationType) {
      case DOMAIN_EVENT_FTL_TRANSFORMER:
        transformer = domainToDomainEventJsonFtlTransformer;
        break;
      case DOMAIN_EVENT_VELOCITY_TRANSFORMER:
        transformer = domainToDomainEventJsonVelocityTransformer;
        break;
      case FTL_TEMPLATE_TRANSFORMER:
        transformer = domainToFtlTemplateTransformer;
        break;
      case VELOCITY_TEMPLATE_TRANSFORMER:
        transformer = domainToVelocityTemplateTransformer;
        break;
      default:
        transformer = domainToStringTransformer;
        break;
    }
    return transformer;
  }

  private void notifyJms(
      final String event,
      final Object domainEntity,
      final Adapter adapter,
      final Transformer transformer)
      throws Exception {
    final Map<String, Object> paramsMap = new HashMap<>();
    paramsMap.put(Transformer.DOMAIN_ENTITY, domainEntity);
    final String payload = transformer.transform(event, paramsMap, adapter.getTemplate());
    posterOutboundJmsPort.post(adapter.getBrokerId(), adapter.getQueueName(), payload);
  }

  private void notifyRest(
      final String event,
      final Object domainEntity,
      final Adapter adapter,
      final Transformer transformer)
      throws Exception {
    final Map<String, Object> paramsMap = new HashMap<>();
    paramsMap.put(Transformer.DOMAIN_ENTITY, domainEntity);
    paramsMap.putAll(getCustomParams(event, domainEntity, adapter.getTemplateCustomizer()));
    final String requestHeader =
        transformer.transform(event, paramsMap, adapter.getRequestHeaderTemplate());
    final Map<String, String> httpHeadersMap =
        JsonUtils.unmarshal(Optional.ofNullable(requestHeader), Map.class.getCanonicalName());
    final String requestBody =
        transformer.transform(event, paramsMap, adapter.getRequestBodyTemplate());
    final String uri = transformer.transform(event, paramsMap, adapter.getUriTemplate());
    final String url =
        adapter.getProtocol() + "://" + adapter.getHostName() + ":" + adapter.getPort() + uri;
    posterOutboundWebPort.post(url, adapter.getMethodType(), requestBody, httpHeadersMap);
  }

  private Map<String, Object> getCustomParams(
      final String event, final Object domainEntity, final String customizerClassName) {
    Map<String, Object> params = MapUtils.EMPTY_MAP;
    try {
      final Class customizerClazz = Class.forName(customizerClassName);
      final Method method = customizerClazz.getDeclaredMethod("getParamsMap", Object.class);
      params = (Map<String, Object>) method.invoke(null, domainEntity);
    } catch (final Exception e) {
      log.error(
          "Skipping customization. Unable to load configured customizer for event: {} customizer: {}",
          event,
          customizerClassName);
    }
    return params;
  }

  private void notifyMail(
      final String event,
      final Object domainEntity,
      final Adapter adapter,
      final Transformer transformer)
      throws Exception {
    final Optional<MailHost> optionalMailHost =
        fusionConfig
            .getMailHosts()
            .stream()
            .filter(host -> host.getHostId().equals(adapter.getHostId()))
            .findFirst();
    if (optionalMailHost.isPresent()) {
      final Map<String, Object> paramsMap = new HashMap<>();
      paramsMap.put(Transformer.DOMAIN_ENTITY, domainEntity);
      final String from = transformer.transform(event, paramsMap, adapter.getFromTemplate());
      final String to = transformer.transform(event, paramsMap, adapter.getToTemplate());
      final String cc = transformer.transform(event, paramsMap, adapter.getCcTemplate());
      final String bcc = transformer.transform(event, paramsMap, adapter.getBccTemplate());
      final String replyTo = transformer.transform(event, paramsMap, adapter.getReplyToTemplate());
      final String subject = transformer.transform(event, paramsMap, adapter.getSubjectTemplate());
      final String text = transformer.transform(event, paramsMap, adapter.getTextTemplate());

      posterOutboundMailPort.post(
          optionalMailHost.get(), from, to, cc, bcc, replyTo, subject, text);
    }
  }
}
