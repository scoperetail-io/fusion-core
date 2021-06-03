/* ScopeRetail (C)2021 */
package com.scoperetail.fusion.core.adapter.out.web.http.impl;

import java.util.Map;
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
