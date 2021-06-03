/* ScopeRetail (C)2021 */
package com.scoperetail.fusion.core.common;

import static java.nio.charset.StandardCharsets.UTF_8;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import javax.xml.XMLConstants;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;

import org.xml.sax.SAXException;

import com.sun.xml.bind.marshaller.DataWriter;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class JaxbUtil {

	private static final Map<String, JAXBContext> contextMap = new HashMap<>(5);

	private JaxbUtil() {
	}

	public static Schema getSchema(final Class clazz, final String xsdName) throws SAXException {
		final SchemaFactory sf = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
		Schema schema = null;
		try {
			schema = sf.newSchema(clazz.getClassLoader().getResource(xsdName));
		} catch (final SAXException e) {
			log.error(e.getMessage(), e);
			throw e;
		}
		return schema;
	}

	/**
	 * Validate the message based on XML schema
	 *
	 * @param message
	 * @param schema
	 * @return validationStatus
	 */
	public static boolean isValidMessage(final String message, final Schema schema) {
		Boolean validationStatus = Boolean.FALSE;
		try {
			final Validator validator = Optional.ofNullable(schema)
					.orElseThrow(() -> new IllegalArgumentException("Schema object not provided.")).newValidator();
			validator.validate(new StreamSource(new ByteArrayInputStream(
					Optional.ofNullable(message).orElseThrow(SAXException::new).getBytes(UTF_8))));
			validationStatus = Boolean.TRUE;
			log.debug("Message Validated successfully");
		} catch (SAXException | IOException e) {
			log.error("Inbound message validation Failed::{}", e);
		}
		return validationStatus;
	}

	/**
	 * Converts XML message to object based on schema
	 *
	 * @param messageOpt
	 * @param schemaOpt
	 * @param clazz
	 * @return
	 * @throws SAXException
	 * @throws JAXBException
	 */
	public static final <T> T unmarshal(final Optional<String> messageOpt, final Optional<Schema> schemaOpt,
			final Class<T> clazz) throws SAXException, JAXBException {
		final JAXBContext jaxbContext = getCachedContext(clazz);
		final StringReader sr = new StringReader(messageOpt.orElseThrow(SAXException::new));
		final Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
		unmarshaller
				.setSchema(schemaOpt.orElseThrow(() -> new IllegalArgumentException("Schema object not provided.")));
		final JAXBElement<T> root = unmarshaller.unmarshal(new StreamSource(sr), clazz);
		log.debug("Message Unmarshaled to {}", clazz.getSimpleName());
		return root.getValue();
	}

	/**
	 * Cache the jaxbContext in a hashmap to reduce metaspace footprint.
	 *
	 * @param clazz
	 * @return jaxbContext
	 * @throws JAXBException
	 */
	private static JAXBContext getCachedContext(final Class clazz) throws JAXBException {
		final JAXBContext jaxbContext;
		if (contextMap.containsKey(clazz.getName())) {
			jaxbContext = contextMap.get(clazz.getName());
		} else {
			jaxbContext = JAXBContext.newInstance(clazz);
			contextMap.put(clazz.getName(), jaxbContext);
		}
		return jaxbContext;
	}

	/**
	 * Converts object to XML message.
	 *
	 * @param clazz
	 * @param element
	 * @return
	 * @throws JAXBException
	 */
	public static final <T> Optional<String> marshal(final Class<T> clazz, final Object element) throws JAXBException {
		final StringWriter sw = new StringWriter();
		final JAXBContext jaxbContext = getCachedContext(clazz);
		final Marshaller marshaller = jaxbContext.createMarshaller();
		marshaller.marshal(element, sw);
		log.debug("Message marshalled to {}", clazz.getSimpleName());
		return Optional.of(sw.toString());
	}

	/**
	 * General purpose marshal method to marshal XML with CDATA
	 *
	 * @param clazz
	 * @param element
	 * @return
	 * @throws JAXBException
	 */
	public static final <T> Optional<String> marshalForCDATA(final Class<T> clazz, final Object element)
			throws JAXBException {
		log.debug("Marshalling Object to message::");
		final JAXBContext jaxbContext = getCachedContext(clazz);
		final Marshaller marshaller = jaxbContext.createMarshaller();
		marshaller.setProperty(Marshaller.JAXB_ENCODING, "UTF-8");
		marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
		final StringWriter stringWriter = new StringWriter();
		final PrintWriter printWriter = new PrintWriter(stringWriter);
		final DataWriter dataWriter = new DataWriter(printWriter, "UTF-8",
				(buf, start, len, b, out) -> out.write(buf, start, len));
		marshaller.marshal(element, dataWriter);
		log.debug("Message marshalled to {}", clazz.getSimpleName());
		return Optional.of(stringWriter.toString());
	}
}
