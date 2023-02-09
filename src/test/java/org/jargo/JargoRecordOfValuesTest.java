package org.jargo;

import java.util.List;

public class JargoRecordOfValuesTest {

  public static List<JargoRoundtripTestArguments> get() {
    return List.of(
        new JargoRoundtripTestArguments("byte", """
            {"_char":"f","_int":42,"_long":42,"_double":42.42,"_boolean":true,"string":"text"}""", new TestValues.Rec.ValueRecord(
            new TestValues.Value.CharValue('f'),
            new TestValues.Value.IntValue(42),
            new TestValues.Value.LongValue(42L),
            new TestValues.Value.DoubleValue(42.42),
            new TestValues.Value.BooleanValue(true),
            new TestValues.Value.StringValue("text")
        ))
    );
  }

}
