package org.darkquest.gs.phandler.client;

import java.util.ArrayList;

import org.apache.mina.common.IoSession;
import org.darkquest.gs.builders.ls.MiscPacketBuilder;
import org.darkquest.gs.connection.Packet;
import org.darkquest.gs.connection.RSCPacket;
import org.darkquest.gs.model.Player;
import org.darkquest.gs.model.World;
import org.darkquest.gs.model.snapshot.Activity;
import org.darkquest.gs.model.snapshot.Chatlog;
import org.darkquest.gs.phandler.PacketHandler;
import org.darkquest.gs.tools.DataConversions;


public class FriendHandler implements PacketHandler {
  /**
   * World instance
   */
  public static final World world = World.getWorld();

  private MiscPacketBuilder loginSender = World.getWorld().getServer().getLoginConnector().getActionSender();

  public void handlePacket(Packet p, IoSession session) throws Exception {
    Player player = (Player) session.getAttachment();
    int pID = ((RSCPacket) p).getID();

    long user = player.getUsernameHash();
    long friend = p.readLong();
    switch(pID) {
    case 168: // Add friend
      if(player.friendCount() >= 200) {
        player.getActionSender().sendMessage("Your friend list is too full");
        return;
      }
      loginSender.addFriend(user, friend);
      player.addFriend(friend, 0);
      world.addEntryToSnapshots(new Activity(player.getUsername(), player.getUsername() + " added friend " + DataConversions.hashToUsername(friend) + " at: " + player.getX() + "/" + player.getY()));

      break;
    case 52: // Remove friend
      loginSender.removeFriend(user, friend);
      player.removeFriend(friend);
      world.addEntryToSnapshots(new Activity(player.getUsername(), player.getUsername() + " removed friend " + DataConversions.hashToUsername(friend) + " at: " + player.getX() + "/" + player.getY()));
      break;
    case 25: // Add ignore
      if(player.ignoreCount() >= 200) {
        player.getActionSender().sendMessage("Your ignore list is too full");
        return;
      }
      loginSender.addIgnore(user, friend);
      player.addIgnore(friend);
      break;
    case 108: // Remove ignore
      loginSender.removeIgnore(user, friend);
      player.removeIgnore(friend);
      break;
    case 254: // Send PM
      try {
        byte[] data = p.getRemainingData();
        String s = DataConversions.byteToString(data, 0, data.length);
        ArrayList<String> temp = new ArrayList<String>();
        temp.add(DataConversions.hashToUsername(friend));
        world.addEntryToSnapshots(new Chatlog(player.getUsername(), "(PM) " + s, temp));
        loginSender.sendPM(user, friend, player.isPMod(), data);
      } catch(NegativeArraySizeException e) {
        player.destroy(false);
      }
      break;
    }
  }

}
