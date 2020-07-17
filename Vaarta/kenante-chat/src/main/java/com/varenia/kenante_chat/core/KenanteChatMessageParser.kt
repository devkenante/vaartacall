package com.varenia.kenante_chat.core

import android.util.Log
import com.google.gson.Gson
import com.google.gson.internal.LinkedTreeMap
import com.varenia.kenante_chat.enums.KenanteChatMessageAction
import com.varenia.kenante_chat.interfaces.KenanteChatEventListener
import org.json.JSONObject

object KenanteChatMessageParser {

    val TAG = KenanteChatMessageParser::class.java.simpleName
    var kenanteChatEventListener: KenanteChatEventListener? = null

    fun setListener(listener: KenanteChatEventListener?) {
        this.kenanteChatEventListener = listener
    }

    fun handleMessage(obj: JSONObject) {
        Log.e(TAG, obj.toString())
        val message = obj.getJSONObject("message")
        when (message.getString("action")) {
            KenanteChatMessageAction.ServerNotify.name -> {
                val msg = message.getString("message")
                if (msg == "connected") {
                    KenanteChatSession.getInstance().handler.post {
                        KenanteChatSession.getInstance().connectedToChat = true
                        kenanteChatEventListener?.onChatJoined()
                    }
                    val usersOnline = message.getJSONArray("users_online")
                    for (i in 0 until usersOnline.length()){
                        val item = usersOnline.getJSONObject(i)
                        val userId = item.getInt("user_id")
                        val channel = item.getString("channel")
                        KenanteChatSession.getInstance().chatChannels.put(userId, channel)
                        KenanteChatSession.getInstance().handler.post {
                            kenanteChatEventListener?.onChatUserConnected(userId, channel)
                        }
                    }
                } else if (msg == "joined") {
                    val userId = message.getInt("user_id")
                    val channel = message.getString("channel")
                    KenanteChatSession.getInstance().chatChannels.put(userId, channel)
                    KenanteChatSession.getInstance().handler.post {
                        kenanteChatEventListener?.onChatUserConnected(userId, channel)
                    }
                } else if (msg == "history") {
                    val userId = message.getInt("user_id")
                    val history = message.getJSONArray("history")
                    val historyArray = ArrayList<KenanteChatMessage>()
                    for (i in 0 until history.length()) {
                        val item = history.getJSONObject(i)
                        val roomId = item.getInt("room_id")
                        val senderId = item.getInt("sender_id")
                        val receiverId = item.getInt("receiver_id")
                        val textMessage = item.getString("message")
                        val action = item.getString("action")
                        val timestamp = item.getString("timestamp")
                                //Adding attachment values as well
                        var act: KenanteChatMessageAction? = null
                        act = if (action == KenanteChatMessageAction.Text.name)
                            KenanteChatMessageAction.Text
                        else
                            KenanteChatMessageAction.Media
                        val chatMessage = KenanteChatMessage(roomId, senderId, receiverId,
                                textMessage, act)

                        if(action==KenanteChatMessageAction.Media.name) {
                            val attachment = Gson().fromJson(item.getString("media_url"),mutableListOf<String>().javaClass)

                            if (attachment!=null) {
                                val t: LinkedTreeMap<*, *> = attachment[0] as LinkedTreeMap<*, *>

                                val extension = t.get("extension")
                                val name = t.get("name")
                                val url = t.get("url")
                                val filetype = t.get("fileType")

                                val kenanteAttachment = KenanteAttachment(name as String, extension as String, url as String, filetype as String)
                                 chatMessage.attachments = arrayListOf(kenanteAttachment)
                            }
                        }
                        chatMessage.timestamp = timestamp
                        historyArray.add(chatMessage)
                    }
                    KenanteChatSession.getInstance().handler.post{
                        KenanteChatSession.getInstance().chatHistoryEventListener?.onSuccess(historyArray, userId)
                    }

                } else if (msg == "left") {
                    val userId = message.getInt("user_id")
                    KenanteChatSession.getInstance().chatChannels.remove(userId)
                    KenanteChatSession.getInstance().connectedToChat = false
                    KenanteChatSession.getInstance().handler.post {
                        kenanteChatEventListener?.onChatUserLeft(userId)
                    }
                }
            }
            KenanteChatMessageAction.Text.name -> {
                val roomId = message.getInt("room_id")
                val msg = message.getString("message")
                val senderId = message.getInt("sender_id")
                val receiverId = message.getInt("receiver_id")
                val action: KenanteChatMessageAction = KenanteChatMessageAction.Text
                val timestamp = message.getString("timestamp")
                val chatMessage = KenanteChatMessage(
                        roomId,
                        senderId,
                        receiverId,
                        msg,
                        action
                )
                chatMessage.timestamp = timestamp
                KenanteChatSession.getInstance().handler.post {
                    kenanteChatEventListener?.onMessage(chatMessage)
                }
            }
            KenanteChatMessageAction.Media.name -> {
                val roomId = message.getInt("room_id")
                val msg = message.getString("message")
                val senderId = message.getInt("sender_id")
                val receiverId = message.getInt("receiver_id")
                val action: KenanteChatMessageAction = KenanteChatMessageAction.Media
                val timestamp = message.getString("timestamp")
                val chatMessage = KenanteChatMessage(
                        roomId,
                        senderId,
                        receiverId,
                        msg,
                        action

                )

                val attachment =message.getJSONArray("attachments")
                if(attachment.length()>0 && attachment!=null) {
                    val extension = attachment.getJSONObject(0).getString("extension")
                    val name = attachment.getJSONObject(0).getString("name")
                    val url = attachment.getJSONObject(0).getString("url")
                    val filetype = attachment.getJSONObject(0).getString("fileType")
                    val kenanteAttachment = KenanteAttachment(name, extension, url, filetype)
                    chatMessage.attachments = arrayListOf(kenanteAttachment)

                }
                chatMessage.timestamp = timestamp
                KenanteChatSession.getInstance().handler.post {
                    kenanteChatEventListener?.onMessage(chatMessage)
                }
            }

        }

    }

}