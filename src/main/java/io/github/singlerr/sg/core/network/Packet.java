package io.github.singlerr.sg.core.network;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import org.bukkit.entity.Player;

public interface Packet {

  void writePayload(ByteArrayDataOutput buffer);

  void readPayload(ByteArrayDataInput buffer);

  void handle(Player player);

}
