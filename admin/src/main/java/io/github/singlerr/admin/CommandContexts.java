package io.github.singlerr.admin;

import io.github.singlerr.sg.core.utils.Transform;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import lombok.experimental.UtilityClass;

@UtilityClass
public class CommandContexts {

  private final Map<UUID, Context> contexts = Collections.synchronizedMap(new HashMap<>());

  public Context getContext(UUID id) {
    return contexts.get(id);
  }

  public void begin(UUID id, Context context) {
    contexts.put(id, context);
  }

  public void end(UUID id) {
    contexts.remove(id);
  }

  public record Context(String modelLocation, Transform transform) {
  }
}
