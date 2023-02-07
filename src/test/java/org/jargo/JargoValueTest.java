package org.jargo;

import java.util.List;

public class JargoValueTest {

  public static List<JargoRoundtripTestArguments> get() {
    return List.of(
        new JargoRoundtripTestArguments("byte", "42", new TestValues.Value.IntValue(42)),
        new JargoRoundtripTestArguments("char", "\"f\"", new TestValues.Value.CharValue('f')),
        new JargoRoundtripTestArguments("short", "42", new TestValues.Value.ShortValue((short) 42)),
        new JargoRoundtripTestArguments("int", "42", new TestValues.Value.IntValue(42)),
        new JargoRoundtripTestArguments("long", "42", new TestValues.Value.LongValue(42)),
        new JargoRoundtripTestArguments("double", "42.42", new TestValues.Value.DoubleValue(42.42)),
        new JargoRoundtripTestArguments("boolean", "true", new TestValues.Value.BooleanValue(true)),
        new JargoRoundtripTestArguments("string", "\"text\"", new TestValues.Value.StringValue("text"))
    );
  }

}
