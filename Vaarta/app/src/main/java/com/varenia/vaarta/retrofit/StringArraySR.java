package com.varenia.vaarta.retrofit;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Created by VCIMS-PC2 on 26-09-2017.
 */

public class StringArraySR implements Serializable{

    //Here we declare variables which we will get in response from the server.

    @SerializedName("response")
    ArrayList<String> response;
    @SerializedName("status")
    int status;
    @SerializedName("msg")
    String msg;

    public ArrayList<String> getResponse() {
        return response;
    }

    public void setResponse(ArrayList<String> response) {
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
