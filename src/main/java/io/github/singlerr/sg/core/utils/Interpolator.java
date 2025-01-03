package io.github.singlerr.sg.core.utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.ListIterator;
import java.util.function.Consumer;
import lombok.Data;
import lombok.RequiredArgsConstructor;

public final class Interpolator {

  private final List<Task> activeTasks;

  public Interpolator() {
    this.activeTasks = Collections.synchronizedList(new ArrayList<>());
  }

  public void tick(long time) {
    ListIterator<Task> it = activeTasks.listIterator();
    while (it.hasNext()) {
      Task t = it.next();
      if (t.end()) {
        it.remove();
        continue;
      }

      t.tick(time);
    }
  }

  public void add(long duration, Consumer<Float> callback) {
    activeTasks.add(new Task(duration, callback));
  }

  @Data
  @RequiredArgsConstructor
  private static class Task {

    private final long duration;
    private final Consumer<Float> callback;
    private long start;
    private boolean started = false;
    private boolean end = false;


    public void tick(long time) {
      if (end) {
        return;
      }
      if (!started) {
        start = time;
        started = true;
      }

      long diff = time - start;
      if (diff >= duration) {
        end = true;
        return;
      }
      float p = (float) diff / (float) duration;
      callback.accept(p);
    }

    public boolean end() {
      return end;
    }
  }
}
