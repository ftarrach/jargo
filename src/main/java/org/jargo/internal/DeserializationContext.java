package org.jargo.internal;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.core.JsonTokenId;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.RecordComponent;
import java.text.MessageFormat;
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
class DeserializationContext {

  private final JsonParser parser;
  private final Stack<String> stack = new Stack<>();

  public DeserializationContext(JsonParser parser) {
    this.parser = parser;
  }

  <T> T doDeserialize(String ctx, Target<?> target) throws IOException {
    stack.push(ctx);
    Object result;
    if (isInteger(target.clazz())) result = deserializeInt();
    else if (isChar(target.clazz())) result = deserializeCharacter();
    else if (isLong(target.clazz())) result = deserializeLong();
    else if (isDouble(target.clazz())) result = deserializeDouble();
    else if (isBoolean(target.clazz())) result = deserializeBoolean();
    else if (String.class == target.clazz()) result = deserializeString();
    else if (target.clazz().isEnum()) result = enumFromValue(target.clazz(), parser.getValueAsString());
    else if (List.class == target.clazz()) result = doDeserializeList(target);
    else if (Set.class == target.clazz()) result = doDeserializeSet(target);
    else if (Map.class == target.clazz()) result = doDeserializeMap(target);
    else if (Optional.class == target.clazz()) result = deserializeOptional(target);
    else if (Instant.class == target.clazz()) result = deserializeOffsetDateTime();
    else if (target.clazz().isRecord()) result = deserializeRecord((Target<? extends Record>) target);
    else throw new IllegalStateException("can't deserialize " + target.clazz());
    stack.pop();
    return (T) result;
  }

  private Instant deserializeOffsetDateTime() throws IOException {
    String value = parser.getValueAsString();
    return Instant.parse(value);
  }

  private <T> List<T> doDeserializeList(Target<T> target) throws IOException {
    if (parser.isExpectedStartArrayToken()) {
      List<T> list = new ArrayList<>();
      parseIntoCollection(target, list);
      return list;
    }
    throw unmetExpectation("array");
  }

  private IOException unmetExpectation(String expectation) {
    String description = switch (parser.currentToken()) {
      case NOT_AVAILABLE -> "n/A";
      case START_OBJECT -> "object";
      case END_OBJECT -> "end of object";
      case START_ARRAY -> "array";
      case END_ARRAY -> "end of array";
      case FIELD_NAME -> "field name";
      case VALUE_EMBEDDED_OBJECT -> "embedded object";
      case VALUE_STRING -> "string";
      case VALUE_NUMBER_INT -> "int";
      case VALUE_NUMBER_FLOAT -> "float";
      case VALUE_TRUE -> "true";
      case VALUE_FALSE -> "false";
      case VALUE_NULL -> "null";
    };
    String message = MessageFormat.format("""
        {0}:
          expected {1}, got {2}""", stack.describe(), expectation, description);
    return new IOException(message);
  }

  private <T> Set<T> doDeserializeSet(Target<T> target) throws IOException {
    if (parser.isExpectedStartArrayToken()) {
      Set<T> set = new HashSet<>();
      parseIntoCollection(target, set);
      return set;
    }
    throw unmetExpectation("array");
  }

  private <T> void parseIntoCollection(Target<T> target, Collection<T> list) throws IOException {
    int i = 0;
    while (parser.nextToken() != JsonToken.END_ARRAY) {
      T item = doDeserialize("[" + i + "]", new Target<>(target.generics().get(0), List.of()));
      list.add(item);
    }
  }

  private Map<?, ?> doDeserializeMap(Target<?> target) throws IOException {
    Map<Object, Object> map = new LinkedHashMap<>();
    Target<?> keyTarget = new Target<>(target.generics().get(0), List.of());
    Target<?> valueTarget = new Target<>(target.generics().get(1), List.of());
    while (parser.nextToken() != JsonToken.END_OBJECT) {
      Object fieldName = doDeserialize("(key)", keyTarget);
      parser.nextToken();
      Object deserializedValue = doDeserialize("(value)", valueTarget);
      map.put(fieldName, deserializedValue);
    }
    return map;
  }

  private <T> Optional<T> deserializeOptional(Target<T> target) throws IOException {
    Object o = doDeserialize("(optional)", new Target<>(target.generics().get(0), List.of()));
    return (Optional<T>) Optional.ofNullable(o);
  }

  private String deserializeString() throws IOException {
    int currentTokenId = parser.currentTokenId();
    if (currentTokenId == JsonTokenId.ID_STRING || currentTokenId == JsonTokenId.ID_FIELD_NAME) {
      return parser.getValueAsString();
    }
    throw unmetExpectation("string");
  }

  private Boolean deserializeBoolean() throws IOException {
    int currentTokenId = parser.currentTokenId();
    if (currentTokenId == JsonTokenId.ID_TRUE || currentTokenId == JsonTokenId.ID_FALSE) {
      return parser.getValueAsBoolean();
    }
    throw unmetExpectation("boolean");
  }

  private Double deserializeDouble() throws IOException {
    return parser.getValueAsDouble();
  }

  private Long deserializeLong() throws IOException {
    return parser.getValueAsLong();
  }

  private Character deserializeCharacter() throws IOException {
    String stringValue = parser.getValueAsString();
    if (stringValue.length() == 1) return stringValue.charAt(0);
    throw unmetExpectation("character");
  }

  private Integer deserializeInt() throws IOException {
    if (parser.currentToken().isNumeric()) {
      return parser.getValueAsInt();
    }
    throw unmetExpectation("number");
  }

  private <T> T deserializeRecord(Target<? extends Record> target) throws IOException {
    stack.push("{");
    Object result;
    if (isValueRecord(target.clazz())) result = deserializeValueRecord(target);
    else result = this.deserializeDataRecord(target);
    return (T) result;
  }

  private <T extends Record> T deserializeValueRecord(Target<T> target) throws IOException {
    Class<T> targetClass = target.clazz();
    Constructor<T> defaultRecordConstructor = getDefaultRecordConstructor(targetClass);
    Target<?> itemTarget = R.getTargetByName(targetClass, "value").orElseThrow();
    Object value = doDeserialize("(value)", itemTarget);
    return createInstance(defaultRecordConstructor, new Object[]{value});
  }

  private <T extends Record> T deserializeDataRecord(Target<T> target) throws IOException {
    Map<String, Integer> nameToPositionMap = getNameToPositionMap(target.clazz());
    Object[] parameters = new Object[nameToPositionMap.size()];
    parser.isExpectedStartObjectToken();
    RecordComponent[] recordComponents = getRecordComponents(target.clazz());
    Set<String> required = getRequiredTypes(recordComponents);
    while (parser.nextToken() != JsonToken.END_OBJECT) {
      String fieldName = parser.getValueAsString();
      parser.nextToken();
      Optional<Target<?>> itemTarget = getTargetByName(target.clazz(), fieldName);
      if (itemTarget.isPresent()) {
        Object deserializedValue = doDeserialize(fieldName, itemTarget.get());
        int pos = nameToPositionMap.get(fieldName);
        parameters[pos] = deserializedValue;
        required.remove(fieldName);
      } else {
        throw new IOException("json contained unknown component '" + fieldName + "' in " + target.clazz());
      }
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
      if (isNotOptionalRecursive(component.getType())) required.add(component.getName());
    return required;
  }
}

