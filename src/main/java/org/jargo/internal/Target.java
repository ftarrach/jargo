package org.jargo.internal;

import java.util.List;

record Target<T>(Class<T> clazz, List<Class<?>> generics) {}
