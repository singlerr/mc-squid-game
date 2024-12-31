package io.github.singlerr.sg.core.utils.serializers;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import java.lang.reflect.Type;
import org.bukkit.Bukkit;
import org.bukkit.Location;

public final class LocationTypeAdapter
    implements JsonSerializer<Location>, JsonDeserializer<Location> {

  @Override
  public Location deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
      throws JsonParseException {
    JsonObject root = json.getAsJsonObject();
    String worldName = root.get("world").getAsString();
    double x = root.get("x").getAsDouble();
    double y = root.get("y").getAsDouble();
    double z = root.get("z").getAsDouble();
    return new Location(Bukkit.getWorld(worldName), x, y, z);
  }

  @Override
  public JsonElement serialize(Location src, Type typeOfSrc, JsonSerializationContext context) {
    JsonObject root = new JsonObject();
    root.addProperty("world", src.getWorld().getName());
    root.addProperty("x", src.getX());
    root.addProperty("y", src.getY());
    root.addProperty("z", src.getZ());
    return root;
  }
}
