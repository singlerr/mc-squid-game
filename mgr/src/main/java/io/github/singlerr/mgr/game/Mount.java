package io.github.singlerr.mgr.game;

import io.github.singlerr.sg.core.context.GamePlayer;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import net.minecraft.util.Mth;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;
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
    pos.add(center.x, 0, center.z).add(0, 1, 0);

    Vector playerPos = p.getLocation().toVector();
    Vector dir = new Vector(pos.x, pos.y, pos.z).subtract(playerPos);
    player.getPlayer().setVelocity(new Vector(dir.getX(), 0, dir.getZ()));
  }

  public void remove() {

  }

}
