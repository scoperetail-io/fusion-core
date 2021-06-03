package com.scoperetail.fusion.core.adapter.out.persistence.jpa;

import com.scoperetail.fusion.audit.persistence.repository.DedupeKeyRepository;
import com.scoperetail.fusion.core.application.port.out.persistence.DedupeOutboundPort;
import com.scoperetail.fusion.shared.kernel.common.annotation.PersistenceAdapter;
import lombok.AllArgsConstructor;

@PersistenceAdapter
@AllArgsConstructor
public class DedupeJpaAdapter implements DedupeOutboundPort {
  private DedupeKeyRepository dedupeKeyRepository;

  @Override
  public Boolean isNotDuplicate(String logKey) {
    return dedupeKeyRepository.insertIfNotExist(logKey) > 0;
  }
}
