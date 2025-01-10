package io.github.singlerr.sg.rlgl.game;

import io.github.singlerr.sg.core.setup.GameSetupContext;
import io.github.singlerr.sg.core.utils.EntitySerializable;
import lombok.extern.slf4j.Slf4j;
import org.bukkit.entity.Display;
import org.bukkit.entity.Interaction;
import org.joml.Quaternionf;

@Slf4j
public class RLGLGameSetupContext extends GameSetupContext<RLGLGameSettings> {

  public RLGLGameSetupContext(RLGLGameSettings settings) {
    super(settings);
  }

  public void setYoungHee(Interaction armorStand) {
    getSettings().setYoungHee(EntitySerializable.of(armorStand));
    Display display =
        armorStand.getPassengers().stream().filter(e -> e instanceof Display).map(e -> (Display) e)
            .findAny().orElse(null);
    if (display != null) {
      Quaternionf rot = display.getTransformation().getLeftRotation();
      getSettings().setOriginalRot(rot);
    }
  }
}
