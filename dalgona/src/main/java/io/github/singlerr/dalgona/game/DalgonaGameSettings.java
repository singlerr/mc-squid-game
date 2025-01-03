package io.github.singlerr.dalgona.game;

import io.github.singlerr.sg.core.setup.GameSettings;
import java.util.HashMap;
import java.util.Map;
import lombok.Data;
import org.bukkit.Material;

@Data
public final class DalgonaGameSettings implements GameSettings {

  private Material dalgonaType = Material.YELLOW_CARPET;

  private Map<String, String> dalgonaImages;

  private float time = 3 * 60;

  public DalgonaGameSettings() {
    dalgonaImages = new HashMap<>();
    dalgonaImages.put("umbrella", "images/umbrella.png");
    dalgonaImages.put("circle", "images/circle.png");
    dalgonaImages.put("star", "images/star.png");
    dalgonaImages.put("triangle", "images/triangle.png");
  }

  public void copy(DalgonaGameSettings o) {
    this.dalgonaType = o.dalgonaType;
  }
}
