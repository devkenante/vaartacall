package com.varenia.vaarta.models;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

/**
 * Created by VCIMS-PC2 on 04-01-2018.
 */

public class KenanteUser implements Serializable {

    @SerializedName("uid")
    private int kid;
    @SerializedName("user_type")
    private int user_type;
    @SerializedName("lu_password")
    private String password;
    @SerializedName("login")
    private String login;
    @SerializedName("name")
    private String name;
    @SerializedName("dname")
    private String dname;
    @SerializedName("email")
    private String email;
    @SerializedName("tags")
    private String roomName;
    @SerializedName("created_at")
    private String created_at;
    @SerializedName("last_sign_in")
    private String last_sign_in;
    @SerializedName("audio_codec")
    private String audioCodec;
    @SerializedName("video_code")
    private String videoCodec;
    @SerializedName("recording")
    private Boolean recording;
    @SerializedName("recording_dir")
    private String recording_dir;
    @SerializedName("bitrate")
    private String bitrate;
    @SerializedName("room_id")
    private int room_id;

    public int getRoom_id() {
        return room_id;
    }

    public void setRoom_id(int room_id) {
        this.room_id = room_id;
    }

    public int getKid() {
        return kid;
    }

    public void setKid(int kid) {
        this.kid = kid;
    }

    public int getUser_type() {
        return user_type;
    }

    public void setUser_type(int user_type) {
        this.user_type = user_type;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDname() {
        return dname;
    }

    public void setDname(String dname) {
        this.dname = dname;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getRoomName() {
        return roomName;
    }

    public void setRoomName(String roomName) {
        this.roomName = roomName;
    }

    public String getCreated_at() {
        return created_at;
    }

    public void setCreated_at(String created_at) {
        this.created_at = created_at;
    }

    public String getLast_sign_in() {
        return last_sign_in;
    }

    public void setLast_sign_in(String last_sign_in) {
        this.last_sign_in = last_sign_in;
    }

    public String getAudioCodec() {
        return audioCodec;
    }

    public void setAudioCodec(String audioCodec) {
        this.audioCodec = audioCodec;
    }

    public String getVideoCodec() {
        return videoCodec;
    }

    public void setVideoCodec(String videoCodec) {
        this.videoCodec = videoCodec;
    }

    public Boolean getRecording() {
        return recording;
    }

    public void setRecording(Boolean recording) {
        this.recording = recording;
    }

    public String getRecording_dir() {
        return recording_dir;
    }

    public void setRecording_dir(String recording_dir) {
        this.recording_dir = recording_dir;
    }

    public String getBitrate() {
        return bitrate;
    }

    public void setBitrate(String bitrate) {
        this.bitrate = bitrate;
    }
}
