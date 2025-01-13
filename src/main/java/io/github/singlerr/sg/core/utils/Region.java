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
    return contains(location.x(), location.y(), location.z());
  }

  public boolean isInStrict(Location location) {
    return contains(location.getBlockX(), location.getBlockY(), location.getBlockZ());
  }

  public boolean contains(int x, int y, int z) {
    if (start == null || end == null) {
      return false;
    }
    double x1 = Math.min(start.getBlockX(), end.getBlockX());
    double x2 = Math.max(start.getBlockX(), end.getBlockX());
    double y1 = Math.min(start.getBlockY(), end.getBlockY());
    double y2 = Math.max(start.getBlockY(), end.getBlockY());
    double z1 = Math.min(start.getBlockZ(), end.getBlockZ());
    double z2 = Math.max(start.getBlockZ(), end.getBlockZ());
    return x >= x1 && x <= x2 && y >= y1 && y <= y2 && z >= z1 && z <= z2;
  }

  public boolean contains(double x, double y, double z) {
    if (start == null || end == null) {
      return false;
    }
    double x1 = Math.min(start.x(), end.x());
    double x2 = Math.max(start.x(), end.x());
    double y1 = Math.min(start.y(), end.y());
    double y2 = Math.max(start.y(), end.y());
    double z1 = Math.min(start.z(), end.z());
    double z2 = Math.max(start.z(), end.z());
    return x >= x1 && x <= x2 && y >= y1 && y <= y2 && z >= z1 && z <= z2;
  }

}
