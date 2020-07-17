package com.varenia.vaarta.activities


import android.content.Context
import android.media.AudioManager
import android.os.Bundle
import android.telecom.Call
import android.util.Log
import android.util.SparseArray
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.kenante.video.core.KenanteMediaStreamManager
import com.kenante.video.core.KenanteMessageParser
import com.kenante.video.core.KenanteSession
import com.kenante.video.enums.KenanteBitrate
import com.kenante.video.enums.MediaType
import com.kenante.video.interfaces.KenanteMediaStreamEventListener
import com.kenante.video.interfaces.UserCallEventListener
import com.kenante.video.media.KenanteAudioTrack
import com.kenante.video.media.KenanteVideoTrack
import com.kenante.video.view.KenanteSurfaceView
import com.varenia.kenante_chat.core.KenanteTasks.handler
import com.varenia.vaarta.R
import com.varenia.vaarta.fragments.UsersListFragment
import com.varenia.vaarta.handler_classes.CallData
import com.varenia.vaarta.interfaces.ActionOnFragment
import com.varenia.vaarta.interfaces.FragmentCallbacks
import com.varenia.vaarta.util.Constants
import com.varenia.vaarta.util.NewSharedPref
import org.webrtc.RendererCommon
import java.math.BigInteger

import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap


/**
 * A simple [Fragment] subclass.
 */
class VideoCallFragment : Fragment(),  UserCallEventListener, KenanteMediaStreamEventListener {

