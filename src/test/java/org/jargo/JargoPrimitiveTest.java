package org.jargo;

import java.util.List;

public class JargoPrimitiveTest {

  public static List<JargoRoundtripTestArguments> get() {
    return List.of(
        new JargoRoundtripTestArguments("byte", "42", 0b101010),
        new JargoRoundtripTestArguments("char", "\"f\"", 'f'),
        new JargoRoundtripTestArguments("short", "42", (short) 42),
        new JargoRoundtripTestArguments("int", "42", 42),
        new JargoRoundtripTestArguments("long", "42", 42L),
        new JargoRoundtripTestArguments("double", "42.42", 42.42),
        new JargoRoundtripTestArguments("boolean", "true", true),
        new JargoRoundtripTestArguments("string", "\"text\"", "text")
    );
  }

}
