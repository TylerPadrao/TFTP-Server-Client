/**
   ISTE 121-02 Project - 12/5/20
   
   ACKPacket
   @Author:       Chloe, Tyler, Austin
   @Description:  Decodes a given DatagramPacket and returns a string to log
   Dependencies:  ProjectClient.java, ProjectServer.java, Constants.java, WRQPacket.java, RRQPacket.java, DataPacket.java, ERRORPacket.java, PacketChecker.java
**/

import java.io.*;
import java.net.*;
import javafx.scene.control.*;
import javafx.scene.control.Alert.*;
import javafx.application.Platform;

/**
   ACKPacket: Builds and Dissects a ACK Packet
*/
public class ACKPacket implements CONSTANTS
{
   private int opcode = ACK;
   private InetAddress address = null;
   private int port = 0;
   private int blockNo = 0;

   // Default constructor for packet dissection
   public ACKPacket(){}

   // Parameterized constructor for packet builder
   public ACKPacket(InetAddress _address, int _port, int _blockNo)
   {
      opcode = ACK;
      address = _address;
      port = _port;
      blockNo = _blockNo;
   }
   
   // Accessors
   public int getOpcode() { return opcode; }
   public InetAddress getAddress() { return address; }
   public int getPort() { return port; }
   public int getBlockNo() { return blockNo; }
   
   public DatagramPacket build() { 
      ByteArrayOutputStream baos = null;
      try
      {
         // Create the byte[] with the right data
         int len = 4;
         baos = new ByteArrayOutputStream(len);
         DataOutputStream pdos = new DataOutputStream(baos);
         pdos.writeShort(opcode);
         pdos.writeShort(blockNo);
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
      
      if(opcode != ACK)
      {
         Platform.runLater(new Runnable() {
            public void run() {
               Alert alert = new Alert(AlertType.ERROR);
               alert.setTitle("ERROR");
               alert.setHeaderText("Error Dissecting Packet");
               alert.setContentText("Opcode Mismatch, expected: ACK, given: " + opcode);
               alert.showAndWait();
            }
         });
         return;
      } 
      
      blockNo = dis.readShort();     
      dis.close();
   }
}