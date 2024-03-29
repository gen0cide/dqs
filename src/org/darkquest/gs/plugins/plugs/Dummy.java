package org.darkquest.gs.plugins.plugs;

import java.util.Arrays;

import org.darkquest.gs.event.MiniEvent;
import org.darkquest.gs.model.GameObject;
import org.darkquest.gs.model.Player;
import org.darkquest.gs.model.World;
import org.darkquest.gs.plugins.listeners.action.ObjectActionListener;

public class Dummy implements ObjectActionListener {

  static int[] ids;
  static {
    ids = new int[] { 49 };
    Arrays.sort(ids);
  }

  @Override
  public void onObjectAction(GameObject object, String command, Player owner) {
    if(Arrays.binarySearch(ids, object.getID()) >= 0) {
      owner.setBusy(true);
      owner.getActionSender().sendMessage("You attempt to hit the Dummy");

      World.getWorld().getDelayedEventHandler().add(new MiniEvent(owner, 3500) {
        public void action() {
          owner.setBusy(false);
          int lvl = owner.getCurStat(0);
          if(lvl > 7 || owner.getMaxStat(0) >= 40) {
            owner.getActionSender().sendMessage("There is only so much you can learn from hitting a Dummy");
            return;
          }
          owner.getActionSender().sendMessage("You hit the Dummy");
          owner.incExp(0, 5, false);
          owner.getActionSender().sendStat(0);
        }
      });
      return;
    }
  }
}
