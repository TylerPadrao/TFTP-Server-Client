/**
 * ISTE 121-02 Project - 12/5/20

 * ProjectServer
 * @Author:       Chloe, Tyler, Austin
 * @Description:  ProjectServer connects to ProjectClient and is able to upload and download files.
 * Dependencies:  ProjectClient.java, Constants.java, WRQPacket.java, RRQPacket.java, ACKPacket.java, DataPacket.java, ERRORPacket.java, PacketChecker.java
**/

import javafx.application.*;
import javafx.event.*;
import javafx.scene.*;
import javafx.scene.control.*;
import javafx.scene.control.Alert.*;
import javafx.scene.text.*;
import javafx.scene.layout.*;
import javafx.stage.*;
import javafx.geometry.*;

import java.net.*;
import java.io.*;
import java.util.*;
import java.net.SocketTimeoutException;
 
/**
   ProjectServer: Connects to ProjectClient and allows for data transfer of a .txt file.
 */
public class ProjectServer extends Application implements CONSTANTS {

   // Window attributes
   private Stage stage;
   private Scene scene;
   private VBox root; 
   final int INIT_X = 900;
   final int INIT_Y = 100;
   
   // GUI Components
   private Button btnChooseFolder = new Button("Choose Folder");
   private TextField tfFolderName = new TextField();
   private Label lblStartStop = new Label("Start the server: "); 
   private Button btnStartStop = new Button("Start");
   private TextArea taData = new TextArea();
   
   // Server attributes
   private DirectoryChooser directoryChooser;
   DatagramSocket serverSocket = null;  
   
   // Packet objects for referencing from outside classes 
   RRQPacket rrq = new RRQPacket();
   WRQPacket wrq = new WRQPacket();
   ERRORPacket error = new ERRORPacket();
   ACKPacket ack = new ACKPacket();
   DataPacket data = new DataPacket();
  
   /**
    * main program
    */
   public static void main(String[] args) {
      launch(args);
   }
   
   public void start(Stage _stage) {
      // Set stage
      stage = _stage;
      // Set title og server GUI
      stage.setTitle("Project TFTP Server");
      // Terminate program when window is closed
      stage.setOnCloseRequest(new EventHandler<WindowEvent>() {
         public void handle(WindowEvent evt) { System.exit(0); }
      } );
      root = new VBox(8);
      
      // Create initial file
      File initial = new File("....");
      // Set font of folder name text field
      tfFolderName.setFont(Font.font(
         "MONOSPACED", FontWeight.NORMAL, tfFolderName.getFont().getSize()));
      // Set folder path to absolute path 
      tfFolderName.setText(initial.getAbsolutePath());
      tfFolderName.setPrefColumnCount(tfFolderName.getText().length());
      // Create ScrollPane
      ScrollPane sp = new ScrollPane();
      sp.setContent(tfFolderName);
      sp.setMaxWidth(300);
      
      // Set height of text area
      taData.setPrefHeight(550);
      
      // Create HBox for scrollPane and choose folder button
      HBox top = new HBox(8);
      top.getChildren().addAll(sp, btnChooseFolder);
      
      // Create HBox for middle components
      HBox middle = new HBox(8);
      btnStartStop.setStyle("-fx-background-color: #00ff00;");
      middle.getChildren().addAll(lblStartStop, btnStartStop);
      
      // Set padding of root
      root.setPadding(new Insets(10));
      // Add everything to root
      root.getChildren().addAll(top, middle, taData);
      
      // Show window
      stage.setX(INIT_X);
      stage.setY(INIT_Y);
      scene = new Scene(root, 425, 550);
      stage.setScene(scene);
      stage.show();   
      
      // Set btnChooseFolder on action
      btnChooseFolder.setOnAction(new EventHandler<ActionEvent>() {
         public void handle(ActionEvent aevt) {
            doChoose(); // Call doChoose
         }
      });
      
      // Set btnStartStop on action
      btnStartStop.setOnAction(new EventHandler<ActionEvent>() {
         public void handle(ActionEvent aevt) {
            doBtnStartStop(); // Call doBtnStartStop
         }
      });
   }
   
