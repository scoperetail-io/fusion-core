package com.scoperetail.fusion.core.application.service.transform;

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

import static org.apache.commons.lang.StringUtils.EMPTY;

import java.util.Map;

public interface Transformer {

	String DOMAIN_ENTITY = "DOMAIN_ENTITY";

	default String transform(final String event, final Map<String, Object> params, final String templateName)
			throws Exception {
		return EMPTY;
	}
}
