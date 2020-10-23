package com.github.gobars.rest;

import lombok.Getter;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

public abstract class TypeRef<T> {
  @Getter private final Type type;

  protected TypeRef() {
    Type t = findSubclass(getClass()).getGenericSuperclass();
    Assert.isInstanceOf(ParameterizedType.class, t, "Type must be a parameterized type");

    Type[] args = ((ParameterizedType) t).getActualTypeArguments();
    Assert.isTrue(args.length == 1, "Number of type arguments must be 1");
    type = args[0];
  }

  @Override
  public boolean equals(@Nullable Object o) {
    return (this == o || (o instanceof TypeRef && type.equals(((TypeRef<?>) o).type)));
  }

  @Override
  public int hashCode() {
    return type.hashCode();
  }

  @Override
  public String toString() {
    return "TypeRef<" + type + ">";
  }

  private static Class<?> findSubclass(Class<?> child) {
    Class<?> parent = child.getSuperclass();
    if (Object.class == parent) {
      throw new IllegalStateException("Expected PTypeRef superclass");
    }

    return TypeRef.class == parent ? child : findSubclass(parent);
  }
}
