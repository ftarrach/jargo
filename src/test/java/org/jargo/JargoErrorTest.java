package org.jargo;

import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class JargoErrorTest {

  @Test
  public void deserializeMissingField() {
    String json = """
        {"_char":"f","_int":42,"_long":42,"_double":42.42,"_boolean":true}""";
    ByteArrayInputStream input = new ByteArrayInputStream(json.getBytes(StandardCharsets.UTF_8));
    IOException e = assertThrows(IOException.class, () -> Jargo.deserialize(input, TestValues.Rec.PrimitiveRecord.class));
    assertTrue(e.getMessage().contains("missing"));
    assertTrue(e.getMessage().contains("string"));
  }

  @Test
  public void deserializeUnknownField() {
    String json = """
        {"_char":"f","_int":42,"_long":42,"_double":42.42,"_boolean":true,"string":"string","cat":1}""";
    ByteArrayInputStream input = new ByteArrayInputStream(json.getBytes(StandardCharsets.UTF_8));
    IOException e = assertThrows(IOException.class, () -> Jargo.deserialize(input, TestValues.Rec.PrimitiveRecord.class));
    assertTrue(e.getMessage().contains("unknown"));
    assertTrue(e.getMessage().contains("cat"));
  }

  @Test
  public void deserializeWrongTypeValueRecord() {
    String json = """
        42""";
    ByteArrayInputStream input = new ByteArrayInputStream(json.getBytes(StandardCharsets.UTF_8));
    IOException e = assertThrows(IOException.class, () -> Jargo.deserialize(input, TestValues.Value.StringValue.class));
    assertTrue(e.getMessage().contains("(root)->{->(value)"));
    assertTrue(e.getMessage().contains("expected string, got int"));
  }

  @Test
  public void deserializeWrongTypeDataRecord() {
    String json = """
        {"first": 42, "second":"bar"}""";
    ByteArrayInputStream input = new ByteArrayInputStream(json.getBytes(StandardCharsets.UTF_8));
    IOException e = assertThrows(IOException.class, () -> Jargo.deserialize(input, TestValues.Rec.SimpleTuple.class));
    assertTrue(e.getMessage().contains("(root)->{->first"), e.getMessage());
    assertTrue(e.getMessage().contains("expected string, got int"), e.getMessage());
  }

}
