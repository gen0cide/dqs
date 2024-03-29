package org.darkquest.gs.db;



import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

import org.darkquest.config.Config;
import org.darkquest.gs.external.EntityHandler;
import org.darkquest.gs.external.GameObjectLoc;
import org.darkquest.gs.external.ItemDef;
import org.darkquest.gs.external.ItemDropDef;
import org.darkquest.gs.external.ItemLoc;
import org.darkquest.gs.external.NPCDef;
import org.darkquest.gs.external.NPCLoc;
import org.darkquest.gs.model.GameObject;
import org.darkquest.gs.model.InvItem;
import org.darkquest.gs.model.Item;
import org.darkquest.gs.model.Npc;
import org.darkquest.gs.model.Point;
import org.darkquest.gs.model.Shop;
import org.darkquest.gs.model.World;
import org.darkquest.gs.util.Logger;

/**
 * Used to interact with the database.
 */
public class DBConnection {

  static {
    testForDriver();
  }

  /**
   * The database connection in use
   */
  private Connection con;
  /**
   * A statement for running queries on
   */
  private Statement statement;
  /**
   * All database queries are handled in this class
   */
  private Queries queries;


  public Queries getQueries() {
    if(queries == null) {
      queries = new Queries();
    }
    return queries;
  }

  /**
   * Tests we have a mysql Driver
   */
  private static void testForDriver() {
    try {
      Class.forName("com.mysql.jdbc.Driver");
    } catch(ClassNotFoundException cnfe) {
      cnfe.printStackTrace();
    }
  }

  /**
   * Instantiates a new database connection
   */
  public DBConnection() {
    if(!createConnection()) {
      new Exception("Unable to connect to MySQL").printStackTrace();
      System.exit(1);
    } else {
      Logger.println("Database connection achieved.");
    }
  }
  public boolean createConnection() {
    try {
      con = DriverManager.getConnection("jdbc:mysql://" + Config.MYSQL_HOST + "/" + Config.MYSQL_DB, Config.MYSQL_USER, Config.MYSQL_PASS);
      statement = con.createStatement();
      statement.setEscapeProcessing(true);
      return isConnected();
    } catch(SQLException e) {
      e.printStackTrace();
      return false;
    }
  }

  public boolean isConnected() {
    try {
      statement.executeQuery("SELECT CURRENT_DATE");
      return true;
    } catch(SQLException e) {
      return false;
    }
  }
  public Connection getConnection() {
    return con;
  }
  /**
   * Closes the database conection.
   *
   * @throws SQLException if there was an error when closing the connection
   */
  public void close() throws SQLException {
    con.close();
    con = null;
  }

  /**
   * Loads npcdefs, objects, npcs and items from the Database
   */

  public void loadItemDefs() {
    ResultSet result;
    try {
      result = this.statement.executeQuery("SELECT * FROM `dq_itemdef` order by id asc");
      ArrayList<ItemDef> itemdefs = new ArrayList<ItemDef>();
      while(result.next()) {
        ItemDef i = new ItemDef();
        i.name = result.getString("name");
        i.basePrice = result.getInt("basePrice");
        i.command = result.getString("command");
        i.mask = result.getInt("mask");
        i.members = result.getInt("members") == 1;
        i.sprite = result.getInt("sprite");
        i.stackable = result.getInt("stackable") == 1;
        i.trade = result.getInt("trade") == 1;
        i.wieldable = result.getInt("wieldable");
        itemdefs.add(i);
      }
      EntityHandler.items = itemdefs.toArray(new ItemDef[] {});
    } catch(SQLException e) {
      System.out.println("Unable to load objects from database");
      e.printStackTrace();
      System.exit(1);
    }
  }

