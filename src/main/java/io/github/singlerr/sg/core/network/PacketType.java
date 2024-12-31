package io.github.singlerr.sg.core.network;

import java.util.Objects;
import java.util.function.Supplier;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.jetbrains.annotations.NotNull;

@Data
@AllArgsConstructor
public final class PacketType<T extends Packet> implements Comparable<PacketType<T>> {
  private final Class<T> packetType;
  private final Supplier<T> factory;

  public static <T extends Packet> PacketType<T> create(Class<T> cls, Supplier<T> factory) {
    return new PacketType<>(cls, factory);
  }

  @Override
  public boolean equals(Object o) {
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    PacketType<?> that = (PacketType<?>) o;
    return Objects.equals(packetType, that.packetType);
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(packetType);
  }

  @Override
  public int compareTo(@NotNull PacketType<T> o) {
    return equals(o) ? 0 : 1;
  }
}