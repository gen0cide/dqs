package org.darkquest.gs.plugins.skills;

import org.darkquest.config.Formulae;
import org.darkquest.gs.event.ShortEvent;
import org.darkquest.gs.external.EntityHandler;
import org.darkquest.gs.external.ObjectWoodcuttingDef;
import org.darkquest.gs.model.Bubble;
import org.darkquest.gs.model.GameObject;
import org.darkquest.gs.model.InvItem;
import org.darkquest.gs.model.Player;
import org.darkquest.gs.model.World;
import org.darkquest.gs.plugins.listeners.action.ObjectActionListener;
import org.darkquest.gs.tools.DataConversions;


public class Woodcutting implements ObjectActionListener {

  @Override
  public void onObjectAction(final GameObject object, String command, Player owner) {
    if(command.equals("chop")) {
      handleWoodcutting(object, owner, owner.click);
      return;
    }
    return;
  }

  private void handleWoodcutting(final GameObject object, Player owner, final int click) {
    final ObjectWoodcuttingDef def = EntityHandler.getObjectWoodcuttingDef(object.getID());
    if(owner.isBusy()) {
      return;
    }
    if(!owner.withinRange(object, 2))
      return;
    if(def == null) {    // This shoudln't happen
      return;
    }
    if(owner.getCurStat(8) < def.getReqLevel()) {
      owner.getActionSender().sendMessage("You need a woodcutting level of " + def.getReqLevel() + " to axe this tree.");
      return;
    }
    int axeId = -1;
    for(int a : Formulae.woodcuttingAxeIDs) {
      if(owner.getInventory().countId(a) > 0) {
        axeId = a;
        break;
      }
    }
    if(axeId < 0) {
      owner.getActionSender().sendMessage("You need an axe to chop this tree down.");
      return;
    }
    owner.setBusy(true);
    Bubble bubble = new Bubble(owner, axeId);
    for(Player p : owner.getViewArea().getPlayersInView()) {
      p.informOfBubble(bubble);
    }
    owner.getActionSender().sendMessage("You swing your " + EntityHandler.getItemDef(axeId).getName() + " at the tree...");
    final int axeID = axeId;
    World.getWorld().getDelayedEventHandler().add(new ShortEvent(owner) {
      public void action() {
        if(Formulae.getLog(def, owner.getCurStat(8), axeID)) {
          InvItem log = new InvItem(def.getLogId());
          owner.getInventory().add(log);
          owner.getActionSender().sendMessage("You get some wood.");
          owner.getActionSender().sendInventory();
          owner.incExp(8, def.getExp(), true);
          owner.getActionSender().sendStat(8);
          if(DataConversions.random(1, 100) <= def.getFell()) {
            world.registerGameObject(new GameObject(object.getLocation(), 4, object.getDirection(), object.getType()));
            world.delayedSpawnObject(object.getLoc(), def.getRespawnTime() * 1000);
            owner.setBusy(false);
          } else {
            owner.setBusy(false);
            if(!owner.getInventory().full()) {
              handleWoodcutting(object, owner, owner.click);
            }
          }

        } else {
          owner.getActionSender().sendMessage("You slip and fail to hit the tree.");
          owner.setBusy(false);
          if(!owner.getInventory().full()) {
            handleWoodcutting(object, owner, owner.click);
          }
        }
      }
    });
  }
}
