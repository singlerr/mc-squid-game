package io.github.singlerr.roulette.game;

import io.github.singlerr.sg.core.setup.GameSettings;
import io.github.singlerr.sg.core.utils.SoundSet;
import lombok.Data;
import org.bukkit.Material;

@Data
public final class RouletteGameSettings implements GameSettings {

  private Material gunType = Material.CROSSBOW;
  private SoundSet gunReloading = new SoundSet("roulette.reloading", 2f);
  private SoundSet gunShot = new SoundSet("roulette.shot", 0.5f);
  private SoundSet gunAiming = new SoundSet("roulette.aiming", 1f);
  private SoundSet gunEmpty = new SoundSet("roulette.empty", 1f);

  private String gunName = "총";

  private int bulletAmount = 6;
  private int realBulletAmount = 1;

  public void copy(RouletteGameSettings o) {
    gunType = o.gunType;
    gunReloading = o.gunReloading;
    gunShot = o.gunShot;
    gunAiming = o.gunAiming;
    gunName = o.gunName;
    bulletAmount = o.bulletAmount;
    realBulletAmount = o.realBulletAmount;
  }
}
