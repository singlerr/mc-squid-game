package io.github.singlerr.sg.core.utils.serializers;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import java.lang.reflect.Type;
import org.joml.Vector3f;

public final class Vector3fTypeAdapter
    implements JsonSerializer<Vector3f>, JsonDeserializer<Vector3f> {
  @Override
  public Vector3f deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
      throws JsonParseException {
    JsonObject o = json.getAsJsonObject();
    return new Vector3f(o.get("x").getAsFloat(), o.get("y").getAsFloat(), o.get("z").getAsFloat());
  }

  @Override
  public JsonElement serialize(Vector3f src, Type typeOfSrc, JsonSerializationContext context) {
    JsonObject o = new JsonObject();
    o.addProperty("x", src.x);
    o.addProperty("y", src.y);
    o.addProperty("z", src.z);
    return o;
  }
}
