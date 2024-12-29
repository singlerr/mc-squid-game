package io.github.singlerr.sg.core.utils;

import io.github.singlerr.sg.core.context.GamePlayer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.ListIterator;
import java.util.concurrent.TimeUnit;
import lombok.Data;
import lombok.Setter;
import lombok.experimental.Accessors;

public final class TickableSoundPlayer {

  private final List<SoundQueue> queues;

  public TickableSoundPlayer() {
    this.queues = Collections.synchronizedList(new ArrayList<>());
  }

  public void tick() {
    ListIterator<SoundQueue> it = queues.listIterator();
    long currentTime = System.currentTimeMillis();
    while (it.hasNext()) {
      SoundQueue sound = it.next();
      if (sound.shouldPlaySound()) {
        for (GamePlayer player : sound.getPlayers()) {
          player.getPlayer().playSound(player.getPlayer(), sound.getSound(), 1.0f, 1.0f);
        }
        sound.shouldPlaySound(false);
      }
      long diff = currentTime - sound.getStart();
      if (diff >= sound.getDelay()) {
        it.remove();
        sound.getCallback().run();
      }
    }
  }

  public void enqueue(Collection<GamePlayer> player, String sound, float delay, Runnable callback) {
    queues.add(SoundQueue.create(player, sound, delay, callback));
  }

  public void enqueue(Collection<GamePlayer> player, String sound, int delay, Runnable callback) {
    queues.add(SoundQueue.create(player, sound, delay, callback));
  }

  @Data
  private static class SoundQueue {

    private final Collection<GamePlayer> players;
    private final String sound;
    private final long start;
    private final long delay;
    private final Runnable callback;

    @Accessors(fluent = true)
    @Setter
    private boolean shouldPlaySound;

    public SoundQueue(Collection<GamePlayer> player, String sound, long start, long delay,
                      Runnable callback) {
      this.players = player;
      this.sound = sound;
      this.start = start;
      this.delay = delay;
      this.callback = callback;
      this.shouldPlaySound = true;
    }

    public static SoundQueue create(Collection<GamePlayer> player, String sound, int delay,
                                    Runnable callback) {
      return new SoundQueue(player, sound, System.currentTimeMillis(),
          TimeUnit.MILLISECONDS.convert(delay, TimeUnit.SECONDS), callback);
    }

    public static SoundQueue create(Collection<GamePlayer> player, String sound, float delay,
                                    Runnable callback) {
      return new SoundQueue(player, sound, System.currentTimeMillis(), (long) (delay * 1000f),
          callback);
    }
  }
}
