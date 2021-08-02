package com.scoperetail.fusion.core.common;

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

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class LoggingInterceptor implements ClientHttpRequestInterceptor {

  @Override
  public ClientHttpResponse intercept(
      final HttpRequest request, final byte[] body, final ClientHttpRequestExecution execution)
      throws IOException {

    logRequest(request, body);
    final ClientHttpResponse response = execution.execute(request, body);
    logResponse(response);
    return response;
  }

  private void logRequest(final HttpRequest request, final byte[] body) throws IOException {
    if (log.isDebugEnabled()) {
      log.debug(
          "===========================request begin================================================");
      log.debug("URI         : {}", request.getURI());
      log.debug("Method      : {}", request.getMethod());
      log.debug("Headers     : {}", request.getHeaders());
      log.debug("Request body: {}", new String(body, StandardCharsets.UTF_8));
      log.debug(
          "==========================request end================================================");
    }
  }

  private void logResponse(final ClientHttpResponse response) throws IOException {
    if (log.isDebugEnabled()) {
      log.debug(
          "============================response begin==========================================");
      log.debug("Status code  : {}", response.getStatusCode());
      log.debug("Status text  : {}", response.getStatusText());
      log.debug("Headers      : {}", response.getHeaders());

      log.debug(
          "=======================response end=================================================");
    }
  }
}
