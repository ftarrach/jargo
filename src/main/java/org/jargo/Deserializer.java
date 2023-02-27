package org.jargo;

import com.fasterxml.jackson.core.JsonParser;

import java.io.IOException;

public interface Deserializer<T> {

  T deserialize(Target<?> target, JsonParser parser) throws IOException;

}
