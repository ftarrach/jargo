package org.jargo.internal;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.RecordComponent;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

class R {

  record MetaRecord(
      RecordComponent[] components,
      Constructor<? extends Record> defaultConstructor,
      Map<String, RecordComponent> componentByName,
      Map<String, Integer> nameToPosition
  ) {}

  private static final Set<Class<?>> VALUE_RECORDS = ConcurrentHashMap.newKeySet();
  private static final Map<Class<?>, Map<String, Object>> ENUMS = new ConcurrentHashMap<>();
  private static final Map<Class<? extends Record>, MetaRecord> RECORDS = new ConcurrentHashMap<>();

  static boolean isValueRecord(Class<?> recordClass) {
    if (VALUE_RECORDS.contains(recordClass)) return true;
    if (recordClass.isRecord()) {
      RecordComponent[] recordComponents = initCache((Class<? extends Record>) recordClass).components();
      boolean isValueRecord = recordComponents.length == 1 && recordComponents[0].getName().equals("value");
      if (isValueRecord) {
        VALUE_RECORDS.add(recordClass);
      }
      return isValueRecord;
    }
    return false;
  }

  static Object getRecordComponentValue(Record record, String name) throws IOException {
    try {
      return initCache(record).componentByName().get(name).getAccessor().invoke(record);
    } catch (Exception e) {
      throw new IOException(e);
    }
  }

  @Deprecated(forRemoval = true)
  static Object getRecordComponentValue(Record record, int i) throws IOException {
    try {
      var recordComponents = initCache(record).components();
      Method accessor = recordComponents[i].getAccessor();
      return accessor.invoke(record);
    } catch (Exception e) {
      throw new IOException(e);
    }
  }

  static <T extends Record> Constructor<T> getDefaultRecordConstructor(Class<T> recordClass) {
    return (Constructor<T>) initCache(recordClass).defaultConstructor();
  }

  static Map<String, Integer> getNameToPositionMap(Class<? extends Record> recordClass) {
    return initCache(recordClass).nameToPosition();
  }

  static RecordComponent getRecordComponentByName(Class<? extends Record> recordClass, String name) throws IOException {
    return initCache(recordClass).componentByName().get(name);
  }

  private static MetaRecord initCache(Record recordClass) {
    return initCache(recordClass.getClass());
  }

  private static MetaRecord initCache(Class<? extends Record> recordClass) {
    if (!RECORDS.containsKey(recordClass)) {
      RecordComponent[] recordComponents = recordClass.getRecordComponents();
      Map<String, Integer> nameToPositionMap = new HashMap<>();
      Map<String, RecordComponent> componentsByName = new HashMap<>();
      for (int i = 0; i < recordComponents.length; i++) {
        RecordComponent recordComponent = recordComponents[i];
        componentsByName.put(recordComponent.getName(), recordComponent);
        nameToPositionMap.put(recordComponent.getName(), i);
      }
      Constructor<? extends Record> defaultRecordConstructor = findDefaultRecordConstructor(recordClass);
      MetaRecord meta = new MetaRecord(recordComponents, defaultRecordConstructor, Map.copyOf(componentsByName), Map.copyOf(nameToPositionMap));
      RECORDS.put(recordClass, meta);
      return meta;
    }
    return RECORDS.get(recordClass);
  }

  private static <T extends Record> Constructor<T> findDefaultRecordConstructor(Class<T> recordClass) {
    Constructor<T>[] constructors = (Constructor<T>[]) recordClass.getConstructors();
    RecordComponent[] components = recordClass.getRecordComponents();
    Class<?>[] componentTypes = new Class<?>[components.length];
    for (int i = 0; i < components.length; i++) {
      RecordComponent component = components[i];
      componentTypes[i] = component.getType();
    }
    for (Constructor<T> constructor : constructors) {
      Class<?>[] parameterTypes = constructor.getParameterTypes();
      if (Arrays.equals(componentTypes, parameterTypes)) {
        return constructor;
      }
    }
    throw new IllegalStateException("unreachable state: record without default constructor");
  }

  static boolean isNotOptionalOrPresentOptional(Object value) throws IOException {
    if (value instanceof Optional<?> opt) return opt.isPresent();
    if (isValueRecord(value.getClass()))
      return isNotOptionalOrPresentOptional(getRecordComponentValue((Record) value, 0));
    return true;
  }

  static boolean isNotOptionalRecursive(Class<?> clazz) throws IOException {
    if (clazz == Optional.class) return false;
    if (isValueRecord(clazz)) {
      RecordComponent component = getRecordComponentByName((Class<? extends Record>) clazz, "value");
      return isNotOptionalRecursive(component.getType());
    }
    return true;
  }

  static Object enumFromValue(Class<?> clazz, String value) {
    if (ENUMS.containsValue(clazz)) {
      return ENUMS.get(clazz).get(value);
    }
    Map<String, Object> enumMapping = new HashMap<>();
    for (Object enumConstant : clazz.getEnumConstants()) {
      enumMapping.put(enumConstant.toString(), enumConstant);
    }
    ENUMS.put(clazz, Map.copyOf(enumMapping));
    return enumMapping.get(value);
  }

  static <T> boolean isBoolean(Type target) {
    return boolean.class == target || Boolean.class == target;
  }

  static <T> boolean isDouble(Type target) {
    return double.class == target || Double.class == target;
  }

  static <T> boolean isLong(Type target) {
    return long.class == target || Long.class == target;
  }

  static <T> boolean isInteger(Type target) {
    return int.class == target || Integer.class == target;
  }

  static <T> boolean isChar(Type target) {
    return char.class == target || Character.class == target;
  }

  static <T> boolean isShort(Type target) {
    return short.class == target || Short.class == target;
  }

  static <T> boolean isByte(Type target) {
    return byte.class == target || Byte.class == target;
  }
}
