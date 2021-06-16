/* ScopeRetail (C)2021 */
package com.scoperetail.fusion.core.adapter.out.web.http.impl;

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

import java.util.Collections;
import java.util.Map;

import com.scoperetail.fusion.core.common.GenericRestResponseErrorHandler;
import com.scoperetail.fusion.core.common.LoggingInterceptor;
import org.apache.http.impl.client.HttpClientBuilder;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import com.scoperetail.fusion.core.adapter.out.web.http.PosterOutboundHttpAdapter;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class PosterOutboundHttpAdapterImpl implements PosterOutboundHttpAdapter {

  @Override
  public void post(final String url, final String methodType, final String requestBody,
      final Map<String, String> httpHeaders) {
    final HttpHeaders headers = new HttpHeaders();
    httpHeaders.entrySet().forEach(mapEntry -> headers.add(mapEntry.getKey(), mapEntry.getValue()));
    final HttpEntity<String> httpEntity = new HttpEntity<>(requestBody, headers);

    HttpComponentsClientHttpRequestFactory clientHttpRequestFactory =
        new HttpComponentsClientHttpRequestFactory(HttpClientBuilder.create().build());
    final RestTemplate restTemplate = new RestTemplate(clientHttpRequestFactory);
    restTemplate.setErrorHandler(new GenericRestResponseErrorHandler());
    restTemplate.setInterceptors(Collections.singletonList(new LoggingInterceptor()));
    final ResponseEntity<String> exchange =
        restTemplate.exchange(url, HttpMethod.valueOf(methodType), httpEntity, String.class);
    log.trace("REST request sent to URL: {} and Response received is: {}", url, exchange);
  }

  @Override
  public void recover(RuntimeException exception, String message) {
    log.error("On recover after retryPost failed. message: {}, Exception was: {} ", message,
        exception.getMessage());
  }
}
