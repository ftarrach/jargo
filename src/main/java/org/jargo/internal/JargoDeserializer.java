package org.jargo.internal;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

public class JargoDeserializer {

  private final JsonFactory jsonFactory;

  public JargoDeserializer(JsonFactory jsonFactory) {
    this.jsonFactory = jsonFactory;
  }

  public <T> T deserialize(InputStream input, Class<?> target) throws IOException {
    JsonParser parser = jsonFactory.createParser(input);
    parser.nextToken();
    DeserializationContext context = new DeserializationContext(parser);
    T deserializedValue = context.doDeserialize("(root)", new Target<>(target, List.of()));
    return deserializedValue;
  }

}
