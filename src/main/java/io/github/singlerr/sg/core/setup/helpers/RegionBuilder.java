package io.github.singlerr.sg.core.setup.helpers;

import io.github.singlerr.sg.core.utils.Region;
import org.bukkit.Location;

public final class RegionBuilder {

  private Location start;
  private Location end;

  public boolean setStart(Location location) {
    this.start = location;
    return true;
  }

  public boolean setEnd(Location location) {
    if (this.start == null) {
      return false;
    }
    this.end = location;
    return true;
  }

  public void reset() {
    this.start = null;
    this.end = null;
  }

  public Region build() {
    if (this.start == null || this.end == null) {
      return null;
    }
    Region region = new Region(start, end);
    reset();
    return region;
  }
}