   /**
    * doChoose
    * A method to open up the directory chooser and have the user select
    * and choose a folder for uploads or downloads
    */
   public void doChoose() {
      // Create directory chooser
      directoryChooser = new DirectoryChooser();
      // Set initial directory
      directoryChooser.setInitialDirectory(new File(tfFolderName.getText()));
      // Set title
      directoryChooser.setTitle("Select Folder for Upload/Downloads");
      // Open directoryChooser
      File selectedDirectory = directoryChooser.showDialog(stage);
      // Set text for tfFolderName text field
      tfFolderName.setText(selectedDirectory.getAbsolutePath());
   }
   
   /**
    * doBtnStartStop
    * A method to control the start and stop buttons
    */
   public void doBtnStartStop(){
      switch(btnStartStop.getText()) {
         case "Start":
            // If Start button is selected:
            btnStartStop.setText("Stop");
            lblStartStop.setText("Stop the server: ");
            btnStartStop.setStyle("-fx-background-color: #ff0000;");
            tfFolderName.setDisable(true);
            btnChooseFolder.setDisable(true);
            
            // Create and start listening thread
            Thread listenerThread = new ListenerThread();
            listenerThread.start();
            break;
         case "Stop":
            // If Stop button is selected:
            btnStartStop.setText("Start");
            lblStartStop.setText("Start the server: ");
            btnStartStop.setStyle("-fx-background-color: #00ff00;");
            tfFolderName.setDisable(false);
            btnChooseFolder.setDisable(false);
            
            // If server socket is not null
            if(serverSocket != null) {
               try{
                  serverSocket.close();
               } catch(Exception e) {}
                  serverSocket = null;
            }
      } // Switch
   }//doBtnStartStop
   
   /**
    * toLog
    * A method to log messages to the server log text area 
    * String msg: A parameter that takes a String message
    */
   public void toLog(String msg) {
         Platform.runLater(new Runnable() {
            public void run() {
               taData.appendText(msg + "\n");
            }
         });
   }
   
   // An inner class for listening and waiting for the server to receive a packet
   class ListenerThread extends Thread {
      public void run() {
         try{
            // Log that listener thread is started
            toLog("ListenerThread.run...Listener thread started");
            // Create server DatagramSocket
            serverSocket = new DatagramSocket(portNumber);
            
            // While true
            while (true){
               // Create DatagramPacket
               DatagramPacket packet = new DatagramPacket(new byte[MAX_PACKET], MAX_PACKET);
               // Have server receive packet
               serverSocket.receive(packet);
               // Log that server received packet
               toLog("ListenerThread.run...Received a packet!");
               // Create and start ClientThread
               Thread clientThread = new ClientThread(packet);
               clientThread.start();
            } // while
         // try 
         } catch (Exception e) {
            toLog("ListenerThread.run...Exception Found:\n" +e);
            return;
         } // catch
      } // run
   
   }//ListenerThread
   
   // An inner class that extends thread and implements the CONSTANTS class
   class ClientThread extends Thread implements CONSTANTS {
      // Datagram attributes 
      private DatagramPacket packet;
      private DatagramSocket cSocket;
      private int opcode;
   
      /**
       * Constructor
       * DatagramPakcet _packet : initializes packet 
       */
      public ClientThread(DatagramPacket _packet) {
         packet = _packet;
      }
      
