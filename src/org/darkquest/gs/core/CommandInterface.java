package org.darkquest.gs.core;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.Collections;
import java.util.ConcurrentModificationException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.List;
import java.util.TreeMap;

import org.apache.mina.common.IoSession;
import org.darkquest.gs.connection.PacketQueue;
import org.darkquest.gs.connection.RSCPacket;
import org.darkquest.gs.event.DelayedEvent;
import org.darkquest.gs.model.ActiveTile;
import org.darkquest.gs.model.Player;
import org.darkquest.gs.model.Shop;
import org.darkquest.gs.model.World;
import org.darkquest.gs.model.snapshot.Snapshot;
import org.darkquest.gs.phandler.PacketHandler;
import org.darkquest.gs.phandler.PacketHandlerDef;
import org.darkquest.gs.plugins.PluginHandler;
import org.darkquest.gs.util.Logger;
import org.darkquest.gs.util.PersistenceManager;
import org.darkquest.gs.external.EntityHandler;

import redis.clients.jedis.Jedis;

/**
 * The central motor of the game. This class is responsible for the primary
 * operation of the entire game.
 */
public final class CommandInterface extends Thread {

  /**
   * World instance
   */
  private static final World world = World.getWorld();

  private Jedis jedis; // = new Jedis("localhost");

  private static String incoming = "command_incoming";
  private static String outgoing = "command_outgoing";
  private List<String> messages;

  public CommandInterface() {
    jedis = new Jedis("localhost");
    messages = null;
  }

  public void run() {
    log("\nCommand Interface thread started.");
    while(true) {
      messages = jedis.blpop(0,incoming);
      String command_string = messages.get(1).trim();
      int firstSpace = command_string.indexOf(" ");
      String cmd = command_string;
      String[] args = new String[0];
      if(firstSpace != -1) {
        cmd = command_string.substring(0, firstSpace).trim();
        args = command_string.substring(firstSpace + 1).trim().split(" ");
      }
      switch (cmd) {
        case "online":
          int count = world.countPlayers();
          reply("Current Players Online: " + count);
          break;
        case "defs":
          EntityHandler.loadDefs();
          world.pushClientUpdates();
          reply("Defs reloaded.");
          break;
      }
    }
  }


  public void reply(String s) {
    jedis.rpush(outgoing, s);
  }

  public synchronized void log(String s) {
    Logger.println(s);
  }
}
