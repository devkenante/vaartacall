package com.varenia.vaarta.models;

public class ChatHistory {

    int currentUser;
    int oppUser;
    String message;

    public int getCurrentUser() {
        return currentUser;
    }

    public void setCurrentUser(int currentUser) {
        this.currentUser = currentUser;
    }

    public int getOppUser() {
        return oppUser;
    }

    public void setOppUser(int oppUser) {
        this.oppUser = oppUser;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
