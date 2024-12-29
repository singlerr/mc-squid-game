package io.github.singlerr.sg.core.utils;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.bukkit.Location;

@Data
@AllArgsConstructor
public class Region {

  private Location start;
  private Location end;

  public boolean isIn(Location location) {
    return start.getX() <= location.getX() && start.getY() <= location.getY() &&
        start.getZ() <= location.getZ() && location.getX() <= end.getX() &&
        location.getY() <= end.getY() && location.getZ() <= end.getZ();
  }
}
