package com.scoperetail.fusion.core.adapter.out.persistence.jpa;

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
