package com.scoperetail.fusion.core.common;

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
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;

import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StreamUtils;

@Slf4j
public class LoggingInterceptor implements ClientHttpRequestInterceptor {

	@Override
	public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution)
			throws IOException {

		logRequest(request, body);
		ClientHttpResponse response = execution.execute(request, body);
		logResponse(response);

		//Add optional additional headers
		response.getHeaders().add("headerName", "VALUE");

		return response;
	}

	private void logRequest(HttpRequest request, byte[] body) throws IOException
	{
		if (log.isDebugEnabled())
		{
			log.debug("===========================request begin================================================");
			log.debug("URI         : {}", request.getURI());
			log.debug("Method      : {}", request.getMethod());
			log.debug("Headers     : {}", request.getHeaders());
			log.debug("Request body: {}", new String(body, StandardCharsets.UTF_8));
			log.debug("==========================request end================================================");
		}
	}

	private void logResponse(ClientHttpResponse response) throws IOException
	{
		if (log.isDebugEnabled())
		{
			log.debug("============================response begin==========================================");
			log.debug("Status code  : {}", response.getStatusCode());
			log.debug("Status text  : {}", response.getStatusText());
			log.debug("Headers      : {}", response.getHeaders());
			log.debug("Response body: {}", StreamUtils.copyToString(response.getBody(), Charset.defaultCharset()));
			log.debug("=======================response end=================================================");
		}
	}
}