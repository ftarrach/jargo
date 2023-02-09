package org.jargo;

import java.util.List;

public class JargoRecordWrappedValueTest {

  public static List<JargoRoundtripTestArguments> get() {
    return List.of(
        new JargoRoundtripTestArguments("primitives", """
            {"_char":"f","_int":42,"_long":42,"_double":42.42,"_boolean":true,"string":"text"}""", new TestValues.Rec.WrappedValueRecord(
            new TestValues.WrappedValue.WrappedCharValue(new TestValues.Value.CharValue('f')),
            new TestValues.WrappedValue.WrappedIntValue(new TestValues.Value.IntValue(42)),
            new TestValues.WrappedValue.WrappedLongValue(new TestValues.Value.LongValue(42)),
            new TestValues.WrappedValue.WrappedDoubleValue(new TestValues.Value.DoubleValue(42.42)),
            new TestValues.WrappedValue.WrappedBooleanValue(new TestValues.Value.BooleanValue(true)),
            new TestValues.WrappedValue.WrappedStringValue(new TestValues.Value.StringValue("text"))
        ))
    );
  }

}