      public void run() {
         try{
            // Create Client Socket
            cSocket = new DatagramSocket();
            // Set timeout
            cSocket.setSoTimeout(1000);
         } catch (Exception e) {
            toLog("ClientThread.run...Exception creating socket:\n" + e);
            return;
         }
      
         try{
            // Create ByteArrayInputStream
            ByteArrayInputStream bais = new ByteArrayInputStream(packet.getData(), packet.getOffset(), packet.getLength());
            DataInputStream dis = new DataInputStream(bais);
            opcode = dis.readShort();
            // Create Error packet
            ERRORPacket errPacket;
            
            switch(opcode) { 
               case 1:
                  // Dissect RRQ packet
                  rrq.dissect(packet);
                  // Log rrq packet
                  toLog("ClientThread.run...RRQ <" + rrq.getFileName() + "> mode <" + rrq.getMode() +">");
                  // Call doRRQ
                  doRRQ(rrq, cSocket);
                  break; 
               case 2:
                  // Dissect WRQ packet
                  wrq.dissect(packet);
                  // Log wrq packet
                  toLog("ClientThread.run...WRQ <" + wrq.getFileName()+ "> mode <" + wrq.getMode() +">");
                  // doWrq
                  doWRQ(wrq, cSocket);
                  break;
               case 5:
                  // Dissect ERROR packet
                  error.dissect(packet);
                  // Log error packet
                  toLog("ERROR Message: " + error.getErrorMsg());
                  // Create error packet
                  errPacket = new ERRORPacket(packet.getAddress(), packet.getPort(), 4, "Unexpected opcode: " + opcode);
                  // Send client build error packet
                  cSocket.send(errPacket.build());
                  break; 
               default:
                  // Log illegal opcode
                  toLog("ClientThread.run...Illegal Opcode: " + opcode);
                  // create error packet
                  errPacket = new ERRORPacket(packet.getAddress(), packet.getPort(), 4, "Illegal opcode: " + opcode);
                  // send built error packet
                  cSocket.send(errPacket.build());
                  break;
            } // Switch
         } catch(Exception e) {
            toLog("ClientThread.run...Exception Found:\n" +e);
         }
      }
   
   }//ClientThread
  
   /**
    * doRRQ
    * RRQPacket rrqPacket : An RRQ packet
    * DatagramSocket cSocket : A DatagramSocket for client
    */
   public void doRRQ(RRQPacket rrqPacket, DatagramSocket cSocket) {
      // Variables for building and dissecting packets
      int blockNo = 1;
      int lastBlock = 512;
      int readSize = 0;
      byte[] block = new byte[512];
      
      // Variables for filename and full file name
      String fileName = rrq.getFileName();
      String fullFileN = tfFolderName.getText() + File.separator;
      // Create a file input stream
      FileInputStream fis = null;
      // Create Data input stream
      DataInputStream dis = null;
      // Create error packet
      ERRORPacket errPacket;
      
      // While size is equal to size 512
      while(lastBlock == 512) {
         try {
            if(dis == null) { 
               // Log RRQ packet is being opened
               toLog("doRRQ...Opening: " + fullFileN);
               try {
                  // Initialize FileInputStream
                  fis = new FileInputStream(new File(fileName)); // fixed
                  // Initialize DataInputStream
                  dis = new DataInputStream(fis);    
               } catch(IOException ioe) {
                  // Create and send error packet
                  errPacket = new ERRORPacket(rrq.getAddress(), rrq.getPort(), 2, "Cannot Open File: " +rrq.getFileName());
                  DatagramPacket errorPacket = errPacket.build();
                  error.dissect(errorPacket);
                  
                  toLog("Server sending... " + PacketChecker.decode(errorPacket));
                  // Send built error packet
                  cSocket.send(errPacket.build());
                  return;
               } // catch         
            } // if
            
            // read size
            readSize = dis.read(block);
            // Create RRQ DataPacket
            DataPacket dataPacket = new DataPacket(rrq.getAddress(), rrq.getPort(), blockNo, block, readSize);
            // Build RRQ packet
            DatagramPacket dPacket = dataPacket.build();
            // Log that it is sending
            toLog("Server sending... " + PacketChecker.decode(dPacket));
            // Send packet
            cSocket.send(dPacket); 
            // Update readSize 
            lastBlock = readSize;
            // Send max packet
            DatagramPacket dpkt = new DatagramPacket(new byte[MAX_PACKET], MAX_PACKET);
            cSocket.receive(dpkt);
            // Log packet checker
            toLog("Server received... " + PacketChecker.decode(dpkt));

            // set opcode to ack opcode 4 
            int opcode = data.getOpcode();  // had ack
            if(opcode == 4) {
               // If opcode is 4, dissect ack packet
               ack.dissect(dpkt);
            } 
            else if(opcode != 3) {
               // Build and send error packet if error occurs
               errPacket = new ERRORPacket(dpkt.getAddress(), dpkt.getPort(), 0, "Illegal Opcode (" + opcode + ") expected 4 --Discarded");
               DatagramPacket errorPacket = errPacket.build();
               cSocket.send(errorPacket);
               return;
            }
            // increment block number
            blockNo++;
            
         } catch (EOFException eofe) {
            readSize = 0;
         } catch (SocketTimeoutException ste){
            toLog("Download timed out!");
            return;
         } catch(Exception e){
            toLog("doRRQ...Exception found during RRQ:\n" + e);
            return;
         }
      
      } // While
      toLog("doRRQ... File " + rrq.getFileName() + " downloaded.");
      try {
         // Try and close input stream and socket
         dis.close();
         cSocket.close();
      } catch(Exception e) { }
   } // doRRQ
   
