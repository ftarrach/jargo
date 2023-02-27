package org.jargo;

import com.fasterxml.jackson.core.JsonFactory;
import org.jargo.internal.JargoDeserializer;
import org.jargo.internal.JargoSerializer;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class Jargo {

  private static final JsonFactory jsonFactory = JsonFactory.builder().build();
  private static final JargoDeserializer deserializer = new JargoDeserializer(jsonFactory);
  private static final JargoSerializer serializer = new JargoSerializer(jsonFactory);

  private Jargo () {}

  public static <T> void registerCustomSerializer(Class<T> target, Serializer<T> serializer) {
    Jargo.serializer.register(target, serializer);
  }

  public static <T> void registerCustomDeserializer(Class<T> target, Deserializer<T> deserializer) {
    Jargo.deserializer.register(target, deserializer);
  }

  public static <T> T deserialize(InputStream input, Class<T> target) throws IOException {
    return deserializer.deserialize(input, target);
  }

  public static void serialize(Object object, OutputStream outputStream) throws IOException {
    serializer.serialize(object, outputStream);
  }

}
