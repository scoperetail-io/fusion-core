/* ScopeRetail (C)2021 */
package com.scoperetail.fusion.core.adapter.out.web;

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

import java.util.Map;
import com.scoperetail.fusion.core.adapter.out.web.http.PosterOutboundHttpAdapter;
import com.scoperetail.fusion.core.application.port.out.web.PosterOutboundWebPort;
import com.scoperetail.fusion.shared.kernel.common.annotation.WebAdapter;

import lombok.AllArgsConstructor;

@WebAdapter
@AllArgsConstructor
public class PosterOutboundWebAdapter implements PosterOutboundWebPort {

	PosterOutboundHttpAdapter posterOutboundHttpAdapter;

	@Override
	public void post(final String url, final String methodType, final String requestBody,
			final Map<String, String> httpHeaders) {
		posterOutboundHttpAdapter.post(url, methodType, requestBody, httpHeaders);
	}
}
