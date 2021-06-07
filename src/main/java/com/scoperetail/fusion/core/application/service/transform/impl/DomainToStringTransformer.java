/* ScopeRetail (C)2021 */
package com.scoperetail.fusion.core.application.service.transform.impl;

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

import java.io.IOException;
import java.util.Map;
import java.util.Optional;
import org.springframework.stereotype.Component;
import com.scoperetail.fusion.core.application.service.transform.Transformer;
import com.scoperetail.fusion.core.common.JsonUtils;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class DomainToStringTransformer implements Transformer {

  @Override
  public String transform(final String event, final Map<String, Object> params, final String templateName) {
    Object object = params.get(DOMAIN_ENTITY);
	String result = object.toString();
    try {
      result = JsonUtils.marshal(Optional.ofNullable(object));
      log.trace("Event: {} transformed to String", event);
    } catch (final IOException e) {
      log.error("Unable to transform object: {}", object);
    }
    return result;
  }
}