  public void loadObjects(World world) {
    ResultSet result;
    try {
      ArrayList<NPCDef> defs = new ArrayList<NPCDef>();
      result = this.con.createStatement().executeQuery("SELECT * FROM `dq_npcdef`");
      while(result.next()) {
        NPCDef def = new NPCDef();
        def.name = result.getString("name");
        def.description = result.getString("description");
        def.command = result.getString("command");
        def.attack = result.getInt("attack");
        def.strength = result.getInt("strength");
        def.hits = result.getInt("hits");
        def.defense = result.getInt("defense");
        def.attackable = result.getBoolean("attackable");
        def.aggressive = result.getBoolean("aggressive");
        def.respawnTime = result.getInt("respawnTime");
        for(int i = 0; i < 12; i++) {
          def.sprites[i] = result.getInt("sprites" + (i + 1));
        }
        def.hairColour = result.getInt("hairColour");
        def.topColour = result.getInt("topColour");
        def.bottomColour = result.getInt("bottomColour");
        def.skinColour = result.getInt("skinColour");
        def.camera1 = result.getInt("camera1");
        def.camera2 = result.getInt("camera2");
        def.walkModel = result.getInt("walkModel");
        def.combatModel = result.getInt("combatModel");
        def.combatSprite = result.getInt("combatSprite");

        ArrayList<ItemDropDef> drops = new ArrayList<ItemDropDef>();
        ResultSet data = this.statement.executeQuery("SELECT * FROM `dq_npcdrops` WHERE npcdef_id = '" + result.getInt("id") + "'");
        while(data.next()) {
          ItemDropDef drop = new ItemDropDef(data.getInt("id"), data.getInt("amount"), data.getInt("weight"));
          drops.add(drop);
        }
        def.drops = drops.toArray(new ItemDropDef[] {});
        defs.add(def);
      }
      EntityHandler.npcs = defs.toArray(new NPCDef[] {});
      for(NPCDef n : EntityHandler.npcs) {
        if(n.isAttackable()) {
          n.respawnTime -= (n.respawnTime / 3);
        }
      }
      result = this.statement.executeQuery("SELECT * FROM `dq_itemdef` order by id asc");
      ArrayList<ItemDef> itemdefs = new ArrayList<ItemDef>();
      while(result.next()) {
        ItemDef i = new ItemDef();
        i.name = result.getString("name");
        i.basePrice = result.getInt("basePrice");
        i.command = result.getString("command");
        i.mask = result.getInt("mask");
        i.members = result.getInt("members") == 1;
        i.sprite = result.getInt("sprite");
        i.stackable = result.getInt("stackable") == 1;
        i.trade = result.getInt("trade") == 1;
        i.wieldable = result.getInt("wieldable");
        itemdefs.add(i);
      }
      EntityHandler.items = itemdefs.toArray(new ItemDef[] {});
      result = this.statement.executeQuery("SELECT * FROM `dq_objects`");
      while(result.next()) {
        world.registerGameObject(new GameObject(new Point(result.getInt("x"), result.getInt("y")), result.getInt("id"), result.getInt("direction"), result.getInt("type")));
      }
      result = this.statement.executeQuery("SELECT * FROM `dq_npclocs`");
      while(result.next()) {
        NPCLoc n = new NPCLoc(result.getInt("id"), result.getInt("startX"), result.getInt("startY"), result.getInt("minX"), result.getInt("maxX"), result.getInt("minY"), result.getInt("maxY"));
        world.registerNpc(new Npc(n));
      }
      result = this.statement.executeQuery("SELECT * FROM `dq_items`");
      while(result.next()) {
        ItemLoc i = new ItemLoc();
        i.id = result.getInt("id");
        i.x = result.getInt("x");
        i.y = result.getInt("y");
        i.amount = result.getInt("amount");
        i.respawnTime = result.getInt("respawn");
        world.registerItem(new Item(i));
      }
      result = this.con.createStatement().executeQuery("SELECT * FROM `dq_shops`");
      while(result.next()) {
        Shop s = new Shop();
        s.name = result.getString("name");
        s.greeting = result.getString("greeting");
        s.general = (result.getInt("general") == 1);
        s.sellModifier = result.getInt("sellModifier");
        s.buyModifier = result.getInt("buyModifier");
        s.minX = result.getInt("minX");
        s.maxX = result.getInt("maxX");
        s.minY = result.getInt("minY");
        s.maxY = result.getInt("maxY");
        s.respawnRate = result.getInt("respawnRate");
        ArrayList<InvItem> items = new ArrayList<InvItem>();
        ResultSet data = this.statement.executeQuery("SELECT * FROM `dq_shops_items` WHERE shop_id = '" + result.getInt("id") + "'");
        while(data.next()) {
          items.add(new InvItem(data.getInt("id"), data.getInt("amount")));
        }
        s.items = items;
        world.registerShop(s);
      }
    } catch(SQLException e) {
      System.out.println("Unable to load objects from database");
      e.printStackTrace();
      System.exit(1);
    }
  }


  /**
   * Stores a ground item to the database
   * @param gameObject
   */
  public void storeGroundItemToDatabase(Item item) {
    ItemLoc i = item.getLoc();
    try {
      World.getWorld().getDB().getConnection().createStatement().execute("INSERT INTO `dq_items`(`id`, `x`, `y`, `amount`, `respawn`) VALUES ('" + item.getID() + "', '" + i.getX() + "', '" + i.getY() + "', '" + i.getAmount() + "', '" + i.getRespawnTime() + "')");
    } catch(SQLException e) {
      e.printStackTrace();
    }
  }


  /**
   * Stores a gameobject to the database
   * @param gameObject
   */
  public void storeGameObjectToDatabase(GameObject obj) {
    GameObjectLoc gameObject = obj.getLoc();
    try {
      World.getWorld().getDB().getConnection().createStatement().execute("INSERT INTO `dq_objects`(`x`, `y`, `id`, `direction`, `type`) VALUES ('" + gameObject.getX() + "', '" + gameObject.getY() + "', '" + gameObject.getId() + "', '" + gameObject.getDirection() + "', '" + gameObject.getType() + "')");
    } catch(SQLException e) {
      e.printStackTrace();
    }
  }
  /**
   * Deletes a gameobject from the database
   * @param gameObject
   */
  public void deleteGameObjectFromDatabase(GameObject obj) {
    GameObjectLoc gameObject = obj.getLoc();
    try {
      this.statement.execute("DELETE FROM `dq_objects` WHERE `x` = '" + gameObject.getX() + "' AND `y` =  '" + gameObject.getY() + "' AND `id` = '" + gameObject.getId() + "' AND `direction` = '" + gameObject.getDirection() + "' AND `type` = '" + gameObject.getType() + "'");
    } catch(SQLException e) {
      e.printStackTrace();
    }
  }
  /**
   * Stores a NPC to the database
   * @param n
   */
  public void storeNpcToDatabase(Npc n) {
    NPCLoc npc = n.getLoc();
    try {
      this.statement.execute("INSERT INTO `dq_npclocs`(`id`,`startX`,`minX`,`maxX`,`startY`,`minY`,`maxY`) VALUES('" + npc.getId() + "', '" + npc.startX() + "', '" + npc.minX() + "', '" + npc.maxX() + "','" + npc.startY() + "','" + npc.minY() + "','" + npc.maxY() + "')");
    } catch(SQLException e) { }
  }

}
