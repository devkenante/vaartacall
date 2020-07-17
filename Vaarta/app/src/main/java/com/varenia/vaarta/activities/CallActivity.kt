package com.varenia.vaarta.activities

import android.Manifest
import android.annotation.TargetApi
import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.util.SparseArray
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.view.WindowManager
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.kenante.video.core.*
import com.kenante.video.enums.KenanteBitrate
import com.kenante.video.interfaces.KenanteSessionEventListener
import com.kenante.video.interfaces.SessionEventListener
import com.kenante.video.interfaces.UserCallEventListener
import com.varenia.kenante_chat.core.KenanteAttachment
import com.varenia.kenante_chat.core.KenanteChatSession
import com.varenia.kenante_core.core.KenanteSettings
import com.varenia.vaarta.R
import com.varenia.vaarta.interfaces.ActionOnFragment
import com.varenia.vaarta.interfaces.FragmentCallbacks
import com.varenia.vaarta.util.*
import com.varenia.vaarta.util.StaticMethods.askPermission
import com.varenia.vaarta.util.StaticMethods.hasPermissions
import org.webrtc.CameraVideoCapturer
import java.io.File
import java.net.URISyntaxException

class CallActivity : AppCompatActivity(), View.OnClickListener, KenanteSessionEventListener,
    ActionOnFragment, UserCallEventListener,FragmentCallbacks,
    NetworkConnectionCheck.OnConnectivityChangedListener {

    val TAG = CallActivity::class.java.simpleName
    var kenanteSession = KenanteSession.getInstance()
    var roomName = ""
    var roomId=0L
    //var callData: CallData? = null
    var videoWidth = 0
    var videoHeight = 0
    var isScreenSharing = false
    var chatAvailable = false
    var fragmentsOnTop: MutableSet<Int>? = null
    var disconnectFromUser = false
    var currentChatOpponent = 0
    var isSessionClosed = false
    private val RECORD_FRAGMENT = 1
    private val VIDEO_CALL_FRAGMENT = 2
    private val FULL_CHAT_FRAGMENT = 3
    private val PICK_FILE = 100
    private val FIREBASE_HANDLER_FRAGMENT = 5
    private val FIREBASE_SHOW_FRAGMENT = 6
    private val ENLARGE_VIDEO_FRAGMENT = 4
    private val BOTTOM_CHAT_FRAGMENT = 7
    private val USERS_LIST_FRAGMENT = 8
    private val FILE_OPENER_FRAGMENT = 10
    private var isModerator = false
    private var audioManager: KenanteAppRTCAudioManager? = null
    private var chatSession: KenanteChatSession? = null
   // private var chatUsers = ArrayList<Int>()
    private var chatChannels = SparseArray<String>()
    private var chatOpen = false
    private var stopTransAudio = false
    private var networkConnectionCheck: NetworkConnectionCheck? = null
    private val PERMISSIONS = arrayOf(
        Manifest.permission.READ_PHONE_STATE,
        Manifest.permission.CAMERA,
        Manifest.permission.RECORD_AUDIO,
        Manifest.permission.READ_CONTACTS,
        Manifest.permission.READ_EXTERNAL_STORAGE,
        Manifest.permission.WRITE_EXTERNAL_STORAGE,
        Manifest.permission.MODIFY_AUDIO_SETTINGS,
        Manifest.permission.CHANGE_NETWORK_STATE
        )
    //INTERNET CODES
    private var INTERNET_AVAILABLE = 1
    private var INTERNET_LOST = 2
    private var currentInternetStatus = INTERNET_AVAILABLE

  /*  //Fragments
    private var fullScreenChatFragment = FullScreenChatFragment()
    private var bottomChatFragment = BottomChatFragment()
*/
    // Views
    var userName: TextView? = null
    var closeButton: ImageView? = null
    //Getting views
   // var clientAudioIcon: ImageView? = null
    var videoShowFl: FrameLayout? = null
    var actionBar: LinearLayout? = null
    var loadingFl: FrameLayout? = null
    var loadingText: TextView? = null
    //var messageBox: FrameLayout? = null
    //var writeChatMessage: EditText? = null
    //var sendMessageIB: ImageButton? = null
    //var sendattachment:ImageButton?=null
    //var beginChatText: TextView? = null
   // var sendChatLL: LinearLayout? = null
    var usersListIcon: ImageView? = null
    var screenShareIcon: ImageView? = null
    var rotateCameraIcon: ImageView? = null
    //var firebaseIcon: ImageView? = null
    var usersConnectedLL: LinearLayout? = null
    var noNetFl: FrameLayout? = null
    var rejoinButton: Button? = null
    val handler = Handler(KenanteSettings.getInstance().getContext()?.mainLooper!!)
    companion object {
        fun start(context: Context) {
            val intent = Intent(context, CallActivity::class.java)
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
        }
    }

    override fun onResume() {
        super.onResume()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            networkConnectionCheck?.registerListener(this)
        }

    }

    override fun onPause() {
        super.onPause()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            networkConnectionCheck?.unregisterListener(this)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_call_activity)

        handler.post(){
            checkAllpermissions()
            setAudioManager()
            initialize()
            clickListener()
            //updateUI()
        }

    }

    private fun checkAllpermissions() {
        //TODO("Not yet implemented")
        if (hasPermissions(this@CallActivity, *PERMISSIONS)) {

        } else {
            askPermission(this@CallActivity, PERMISSIONS)
        }
    }

    private fun initialize() {
        initViews()
        initNetworkListeners()
        initCallData()
        getVideoLayoutSize()
        //handleChatBoxUi()
        keepScreenAwake()
    }

    private fun initViews() {
        userName = findViewById(R.id.userNameTV)
        closeButton = findViewById(R.id.closeCallIV)
        videoShowFl = findViewById(R.id.videoShowFL)
        actionBar = findViewById(R.id.actionBarIconsLL)
      //  clientAudioIcon = findViewById(R.id.clientAudioIcon)
        loadingFl = findViewById(R.id.loadingFL)
        loadingText = findViewById(R.id.loadingText)
        /*messageBox = findViewById(R.id.messageFL)
        writeChatMessage = findViewById(R.id.callTypeMessageET)
        sendMessageIB = findViewById(R.id.sendMessageIB)
        sendattachment=findViewById(R.id.sendattachment)
        beginChatText = findViewById(R.id.beginChatTV)
        sendChatLL = findViewById(R.id.sendChatLL)
      */  usersListIcon = findViewById(R.id.usersListIcon)
        screenShareIcon = findViewById(R.id.screenShareIcon)
        rotateCameraIcon = findViewById(R.id.rotateCameraIcon)
       // firebaseIcon = findViewById(R.id.firebaseIcon)
        usersConnectedLL = findViewById(R.id .user_connected_ll)
        noNetFl = findViewById(R.id.noNetFl)
        rejoinButton = findViewById(R.id.rejoinButton)
       //CustomEditText.setListener(this)
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private fun initNetworkListeners() {
        networkConnectionCheck = NetworkConnectionCheck(application)
    }

    private fun initCallData() {
        roomName = "optional"
        roomId = NewSharedPref.GetSharedPreferences(this).getString(Constants.ROOM_ID,"0").toString().toLong()
        fragmentsOnTop = mutableSetOf()
        kenanteSession.registerSessionEventListeners(this)
        kenanteSession.registerUserCallEventListener(this)
       // chatUsers = callData!!.chatUsers
    }

    fun setAudioManager() {
        audioManager = KenanteAppRTCAudioManager.Companion.create(this)

        audioManager!!.setOnWiredHeadsetStateListener(object : KenanteAppRTCAudioManager.OnWiredHeadsetStateListener {
            override fun onWiredHeadsetStateChanged(var1: Boolean, var2: Boolean) {
                if (var1) setAudioDeviceDelayed(KenanteAppRTCAudioManager.AudioDevice.WIRED_HEADSET, 1) else {
                    setAudioDeviceDelayed(KenanteAppRTCAudioManager.AudioDevice.SPEAKER_PHONE, 2)
                }
            }
        })

        val events: KenanteAppRTCAudioManager.AudioManagerEvents = object : KenanteAppRTCAudioManager.AudioManagerEvents {
            override fun onAudioDeviceChanged(var1: KenanteAppRTCAudioManager.AudioDevice?, var2: Set<KenanteAppRTCAudioManager.AudioDevice?>?) {
                Log.d(TAG, "Audio Device: $var1")
                if (var1?.equals(KenanteAppRTCAudioManager.AudioDevice.BLUETOOTH)!!) {
                    setAudioDeviceDelayed(KenanteAppRTCAudioManager.AudioDevice.BLUETOOTH, 3)
                }
            }
        }

        audioManager!!.start(events)
    }

    private fun setAudioDeviceDelayed(audioDevice: KenanteAppRTCAudioManager.AudioDevice, type: Int) {
        Handler().postDelayed({
            when (type) {
                1 -> {
                    Log.d(TAG, "AppRTCAudioManager.AudioDevice.WIRED_HEADSET")
                    Toast.makeText(this@CallActivity, getString(R.string.wired_headset_in), Toast.LENGTH_SHORT).show()
                }
                2 -> {
                    Log.d(TAG, "AppRTCAudioManager.AudioDevice.SPEAKER_PHONE")
                    //Toast.makeText(CallActivity.this, "Speaker Active", Toast.LENGTH_SHORT).show();
                }
                3 -> {
                    Log.d(TAG, "AppRTCAudioManager.AudioDevice.BLUETOOTH")
                    Toast.makeText(this@CallActivity, "Bluetooth Active", Toast.LENGTH_SHORT).show()
                }
            }
            audioManager!!.setAudioDevice(audioDevice)
        }, 1000)
    }

    private fun keepScreenAwake() {
        window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
    }

    private fun clickListener() {
        closeButton?.setOnClickListener(this)
        usersListIcon?.setOnClickListener(this)
        screenShareIcon?.setOnClickListener(this)
        rotateCameraIcon?.setOnClickListener(this)
      /*  firebaseIcon?.setOnClickListener(this)
        messageBox?.setOnClickListener(this)
        sendMessageIB?.setOnClickListener(this)
        sendattachment?.setOnClickListener(this)
     */   rejoinButton?.setOnClickListener(this)
       // clientAudioIcon?.setOnClickListener(this)
    }

   /* private fun updateUI() {
       // userName?.text = callData?.currentUserName
        if (callData!!.chatUsers.size > 0) {
            messageBox?.visibility = View.VISIBLE
            chatAvailable = true
        } else {
            messageBox?.visibility = View.GONE
        }
      
    }*/

   /* private fun handleChatBoxUi() {
        if (chatUsers.size > 0) {
            messageBox?.visibility = View.VISIBLE
            beginChatText?.text = "Connecting to chat room"
            initChat()
        }
    }

    private fun initChat() {
        chatSession = KenanteChatSession.getInstance()
        chatSession?.setChatListener(this)
        chatSession?.enterChat(roomId, callData!!.currentUserId)
    }*/

    private fun getVideoLayoutSize() {
        videoShowFl?.post {
            videoWidth = videoShowFl?.width!!
            videoHeight = videoShowFl?.height!!
            startCall()
            runOnUiThread { showLoadingScreen(true, getString(R.string.connecting_call)) }
        }
    }

    private fun startCall() {
        startFragment(VIDEO_CALL_FRAGMENT, null)
        kenanteSession.startCall(roomId, audio = true, video = true, bitrate = KenanteBitrate.low)
    }

    fun showLoadingScreen(show: Boolean, message: String?) {
        if (show) {
            actionBar?.visibility = View.INVISIBLE
            if (loadingText != null) loadingText?.text = message
            loadingFl?.visibility = View.VISIBLE
        } else {
            actionBar?.visibility = View.VISIBLE
            loadingFl?.visibility = View.GONE
        }
    }

    override fun onClick(p0: View?) {
        when (p0?.id) {
            R.id.closeCallIV -> {
                showDialog(" VaartaCall ")
            }

            R.id.usersListIcon -> {

                if (!fragmentsOnTop!!.contains(USERS_LIST_FRAGMENT)) fragmentsOnTop!!.add(USERS_LIST_FRAGMENT)
                if (fragmentsOnTop!!.size == 1) closeButton?.background = resources.getDrawable(R.drawable.back_arrow)
                startFragment(USERS_LIST_FRAGMENT, null)

            }

            R.id.screenShareIcon -> {
                if (!isScreenSharing) {
                    KenanteScreenShare.requestPermissions(this)
                } else {
                    screenShareIcon!!.setBackgroundResource(R.drawable.screen_share)
                    returnToCamera()
                }
            }

            R.id.rotateCameraIcon -> {
                if (!isScreenSharing) {
                    KenanteMediaStreamManager.GetManager().switchCamera(object : CameraVideoCapturer.CameraSwitchHandler {
                        override fun onCameraSwitchDone(p0: Boolean) {
                            val fragment1: VideoCallFragment? = supportFragmentManager.findFragmentByTag(Constants.VIDEO_SHOW_FRAG) as VideoCallFragment?
                            if (fragment1 != null && fragment1.isAdded) {
                                fragment1.cameraSwitch(p0)
                            }
                            if (p0) {
                                rotateCameraIcon!!.setBackgroundResource(R.drawable.rotate_camera)
                            } else {
                                rotateCameraIcon!!.setBackgroundResource(R.drawable.rotate_camera_highlight)
                            }
                        }

                        override fun onCameraSwitchError(p0: String?) {
                            Log.e(TAG, getString(R.string.camera_switch_error))
                        }

                    })
                } else {
                    Toast.makeText(this, getString(R.string.close_screen_share), Toast.LENGTH_SHORT).show()
                }
            }

          /*  R.id.firebaseIcon -> {
                showFirebaseList()
            }*/

            /*R.id.messageFL -> {
                if (chatOpen) {
                    hideFragment(USERS_LIST_FRAGMENT)
                    if (!fragmentsOnTop!!.contains(FULL_CHAT_FRAGMENT)) fragmentsOnTop!!.add(FULL_CHAT_FRAGMENT)
                    if (fragmentsOnTop!!.size == 1) closeButton?.background = resources.getDrawable(R.drawable.back_arrow)
                    fullScreenChatFragment.handleFullChatVisibility(1, 0)
                } else {
                    beginChatText?.text = "Connecting to chat room"
                    chatSession?.enterChat(roomId, callData!!.currentUserId)
                }
            }

            R.id.sendMessageIB -> {
                val text: String = writeChatMessage?.text.toString().trim({ it <= ' ' })
                if (text.trim { it <= ' ' } != "")
                    if (chatChannels.get(currentChatOpponent) != null)
                        sendMessage(text, null)
                    else Toast.makeText(this@CallActivity,"User is not online",Toast.LENGTH_LONG).show()
                else Toast.makeText(this@CallActivity,"Text is Missing",Toast.LENGTH_LONG).show()
            }

            R.id.sendattachment ->{
                if (hasPermission())
                    sendAttachment()

            }*/

            R.id.rejoinButton -> {
                if (StaticMethods.isNetworkAvailable(this@CallActivity)) {
                    rejoinButton!!.visibility = View.GONE
                    showLoadingScreen(true, getString(R.string.rejoinging_to_call))
                    restartCall()
                } else Toast.makeText(this@CallActivity, getString(R.string.internet_error), Toast.LENGTH_SHORT).show()

            }
          /*  R.id.clientAudioIcon ->{
                val videoCallFragment = supportFragmentManager.findFragmentByTag(Constants.VIDEO_SHOW_FRAG) as VideoCallFragment?
                if (videoCallFragment != null) if (videoCallFragment.isAdded) {
                    if (!stopTransAudio) {
                        stopTransAudio = true
                        clientAudioIcon!!.background = resources.getDrawable(R.drawable.translator_inactive)
                        videoCallFragment.pauseAudioForTranslator(stopTransAudio)
                        Toast.makeText(this@CallActivity, "Swittched To Default Mode", Toast.LENGTH_SHORT).show()
                    } else {
                        stopTransAudio = false
                        clientAudioIcon!!.background = resources.getDrawable(R.drawable.translator_active)
                        videoCallFragment.pauseAudioForTranslator(stopTransAudio)
                        Toast.makeText(this@CallActivity, "Switched To Translator Mode", Toast.LENGTH_SHORT).show()
                    }
                }
            }*/
        }
    }

    private fun releaseAllObjectsAndFinish() {
        disconnectFromUser = true
        showLoadingScreen(true, "Closing call...")
        /*if (isRecording) {
            removeFragment(RECORD_FRAGMENT)
        }*/
        //Already removed when the used cut the call
        //removeFragment(VIDEO_CALL_FRAGMENT);
        if (chatSession!=null) {
            removeFragment(FULL_CHAT_FRAGMENT)
            removeFragment(BOTTOM_CHAT_FRAGMENT)
        }
        removeFragment(USERS_LIST_FRAGMENT)
        //removeFragment(FIREBASE_HANDLER_FRAGMENT)
       // removeFragment(FIREBASE_SHOW_FRAGMENT)
      //  removeFragment(ENLARGE_VIDEO_FRAGMENT)
      //  removeFragment(FILE_OPENER_FRAGMENT)
        val sharedPref = SharedPref.getInstance()
        sharedPref.CALL_STARTED = false
        //sharedPref.updateSetting()
        delayedActivityDestruction()
    }
    private fun delayedActivityDestruction() {
        Handler().postDelayed({
            showLoadingScreen(false, null)
            Log.e(TAG, "Destroy Base Call Activity")
            finish()
        }, 500)
    }

    private fun hasPermission(): Boolean {
        val PERMISSIONS = arrayOf(
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE)
        if (StaticMethods.hasPermissions(this, *PERMISSIONS)) return true else StaticMethods.askPermission(this, PERMISSIONS)
        return false
    }

    override fun onSessionClosed() {
        Log.e(TAG, "onSessionClosed called")
        isSessionClosed = true
        if (disconnectFromUser) {
            releaseAllObjectsAndFinish(true)
        } else {
            releaseAllObjectsAndFinish(false)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == KenanteScreenShare.REQUEST_MEDIA_PROJECTION) {
                isScreenSharing = true
                startScreenShare(data)
            } else if (requestCode == PICK_FILE) {
                val u = data!!.data
                var path: String? = null
                try {
                    path = PathUtil.getPath(this, u)
                    if (path != null) {
                        val attachmentFile = File(path)
                        val fileName = attachmentFile.toString().substring(attachmentFile.toString().lastIndexOf("/") + 1)
                        val ext = fileName.substring(fileName.lastIndexOf(".") + 1)
                        if (KenanteAttachment.isFileSupported(ext)) {
                           // askToSendThisAsAttachment(attachmentFile)
                            return
                        } else
                            Toast.makeText(this, getString(R.string.unsupported_format), Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(this, getString(R.string.error_uploading_file), Toast.LENGTH_SHORT).show()
                    }
                } catch (e: URISyntaxException) {
                    e.printStackTrace()
                }
            }
        }
    }

    private fun startScreenShare(data: Intent?) {
        screenShareIcon!!.setBackgroundResource(R.drawable.screen_share_highlight)
        KenanteMediaStreamManager.GetManager().setVideoCapturer(KenanteScreenShare.KenanteScreenCapturer(data, null))
    }

    private fun returnToCamera() {
        isScreenSharing = false
        KenanteMediaStreamManager.GetManager().setVideoCapturer(KenanteCamera.GetCamera())
    }

   /* private fun showFirebaseList() {
        val alert = AlertDialog.Builder(this).create()
        val linearLayout = LinearLayout(this)
        val lv = ListView(this)
        val adapter = FirebaseVideoAdapter(this, firebaseImageVideoArray)
        lv.adapter = adapter
        linearLayout.addView(lv)
        alert.setView(linearLayout)
        lv.onItemClickListener = AdapterView.OnItemClickListener { parent, view, position, id ->
            firebaseModel = firebaseImageVideoArray[position]
            isModerator = true
            startFragment(FIREBASE_SHOW_FRAGMENT, null)
            alert.dismiss()
        }
        alert.show()
    }*/

    override fun startFragment(code: Int, extras: Bundle?) {
        when (code) {
            VIDEO_CALL_FRAGMENT -> {
                val videoCallFragment = VideoCallFragment()

                val bundle = Bundle()
                bundle.putInt(Constants.EXTRA_VIDEO_SHOW_WIDTH, videoWidth)
                bundle.putInt(Constants.EXTRA_VIDEO_SHOW_HEIGHT, videoHeight)
                videoCallFragment.arguments = bundle
                supportFragmentManager
                    .beginTransaction()
                    .replace(R.id.videoShowFL, videoCallFragment, Constants.VIDEO_SHOW_FRAG).commitAllowingStateLoss()

            }

            FIREBASE_HANDLER_FRAGMENT -> {

               /* var firebaseHandlerFragment = supportFragmentManager.findFragmentByTag(Constants.FIREBASE_HANDLER_FRAG)
                if (firebaseHandlerFragment == null) {
                  //  firebaseHandlerFragment = FirebaseHandlerFragment()
                    supportFragmentManager.beginTransaction().add(R.id.videoShowFL, firebaseHandlerFragment, Constants.FIREBASE_HANDLER_FRAG).commitAllowingStateLoss()
                }
*/
            }

            FIREBASE_SHOW_FRAGMENT -> {
               /* val firebaseHandlerFragment = supportFragmentManager.findFragmentByTag(Constants.FIREBASE_HANDLER_FRAG) as FirebaseHandlerFragment?
                if (firebaseHandlerFragment != null) if (firebaseHandlerFragment.isAdded) firebaseHandlerFragment.removeListeners()

                if (firebaseModel!!.type == 2) {
                    val videoCallFragment: VideoCallFragment? = supportFragmentManager.findFragmentByTag(Constants.VIDEO_SHOW_FRAG) as VideoCallFragment?
                    //Todo: Pause audio when opening firebase show fragment
                    //if (videoCallFragment != null) if (videoCallFragment.isAdded) videoCallFragment.pauseAudio()
                }

                //videoShowFL.setVisibility(View.GONE);

                //videoShowFL.setVisibility(View.GONE);
                var firebaseShowFragment = supportFragmentManager.findFragmentByTag(Constants.FIREBASE_SHOW_FRAG) as FirebaseShowFragment?
                if (firebaseShowFragment == null) {
                    firebaseShowFragment = FirebaseShowFragment()
                    val b5 = Bundle()
                    b5.putBoolean(Constants.EXTRA_IS_MOD, isModerator)
                    b5.putSerializable(Constants.EXTRA_FVIDEO_MODEL, firebaseModel)
                    firebaseShowFragment.arguments = b5
                    supportFragmentManager.beginTransaction().add(R.id.callActivityFL, firebaseShowFragment, Constants.FIREBASE_SHOW_FRAG).commitAllowingStateLoss()
                }*/
            }

            USERS_LIST_FRAGMENT -> {

              /*  var usersListFragment = supportFragmentManager.findFragmentByTag(Constants.USERS_LIST_FRAG) as UsersListFragment?
                if (usersListFragment == null || !usersListFragment.isAdded) {
                    usersListFragment = UsersListFragment()
                    *//*val bundle1 = Bundle()
                    bundle1.putSerializable(Constants.CURRENT_PRESENT_USERS, usersOnline as Serializable?)
                    usersListFragment.arguments = bundle1*//*
                    supportFragmentManager.beginTransaction().add(R.id.videoShowFL, usersListFragment, Constants.USERS_LIST_FRAG).commitAllowingStateLoss()
                    //usersListIcon!!.setBackgroundResource(R.drawable.ic_users_highlight)
                } else {
                    usersListFragment.handleVisibility(1)
                    usersListIcon!!.setBackgroundResource(R.drawable.ic_users_highlight)
                }*/

            }

           /* FULL_CHAT_FRAGMENT -> {
                var opponentId = 0
                if (extras != null) opponentId = extras.getInt(Constants.EXTRA_OPPONENT_ID)
                if (!fullScreenChatFragment.isAdded) {
                    val b2 = Bundle()
                    b2.putInt(Constants.EXTRA_OPPONENT_ID, opponentId)
                    fullScreenChatFragment.arguments = b2
                    supportFragmentManager.beginTransaction().add(R.id.videoShowFL, fullScreenChatFragment, Constants.FULL_SCREEN_FRAG).commitAllowingStateLoss()
                } else { //Show Full Screen Fragment
                    if (fullScreenChatFragment.isAdded)
                        fullScreenChatFragment.handleFullChatVisibility(1, opponentId)

                    if (!fragmentsOnTop!!.contains(FULL_CHAT_FRAGMENT)) fragmentsOnTop!!.add(FULL_CHAT_FRAGMENT)
                    if (fragmentsOnTop!!.size == 1) closeButton?.background = resources.getDrawable(R.drawable.back_arrow)
                }

            }

            BOTTOM_CHAT_FRAGMENT -> {
                if (!bottomChatFragment.isAdded) {
                    supportFragmentManager.beginTransaction().add(R.id.bottomChatFragment, bottomChatFragment, Constants.BOTTOM_CHAT_FRAG).commitAllowingStateLoss()
                }
            }*/

            ENLARGE_VIDEO_FRAGMENT -> {
                if (!fragmentsOnTop!!.contains(ENLARGE_VIDEO_FRAGMENT))
                    fragmentsOnTop?.add(ENLARGE_VIDEO_FRAGMENT);
                if (fragmentsOnTop?.size == 1)
                    closeButton?.setBackground(getResources().getDrawable(R.drawable.back_arrow))
            }
        }

    }

    override fun removeFragment(code: Int) {
        when (code) {
            VIDEO_CALL_FRAGMENT -> {
                val fragment1: VideoCallFragment? = supportFragmentManager.findFragmentByTag(Constants.VIDEO_SHOW_FRAG) as VideoCallFragment?
                if (fragment1 != null && fragment1.isAdded) {
                    fragment1.clearAllObjects()
                    supportFragmentManager.beginTransaction().remove(fragment1).commitAllowingStateLoss()
                }
            }

            USERS_LIST_FRAGMENT -> {

                /*if (bottomChatFragment.isAdded) {
                    if (bottomChatFragment.isBottomChatVisible) {
                        bottomChatFragment.hideThisFragment()
                    }
                }
*//*
                val usersListFragment = supportFragmentManager.findFragmentByTag(Constants.USERS_LIST_FRAG) as UsersListFragment?
                if (usersListFragment != null) {
                    if (usersListFragment.isAdded) supportFragmentManager.beginTransaction().remove(usersListFragment).commitAllowingStateLoss()
                }*/

            }

            ENLARGE_VIDEO_FRAGMENT -> {
                /*EnlargedVideoFragment fragment4 = (EnlargedVideoFragment) getSupportFragmentManager().findFragmentByTag(Constants.ENLARGE_VIDEO_FRAG);
                if (fragment4 != null)
                    if (fragment4.isAdded())
                        getSupportFragmentManager().beginTransaction().remove(fragment4).commitAllowingStateLoss();*/
                fragmentsOnTop?.remove(ENLARGE_VIDEO_FRAGMENT);
                if (fragmentsOnTop?.size == 0)
                    closeButton?.setBackground(getResources().getDrawable(R.drawable.cross_icon));
                //fragmentOnTop = VIDEO_CALL_FRAGMENT;

                if (!disconnectFromUser) {
                    val fragment1: VideoCallFragment? = supportFragmentManager.findFragmentByTag(Constants.VIDEO_SHOW_FRAG) as VideoCallFragment?
                    if (fragment1 != null)
                        if (fragment1.isAdded()) {
                            //fragment1.resizeAllViews();
                        }
                }
            }
        }
    }

    override fun hideFragment(code: Int) {
        when (code) {
            /*FIREBASE_SHOW_FRAGMENT -> {

                val fragment3 = supportFragmentManager.findFragmentByTag(Constants.FIREBASE_SHOW_FRAG) as FirebaseShowFragment?
                if (fragment3 != null) if (fragment3.isAdded) supportFragmentManager.beginTransaction().remove(fragment3).commitAllowingStateLoss()

                val firebaseHandlerFragment = supportFragmentManager.findFragmentByTag(Constants.FIREBASE_HANDLER_FRAG) as FirebaseHandlerFragment?
                if (firebaseHandlerFragment != null) if (firebaseHandlerFragment.isAdded) firebaseHandlerFragment.onResume()

                //Todo: Uncomment this code
                *//*if (firebaseModel != null) if (firebaseModel!!.type == 2) {
                    val videoCallFragment1: VideoCallFragment? = supportFragmentManager.findFragmentByTag(Constants.VIDEO_SHOW_FRAG) as VideoCallFragmentTwo?
                    if (videoCallFragment1 != null) if (videoCallFragment1.isAdded()) videoCallFragment1.resumeAudio()
                }*//*

            }*/

            USERS_LIST_FRAGMENT -> {
               /* if (bottomChatFragment.isAdded) {
                    if (bottomChatFragment.isBottomChatVisible) {
                        bottomChatFragment.hideThisFragment()
                    }
                }*/

              /*  val usersListFragment = supportFragmentManager.findFragmentByTag(Constants.USERS_LIST_FRAG) as UsersListFragment?
                if (usersListFragment != null) {
                    if (usersListFragment.isAdded) usersListFragment.handleVisibility(2)
                    usersListIcon!!.setBackgroundResource(R.drawable.ic_users)
                    if (fragmentsOnTop!!.contains(USERS_LIST_FRAGMENT)) fragmentsOnTop!!.remove(USERS_LIST_FRAGMENT)
                    if (fragmentsOnTop!!.size == 0) closeButton!!.background = resources.getDrawable(R.drawable.call_cut_icon)
                }*/

            }

           /* FULL_CHAT_FRAGMENT -> {

                if (fullScreenChatFragment.isAdded) {
                    fullScreenChatFragment.handleFullChatVisibility(2, 0)
                    onStartChat(false)
                    if (fragmentsOnTop!!.contains(FULL_CHAT_FRAGMENT)) fragmentsOnTop!!.remove(FULL_CHAT_FRAGMENT)
                    if (fragmentsOnTop!!.size == 0) closeButton?.background = resources.getDrawable(R.drawable.call_cut_icon)
                }

            }*/

            /*BOTTOM_CHAT_FRAGMENT -> {

                if (bottomChatFragment.isAdded) {
                    if (bottomChatFragment.isBottomChatVisible) {
                        bottomChatFragment.hideThisFragment()
                    }
                }

            }*/

        }
    }

    private fun releaseAllObjectsAndFinish(toKill: Boolean) {
        removeFragment(VIDEO_CALL_FRAGMENT)
      //  removeFragment(USERS_LIST_FRAGMENT)
       // removeFragment(FIREBASE_HANDLER_FRAGMENT)
       // removeFragment(FIREBASE_SHOW_FRAGMENT)
       // removeFragment(ENLARGE_VIDEO_FRAGMENT)
       // removeFragment(FILE_OPENER_FRAGMENT)
        if (toKill)
            finish()
    }

    override fun onTranslatorPresent(present: Boolean?) {
        //TODO("Not yet implemented")
    }

    /*   override fun onTranslatorPresent(present: Boolean?) {
           if (present!!) {
               clientAudioIcon!!.setBackground(getResources().getDrawable(R.drawable.translator_active));
               clientAudioIcon!!.setVisibility(View.VISIBLE)
           } else {
               clientAudioIcon!!.setBackground(getResources().getDrawable(R.drawable.translator_icon_highlight));
               clientAudioIcon!!.setVisibility(View.GONE)
           }
       }*/

    override fun onFragmentStarted(type: Int, started: Boolean?) {
        when (type) {
            RECORD_FRAGMENT -> {
                if (started!!) {
                    startFragment(VIDEO_CALL_FRAGMENT, null)
                    //isRecording = true
                } else {
                    releaseAllObjectsAndFinish(true)
                }
                if (started) {
                    //startFragment(FIREBASE_HANDLER_FRAGMENT, null)
                  //  startFragment(USERS_LIST_FRAGMENT, null)
                }
            }
            VIDEO_CALL_FRAGMENT -> if (started!!) {
              //  startFragment(FIREBASE_HANDLER_FRAGMENT, null)
               // startFragment(USERS_LIST_FRAGMENT, null)
            }
          /*  FIREBASE_HANDLER_FRAGMENT -> *//*if (!isRecordingAvailable) *//* if (started!! && chatAvailable) {
                startFragment(FULL_CHAT_FRAGMENT, null)
                startFragment(BOTTOM_CHAT_FRAGMENT, null)
                startFragment(FILE_OPENER_FRAGMENT, null)
            }*/
        }
    }

    override fun localVideoExists() {
        screenShareIcon!!.visibility = View.VISIBLE
        rotateCameraIcon!!.visibility = View.VISIBLE
    }

    override fun onStartChat(start: Boolean) {
        //fsdjfndksn
    }

    /*override fun onStartChat(start: Boolean) {
        if (start) {
            beginChatText?.visibility = View.INVISIBLE
            sendChatLL?.visibility = View.VISIBLE
        } else {
            beginChatText?.visibility = View.VISIBLE
            sendChatLL?.visibility = View.INVISIBLE
        }
    }*/

   /* override fun startFirebaseShowFragment(model: FirebaseVideoModel?, isMod: Boolean?) {
        //This callback will be invoked for moderator, respondent, client, translator
        Log.e(TAG, "firebase handler callback")
        firebaseModel = model
        isModerator = isMod!!
        startFragment(FIREBASE_SHOW_FRAGMENT, null)
    }*/

    override fun onRemoveFragmentFromStack() {
        //TODO("Not yet implemented")
    }

    override fun onChangeChatOpponent(opponentId: Int) {
        currentChatOpponent = opponentId
        Log.e(TAG, "Chat opponent changed to: $opponentId")
    }

    override fun onCameraSwitched(frontCamera: Boolean?) {
        TODO("Not yet implemented")
    }

    fun showUserConnectionStatusToast(name: String?, connectionStatus: Int) {
        val tv = TextView(this)
        tv.setTextColor(Color.parseColor("#ffffff"))
        tv.textSize = 16f
        tv.background = resources.getDrawable(R.drawable.user_name_background)
        tv.setPadding(10, 10, 10, 10)
        //tv.setBackgroundColor(Color.parseColor("#ffd900"));
        val params = LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        params.setMargins(0, 0, 0, 10)
        tv.layoutParams = params
        var status = ""
        if (connectionStatus == 1) status = "connected" else if (connectionStatus == 2) status = "disconnected" else if (connectionStatus == 3) status = "left"
        tv.text = "$name $status"
        usersConnectedLL?.addView(tv)
        Handler().postDelayed({ usersConnectedLL?.removeView(tv) }, 5000)
    }

   /* override fun onChatJoined() {
        Log.e(TAG, "onChatJoined")
        chatOpen = true
        initializeEditTextChangeListener()
        beginChatText?.text = "Click to begin chat"
    }

    override fun onChatUserConnected(userId: Int, channelName: String) {
        Log.e(TAG, "user: $userId joined")
        chatChannels.put(userId, channelName)
    }

    override fun onChatUserLeft(userId: Int) {
        Log.e(TAG, "user: $userId left")
        chatChannels.remove(userId)
    }

    override fun onMessage(chatMessage: KenanteChatMessage) {
        Log.e(TAG, chatMessage.message)
        if (chatMessage.action == KenanteChatMessageAction.Text ||
            chatMessage.action == KenanteChatMessageAction.Media) {
            insertMessageInDatabase(chatMessage)
        }
        if (fragmentsOnTop!!.size > 0) {
            if (fragmentsOnTop!!.toTypedArray()[fragmentsOnTop!!.size - 1] == FULL_CHAT_FRAGMENT)
                fullScreenChatFragment.processMessage(chatMessage, chatMessage.senderId)
        } else bottomChatFragment.processMessage(chatMessage, chatMessage.senderId)
    }

    override fun onError(message: String?) {
        Log.e(TAG, message)
        chatOpen = false
        beginChatText?.text = "Chat room connection closed. Click to reconnect."
        hideFragment(FULL_CHAT_FRAGMENT)
        hideFragment(BOTTOM_CHAT_FRAGMENT)
    }

    override fun onChatLeft() {
        chatOpen = false
        beginChatText?.text = "You left chat room. Click to reconnect."
        Log.e(TAG, "onChatLeft")
    }*/

    /*private fun initializeEditTextChangeListener() {
        writeChatMessage?.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {}
            override fun onTextChanged(charSequence: CharSequence, i: Int, i1: Int, count: Int) {
                if (count > 0) {
                    sendMessageIB?.background = resources.getDrawable(R.drawable.send_button)
                }
            }

            override fun afterTextChanged(editable: Editable) {}
        })
    }*/

    /*override fun onEditTextBackPressed() {
        val imm = this.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        //Find the currently focused view, so we can grab the correct window token from it.
        //Find the currently focused view, so we can grab the correct window token from it.
        var view = this.currentFocus
        //If no view currently has focus, create a new one, just so we can grab a window token from it
        //If no view currently has focus, create a new one, just so we can grab a window token from it
        if (view == null) {
            view = View(this)
        }
        imm.hideSoftInputFromWindow(view.windowToken, 0)
        //Handler().postDelayed({ requestLayout() }, 500)
    }
*/
   /* private fun askToSendThisAsAttachment(attachmentFile: File) {
        val alert = AlertDialog.Builder(this)
        alert.setMessage("Send this as attachment?")
        alert.setCancelable(false)
        alert.setPositiveButton(getString(R.string.yes)) { dialog, which ->
            uploadAttachment(attachmentFile)
            dialog.cancel()
        }
        alert.setNegativeButton(getString(R.string.no)) { dialog, which -> dialog.cancel() }
        alert.show()
    }

    private fun uploadAttachment(attachmentFile: File) {

        val attachmentPD = ProgressDialog(this)
        attachmentPD.setMessage("Uploading file...")
        attachmentPD.setCancelable(false)
        attachmentPD.setCanceledOnTouchOutside(false)
        attachmentPD.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL)
        attachmentPD.progress = 0
        attachmentPD.max = 100
        attachmentPD.show()

        KenanteTasks.init(applicationContext,aws_key = NewSharedPref.getStringValue(Constants.AWS_KEY)!!,aws_secret_key =NewSharedPref.getStringValue(Constants.AWS_SECRET_KEY)!! )
        KenanteTasks.uploadFile(attachmentFile, object : KenanteChatFileUploadListener {
            override fun onProgress(i: Int) {
                attachmentPD.progress = i
            }

            override fun onSuccess(file: KenanteFile) {
                val attachment = KenanteAttachment(file.fileName, file.extension, file.fileUrl, file.fileType)
                sendMessage("", arrayListOf(attachment))
                attachmentPD.cancel()
            }

            override fun onError(error: String) {
                attachmentPD.cancel()
            }

        })
    }*/

//    private fun sendMessage(message: String, attachments: ArrayList<KenanteAttachment>?) {
//        var chatMessage: KenanteChatMessage? = null
//
//        if (message != "") {
//            chatMessage = KenanteChatMessage(roomId, callData!!.currentUserId,
//                currentChatOpponent, message, KenanteChatMessageAction.Text )
//            chatMessage.channel = chatChannels[chatMessage.receiverId]
//        } else if (attachments != null) {
//            chatMessage = KenanteChatMessage(roomId, callData!!.currentUserId,
//                currentChatOpponent, message, KenanteChatMessageAction.Media)
//            chatMessage.attachments = attachments
//            chatMessage.channel = chatChannels[chatMessage.receiverId]
//        }
//
//        try {
//            chatSession?.sendMessage(chatMessage!!)
//            writeChatMessage?.setText(getString(R.string.empty))
//            insertMessageInDatabase(chatMessage!!)
//            if (fragmentsOnTop!!.size > 0 && fragmentsOnTop!!.toTypedArray()[fragmentsOnTop!!.size - 1] == FULL_CHAT_FRAGMENT)
//                fullScreenChatFragment.showMessage(chatMessage)
//            else if (fragmentsOnTop!!.size == 0 || fragmentsOnTop!!.toTypedArray()[fragmentsOnTop!!.size - 1] == ENLARGE_VIDEO_FRAGMENT)
//                bottomChatFragment.showMessage(chatMessage)
//        } catch (e: Exception) {
//            print(e.message)
//        }
//    }
//
//   /* private fun insertMessageInDatabase(chatMessage: KenanteChatMessage) {
//        db.insertChatMessage(chatMessage)
//    }*/
//
//    private fun sendAttachment() {
//        val intent = Intent(Intent.ACTION_GET_CONTENT)
//        intent.type = "*/*"
//        intent.addCategory(Intent.CATEGORY_OPENABLE)
//        startActivityForResult(Intent.createChooser(intent, "Select a File to Upload"), PICK_FILE)
//    }

    override fun onUserAvailable(userId: String) {
        Log.i("OnUserAvailableToCall",""+userId)

    }

    override fun onUserConnectedToCall(userId: String) {
        Log.i("OnUserConnectedToCall",""+userId)

    }

    override fun onUserDisconnectedFromCall(userId: String) {
        Log.i("UserDisConnectedToCall",""+userId)

    }

    override fun onUserConnectionClosed(userId: String) {
       /* if (bottomChatFragment.isAdded)
            if (bottomChatFragment.isBottomChatVisible)
                bottomChatFragment.userDisconnected(userId)*/
        Log.i("OnUserConnectionClosed",""+userId)

    }

    override fun onBackPressed() {
        if (fragmentsOnTop?.size == 0) {
            disconnectFromUser = true
            chatSession?.leaveChat()
            kenanteSession.leave()
            showLoadingScreen(true, getString(R.string.closing_call))
        } else {
            hideFragment(fragmentsOnTop!!.toTypedArray()[fragmentsOnTop!!.size - 1])
        }
    }

    override fun onStop() {
        super.onStop()
        kenanteSession.unregisterSessionEventListeners(this)
        kenanteSession.unregisterUserCallEventListener(this)
    }

    override fun onLost() {
        if (currentInternetStatus != INTERNET_LOST) {
            Log.e(TAG, "onLost called")
            currentInternetStatus = INTERNET_LOST
            showLoadingScreen(true, "Lost Internet Connection")
            noNetFl?.visibility = View.VISIBLE
            removeFragment(FIREBASE_SHOW_FRAGMENT)
            if (fragmentsOnTop!!.size > 0) {
                fragmentsOnTop!!.clear()
                closeButton?.background = resources.getDrawable(R.drawable.call_cut_icon)
            }
        }
    }

    override fun onUnavailable() {
        Log.e(TAG, "onUnavailable called")
    }

    override fun onLosing(maxMsToLive: Int) {
        Log.e(TAG, "onLosing called")
    }

    override fun onAvailable() {
        if (currentInternetStatus != INTERNET_AVAILABLE) {
            Log.e(TAG, "onAvailable called")
            currentInternetStatus = INTERNET_AVAILABLE
            noNetFl?.visibility = View.GONE
            if (isSessionClosed) {
                rejoinButton!!.visibility = View.VISIBLE
                showLoadingScreen(true, "Call session got closed.")
            } else {
                //kenanteSession.leave()
                rejoinButton?.visibility = View.GONE
                showLoadingScreen(true, "Internet available. Please wait...")
            }
        }
    }

    private fun restartCall() {
        kenanteSession.createSession(object : SessionEventListener {
            override fun onSuccess(message: String) {
                isSessionClosed = false
                startCall()
            }

            override fun onError(error: String) {
                showLoadingScreen(true, error)
                rejoinButton?.visibility = View.VISIBLE
            }

        })
    }
    private fun showDialog(title: String) {
        val dialog = Dialog(this@CallActivity)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCancelable(false)
        dialog.setContentView(R.layout.dialog_thankyou_layout)
        val lp = WindowManager.LayoutParams()
        lp.copyFrom(dialog.window!!.attributes)
        lp.width = WindowManager.LayoutParams.MATCH_PARENT;
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT;

        dialog.window!!.attributes=lp
        val body = dialog.findViewById(R.id.tv_body_title) as TextView
        body.text = title
        val btn_ok = dialog.findViewById(R.id.btn_thanku_ok) as Button
        val btnshareApp = dialog.findViewById(R.id.btn_share_app) as TextView
        btn_ok.setOnClickListener {
            closeCall()
            dialog.dismiss()
        }
        btnshareApp.visibility=View.GONE
        btnshareApp.setOnClickListener {
            dialog.dismiss()
        }
        dialog.show()
    }

    private fun closeCall(){
        if (StaticMethods.isNetworkAvailable(this@CallActivity)) {

            if (fragmentsOnTop?.size == 0) {
                handler.post() {
                    showLoadingScreen(true, getString(R.string.closing_call))
                    disconnectFromUser = true
                    removeFragment(VIDEO_CALL_FRAGMENT)
                    if(chatSession!=null )
                        chatSession?.leaveChat()
                    if(kenanteSession!=null)
                        kenanteSession.leave()

                    releaseAllObjectsAndFinish()
                }
            } else {
                hideFragment(fragmentsOnTop!!.toTypedArray()[fragmentsOnTop!!.size - 1])
            }
        }else{
            releaseAllObjectsAndFinish()
            Log.e(TAG,"Network not available")
        }
    }
}
