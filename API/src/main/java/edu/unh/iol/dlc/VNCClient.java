/*
 *                       University of New Hampshire
 *                       InterOperability Laboratory
 *                           Copyright (c) 2014
 *
 * This software is provided by the IOL ``AS IS'' and any express or implied
 * warranties, including, but not limited to, the implied warranties of
 * merchantability and fitness for a particular purpose are disclaimed.
 * In no event shall the InterOperability Lab be liable for any direct,
 * indirect, incidental, special, exemplary, or consequential damages.
 *
 * This software may not be resold without the express permission of
 * the InterOperability Lab.
 *
 * Feedback on this code may be sent to Mike Johnson (mjohnson@iol.unh.edu)
 * and dlnalab@iol.unh.edu.
 */
package edu.unh.iol.dlc;

import java.io.*;
import java.net.Socket;
import java.nio.charset.Charset;
import java.util.SortedSet;
import java.util.TreeSet;

import org.sikuli.basics.Debug;

/**
 * The VNCClient class controls all of the messages between
 * the client and the server.
 *
 * @author Mike Johnson
 */
public class VNCClient {
  private static final SortedSet<ProtocolVersion> SUPPORTED_VERSIONS;
  static {
    SUPPORTED_VERSIONS = new TreeSet<>();
    SUPPORTED_VERSIONS.add(ProtocolVersion.parse("RFB 003.003"));
    SUPPORTED_VERSIONS.add(ProtocolVersion.parse("RFB 003.007"));
    SUPPORTED_VERSIONS.add(ProtocolVersion.parse("RFB 003.008"));
  }

  private static final Charset ASCII = Charset.forName("US-ASCII");

  public static final int SECURITY_TYPE_INVALID = 0;
  public static final int SECURITY_TYPE_NONE = 1;
  public static final int SECURITY_TYPE_VNC_AUTH = 2;

  private static final int VNC_KEY_EVENT_MSG = 4;

  private static final int VNC_KEY_EVENT_DOWN = 1;
  private static final int VNC_KEY_EVENT_UP = 0;

  private static final int VNC_POINTER_EVENT_MSG = 5;
  public static final int VNC_POINTER_EVENT_BUTTON_1 = 1 << 0;
  public static final int VNC_POINTER_EVENT_BUTTON_2 = 1 << 1;
  public static final int VNC_POINTER_EVENT_BUTTON_3 = 1 << 2;
  public static final int VNC_POINTER_EVENT_BUTTON_4 = 1 << 3;
  public static final int VNC_POINTER_EVENT_BUTTON_5 = 1 << 4;
  public static final int VNC_POINTER_EVENT_BUTTON_6 = 1 << 5;
  public static final int VNC_POINTER_EVENT_BUTTON_7 = 1 << 6;
  public static final int VNC_POINTER_EVENT_BUTTON_8 = 1 << 7;

  /*
         * Below are the fields and objects associated with the handshaking phase
         */
  private BufferedOutputStream out = null;
  private BufferedInputStream in = null;
  private DataOutputStream dataOut = null;
  private DataInputStream dataIn = null;
  private int version = 0;
  private Socket socket = null;
  private int bef;

  /**
   * Constructor
   *
   * @param soc Socket to be used in VNC connection
   */
  public VNCClient(Socket soc) {
    socket = soc;
    try {
      socket.setTcpNoDelay(true);
      out = new BufferedOutputStream(socket.getOutputStream());
      in = new BufferedInputStream(socket.getInputStream());
      dataOut = new DataOutputStream(out);
      dataIn = new DataInputStream(in);
    } catch (IOException e) {
      Debug.log(-1, "Error: IO Exception" + e);
    }
  }

//Handshaking Phase*****************************************************************************/

    /* Below are all of the methods associated with the establishing the VNC
     * Connection.
     */

  /**
   * This method reads the Protocol Version message from the server and then
   * selects the highest protocol version supported by the server.  If an
   * unrecognized protocol version is sent, the client closes the connection.
   * The encoding of the Protocol Version message is 7-bit ASCII.
   */
  protected void protocolHandshake() throws IOException {
    ProtocolVersion version = ProtocolVersion.parse(readLine());

    ProtocolVersion clientVersion = null;
    for (ProtocolVersion supportedVersion : SUPPORTED_VERSIONS) {
      if (version.compareTo(supportedVersion) >= 0) {
        clientVersion = supportedVersion;
      }
    }

    if (clientVersion == null) {
      throw new IOException("Unsupported protocol version: " + version);
    }

    final String replyCode = clientVersion.toString();
    writeLine(replyCode);
    out.flush();
    this.version = clientVersion.getMinorVersion();
  }

