/* ScopeRetail (C)2021 */
package com.scoperetail.fusion.core.application.service.command;

/*-
 * *****
 * fusion-core
 * -----
 * Copyright (C) 2018 - 2021 Scope Retail Systems Inc.
 * -----
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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

  private final DomainToDomainEventJsonVelocityTransformer domainToDomainEventJsonVelocityTransformer;

  private final DomainToDomainEventJsonFtlTransformer domainToDomainEventJsonFtlTransformer;

  private final DomainToFtlTemplateTransformer domainToFtlTemplateTransformer;

  private final DomainToVelocityTemplateTransformer domainToVelocityTemplateTransformer;

  private final DomainToStringTransformer domainToStringTransformer;

  private final FusionConfig fusionConfig;

  @Override
  public void post(final String event, final Object domainEntity, final boolean isValid)
      throws Exception {
    handleEvent(event, domainEntity, isValid);
  }

  private void handleEvent(final String event, final Object domainEntity, final boolean isValid)
      throws Exception {
    final Optional<UseCaseConfig> optUseCase = fusionConfig.getUsecases().stream()
        .filter(u -> u.getName().equals(event)).findFirst();
    if (optUseCase.isPresent()) {
      final UseCaseConfig useCase = optUseCase.get();
      final String activeConfig = useCase.getActiveConfig();
      final Optional<Config> optConfig =
          useCase.getConfigs().stream().filter(c -> activeConfig.equals(c.getName())).findFirst();
      if (optConfig.isPresent()) {
        final Config config = optConfig.get();
        final UsecaseResult usecaseResult = isValid ? SUCCESS : FAILURE;
        final List<Adapter> adapters = config.getAdapters().stream()
            .filter(c -> c.getAdapterType().equals(Adapter.AdapterType.OUTBOUND)
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
            default:
              log.error("Invalid adapter transport type: {} for adapter: {}", trasnportType,
                  adapter);
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

  private void notifyJms(final String event, final Object domainEntity, final Adapter adapter,
      final Transformer transformer) throws Exception {
    Map<String, Object> paramsMap = new HashMap<>();
    paramsMap.put(Transformer.DOMAIN_ENTITY, domainEntity);
    final String payload = transformer.transform(event, paramsMap, adapter.getTemplate());
    posterOutboundJmsPort.post(adapter.getBrokerId(), adapter.getQueueName(), payload);
  }

  private void notifyRest(final String event, final Object domainEntity, final Adapter adapter,
      final Transformer transformer) throws Exception {
    final Map<String, Object> paramsMap = new HashMap<>();
    paramsMap.put(Transformer.DOMAIN_ENTITY, domainEntity);
    paramsMap.putAll(getCustomParams(event, adapter.getTemplateCustomizer()));
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

  private Map<String, Object> getCustomParams(String event, String customizerClassName) {
    Map<String, Object> params = MapUtils.EMPTY_MAP;
    try {
      Class customizerClazz = Class.forName(customizerClassName);
      Method method = customizerClazz.getDeclaredMethod("getParamsMap", new Class[0]);
      params = (Map<String, Object>) method.invoke(null, new Object[0]);
    } catch (Exception e) {
      log.error(
          "Skipping customization. Unable to load configured customizer for event: {} customizer: {}",
          event, customizerClassName.toString());
    }
    return params;
  }
}
