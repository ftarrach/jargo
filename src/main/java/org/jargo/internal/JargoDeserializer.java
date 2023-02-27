package org.jargo.internal;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import org.jargo.Deserializer;
import org.jargo.Target;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class JargoDeserializer {

  private final JsonFactory jsonFactory;
  private final Map<Class<?>, Deserializer<?>> customDeserializer = new HashMap<>();

  public JargoDeserializer(JsonFactory jsonFactory) {
    this.jsonFactory = jsonFactory;
  }

  public <T> void register(Class<T> target, Deserializer<T> deserializer) {
    customDeserializer.put(target, deserializer);
  }

  public <T> T deserialize(InputStream input, Class<?> target) throws IOException {
    JsonParser parser = jsonFactory.createParser(input);
    parser.nextToken();
    DeserializationContext context = new DeserializationContext(parser, customDeserializer);
    T deserializedValue = context.doDeserialize("(root)", new Target<>(target, List.of()));
    return deserializedValue;
  }
}
