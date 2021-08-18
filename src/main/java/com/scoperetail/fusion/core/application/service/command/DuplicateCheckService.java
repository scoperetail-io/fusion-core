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
import com.scoperetail.fusion.core.adapter.out.persistence.jpa.DedupeJpaAdapter;
import com.scoperetail.fusion.core.application.port.in.command.DuplicateCheckUseCase;
import com.scoperetail.fusion.core.application.service.transform.Transformer;
import com.scoperetail.fusion.core.application.service.transform.impl.DomainToHashKeyFtlTemplateTransformer;
import com.scoperetail.fusion.core.application.service.transform.impl.DomainToHashKeyVelocityTemplateTransformer;
import com.scoperetail.fusion.shared.kernel.common.annotation.UseCase;
import lombok.AllArgsConstructor;

@UseCase
@AllArgsConstructor
public class DuplicateCheckService implements DuplicateCheckUseCase {
  private DomainToHashKeyFtlTemplateTransformer hashKeyFtlTemplateTransformer;
  private DomainToHashKeyVelocityTemplateTransformer hashKeyVelocityTemplateTransformer;
  private DedupeJpaAdapter dedupeJpaAdapter;
  private FusionConfig fusionConfig;

  @Override
  public boolean isDuplicate(final String eventName, final Object domainEntity) throws Exception {
    boolean isDuplicate = false;
    final Optional<UseCaseConfig> optUseCase = fusionConfig.getUsecase(eventName);
    if (optUseCase.isPresent() && optUseCase.get().getDedupeCheck()) {
      final UseCaseConfig useCase = optUseCase.get();
      final String templateName = useCase.getHashKeyTemplate();
      final Transformer transformer = getTransformer(useCase.getHashKeyTransformationType());

      final Map<String, Object> paramsMap = new HashMap<>();
      paramsMap.put(Transformer.DOMAIN_ENTITY, domainEntity);
      final String hashKey = transformer.transform(eventName, paramsMap, templateName);
      isDuplicate = !dedupeJpaAdapter.isNotDuplicate(hashKey);
    }
    return isDuplicate;
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
