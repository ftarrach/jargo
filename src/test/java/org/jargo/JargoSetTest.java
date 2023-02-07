package org.jargo;

import java.util.List;

public class JargoSetTest {

  public static List<JargoRoundtripTestArguments> get() {
    return List.of(
        new JargoRoundtripTestArguments("primitive", """
            {"set":[42]}""", new TestValues.Li.SetOfPrimitive(List.of(42))),
        new JargoRoundtripTestArguments("values", """
            {"set":[42]}""", new TestValues.Li.SetOfValue(List.of(new TestValues.Value.IntValue(42)))),
        new JargoRoundtripTestArguments("record", """
            {"set":[{"_byte":42,"_char":"f","_short":42,"_int":42,"_long":42,"_double":42.42,"_boolean":true,"string":"text"}]}""",
            new TestValues.Li.SetOfRecord(List.of(
                new TestValues.Rec.PrimitiveRecord(
                    (byte) 42,
                    'f',
                    (short) 42,
                    42,
                    42,
                    42.42,
                    true,
                    "text"
                )
            )))
    );
  }
}
