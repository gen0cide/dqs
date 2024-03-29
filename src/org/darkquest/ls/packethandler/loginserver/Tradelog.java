package org.darkquest.ls.packethandler.loginserver;

import org.apache.mina.common.IoSession;
import org.darkquest.ls.Server;
import org.darkquest.ls.net.Packet;
import org.darkquest.ls.packethandler.PacketHandler;


public class Tradelog implements PacketHandler {
  public void handlePacket(Packet p, IoSession session) throws Exception {
    long from = p.readLong();
    long to = p.readLong();
    int item = p.readInt();
    long amount = p.readLong();
    int x = p.readInt();
    int y = p.readInt();
    int type = p.readInt();
    long date = (System.currentTimeMillis() / 1000);
    Server.db.updateQuery("INSERT `rsca2_tradelog` VALUES('" + from + "','" + to + "','" + date + "','" + item + "','" + x + "','" + y + "','" + amount + "','" + type + "')");
  }
}
