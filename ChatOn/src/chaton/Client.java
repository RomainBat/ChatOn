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
 *
 * @author Aux
 */
public class Client {
    private ObjectInputStream iStream;
    private ObjectOutputStream oStream;
    private Socket cSocket;
    private int port;
    private String serverAddress, userName;

    public Client(int port, String serverAddress, String userName) {
        this.port = port;
        this.serverAddress = serverAddress;
        this.userName = userName;
    }
    
    public void start(){
        
    }
    
}
