package org.darkquest.gs.connection;

import org.apache.mina.common.ByteBuffer;
import org.apache.mina.common.IoSession;
import org.apache.mina.filter.codec.CumulativeProtocolDecoder;
import org.apache.mina.filter.codec.ProtocolDecoderOutput;
import org.darkquest.gs.util.Logger;

import java.util.Random;
import java.util.Arrays;


/**
 * A decoder for the RSC protocol. Parses the incoming data from an IoSession
 * and outputs it as a <code>RSCPacket</code> object.
 */
public class RSCProtocolDecoder extends CumulativeProtocolDecoder {
  /**
   * Releases the buffer used by the given session.
   *
   * @param session
   *            The session for which to release the buffer
   * @throws Exception
   *             if failed to dispose all resources
   */
  public void dispose(IoSession session) throws Exception {
    super.dispose(session);
  }

  /**
   * Parses the data in the provided byte buffer and writes it to
   * <code>out</code> as a <code>RSCPacket</code>.
   *
   * @param session
   *            The IoSession the data was read from
   * @param in
   *            The buffer
   * @param out
   *            The decoder output stream to which to write the
   *            <code>RSCPacket</code>
   * @return Whether enough data was available to create a packet
   */
  protected synchronized boolean doDecode(IoSession session, ByteBuffer in, ProtocolDecoderOutput out) {
    try {      
      // make sure the packet has contents
      if(in.remaining() >= 1) {
        // parse the first two bytes        
        byte[] buf = new byte[] { in.get(), in.get() };                
        if(in.remaining() > 0) {          
          // ok we have a valid packet, get the ID
          int id = in.get() & 0xff;
          // build a payload byte[] to pass
          // need to add room for the LSB
          byte[] payload = new byte[in.remaining() + 1];
          if(in.remaining() > 0) {
            // move the ByteBuffer contents into the payload
            in.get(payload, 0, in.remaining());
          }
          // add the LSB to the end of the payload
          payload[payload.length-1] = in.get(1);  
          // Create a new packet with the parsed payload        
          RSCPacket p = new RSCPacket(session, id, payload);          
          // Send it on up the stack to the Packet Handler
          out.write(p);
          return true;
        } else {        
          in.rewind();
          return false;
        }
      }
    } catch(Exception e) {
      e.printStackTrace();
    }
    return false;
  }
}
