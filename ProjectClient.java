/**
   ISTE 121-02 Project - 12/5/20
   
   ProjectClient
   @Author:       Chloe, Tyler, Austin
   @Description:  Client connects to a server and allows upload/download of .txt files.
   Dependencies:  ProjectServer.java, Constants.java, WRQPacket.java, RRQPacket.java, ACKPacket.java, DataPacket.java, ERRORPacket.java, PacketChecker.java
**/

import javafx.application.*;
import javafx.event.*;
import javafx.scene.*;
import javafx.scene.control.*;
import javafx.scene.control.Alert.*;
import javafx.scene.layout.*;
import javafx.scene.text.*;
import javafx.stage.*;
import javafx.geometry.*;

import java.io.*;
import java.net.*;
import java.util.*;
import java.net.SocketTimeoutException;

/**
   ProjectClient: Connects to ProjectServer and allows for data transfer of a .txt file.
*/
public class ProjectClient extends Application{
   
   // GUI Attributes
   private VBox root;
   private Scene scene;
   private Stage stage;
   
   private HBox flFolderBox;
   private HBox buttons;
   private HBox serverIP;
   private HBox flBox;
   private HBox bBox;
   
   private Button upld = new Button("Upload");
   private Button dwnld = new Button("Download");
   private Button chgFolder = new Button("Choose Folder");
   
   private TextField tfServer = new TextField();
   private TextField tfFolder = new TextField();
   
   private Label lbServer = new Label("Server: ");
   private Label lbLog = new Label("Log: ");
   
   private ScrollPane sp;
   private TextArea txtArea = new TextArea();

   // Dir/File Chooser
   private FileChooser flChooser;
   private DirectoryChooser directoryChooser;
   
   // Packet Classes Objects 
   RRQPacket rrq = new RRQPacket();
   WRQPacket wrq = new WRQPacket();
   ERRORPacket error = new ERRORPacket();
   ACKPacket ack = new ACKPacket();
   DataPacket data = new DataPacket();
   
   // Main
   public static void main(String[] args) {
      launch(args);
      
   }//void main
   
   // Starts GUI
   public void start(Stage _stage) {

      // GUI  Componets
      stage = _stage;
      root = new VBox(8);
      flFolderBox = new HBox(10);
      buttons = new HBox(10);
      serverIP = new HBox(10);
      flBox = new HBox(10);
      bBox = new HBox(10);
      
      // GUI Opening Location Constants
      final int INIT_X = 50;
      final int INIT_Y = 150;
      
      stage.setTitle("Project Client");
      
      // Setting the Scrollbar
      tfFolder.setFont(Font.font("MONOSPACED", FontWeight.NORMAL, tfFolder.getFont().getSize()));
      File initial = new File(".");
      sp = new ScrollPane();
      sp.setHbarPolicy(ScrollPane.ScrollBarPolicy.ALWAYS);
      sp.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
      sp.setContent(tfFolder);
      tfFolder.setText(initial.getAbsolutePath());
      tfFolder.setPrefColumnCount(tfFolder.getText().length());
      tfFolder.setEditable(false);
      
      
      // Add Spacings
      txtArea.setPrefWidth(580);
      txtArea.setPrefHeight(275);
      chgFolder.setPrefWidth(125);

      // Adding Children to Root
      flBox.getChildren().addAll(tfFolder, sp);
      serverIP.getChildren().addAll(lbServer, tfServer);
      flFolderBox.getChildren().addAll(flBox, chgFolder);
      buttons.getChildren().addAll(upld, dwnld);
      root.getChildren().addAll(serverIP, flFolderBox, buttons, lbLog, txtArea);
      
      // Setting Padding 
      root.setPadding(new Insets(10));
      
      // Setting the Size and Showing Stage
      scene = new Scene(root, 600, 400);
      stage.setScene(scene);
      stage.setX(INIT_X);
      stage.setY(INIT_Y);
      stage.show();    
      
      // EventHandler For Buttons
      chgFolder.setOnAction(new EventHandler<ActionEvent>() {
         public void handle(ActionEvent aevt) {
            doChoose();
         }
      });
      
      upld.setOnAction(new EventHandler<ActionEvent>() {
         public void handle(ActionEvent aevt) {
            doUpload();
         }
      });
      
      dwnld.setOnAction(new EventHandler<ActionEvent>() {
         public void handle(ActionEvent aevt) {
            doDownload();
         }
      });         
   }//Start 
   
   // Create Alerts for Error Reporting.
   private void doAlert(AlertType aType, String title, String header, String message) {
      Alert alert = new Alert(aType);
      alert.setTitle(title);
      alert.setHeaderText(header);
      alert.setContentText(message);
      alert.showAndWait();
   }
   
   // Create Logging Method
   public void toLog(String msg) {
         Platform.runLater(new Runnable() {
            public void run() {
               txtArea.appendText(msg + "\n");
            }
         });
   }
   
