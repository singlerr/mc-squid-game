package io.github.singlerr.sg.core.registry.impl;

import io.github.singlerr.sg.core.registry.Registry;

public interface RegistryFactory {

  static RegistryFactory defaultFactory() {
    return new RegistryFactory() {
      @Override
      public <T> Registry<T> create(String id) {
        return new MappedRegistry<T>() {
          @Override
          public String getId() {
            return id;
          }
        };
      }
    };
  }

  <T> Registry<T> create(String id);
}
