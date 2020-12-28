/**
   ISTE 121-02 Project - 12/5/20
   
   ERRORPacket
   @Author:       Chloe, Tyler, Austin
   @Description:  Builds and Dissects a DatagramPacket in WRQ Packet format.
   Dependencies:  ProjectClient.java, ProjectServer.java, Constants.java, WRQPacket.java, RRQPacket.java, ACKPacket.java, DataPacket.java, PacketChecker.java
**/

import java.io.*;
import java.net.*;
import javafx.scene.control.*;
import javafx.scene.control.Alert.*;
import javafx.application.Platform;

/**
   ERRORPacket: Builds and Dissects Error Packets
*/
public class ERRORPacket implements CONSTANTS
{
   private int opcode = ERROR;
   private InetAddress address = null;
   private int port = 0;
   private int errorNo = 0;
   private String errorMsg = "";

   // Default constructor for packet dissection
   public ERRORPacket(){}

   // Parameterized constructor for packet builder
   public ERRORPacket(InetAddress _address, int _port, int _errorNo, String _errorMsg)
   {
      opcode = ERROR;
      address = _address;
      port = _port;
      errorNo = _errorNo;
      errorMsg = _errorMsg;
   }
   
   // Accessors
   public int getOpcode() { return opcode; }
   public InetAddress getAddress() { return address; }
   public int getPort() { return port; }
   public int getErrorNo() { return errorNo; }
   public String getErrorMsg() { return errorMsg; }
   
   public DatagramPacket build() { 
      ByteArrayOutputStream baos = null;
      try
      {
         // Create the byte[] with the right data
         int len = 5 + errorMsg.length();
         baos = new ByteArrayOutputStream(len);
         DataOutputStream pdos = new DataOutputStream(baos);
         pdos.writeShort(opcode);
         pdos.writeShort(errorNo);
         pdos.writeBytes(errorMsg);
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
      
      // Build the packet
      byte[] holder = baos.toByteArray();
      DatagramPacket pkt = new DatagramPacket(
         holder, 0, holder.length, getAddress(), getPort());
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
      
      if(opcode != ERROR)
      {
         Platform.runLater(new Runnable() {
            public void run() {
               Alert alert = new Alert(AlertType.ERROR);
               alert.setTitle("ERROR");
               alert.setHeaderText("Error Dissecting Packet");
               alert.setContentText("Opcode Mismatch, expected: ERROR, given: " + opcode);
               alert.showAndWait(); 
            }
         }); 
         return;
      } 
      
      errorNo = dis.readShort();
      errorMsg = readToZ(dis);     
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