package io.github.singlerr.sg.core.utils;

import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public final class EntitySerializable {

  private String world;
  private UUID id;

}
