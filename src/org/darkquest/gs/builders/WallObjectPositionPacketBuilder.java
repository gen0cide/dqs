package org.darkquest.gs.builders;

import java.util.Collection;

import org.darkquest.gs.connection.RSCPacket;
import org.darkquest.gs.model.GameObject;
import org.darkquest.gs.model.Player;
import org.darkquest.gs.tools.DataConversions;
import org.darkquest.gs.util.StatefulEntityCollection;


public class WallObjectPositionPacketBuilder {
  private Player playerToUpdate;

  public RSCPacket getPacket() {
    StatefulEntityCollection<GameObject> watchedObjects = playerToUpdate.getWatchedObjects();
    if(watchedObjects.changed()) {
      Collection<GameObject> newObjects = watchedObjects.getNewEntities();
      Collection<GameObject> knownObjets = watchedObjects.getKnownEntities();
      RSCPacketBuilder packet = new RSCPacketBuilder();
      packet.setID(95);
      for(GameObject o : knownObjets) {
        if(o.getType() != 1) {
          continue;
        }
        // We should remove ones miles away differently I think
        if(watchedObjects.isRemoving(o)) {
          byte[] offsets = DataConversions.getObjectPositionOffsets(o.getLocation(), playerToUpdate.getLocation());
          packet.addShort(60000);
          packet.addByte(offsets[0]);
          packet.addByte(offsets[1]);
          packet.addByte((byte) o.getDirection());
        }
      }
      for(GameObject o : newObjects) {
        if(o.getType() != 1) {
          continue;
        }
        byte[] offsets = DataConversions.getObjectPositionOffsets(o.getLocation(), playerToUpdate.getLocation());
        packet.addShort(o.getID());
        packet.addByte(offsets[0]);
        packet.addByte(offsets[1]);
        packet.addByte((byte) o.getDirection());
      }
      return packet.toPacket();
    }
    return null;
  }

  /**
   * Sets the player to update
   */
  public void setPlayer(Player p) {
    playerToUpdate = p;
  }
}
