package org.jargo;

import java.util.List;

public record Target<T>(Class<T> clazz, List<Class<?>> generics) {}
