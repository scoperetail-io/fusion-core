package com.scoperetail.fusion.core.adapter.in.messaging.jms;

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

import static com.scoperetail.fusion.messaging.adapter.in.messaging.jms.TaskResult.DISCARD;
import static com.scoperetail.fusion.messaging.adapter.in.messaging.jms.TaskResult.SUCCESS;
import static java.util.Optional.ofNullable;
import static org.apache.commons.lang3.StringUtils.isNotBlank;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Optional;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.validation.Schema;
import org.apache.commons.collections.CollectionUtils;
import org.w3c.dom.Document;
import com.scoperetail.fusion.config.Adapter;
import com.scoperetail.fusion.config.Adapter.MessageType;
import com.scoperetail.fusion.config.FusionConfig;
import com.scoperetail.fusion.core.common.Event;
import com.scoperetail.fusion.core.common.JaxbUtil;
import com.scoperetail.fusion.core.common.JsonUtils;
import com.scoperetail.fusion.messaging.adapter.in.messaging.jms.MessageListener;
import com.scoperetail.fusion.messaging.adapter.in.messaging.jms.TaskResult;
import com.scoperetail.fusion.messaging.adapter.out.messaging.jms.MessageRouterReceiver;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public abstract class AbstractMessageListener implements MessageListener<String> {

  private static final String BO_QUEUE_SUFFIX = ".BO";
  private final MessageType messageType;
  private final Schema schema;
  private final List<String> messageIdentifiers;
  private final String boBrokerId;
  private final String boQueueName;

  protected AbstractMessageListener(
      final String usecase,
      final Schema schema,
      final MessageRouterReceiver messageRouterReceiver,
      final FusionConfig fusionConfig) {
    final Optional<Adapter> optAdapter = fusionConfig.getInboundAdapter(usecase);
    final Adapter adapter =
        optAdapter.orElseThrow(
            () -> new RuntimeException("Inbound adapter not found for usecase:" + usecase));
    this.messageType = adapter.getMessageType();
    this.messageIdentifiers = adapter.getMessageIdentifiers();
    this.boBrokerId =
        isNotBlank(adapter.getBoBrokerId()) ? adapter.getBoBrokerId() : adapter.getBrokerId();
    this.boQueueName =
        isNotBlank(adapter.getBoQueueName())
            ? adapter.getBoQueueName()
            : adapter.getQueueName() + BO_QUEUE_SUFFIX;
    this.schema = schema;
    messageRouterReceiver.registerListener(adapter.getBrokerId(), adapter.getQueueName(), this);
  }

  @Override
  public boolean canHandle(final String message) {
    boolean canHandle = false;
    switch (messageType) {
      case XML:
        canHandle = isValidXmlMessageIdentifier(message);
        break;
      case JSON:
        canHandle = isValidJsonMessageIdentifier(message);
        break;
      default:
        canHandle = false;
        log.error("Invalid message type: {}", messageType);
    }
    return canHandle;
  }

  @Override
  public TaskResult doTask(final String message) throws Exception {
    Object object = message;
    boolean isValid = validate(message);
    if (isValid) {
      try {
        if (schema != null) {
          object = JaxbUtil.unmarshal(ofNullable(message), ofNullable(schema), getClazz());
        }
      } catch (final Exception t) {
        log.error("Unable to unmarshal incoming message: {} Exception occured: {}", message, t);
        isValid = false;
      }
      if (isValid) {
        handleMessage(object);
      }
    }
    return isValid ? SUCCESS : DISCARD;
  }

  protected boolean validate(final String message) {
    boolean result = true;
    if (messageType.equals(MessageType.XML)) {
      result = JaxbUtil.isValidMessage(message, schema);
    }
    return result;
  }

  protected abstract void handleMessage(Object event) throws Exception;

  protected Class getClazz() {
    return String.class;
  }

  protected String getBoBrokerId() {
    return boBrokerId;
  }

  protected String getBoQueueName() {
    return boQueueName;
  }

  private boolean isValidXmlMessageIdentifier(final String message) {
    boolean canHandle = false;
    try {
      final DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
      final DocumentBuilder documentBuilder = dbf.newDocumentBuilder();
      final InputStream is = new ByteArrayInputStream(message.getBytes());
      final Document document = documentBuilder.parse(is);
      final String rootElement = document.getDocumentElement().getNodeName();
      canHandle = messageIdentifiers.contains(rootElement);
    } catch (final Exception e) {
      log.error("Invalid XMl message: {} exception: {}", message, e);
    }
    return canHandle;
  }

  private boolean isValidJsonMessageIdentifier(final String message) {
    boolean canHandle = false;
    if (CollectionUtils.isNotEmpty(messageIdentifiers)) {
      try {
        final Event event = unmarshal(message);
        canHandle = messageIdentifiers.contains(event.getEventName());
      } catch (final Exception e) {
        log.error("Invalid JSON message: {} exception: {}", message, e);
      }
    } else {
      canHandle = true;
    }
    return canHandle;
  }

  private Event unmarshal(final Object message) throws IOException {
    return JsonUtils.unmarshal(
        Optional.ofNullable(message.toString()), Event.class.getCanonicalName());
  }
}
