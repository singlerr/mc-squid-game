package io.github.singlerr.sg.core.utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.ListIterator;
import lombok.AllArgsConstructor;
import lombok.Data;

public class TaskScheduler {

  private final List<DelayedTask> tasks;

  public TaskScheduler() {
    this.tasks = Collections.synchronizedList(new ArrayList<>());
  }

  public void tick() {
    ListIterator<DelayedTask> it = tasks.listIterator();
    long current = System.currentTimeMillis();
    while (it.hasNext()) {
      DelayedTask t = it.next();
      if (t.end(current)) {
        it.remove();
        t.getCallback().run();
      }
    }
  }

  public void enqueue(long delay, Runnable callback) {
    tasks.add(new DelayedTask(System.currentTimeMillis(), callback, delay));
  }

  @Data
  @AllArgsConstructor
  private static class DelayedTask {
    private final long duration;
    private final Runnable callback;
    private long start;

    public boolean end(long current) {
      return current - start > duration;
    }
  }
}
