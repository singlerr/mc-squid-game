package io.github.singlerr.sg.core.utils;

import java.util.function.Consumer;

public final class Interpolator {

  private final long duration;
  private final Consumer<Float> callback;
  private long start;
  private boolean started;

  public Interpolator(long duration, Consumer<Float> callback) {
    this.started = false;
    this.duration = duration;
    this.callback = callback;
  }

  public void tick() {
    if (!started) {
      start = System.currentTimeMillis();
      started = true;
    }
    if (end()) {
      return;
    }

    long diff = System.currentTimeMillis() - start;
    float progress = (float) diff / (float) duration;
    callback.accept(progress);
  }

  public boolean end() {
    return System.currentTimeMillis() > start + duration;
  }
}
