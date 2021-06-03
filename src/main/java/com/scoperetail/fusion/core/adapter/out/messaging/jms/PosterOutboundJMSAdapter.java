/* ScopeRetail (C)2021 */
package com.scoperetail.fusion.core.adapter.out.messaging.jms;

import com.scoperetail.fusion.core.application.port.out.jms.PosterOutboundJmsPort;
import com.scoperetail.fusion.messaging.adapter.out.messaging.jms.MessageRouterSender;
import com.scoperetail.fusion.shared.kernel.common.annotation.MessagingAdapter;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@MessagingAdapter
@AllArgsConstructor
@Slf4j
public class PosterOutboundJMSAdapter implements PosterOutboundJmsPort {

  private MessageRouterSender messageSender;

  @Override
  public void post(final String brokerId, final String queueName, final String payload) {
    messageSender.send(brokerId, queueName, payload);
    log.trace("Sent Message to Broker Id:{}  Queue: {} Message: {}", brokerId, queueName, payload);
  }
}
