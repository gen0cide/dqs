package org.darkquest.gs.builders.ls;

import org.darkquest.gs.builders.LSPacketBuilder;
import org.darkquest.gs.connection.LSPacket;
import org.darkquest.gs.model.Player;
import org.darkquest.gs.model.World;


public class ReportInfoRequestPacketBuilder {
  /**
   * World instance
   */
  public static final World world = World.getWorld();
  /**
   * The player to provide information on
   */
  private Player player;
  /**
   * Packets uID
   */
  private long uID;

  public LSPacket getPacket() {
    LSPacketBuilder packet = new LSPacketBuilder();
    packet.setUID(uID);
    packet.addShort(player.getX());
    packet.addShort(player.getY());
    packet.addBytes(player.getStatus().toString().getBytes());
    return packet.toPacket();
  }

  /**
   * Sets the player to provide information on
   */
  public void setPlayer(Player player) {
    this.player = player;
  }

  /**
   * Sets the packet to reply to
   */
  public void setUID(long uID) {
    this.uID = uID;
  }
}
