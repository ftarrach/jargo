package org.jargo.internal;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.RecordComponent;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

class R {

  private record MetaRecord(RecordComponent[] components, Constructor<?> defaultConstructor,
                    Map<String, RecordComponent> componentByName, Map<String, Integer> nameToPosition) {}

  private static final Map<Class<?>, Boolean> IS_VALUE_RECORD = new ConcurrentHashMap<>();
  private static final Map<Class<?>, Map<String, Object>> ENUMS = new ConcurrentHashMap<>();
  private static final Map<Class<?>, MetaRecord> RECORDS = new ConcurrentHashMap<>();

  public static RecordComponent[] getRecordComponents(Class<?> clazz) {
    return initCache(clazz).components();
  }

  static boolean isValueRecord(Class<?> clazz) {
    Boolean result = IS_VALUE_RECORD.get(clazz);
    if (result != null) return result;
    result = false;
    if (clazz.isRecord()) {
      RecordComponent[] recordComponents = initCache(clazz).components();
      result = recordComponents.length == 1 && recordComponents[0].getName().equals("value");
    }
    IS_VALUE_RECORD.put(clazz, result);
    return result;
  }

  static Object getRecordComponentValue(Record record, String name) throws IOException {
    try {
      return initCache(record).componentByName().get(name).getAccessor().invoke(record);
    } catch (Exception e) {
      throw new IOException(e);
    }
  }

  @SuppressWarnings("unchecked")
  static <T> Constructor<T> getDefaultRecordConstructor(Class<T> recordClass) {
    return (Constructor<T>) initCache(recordClass).defaultConstructor();
  }

  static Map<String, Integer> getNameToPositionMap(Class<?> recordClass) {
    return initCache(recordClass).nameToPosition();
  }

  private static MetaRecord initCache(Record recordClass) {
    return initCache(recordClass.getClass());
  }

  private static MetaRecord initCache(Class<?> recordClass) {
    if (!RECORDS.containsKey(recordClass)) {
      RecordComponent[] recordComponents = recordClass.getRecordComponents();
      Map<String, Integer> nameToPositionMap = new HashMap<>();
      Map<String, RecordComponent> componentsByName = new HashMap<>();
      for (int i = 0; i < recordComponents.length; i++) {
        RecordComponent recordComponent = recordComponents[i];
        componentsByName.put(recordComponent.getName(), recordComponent);
        nameToPositionMap.put(recordComponent.getName(), i);
      }
      Constructor<?> defaultRecordConstructor = findDefaultRecordConstructor(recordClass);
      MetaRecord meta = new MetaRecord(recordComponents, defaultRecordConstructor, Map.copyOf(componentsByName), Map.copyOf(nameToPositionMap));
      RECORDS.put(recordClass, meta);
      return meta;
    }
    return RECORDS.get(recordClass);
  }

  private static Constructor<?> findDefaultRecordConstructor(Class<?> recordClass) {
    Constructor<?>[] constructors = recordClass.getConstructors();
    RecordComponent[] components = recordClass.getRecordComponents();
    Class<?>[] componentTypes = new Class<?>[components.length];
    for (int i = 0; i < components.length; i++) componentTypes[i] = components[i].getType();
    for (Constructor<?> constructor : constructors) {
      Class<?>[] parameterTypes = constructor.getParameterTypes();
      if (Arrays.equals(componentTypes, parameterTypes)) return constructor;
    }
    throw new IllegalStateException("unreachable state: record without default constructor");
  }

  static boolean isNotOptionalOrPresentOptional(Object value) throws IOException {
    if (value instanceof Optional<?> opt) return opt.isPresent();
    if (isValueRecord(value.getClass()))
      return isNotOptionalOrPresentOptional(getRecordComponentValue((Record) value, "value"));
    return true;
  }

  static boolean isNotOptionalRecursive(Class<?> clazz) {
    if (clazz == Optional.class) return false;
    if (isValueRecord(clazz)) {
      RecordComponent component = initCache(clazz).componentByName().get("value");
      return isNotOptionalRecursive(component.getType());
    }
    return true;
  }

  static Object enumFromValue(Class<?> clazz, String value) {
    if (ENUMS.containsKey(clazz)) return ENUMS.get(clazz).get(value);
    Map<String, Object> enumMapping = new HashMap<>();
    for (Object enumConstant : clazz.getEnumConstants()) enumMapping.put(enumConstant.toString(), enumConstant);
    ENUMS.put(clazz, Map.copyOf(enumMapping));
    return enumMapping.get(value);
  }

  static boolean isBoolean(Type target) {
    return boolean.class == target || Boolean.class == target;
  }

  static boolean isDouble(Type target) {
    return double.class == target || Double.class == target;
  }

  static boolean isLong(Type target) {
    return long.class == target || Long.class == target;
  }

  static boolean isInteger(Type target) {
    return int.class == target || Integer.class == target;
  }

  static boolean isChar(Type target) {
    return char.class == target || Character.class == target;
  }

  static Target<?> getTargetByName(Class<? extends Record> clazz, String name) {
    RecordComponent recordComponent = initCache(clazz).componentByName().get(name);
    Class<?> type = recordComponent.getType();
    if (List.class == type) {
      Class<?> generic = (Class<?>) ((ParameterizedType) recordComponent.getGenericType()).getActualTypeArguments()[0];
      return new Target<>(type, List.of(generic));
    } else if (Map.class == type) {
      Type[] typeArguments = ((ParameterizedType) recordComponent.getGenericType()).getActualTypeArguments();
      Class<?> key = (Class<?>) typeArguments[0];
      Class<?> value = (Class<?>) typeArguments[1];
      return new Target<>(type, List.of(key, value));
    } else if (Optional.class == type) {
      Class<?> generic = (Class<?>) ((ParameterizedType) recordComponent.getGenericType()).getActualTypeArguments()[0];
      return new Target<>(type, List.of(generic));
    }
    return new Target<>(type, List.of());
  }
}
