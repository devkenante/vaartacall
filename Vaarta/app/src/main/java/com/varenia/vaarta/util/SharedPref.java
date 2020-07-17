package com.varenia.vaarta.util;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by VCIMS-PC2 on 11-01-2018.
 */

public class SharedPref {

    public static SharedPref sharedPref;
    public Context context;

    private String SETTING_DB = Constants.APP_NAME;
    //Quickblox Details
    public Boolean LOGIN_STATUS;
    public String USER_ID;
    public int QB_ID;
    public String LOGIN;
    public String FULLNAME;
    public String PASSWORD;
    public String EMAIL;
    public String EXT_ID;
    public String FACEBOOK;
    public String TWITTER;
    public String TWITTER_DIGITS;
    public String TAGS;
    public String LAST_SIGN_IN;
    public String CREATED_AT;
    //Other Details
    public int USER_TYPE;
    public String SUBSCRIPTION_ID;
    public Boolean GROUP_EXPIRED;
    public String APP_ID;
    public String AUTH_KEY;
    public String AUTH_SECRET;
    public String ACCOUNT_KEY;
    public String API_DOMAIN;
    public String CHAT_DOMAIN;
    public String JANUS_SERVER;
    public String JANUS_PROTOCOL;
    public String JANUS_PLUGIN;
    public Boolean CALL_STARTED;
    public String PREF_LANGUAGE;

    public static SharedPref getInstance(){
        if(sharedPref == null || sharedPref.context == null){
            sharedPref = new SharedPref();

            if(sharedPref.context != null)
                sharedPref.loadSetting();
        }
        return sharedPref;
    }

    public static void createInstance(Context context){
        synchronized (SharedPref.class){
            if(sharedPref == null) {
                sharedPref = new SharedPref();
                sharedPref.context = context;
                sharedPref.loadSetting();
            }
        }
    }

    private void loadSetting(){
        if(this.context != null) {
            try{

                SharedPreferences pref = this.context.getSharedPreferences(SETTING_DB,Context.MODE_PRIVATE);
                this.LOGIN_STATUS = pref.getBoolean(Constants.LOGIN_STATUS,false);
                this.USER_ID = pref.getString(Constants.USER_ID,null);
                this.QB_ID = pref.getInt(Constants.QB_ID,0);
                this.LOGIN = pref.getString(Constants.LOGIN,null);
                this.FULLNAME = pref.getString(Constants.FULLNAME,null);
                this.PASSWORD = pref.getString(Constants.PASSWORD,null);
                this.EMAIL = pref.getString(Constants.EMAIL,null);
                this.EXT_ID = pref.getString(Constants.EXT_ID,null);
                this.FACEBOOK = pref.getString(Constants.FACEBOOK,null);
                this.TWITTER = pref.getString(Constants.TWITTER,null);
                this.TWITTER_DIGITS = pref.getString(Constants.TWITTER_DIGITS,null);
                this.TAGS = pref.getString(Constants.TAGS,null);
                this.LAST_SIGN_IN = pref.getString(Constants.LAST_SIGN_IN,null);
                this.CREATED_AT = pref.getString(Constants.CREATED_AT,null);
                this.USER_TYPE = pref.getInt(Constants.USER_TYPE,-1);
                this.SUBSCRIPTION_ID = pref.getString(Constants.SUBSCRIPTION_ID,null);
                this.APP_ID = pref.getString(Constants.APP_ID,null);
                this.AUTH_KEY = pref.getString(Constants.AUTH_KEY,null);
                this.AUTH_SECRET = pref.getString(Constants.AUTH_SECRET,null);
                this.ACCOUNT_KEY = pref.getString(Constants.ACCOUNT_KEY,null);
                this.API_DOMAIN = pref.getString(Constants.API_DOMAIN,null);
                this.CHAT_DOMAIN = pref.getString(Constants.CHAT_DOMAIN,null);
                this.JANUS_SERVER = pref.getString(Constants.JANUS_SERVER,null);
                this.JANUS_PROTOCOL = pref.getString(Constants.JANUS_PROTOCOL,null);
                this.JANUS_PLUGIN = pref.getString(Constants.JANUS_PLUGIN,null);
                this.CALL_STARTED = pref.getBoolean(Constants.CALL_STARTED, false);
                this.PREF_LANGUAGE = pref.getString(Constants.PREF_LANGUAGE, null);
            } catch (Exception e){
                e.printStackTrace();
            }
        }
    }

    public void clear() {
        SharedPreferences.Editor editor = this.context.getSharedPreferences(SETTING_DB, Context.MODE_PRIVATE).edit();
        editor.clear();
        editor.apply();
    }

    public boolean updateSetting(){

        SharedPreferences.Editor editor = null;
        if(this.context != null){
            try{
                editor = this.context.getSharedPreferences(SETTING_DB, Context.MODE_PRIVATE).edit();
                editor.putBoolean(Constants.LOGIN_STATUS,this.LOGIN_STATUS);
                editor.putString(Constants.USER_ID,this.USER_ID);
                editor.putInt(Constants.QB_ID,this.QB_ID);
                editor.putString(Constants.LOGIN,this.LOGIN);
                editor.putString(Constants.FULLNAME,this.FULLNAME);
                editor.putString(Constants.PASSWORD,this.PASSWORD);
                editor.putString(Constants.EMAIL,this.EMAIL);
                editor.putString(Constants.EXT_ID,this.EXT_ID);
                editor.putString(Constants.FACEBOOK,this.FACEBOOK);
                editor.putString(Constants.TWITTER,this.TWITTER);
                editor.putString(Constants.TWITTER_DIGITS,this.TWITTER_DIGITS);
                editor.putString(Constants.TAGS,this.TAGS);
                editor.putString(Constants.LAST_SIGN_IN,this.LAST_SIGN_IN);
                editor.putString(Constants.CREATED_AT,this.CREATED_AT);
                editor.putInt(Constants.USER_TYPE,this.USER_TYPE);
                editor.putString(Constants.SUBSCRIPTION_ID,this.SUBSCRIPTION_ID);
                editor.putString(Constants.APP_ID,this.APP_ID);
                editor.putString(Constants.AUTH_KEY,this.AUTH_KEY);
                editor.putString(Constants.AUTH_SECRET,this.AUTH_SECRET);
                editor.putString(Constants.ACCOUNT_KEY,this.ACCOUNT_KEY);
                editor.putString(Constants.API_DOMAIN,this.API_DOMAIN);
                editor.putString(Constants.CHAT_DOMAIN,this.CHAT_DOMAIN);
                editor.putString(Constants.JANUS_SERVER,this.JANUS_SERVER);
                editor.putString(Constants.JANUS_PROTOCOL,this.JANUS_PROTOCOL);
                editor.putString(Constants.JANUS_PLUGIN,this.JANUS_PLUGIN);
                editor.putBoolean(Constants.CALL_STARTED,this.CALL_STARTED);
                editor.putString(Constants.PREF_LANGUAGE,this.PREF_LANGUAGE);
                return editor.commit();
            }catch (Exception e){
                e.printStackTrace();
            }
        }
        return false;
    }

}
