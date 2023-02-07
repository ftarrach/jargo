package org.jargo;

import java.util.List;

public class JargoListRecordTest {

  public static List<JargoRoundtripTestArguments> get() {
    return List.of(
        new JargoRoundtripTestArguments("values", """
            [{"_byte":42,"_char":"f","_short":42,"_int":42,"_long":42,"_double":42.42,"_boolean":true,"string":"text"}]""",
            List.of(
                new TestValues.Rec.PrimitiveRecord(
                    (byte) 42,
                    'f',
                    (short) 42,
                    42,
                    42,
                    42.42,
                    true,
                    "text"
                )))
    );
  }

}