   // Directory Chooser for Choosing Folder Directory
   public void doChoose() {
      directoryChooser = new DirectoryChooser();
      directoryChooser.setInitialDirectory(new File(tfFolder.getText()));
      directoryChooser.setTitle("Select Folder for Upload/Downloads");
      File selectedDirectory = directoryChooser.showDialog(stage);
      tfFolder.setText(selectedDirectory.getAbsolutePath());
   }
   
   // Starts Uploading Process; Allows User to Select File to Upload.   
   public void doUpload() {
   
      // Select File for Uploading
      String remoteName;
      flChooser = new FileChooser();
      flChooser.setInitialDirectory(new File(tfFolder.getText()));
      flChooser.setTitle("Select File to Upload");
      File selectedFile = flChooser.showOpenDialog(stage);
      
      // Checks that File is Selected
      if(selectedFile == null){
         doAlert(AlertType.ERROR, "ERROR", "File Not Selected!", "Please Select a File");
         return;
      }
      
      // Rename File for Remote Server
      TextInputDialog textDialog = new TextInputDialog();
      textDialog.setHeaderText("Please enter a remote file name to save to the server");
      textDialog.setTitle("Remote File Name");
      textDialog.showAndWait();
      remoteName = textDialog.getEditor().getText();
      
      // Create and Start a clientThread Thread.
      Thread clientThread = new Upload(selectedFile, remoteName);
      clientThread.start();
   }
   
   // Upload Thread Class
   class Upload extends Thread implements CONSTANTS{
   
      // Networking Constants
      DatagramSocket socket = null;
      File localFile = null;
      String remoteName = null;
      InetAddress serverAddress = null;
      int port = -1;
      
      // Class Constructor for Passing File Names.
      public Upload(File _localFile, String _remoteName) {
         this.localFile = _localFile;
         this.remoteName = _remoteName;
         
         // Create Socket for Server Connection
         try{
            serverAddress = InetAddress.getByName(tfServer.getText());
            socket = new DatagramSocket();
            socket.setSoTimeout(10000);
         }catch(Exception e){
            toLog("Problem Uploading: " + e);
            e.printStackTrace();
            System.exit(1);
         }
      }//Upload constructor
      
      // Runs Thread.
      public void run() {
         int blockNum = 0;
         int blockEnd = 512;
         DataInputStream dis = null;
         byte[] block = new byte[512];
         int readSize = 0;
      
         //  Start Uploading; Sends Write Request Packet to Server
         toLog("Start Uploading " + localFile.getName() + " as " + remoteName); 
         try {
            WRQPacket packet = new WRQPacket(serverAddress, portNumber, remoteName, "octet");
            toLog("Client sending... Opcode " + packet.getOpcode() + " " + opName[packet.getOpcode()] + " FileName <" + packet.getFileName() + "> Mode <" + packet.getMode() + ">"); 
            socket.send(packet.build());
            
            // Waits for ACK Packet From Server
            while (true){
               
               // Handles Incoming Packet and Dissects It.
               DatagramPacket incomingPacket = new DatagramPacket(new byte[MAX_PACKET], MAX_PACKET);
               socket.receive(incomingPacket);
               toLog("Client received... " + PacketChecker.decode(incomingPacket) + "\n");
               ack.dissect(incomingPacket);
               if(ack.getOpcode() != ACK) {
                  ERRORPacket errorPkt = new ERRORPacket(serverAddress, portNumber, 5, "Invalid Opcode ... expected 4");
                  DatagramPacket errPkt = errorPkt.build();
                  toLog("Client received... " + PacketChecker.decode(errPkt) + "\n");
                  socket.send(errPkt);
                  return;
               } 
               serverAddress = ack.getAddress();
               port = ack.getPort();
               
               // Block Number Incrementation for Data Packets
               if (blockEnd < 512) {
                  break;
               }
               blockNum++;
               
               // Opens File in DataInputStream to Write.
               if(dis == null) {
                  String fullFileName = localFile.getAbsolutePath();
                  toLog("doWRQ...Opening" + fullFileName);
                  try {
                     FileInputStream fis = new FileInputStream(fullFileName);
                     dis = new DataInputStream(fis);
                     
                  } catch(IOException ioe) {
                     toLog("doWRQ...Cannot open file..." + localFile.getName());
                     ERRORPacket errorPkt = new ERRORPacket(serverAddress, port, 5, "Cannot open file");
                     socket.send(errorPkt.build());
                     return;
                  }
               
               }
               
               try {
                  readSize = dis.read(block);
               } catch(EOFException eofe) {
                  readSize = 0;
               }
               
               //Sends Data to Server
               DataPacket dataPkt = new DataPacket(serverAddress, port, blockNum, block, readSize);
               DatagramPacket dPkt = dataPkt.build();
               toLog("Client sending..." + PacketChecker.decode(dPkt) + "\n");
               socket.send(dataPkt.build());
               blockEnd = readSize;
            }
            socket.close();
            toLog("Uploaded " + localFile.getName() + " --> " + remoteName);
         }
         catch(SocketTimeoutException stee) {
            toLog("Uploading timed out!");
            return;
         }catch(Exception e) {
            toLog("doWRQ...Exception during WRQ" + e);
         }
         
      }//run
  
   }//Upload Class
   
