package org.darkquest.gs.phandler.client;

import java.net.InetSocketAddress;

import org.apache.mina.common.IoSession;
import org.darkquest.config.Config;
import org.darkquest.gs.builders.RSCPacketBuilder;
import org.darkquest.gs.connection.Packet;
import org.darkquest.gs.model.Player;
import org.darkquest.gs.model.World;
import org.darkquest.gs.phandler.PacketHandler;
import org.darkquest.gs.util.RSA;
import org.darkquest.gs.util.Buffer;

import java.math.BigInteger;
import java.util.Arrays;



public class PlayerLogin implements PacketHandler {

  /**
   * World instance
   */
  public static final World world = World.getWorld();

  public void handlePacket(Packet p1, IoSession session) throws Exception {
    Player player = (Player) session.getAttachment();
    final String ip = ((InetSocketAddress) session.getRemoteAddress()).getAddress().toString().replaceAll("/", "");
    byte loginCode;
    try {
      boolean reconnecting = (p1.readByte() == 1);
      short clientVersion = p1.readShort();
      int limit30 = (int) p1.readByte();
      int enc_buf_length = ((int) p1.readByte());
      byte[] buff = p1.readBytes(enc_buf_length);
      BigInteger rsa_p = new BigInteger("97428836110452297800816417857719027666621443439141161878468301741566689765913");
      BigInteger rsa_q = new BigInteger("73519307129045672142021472028789557414492175507481976590693256758658848346853");
      BigInteger rsa_n = rsa_p.multiply(rsa_q); // eq known modulus
      BigInteger phi = rsa_p.subtract(BigInteger.valueOf(1)).multiply(rsa_q.subtract(BigInteger.valueOf(1)));
      BigInteger rsa_e = new BigInteger("58778699976184461502525193738213253649000149147835990136706041084440742975821");      
      BigInteger rsa_d = rsa_e.modInverse(phi);
      BigInteger bi1 = new BigInteger(buff);
      BigInteger bi2 = bi1.modPow(rsa_d, rsa_n);
      byte decrypted[] = bi2.toByteArray();
      int b0 = (int) decrypted[0];
      Packet p = new Packet(session, decrypted);
      int b1 = (int) p.readByte();
      int[] sessionKeys = new int[4];
      for(int key = 0; key < sessionKeys.length; key++) {
        sessionKeys[key] = p.readInt();
        System.out.println("sessionKeys["+key+"]="+sessionKeys[key]);
      }
      int luid = p.readInt();
      String username = "";
      String password = "";
      username = p.readString(20).trim();
      password = p.readString(20).trim();
      System.out.println("LOGIN ATTEMPT: [" + username + "] CLIENT_VERSION=" + clientVersion + " ? RUNNING=" + Config.SERVER_VERSION);
      if(world.countPlayers() >= Config.MAX_PLAYERS) {
        loginCode = 10;
      } else if(clientVersion < Config.SERVER_VERSION) {
        loginCode = 4;
      } else if(!player.setSessionKeys(sessionKeys)) {
        loginCode = 5;
      } else {
        player.load(username, password, 0, reconnecting);
        return;
      }
    } catch(Exception e) {
      System.err.println("Login exception with: " + ip);
      e.printStackTrace();
      loginCode = 4;
    }
    RSCPacketBuilder pb = new RSCPacketBuilder();
    pb.setBare(true);
    pb.addByte((byte) loginCode);
    session.write(pb.toPacket());
    player.destroy(true);
  }
}
