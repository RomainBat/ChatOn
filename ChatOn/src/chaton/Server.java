/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package chaton;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
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
    private boolean oneMoreTime;
    @SuppressWarnings("FieldMayBeFinal")
    private ArrayList<ClientThread> clientList;
    private static int id = 0;
    
    /**
     * Constructor.
     * @param port the port on which the server can be connected to.
     */
    public Server(int port){
        this.port = port;
        this.clientList = new ArrayList<>();
    }
    
    /**
     * Constructor. Uses the default port.
     */
    public Server(){
        this.port = DEFAULTPORT;
        this.clientList = new ArrayList<>();
    }
    
    /**
     * Start the server and loop waiting for connections from clients.
     */
    private void start(){
        try {
            sSocket = new ServerSocket(this.port);
            
            oneMoreTime = true;
            while(oneMoreTime){
                Socket communication = sSocket.accept();
                write("waitig for connection on port " + this.port);
                ClientThread cThread = new ClientThread(communication);
                // Add the client
                this.clientList.add(cThread);
                cThread.start();
                // Tell everybody someone just joined
                writeToEverybody(cThread.getClientName() + " has just joined the ChatOn.", cThread);
            }
            
        } catch (IOException ex) {
            write("new ServerSocket error, port : " + this.port);
        }        
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
    private synchronized void writeToEverybody(String txt, ClientThread client){        
        // TODO
        // Does the server know about the conversations ?
        
        // Reverse looping through the client threads so that disconnected clients do not get to miss another one.
        for(int i=this.clientList.size()-1; i >=0 ; i--){
            if(client != this.clientList.get(i)){
                if(!this.clientList.get(i).writeToUser(txt)){
                    write(this.clientList.get(i).getClientName() + " has left the chatOn.");
                    this.clientList.remove(i);
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
    
    
    /**
     * Tester
     * @param args 
     */
    public static void main(String[] args) {
        int inputPort = 1001;
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
                } catch (ClassNotFoundException ex) {
                    write("Could not get the client's name. Initialized to anonymous");
                    this.clientName = "Anonymous";
                }
                write(this.clientName + " has join the ChatOn.");
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
                            //TODO
                            // Retourner la liste des clients présents
                            break;
                        case Message.LOGOUT :
                            // Close cSocket and everything mtf
                            write(this.clientName + " just logged out.");
                            oneMoreTime = false;
                            this.close();
                            removeClient(this.clientId);
                            break;
                        case Message.MESSAGE :
                            write(this.clientName + " : " + message.getMessage());
                            writeToEverybody(this.clientName + " : " + message.getMessage(), this);
                            writeToUser("You : " + message.getMessage());
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
                write("Failed to close the socket of : " + this.clientName);
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
