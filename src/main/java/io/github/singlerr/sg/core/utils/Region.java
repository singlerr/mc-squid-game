package io.github.singlerr.sg.core.utils;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.bukkit.Location;

@Data
@AllArgsConstructor
public class Region {

  private Location start;
  private Location end;

}
