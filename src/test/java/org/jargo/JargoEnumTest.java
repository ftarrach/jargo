package org.jargo;

import java.util.List;

public class JargoEnumTest {

  public static List<JargoRoundtripTestArguments> get() {
    return List.of(
        new JargoRoundtripTestArguments("red", "\"RED\"", TestValues.En.Colors.RED),
        new JargoRoundtripTestArguments("green", "\"GREEN\"", TestValues.En.Colors.GREEN),
        new JargoRoundtripTestArguments("blue", "\"BLUE\"", TestValues.En.Colors.BLUE)
    );
  }

}
