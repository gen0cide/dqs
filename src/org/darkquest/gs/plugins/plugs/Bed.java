package org.darkquest.gs.plugins.plugs;

import org.darkquest.gs.event.ShortEvent;
import org.darkquest.gs.model.GameObject;
import org.darkquest.gs.model.Player;
import org.darkquest.gs.model.World;
import org.darkquest.gs.plugins.listeners.action.ObjectActionListener;

public class Bed implements ObjectActionListener {

  @Override
  public void onObjectAction(GameObject object, String command, Player owner) {
    if(command.equalsIgnoreCase("rest")) {
      owner.getActionSender().sendMessage("You rest on the bed");
      World.getWorld().getDelayedEventHandler().add(new ShortEvent(owner) {
        public void action() {
          owner.getActionSender().sendEnterSleep();
        }
      });
    }
  }
}
