package com.varenia.vaarta.models;

import com.google.gson.annotations.SerializedName;

public class Room {
    @SerializedName("room_id")
    String room_id;

    public String getRoom_id() {
        return room_id;
    }

    public void setRoom_id(String room_id) {
        this.room_id = room_id;
    }
}
