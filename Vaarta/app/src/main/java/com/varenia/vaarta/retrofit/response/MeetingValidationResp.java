package com.varenia.vaarta.retrofit.response;

import com.google.gson.annotations.SerializedName;
import com.varenia.vaarta.models.Room;

import java.util.ArrayList;

public class MeetingValidationResp {
    @SerializedName("status")
    private int status;
    @SerializedName("msg")
    private String msg;
    @SerializedName("response")
    private ArrayList<Room> response;

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public ArrayList<Room> getResponse() {
        return response;
    }

    public void setResponse(ArrayList<Room> response) {
        this.response = response;
    }
}
