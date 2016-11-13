/*
* To change this license header, choose License Headers in Project Properties.
* To change this template file, choose Tools | Templates
* and open the template in the editor.
*/
package chaton;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Represents the server gathering all the connections. Events are displayed in the console.
 * @author Auxane & Romain
 */
public class Server {
    @SuppressWarnings("FieldMayBeFinal")
    private int port;
    private ServerSocket sSocket;
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
    private static final int DEFAULTPORT = 1200;
    private static final int NB_MAX_MESSAGE = 5;
    private boolean oneMoreTime;
    @SuppressWarnings("FieldMayBeFinal")
    private ArrayList<ClientThread> clientList;
    private static int id = 0;
    private static int nameNumber = 1;
    private ServerThread serverDeamon;
    //hashtable qui lie le nom d'un salon avec un tableau de string qui contient ses messages
    HashMap messagesHist = new HashMap();
    
    /**
     * Constructor.
     * @param port the port on which the server can be connected to.
     */
    public Server(int port){
        this.port = port;
        this.clientList = new ArrayList<>();
        this.load();
    }
    
    /**
     * Constructor. Uses the default port.
     */
    public Server(){
        this.port = DEFAULTPORT;
        this.clientList = new ArrayList<>();
        this.load();
    }
    
    /**
     * Start the server and loop waiting for connections from clients.
     */
    private void start(){
        try {
            sSocket = new ServerSocket(this.port);
            this.oneMoreTime = true;
            this.serverDeamon = new ServerThread(this);
            new Thread(this.serverDeamon).start();
            while(this.oneMoreTime){
                write("Waiting for connection on port " + this.port +".");
                Socket communication = sSocket.accept();
                if(this.oneMoreTime){
                    
                    ClientThread cThread = new ClientThread(communication);
                    // Add the client
                    this.clientList.add(cThread);
                    cThread.start();
                    // Tell everybody someone just joined
                    writeToEverybody(cThread.getClientName() + " has just joined the room.", cThread, cThread.getCurrentRoom());
                }
            }
            // TODO lancer la fermeture propre (Parcours des ClientThreads, fermeture des streams, Affichage de messages de déconnexion)
            this.save();
            System.out.println("Server ended");
            System.exit(0);
        } catch (IOException ex) {
            write("new ServerSocket error, port : " + this.port);
        }
    }
    
    private void save(){
        try
        {
            FileOutputStream fos = new FileOutputStream("save.ser");
            ObjectOutputStream oos = new ObjectOutputStream(fos);
            oos.writeObject(this.messagesHist);
            oos.close();
            fos.close();
            System.out.println("Messages saved in hashmap.ser");
        }
        catch(IOException ioe)
        {
            ioe.printStackTrace();
        }
    }
    
    private void load(){
        try
        {
            File myFile = new File("save.ser");
            myFile.createNewFile(); // if file already exists will do nothing 
            
            FileInputStream fis = new FileInputStream("save.ser"); 
            ObjectInputStream ois = new ObjectInputStream(fis);
            this.messagesHist = (HashMap) ois.readObject();
            ois.close();
            fis.close();
        }
        catch(IOException ioe)
        {
            System.out.println("Could not load messages from save.ser");
            return;
        }
        catch(ClassNotFoundException c)
        {
            System.out.println("Class not found");
            return;
        }
      System.out.println("Messages loaded");
    }
    
    /**
     * Writes a message to the server console.
     * @param txt the message to write.
     */
    private void write(String txt){
        System.out.println(DATE_FORMAT.format(new Date()) + " | " + txt);
    }
    
    
    /**
     * Broadcasts a message to all the clients.
     * @param txt the message to broadcast.
     */
    private synchronized void writeToEverybody(String txt, ClientThread client, String room){
        // TODO Add the message to the room arrayList
        if(!this.messagesHist.containsKey(room)){
            this.messagesHist.put(room, new ArrayList());
        }
        ((ArrayList)(this.messagesHist.get(room))).add(DATE_FORMAT.format(new Date()) + " | " + txt);
        
        while( ((ArrayList)(this.messagesHist.get(room))).size() > NB_MAX_MESSAGE){
            for(int i = 1; i < ((ArrayList)(this.messagesHist.get(room))).size(); i++){
                ((ArrayList)(this.messagesHist.get(room))).set(i-1, ((ArrayList)(this.messagesHist.get(room))).get(i));
            }
            ((ArrayList)(this.messagesHist.get(room))).remove(((ArrayList)(this.messagesHist.get(room))).size()-1);
        }
        // Reverse looping through the client threads so that disconnected clients do not get to miss another one.
        for(int i=this.clientList.size()-1; i >=0 ; i--){
            if(client != this.clientList.get(i)){
                if(this.clientList.get(i).getCurrentRoom().equals(room)){
                    if(!this.clientList.get(i).writeToUser(txt)){
                        write(this.clientList.get(i).getClientName() + " has left the chatOn.");
                        this.clientList.remove(i);
                    }
                }
            }
        }
    }
    
