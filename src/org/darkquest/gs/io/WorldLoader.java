package org.darkquest.gs.io;

import java.io.BufferedInputStream;
import java.io.File;
import java.nio.ByteBuffer;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.darkquest.config.Config;
import org.darkquest.gs.external.EntityHandler;
import org.darkquest.gs.model.Sector;
import org.darkquest.gs.model.World;
import org.darkquest.gs.tools.DataConversions;
import org.darkquest.gs.util.Logger;



public class WorldLoader {
  private ZipFile tileArchive;

  private void loadSection(int sectionX, int sectionY, int height, World world, int bigX, int bigY) {

    Sector s = null;
    try {
      String filename = "h" + height + "x" + sectionX + "y" + sectionY;
      ZipEntry e = tileArchive.getEntry(filename);
      if(e == null) {
        throw new Exception("Missing tile: " + filename);
      }
      ByteBuffer data = DataConversions.streamToBuffer(new BufferedInputStream(tileArchive.getInputStream(e)));
      s = Sector.unpack(data);
    } catch(Exception e) {
      Logger.error(e);
    }
    for(int y = 0; y < Sector.HEIGHT; y++) {
      for(int x = 0; x < Sector.WIDTH; x++) {
        int bx = bigX + x;
        int by = bigY + y;
        if(!world.withinWorld(bx, by)) {
          continue;
        }

        world.getTileValue(bx, by).overlay = s.getTile(x, y).groundOverlay;
        world.getTileValue(bx, by).diagWallVal = s.getTile(x, y).diagonalWalls;
        world.getTileValue(bx, by).horizontalWallVal = s.getTile(x, y).horizontalWall;
        world.getTileValue(bx, by).verticalWallVal = s.getTile(x, y).verticalWall;
        world.getTileValue(bx, by).elevation = s.getTile(x, y).groundElevation;
        /** start of shit **/
        if((s.getTile(x, y).groundOverlay & 0xff) == 250) {
          s.getTile(x, y).groundOverlay = (byte) 2;
        }
        /** break in shit **/
        int groundOverlay = s.getTile(x, y).groundOverlay & 0xFF;
        if(groundOverlay > 0 && EntityHandler.getTileDef(groundOverlay - 1).getObjectType() != 0) {
          world.getTileValue(bx, by).mapValue |= 0x40;    // 64
        }

        int verticalWall = s.getTile(x, y).verticalWall & 0xFF;
        if(verticalWall > 0 && EntityHandler.getDoorDef(verticalWall - 1).getUnknown() == 0 && EntityHandler.getDoorDef(verticalWall - 1).getDoorType() != 0) {
          world.getTileValue(bx, by).mapValue |= 1;    // 1
          world.getTileValue(bx, by - 1).mapValue |= 4;    // 4
        }

        int horizontalWall = s.getTile(x, y).horizontalWall & 0xFF;
        if(horizontalWall > 0 && EntityHandler.getDoorDef(horizontalWall - 1).getUnknown() == 0 && EntityHandler.getDoorDef(horizontalWall - 1).getDoorType() != 0) {
          world.getTileValue(bx, by).mapValue |= 2;    // 2
          world.getTileValue(bx - 1, by).mapValue |= 8;    // 8
        }

        int diagonalWalls = s.getTile(x, y).diagonalWalls;
        if(diagonalWalls > 0 && diagonalWalls < 12000 && EntityHandler.getDoorDef(diagonalWalls - 1).getUnknown() == 0 && EntityHandler.getDoorDef(diagonalWalls - 1).getDoorType() != 0) {
          world.getTileValue(bx, by).mapValue |= 0x20;    // 32
        }
        if(diagonalWalls > 12000 && diagonalWalls < 24000 && EntityHandler.getDoorDef(diagonalWalls - 12001).getUnknown() == 0 && EntityHandler.getDoorDef(diagonalWalls - 12001).getDoorType() != 0) {
          world.getTileValue(bx, by).mapValue |= 0x10;    // 16
        }
        /** end of shit **/
      }
    }
  }


  public void loadWorld(World world) {
    try {
      tileArchive = new ZipFile(new File(Config.CONF_DIR, "data/Landscape.rscd"));
    } catch(Exception e) {
      Logger.error(e);
    }
    for(int lvl = 0; lvl < 4; lvl++) {
      int wildX = 2304;
      int wildY = 1776 - (lvl * 944);
      for(int sx = 0; sx < 1000; sx += 48) {
        for(int sy = 0; sy < 1000; sy += 48) {
          int x = (sx + wildX) / 48;
          int y = (sy + (lvl * 944) + wildY) / 48;
          loadSection(x, y, lvl, world, sx, sy + (944 * lvl));
        }
      }
    }
    System.gc();
  }

}
