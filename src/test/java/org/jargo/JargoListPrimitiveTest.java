package org.jargo;

import java.util.List;

public class JargoListPrimitiveTest {

  public static List<JargoRoundtripTestArguments> get() {
    return List.of(
        new JargoRoundtripTestArguments("bytes", "[42]", List.of(0b101010)),
        new JargoRoundtripTestArguments("chars", "[\"f\"]", List.of('f')),
        new JargoRoundtripTestArguments("shorts", "[42]", List.of((short) 42)),
        new JargoRoundtripTestArguments("ints", "[42]", List.of(42)),
        new JargoRoundtripTestArguments("longs", "[42]", List.of(42L)),
        new JargoRoundtripTestArguments("doubles", "[42.42]", List.of(42.42)),
        new JargoRoundtripTestArguments("booleans", "[true]", List.of(true)),
        new JargoRoundtripTestArguments("strings", "[\"text\"]", List.of("text"))
    );
  }

}