  /**
   * This method handles the initial security message exchanges between the
   * client and the server.  If RFB 3.3 is used the server will select the
   * security type and tell the client.  Otherwise the server will list the
   * security types that it supports and the client will select the one it
   * wants to use. If there is an error the server tells the client and the
   * client prints the reason to the standard error.
   *
   * @return selectedType If there is an IOerror the method returns a -1,
   * otherwise it returns the security type selected
   * for use.
   */
  protected int securityInit(int desiredSecurityType) throws IOException {
    if (version >= 7) {
      int numSecurityTypes = (int) dataIn.readByte();
      if (numSecurityTypes == 0) {
        int reasonLength = dataIn.readInt();
        byte[] reasonBytes = new byte[reasonLength];
        readFully(reasonBytes);
        String reason = new String(reasonBytes, "US-ASCII");
        Debug.log(-1, "Error: Server reported an error, closing connection: %s", reason);
        socket.close();
        return -1;
      }

      byte[] securityTypes = new byte[numSecurityTypes];
      readFully(securityTypes);

      boolean foundDesiredSecurityType = false;
      for (int i = 0; i < securityTypes.length; i++) {
        if (securityTypes[i] == desiredSecurityType) {
          foundDesiredSecurityType = true;
          break;
        }
      }

      if (foundDesiredSecurityType) {
        dataOut.write((byte) desiredSecurityType);
        dataOut.flush();

        return desiredSecurityType;
      } else {
        Debug.log(-1, "Error: Desired security type not supported by Server, closing connection");
        socket.close();
        return -1;
      }
    } else {
      return dataIn.readInt();
    }
  }

  /**
   * Method that takes the selected security type and calls the necessary
   * methods involved with that security type.
   *
   * @param type The security type to be used as defined in the standard
   * @param password
   */
  protected void securityMethod(int type, String password) throws IOException
  {
    switch (type) {
      case SECURITY_TYPE_INVALID:
        try {
          Debug.log(-1, "Error: Server" +
                  " reported an error, closing connection");
          socket.close();
        } catch (IOException e) {
          Debug.log(-1, "Error: IO Exception" + e);
        }
        break;
      case SECURITY_TYPE_NONE:
        if (version == 8) {
          securityResult();
        }
        break;
      case SECURITY_TYPE_VNC_AUTH:
        byte[] challenge = new byte[16];
        readFully(challenge);

        byte[] key = new byte[8];
        byte[] passwordBytes = password.getBytes(ASCII);
        System.arraycopy(passwordBytes, 0, key, 0, Math.min(key.length, passwordBytes.length));

        DesCipher des = new DesCipher(key);

        byte[] result = challenge.clone();
        des.encrypt(result, 0, result, 0);
        des.encrypt(result, 8, result, 8);

        out.write(result);
        out.flush();

        securityResult();
        break;
      default:
        try {
          Debug.log(-1, "Error: Desired security type Not supported, closing connection");
          socket.close();
        } catch (IOException e) {
          Debug.log(-1, "Error: IO Exception" + e);
        }
        break;
    }
  }

  /**
   * Handles the server's response to how the security handshaking went.
   */
  protected void securityResult() {
    try {
      int securityResult = dataIn.readInt();
      if (securityResult != 0) {
        Debug.log(3, "Error: Server reported an error (%d), closing connection", securityResult);
        socket.close();
      }
    } catch (IOException e) {
      Debug.log(-1, "Error: IO Exception" + e);
    }
  }

  /**
   * Method that tells the server whether to share
   * the connection with others.
   *
   * @param share 1 Share the connection with other clients
   *              0 Do not share the connection
   */
  protected void clientInit(int share) throws IOException,
          InterruptedException {
    dataOut.writeByte(share);
    dataOut.flush();
  }

  /**
   * Method that listens to the ServerInit message and records information
   * about the framebuffer.
   *
   * @return framebuffer information
   */
  protected synchronized int[] listenServerInit() throws IOException,
          InterruptedException {
    int[] data = new int[12];
    data[0] = dataIn.readUnsignedShort();  //width
    data[1] = dataIn.readUnsignedShort();  //height
    data[2] = dataIn.readUnsignedByte();   //bitsPerPixel
    data[3] = dataIn.readUnsignedByte();   //depth
    data[4] = dataIn.readUnsignedByte();   //bigEndianFlag
    bef = data[4]; //for use later
    data[5] = dataIn.readUnsignedByte();   //trueColorFlag
    data[6] = dataIn.readUnsignedShort();  //redMax
    data[7] = dataIn.readUnsignedShort();  //greenMax
    data[8] = dataIn.readUnsignedShort();  //blueMax
    data[9] = dataIn.readUnsignedByte();   //redShift
    data[10] = dataIn.readUnsignedByte();   //greenShift
    data[11] = dataIn.readUnsignedByte();   //blueShift
    dataIn.readUnsignedByte();
    dataIn.readUnsignedByte(); //padding
    dataIn.readUnsignedByte();
    return data;
  }

