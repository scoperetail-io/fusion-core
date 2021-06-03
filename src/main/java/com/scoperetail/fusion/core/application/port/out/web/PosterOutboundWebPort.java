/* ScopeRetail (C)2021 */
package com.scoperetail.fusion.core.application.port.out.web;

import java.util.Map;

public interface PosterOutboundWebPort {

  void post(String url, String methodType, String requestBody, Map<String, String> httpHeaders);
}
