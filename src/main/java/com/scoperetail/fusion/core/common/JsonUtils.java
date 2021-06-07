/* ScopeRetail (C)2021 */
package com.scoperetail.fusion.core.common;

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

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalTimeSerializer;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public final class JsonUtils {

  static final ObjectMapper mapper;

  private JsonUtils() {}

  static {
    mapper = new ObjectMapper();
    mapper.setSerializationInclusion(Include.NON_NULL);
    final JavaTimeModule module = new JavaTimeModule();
    mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    module.addDeserializer(
        LocalDateTime.class, new LocalDateTimeDeserializer(DateTimeFormatter.ISO_DATE_TIME));
    module.addSerializer(
        LocalDateTime.class, new LocalDateTimeSerializer(DateTimeFormatter.ISO_DATE_TIME));
    module.addDeserializer(LocalTime.class, new LocalTimeDeserializer(DateTimeFormatter.ISO_TIME));
    module.addSerializer(LocalTime.class, new LocalTimeSerializer(DateTimeFormatter.ISO_TIME));
  }

  public static final <T> T unmarshal(
      final Optional<String> message, final Optional<TypeReference<T>> typeReference)
      throws IOException {
    final String incomingMessage =
        message.orElseThrow(() -> new IOException("Unable to unmarshal :: Message = null"));
    final TypeReference<T> incomingType =
        typeReference.orElseThrow(() -> new IOException("Unable to unmarshal :: Type = null"));
    // log.debug("Trying to unmarshal json message {} into {} type",
    // incomingMessage, incomingType);
    return mapper.readValue(incomingMessage, incomingType);
  }

  public static final <T> T unmarshal(final Optional<String> message, final String canonicalName)
      throws IOException {
    final String incomingMessage =
        message.orElseThrow(() -> new IOException("Unable to unmarshal :: Message = null"));
    final JavaType javaType = TypeFactory.defaultInstance().constructFromCanonical(canonicalName);
    // log.debug("Trying to unmarshal json message {} into {} type",
    // incomingMessage, javaType);
    return mapper.readValue(incomingMessage, javaType);
  }

  public static <E> String marshal(final Optional<E> obj) throws IOException {
    mapper.setSerializationInclusion(Include.NON_NULL);
    return mapper.writeValueAsString(
        obj.orElseThrow(() -> new IOException("Unable to marshal :: obj = null")));
  }
}
