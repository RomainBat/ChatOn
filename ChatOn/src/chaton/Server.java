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
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author asus
 */
public class Server {
    private int port;
    private ServerSocket sSocket;
    private SimpleDateFormat dateFormat;
    private static final int DEFAULTPORT = 1200;
    private boolean oneMoreTime;
    
    /**
     * 
     * @param port 
     */
    public Server(int port){
        this.port = port;
        this.dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
    }
    
    /**
     * 
     */
    public Server(){
        this.port = DEFAULTPORT;
        this.dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
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
            }
            
        } catch (IOException ex) {
            write("new ServerSocket error, port : " + port);
        }        
    }
    
    /**
     * 
     * @param txt 
     */
    private void write(String txt){
        System.out.println(dateFormat.format(new Date()) + " -> " + txt);
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
        String clientName;
        
        
        public ClientThread(Socket mySocket){
            this.cSocket= mySocket;
            
            try {
                this.oStream = new ObjectOutputStream(this.cSocket.getOutputStream());
                this.iStream = new ObjectInputStream(this.cSocket.getInputStream());
            } catch (IOException ex) {
                write("Client Streams initialization error");
            }
        }
        
       // public run(){
            //todo
        //}
    }
}
