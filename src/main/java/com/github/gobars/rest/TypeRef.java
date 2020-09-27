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

    Type[] actualTypeArguments = ((ParameterizedType) t).getActualTypeArguments();
    Assert.isTrue(actualTypeArguments.length == 1, "Number of type arguments must be 1");
    this.type = actualTypeArguments[0];
  }

  @Override
  public boolean equals(@Nullable Object other) {
    return (this == other
        || (other instanceof TypeRef && this.type.equals(((TypeRef<?>) other).type)));
  }

  @Override
  public int hashCode() {
    return this.type.hashCode();
  }

  @Override
  public String toString() {
    return "TypeRef<" + this.type + ">";
  }

  private static Class<?> findSubclass(Class<?> child) {
    Class<?> parent = child.getSuperclass();
    if (Object.class == parent) {
      throw new IllegalStateException("Expected PTypeRef superclass");
    }

    if (TypeRef.class == parent) {
      return child;
    }

    return findSubclass(parent);
  }
}
