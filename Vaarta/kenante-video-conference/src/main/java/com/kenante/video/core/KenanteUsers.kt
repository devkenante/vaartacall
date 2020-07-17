package com.kenante.video.core

import android.util.ArraySet
import android.util.SparseArray
import androidx.core.util.set
import com.kenante.video.enums.KenanteBitrate
import java.math.BigInteger

object KenanteUsers {

    private var users = ArrayList<User>()
    internal val liveUsers = mutableListOf<String>()

    fun setUserCallParameters(
            id: String,
            audio: Boolean,
            video: Boolean,
            bitrate: KenanteBitrate
    ) {
        users.add(User(id,audio, video, bitrate))
    }

    fun getUser(id: String): User {
        var index = -1
        var k=0;
        for (i in users){
            if(i.id.equals(id)){
                index=k
            }
            k++
        }
        return users.get(index)
    }

    fun setUsersContainer(user: String) {
        users.add(User(user,audio = true, video = true, bitrate = KenanteBitrate.low))
    }

}


data class User(
        var id: String,
        var audio: Boolean,
        var video: Boolean,
        var bitrate: KenanteBitrate = KenanteBitrate.low
)