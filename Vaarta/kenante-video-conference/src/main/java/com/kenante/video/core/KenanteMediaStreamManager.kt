package com.kenante.video.core

import android.util.Log
import android.util.SparseArray
import com.kenante.video.enums.MediaEvent
import com.kenante.video.media.KenanteAudioTrack
import com.kenante.video.media.KenanteVideoTrack
import com.varenia.kenante_core.core.KenanteSettings
import org.webrtc.*
import java.math.BigInteger
import java.text.FieldPosition

class KenanteMediaStreamManager{

    private val TAG = KenanteMediaStreamManager::class.java.simpleName

    private var kenanteVideoTracks = HashMap<String,KenanteVideoTrack>()
    private var kenanteAudioTracks = HashMap<String,KenanteAudioTrack>()
    internal var videoTracks = HashMap<String,VideoTrack>()
    internal var audioTracks = HashMap<String,AudioTrack>()

    //Audio-Video Capturer
    internal var mediaStream: MediaStream? = null
    internal var videoCapturerAndroid: VideoCapturer? = null
    internal var audioSource: AudioSource? = null
    internal var videoSource: VideoSource? = null
    internal var localVideoTrack: VideoTrack? = null
    internal var localAudioTrack: AudioTrack? = null

    //Peer connection
    var peerConnectionFactory: PeerConnectionFactory? = null

    companion object {
        private var mediaStreamManager: KenanteMediaStreamManager = KenanteMediaStreamManager()

        fun GetManager(): KenanteMediaStreamManager {
            return mediaStreamManager
        }
    }

    internal fun setAudioTrack(position:String , audioTrack: AudioTrack?, isLocal: Boolean){

        if(audioTrack!=null) {
            val kenanteAudioTrack =
                KenanteMessageParser.currentuserid?.let { KenanteAudioTrack(it, audioTrack = audioTrack) }
            if (kenanteAudioTrack != null) {
                kenanteAudioTracks.set(position, kenanteAudioTrack)
            }
            audioTracks.set( KenanteMessageParser.currentuserid.toString(),audioTrack)
            if(isLocal)
                KenanteSendCallbacks.getInstance().sendMediaStreamCallEventCB(MediaEvent.localaudio, kenanteAudioTrack, null)
            else
                KenanteSendCallbacks.getInstance().sendMediaStreamCallEventCB(MediaEvent.remoteaudio, kenanteAudioTrack, null)
        }

    }

    internal fun setVideoTrack(id: String, videoTrack: VideoTrack?, isLocal: Boolean){
        if(videoTrack!=null) {
            val kenanteVideoTrack = KenanteVideoTrack(id, videoTrack)
            kenanteVideoTracks.set(id,kenanteVideoTrack)
            videoTracks.set(id, videoTrack)
            if(isLocal)
                KenanteSendCallbacks.getInstance().sendMediaStreamCallEventCB(MediaEvent.localvideo, null, kenanteVideoTrack)
            else
                KenanteSendCallbacks.getInstance().sendMediaStreamCallEventCB(MediaEvent.remotevideo, null, kenanteVideoTrack)
        }

    }

    fun getAudioTrack(id: String) : KenanteAudioTrack?{
        if(kenanteAudioTracks.get(id) != null)
            return kenanteAudioTracks.get(id)
        return null
    }

    fun getVideoTrack(id: String) : KenanteVideoTrack?{
        if(kenanteVideoTracks.get(id) != null)
            return kenanteVideoTracks.get(id)
        return null
    }

    internal fun removeVideoTrack(id: String) {
        if(videoTracks.get(id) != null)
            videoTracks.remove(id)
        if(kenanteVideoTracks.get(id) != null)
            kenanteVideoTracks.remove(id)
    }

    internal fun removeAudioTrack(id: String) {
        if(audioTracks.get(id) != null)
            audioTracks.remove(id)
        if(kenanteAudioTracks.get(id) != null)
            kenanteAudioTracks.remove(id)
    }

    fun setVideoCapturer(videoCapturer: VideoCapturer?){
        if(videoCapturer != null){
            releaseCapturer()
            videoCapturerAndroid = videoCapturer
            tryInitVideo()
            if(localVideoTrack != null) {
                KenanteMessageParser.currentuserid?.let { setVideoTrack(it, localVideoTrack, true) }
            }
        }
    }

