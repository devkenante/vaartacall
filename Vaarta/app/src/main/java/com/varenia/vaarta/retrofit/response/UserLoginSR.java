package com.varenia.vaarta.retrofit.response;

import com.google.gson.annotations.SerializedName;
import com.varenia.vaarta.models.Room;
import com.varenia.vaarta.models.UserId;

import java.util.ArrayList;

/**
 * Created by VCIMS-PC2 on 22-01-2018.
 */

public class UserLoginSR {

    @SerializedName("status")
    private int status;
    @SerializedName("msg")
    private String msg;
    @SerializedName("response")
    private ArrayList<Room> response;

    public ArrayList<Room> getResponse() {
        return response;
    }

    public void setResponse(ArrayList<Room> response) {
        this.response = response;
    }

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


}
