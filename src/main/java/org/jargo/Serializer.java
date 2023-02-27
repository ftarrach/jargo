package org.jargo;

import com.fasterxml.jackson.core.JsonGenerator;

import java.io.IOException;

public interface Serializer<T> {

  void serialize(JsonGenerator generator, T value) throws IOException;
}