    /**
     * Removes a client thread from the client threads list, using its id.
     * @param id the id of the client to be removed.
     */
    private synchronized void removeClient(int id){
        for(int i=0; i<this.clientList.size() ; i++){
            if(this.clientList.get(i).getClientId()==id){
                this.clientList.remove(i);
                return;
            }
        }
        
        
    }
    
    public boolean exists(String name){
        for(int i=0; i<clientList.size() ; i++){
            if(clientList.get(i).getClientName().equals(name)){
                return true;
            }
        }
        return false;
    }
    
    
    private boolean whisper(String origin, String dest, String mess) {
        for(int i=0; i<clientList.size() ; i++){
            if(clientList.get(i).getClientName().equals(dest)){
                clientList.get(i).writeToUser("Private message from " + origin + " : " + mess);
                return true;
            }
        }
        return false;
    }
    
    /**
     * Lecture en continue des entrées de la console du serveur.
     */
    class ServerThread implements Runnable {
        Server s;
        public ServerThread(Server s){
            this.s = s;
        }
        @Override
        public void run(){
            Scanner sc = new Scanner(System.in);
            while(s.oneMoreTime){
                if(sc.nextLine().equals("OFF")){
                    s.oneMoreTime = false;
                    //Creation d'un nouveau client pour pouvoir quitter la boucle
                    Client ghostClient = new Client(port, "localhost");
                    ghostClient.start();
                }
            }
        }
    }
    
    /**
     * Tester
     * @param args
     */
    public static void main(String[] args) {
        int inputPort = 1500;
        switch(args.length){
            case 0 :
                break;
            case 1 :
                System.out.println("Unknown port " + inputPort);
                System.out.println("Waiting for 3 args as : server adress, port, username");
                try {
                    inputPort = Integer.parseInt(args[0]);
                }
                catch(Exception e){
                    System.out.println("Unknown port " + inputPort);
                    System.out.println("Waiting for 1 arg as : port");
                }
                break;
            default :
                System.out.println("Waiting for 1 arg as : port");
        }
        Server myServer = new Server(inputPort);
        myServer.start();
    }
    
    
    
    /**
     * A thread associated to a client. It will independently read and write messages to the client.
     */
    class ClientThread extends Thread{
        private Socket cSocket;
        private ObjectInputStream iStream;
        private ObjectOutputStream oStream;
        private String clientName;
        private int clientId;
        private String currentRoom;
        
        public String getCurrentRoom(){
            return this.currentRoom;
        }
        
        /**
         * Constructor.
         * @param mySocket the socket of the client that is associated to the thread.
         */
        public ClientThread(Socket mySocket){
            this.clientId = id;
            id++;
            
            this.cSocket= mySocket;
            
            try {
                this.oStream = new ObjectOutputStream(this.cSocket.getOutputStream());
                this.iStream = new ObjectInputStream(this.cSocket.getInputStream());
                try {
                    this.clientName = (String) iStream.readObject();
                    if(this.clientName.equals("Guest")){
                        this.clientName = "Guest" + Server.nameNumber;
                        this.oStream.writeObject("" + Server.nameNumber);
                        Server.nameNumber++;
                    }
                } catch (ClassNotFoundException ex) {
                    write("Could not get the client's name. Initialized to anonymous");
                    this.clientName = "Anonymous";
                }
                
                try {
                    this.currentRoom = ((String) iStream.readObject()).toLowerCase();
                } catch (ClassNotFoundException ex) {
                    write("Could not get the client's room. Initialized to default");
                    this.currentRoom = "default";
                }
                // TODO Récupérer et afficher les messages stockés dans le tableau du salon
                
                if(messagesHist.containsKey(this.currentRoom)){
                    for(int i = 0; i < ((ArrayList)(messagesHist.get(this.currentRoom))).size(); i++){
                        writeToUserWithoutDate( (String)(((ArrayList)(messagesHist.get(this.currentRoom))).get(i)) );
                    }
                }
                
                write(this.clientName + " has joined the " + this.currentRoom + " room.");
            } catch (IOException ex) {
                write("Client Streams initialization error.");
            }
        }
        
        /**
         * Getter for the client thread name.
         * @return the client thread name.
         */
        public String getClientName() {
            return clientName;
        }
        
        /**
         * Getter for the client thread id.
         * @return the client thread id.
         */
        public int getClientId() {
            return clientId;
        }
        
