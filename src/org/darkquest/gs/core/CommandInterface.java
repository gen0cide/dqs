package org.darkquest.gs.core;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.Collections;
import java.util.ConcurrentModificationException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.HashSet;
import java.util.List;
import java.util.TreeMap;

import org.apache.mina.common.IoSession;
import org.apache.commons.lang3.StringUtils;
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
import org.darkquest.gs.Server;
import org.darkquest.config.Config;
import org.darkquest.config.Constants;

import redis.clients.jedis.Jedis;
import com.google.gson.Gson;
import com.google.gson.ExclusionStrategy;
import com.google.gson.FieldAttributes;
import com.google.gson.GsonBuilder;
import java.io.PrintWriter;
import java.util.ArrayList;
import com.cedarsoftware.util.io.JsonObject;
import com.cedarsoftware.util.io.JsonWriter;
import com.cedarsoftware.util.io.*;

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

  private static String incoming = "command_incoming_" + Config.SERVER_NUM;
  private static String outgoing = "command_outgoing_" + Config.SERVER_NUM;
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
          if(args.length > 0) {
            switch (args[0]) {
              case "count":
                int count = world.countPlayers();
                reply("Current Players Online: " + count);
                break;
              case "list":
                ArrayList<String> current_players = new ArrayList<String>();
                for(Player p : world.getPlayers()) {
                  current_players.add(p.getUsername());
                }
                String[] cp = current_players.toArray(new String[0]);
                reply("Logged In: " + StringUtils.join(cp, ", "));
                break;
              case "unique":
                Set<String> current_ips = new HashSet<String>();
                for(Player p : world.getPlayers()) {
                  current_ips.add(p.getCurrentIP());
                }
                //String[] cip = current_ips.toArray(new String[0]);
                reply("Unique IPs: " + current_ips.size());
                break;
            }
          }
          break;          
        case "defs":
          EntityHandler.loadDefs();
          world.pushClientUpdates();
          reply("Defs reloaded.");
          break;
        case "say":
          if(args.length > 0) {            
            world.sendWorldAnnouncement("@IRC: " + StringUtils.join(args, " "));           
          }
          break;

        // case "dump":
        //   try {
        //     PrintWriter writer = new PrintWriter("/opt/web/world.json", "UTF-8");
        //     String jsons = JsonWriter.objectToJson(World.getWorld().getPlayers());
        //     writer.println(jsons);
        //     writer.close();
        //     reply("Done: http://rsc.beefsec.com/world.json");
        //   } catch(Exception e) {
        //     log("What in the fuck? File not found.");
        //   }
        //   break;
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
