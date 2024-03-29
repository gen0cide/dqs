package org.darkquest.gs.phandler.client;

import org.apache.mina.common.IoSession;
import org.darkquest.gs.builders.ls.GameSettingUpdatePacketBuilder;
import org.darkquest.gs.connection.LSPacket;
import org.darkquest.gs.connection.Packet;
import org.darkquest.gs.model.Player;
import org.darkquest.gs.model.World;
import org.darkquest.gs.phandler.PacketHandler;


public class GameSettingHandler implements PacketHandler {
  /**
   * World instance
   */
  public static final World world = World.getWorld();

  private GameSettingUpdatePacketBuilder builder = new GameSettingUpdatePacketBuilder();

  public void handlePacket(Packet p, IoSession session) throws Exception {
    Player player = (Player) session.getAttachment();
    int idx = (int) p.readByte();
    if(idx < 0 || idx > 6) {
      player.setSuspiciousPlayer(true);
      return;
    }
    boolean on = p.readByte() == 1;
    player.setGameSetting(idx, on);

    builder.setPlayer(player);
    builder.setIndex(idx);
    builder.setOn(on);

    LSPacket packet = builder.getPacket();
    if(packet != null) {
      World.getWorld().getServer().getLoginConnector().getSession().write(packet);
    }
  }

}
