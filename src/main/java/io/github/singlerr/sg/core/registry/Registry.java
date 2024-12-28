package io.github.singlerr.sg.core.registry;

import java.util.Collection;

public interface Registry<T> {

  String getId();

  void register(String id, T reg);

  T getById(String id);

  Collection<T> values();

  Collection<String> keys();
}
