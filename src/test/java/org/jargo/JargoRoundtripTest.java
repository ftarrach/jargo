package org.jargo;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvFileSource;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class JargoRoundtripTest {

  @ParameterizedTest(name = "{0}")
  @CsvFileSource(resources = "/org/jargo/roundtrip.tsv", delimiter = '\t', quoteCharacter = '\0')
  public void roundtrip(String name, String target, String json) throws Exception {
    Class<?> targetClass = Class.forName(target);
    Object deserialized = Jargo.deserialize(new ByteArrayInputStream(json.getBytes(StandardCharsets.UTF_8)), targetClass);
    ByteArrayOutputStream bos = new ByteArrayOutputStream();
    Jargo.serialize(deserialized, bos);
    assertEquals(json, bos.toString(StandardCharsets.UTF_8));
  }

}
