package org.darkquest.gs.plugins.commands;

import org.darkquest.gs.event.SingleEvent;
import org.darkquest.gs.external.EntityHandler;
import org.darkquest.gs.model.Mob;
import org.darkquest.gs.model.Npc;
import org.darkquest.gs.model.Player;
import org.darkquest.gs.model.World;
import org.darkquest.gs.plugins.listeners.action.CommandListener;
import org.darkquest.gs.states.CombatState;
import org.darkquest.gs.tools.DataConversions;
import org.darkquest.gs.util.Logger;

public class Moderator implements CommandListener {
  /**
   * World instance
   */
  public static final World world = World.getWorld();



  @Override
  public void onCommand(String command, String[] args, Player player) {

    if(!player.isMod())
      return;

    if(command.equals("modroom")) {
      Logger.mod(player.getUsername() + " teleported to the mod room");
      player.teleport(70, 1640, true);
      return;
    } else if(command.equals("npc")) {
      if(args.length < 1) {
        player.getActionSender().sendMessage("Invalid args. Syntax: NPC id");
        return;
      }
      boolean percist = (args.length > 1 ? args[1].equalsIgnoreCase("true") : false);

      int id = Integer.parseInt(args[0]);
      if(EntityHandler.getNpcDef(id) != null) {
        final Npc n = new Npc(id, player.getX(), player.getY(), player.getX() - 2, player.getX() + 2, player.getY() - 2, player.getY() + 2);
        n.setRespawn(percist);
        world.registerNpc(n);
        if(!percist) {
          World.getWorld().getDelayedEventHandler().add(new SingleEvent(null, 60000) {
            public void action() {
              Mob opponent = n.getOpponent();
              if(opponent != null) {
                opponent.resetCombat(CombatState.ERROR);
              }
              n.resetCombat(CombatState.ERROR);
              world.unregisterNpc(n);
              n.remove();
            }
          });
        } else {
          if(player.isAdmin()) {
            player.getActionSender().sendMessage("Npc stored to the database.");
            world.getDB().storeNpcToDatabase(n);
          }
        }
        Logger.mod(player.getUsername() + " spawned a " + n.getDef().getName() + " at " + player.getLocation().toString());
      } else {
        player.getActionSender().sendMessage("Invalid id");
      }
      return;
    }
    return;
  }

}
