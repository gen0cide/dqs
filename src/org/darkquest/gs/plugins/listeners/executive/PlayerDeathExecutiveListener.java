package org.darkquest.gs.plugins.listeners.executive;

import org.darkquest.gs.model.Player;

public interface PlayerDeathExecutiveListener {
  /**
   * Return true to prevent the default action on death (stake item drop, wild item drop etc)
   * @param p
   * @return
   */
  public boolean blockPlayerDeath(Player p);
}
