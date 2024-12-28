package io.github.singlerr.sg.core.registry.impl;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import io.github.singlerr.sg.core.registry.Registry;
import java.util.Collection;

abstract class MappedRegistry<T> implements Registry<T> {

  private final BiMap<String, T> idMap;

  public MappedRegistry() {
    this.idMap = HashBiMap.create();
  }

  @Override
  public void register(String id, T reg) {
    if (this.idMap.containsKey(id)) {
      throw new IllegalStateException("Already instance mapped with that key!");
    }

    this.idMap.put(id, reg);
  }

  @Override
  public T getById(String id) {
    return this.idMap.get(id);
  }

  @Override
  public Collection<String> keys() {
    return this.idMap.keySet();
  }

  public Collection<T> values() {
    return this.idMap.values();
  }
}
