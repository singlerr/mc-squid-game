package io.github.singlerr.sg.core.utils;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public final class Animation {

  private final int nodeIndex;
  private final Transform from;
  private final Transform to;
  private final long duration;
}