  /**
   * Method that reads the desktop name of the remote host.
   *
   * @return The name of the desktop
   * @throws IOException
   */
  protected synchronized String readDesktopName() throws IOException {
    int nameLength = dataIn.readInt();
    byte[] stringBytes = new byte[nameLength];
    dataIn.read(stringBytes); //desktopName
    return new String(stringBytes, "UTF-8");
  }

//VNC Messages****************************************************************/

    /* Below are all of the methods the VNC Client can perform once connected.
     */

  /**
   * The SetPixelFormat message sets the format of the raw pixel data sent
   * across the network by the VNC Server. See RFB 3.8 standard for
   * SelPixelFormat message.
   *
   * @param bpp   The number of bits per pixel
   * @param Depth The number of bits used for data
   * @param be    The big endian flag
   * @param tcf   The true color flag
   * @param rm    The maximum red value
   * @param gm    The maximum blue value
   * @param bm    The maximum green value
   * @param rs    The red shift value
   * @param gs    The green shift value
   * @param bs    The blue shift value
   * @throws IOException If there is a socket error.
   */
  protected void setPixelFormat(int bpp, int Depth, int be, int tcf, int rm,
                                int gm, int bm, int rs, int gs, int bs) throws IOException {
    dataOut.writeByte(0); //message identifier
    dataOut.writeByte(0); //padding
    dataOut.writeByte(0); //padding
    dataOut.writeByte(0); //padding
    dataOut.writeByte(bpp);
    dataOut.writeByte(Depth);
    dataOut.writeByte(be);
    dataOut.writeByte(tcf);
    dataOut.writeShort(rm);
    dataOut.writeShort(gm);
    dataOut.writeShort(bm);
    dataOut.writeByte(rs);
    dataOut.writeByte(gs);
    dataOut.writeByte(bs);
    dataOut.writeByte(0); //padding
    dataOut.writeByte(0); //padding
    dataOut.writeByte(0); //padding
    bef = be;
    dataOut.flush();
  }

  /**
   * Sets the encoding of the pixel data sent by the server. See standard
   * for details of encoding type.
   *
   * @param numberOfEncodings The number of encodings the server supports
   * @param encoding          The value representing an encoding type.
   * @throws IOException If there is a socket error.
   */
  protected void setEncodings(short numberOfEncodings,
                              int... encoding) throws IOException {
    dataOut.writeByte(2); //message identifier
    dataOut.writeByte(0); //padding
    dataOut.writeShort(numberOfEncodings);
    for (int index : encoding) {
      dataOut.writeInt(index);
    }
    dataOut.flush();
  }

  /**
   * Sends FramebufferUpdateRequest message to server.
   *
   * @param incremental Zero sends entire desktop, One sends changes only.
   * @param x           X coordinate of desired region
   * @param y           Y coordinate of desired region
   * @param w           Width of desired region
   * @param h           Height of desired region
   * @throws IOException If there is a socket error
   */
  protected void framebufferUpdateRequest(boolean flag,
                                          int incremental, short x, short y,
                                          short w, short h) throws IOException {
    if (flag == true) {
      Debug.log(-1, "Error: SetPixelFormat Required.");
      return;
    }
    dataOut.writeByte(3); //message identifier
    dataOut.writeByte(incremental);
    dataOut.writeShort(x);
    dataOut.writeShort(y);
    dataOut.writeShort(w);
    dataOut.writeShort(h);
    dataOut.flush();
  }

  /**
   * Tells VNC server to depress key.
   *
   * @param key X Window System Keysym for key.
   * @throws IOException If there is a socket error.
   */
  protected void keyDown(int key) throws IOException {
    dataOut.writeByte(VNC_KEY_EVENT_MSG);
    dataOut.writeByte(VNC_KEY_EVENT_DOWN);
    dataOut.writeByte(0); //padding
    dataOut.writeByte(0); //padding
    dataOut.writeInt(key);
    dataOut.flush();
    Debug.log(4, "Writing key down-" + Integer.toHexString(key));
  }

