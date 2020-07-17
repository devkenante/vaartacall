package com.varenia.vaarta.util

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import com.varenia.vaarta.models.KenanteUser

object NewSharedPref {

    private val TAG = SharedPref::class.java.simpleName
    private const val settingsDb = Constants.APP_NAME
    private var sharedPreferences: SharedPreferences? = null

    private fun createSharedPreferences(context: Context){
        sharedPreferences = context.getSharedPreferences(settingsDb, Context.MODE_PRIVATE)
    }

    @Synchronized
    fun GetSharedPreferences(context: Context) : SharedPreferences {
        if(sharedPreferences == null){
            createSharedPreferences(context)
        }
        return sharedPreferences!!
    }

    fun setValue(key: String, value: Any){

        if(isSharedPrefNull())
            return

        when(value){
            is Int -> {
                sharedPreferences!!.edit().putInt(key, value).apply()
            }
            is String -> {
                if(value!=null)
                sharedPreferences!!.edit().putString(key, value).apply()else  sharedPreferences!!.edit().putString(key, "NA").apply()
            }
            is Boolean -> {
                sharedPreferences!!.edit().putBoolean(key, value).apply()
            }
            is Long ->{
                sharedPreferences!!.edit().putLong(key, value).apply()
            }
        }

    }

    fun setValueButNotApply(key: String, value: Any){

        if(isSharedPrefNull())
            return

        when(value){
            is Int -> {
                sharedPreferences!!.edit().putInt(key, value)
            }
            is String -> {
                if(value!=null)
                    sharedPreferences!!.edit().putString(key, value)else  sharedPreferences!!.edit().putString(key, "NA")
            }
            is Boolean -> {
                sharedPreferences!!.edit().putBoolean(key, value)
            }
        }

    }

    fun getIntValue(key: String) : Int?{
        if(isSharedPrefNull())
            return null

        return sharedPreferences!!.getInt(key, 0)
    }

    fun getStringValue(key: String) : String?{
        if(isSharedPrefNull())
            return null

        return sharedPreferences!!.getString(key, "")
    }

    fun getLongValue(key: String) : Long?{
        if(isSharedPrefNull())
            return null

        return sharedPreferences!!.getLong(key, 0L)
    }

    fun getBooleanValue(key: String) : Boolean?{
        if(isSharedPrefNull())
            return null

        return sharedPreferences!!.getBoolean(key, false)
    }

    fun isSharedPrefNull() : Boolean{
        if(sharedPreferences==null) {
            Log.e(TAG, "Shared Preferences null, not yet created.")
            return true
        }
        return false
    }
    
    fun storeCurrentUser(user: KenanteUser){
        setValue(Constants.USER_TYPE, user.user_type)
        setValue(Constants.PASSWORD, user.password)
        setValue(Constants.LOGIN, user.login)
        setValue(Constants.NAME, user.name)
        setValue(Constants.DNAME, user.dname)
        setValue(Constants.EMAIL, user.email)
        setValue(Constants.AUDIO_CODEC, user.audioCodec)
        setValue(Constants.VIDEO_CODEC, user.videoCodec)
        setValue(Constants.RECORDING, user.recording)
        setValue(Constants.RECORDING_DIR, user.recording_dir)
        setValue(Constants.BITRATE, user.bitrate)
        setValue(Constants.ROOM,user.roomName)
    }

    fun getCurrentUser() : KenanteUser {
        val user = KenanteUser()

        user.kid = getStringValue(Constants.KID)?.toInt()!!
        user.user_type = getIntValue(Constants.USER_TYPE)!!
        user.password = getStringValue(Constants.PASSWORD)
        user.login = getStringValue(Constants.LOGIN)
        user.name = getStringValue(Constants.NAME)
        user.dname = getStringValue(Constants.DNAME)
        user.audioCodec = getStringValue(Constants.AUDIO_CODEC)
        user.videoCodec = getStringValue(Constants.VIDEO_CODEC)
        user.recording = getBooleanValue(Constants.RECORDING)
        user.recording_dir = getStringValue(Constants.RECORDING_DIR)
        user.bitrate = getStringValue(Constants.BITRATE)

        return user
    }
    
    fun clear(context: Context) {
        val editor: SharedPreferences.Editor = context.getSharedPreferences(settingsDb, Context.MODE_PRIVATE).edit()
        editor.clear()
        editor.apply()
    }
    
}