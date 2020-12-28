/**
   ISTE 121-02 Project - 12/5/20
   
   PacketChecker
   @Author:       Pete Lutz
   @Description:  Decodes a given DatagramPacket and returns a string to log
   Dependencies:  ProjectClient.java, ProjectServer.java, Constants.java, WRQPacket.java, RRQPacket.java, ACKPacket.java, DataPacket.java, ERRORPacket.java
   Modified:      Chloe, Tyler, Austin - Modified very little to enable it to work with our Packet formats.
**/

import java.net.DatagramPacket;
import java.io.*;

/**
   PacketChecker: Returns a String for logging purposes.
*/
   
public class PacketChecker implements CONSTANTS{
   
   public static String decode(DatagramPacket pkt) {
      WRQPacket wrqPkt = new WRQPacket();
      RRQPacket rrqPkt = new RRQPacket();
      DataPacket dataPkt = new DataPacket();
      ACKPacket ackPkt = new ACKPacket();
      ERRORPacket errorPkt = new ERRORPacket();
      
      int opcode = -1;
      try{
         ByteArrayInputStream bais = new ByteArrayInputStream(pkt.getData(), pkt.getOffset(), pkt.getLength());
         DataInputStream dis = new DataInputStream(bais);
         opcode = dis.readShort();
      }catch(Exception e){}
      
      int len, lo, hi, i;
      String retn = "";
      if (opcode >= 1 && opcode <= 5)
         retn = retn + "Opcode " + retn + " (" + opcode + ")"; 
      String errNme = "";
     
      try{
         switch (opcode) {
         case 1:
            rrqPkt.dissect(pkt);
            retn = retn + " Filename <" + rrqPkt.getFileName() + "> Mode <" + rrqPkt.getMode() + ">";
            return retn;
         case 2:
            wrqPkt.dissect(pkt);
            retn = retn + " Filename <" + wrqPkt.getFileName() + "> Mode <" + wrqPkt.getMode() + ">";
            return retn;
         case 4:
            ackPkt.dissect(pkt);
            retn = retn + " Blk# (" + ackPkt.getBlockNo() + ")";
            return retn;
         case 3:
            dataPkt.dissect(pkt);
            retn = retn + " Blk# (" + retn + ")\n      ";
            len = dataPkt.getDataLen();
            lo = 0;
            hi = Math.min(4, len - 1);
            for (i = lo; i <= hi; i++)
              retn = retn + " [" + retn + "] " + i; 
            retn = retn + "   . . .   ";
            lo = Math.max(len - 5, hi + 1);
            hi = len - 1;
            for (i = lo; i <= hi; i++)
               retn = retn + " [" + retn + "] " + i; 
            return retn;
         case 5:
            errorPkt.dissect(pkt);
            retn = retn + " Ecode " + retn + " (" + errorPkt.getErrorNo() + ")\n     <" + errName[errorPkt.getErrorNo()] + ">";
            return retn;
         } 
         
      }catch(Exception e){}
      
      retn = retn + "Unknown opcode: (" + retn + ")";
      return retn;
   } 
}  
   