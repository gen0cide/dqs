package org.darkquest.gs.plugins.commands;

import org.darkquest.config.Constants;
import org.darkquest.gs.core.ClientUpdater;
import org.darkquest.gs.external.EntityHandler;
import org.darkquest.gs.external.ItemLoc;
import org.darkquest.gs.model.GameObject;
import org.darkquest.gs.model.InvItem;
import org.darkquest.gs.model.Item;
import org.darkquest.gs.model.Player;
import org.darkquest.gs.model.World;
import org.darkquest.gs.model.Npc;
import org.darkquest.gs.plugins.listeners.action.CommandListener;
import org.darkquest.gs.tools.DataConversions;
import org.darkquest.gs.util.Logger;

public class Admins implements CommandListener {
  /**
   * World instance
   */
  public static final World world = World.getWorld();

  @Override
  public void onCommand(String command, String[] args, Player player) {
    if(!player.isAdmin()) {
      return;
    }
    if(command.equals("ban") || command.equals("unban")) {
      boolean banned = command.equals("ban");
      if(args.length != 1) {
        player.getActionSender().sendMessage("Invalid args. Syntax: " + (banned ? "BAN" : "UNBAN") + " name");
        return;
      }
      World.getWorld().getServer().getLoginConnector().getActionSender().banPlayer(player, DataConversions.usernameToHash(args[0]), banned);
      return;
    }
    if(command.equals("shutdown")) {
      Logger.mod(player.getUsername() + " shut down the server!");
      World.getWorld().getServer().kill();
      return;
    }
    if(command.equals("update")) {
      String reason = "";
      if(args.length > 0) {
        for(String s : args) {
          reason += (s + " ");
        }
        reason = reason.substring(0, reason.length() - 1);
      }
      if(World.getWorld().getServer().shutdownForUpdate()) {
        Logger.mod(player.getUsername() + " updated the server: " + reason);
        for(Player p : world.getPlayers()) {
          p.getActionSender().sendAlert("The server will be shutting down in 60 seconds: " + reason, false);
          p.getActionSender().startShutdown(60);
        }
      }
      return;
    }
    if(command.equals("appearance")) {
      player.setChangingAppearance(true);
      player.getActionSender().sendAppearanceScreen();
      return;
    }
    if(command.equals("updatedefs")) {
      Logger.mod(player.getUsername() + " is updating defs.");
      String reason = "";
      if(args.length > 0) {
        for(String s : args) {
          reason += (s + " ");
        }
        reason = reason.substring(0, reason.length() - 1);
      }
      if(World.getWorld().getServer().emptyForUpdate()) {
        for(Player p : world.getPlayers()) {
          p.getActionSender().sendAlert("Defs have been updated. You will be automatically logged out in 60 seconds.", false);
          p.getActionSender().startShutdown(60);
        }
      }    
    }
    if(command.equals("item")) {
      if(args.length < 1 || args.length > 2) {
        player.getActionSender().sendMessage("Invalid args. Syntax: ITEM id [amount]");
        return;
      }
      int id = Integer.parseInt(args[0]);
      if(EntityHandler.getItemDef(id) != null) {
        int amount = 1;
        if(args.length == 2) {
          amount = Integer.parseInt(args[1]);
        }
        if(EntityHandler.getItemDef(id).isStackable())
          player.getInventory().add(new InvItem(id, amount));
        else {
          for(int i = 0; i < amount; i++) {
            player.getInventory().add(new InvItem(id, 1));
          }
        }
        player.getActionSender().sendInventory();
        Logger.mod(player.getUsername() + " spawned themself " + amount + " " + EntityHandler.getItemDef(id).getName() + "(s)");
      } else {
        player.getActionSender().sendMessage("Invalid id");
      }
      return;
    }
    if(command.equals("grounditem")) {
      if(args.length < 1 || args.length > 2) {
        player.getActionSender().sendMessage("Invalid args. Syntax: grounditem id [amount]");
        return;
      }
      int id = Integer.parseInt(args[0]);
      if(EntityHandler.getItemDef(id) != null) {
        int amount = 1;
        if(args.length == 2) {
          amount = Integer.parseInt(args[1]);
        }
        ItemLoc i = new ItemLoc();
        i.id = id;
        i.x = player.getX();
        i.y = player.getY();
        i.amount = amount;
        i.respawnTime = 60;
        Item item = new Item(i);
        world.registerItem(item);
        world.getDB().storeGroundItemToDatabase(item);
        player.getActionSender().sendMessage("Grounditem stored in the database");
      } else {
        player.getActionSender().sendMessage("Invalid id");
      }
      return;
    }
    if(command.equals("object")) {
      if(args.length < 1 || args.length > 3) {
        player.getActionSender().sendMessage("Invalid args. Syntax: OBJECT id [direction] true/false");
        return;
      }
      boolean percist = (args.length > 1 ? args[2].equalsIgnoreCase("true") : false);
      int id = Integer.parseInt(args[0]);
      if(id < 0) {
        GameObject object = world.getTile(player.getLocation()).getGameObject();
        if(object != null) {
          world.unregisterGameObject(object);
          if(percist) {
            player.getActionSender().sendMessage("Deleted object from the database");
            world.getDB().deleteGameObjectFromDatabase(object);
          }
        }
      } else if(EntityHandler.getGameObjectDef(id) != null) {
        int dir = args.length == 2 ? Integer.parseInt(args[1]) : 0;
        GameObject obj = new GameObject(player.getLocation(), id, dir, 0);
        world.registerGameObject(obj);
        if(percist) {
          player.getActionSender().sendMessage("Stored object in the database");
          world.getDB().storeGameObjectToDatabase(obj);
        }
      } else {
        player.getActionSender().sendMessage("Invalid id");
      }
      return;
    }
    if(command.equals("door")) {
      if(!player.getLocation().inModRoom()) {
        player.getActionSender().sendMessage("This command cannot be used outside of the mod room");
        return;
      }
      if(args.length < 1 || args.length > 2) {
        player.getActionSender().sendMessage("Invalid args. Syntax: DOOR id [direction]");
        return;
      }
      int id = Integer.parseInt(args[0]);
      if(id < 0) {
        GameObject object = world.getTile(player.getLocation()).getGameObject();
        if(object != null) {
          world.unregisterGameObject(object);
        }
      } else if(EntityHandler.getDoorDef(id) != null) {
        int dir = args.length == 2 ? Integer.parseInt(args[1]) : 0;
        world.registerGameObject(new GameObject(player.getLocation(), id, dir, 1));
      } else {
        player.getActionSender().sendMessage("Invalid id");
      }
      return;
    }
    if(command.equals("dropall")) {
      player.getInventory().getItems().clear();
      player.getActionSender().sendInventory();
      return;
    }
    if(command.equals("maxout")) {
      int new_stat = 231;
      int[] new_stats = new int[18];
      for(int i = 0; i < 18; i++) {
        new_stats[i] = new_stat;
      }
      player.setCurStat(new_stats);
      player.getActionSender().sendStats();
    }
    if(command.equals("spawn")) {
      if(args.length != 1) {
        player.getActionSender().sendMessage("Inavlid args. Syntax: SPAWN id");
      }
      int base_x = player.getX();
      int max_x = base_x + 20;
      int min_x = base_x - 20;
      int base_y = player.getY();
      int max_y = base_y + 20;
      int min_y = base_y - 20;
      int base_i = Integer.parseInt(args[0]);
      Npc new_npc = new Npc(base_i, base_x, base_y, min_x, max_x, min_y, max_y);
      world.registerNpc(new_npc);
    }
    if(command.equals("f2pwild")) {
      Constants.GameServer.F2P_WILDY = true;
      player.getActionSender().sendMessage("F2P wilderness enabled");
    }
    if(command.equals("p2pwild")) {
      Constants.GameServer.F2P_WILDY = false;
      player.getActionSender().sendMessage("P2P wilderness enabled");
    }
    if(command.equals("thread")) {
      ClientUpdater.threaded = !ClientUpdater.threaded;
      player.getActionSender().sendMessage("Threaded client updater: " + ClientUpdater.threaded);
    }
    if(command.equals("noaggro")) {
      player.setNonaggro(!player.isNonaggro());
      player.getActionSender().sendMessage("NPCs are aggro: " + player.isNonaggro());
    }
    if(command.equals("doublexp")) {
      world.setDoubleXpWeekend(!world.isDoubleXpWeekend());
      player.getActionSender().sendMessage("Double XP for skills is: " + world.isDoubleXpWeekend());
    }
    return;
  }


}
