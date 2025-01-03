package io.github.singlerr.trolley.game;

import io.github.singlerr.sg.core.utils.EntitySerializable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bukkit.Location;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Train {
  private Location initialLocation;
  private EntitySerializable entity;
}
