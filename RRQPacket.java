/**
   ISTE 121-02 Project - 12/5/20
   
   RRQPacket
   @Author:       Chloe, Tyler, Austin
   @Description:  Builds and Dissects a DatagramPacket in RRQ Packet format.
   Dependencies:  ProjectClient.java, ProjectServer.java, Constants.java, WRQPacket.java, ACKPacket.java, DataPacket.java, ERRORPacket.java, PacketChecker.java
**/

import java.io.*;
import java.net.*;
import javafx.scene.control.*;
import javafx.scene.control.Alert.*;

/**
   RRQPacket: Builds and Dissecets RRQPackets.
*/
public class RRQPacket implements CONSTANTS
{
   private int opcode = RRQ;
   private InetAddress address = null;
   private int port = 0;
   private String filename = "";
   private String mode = "";

   // Default constructor for packet dissection
   public RRQPacket(){}

   // Parameterized constructor for packet builder
   public RRQPacket(InetAddress _address, int _port, String _filename, String _mode)
   {
      opcode = RRQ;
      address = _address;
      port = _port;
      filename = _filename;
      mode = _mode;
   }
   
   // Accessors
   public int getOpcode() { return opcode; }
   public InetAddress getAddress() { return address; }
   public int getPort() { return port; }
   public String getFileName() { return filename; }
   public String getMode() { return mode; }
   
   public DatagramPacket build() { 
      ByteArrayOutputStream baos = null;
      try
      {
         // Create the byte[] with the right data
         baos = new ByteArrayOutputStream();
         DataOutputStream pdos = new DataOutputStream(baos);
         pdos.writeShort(opcode);
         pdos.writeBytes(filename);
         pdos.writeByte(0);
         pdos.writeBytes("octet");
         pdos.writeByte(0);
         pdos.close();
      }
      catch(Exception e)
      {
         Alert alert = new Alert(AlertType.ERROR);
         alert.setTitle("ERROR");
         alert.setHeaderText("Error Building Packet");
         alert.setContentText("Exception found while building packet: " + e);
         alert.showAndWait();
      }
      
      // Build the pakcet
      byte[] holder = baos.toByteArray();
      DatagramPacket pkt = new DatagramPacket(
         holder, holder.length, getAddress(), getPort());
      return pkt;
   }
   
   // Dissect a packet
   public void dissect(DatagramPacket pkt) throws Exception
   {
      // Get the address, port, and byte[] from the packet
      address = pkt.getAddress();
      port = pkt.getPort();
      ByteArrayInputStream bais = new ByteArrayInputStream(
         pkt.getData(), pkt.getOffset(), pkt.getLength());
      
      // Get the parts from the byte[]  
      DataInputStream dis = new DataInputStream(bais);   
      opcode = dis.readShort();
      
      if(opcode != RRQ)
      {
         Alert alert = new Alert(AlertType.ERROR);
         alert.setTitle("ERROR");
         alert.setHeaderText("Error Dissecting Packet");
         alert.setContentText("Opcode Mismatch, expected: RRQ, given: " + opcode);
         alert.showAndWait(); 
         return;
      } 
      
      filename = readToZ(dis);
      mode = readToZ(dis);
      dis.close();
   }
   
   // Read until we get a 0 byte
   public static String readToZ(DataInputStream dis) throws Exception
   {
      String s = "";
      
      while(true)
      {
         byte b = dis.readByte();
         if(b == 0)
         {
            return s;
         }
         s += (char) b;
      }
   }
}