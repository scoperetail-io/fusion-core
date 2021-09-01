package com.scoperetail.fusion.core.adapter.out.web;

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

import java.util.Map;
import com.scoperetail.fusion.config.Adapter;
import com.scoperetail.fusion.core.adapter.out.web.http.PosterOutboundHttpAdapter;
import com.scoperetail.fusion.core.application.port.out.web.PosterOutboundWebPort;
import com.scoperetail.fusion.shared.kernel.common.annotation.WebAdapter;
import lombok.AllArgsConstructor;

@WebAdapter
@AllArgsConstructor
public class PosterOutboundWebAdapter implements PosterOutboundWebPort {

  PosterOutboundHttpAdapter posterOutboundHttpAdapter;

  @Override
  public void post(
      final String usecase,
      final String hashKeyJson,
      final String hashKey,
      final Adapter adapter,
      final String url,
      final String requestBody,
      final Map<String, String> httpHeaders)
      throws Exception {
    posterOutboundHttpAdapter.post(
        usecase, hashKeyJson, hashKey, adapter, url, requestBody, httpHeaders);
  }
}
