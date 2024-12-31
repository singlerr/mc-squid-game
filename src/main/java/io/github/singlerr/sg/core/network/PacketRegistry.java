package io.github.singlerr.sg.core.network;

import java.util.Objects;
import java.util.function.Supplier;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.jetbrains.annotations.NotNull;

@Data
@AllArgsConstructor
public class PacketRegistry<T extends Packet> implements Comparable<PacketRegistry<T>> {

  private final PacketType<T> packetType;
  private final PacketHandler<T> handler;

  public static <T extends Packet> PacketRegistry<T> createRegistry(Class<T> cls,
                                                                    Supplier<T> factory,
                                                                    PacketHandler<T> handler) {
    return new PacketRegistry<>(PacketType.create(cls, factory), handler);
  }

  @Override
  public boolean equals(Object o) {
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    PacketRegistry<?> that = (PacketRegistry<?>) o;
    return Objects.equals(packetType, that.packetType);
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(packetType);
  }

  @Override
  public int compareTo(@NotNull PacketRegistry<T> o) {
    return equals(o) ? 0 : 1;
  }
}