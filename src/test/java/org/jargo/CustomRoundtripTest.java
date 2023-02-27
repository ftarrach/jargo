package org.jargo;

import com.fasterxml.jackson.core.JsonToken;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class CustomRoundtripTest {

  public record ISODate(String thisWillBeNull) {}

  @Test
  public void customRoundtrip() throws IOException {
    ISODate original = new ISODate("1970-01-31");
    Jargo.registerCustomSerializer(ISODate.class, (generator, value) -> {
      generator.writeStartObject();
      String[] split = value.thisWillBeNull().split("-");
      generator.writeFieldName("year");
      generator.writeString(split[0]);
      generator.writeFieldName("month");
      generator.writeString(split[1]);
      generator.writeFieldName("day");
      generator.writeString(split[2]);
      generator.writeEndObject();
    });
    Jargo.registerCustomDeserializer(ISODate.class, (target, parser) -> {
      if (parser.isExpectedStartObjectToken()) {
        String year = null, month = null, day = null;
        while (parser.nextToken() == JsonToken.FIELD_NAME) {
          String fieldName = parser.getValueAsString();
          parser.nextToken();
          switch (fieldName) {
            case "year" -> year = parser.getValueAsString();
            case "month" -> month = parser.getValueAsString();
            case "day" -> day = parser.getValueAsString();
            default -> throw new IOException("");
          }
        }
        parser.nextToken(); // }
        if (year == null || month == null || day == null) throw new IOException("one of the parts of ISODate is missing");
        return new ISODate(year + "-" + month + "-" + day);
      }
      throw new IOException("expected object, got" + parser.currentToken());
    });

    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    Jargo.serialize(original, baos);
    String serializedValue = baos.toString(StandardCharsets.UTF_8);
    assertEquals("""
        {"year":"1970","month":"01","day":"31"}""", serializedValue);
    ISODate deserialized = Jargo.deserialize(new ByteArrayInputStream(serializedValue.getBytes(StandardCharsets.UTF_8)), ISODate.class);
    assertEquals(original, deserialized);
  }

}