    fun createAudioStream() {
        val audioConstraints = MediaConstraints()
        audioConstraints.mandatory.add(
                MediaConstraints.KeyValuePair(
                        "googEchoCancellation",
                        "true"
                )
        )
        audioConstraints.mandatory.add(
                MediaConstraints.KeyValuePair(
                        "googEchoCancellation2",
                        "true"
                )
        )
        audioConstraints.mandatory.add(
                MediaConstraints.KeyValuePair(
                        "googDAEchoCancellation",
                        "true"
                )
        )

        audioConstraints.mandatory.add(
                MediaConstraints.KeyValuePair(
                        "googTypingNoiseDetection",
                        "true"
                )
        )

        audioConstraints.mandatory.add(
                MediaConstraints.KeyValuePair(
                        "googAutoGainControl",
                        "true"
                )
        )

        audioConstraints.mandatory.add(
                MediaConstraints.KeyValuePair(
                        "googAutoGainControl2",
                        "true"
                )
        )

        audioConstraints.mandatory.add(
                MediaConstraints.KeyValuePair(
                        "googNoiseSuppression",
                        "true"
                )
        )
        audioConstraints.mandatory.add(
                MediaConstraints.KeyValuePair(
                        "googNoiseSuppression2",
                        "true"
                )
        )

        audioConstraints.mandatory.add(MediaConstraints.KeyValuePair("googAudioMirroring", "false"))
        audioConstraints.mandatory.add(MediaConstraints.KeyValuePair("googHighpassFilter", "true"))

        audioSource = peerConnectionFactory?.createAudioSource(audioConstraints)
        localAudioTrack  = peerConnectionFactory?.createAudioTrack("101", audioSource)
        mediaStream?.addTrack(localAudioTrack)

      //  setAudioTrack(KenanteSession.getInstance().currentUserId, mediaStreamManager.localAudioTrack, true)
    }

    fun switchCamera(observer: CameraVideoCapturer.CameraSwitchHandler){
        KenanteCamera.switchCamera(observer)
    }

    private fun tryInitVideo() {
        if(mediaStream != null){
            val videoTrack = createVideoTrack()
            if(videoTrack != null) {
                localVideoTrack = videoTrack
                mediaStream?.addTrack(localVideoTrack)
            }
        }
    }

    private fun createVideoTrack() : VideoTrack? {
        Log.e(TAG, "createVideoTrack for " + videoCapturerAndroid.toString())
        videoSource = this.peerConnectionFactory?.createVideoSource(false)
        val videoWidth = 1280
        val videoHeight = 1280
        val videoFps = 30
        Log.e(TAG, "video resolution: $videoWidth:$videoHeight:$videoFps")

        val rootEglBase = KenanteEglBase.GetEglBaseContext()
        val surfaceTextureHelper =
                SurfaceTextureHelper.create("CaptureThread", rootEglBase.eglBaseContext)

        videoCapturerAndroid?.initialize(
                surfaceTextureHelper,
                KenanteSettings.getInstance().getContext()!!,
                videoSource?.capturerObserver
        )

        videoCapturerAndroid?.startCapture(videoWidth, videoHeight, videoFps)
        val videoTrack = peerConnectionFactory!!.createVideoTrack("ARDAMSv0", videoSource)
        videoTrack.setEnabled(true)
        Log.e(TAG, "created video track: $videoTrack")
        return videoTrack
    }

    private fun releaseCapturer() {
        if (videoCapturerAndroid != null) {
            Log.e(TAG, "stop Capturer")
            try {
                videoCapturerAndroid?.stopCapture()
            } catch (var2: InterruptedException) {
                Log.e(TAG,  "error stopping Capturer")
            }
            videoCapturerAndroid?.dispose()
            videoCapturerAndroid = null
        }
        if (this.videoSource != null) {
            Log.e(TAG, "Video source state is " + videoSource?.state())
            videoSource?.dispose()
            videoSource = null
            Log.e(TAG, "Video source disposed")
        }
        if (localVideoTrack != null && mediaStream != null) {
            mediaStream?.removeTrack(localVideoTrack)
        }
    }

    internal fun clearAllObjects() {
        audioTracks.clear()
        videoTracks.clear()
        kenanteAudioTracks.clear()
        kenanteVideoTracks.clear()
        releaseCapturer()
    }

}