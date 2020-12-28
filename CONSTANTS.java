/**
   ISTE 121-02 Project - 12/5/20
   
   COONSTANTS
   @Author:       Chloe, Tyler, Austin
   @Description:  Interface for Constants used throughout the Project
   Dependencies:  ProjectServer.java, ProjectClient.java, WRQPacket.java, RRQPacket.java, ACKPacket.java, DataPacket.java, ERRORPacket.java, PacketChecker.java
**/

//Interface for Constants
public interface CONSTANTS {
   
   //Networking
   public static final int portNumber = 69;
   public static final int MAX_PACKET = 1500;
   
   //Error Codes
   public static final int UNDEF = 0;
   public static final int NOTFD = 1;
   public static final int ACCESS = 2;
   public static final int ILLOP = 4;
   
   //Error Codes not Implemented
   public static final int DSKFUL = 3;
   public static final int UNKID = 5;
   public static final int FILEX = 6;
   public static final int NOUSR = 7;
   
   //Opcode Codes
   public static final int RRQ = 1;
   public static final int WRQ = 2;
   public static final int DATA = 3;
   public static final int ACK = 4;
   public static final int ERROR = 5;
   
   //Opcode Strings
   public static final String[] opName = new String[] {"", "(RRQ)", "(WRQ)", "(DATA)", "(ACK)", "(ERROR)" };

   //Error Strings
   public static final String[] errName = new String[] { "UNDEF", "NOTFND", "ACCESS", "DISKFULL", "ILLOP", "UNKID", "FILEEX", "NOUSER" };

}