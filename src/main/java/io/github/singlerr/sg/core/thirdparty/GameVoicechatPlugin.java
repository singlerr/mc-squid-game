package io.github.singlerr.sg.core.thirdparty;

import de.maxhenkel.voicechat.api.VoicechatApi;
import de.maxhenkel.voicechat.api.VoicechatPlugin;
import de.maxhenkel.voicechat.api.events.EventRegistration;
import de.maxhenkel.voicechat.api.events.PlayerStateChangedEvent;
import io.github.singlerr.sg.core.GameCore;
import io.github.singlerr.sg.core.GameLifecycle;
import io.github.singlerr.sg.core.context.GameContext;
import io.github.singlerr.sg.core.context.GamePlayer;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public final class GameVoicechatPlugin implements VoicechatPlugin {

  private final GameCore instance;

  @Override
  public String getPluginId() {
    return "squid_game";
  }

  @Override
  public void initialize(VoicechatApi api) {

  }

  @Override
  public void registerEvents(EventRegistration registration) {
    registration.registerEvent(PlayerStateChangedEvent.class, this::syncPlayerName);
  }

  private void syncPlayerName(PlayerStateChangedEvent event) {
    if (instance.getCoreLifecycle() == null) {
      return;
    }
    GameLifecycle.GameInfo currentGame = instance.getCoreLifecycle().getCurrentGame();
    if (currentGame == null) {
      return;
    }
    if (currentGame.context() == null) {
      return;
    }

    GameContext context = currentGame.context();
    GamePlayer player = context.getPlayer(event.getPlayerUuid());
    if (player == null) {
      return;
    }
    context.syncVoicechatName(player);
  }
}
