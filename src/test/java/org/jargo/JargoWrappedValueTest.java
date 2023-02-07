package org.jargo;

import java.util.List;

public class JargoWrappedValueTest {

  public static List<JargoRoundtripTestArguments> get() {
    return List.of(
        new JargoRoundtripTestArguments("byte", "42", new TestValues.WrappedValue.WrappedByteValue(new TestValues.Value.ByteValue((byte) 42))),
        new JargoRoundtripTestArguments("char", "\"f\"", new TestValues.WrappedValue.WrappedCharValue(new TestValues.Value.CharValue('f'))),
        new JargoRoundtripTestArguments("short", "42", new TestValues.WrappedValue.WrappedShortValue(new TestValues.Value.ShortValue((short) 42))),
        new JargoRoundtripTestArguments("int", "42", new TestValues.WrappedValue.WrappedIntValue(new TestValues.Value.IntValue(42))),
        new JargoRoundtripTestArguments("long", "42", new TestValues.WrappedValue.WrappedLongValue(new TestValues.Value.LongValue(42))),
        new JargoRoundtripTestArguments("double", "42.42", new TestValues.WrappedValue.WrappedDoubleValue(new TestValues.Value.DoubleValue(42.42))),
        new JargoRoundtripTestArguments("boolean", "true", new TestValues.WrappedValue.WrappedBooleanValue(new TestValues.Value.BooleanValue(true))),
        new JargoRoundtripTestArguments("string", "\"text\"", new TestValues.WrappedValue.WrappedStringValue(new TestValues.Value.StringValue("text")))
    );
  }

}