  /**
   * Tells VNC server to release key.
   *
   * @param key X Window System Keysym for key.
   * @throws IOException If there is a socket error.
   */
  protected void keyUp(int key) throws IOException {
    dataOut.writeByte(VNC_KEY_EVENT_MSG); //message identifier
    dataOut.writeByte(VNC_KEY_EVENT_UP);
    dataOut.writeByte(0); //padding
    dataOut.writeByte(0); //padding
    dataOut.writeInt(key);
    dataOut.flush();
    Debug.log(4, "Writing key up-" + Integer.toHexString(key));
  }

  /**
   * Tells VNC server to perform a mouse event. bOne through bEight are mouse
   * buttons one through eight respectively.  A zero means release that
   * button, and a one means depress that button.
   *
   * @param buttonState logical or of BUTTON_N_DOWN
   * @param x      X coordinate of action
   * @param y      Y coordinate of action
   * @throws IOException If there is a socket error.
   */
  protected void mouseEvent(int buttonState, int x, int y) throws IOException {
    byte buttons = (byte) buttonState;

    dataOut.writeByte(VNC_POINTER_EVENT_MSG);
    dataOut.writeByte(buttons);
    dataOut.writeShort(x);
    dataOut.writeShort(y);
    dataOut.flush();
    Debug.log(4, "MouseEvent-" + Byte.toString(buttons));
  }

  /**
   * TODO: Method to cut and paste text over VNC connection with
   *
   * @param text
   * @throws IOException
   */
  protected void clientPasteText(String text) throws IOException {
    byte[] b = text.getBytes("ISO-8859-1");
    dataOut.writeByte(6);
    dataOut.writeByte(0);
    dataOut.writeByte(0);
    dataOut.writeByte(0);
    dataOut.writeInt(b.length);
    for (int i = 0; i < b.length; i++) {
      dataOut.writeByte(b[i]);
    }
    dataOut.flush();
  }

  protected void readFully(byte[] buffer) throws IOException
  {
    int remaining = buffer.length;
    int offset = 0;
    int bytesRead = 0;
    while(remaining > 0 && (bytesRead = dataIn.read(buffer, offset, remaining)) != -1) {
      remaining -= bytesRead;
      offset += bytesRead;
    }

    if (bytesRead == -1) {
      throw new EOFException();
    }
  }

  /**
   * Reads a single unsigned byte off of the wire
   *
   * @return int the unsigned byte
   * @throws IOException
   */
  protected synchronized int readByte() throws IOException {
    return dataIn.readUnsignedByte();
  }

  /**
   * Reads an unsigned short off of the wire
   *
   * @return int the unsigned short
   * @throws IOException
   */
  protected synchronized int readShort() throws IOException {
    return dataIn.readUnsignedShort();
  }

  /**
   * Reads an int off of the wire
   *
   * @return int the int
   * @throws IOException
   */
  protected synchronized int readInt() throws IOException {
    return dataIn.readInt();
  }

  protected synchronized String readLine() throws IOException
  {
    ByteArrayOutputStream tmp = new ByteArrayOutputStream();
    int byteRead;
    while((byteRead = in.read()) != -1) {
      if (byteRead == '\n') {
        break;
      }
      tmp.write(byteRead);
    }
    return new String(tmp.toByteArray(), ASCII);
  }

  protected synchronized void writeLine(String line) throws IOException
  {
    out.write(line.getBytes(ASCII));
    out.write('\n');
  }

  /**
   * Reads truecolor 32 bit data off of the wire
   *
   * @param length of the data
   * @return int[] the data
   * @throws IOException
   * @throws InterruptedException
   */
  protected synchronized int[] readTC32Data(int length) throws IOException, InterruptedException {
    int[] input = new int[length];
    if (bef > 0) { //big endian
      for (int j = 0; j < length; j += 3) {
        dataIn.readUnsignedByte();
        input[j] = dataIn.readUnsignedByte();
        input[j + 1] = dataIn.readUnsignedByte();
        input[j + 2] = dataIn.readUnsignedByte();
      }
    } else { //little endian
      for (int j = 0; j < length; j += 3) {
        input[j + 2] = dataIn.readUnsignedByte();
        input[j + 1] = dataIn.readUnsignedByte();
        input[j] = dataIn.readUnsignedByte();
        dataIn.readUnsignedByte();
      }
    }
    return input;
  }

  /**
   * Closes the connection
   *
   * @throws IOException
   */
  protected void close() throws IOException {
    socket.close();
  }

  /**
   * Returns true if there is data waiting to be read from the socket
   *
   * @return
   * @throws IOException
   */
  protected boolean available() throws IOException {
    if (dataIn.available() > 0) {
      return true;
    }
    return false;
  }

  /**
   * Returns the VNCClient Object as a string
   */
  public String toString() {
    return "VNCClient: Socket: " + socket.toString();
  }
}
