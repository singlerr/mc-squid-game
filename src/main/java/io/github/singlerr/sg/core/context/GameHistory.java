package io.github.singlerr.sg.core.context;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public final class GameHistory {

  private final String id;
  private int playerCount;
  private int survivorsCount;

  public GameHistory(String id) {
    this(id, 0, 0);
  }

  public GameHistory(String id, int playerCount) {
    this(id, playerCount, playerCount);
  }

  public void recordPlayerQuit() {
    if (survivorsCount == 0) {
      return;
    }

    survivorsCount--;
  }


}
