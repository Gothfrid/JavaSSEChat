/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.simplechat.entities;

/**
 *
 * @author bohdan
 */
public class Message {
    
    private String name;
    private String token;
    private String message;

    public Message() {
    }

    public Message(String name, String message, String token) {
        this.name = name;
        this.message = message;
        this.token = token;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }
    
    @Override
    public String toString() {
        return "[" + name + ": " + message + "]";
    }
    
}
