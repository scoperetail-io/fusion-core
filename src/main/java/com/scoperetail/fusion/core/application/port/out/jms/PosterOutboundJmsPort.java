/* ScopeRetail (C)2021 */
package com.scoperetail.fusion.core.application.port.out.jms;

public interface PosterOutboundJmsPort {

  void post(String brokerId, String queueName, String payload);
}
