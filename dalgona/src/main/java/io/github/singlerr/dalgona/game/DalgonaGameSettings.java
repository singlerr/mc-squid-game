package io.github.singlerr.dalgona.game;

import io.github.singlerr.sg.core.setup.GameSettings;
import java.util.HashMap;
import java.util.Map;
import lombok.Data;
import org.bukkit.Material;

@Data
public final class DalgonaGameSettings implements GameSettings {

  private Material dalgonaType = Material.YELLOW_CARPET;

  private Map<String, String> dalgonaImages = new HashMap<>();

  private float time = 3 * 60;

  public void copy(DalgonaGameSettings o) {
    this.dalgonaType = o.dalgonaType;
  }
}
