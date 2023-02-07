package org.jargo;

import org.junit.jupiter.api.DynamicNode;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Named;
import org.junit.jupiter.api.TestFactory;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class JargoTest {

  @TestFactory
  public Collection<DynamicNode> roundtripTests() {
    List<DynamicNode> nodes = new ArrayList<>();
    nodes.addAll(createTests("primitive", JargoPrimitiveTest.get()));
    nodes.addAll(createTests("datetime", JargoDateTest.get()));
    nodes.addAll(createTests("enum", JargoEnumTest.get()));
    nodes.addAll(createTests("value", JargoValueTest.get()));
    nodes.addAll(createTests("wrapped value", JargoWrappedValueTest.get()));
    nodes.addAll(createTests("record", JargoRecordTest.get()));
    nodes.addAll(createTests("record of values", JargoRecordOfValuesTest.get()));
    nodes.addAll(createTests("list of records", JargoListRecordTest.get()));
    nodes.addAll(createTests("list of primitives", JargoListPrimitiveTest.get()));
    nodes.addAll(createTests("object with list", JargoListTest.get()));
    nodes.addAll(createTests("object with set", JargoSetTest.get()));
    nodes.addAll(createTests("map of primitives", JargoRecordWrappedValueTest.get()));
    nodes.addAll(createTests("map", JargoMapTest.get()));
    nodes.addAll(createTests("optional", JargoOptionalTest.get()));
    return nodes;
  }

  private List<DynamicTest> createTests(String ctx, List<JargoRoundtripTestArguments> arguments) {
    return DynamicTest.stream(getTests(ctx, arguments), this::executeTests).toList();
  }

  private Stream<Named<JargoRoundtripTestArguments>> getTests(String ctx, List<JargoRoundtripTestArguments> arguments) {
    return arguments.stream().map(args -> Named.of(ctx + ": " + args.name(), args));
  }

  public void executeTests(JargoRoundtripTestArguments args) throws IOException {
    Object java = args.java();
    String json = args.json();
    ByteArrayOutputStream bos = new ByteArrayOutputStream();
    Jargo.serialize(java, bos);
    String serialized = bos.toString(StandardCharsets.UTF_8);
    assertEquals(json, serialized, "serialize java object check");
    Class<?> aClass = java.getClass();
    ByteArrayInputStream bis = new ByteArrayInputStream(serialized.getBytes(StandardCharsets.UTF_8));
    if (!List.class.isAssignableFrom(aClass) && !Map.class.isAssignableFrom(aClass)) {
      assertEquals(java, Jargo.deserialize(bis, aClass), "serialize serialized java object");
    }
  }

}
