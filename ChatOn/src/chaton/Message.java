/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package chaton;

/**
 *
 * @author asus
 */
public class Message {
    //constantes de type
    static final int MESSAGE = 1,  WHOSTHERE = 2, LOGOUT = 3;
    String message; //le message en lui-même
    int type;   //type du message pour savoir quel traitement appliquer

    /**
     * Constructor
     * @param message
     * @param type 
     */
    public Message(String message, int type) {
        this.message = message;
        this.type = type;
    }

    public int getType() {
        return type;
    }

    public String getMessage() {
        return message;
    }
}
