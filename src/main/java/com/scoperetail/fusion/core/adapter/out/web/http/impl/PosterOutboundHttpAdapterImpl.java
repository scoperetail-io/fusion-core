/* ScopeRetail (C)2021 */
package com.scoperetail.fusion.core.adapter.out.web.http.impl;

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

import java.util.Collections;
import java.util.Map;
import org.apache.http.impl.client.HttpClientBuilder;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.DefaultResponseErrorHandler;
import org.springframework.web.client.RestTemplate;
import com.scoperetail.fusion.core.adapter.out.web.http.PosterOutboundHttpAdapter;
import com.scoperetail.fusion.core.common.LoggingInterceptor;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class PosterOutboundHttpAdapterImpl implements PosterOutboundHttpAdapter {

  @Override
  public void post(
      final String url,
      final String methodType,
      final String requestBody,
      final Map<String, String> httpHeaders) {
    final HttpHeaders headers = new HttpHeaders();
    httpHeaders.entrySet().forEach(mapEntry -> headers.add(mapEntry.getKey(), mapEntry.getValue()));
    final HttpEntity<String> httpEntity = new HttpEntity<>(requestBody, headers);

    final HttpComponentsClientHttpRequestFactory clientHttpRequestFactory =
        new HttpComponentsClientHttpRequestFactory(HttpClientBuilder.create().build());
    final RestTemplate restTemplate = new RestTemplate(clientHttpRequestFactory);
    restTemplate.setErrorHandler(new DefaultResponseErrorHandler());
    restTemplate.setInterceptors(Collections.singletonList(new LoggingInterceptor()));
    try {
      final ResponseEntity<String> exchange =
          restTemplate.exchange(url, HttpMethod.valueOf(methodType), httpEntity, String.class);
      log.trace("REST request sent to URL: {} and Response received is: {}", url, exchange);
    } catch (final Exception e) {
      log.error(
          "An error occured while invoking REST endpoint \nURL: {} \nMethod:{} \nRequestBody:{} \nHeaders:{}",
          url,
          methodType,
          requestBody,
          headers);
      throw e;
    }
  }

  @Override
  public void recover(final RuntimeException exception, final String message) {
    log.error(
        "On recover after retryPost failed. message: {}, Exception was: {} ",
        message,
        exception.getMessage());
  }
}
