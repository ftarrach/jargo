package org.jargo.internal;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.RecordComponent;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static org.jargo.internal.R.enumFromValue;
import static org.jargo.internal.R.getDefaultRecordConstructor;
import static org.jargo.internal.R.getNameToPositionMap;
import static org.jargo.internal.R.getRecordComponents;
import static org.jargo.internal.R.getTargetByName;
import static org.jargo.internal.R.isBoolean;
import static org.jargo.internal.R.isChar;
import static org.jargo.internal.R.isDouble;
import static org.jargo.internal.R.isInteger;
import static org.jargo.internal.R.isLong;
import static org.jargo.internal.R.isNotOptionalRecursive;
import static org.jargo.internal.R.isValueRecord;

@SuppressWarnings("unchecked")
public class JargoDeserializer {

  private final JsonFactory jsonFactory;

  public JargoDeserializer(JsonFactory jsonFactory) {
    this.jsonFactory = jsonFactory;
  }

  public <T> T deserialize(InputStream input, Class<?> target) throws IOException {
    JsonParser parser = jsonFactory.createParser(input);
    parser.nextToken();
    return doDeserialize(parser, new Target<>(target, List.of()));
  }

  private <T> T doDeserialize(JsonParser parser, Target<?> target) throws IOException {
    if (isInteger(target.clazz())) return (T) deserializeInt(parser);
    else if (isChar(target.clazz())) return (T) deserializeCharacter(parser);
    else if (isLong(target.clazz())) return (T) deserializeLong(parser);
    else if (isDouble(target.clazz())) return (T) deserializeDouble(parser);
    else if (isBoolean(target.clazz())) return (T) deserializeBoolean(parser);
    else if (String.class == target.clazz()) return (T) deserializeString(parser);
    else if (target.clazz().isEnum()) return (T) enumFromValue(target.clazz(), parser.getValueAsString());
    else if (List.class == target.clazz()) return (T) doDeserializeList(parser, target);
    else if (Set.class == target.clazz()) return (T) doDeserializeSet(parser, target);
    else if (Map.class == target.clazz()) return (T) doDeserializeMap(parser, target);
    else if (Optional.class == target.clazz()) return (T) deserializeOptional(parser, target);
    else if (Instant.class == target.clazz()) return (T) deserializeOffsetDateTime(parser);
    else if (target.clazz().isRecord()) return deserializeRecord(parser, (Target<? extends Record>) target);
    throw new IllegalStateException("can't deserialize " + target.clazz());
  }

  private Instant deserializeOffsetDateTime(JsonParser parser) throws IOException {
    String value = parser.getValueAsString();
    return Instant.parse(value);
  }

  private <T> List<T> doDeserializeList(JsonParser parser, Target<T> target) throws IOException {
    if (parser.isExpectedStartArrayToken()) {
      List<T> list = new ArrayList<>();
      parseIntoCollection(parser, target, list);
      return list;
    }
    throw new IOException("expected array, got " + parser.currentToken());
  }

  private <T> Set<T> doDeserializeSet(JsonParser parser, Target<T> target) throws IOException {
    if (parser.isExpectedStartArrayToken()) {
      Set<T> set = new HashSet<>();
      parseIntoCollection(parser, target, set);
      return set;
    }
    throw new IOException("expected array, got " + parser.currentToken());
  }

  private <T> void parseIntoCollection(JsonParser parser, Target<T> target, Collection<T> list) throws IOException {
    while (parser.nextToken() != JsonToken.END_ARRAY) {
      T item = doDeserialize(parser, new Target<>(target.generics().get(0), List.of()));
      list.add(item);
    }
  }

  private Map<?, ?> doDeserializeMap(JsonParser parser, Target<?> target) throws IOException {
    Map<Object, Object> map = new LinkedHashMap<>();
    Target<?> keyTarget = new Target<>(target.generics().get(0), List.of());
    Target<?> valueTarget = new Target<>(target.generics().get(1), List.of());
    while (parser.nextToken() != JsonToken.END_OBJECT) {
      Object fieldName = doDeserialize(parser, keyTarget);
      parser.nextToken();
      Object deserializedValue = doDeserialize(parser, valueTarget);
      map.put(fieldName, deserializedValue);
    }
    return map;
  }