        public ArrayList<String> getUsersList(String room){
            ArrayList<String> roomUsers = new ArrayList<String>();
            for(int i=0; i<clientList.size() ; i++){
                if(clientList.get(i).getCurrentRoom().equals(room)){
                    roomUsers.add(clientList.get(i).clientName);
                }
            }
            return roomUsers;
        }
        
        
        @Override
        /**
         * Loop the input stream to listen to the messages the client sends.
         */
        public void run(){
            //Might not be here
            Message message;
            boolean oneMoreTime = true;
            while(oneMoreTime){
                try {
                    message = (Message) iStream.readObject();
                    switch(message.getType()){
                        case Message.WHOSTHERE :
                            String userList = "";
                            for (String userName : getUsersList(this.currentRoom)){
                                userList+=("\n\t" + userName);
                            }
                            writeToUser("Users in the " + this.currentRoom + " room :" + userList);
                            break;
                        case Message.LOGOUT :
                            // Close cSocket and everything
                            write(this.clientName + " just logged out.");
                            writeToEverybody(this.clientName + " just logged out.", this, this.currentRoom);
                            oneMoreTime = false;
                            this.close();
                            removeClient(this.clientId);
                            break;
                        case Message.MESSAGE :
                            write(this.clientName + " in room " + this.currentRoom + " : " + message.getMessage());
                            writeToEverybody(this.clientName + " : " + message.getMessage(), this, this.currentRoom);
                            writeToUser("You : " + message.getMessage());
                            break;
                        case Message.CHANGEROOM :
                            write(this.clientName + " moved from the " + this.currentRoom + " room to the " + message.getMessage() + " room.");
                            writeToEverybody(this.clientName + " left the " + this.currentRoom + ".", this, this.currentRoom);
                            this.currentRoom = message.getMessage();
                            // TODO Récupérer et afficher les messages stockés dans le tableau du salon
                            if(messagesHist.containsKey(this.currentRoom)){
                                for(int i = 0; i < ((ArrayList)(messagesHist.get(this.currentRoom))).size(); i++){
                                    writeToUserWithoutDate( (String)(((ArrayList)(messagesHist.get(this.currentRoom))).get(i)) );
                                }
                            }
                            writeToUser("You joined the " + message.getMessage() + " room.");
                            writeToEverybody(this.clientName + " joined the room.", this, this.currentRoom);
                            break;
                        case Message.CHANGENAME :
                            if(!exists(message.getMessage())){
                                write(this.clientName + " is now called " + message.getMessage() + ".");
                                writeToEverybody(this.clientName + " is now called " + message.getMessage() + ".", this, this.currentRoom);
                                this.clientName = message.getMessage();
                                writeToUser("You changed your name into " + message.getMessage() + ".");
                            }
                            else{
                                writeToUser(message.getMessage() + " is already used by somebody else.");
                            }
                            break;
                        case Message.WHISPER :
                            String dest = message.getMessage().split(" ",2)[0];
                            String mess = message.getMessage().split(" ",2)[1];
                            if(whisper(this.clientName, dest, mess)){
                                writeToUser("Private message to " + dest + " : " + mess);
                            } else {
                                writeToUser("There is nobody named " + dest + " in the chatOn.");
                            }
                            write(this.clientName + " whispers to " + dest + " : " + mess);
                            break;
                        default :
                            write("Wrong message type from user : " + this.clientName);
                            break;
                    }
                } catch (IOException ex) {
                    write("Could not read the message from the client : " + this.clientName + ". The connection is now closed.");
                    this.close();
                    oneMoreTime = false;
                } catch (ClassNotFoundException ex) {
                    write("Could not find the class reading the message from : " + this.clientName + ". The connection is now closed.");
                    this.close();
                    oneMoreTime = false;
                }
            }
        }
        
        /**
         * Writes a message to the client using the output stream.
         * @param txt the message to be sent
         * @return true if it is done, false if not.
         */
        private boolean writeToUser(String txt){
            // If the client is not connected anymore
            if(!this.cSocket.isConnected()){
                this.close();
                return false;
            }
            try {
                this.oStream.writeObject(DATE_FORMAT.format(new Date()) + " | " + txt);
            } catch (IOException ex) {
                write(this.clientName + " failed to send a message.");
            }
            return true;
        }
        
        /**
         * Writes a message to the client using the output stream.
         * @param txt the message to be sent
         * @return true if it is done, false if not.
         */
        private boolean writeToUserWithoutDate(String txt){
            // If the client is not connected anymore
            if(!this.cSocket.isConnected()){
                this.close();
                return false;
            }
            try {
                this.oStream.writeObject(txt);
            } catch (IOException ex) {
                write(this.clientName + " failed to send a message.");
            }
            return true;
        }
        
        /**
         * Closes the connection with the client.
         */
        private void close(){
            try {
                this.cSocket.close();
            } catch (IOException ex) {
                write("Failed to close the socket of : " + this.clientName + ". " + ex);
            }
            try {
                this.iStream.close();
            } catch (IOException ex) {
                write("Failed to close the input stream of : " + this.clientName);
            }
            try {
                this.oStream.close();
            } catch (IOException ex) {
                write("Failed to close the output stream of : " + this.clientName);
            }
        }
    }
    
}
