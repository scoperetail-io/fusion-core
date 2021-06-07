/* ScopeRetail (C)2021 */
package com.scoperetail.fusion.core.adapter.out.messaging.jms;

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
