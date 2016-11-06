/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package chaton;

import java.io.Serializable;

/**
 * Message contenant un type et, en fonction de lui, un texte destiné à l'affichage ou non.
 * @author Auxane & Romain
 */
public class Message implements Serializable{
    protected static final long serialVersionUID = 42L;
    //constantes de type
    public static final int MESSAGE = 1,  WHOSTHERE = 2, LOGOUT = 3, CHANGEROOM = 4, CHANGENAME = 5, WHISPER = 6;
    private String message; //le message en lui-même
    private int type;   //type du message pour savoir quel traitement appliquer
        
    /**
     * Constructeur.
     * @param message le message à afficher ou non
     * @param type le type de message
     */
    public Message(String message, int type) {
        this.message = message;
        this.type = type;
    }

    /**
     * Getter pour le type.
     * @return le type
     */
    public int getType() {
        return type;
    }

    /**
     * Getter pour le message.
     * @return le message
     */
    public String getMessage() {
        return message;
    }

    @Override
    /**
     * Méthode toString pour le debug.
     */
    public String toString() {
        return "Message{" + "message=" + message + ", type=" + type + '}';
    }
    
}