    var TAG = VideoCallFragment::class.java.simpleName
    var fragmentCallbacks: FragmentCallbacks? = null
    var videoViews = HashMap<String,View>()
    var recyclerViewWidth = 0
    var recyclerViewHeight = 0
    var kenanteSession = KenanteSession.getInstance()
    var videoClickLisntener: View.OnClickListener? = null
    var audioClickListener: View.OnClickListener? = null
    var activeHearUsers: MutableSet<String>? = mutableSetOf()
    var videoTrackMap: HashMap<String,KenanteVideoTrack>? = null
    var activeRemoteUsers: MutableSet<String>? = mutableSetOf()
    private var actionOnFragment: ActionOnFragment? = null
    var currentuserid=BigInteger.valueOf(0)
    private var clientControlMRAudio = ArrayList<Int>()
    private  var clientControlTAudio = ArrayList<Int>()
    private val translators: MutableSet<Int>? = mutableSetOf()
    private var userlist = ArrayList<String>()
    private var currentEnlargedUser = 0
    private val ENLARGE_VIDEO_FRAGMENT = 4
    // Views
    var confCallOnePersonView: View? = null
    var videoGrid: GridLayout? = null
    private var expandclose: ImageButton?=null
    private var enlargeUserRV: KenanteSurfaceView? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)
        actionOnFragment = context as ActionOnFragment

        fragmentCallbacks = context as FragmentCallbacks
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        userlist= ArrayList()
        activity?.window?.setFlags(WindowManager.LayoutParams.FLAG_SECURE,WindowManager.LayoutParams.FLAG_SECURE)
        val view = inflater.inflate(R.layout.fragment_video_call, container, false)
        handler.post() {
            parseExtras()
            init(view)
            clickListener()
        }

        fragmentCallbacks!!.onFragmentStarted(2, true)

        return view
    }




    private fun parseExtras() {
        recyclerViewWidth = arguments?.getInt(Constants.EXTRA_VIDEO_SHOW_WIDTH)!!
        recyclerViewHeight = arguments?.getInt(Constants.EXTRA_VIDEO_SHOW_HEIGHT)!!
        var userid=NewSharedPref.getLongValue(Constants.USER_ID)

        if (userid != null) {
            currentuserid= BigInteger.valueOf(userid)
        }
    }

    private fun init(view: View) {
        confCallOnePersonView = view.findViewById(R.id.conf_call_one_person_view)
        enlargeUserRV = view.findViewById(R.id.enlargeUserRV)
        expandclose = view.findViewById(R.id.expandclose)

        videoGrid = view.findViewById(R.id.video_grid)
        kenanteSession.registerUserCallEventListener(this)
        kenanteSession.registerMediaStreamEventListener(this)
      //  audioVideoStatus = callData!!.audioVideoStatus
    }

    private fun clickListener() {
        videoClickLisntener = View.OnClickListener {
            val videoTrack = getVideoTracks().get(view?.getTag())
            if (videoTrack != null) {
                if (videoTrack.enabled()) {
                    videoTrack.setEnabled(false)
                    it!!.background = resources.getDrawable(R.drawable.video_control_icon_disabled)
                } else {
                    videoTrack.setEnabled(true)
                    it!!.background = resources.getDrawable(R.drawable.video_control_icon_enabled)
                }
            }
        }

        audioClickListener = View.OnClickListener {
            var userId = (it!!.contentDescription.toString())
            var status = false
            val audioTrack = KenanteMediaStreamManager.GetManager().getAudioTrack(userId)
            if (audioTrack != null) {
                status = !audioTrack.enabled()
                if (status) {
                    audioTrack.setEnabled(true)
                }
                    audioTrack.setEnabled(false)
            }
            if(userId.equals(KenanteMessageParser.currentuserid)){
                val audioManager: AudioManager = activity!!.getSystemService(Context.AUDIO_SERVICE) as AudioManager
                audioManager.setMode(AudioManager.MODE_IN_CALL)
                //audioManager.setMode(AudioManager.STREAM_VOICE_CALL)
                if (audioManager.isMicrophoneMute() === false) {
                    status=false
                    audioManager.setMicrophoneMute(true)
                } else {
                    status=true
                    audioManager.setMicrophoneMute(false)
                }
            }
            toggleVoiceIcon(userId, status, it)
        }
        expandclose?.setOnClickListener {
            enlargeUserRV?.setVisibility(View.GONE)
            expandclose?.setVisibility(View.GONE)
            //enlargeUserRV?.release()
            videoGrid?.setVisibility(View.VISIBLE)
            videoGrid?.requestLayout()
        }
    }

    private fun toggleVoiceIcon(userId: String, enable: Boolean, view: View) {
        if (enable) {
            view.background = activity!!.resources.getDrawable(R.drawable.round_mic_white_48)
            if (!activeHearUsers?.contains(userId)!!) activeHearUsers?.add(userId)
        } else {
            view.background = activity!!.resources.getDrawable(R.drawable.round_mic_off_white_48)
            //voiceControl.setBackground(getResources().getDrawable(R.drawable.ic_voice_disable));
            if (activeHearUsers?.contains(userId)!!) activeHearUsers?.remove(userId)
        }
    }

    override fun onUserAvailable(userId: String) {
        Log.e(TAG, "User with id $userId joined the call")
        kenanteSession.configureUser(userId, audio = true, video = true, bitrate = KenanteBitrate.low)
        kenanteSession.subscribeToPublisher(userId)
    }

    override fun onUserConnectedToCall(userId: String) {
        Log.e(TAG, "onUserConnectedToCall called with id: $userId")
       // isTranslatorPresent=callData!!.allUsersType.containsValue(3)
        //Connection status
        if (userId .equals( currentuserid))
            return

        if (!userId.equals( currentuserid))
            activeRemoteUsers?.add(userId)


      //  updateUsersList(userId, true)
        updateOnePersonView()
      //  updateUserConnectionStatus(callData!!.longNames!![userId], 1)
    }

    private fun updateUsersList(userId: Int, active: Boolean) {
        val usersListFragment = activity!!.supportFragmentManager.findFragmentByTag(Constants.USERS_LIST_FRAG) as UsersListFragment?
        if (active) {
            if (usersListFragment != null) if (usersListFragment.isAdded) usersListFragment.changeStatus(userId, true)
        } else {
            if (usersListFragment != null) if (usersListFragment.isAdded) usersListFragment.changeStatus(userId, false)
        }
    }

    private fun updateOnePersonView() {
        if (activeRemoteUsers!!.size > 0) {
            if (confCallOnePersonView?.visibility == View.VISIBLE)
                confCallOnePersonView?.visibility = View.GONE
        } else {
            if (confCallOnePersonView?.visibility != View.VISIBLE)
                confCallOnePersonView?.visibility = View.VISIBLE
        }
    }

    override fun onUserConnectionClosed(userId: String) {
        Log.e(TAG, "onUserConnectionClosed called with id: $userId")

        var position=activeRemoteUsers?.indexOf(userId)
        if (position != null) {
            removeUI(userId)
        }

        if (activeHearUsers!!.contains(userId)) activeHearUsers!!.remove(userId)

        userlist.remove(userId)
        updateOnePersonView()
        resizeAllViews()
        //updateUsersList(userId, false)
        updateUserConnectionStatus(userId, 3)
    }

    private fun updateUserConnectionStatus(s: String?, i: Int) {
        (activity as CallActivity).showUserConnectionStatusToast(s, i)
    }

    override fun onUserDisconnectedFromCall(userId: String) {
        Log.e(TAG, "onUserDisconnectedFromCall called with id: $userId")
        //Connection status
        if (videoViews.get(userId)!= null)
        videoViews.get(userId)!!.findViewWithTag<View>("connectionStatusTV").background = activity!!.resources.getDrawable(R.drawable.user_disconneted)
    //Removing Translator and changing the state of Translator

        updateUserConnectionStatus(userId, 2)
    }

    override fun onLocalAudioStream(audioTrack: KenanteAudioTrack) {
        //  (activity as CallActivity).setAudioManager()
        Log.e(TAG, "onLocalAudioStream call with id: ${audioTrack.userId}")

    }

    override fun onLocalVideoStream(videoTrack: KenanteVideoTrack) {
        Log.e(TAG, "onLocalVideoStream call with id: ${videoTrack.userId}")
        (activity as CallActivity) .showLoadingScreen(false, null)

                fragmentCallbacks!!.localVideoExists()

                getVideoTracks().set(videoTrack.userId,videoTrack)
                makeUI(videoTrack.userId)
                //videoTrack.setEnabled(true)
                videoTrack.setEnabled(false)
                videoTrack.setEnabled(true)


    }

    override fun onMediaStartedFlowing(userId: BigInteger, mediaType: MediaType) {
        Log.e(TAG, "onMediaStartedFlowing call with id: $userId and media: $mediaType")
    }

    override fun onMediaStoppedFlowing(userId: BigInteger, mediaType: MediaType) {
        Log.e(TAG, "onMediaStoppedFlowing call with id: $userId and media: $mediaType")
    }

    override fun onRemoteAudioStream(audioTrack: KenanteAudioTrack) {
        Log.e(TAG, "onRemoteAudioStream call with id: ${audioTrack.userId}")
        if(audioTrack!=null){
                activeHearUsers!!.add(audioTrack.userId)
                audioTrack.setEnabled(true)
        }else{
            Log.e(TAG,"Not able to get audiostream from remote")
        }
    }

    override fun onRemoteVideoStream(videoTrack: KenanteVideoTrack) {
        Log.e(TAG, "onRemoteVideoStream call with id: ${videoTrack.userId}")
        getVideoTracks().set(videoTrack.userId, videoTrack)

        makeUI(videoTrack.userId)


        if (videoViews.get(videoTrack.userId) != null){
            videoViews.get(videoTrack.userId)!!.findViewWithTag<View>("connectionStatusTV").background = activity!!.resources.getDrawable(R.drawable.user_connected)
        }



        if (videoViews.get(videoTrack.userId) != null) {
            //Attach video strea
            attachVideoStream(videoTrack)
        }
    }

    private fun getVideoTracks(): HashMap<String,KenanteVideoTrack> {
        if (videoTrackMap == null) {
            videoTrackMap = HashMap()
        }
        return videoTrackMap!!
    }

    fun makeUI(id: String) {
        userlist.add(id)
        if (videoViews.get(id) == null) {
            val view = layoutInflater.inflate(R.layout.show_video_row, null)
            view.setTag(id)
            view.contentDescription = "$id"
            videoViews.set(id, view)
            makeUiAccordingToUser(id, view)
            videoGrid?.addView(view)
            changeGridLayoutParameters(videoViews.size)
            resizeAllViews()
            Log.i("VideoView \n grid Count",""+videoViews.size +"\n"+videoGrid!!.childCount  )
        } else {
            if (KenanteMessageParser.currentuserid!!.equals(id)) {
                if ((activity as CallActivity).isScreenSharing) {

                } else {

                }
                val v = videoViews.get(id)
                val kenanteSurfaceView: KenanteSurfaceView = v!!.findViewWithTag("videoShowView")

                KenanteMediaStreamManager.GetManager().getVideoTrack(id)
                    ?.addSink(kenanteSurfaceView)
            }

        }
    }

    fun attachVideoStream(videoTrack: KenanteVideoTrack) {
        val view = videoViews.get(videoTrack.userId)
        val kenanteSurfaceView: KenanteSurfaceView = view!!.findViewWithTag("videoShowView")

        if (videoTrack.getSink() == null) {
            videoTrack.addSink(kenanteSurfaceView)

        }
    }

    fun removeUI(id: String) {
        if (videoViews.get(id) != null) {
            val view = videoViews.get(id)
            val surfaceViewRenderer: KenanteSurfaceView = view!!.findViewWithTag("videoShowView")
            surfaceViewRenderer.release()
            videoGrid?.removeView(view)
            videoViews.remove(id)
            resizeAllViews()
            changeGridLayoutParameters(videoViews.size)
        }
        if (getVideoTracks().get(id) != null) {
            getVideoTracks().get(id)?.removeSink(getVideoTracks().get(id)?.getSink())
            getVideoTracks().remove(id)
        }
    }

    private fun changeGridLayoutParameters(totalSize: Int) {
        when (totalSize) {
            in 1..2 -> {
                videoGrid?.columnCount = 1
                videoGrid?.rowCount = 2
            }
            in 3..4 -> {
                videoGrid?.columnCount = 2
                videoGrid?.rowCount = 2
            }
            in 5..6 -> {
                videoGrid?.columnCount = 2
                videoGrid?.rowCount = 3
            }
            in 7..9 -> {
                videoGrid?.columnCount = 3
                videoGrid?.rowCount = 3
            }
            in 10..16 -> {
                videoGrid?.columnCount = 4
                videoGrid?.rowCount = 4
            }
        }
    }

    fun resizeAllViews() {

        val width = getWidth(recyclerViewWidth, videoViews.size)
        val height = getHeight(recyclerViewHeight, videoViews.size)
        val uniquelist= (userlist).distinct()
        for(j in 0 until uniquelist.size) {

            for (i in 0 until videoViews.size) {
                if (videoViews.contains(uniquelist.elementAt(j).toString())) {
                    val v = videoViews.get(uniquelist.elementAt(j))
                    v!!.layoutParams = GridLayout.LayoutParams()
                    v!!.layoutParams.width = width
                    v!!.layoutParams.height = height
                    v!!.requestLayout()
                    v!!.requestFocus()
                }
            }
        }

       /* videoGrid!!.refreshDrawableState()
        videoGrid!!.requestLayout()
        videoGrid!!.requestFocus()
        videoGrid!!.invalidate()*/

    }

    fun getWidth(parentWidth: Int, size: Int): Int {
        return when {
            size in 1..2 -> parentWidth
            size in 3..6 -> parentWidth / 2
            size in 7..9 -> parentWidth / 3
            size > 9 -> parentWidth / 4
            else -> 0
        }
    }

    fun getHeight(parentHeight: Int, size: Int): Int {
        return when {
            size == 1 -> parentHeight
            size in 2..4 -> parentHeight / 2
            size in 5..6 -> parentHeight / 3
            size in 7..9 -> parentHeight / 3
            size > 9 -> parentHeight / 4
            else -> 0
        }
    }

    private fun makeUiAccordingToUser(userId: String, v: View) {
        if (userId.equals(KenanteMessageParser.currentuserid)) {
            v.findViewWithTag<View>("connectionStatusTV").visibility = View.GONE
        }
        //Video Control UI
        val videoControl = v.findViewWithTag<ImageButton>("videoControl")
        videoControl.contentDescription = userId
        videoControl.setOnClickListener(videoClickLisntener)
        if (!userId.equals(currentuserid)) {
            videoControl.visibility = View.GONE
        }
        //Name of user UI
        (v.findViewWithTag<View>("videoShowUserNameTV") as TextView).text = userId

        //Attaching Video
        val kenanteSurfaceView: KenanteSurfaceView = v.findViewWithTag("videoShowView")
        kenanteSurfaceView.setZOrderMediaOverlay(true)
        val isMirror = userId.equals(KenanteMessageParser.currentuserid)

        kenanteSurfaceView.setScalingType(RendererCommon.ScalingType.SCALE_ASPECT_FILL)
        kenanteSurfaceView.setMirror(isMirror)
        kenanteSurfaceView.requestLayout()
        kenanteSurfaceView.setOnClickListener {
            val userId: String = userId
            if (userId.equals(KenanteMessageParser.currentuserid) )
                Toast.makeText(activity,"Camera view can't enlarged",Toast.LENGTH_LONG).show()
            else
                showFullScreenOfUser(userId)
        }
        //Attach video stream
        val videoTrack = getVideoTracks().get(userId)
        videoTrack?.addSink(kenanteSurfaceView)

        //Audio Control UI
        val audioControl = v.findViewWithTag<ImageButton>("videoVoiceControl")
        audioControl.contentDescription = userId
        audioControl.setOnClickListener(audioClickListener)
         //   if (userId.equals(currentuserid)) {
                val audioTrack = KenanteMediaStreamManager.GetManager().getAudioTrack(userId)
                if (audioTrack != null) {
                    audioTrack.setEnabled(true)
                    if (audioTrack.enabled()) audioControl.background = activity!!.resources.getDrawable(R.drawable.round_mic_white_48) else audioControl.background = activity!!.resources.getDrawable(R.drawable.round_mic_off_white_48)
                }

        //    } else {

               /* val audioTrack = KenanteMediaStreamManager.GetManager().getAudioTrack(position)
                if (audioTrack != null) {

                    if (audioTrack.enabled()) audioControl.background = activity!!.resources.getDrawable(R.drawable.round_mic_white_48) else audioControl.background = activity!!.resources.getDrawable(R.drawable.round_mic_off_white_48)

                }*/
         //   }
    }

    /*public void handleRecyclerViewVisibility(Boolean show) {
        if (show)
            videoShowRV.setVisibility(View.VISIBLE);
        else
            videoShowRV.setVisibility(View.GONE);
    }*/
    private fun showFullScreenOfUser(userId: String) {

        // enlargeUserRV=view?.findViewById(R.id.enlargeUserRV)
        enlargeUserRV?.setVisibility(View.VISIBLE)
        expandclose?.setVisibility(View.VISIBLE)
        enlargeUserRV?.setZOrderMediaOverlay(true)
        val isMirror = userId .equals( currentuserid)

        enlargeUserRV?.setScalingType(RendererCommon.ScalingType.SCALE_ASPECT_FILL)
        enlargeUserRV?.setMirror(isMirror)
        enlargeUserRV?.requestLayout()

        getVideoTracks().get(userId)?.addSink(enlargeUserRV as KenanteSurfaceView)
        videoGrid?.requestLayout()

    }

    fun clearAllObjects() {
        videoViews.clear()
        videoGrid?.removeAllViews()
        videoViews.clear()
        removeVideoTrackRenderers()
        removeAllListeners()
    }

    fun removeVideoTrackRenderers() {
        val videoTracks = getVideoTracks()
            if (videoTracks.size > 0) {
              videoTracks.clear()
                  } else {

            }

    }

    fun removeAllListeners() {
        kenanteSession.unregisterUserCallEventListener(this)
        kenanteSession.unregisterMediaStreamEventListener(this)
    }

    fun cameraSwitch(isCameraFront: Boolean) {
        val v = videoViews.get(KenanteMessageParser.currentuserid)
        if (v != null) {
            val kenanteSurfaceView: KenanteSurfaceView = v.findViewWithTag("videoShowView")
            kenanteSurfaceView.setScalingType(RendererCommon.ScalingType.SCALE_ASPECT_FILL)
            kenanteSurfaceView.setMirror(isCameraFront)
            kenanteSurfaceView.requestLayout()
        }
    }


}
