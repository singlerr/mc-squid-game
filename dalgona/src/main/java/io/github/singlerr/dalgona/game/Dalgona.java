package io.github.singlerr.dalgona.game;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public final class Dalgona {

  private String imagePath;
  private int threshold;

}
