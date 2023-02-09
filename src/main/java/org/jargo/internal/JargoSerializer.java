package org.jargo.internal;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;

import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.RecordComponent;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;

import static org.jargo.internal.R.getRecordComponentValue;
import static org.jargo.internal.R.isNotOptionalOrPresentOptional;
import static org.jargo.internal.R.isValueRecord;

public class JargoSerializer {

  private final JsonFactory jsonFactory;

  public JargoSerializer(JsonFactory jsonFactory) {
    this.jsonFactory = jsonFactory;
  }

  public void serialize(Object object, OutputStream outputStream) throws IOException {
    JsonGenerator generator = jsonFactory.createGenerator(outputStream);
    doSerialize(generator, object);
    generator.close();
  }

  private void doSerialize(JsonGenerator generator, Object object) throws IOException {
    if (object == null) { /* omit null values */ }
    else if (Character.class.equals(object.getClass())) generator.writeString(String.valueOf(object));
    else if (Integer.class.equals(object.getClass())) generator.writeNumber((int) object);
    else if (Long.class.equals(object.getClass())) generator.writeNumber((long) object);
    else if (Double.class.equals(object.getClass())) generator.writeNumber((double) object);
    else if (Boolean.class.equals(object.getClass())) generator.writeBoolean((boolean) object);
    else if (Instant.class.equals(object.getClass())) serializeOffsetDateTime(generator, (Instant) object);
    else if (object instanceof String string) generator.writeString(string);
    else if (object instanceof Record record) serializeRecord(generator, record);
    else if (object instanceof Enum<?> en) generator.writeString(en.name());
    else if (object instanceof Collection<?> list) serializeList(generator, list);
    else if (object instanceof Map<?, ?> map) serializeMap(generator, map);
    else if (object instanceof Optional<?> opt) serializeOptional(generator, opt);
    else throw new IllegalArgumentException("don't know how to serialize " + object.getClass());
  }

  private void serializeOffsetDateTime(JsonGenerator generator, Instant object) throws IOException {
    String value = DateTimeFormatter.ISO_INSTANT.format(object);
    generator.writeString(value);
  }

  private void serializeOptional(JsonGenerator generator, Optional<?> opt) throws IOException {
    if (opt.isPresent()) doSerialize(generator, opt.get());
    else generator.writeNull();
  }

  private void serializeRecord(JsonGenerator generator, Record record) throws IOException {
    if (isValueRecord(record.getClass())) doSerialize(generator, getRecordComponentValue(record, "value"));
    else doSerializeRecord(generator, record);
  }

  private void doSerializeRecord(JsonGenerator generator, Record record) throws IOException {
    RecordComponent[] recordComponents = record.getClass().getRecordComponents();
    generator.writeStartObject();
    for (int i = 0; i < recordComponents.length; i++) {
      serializeRecordComponent(generator, record, recordComponents, i);
    }
    generator.writeEndObject();
  }

  private void serializeRecordComponent(JsonGenerator generator, Record record, RecordComponent[] recordComponents, int i) throws IOException {
    String name = recordComponents[i].getName();
    Object value = getRecordComponentValue(record, name);
    if (isNotOptionalOrPresentOptional(value)) {
      generator.writeFieldName(name);
      doSerialize(generator, value);
    }
  }

  private void serializeList(JsonGenerator generator, Collection<?> list) throws IOException {
    generator.writeStartArray();
    for (Object item : list) doSerialize(generator, item);
    generator.writeEndArray();
  }

  private void serializeMap(JsonGenerator generator, Map<?, ?> map) throws IOException {
    generator.writeStartObject();
    for (Map.Entry<?, ?> entry : map.entrySet()) {
      Object key = entry.getKey();
      Object value = entry.getValue();
      serializeKeyValue(generator, key, value);
    }
    generator.writeEndObject();
  }

  private void serializeKeyValue(JsonGenerator generator, Object key, Object value) throws IOException {
    if (key instanceof Enum<?> en) generator.writeFieldName(en.name());
    else if (key instanceof String stringKey) generator.writeFieldName(stringKey);
    else throw new IOException("expected string key or enum key in map, got " + key.getClass());
    doSerialize(generator, value);
  }

}
