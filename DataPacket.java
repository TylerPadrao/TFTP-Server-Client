/**
   ISTE 121-02 Project - 12/5/20
   
   DataPacket
   @Author:       Chloe, Tyler, Austin
   @Description:  Builds and Dissects a DatagramPacket in WRQ Packet format.
   Dependencies:  ProjectClient.java, ProjectServer.java, Constants.java, WRQPacket, RRQPacket.java, ACKPacket.java, ERRORPacket.java, PacketChecker.java
**/

import java.io.*;
import java.net.*;
import javafx.scene.control.*;
import javafx.scene.control.Alert.*;
import javafx.application.Platform;

/**
   DataPacket: Builds and Dissects Data Packets
*/
public class DataPacket implements CONSTANTS
{
   private int opcode = DATA;
   private InetAddress address = null;
   private int port = 0;
   private int blockNo = 0;
   private byte[] data = new byte[512];
   private int dataLen = 0;

   // Default constructor for packet dissection
   public DataPacket(){}

   // Parameterized constructor for packet builder
   public DataPacket(InetAddress _address, int _port, int _blockNo, byte[] _data, int _dataLen)
   {
      opcode = DATA;
      address = _address;
      port = _port;
      blockNo = _blockNo;
      data = _data;
      dataLen = _dataLen;
   }
   
   // Accessors
   public int getOpcode() { return opcode; }
   public InetAddress getAddress() { return address; }
   public int getPort() { return port; }
   public int getBlockNo() { return blockNo; }
   public byte[] getData() { return data; }
   public int getDataLen() { return dataLen; }
   
   public DatagramPacket build() { 
      ByteArrayOutputStream baos = null;
      try
      {
         // Create the byte[] with the right data
         int len = 4 + data.length;
         baos = new ByteArrayOutputStream(len);
         DataOutputStream pdos = new DataOutputStream(baos);
         pdos.writeShort(opcode);
         pdos.writeShort(blockNo);
         if (dataLen > 0){
            pdos.write(data, 0, dataLen);
         }
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
      
      if(opcode != DATA)
      {
         Platform.runLater(new Runnable() {
            public void run() {
               Alert alert = new Alert(AlertType.ERROR);
               alert.setTitle("ERROR");
               alert.setHeaderText("Error Dissecting Packet");
               alert.setContentText("Opcode Mismatch, expected: DATA, given: " + opcode);
               alert.showAndWait();
            } 
         });
         return;
      } 
      
      blockNo = dis.readShort();
      dataLen = pkt.getLength() - 4;
      data = new byte[dataLen];
      int numRead = dis.read(data, 0, dataLen);
      
      dis.close();
   }
}