   /**
    * doWRQ
    * WRQPacket wrqPacket : An WRQ packet
    * DatagramSocket cSocket : A DatagramSocket for client
    */
   private void doWRQ(WRQPacket wrqPacket, DatagramSocket cSocket) {
      // Variables for building and dissecting packets
      int blockNo = 1;
      int lastBlock = 512;
      int ackBlockNo = 0;
      
      // Variables for filename and full file name
      String fileName = wrq.getFileName();
      String fullFileN = tfFolderName.getText() + File.separator;
      // Create a file input stream
      FileOutputStream fos = null;
      DataOutputStream dos = null;
      // Create error packet
      ERRORPacket errPacket;
      
      try{
         // Log that opening WRQ packet
         toLog("doWRQ... Opening... " + fullFileN + "\n");
         // Initialize FileOutputStream for filename
         fos = new FileOutputStream(new File(fileName)); // got rid of new File(fullFileN)
         // Initialize DataOutputStream for FileOutputStream
         dos = new DataOutputStream(fos); // debugger doesn't get here...
      }catch(IOException ioe){
         // Create and send error packet
         errPacket = new ERRORPacket(wrqPacket.getAddress(), wrqPacket.getPort(), 2, "Cannot open File: " + wrqPacket.getFileName());
         DatagramPacket errpkt = errPacket.build();
         toLog("Sending... " + PacketChecker.decode(errpkt));
         try{
            // Send error packet
            cSocket.send(errpkt);
         }catch(IOException ioe2){ }
            return;
      }
      try{
         // While true
         while(true) {
            // Create ACK packet
            ACKPacket ackPacket = new ACKPacket(wrq.getAddress(), wrq.getPort(), ackBlockNo);
            // Used for testing purposes
            System.out.println("" + wrq.getAddress() + "\t" + wrq.getPort());
            // Build ack packet
            DatagramPacket dPacket = ackPacket.build();
            // Log that server is sending ack packet
            toLog("Server sending... " + PacketChecker.decode(dPacket));
            // Send packet
            cSocket.send(dPacket);  
            
            // If last block is less than 512
            if(lastBlock < 512) {
               break;
            }
            // Create Datagram packet with max bytes
            DatagramPacket dpkt = new DatagramPacket(new byte[MAX_PACKET], MAX_PACKET);
            // receive packet
            cSocket.receive(dpkt);
            // Log server received packet
            toLog("Server received... " + PacketChecker.decode(dpkt));
            // Dissect data packet
            data.dissect(dpkt);

            // Get data opcode
            int opcode = data.getOpcode();
            if(opcode == 5){
               return;
            }else if(opcode !=3){
               // Build error packet
               errPacket = new ERRORPacket(dpkt.getAddress(), dpkt.getPort(), 0, "Illegal Opcode (" + opcode + ") expected 3 --Discarded");
               DatagramPacket errorPacket = errPacket.build();
               cSocket.send(errorPacket);
               return;
            }
            
            // Increment block number
            blockNo++;
            // Update ack block number
            ackBlockNo = ack.getBlockNo();
            // Write data to DataOutputStream
            dos.write(data.getData(), 0, data.getDataLen());
            // Flush DataOutputStream
            dos.flush();
            // Set last block to length of data
            lastBlock = data.getDataLen();
          }  
         }catch (SocketTimeoutException ste){
            toLog("Upload timed out!");
            return;
         }catch(Exception e){
            toLog("doWRQ...Exception found during WRQ:\n" + e);
            return;
         }
      
      // Log that File was uploaded
      toLog("doWRQ... File " + rrq.getFileName() + " uploaded.");
      try{
         // Try and close DataOutputStreama and client socket
         dos.close();
         cSocket.close();
      }catch(Exception e){ }
      
   }//DoWRQ
} // ProjectServer