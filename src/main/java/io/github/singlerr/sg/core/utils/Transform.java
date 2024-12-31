package io.github.singlerr.sg.core.utils;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.joml.Quaternionf;
import org.joml.Vector3f;

@Data
@AllArgsConstructor
public class Transform {

  private Vector3f translation;
  private Quaternionf rotation;
  private Vector3f scale;
}
