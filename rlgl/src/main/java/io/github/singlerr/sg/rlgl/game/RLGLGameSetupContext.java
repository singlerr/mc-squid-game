package io.github.singlerr.sg.rlgl.game;

import io.github.singlerr.sg.core.setup.GameSetupContext;
import io.github.singlerr.sg.core.utils.SerializationUtils;
import org.bukkit.entity.ArmorStand;

public class RLGLGameSetupContext extends GameSetupContext<RLGLGameSettings> {

  public RLGLGameSetupContext(RLGLGameSettings settings) {
    super(settings);
  }

  public void setYoungHee(ArmorStand armorStand) {
    SerializationUtils.writeModelData(getSettings().getModelLocation(), armorStand);
    getSettings().setYoungHeeId(armorStand.getUniqueId());
    getSettings().setYoungHeeWorld(armorStand.getWorld().getName());
  }
}
