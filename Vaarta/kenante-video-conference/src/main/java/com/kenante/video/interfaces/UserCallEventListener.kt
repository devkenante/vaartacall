package com.kenante.video.interfaces

import java.math.BigInteger

interface UserCallEventListener {

    fun onUserAvailable(userId: String)
    fun onUserConnectedToCall(userId: String)
    fun onUserDisconnectedFromCall(userId: String)
    fun onUserConnectionClosed(userId: String)

}