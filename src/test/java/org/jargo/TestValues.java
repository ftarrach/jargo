package org.jargo;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public class TestValues {

  public static class En {
    public enum Colors { RED, GREEN, BLUE }
  }

  public static class Li {
    public record ListOfPrimitive(List<Integer> list) {}
    public record ListOfValue(List<Value.IntValue> list) {}
    public record ListOfRecord(List<Rec.PrimitiveRecord> list) {}

    public record SetOfPrimitive(List<Integer> set) {}
    public record SetOfValue(List<Value.IntValue> set) {}
    public record SetOfRecord(List<Rec.PrimitiveRecord> set) {}
  }

  public static class Value {
    public record ByteValue(byte value) {}
    public record CharValue(char value) {}
    public record ShortValue(short value) {}
    public record IntValue(int value) {}
    public record LongValue(long value) {}
    public record DoubleValue(double value) {}
    public record BooleanValue(boolean value) {}
    public record StringValue(String value) {}
  }

  public static class OptionalValue {
    public record OptionalString(Optional<String> value) {}
  }

  public static class Rec {
    public record PrimitiveRecord(
        byte _byte,
        char _char,
        short _short,
        int _int,
        long _long,
        double _double,
        boolean _boolean,
        String string
    ) {}

    public record ValueRecord(
        Value.ByteValue _byte,
        Value.CharValue _char,
        Value.ShortValue _short,
        Value.IntValue _int,
        Value.LongValue _long,
        Value.DoubleValue _double,
        Value.BooleanValue _boolean,
        Value.StringValue string
    ) {}

    public record WrappedValueRecord(
        WrappedValue.WrappedByteValue _byte,
        WrappedValue.WrappedCharValue _char,
        WrappedValue.WrappedShortValue _short,
        WrappedValue.WrappedIntValue _int,
        WrappedValue.WrappedLongValue _long,
        WrappedValue.WrappedDoubleValue _double,
        WrappedValue.WrappedBooleanValue _boolean,
        WrappedValue.WrappedStringValue string
    ) {}

    public record OptionalRecord(
        Optional<String> string
    ) {}

    public record OptionalRecordValue(
        OptionalValue.OptionalString string
    ) {}
  }

  public static class WrappedValue {
    public record WrappedByteValue(Value.ByteValue value) {}
    public record WrappedCharValue(Value.CharValue value) {}
    public record WrappedShortValue(Value.ShortValue value) {}
    public record WrappedIntValue(Value.IntValue value) {}
    public record WrappedLongValue(Value.LongValue value) {}
    public record WrappedDoubleValue(Value.DoubleValue value) {}
    public record WrappedBooleanValue(Value.BooleanValue value) {}
    public record WrappedStringValue(Value.StringValue value) {}
  }

  public static class MapValue {

    public record StringIntMapValue(Map<String, Integer> value) { }
    public record EnumMapValue(Map<En.Colors, String> value) { }
  }
}
