package io.github.singlerr.dalgona.game;

import io.github.singlerr.sg.core.setup.GameSettings;
import java.util.HashMap;
import java.util.Map;
import lombok.Data;
import org.bukkit.Material;

@Data
public final class DalgonaGameSettings implements GameSettings {

  private Material dalgonaType = Material.MANGROVE_LEAVES;

  private Map<String, Dalgona> dalgonaList;
  private float time = 3 * 60;

  public DalgonaGameSettings() {
    dalgonaList = new HashMap<>();
    dalgonaList.put("umbrella", new Dalgona("images/umbrella.png", 380));
    dalgonaList.put("circle", new Dalgona("images/circle.png", 210));
    dalgonaList.put("star", new Dalgona("images/star.png", 260));
    dalgonaList.put("triangle", new Dalgona("images/triangle.png", 195));
  }

  public void copy(DalgonaGameSettings o) {
    this.dalgonaType = o.dalgonaType;
  }
}
