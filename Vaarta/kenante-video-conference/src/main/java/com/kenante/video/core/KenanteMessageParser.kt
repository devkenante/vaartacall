package com.kenante.video.core

import android.util.Log
import com.example.kenante_janus.enums.KenanteMessageType
import com.kenante.video.enums.MediaType
import com.kenante.video.enums.VideoRoomResponse
import com.kenante.video.interfaces.KenanteCallEventListener
import org.json.JSONArray
import org.json.JSONObject
import java.math.BigInteger

object KenanteMessageParser {

    var currentuserid:String ? = null
    //val TAG = KenanteMessageParser::class.java.simpleName
    var TAG = "KenanteDebug"

    var listener: KenanteCallEventListener? = null

    fun setCallBackListener(janusConnectionCallBackListener: KenanteCallEventListener) {
        listener = janusConnectionCallBackListener
    }

    fun handleMessage(obj: JSONObject) {
        Log.e(TAG, obj.toString())
        if (obj.has("janus")) {
            val janusMessage = obj.getString("janus")
            when (janusMessage) {
                KenanteMessageType.success.toString() -> {

                    when {
                        obj.getString("transaction") == KenanteTransactions.getCreateTransaction() -> listener!!.onSessionCreated(
                            obj.getJSONObject("data").getLong("id").toBigInteger()
                        )
                        obj.getString("transaction") == KenanteTransactions.getPublisherAttachTransaction() -> {
                            val handleId = obj.getJSONObject("data").getLong("id").toBigInteger()
                            currentuserid=obj.getJSONObject("data").getString("id")
                            listener!!.onPluginAttached(
                                handleId,
                                true,
                                obj.getJSONObject("data").getString("id")                            )
                        }
                        KenanteTransactions.isSubscriberAttachTransaction(obj.getString("transaction")) -> {
                            val subscriberId = KenanteTransactions.getSubscriberId(obj.getString("transaction"))
                            if(subscriberId != null) {
                                val handleId =
                                    obj.getJSONObject("data").getLong("id").toBigInteger()
                                listener!!.onPluginAttached(
                                    handleId,
                                    false,
                                    subscriberId
                                )
                            }
                        }
                    }

                }
                KenanteMessageType.event.toString() -> {

                    if (obj.has("plugindata")) {
                        val pluginData = obj.getJSONObject("plugindata")
                        if (pluginData.has("data")) {
                            val senderId = obj.getLong("sender").toBigInteger()
                            var jsep = JSONObject()
                            if (obj.has("jsep")) {
                                jsep = obj.getJSONObject("jsep")
                            }
                            handleVideoRoomMessages(
                                pluginData.getJSONObject("data"),
                                jsep,
                                senderId
                            )
                        }
                    }

                }
                KenanteMessageType.ack.toString() -> {
                    Log.e(TAG, "Ack Acknowledged")
                }
                KenanteMessageType.error.toString() -> {

                }
                KenanteMessageType.timeout.toString() -> {

                }
                KenanteMessageType.detached.toString() -> {

                }
                KenanteMessageType.webrtcup.toString() -> {
                    val handleId = obj.getLong("sender").toBigInteger()
                    listener!!.onWebrtcUp(handleId)
                }
                KenanteMessageType.media.toString() -> {
                    val handleId = obj.getLong("sender").toBigInteger()
                    val type = obj.getString("type")
                    val receiving = obj.getBoolean("receiving")
                    val mediaType = if (type == "video") {
                        MediaType.video
                    } else {
                        MediaType.audio
                    }
                    listener!!.onMedia(handleId, mediaType, receiving)
                }
                KenanteMessageType.hangup.toString() -> {

                }
                KenanteMessageType.trickle.toString() -> {
                    if(obj.has("sender")){
                        if(obj.has("candidate")){
                            val candidate = obj.getJSONObject("candidate")
                            val handleId = obj.getLong("sender").toBigInteger()
                            listener!!.onTrickleReceived(handleId, candidate)
                        }
                    }
                }
            }
        }
    }

    fun handleVideoRoomMessages(data: JSONObject, jsep: JSONObject, handleId: BigInteger) {
        if (data.has("videoroom")) {
            when (data.getString("videoroom")) {
                KenanteMessageType.event.toString() -> {

                    if (data.has(VideoRoomResponse.error.toString())) {
                        val errorCode = data.getInt("error_code")
                        val errorMessage = data.getString("error")
                        if(errorCode == 436){
                            //User already exists
                            KenanteWsSendMessages.leave(KenanteSession.sessionId, KenanteSession.handleId)
                            KenanteSession.getInstance().handler.post {
                                KenanteSession.getInstance().startSessionListener?.onError(errorMessage)
                            }
                        }
                        return
                    }

                    when {
                        data.has(VideoRoomResponse.joining.toString()) -> {

                        }
                        data.has(VideoRoomResponse.configured.toString()) -> {

                            if (data.getString("configured") == "ok") {
                                listener!!.onConfigured(handleId, jsep)
                            }

                        }
                        data.has(VideoRoomResponse.unpublished.toString()) -> {
                            var unpub=(data.getString("unpublished"))
                            listener!!.onUnpublished(unpub)
                        }
                        data.has(VideoRoomResponse.leaving.toString()) -> {
                            val leavingVal = data.get("leaving")
                            if(leavingVal == "ok"){
                             //   listener!!.onLeaving(KenanteSession.getInstance().currentUserId)
                            }
                            else {
                                var leave=(data.getString("leaving"))

                                listener!!.onLeaving(leave)
                            }
                        }
                        data.has(VideoRoomResponse.publishers.toString()) -> {

                            val publishers: JSONArray = data.getJSONArray("publishers")
                            Log.i("Publisher DATA",publishers.toString())

                            for (i in 0 until publishers.length()) {
                                val each = publishers.get(i) as JSONObject
                                val pubId = each.getString("id")
                                //val display = each.getString("display")
                                val talking = each.getBoolean("talking")
                                //Changed because of Client when enters there might be no audio and video coded
                                var audioCodec ="opus"
                                var videoCodec ="vp8"
                                if (talking !=false) {
                                    audioCodec   = each.getString("audio_codec")
                                    videoCodec   = each.getString("video_codec")

                                }
                                listener!!.onOtherPublisherAvailable(
                                    pubId,
                                    audioCodec,
                                    videoCodec,
                                    talking
                                )
                            }

                        }
                        data.has(VideoRoomResponse.started.toString()) -> {
                            listener!!.onStarted(handleId)
                        }
                    }

                }
                KenanteMessageType.joined.toString() -> {

                    listener!!.onPublisherJoined(handleId)

                    val publishers = data.getJSONArray("publishers")
                    Log.i("Publisher JSON",publishers.toString())
                    for (i in 0 until publishers.length()) {
                        val each = publishers.get(i) as JSONObject
                        val pubId = each.getString("id")
                        val audioCodec = each.getString("audio_codec")
                        val videoCodec = each.getString("video_codec")
                        val talking = each.getBoolean("talking")
                        listener!!.onOtherPublisherAvailable(
                            pubId,
                            audioCodec,
                            videoCodec,
                            talking
                        )
                    }

                }
                KenanteMessageType.attached.toString() -> {

                    listener!!.onSubscriberAttached(handleId, jsep)

                }
            }
        }
    }

}