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


/**
 *
 * @author asus
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
    
    /**
     * 
     * @param port 
     */
    public Server(int port){
        this.port = port;
        this.clientList = new ArrayList<>();
    }
    
    /**
     * 
     */
    public Server(){
        this.port = DEFAULTPORT;
        this.clientList = new ArrayList<>();
    }
    
    /**
     * 
     */
    private void start(){
        try {
            sSocket = new ServerSocket(port);
            
            oneMoreTime = true;
            while(oneMoreTime){
                Socket communication = sSocket.accept();
                ClientThread cThread = new ClientThread(communication);
                // Add the client
                this.clientList.add(cThread);
                // Tell everybody someone just joined
                writeToEverybody(cThread.getClientName() + " has just joined the ChatOn.");
                write(cThread.getClientName() + " has joined the ChatOn.");
                
            }
            
        } catch (IOException ex) {
            write("new ServerSocket error, port : " + port);
        }        
    }
    
    /**
     * Write a message on the Server console.
     * @param txt 
     */
    private void write(String txt){
        System.out.println(DATE_FORMAT.format(new Date()) + " -> " + txt);
    }
    
    
    /**
     * Broadcast a message to all the clients
     * @param txt 
     */
    private void writeToEverybody(String txt){
        // TODO
        // Does the server know about the conversations ?
        
        // Reverse looping through the client threads so that disconnected clients do not get to miss another one.
        for(int i=this.clientList.size(); i >=0 ; i--){
            if(!this.clientList.get(i).writeToUser(txt)){
                write(this.clientList.get(i).getClientName() + " has left the chatOn.");
                this.clientList.remove(i);
            }
        }
    }
    
    
    /**
     * 
     * @param args 
     */
    public static void main(String[] args) {
        Server myServer = new Server(0);
        myServer.start();
    }
    
    /**
     * 
     */
    class ClientThread extends Thread{
        private Socket cSocket;
        private ObjectInputStream iStream;
        private ObjectOutputStream oStream;
        private String clientName;
        
        
        public ClientThread(Socket mySocket){
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

        public String getClientName() {
            return clientName;
        }
        
        @Override
        public void run(){
            //Might not be here
            Message message;
            boolean oneMoreTime = true;
            while(oneMoreTime){
                try {
                    message = (Message) iStream.readObject();
                    switch(message.getType()){
                        case Message.WHOSTHERE :
                            // Retourner la liste des clients présents
                            break;
                        case Message.LOGOUT :
                            // Close cSocket and everything mtf
                            this.cSocket.close();
                            write(this.clientName + " just logged out.");
                            break;
                        case Message.MESSAGE :
                            writeToEverybody(message.getMessage());
                            break;
                        default :
                            write("Wrong message type from user : " + this.clientName);
                            break;
                    }
                } catch (IOException ex) {
                    write("Could not read the message from the client : " + this.clientName);
                } catch (ClassNotFoundException ex) {
                    write("Could not find the class reading the message from : " + this.clientName);
                }
            }
        }
        
        
        private boolean writeToUser(String txt){
            // If the client is not connected anymore
            if(!this.cSocket.isConnected()){
                this.close();
                return false;
            }
            try {
                this.oStream.writeObject(txt);
            } catch (IOException ex) {
                write(this.clientName + " faile to send a message.");
            }
            return true;
        }
        
        private void close(){
            
        }
    }
}
