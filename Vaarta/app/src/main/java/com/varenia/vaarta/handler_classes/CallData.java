package com.varenia.vaarta.handler_classes;

import android.content.Context;

import java.util.ArrayList;
import java.util.HashMap;



public class CallData {

    private static CallData callData;
    private Context context;
    private int currentUserType = -1;
    private int currentUserId = -1;
    private String currentUserName = "";
    private ArrayList<Integer> chatUsers = new ArrayList<>();
    //private ArrayList<Integer> usersToSubsribe = new ArrayList<>();
    //private HashMap<Integer, String> longNames = new HashMap<>();
    //private HashMap<Integer, String> opponentsPrivateDialogs = new HashMap<>();
    //private HashMap<Integer, Integer> allUsersType = new HashMap<>();
    private String roomName = "";
    //private HashMap<String, String> audioVideoStatus = new HashMap<>();
    //private HashMap<String, Boolean> chatDialogIdsHistoryStatus = new HashMap<>();

    public CallData(Context context) {
        this.context = context;
    }

    public static CallData getInstance(Context context) {
        if (callData == null) {
            callData = new CallData(context);
        }
        return callData;
    }

    public void removeAllElements(){
        callData.getChatUsers().clear();
    }

    public Context getContext() {
        return context;
    }

    public void setContext(Context context) {
        this.context = context;
    }

    public int getCurrentUserType() {
        return currentUserType;
    }

    public void setCurrentUserType(int currentUserType) {
        this.currentUserType = currentUserType;
    }

    public int getCurrentUserId() {
        return currentUserId;
    }

    public void setCurrentUserId(int currentUserId) {
        this.currentUserId = currentUserId;
    }

    public String getCurrentUserName() {
        return currentUserName;
    }

    public void setCurrentUserName(String currentUserName) {
        this.currentUserName = currentUserName;
    }

    public ArrayList<Integer> getChatUsers() {
        return chatUsers;
    }

    public void setChatUsers(ArrayList<Integer> chatUsers) {
        this.chatUsers.addAll(chatUsers);
    }



    public String getRoomName() {
        return roomName;
    }

    public void setRoomName(String roomName) {
        this.roomName = roomName;
    }

}