   // Starts Download Process
   public void doDownload() {
   
      // Input File to Download
      TextInputDialog textDialog = new TextInputDialog();
      textDialog.setHeaderText("Please enter a remote file name to download from the server");
      textDialog.setTitle("Remote File Name");
      textDialog.showAndWait();
      String remoteName;
      remoteName = textDialog.getEditor().getText();
      
      flChooser = new FileChooser();
      flChooser.setInitialDirectory(new File(tfFolder.getText()));
      flChooser.setTitle("Select File to Download");
      File selectedFile = flChooser.showSaveDialog(stage);
      
      // Checks if File is Selected
      if(selectedFile == null){
         doAlert(AlertType.ERROR, "ERROR", "File Not Selected!", "Please Select a File");
         return;
      }
      
      // Create and Start downloadThread Thread
      Thread downloadThread = new Download(selectedFile, remoteName);
      downloadThread.start();
   }
   
   // Download Thread Class
   class Download extends Thread implements CONSTANTS{
      
      // Networking Attributes
      DatagramSocket socket = null;
      File localFile = null;
      String remoteName = null;
      InetAddress serverAddress = null;
      
      //Class Constructor for Passing File Names
      public Download(File _localFile, String _remoteName) {
         this.localFile = _localFile;
         this.remoteName = _remoteName;
         
         //Create Socket for Server Connection
         try{
            serverAddress = InetAddress.getByName(tfServer.getText());
            socket = new DatagramSocket();
            socket.setSoTimeout(1000);
         }catch(Exception e){
            toLog("Problem Downloading: " + e);
            System.exit(1);
         }
      }// Download Constructor
      
      // Runs Thread
      public void run() {
         int expectedBlockNum = 1;
         int blockEnd = 512;
         DataOutputStream dos = null;
         byte[] block = new byte[512];
         int readSize = 0;
         int port = 0;
         
         //Starts to Download; Sends Read Request Packet.
         toLog("Start Downloading " + localFile.getName() + " as " + remoteName); 
         try {
            RRQPacket packet = new RRQPacket(serverAddress, portNumber, remoteName, "octet");
            toLog("Client sending... Opcode " + packet.getOpcode() + " " + opName[packet.getOpcode()] + " FileName <" + packet.getFileName() + "> Mode <" + packet.getMode() + ">"); 
            socket.send(packet.build());
            
            // Reads Data from Server.
            while(blockEnd == 512){
                
               // Handles Incoming Data Packets  
               DatagramPacket incomingPacket = new DatagramPacket(new byte[MAX_PACKET], MAX_PACKET);
               socket.receive(incomingPacket);
               toLog("Client received... packet"); // + PacketChecker.decode(incomingPacket) + "\n");
               data.dissect(incomingPacket);
               port = data.getPort();
               if(data.getOpcode() != DATA) {
                  ERRORPacket errorPkt = new ERRORPacket(serverAddress, portNumber, 5, "Invalid Opcode ... expected 4");
                  DatagramPacket errPkt = errorPkt.build();
                  toLog("Client received... " + PacketChecker.decode(errPkt) + "\n");
                  socket.send(errPkt);
                  return;
               } 
               expectedBlockNum++;
               
               // Opens File in DataOutputStream
               if(dos == null) {
                  String fullFileName = localFile.getAbsolutePath();
                  toLog("Opening: " + fullFileName);
                  try {
                     FileOutputStream fos = new FileOutputStream(fullFileName);
                     dos = new DataOutputStream(fos);
                     
                  } catch(IOException ioe) {
                     toLog("doRRQ...Cannot open file..." + localFile.getName());
                     ERRORPacket errorPkt = new ERRORPacket(serverAddress, portNumber, 5, "Cannot open file");
                     socket.send(errorPkt.build());
                     return;
                  }
               }
            
               // Writes the Data
               dos.write(data.getData(), 0, data.getDataLen());
               dos.flush();
               ACKPacket ackPkt = new ACKPacket(serverAddress, port, data.getBlockNo());
               DatagramPacket aPkt = ackPkt.build();
               socket.send(ackPkt.build());
               toLog("Client sending..." + PacketChecker.decode(aPkt) + "(MArk1)\n"); 
               if(data.getDataLen() != 512) break;
            }
            dos.close();
     
            toLog("Downloaded " + remoteName + " --> " + localFile);
         }
         catch(SocketTimeoutException stee) {
            toLog("Downloading timed out!");
            return;
         }catch(Exception e) {
            toLog("doRRQ...Exception during RRQ" + e);
         }
         
      }//run
  
   }//Download Class
   
}//Main Class
