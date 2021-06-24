package com.scoperetail.fusion.core.application.service.command;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import com.scoperetail.fusion.core.adapter.out.persistence.jpa.DedupeJpaAdapter;
import com.scoperetail.fusion.core.application.port.in.command.DuplicateCheckUseCase;
import com.scoperetail.fusion.core.application.service.transform.Transformer;
import com.scoperetail.fusion.core.application.service.transform.impl.DomainToHashKeyFtlTemplateTransformer;
import com.scoperetail.fusion.core.application.service.transform.impl.DomainToHashKeyVelocityTemplateTransformer;
import com.scoperetail.fusion.messaging.config.FusionConfig;
import com.scoperetail.fusion.messaging.config.UseCaseConfig;
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
    final boolean isDuplicate = false;
    final Optional<UseCaseConfig> optUseCase =
        fusionConfig.getUsecases().stream().filter(u -> u.getName().equals(eventName)).findFirst();

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
    if (transformationType == UseCaseConfig.HashKeyTransformationType.HASH_KEY_VELOCITY_TRANSFORMER) {
      transformer = hashKeyVelocityTemplateTransformer;
    } else {
      transformer = hashKeyFtlTemplateTransformer;
    }
    return transformer;
  }
}
