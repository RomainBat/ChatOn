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
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;
/**
 *
 * @author Aux
 */
public class Client {
    private ObjectInputStream iStream;
    private ObjectOutputStream oStream;
    private Socket cSocket;
    private int port;
    private String serverAddress, userName;
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");

    public Client(int port, String serverAddress, String userName) {
        this.port = port;
        this.serverAddress = serverAddress;
        this.userName = userName;
    }
    
    public boolean start(){
        try {
            this.cSocket = new Socket(this.serverAddress, this.port);            
        } catch (IOException ex) {
            write("Error : could not create socket on port " + this.port + " at adresse " + this.serverAddress);
        }
        
        try {
            iStream = new ObjectInputStream(this.cSocket.getInputStream());
            this.oStream = new ObjectOutputStream(this.cSocket.getOutputStream());
        } catch (IOException ex) {
            write("Error : could not create streams ");
        }
        
        new ServerListener().start();
        
        try {
            this.oStream.writeObject(this.userName);
        } catch (IOException ex) {
            write("Error : could not login");
            disconnect();
            return false;
        }
        return true;
    }
    
    public void disconnect(){
        try {
            this.iStream.close();
        } catch (IOException ex) {
            write("Error closing input stream");
        }
        try {
            this.oStream.close();
        } catch (IOException ex) {
            write("Error closing output stream");
        }
        try {
            this.cSocket.close();
        } catch (IOException ex) {
            write("Error closing client socket");
        }
    }
    
    /**
     * Writes a message to the server console.
     * @param txt the message to write.
     */
    private void write(String txt){
        System.out.println(DATE_FORMAT.format(new Date()) + " -> " + txt);
    }
    
    private void sendMessage(Message msg){
        try {
            this.oStream.writeObject(msg);
        } catch (IOException ex) {
            write("Could not send a message");
        }
    }
    
    public static void main(String[] args) {
        int inputPort = 1001;
        String inputAdress = "localhost";
        String inputName = "Guest";
        
        switch(args.length){
            case 0 :
                break;
            case 1 :
                inputAdress = args[0];
                break;
            case 2 :
                inputAdress = args[0];
                try{
                  inputPort = Integer.parseInt(args[1]);
                }
                catch(Exception e){
                    System.out.println("Unknown port " + inputPort);
                    System.out.println("Waiting for 3 args as : server adress, port, username");
                    return;
                }
                break;
            case 3 :
                inputAdress = args[0];
                try{
                  inputPort = Integer.parseInt(args[1]);
                }
                catch(Exception e){
                    System.out.println("Unknown port " + inputPort);
                    System.out.println("Waiting for 3 args as : server adress, port, username");
                    return;
                }
                inputName = args[2];
                break;
            default :
                System.out.println("Waiting for 3 args as : server adress, port, username");
                return;
        }
        
        Client myClient = new Client(inputPort, inputAdress, inputName);
        
        if(!myClient.start())
            return;
        else{
            Scanner sc = new Scanner(System.in);
            boolean oneMoreTime = true;
            while(oneMoreTime){
                System.out.println("> ");
                
                String s = sc.nextLine();
                
                if(s.equalsIgnoreCase("LOGOUT")){
                    myClient.sendMessage(new Message("", Message.LOGOUT));
                    oneMoreTime = false;
                }
                else if(s.equalsIgnoreCase("WHOSTHERE")){
                    myClient.sendMessage(new Message("", Message.WHOSTHERE));
                }
                else{
                    myClient.sendMessage(new Message(s, Message.MESSAGE));
                    
                }
            }
            
            myClient.disconnect();
        }
    }
    
    class ServerListener extends Thread{
        
        public void run(){
            while(true){
                try {
                    String message = (String) iStream.readObject();
                    
                    write(message);
                } catch (IOException ex) {
                    write("Connection closed by the server");
                } catch (ClassNotFoundException ex) {
                    write("Unknown error reading the server message");
                }
            }
        }
    }
}
