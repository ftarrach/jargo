package org.jargo;

import java.util.List;

public class JargoRecordTest {

  public static List<JargoRoundtripTestArguments> get() {
    return List.of(
        new JargoRoundtripTestArguments("primitive record", """
            {"_char":"f","_int":42,"_long":42,"_double":42.42,"_boolean":true,"string":"text"}""", new TestValues.Rec.PrimitiveRecord(
            'f',
            42,
            42,
            42.42,
            true,
            "text"
        ))
    );
  }

}
