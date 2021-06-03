/* ScopeRetail (C)2021 */
package com.scoperetail.fusion.core.adapter.out.web.http;

import java.util.Map;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;

public interface PosterOutboundHttpAdapter {
  
  @Retryable(value = {RuntimeException.class},
      maxAttemptsExpression = "#{${fusion.retryPolicies[0].maxAttempt}}",
      backoff = @Backoff(delayExpression = "#{${fusion.retryPolicies[0].backoffMS}}"))
  public void post(final String url, final String methodType, final String requestBody,
          final Map<String, String> httpHeaders) ;

  @Recover
  void recover(RuntimeException e, String message);
}
