package io.github.singlerr.roulette.game;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public final class Gun {

  // true - real bullet, false - fake bullet
  private Deque<Boolean> bullets = new ArrayDeque<>();
  private int bulletAmount = 0;
  private int realBulletAmount = 0;


  public boolean reload(int bulletCount, int realBulletCount) {
    if (realBulletCount > bulletCount) {
      return false;
    }
    List<Boolean> buf = new ArrayList<>();
    buf.addAll(Collections.nCopies(bulletCount - realBulletCount, false));
    buf.addAll(Collections.nCopies(realBulletCount, true));
    Collections.shuffle(buf);
    this.bullets = new ArrayDeque<>(buf);
    this.bulletAmount = bulletCount;
    this.realBulletAmount = realBulletCount;
    return true;
  }

}
