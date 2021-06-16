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

import static com.scoperetail.fusion.messaging.adapter.in.messaging.jms.TaskResult.FAILURE;
import static com.scoperetail.fusion.messaging.adapter.in.messaging.jms.TaskResult.SUCCESS;
import static java.util.Optional.ofNullable;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.validation.Schema;

import org.w3c.dom.Document;
import com.scoperetail.fusion.messaging.adapter.in.messaging.jms.MessageListener;
import com.scoperetail.fusion.messaging.adapter.in.messaging.jms.TaskResult;
import com.scoperetail.fusion.messaging.adapter.out.messaging.jms.MessageRouterReceiver;
import com.scoperetail.fusion.core.common.JaxbUtil;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public abstract class AbstractMessageListener implements MessageListener<String> {

	private final MessageType messageType;
	private final Schema schema;
	private final String messageIdentifier;

	protected enum MessageType {
		XML, JSON
	}

	protected AbstractMessageListener(final String broker, final String queue, final MessageType messageType,
			final Schema schema, final String messageIdentifier, final MessageRouterReceiver messageRouterReceiver) {
		this.messageType = messageType;
		this.schema = schema;
		this.messageIdentifier = messageIdentifier;
		messageRouterReceiver.registerListener(broker, queue, this);
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
		}
		handleMessage(object, isValid);
		return isValid ? SUCCESS : FAILURE;
	}

	protected boolean validate(final String message) {
		boolean result = true;
		if (messageType.equals(MessageType.XML)) {
			result = JaxbUtil.isValidMessage(message, schema);
		}
		return result;
	}

	protected abstract void handleMessage(Object event, boolean isValid) throws Exception;

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
			canHandle = messageIdentifier.equals(rootElement);
		} catch (final Exception e) {
			log.error("Invalid XMl message: {} exception: {}", message, e);
		}
		return canHandle;
	}

	private boolean isValidJsonMessageIdentifier(final String message) {
		return true;
	}
}
