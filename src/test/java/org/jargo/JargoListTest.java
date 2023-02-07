package org.jargo;

import java.util.List;

public class JargoListTest {

  public static List<JargoRoundtripTestArguments> get() {
    return List.of(
        new JargoRoundtripTestArguments("primitive", """
            {"list":[42]}""", new TestValues.Li.ListOfPrimitive(List.of(42))),
        new JargoRoundtripTestArguments("values", """
            {"list":[42]}""", new TestValues.Li.ListOfValue(List.of(new TestValues.Value.IntValue(42)))),
        new JargoRoundtripTestArguments("record", """
            {"list":[{"_byte":42,"_char":"f","_short":42,"_int":42,"_long":42,"_double":42.42,"_boolean":true,"string":"text"}]}""",
            new TestValues.Li.ListOfRecord(List.of(
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
