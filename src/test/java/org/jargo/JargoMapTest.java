package org.jargo;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class JargoMapTest {

  public static List<JargoRoundtripTestArguments> get() {
    return List.of(
        new JargoRoundtripTestArguments("primitive int", """
            {"1":1}""", new TestValues.MapValue.StringIntMapValue(Map.of("1", 1))),
        new JargoRoundtripTestArguments("enum as key", """
            {"RED":"red","GREEN":"green","BLUE":"blue"}""",
            new TestValues.MapValue.EnumMapValue(createTestColorsMap()))
    );
  }

  private static LinkedHashMap<TestValues.En.Colors, String> createTestColorsMap() {
    LinkedHashMap<TestValues.En.Colors, String> map = new LinkedHashMap<>();
    map.put(TestValues.En.Colors.RED, "red");
    map.put(TestValues.En.Colors.GREEN, "green");
    map.put(TestValues.En.Colors.BLUE, "blue");
    return map;
  }

}
