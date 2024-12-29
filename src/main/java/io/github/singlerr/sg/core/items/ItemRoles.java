package io.github.singlerr.sg.core.items;

import io.github.singlerr.sg.core.context.GameContext;
import io.github.singlerr.sg.core.context.GamePlayer;
import java.util.function.BiConsumer;
import lombok.Getter;

public enum ItemRoles {
  KILLER(GameContext::kickPlayer);

  @Getter
  private final BiConsumer<GameContext, GamePlayer> executor;


  ItemRoles(BiConsumer<GameContext, GamePlayer> executor) {
    this.executor = executor;
  }
}