  private <T> Optional<T> deserializeOptional(JsonParser parser, Target<T> target) throws IOException {
    Object o = doDeserialize(parser, new Target<>(target.generics().get(0), List.of()));
    return (Optional<T>) Optional.ofNullable(o);
  }

  private String deserializeString(JsonParser parser) throws IOException {
    return parser.getValueAsString();
  }

  private Boolean deserializeBoolean(JsonParser parser) throws IOException {
    return parser.getValueAsBoolean();
  }

  private Double deserializeDouble(JsonParser parser) throws IOException {
    return parser.getValueAsDouble();
  }

  private Long deserializeLong(JsonParser parser) throws IOException {
    return parser.getValueAsLong();
  }

  private Character deserializeCharacter(JsonParser parser) throws IOException {
    String stringValue = parser.getValueAsString();
    if (stringValue.length() == 1) return stringValue.charAt(0);
    throw new IOException("expected character, got " + parser.currentToken());
  }

  private Integer deserializeInt(JsonParser parser) throws IOException {
    return parser.getValueAsInt();
  }

  private <T> T deserializeRecord(JsonParser parser, Target<? extends Record> target) throws IOException {
    if (isValueRecord(target.clazz())) return (T) deserializeValueRecord(parser, target);
    else return (T) this.deserializeDataRecord(parser, target);
  }

  private <T extends Record> T deserializeValueRecord(JsonParser parser, Target<T> target) throws IOException {
    Class<T> targetClass = target.clazz();
    Constructor<T> defaultRecordConstructor = getDefaultRecordConstructor(targetClass);
    Target<?> itemTarget = R.getTargetByName(targetClass, "value");
    Object value = doDeserialize(parser, itemTarget);
    try {
      return defaultRecordConstructor.newInstance(value);
    } catch (InstantiationException | InvocationTargetException | IllegalAccessException e) {
      throw new IOException(e);
    }
  }

  private <T extends Record> T deserializeDataRecord(JsonParser parser, Target<T> target) throws IOException {
    Map<String, Integer> nameToPositionMap = getNameToPositionMap(target.clazz());
    Object[] parameters = new Object[nameToPositionMap.size()];
    parser.isExpectedStartObjectToken();
    RecordComponent[] recordComponents = getRecordComponents(target.clazz());
    Set<String> required = getRequiredTypes(recordComponents);
    while (parser.nextToken() != JsonToken.END_OBJECT) {
      String fieldName = parser.getValueAsString();
      parser.nextToken();
      Target<?> itemTarget = getTargetByName(target.clazz(), fieldName);
      Object deserializedValue = doDeserialize(parser, itemTarget);
      int pos = nameToPositionMap.get(fieldName);
      parameters[pos] = deserializedValue;
      required.remove(fieldName);
    }

    if (!required.isEmpty()) {
      throw new IOException("missing properties: " + required);
    }

    for (int i = 0; i < parameters.length; i++) {
      Object parameter = parameters[i];
      if (parameter == null) {
        // we already checked for required parameters above - this must be an optional
        parameters[i] = createEmpty(recordComponents[i].getType());
      }
    }
    Constructor<T> defaultRecordConstructor = getDefaultRecordConstructor(target.clazz());
    return createInstance(defaultRecordConstructor, parameters);
  }

  private Object createEmpty(Class<?> type) throws IOException {
    if (type == Optional.class) return Optional.empty();
    else if (type.isRecord()) {
      RecordComponent[] recordComponents = getRecordComponents(type);
      Constructor<? extends Record> defaultRecordConstructor = getDefaultRecordConstructor((Class<? extends Record>) type);
      Object[] params = new Object[defaultRecordConstructor.getParameterTypes().length];
      for (int i = 0; i < recordComponents.length; i++) {
        RecordComponent recordComponent = recordComponents[i];
        params[i] = createEmpty(recordComponent.getType());
      }
      return createInstance(defaultRecordConstructor, params);
    }
    throw new IllegalStateException();
  }

  private <T extends Record> T createInstance(Constructor<T> constructor, Object[] parameters) throws IOException {
    try {
      return constructor.newInstance(parameters);
    } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
      throw new IOException(e);
    }
  }

  private Set<String> getRequiredTypes(RecordComponent[] expectedTypes) {
    Set<String> required = new HashSet<>();
    for (RecordComponent component : expectedTypes)
      if (isNotOptionalRecursive(component.getType()))
        required.add(component.getName());
    return required;
  }

}
