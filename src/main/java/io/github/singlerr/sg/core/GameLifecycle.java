package io.github.singlerr.sg.core;

import io.github.singlerr.sg.core.context.GameContext;
import io.github.singlerr.sg.core.context.GameEventBus;
import io.github.singlerr.sg.core.context.GameHistoryRecorder;
import io.github.singlerr.sg.core.context.GameStatus;
import io.github.singlerr.sg.core.context.impl.DefaultGameEventBus;
import io.github.singlerr.sg.core.events.GameEventListener;
import io.github.singlerr.sg.core.registry.Registry;
import io.github.singlerr.sg.core.registry.impl.RegistryFactory;
import io.github.singlerr.sg.core.setup.GameSettings;
import java.util.ArrayDeque;
import java.util.Collection;
import java.util.Deque;
import java.util.HashMap;
import java.util.function.Consumer;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

@Slf4j
public final class GameLifecycle extends BukkitRunnable {
  private final GameRegistry games;
  private final GameSettingsRegistry gameSettings;
  private final GameCore plugin;
  private final Deque<Task> queuedTasks;

  @Getter
  private GameInfo currentGame;
  private GameContext prevGameContext;


  public GameLifecycle(GameRegistry registry, GameSettingsRegistry settingsRegistry,
                       GameCore corePlugin) {
    this.games = registry;
    this.gameSettings = settingsRegistry;
    this.queuedTasks = new ArrayDeque<>();
    this.plugin = corePlugin;
  }

  @Override
  public void run() {
    if (!queuedTasks.isEmpty()) {
      Task task = queuedTasks.removeFirst();
      task.runnable().run();
      task.callback().run();
    }

    if (currentGame != null) {
      currentGame.eventBus().postGameTick(currentGame.context());
    }
  }

  private void endGame() {
    if (currentGame != null) {
      currentGame.context().setStatus(GameStatus.END);
      unregisterEvents(currentGame.minecraftListeners().values());
      currentGame.eventBus().postGameEnd(currentGame.context());
      prevGameContext = currentGame.context();
      currentGame = null;
    }
  }

  public boolean endGame(Runnable callback) {
    if (currentGame == null) {
      return false;
    }

    return queuedTasks.add(new Task(this::endGame, callback));
  }

  private void startGame(String id, Game game) {
    if (game != null) {

      if (prevGameContext != null) {
        GameHistoryRecorder.recordGame(prevGameContext.getId(), prevGameContext);
      }

      game.initialize();
      Registry<GameEventListener> gameListeners =
          initRegistry("game_events", game::registerGameListener);
      Registry<Listener> minecraftListeners =
          initRegistry("minecraft_events", game::registerListener);
      registerEvents(minecraftListeners.values(), plugin);
      log.info("Initializing game {}, {}", gameListeners.keys(), minecraftListeners.keys());
      GameEventBus eventBus = createEventBus(gameListeners);
      GameSettings settings = game.getGameSetup().getSettings(null);
      GameContext context =
          game.createContext(prevGameContext, eventBus, GameStatus.START, settings);
      if (context == null) {
        context = new GameContext(new HashMap<>(), GameStatus.START, eventBus,
            settings);
      }
      context.setId(id);
      currentGame = new GameInfo(id, game, context, eventBus, minecraftListeners);
      eventBus.postGameStart(context);

      GameHistoryRecorder.recordGame(id, context);
    }
  }

  private <T> Registry<T> initRegistry(String id, Consumer<Registry<T>> registrar) {
    Registry<T> registry = RegistryFactory.defaultFactory().create(id);
    registrar.accept(registry);

    return registry;
  }

  public boolean startGame(String id, Runnable callback) {
    Game game = this.games.getById(id);
    if (game == null) {
      return false;
    }
    return queuedTasks.add(new Task(() -> startGame(id, game), callback));
  }

  private GameEventBus createEventBus(Registry<GameEventListener> registry) {
    return new DefaultGameEventBus(registry);
  }

  private void registerEvents(Collection<Listener> listeners, Plugin plugin) {
    for (Listener listener : listeners) {
      plugin.getServer().getPluginManager().registerEvents(listener, plugin);
    }
  }

  private void unregisterEvents(Collection<Listener> listeners) {
    for (Listener listener : listeners) {
      HandlerList.unregisterAll(listener);
    }
  }

  public record GameInfo(String id, Game instance, GameContext context, GameEventBus eventBus,
                         Registry<Listener> minecraftListeners) {
  }

  private record Task(Runnable runnable, Runnable callback) {
  }
}
