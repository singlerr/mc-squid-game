package io.github.singlerr.sg.core.context;

import lombok.Getter;

public enum GameRole {
  USER(0),
  ADMIN(2),
  TROY(1);

  @Getter
  private int level;

  GameRole(int level) {
    this.level = level;
  }
}
