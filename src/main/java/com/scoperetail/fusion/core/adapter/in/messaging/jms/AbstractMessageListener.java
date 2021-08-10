package com.scoperetail.fusion.core.adapter.in.messaging.jms;

import static com.scoperetail.fusion.messaging.adapter.in.messaging.jms.TaskResult.DISCARD;
import static com.scoperetail.fusion.messaging.adapter.in.messaging.jms.TaskResult.SUCCESS;
import static java.util.Optional.ofNullable;
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
import com.scoperetail.fusion.core.common.Event;
import com.scoperetail.fusion.core.common.JaxbUtil;
import com.scoperetail.fusion.core.common.JsonUtils;
import com.scoperetail.fusion.messaging.adapter.in.messaging.jms.MessageListener;
import com.scoperetail.fusion.messaging.adapter.in.messaging.jms.TaskResult;
import com.scoperetail.fusion.messaging.adapter.out.messaging.jms.MessageRouterReceiver;
import com.scoperetail.fusion.messaging.config.Adapter;
import com.scoperetail.fusion.messaging.config.Adapter.MessageType;
import com.scoperetail.fusion.messaging.config.FusionConfig;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public abstract class AbstractMessageListener implements MessageListener<String> {

  private final MessageType messageType;
  private final Schema schema;
  private final List<String> messageIdentifiers;

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
