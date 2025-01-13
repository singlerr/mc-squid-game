package io.github.singlerr.mgr.game;

import io.github.singlerr.sg.core.context.GamePlayer;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import net.minecraft.util.Mth;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.joml.Vector3f;

@Slf4j
@Data

public final class Mount {

  private GamePlayer player;
  private Vector3f center;
  private float rotationSpeed;
  private Vector3f currentPos;

  public Mount(GamePlayer player, Vector3f center, float rotationSpeed) {
    this.player = player;
    this.center = center;
    this.rotationSpeed = rotationSpeed;
    this.currentPos =
        player.getPlayer().getLocation().toVector().toVector3f().sub(center.x, 0, center.z);
  }


  public void tick() {
    if (!player.available()) {
      return;
    }

    Player p = player.getPlayer();
    currentPos.rotateY(Mth.TWO_PI / (20 * 20));

    Vector3f pos = new Vector3f(currentPos);
    pos.add(center.x, 0, center.z);

    p.teleport(new Location(p.getWorld(), pos.x, pos.y, pos.z, p.getYaw(), p.getPitch()));
  }

  public void remove() {

  }

}
