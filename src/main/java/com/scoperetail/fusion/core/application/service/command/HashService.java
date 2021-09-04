package com.scoperetail.fusion.core.application.service.command;

import java.io.IOException;

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

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;
import com.fasterxml.jackson.core.type.TypeReference;
import com.scoperetail.fusion.config.FusionConfig;
import com.scoperetail.fusion.config.UseCaseConfig;
import com.scoperetail.fusion.core.application.port.in.command.HashServiceUseCase;
import com.scoperetail.fusion.core.application.service.transform.Transformer;
import com.scoperetail.fusion.core.application.service.transform.impl.DomainToHashKeyJsonFtlTemplateTransformer;
import com.scoperetail.fusion.core.application.service.transform.impl.DomainToHashKeyJsonVelocityTemplateTransformer;
import com.scoperetail.fusion.core.common.HashUtil;
import com.scoperetail.fusion.core.common.JsonUtils;
import com.scoperetail.fusion.shared.kernel.common.annotation.UseCase;
import com.scoperetail.fusion.shared.kernel.events.Property;
import lombok.AllArgsConstructor;

@UseCase
@AllArgsConstructor
public class HashService implements HashServiceUseCase {
  private static final String EQUALS = "=";
  private static final String SEMICOLON = ";";
  private FusionConfig fusionConfig;
  private DomainToHashKeyJsonFtlTemplateTransformer hashKeyFtlTemplateTransformer;
  private DomainToHashKeyJsonVelocityTemplateTransformer hashKeyVelocityTemplateTransformer;

  @Override
  public Set<Property> getProperties(final String usecase, final Object domainEntity)
      throws Exception {
    Set<Property> properties = null;
    final Optional<UseCaseConfig> optUseCase = fusionConfig.getUsecase(usecase);
    if (optUseCase.isPresent()) {
      final UseCaseConfig useCase = optUseCase.get();
      final String templateName = useCase.getHashKeyTemplate();
      final Transformer transformer = getTransformer(useCase.getHashKeyTransformationType());

      final Map<String, Object> paramsMap = new HashMap<>();
      paramsMap.put(Transformer.DOMAIN_ENTITY, domainEntity);
      final String hashKeyJson = transformer.transform(usecase, paramsMap, templateName);
      properties = getProperties(hashKeyJson);
    }
    return properties;
  }

  @Override
  public String generateHash(final Set<Property> properties) {
    final StringBuilder hashKeyBuilder = new StringBuilder();
    properties.forEach(
        property -> {
          hashKeyBuilder.append(property.getKey());
          hashKeyBuilder.append(EQUALS);
          hashKeyBuilder.append(property.getValue());
          hashKeyBuilder.append(SEMICOLON);
        });
    return HashUtil.getHash(hashKeyBuilder.toString(), HashUtil.SHA3_512);
  }

  @Override
  public String generateHash(final String usecase, final Object domainEntity) throws Exception {
    return generateHash(getProperties(usecase, domainEntity));
  }

  private Transformer getTransformer(
      final UseCaseConfig.HashKeyTransformationType transformationType) {
    Transformer transformer;
    if (transformationType
        == UseCaseConfig.HashKeyTransformationType.HASH_KEY_VELOCITY_TRANSFORMER) {
      transformer = hashKeyVelocityTemplateTransformer;
    } else {
      transformer = hashKeyFtlTemplateTransformer;
    }
    return transformer;
  }

  public Set<Property> getProperties(final String hashKeyJson) throws IOException {
    final Set<Property> properties =
        JsonUtils.unmarshal(
            Optional.of(hashKeyJson), Optional.of(new TypeReference<TreeSet<Property>>() {}));
    properties.forEach(
        u -> {
          u.setKey(u.getKey().trim());
          u.setValue(u.getValue().trim());
        });
    return properties;
  }
}
