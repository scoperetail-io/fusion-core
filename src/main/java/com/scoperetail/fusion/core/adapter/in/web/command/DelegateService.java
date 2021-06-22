package com.scoperetail.fusion.core.adapter.in.web.command;

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

import com.scoperetail.fusion.core.adapter.in.web.DelegateUseCase;
import com.scoperetail.fusion.core.adapter.out.persistence.jpa.DedupeJpaAdapter;
import com.scoperetail.fusion.core.application.service.transform.Transformer;
import com.scoperetail.fusion.core.application.service.transform.impl.HashKeyFtlTemplateTransformer;
import com.scoperetail.fusion.core.application.service.transform.impl.HashKeyVelocityTemplateTransformer;
import com.scoperetail.fusion.messaging.config.FusionConfig;
import com.scoperetail.fusion.messaging.config.UseCaseConfig;
import com.scoperetail.fusion.shared.kernel.common.annotation.UseCase;

import lombok.AllArgsConstructor;

import java.util.*;

@UseCase
@AllArgsConstructor
public class DelegateService implements DelegateUseCase {
  private HashKeyFtlTemplateTransformer hashKeyFtlTemplateTransformer;
  private HashKeyVelocityTemplateTransformer hashKeyVelocityTemplateTransformer;
  private DedupeJpaAdapter dedupeJpaAdapter;
  private FusionConfig fusionConfig;

  @Override
  public boolean isNotDuplicate(final String eventName, final Object domainEntity) throws Exception {
    Map<String, Object> paramsMap = new HashMap<>();
    paramsMap.put(Transformer.DOMAIN_ENTITY, domainEntity);
    String hashKey = null;
    Optional<UseCaseConfig> optUseCase = fusionConfig.getUsecases().stream()
        .filter(u -> u.getName().equals(eventName))
        .findFirst();

    if (optUseCase.isPresent() && optUseCase.get().getDedupeCheck()) {
      final UseCaseConfig useCase = optUseCase.get();
      final String templateName = useCase.getHashKeyTemplate();
      Transformer transformer = getTransformer(useCase.getHashKeyTransformationType());
      hashKey = transformer.transform(eventName, paramsMap, templateName);
    }
    return dedupeJpaAdapter.isNotDuplicate(hashKey);
  }

  private Transformer getTransformer(final UseCaseConfig.HashKeyTransformationType transformationType) {
    Transformer transformer;
    if (transformationType == UseCaseConfig.HashKeyTransformationType.HASH_KEY_VELOCITY_TRANSFORMER) {
      transformer = hashKeyVelocityTemplateTransformer;
    } else {
      transformer = hashKeyFtlTemplateTransformer;
    }
    return transformer;
  }
}
