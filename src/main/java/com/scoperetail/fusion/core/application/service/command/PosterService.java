package com.scoperetail.fusion.core.application.service.command;

import static com.scoperetail.fusion.config.Adapter.UsecaseResult.FAILURE;
import static com.scoperetail.fusion.config.Adapter.UsecaseResult.SUCCESS;
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
import static java.io.File.separator;

import java.io.FileNotFoundException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.commons.collections.MapUtils;
import org.springframework.util.ResourceUtils;

import com.scoperetail.fusion.config.Adapter;
import com.scoperetail.fusion.config.Adapter.TransformationType;
import com.scoperetail.fusion.config.Adapter.TransportType;
import com.scoperetail.fusion.config.Adapter.UsecaseResult;
import com.scoperetail.fusion.config.Config;
import com.scoperetail.fusion.config.FusionConfig;
import com.scoperetail.fusion.config.MailHost;
import com.scoperetail.fusion.core.application.port.in.command.create.PosterUseCase;
import com.scoperetail.fusion.core.application.port.out.jms.PosterOutboundJmsPort;
import com.scoperetail.fusion.core.application.port.out.kafka.PosterOutboundKafkaPort;
import com.scoperetail.fusion.core.application.port.out.mail.MailDetailsDto;
import com.scoperetail.fusion.core.application.port.out.mail.PosterOutboundMailPort;
import com.scoperetail.fusion.core.application.port.out.web.PosterOutboundWebPort;
import com.scoperetail.fusion.core.application.service.transform.Transformer;
import com.scoperetail.fusion.core.application.service.transform.impl.DomainToDomainEventJsonFtlTransformer;
import com.scoperetail.fusion.core.application.service.transform.impl.DomainToDomainEventJsonVelocityTransformer;
import com.scoperetail.fusion.core.application.service.transform.impl.DomainToFtlTemplateTransformer;
import com.scoperetail.fusion.core.application.service.transform.impl.DomainToStringTransformer;
import com.scoperetail.fusion.core.application.service.transform.impl.DomainToVelocityTemplateTransformer;
import com.scoperetail.fusion.core.common.JsonUtils;
import com.scoperetail.fusion.shared.kernel.common.annotation.UseCase;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@UseCase
@AllArgsConstructor
@Slf4j
class PosterService implements PosterUseCase {

  private static final String DEFAULT_EMAIL_TEMPLATE_LOOKUP_PATH = "default";

  private final PosterOutboundJmsPort posterOutboundJmsPort;
  private final PosterOutboundKafkaPort posterOutboundKafkaPort;

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
    final Optional<Config> optActiveConfig = fusionConfig.getActiveConfig(event);
    if (optActiveConfig.isPresent()) {
      final UsecaseResult usecaseResult = isValid ? SUCCESS : FAILURE;
      final List<Adapter> adapters = getAdapters(usecaseResult, optActiveConfig.get());
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
          case KAFKA:
            notifyKafka(event, domainEntity, adapter, transformer);
            break;
          default:
            log.error("Invalid adapter transport type: {} for adapter: {}", trasnportType, adapter);
        }
      }
    }
  }

  private List<Adapter> getAdapters(final UsecaseResult usecaseResult, final Config config) {
    return config
        .getAdapters()
        .stream()
        .filter(
            c ->
                c.getAdapterType().equals(Adapter.AdapterType.OUTBOUND)
                    && c.getUsecaseResult().equals(usecaseResult))
        .collect(Collectors.toList());
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
    posterOutboundWebPort.post(adapter, url, requestBody, httpHeadersMap);
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

      final String templateDirBasePath = transformer.getTemplateDirBasePath(event);
      final String templateFileExtension = transformer.getTemplateFileExtension();
      final String lookupPath = getLookupPath(domainEntity);
      final String from =
          transformer.transform(
              event,
              paramsMap,
              getEmailTemplateLookupPath(
                  templateDirBasePath,
                  lookupPath,
                  adapter.getFromTemplate(),
                  templateFileExtension));

      final String to =
          transformer.transform(
              event,
              paramsMap,
              getEmailTemplateLookupPath(
                  templateDirBasePath, lookupPath, adapter.getToTemplate(), templateFileExtension));

      final String replyTo =
          transformer.transform(
              event,
              paramsMap,
              getEmailTemplateLookupPath(
                  templateDirBasePath,
                  lookupPath,
                  adapter.getReplyToTemplate(),
                  templateFileExtension));

      final String subject =
          transformer.transform(
              event,
              paramsMap,
              getEmailTemplateLookupPath(
                  templateDirBasePath,
                  lookupPath,
                  adapter.getSubjectTemplate(),
                  templateFileExtension));

      final String body =
          transformer.transform(
              event,
              paramsMap,
              getEmailTemplateLookupPath(
                  templateDirBasePath,
                  lookupPath,
                  adapter.getTextTemplate(),
                  templateFileExtension));

      final MailDetailsDto mailDetailsDto =
          MailDetailsDto.builder()
              .mailHost(optionalMailHost.get())
              .from(from)
              .to(to)
              .replyTo(replyTo)
              .subject(subject)
              .body(body)
              .build();
      posterOutboundMailPort.post(mailDetailsDto);
    }
  }

  private String getEmailTemplateLookupPath(
      final String templateDirBasePath,
      final String lookupPath,
      final String templateName,
      final String templateFileExtension) {
    String targetLookupPath = DEFAULT_EMAIL_TEMPLATE_LOOKUP_PATH + separator + templateName;

    if (Objects.nonNull(lookupPath)) {
      final boolean exists =
          isLookupPathExists(templateDirBasePath, lookupPath, templateName, templateFileExtension);
      if (exists) {
        targetLookupPath = lookupPath + separator + templateName;
      }
    }
    return targetLookupPath;
  }

  private String getLookupPath(final Object domainEntity)
      throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
    String result = null;
    final Class<? extends Object> clazz = domainEntity.getClass();
    final Method method = clazz.getDeclaredMethod("getLookupPath", new Class[0]);
    if (Objects.nonNull(method) && method.getReturnType().equals(String.class)) {
      final Object object = method.invoke(domainEntity, new Object[0]);
      if (object instanceof String) {
        result = (String) object;
      }
    }
    return result;
  }

  private boolean isLookupPathExists(
      final String templateDirBasePath,
      final String lookupPath,
      final String templateName,
      final String templateFileExtension) {
    try {
      ResourceUtils.getFile(
          "classpath:"
              + templateDirBasePath
              + separator
              + lookupPath
              + separator
              + templateName
              + templateFileExtension);
      return true;
    } catch (final FileNotFoundException e) {
      return false;
    }
  }

  private void notifyKafka(
      final String event,
      final Object domainEntity,
      final Adapter adapter,
      final Transformer transformer)
      throws Exception {

    final Map<String, Object> paramsMap = new HashMap<>();
    paramsMap.put(Transformer.DOMAIN_ENTITY, domainEntity);
    final String payload = transformer.transform(event, paramsMap, adapter.getTemplate());
    posterOutboundKafkaPort.post(adapter.getBrokerId(), adapter.getTopicName(), payload);
  }
}
