package io.github.singlerr.sg.core.utils.serializers;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import java.lang.reflect.Type;
import org.joml.Quaternionf;

public final class QuaternionfTypeAdapter
    implements JsonSerializer<Quaternionf>, JsonDeserializer<Quaternionf> {
  @Override
  public Quaternionf deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
      throws JsonParseException {
    JsonObject o = json.getAsJsonObject();
    return new Quaternionf(o.get("x").getAsFloat(), o.get("y").getAsFloat(),
        o.get("z").getAsFloat(), o.get("w").getAsFloat());
  }

  @Override
  public JsonElement serialize(Quaternionf src, Type typeOfSrc, JsonSerializationContext context) {
    JsonObject o = new JsonObject();
    o.addProperty("x", src.x);
    o.addProperty("y", src.y);
    o.addProperty("z", src.z);
    o.addProperty("w", src.w);
    return o;
  }
}
