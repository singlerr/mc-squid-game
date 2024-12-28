package io.github.singlerr.sg.core.utils;

import java.lang.reflect.InvocationTargetException;
import lombok.experimental.UtilityClass;

@UtilityClass
public class ReflectionUtils {

  public <T> T expectEmptyConstructor(Class<T> type) {
    try {
      return type.getConstructor().newInstance();
    } catch (NoSuchMethodException | InstantiationException | IllegalAccessException |
             InvocationTargetException e) {
      return null;
    }
  }
}
