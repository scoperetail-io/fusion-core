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

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import com.scoperetail.fusion.config.FusionConfig;
import com.scoperetail.fusion.config.UseCaseConfig;
import com.scoperetail.fusion.core.application.port.in.command.HashServiceUseCase;
import com.scoperetail.fusion.core.application.service.transform.Transformer;
import com.scoperetail.fusion.core.application.service.transform.impl.DomainToHashKeyJsonFtlTemplateTransformer;
import com.scoperetail.fusion.core.application.service.transform.impl.DomainToHashKeyJsonVelocityTemplateTransformer;
import com.scoperetail.fusion.core.common.HashUtil;
import com.scoperetail.fusion.shared.kernel.common.annotation.UseCase;
import lombok.AllArgsConstructor;

@UseCase
@AllArgsConstructor
public class HashService implements HashServiceUseCase {
  private FusionConfig fusionConfig;
  private DomainToHashKeyJsonFtlTemplateTransformer hashKeyFtlTemplateTransformer;
  private DomainToHashKeyJsonVelocityTemplateTransformer hashKeyVelocityTemplateTransformer;

  @Override
  public String getHashKeyJson(final String eventName, final Object domainEntity) throws Exception {
    String hashKeyJson = null;
    final Optional<UseCaseConfig> optUseCase = fusionConfig.getUsecase(eventName);
    if (optUseCase.isPresent()) {
      final UseCaseConfig useCase = optUseCase.get();
      final String templateName = useCase.getHashKeyTemplate();
      final Transformer transformer = getTransformer(useCase.getHashKeyTransformationType());

      final Map<String, Object> paramsMap = new HashMap<>();
      paramsMap.put(Transformer.DOMAIN_ENTITY, domainEntity);
      hashKeyJson = transformer.transform(eventName, paramsMap, templateName);
    }
    return hashKeyJson;
  }

  @Override
  public String generateHash(final String hashKeyJson) {
    return HashUtil.getHash(hashKeyJson, HashUtil.SHA3_512);
  }

  @Override
  public String generateHash(final String eventName, final Object domainEntity) throws Exception {
    return generateHash(getHashKeyJson(eventName, domainEntity));
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
}
