package com.varenia.vaarta.activities;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.dynamiclinks.FirebaseDynamicLinks;
import com.google.firebase.dynamiclinks.PendingDynamicLinkData;
import com.google.firebase.iid.FirebaseInstanceId;
import com.kenante.video.core.KenanteSession;
import com.kenante.video.interfaces.SessionEventListener;
import com.varenia.vaarta.R;
import com.varenia.vaarta.handler_classes.CallData;
import com.varenia.vaarta.models.KenanteUser;
import com.varenia.vaarta.models.UserId;
import com.varenia.vaarta.retrofit.Communicator;
import com.varenia.vaarta.retrofit.RetrofitInterface;
import com.varenia.vaarta.retrofit.response.UserLoginSR;
import com.varenia.vaarta.util.Constants;
import com.varenia.vaarta.util.NetworkConnectionCheck;
import com.varenia.vaarta.util.NewSharedPref;
import com.varenia.vaarta.util.StaticMethods;
import com.varenia.vaarta.util.ThreadExecuter;

import org.jetbrains.annotations.NotNull;

import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;

import kotlin.jvm.internal.Intrinsics;
import kotlin.text.Charsets;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import com.varenia.vaarta.activities.CallActivity;

import static com.varenia.vaarta.util.StaticMethods.getImei;

public class SetupCallWithLink extends AppCompatActivity implements NetworkConnectionCheck.OnConnectivityChangedListener {
    private SharedPreferences sharedPreferences;

    private static final String TAG = SetupCallWithLink.class.getSimpleName();
    private final int START = 1, VERIFIED = 2;
    private final int NO_PERMISSION = 1, INIT_CALL_INTERRUPTED = 2, INIT_CALL = 3;
    private String mobileNumber = "", password = "", dialogId = "", roomName = "", scheduleFlag = "", fcmId = "", imei = "", url = "";
    private int callId = 0;
    //Action flag - 1 (permission) 2 (internet) 3 (retry call)
    private int actionFlag = 0;
    private Boolean isInitCallOngoing = false, permissionsGiven = false, handleJoinButtonStarted = false;
    private  int currentuserid=0;
    //Views
    private LinearLayout dynamicLinkStatusLL;
    private FrameLayout dynamicLinkPermissionFL;
    private TextView dynamicLinkStatusTV, dynamicLinkUserNameTV, actionButton, actionText;
    private ProgressBar progressBar;
    private KenanteSession kenanteSession;

    //Components
    private NewSharedPref newsharedPref;
    private KenanteUser currentUser;
    private CallData callData;
    private BroadcastReceiver receiver;
    private NetworkConnectionCheck networkConnectionCheck;
    private Uri callUri = null;

    //Permissions
    private String[] PERMISSIONS = {
            Manifest.permission.READ_PHONE_STATE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.CAMERA,
        };

    public static void start(Context context, Uri dynamicUri) {
        Intent intent = new Intent(context, SetupCallWithLink.class);
        intent.putExtra(Constants.EXTRA_DYNAMIC_LINK_URI, dynamicUri);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        context.startActivity(intent);
    }

    @Override
    protected void onStart() {
        super.onStart();
        doReceivingWorkHere();
    }

    @Override
    public void onBackPressed() {

    }

    @Override
    protected void onResume() {
        super.onResume();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            networkConnectionCheck.registerListener(this);
        }
        if (StaticMethods.hasPermissions(this, PERMISSIONS))
            permissionsGiven = true;
        else
            permissionsGiven = false;

        if (permissionsGiven) {
            if (actionFlag == INIT_CALL_INTERRUPTED && !isInitCallOngoing) {
                if (StaticMethods.isNetworkAvailable(this)) {
                    isInitCallOngoing = true;
                    runOnUiThread(() -> handleUI(INIT_CALL));
                    initComponents(START);
                }
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            networkConnectionCheck.unregisterListener(this);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setup_call_with_link);

        getIntentExtras();
        initViews();
        if (StaticMethods.hasPermissions(this, PERMISSIONS)) {
            permissionsGiven = true;
            if (StaticMethods.isNetworkAvailable(this)) {
                runOnUiThread(() -> {
                    isInitCallOngoing = true;
                    handleUI(INIT_CALL);
                    initComponents(START);
                });

            } else
                runOnUiThread(() -> handleUI(INIT_CALL_INTERRUPTED));
        } else {
            runOnUiThread(() -> handleUI(NO_PERMISSION));
            StaticMethods.askPermission(this, PERMISSIONS);
        }
        clickListener();

    }
    private void initComponents(int type) {
        switch (type) {
            case START:
                isInitCallOngoing = true;
                newsharedPref = NewSharedPref.INSTANCE;
                sharedPreferences=newsharedPref.GetSharedPreferences(getApplicationContext());

                if (mobileNumber.equals("") && password.equals("") && callId == 0)
                    getFirebaseDynamicLinkValue();
                else {
                    progressBar.setProgress(1);
                    initComponents(VERIFIED);
                   // new StartLogin(url, mobileNumber, password, imei, fcmId).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                }
                break;

            case VERIFIED:
                handleJoinButton();
                break;
        }
    }

    private void getIntentExtras() {
        callUri = getIntent().getParcelableExtra(Constants.EXTRA_DYNAMIC_LINK_URI);
//        Log.e("CallURI",callUri.toString());
    }

    private void initViews() {
        dynamicLinkStatusLL = findViewById(R.id.dynamicLinkStatusLL);
        dynamicLinkPermissionFL = findViewById(R.id.dynamicLinkPermissionFL);
        dynamicLinkUserNameTV = findViewById(R.id.dynamicLinkUserNameTV);
        dynamicLinkStatusTV = dynamicLinkStatusLL.findViewWithTag("statusTV");
        actionText = dynamicLinkPermissionFL.findViewWithTag("actionText");
        actionButton = dynamicLinkPermissionFL.findViewWithTag("actionButton");
        progressBar = dynamicLinkStatusLL.findViewWithTag("progressBar");
        progressBar.setVisibility(View.GONE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            networkConnectionCheck = new NetworkConnectionCheck(getApplication());
        }
    }

    private void clickListener() {
        actionButton.setOnClickListener(view -> {
            if (actionFlag == NO_PERMISSION)
                StaticMethods.askPermission(SetupCallWithLink.this, PERMISSIONS);
            else if (actionFlag == INIT_CALL_INTERRUPTED) {
                if (StaticMethods.hasPermissions(this, PERMISSIONS)) {
                    permissionsGiven = true;
                    if (StaticMethods.isNetworkAvailable(this)) {
                        if (!isInitCallOngoing) {
                            runOnUiThread(() -> {
                                isInitCallOngoing = true;
                                handleUI(INIT_CALL);
                                initComponents(START);
                            });
                        }
                    } else {
                        runOnUiThread(() -> {
                            handleUI(INIT_CALL_INTERRUPTED);
                            Toast.makeText(this, getString(R.string.internet_error), Toast.LENGTH_SHORT).show();
                        });
                        startActivity(new Intent(Settings.ACTION_WIFI_SETTINGS));
                    }
                } else {
                    runOnUiThread(() -> handleUI(NO_PERMISSION));
                    StaticMethods.askPermission(this, PERMISSIONS);
                }
            }
        });
    }

    private void handleUI(int type) {
        switch (type) {
            case NO_PERMISSION:

                actionFlag = NO_PERMISSION;
                isInitCallOngoing = false;
                dynamicLinkPermissionFL.setVisibility(View.VISIBLE);
                dynamicLinkStatusTV.setText("You don't have the required permissions to start this call.");
                actionText.setText("Please allow permissions");
                actionButton.setText("Allow Permission");
                progressBar.setVisibility(View.GONE);
                progressBar.setProgress(0);
                dynamicLinkUserNameTV.setText("");

                break;

            case INIT_CALL_INTERRUPTED:

                actionFlag = INIT_CALL_INTERRUPTED;
                isInitCallOngoing = false;
                dynamicLinkPermissionFL.setVisibility(View.VISIBLE);
                dynamicLinkStatusTV.setText("Some error occurred while connecting your call. Make sure you are connected to the Internet.");
                actionText.setText("Call Interrupted");
                actionButton.setText("Try Again");
                progressBar.setVisibility(View.GONE);
                progressBar.setProgress(0);
                dynamicLinkUserNameTV.setText("");

                break;

            case INIT_CALL:

                actionFlag = INIT_CALL;
                dynamicLinkPermissionFL.setVisibility(View.GONE);
                actionText.setText("");
                actionButton.setText("");

                progressBar.setVisibility(View.VISIBLE);
                progressBar.setProgress(0);
                dynamicLinkStatusTV.setText(getString(R.string.loading));

                break;

        }
    }

    private void getFirebaseDynamicLinkValue() {

        FirebaseDynamicLinks.getInstance().getDynamicLink(getIntent()).addOnSuccessListener(this, new OnSuccessListener<PendingDynamicLinkData>() {
            @Override
            public void onSuccess(PendingDynamicLinkData pendingDynamicLinkData) {

                Uri deepLink = null;
                if (pendingDynamicLinkData != null) {
                    deepLink = pendingDynamicLinkData.getLink();
                    Log.e(TAG,deepLink.toString());
                    String value = deepLink.toString().split("/")[5];
                    String finalval = decodeString(value);
                    mobileNumber=finalval;
                    progressBar.setProgress(1);
                    NewSharedPref.INSTANCE.setValue(Constants.ROOM_ID, mobileNumber);

                    initComponents(VERIFIED);
                 //   getCurrentUserDetails();
                  //  new StartLogin(url, mobileNumber, password, imei, fcmId).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                } else {
                    runOnUiThread(() -> handleUI(INIT_CALL_INTERRUPTED));
                }
            }
        }).addOnFailureListener(this, new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                e.printStackTrace();
                runOnUiThread(() -> handleUI(INIT_CALL_INTERRUPTED));
            }
        });
    }
    public final String decodeString(@NotNull String encoded) {
        Intrinsics.checkParameterIsNotNull(encoded, "encoded");
        byte[] var10000 = Base64.decode(encoded, 3);
        Intrinsics.checkExpressionValueIsNotNull(var10000, "Base64.decode(encoded, B…RAP or Base64.NO_PADDING)");
        byte[] dataDec = var10000;
        String decodedString = "";
        try {
            Charset var4 = Charsets.UTF_8;
            boolean var5 = false;
            decodedString = new String(dataDec, var4);
            return decodedString;
        } finally {
            Log.e("ERROR","DON't Know what happend");
        }
    }
    private void initRequiredData() {
        url = Constants.LOGIN_URL;
        fcmId = FirebaseInstanceId.getInstance().getToken();
        imei = getImei(SetupCallWithLink.this);
        callData = CallData.getInstance(getApplicationContext());
    }

    private void getCurrentUserDetails() {
        //Store the details of the current user in Shared Preferences.

            Intent intent = new Intent("android.intent.action.MAIN");
            intent.putExtra("status", true);
            intent.putExtra("android.intent.action.MAIN", SetupCallWithLink.this.getPackageName());
            sendBroadcast(intent);

    }

    private void doReceivingWorkHere() {
        IntentFilter intentFilter = new IntentFilter(
                "android.intent.action.MAIN");

        receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (SetupCallWithLink.this.getPackageName().equals(intent.getStringExtra("android.intent.action.MAIN"))) {
                    Boolean status = intent.getBooleanExtra("status", false);
                    if (status) {
                        //subscribeUserForNotifications();
                        progressBar.setProgress(4);


                       // editor.putString(Constants.KID,String.valueOf(currentuserid));
                        //handleJoinButton();

                        //sharedPref.TAGS = roomName;
                        //sharedPref.updateSetting();
                        //    getThisCallData();
                        //new StoreNames(sharedPref.USER_ID, roomName).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                    }
                    else
                        runOnUiThread(() -> handleUI(INIT_CALL_INTERRUPTED));
                }
            }
        };
        registerReceiver(receiver, intentFilter);
    }

    /*private void subscribeUserForNotifications() {
        QBSubscription subscription = new QBSubscription(QBNotificationChannel.GCM);
        subscription.setEnvironment(QBEnvironment.PRODUCTION);
        String deviceId = getDeviceId();
        subscription.setDeviceUdid(deviceId);
        String regId = FirebaseInstanceId.getInstance().getToken();
        subscription.setRegistrationID(regId);
        QBPushNotifications.createSubscription(subscription).performAsync(new QBEntityCallback<ArrayList<QBSubscription>>() {
            @Override
            public void onSuccess(ArrayList<QBSubscription> qbSubscriptions, Bundle bundle) {
                for (QBSubscription subscription : qbSubscriptions) {
                    if (subscription.getDevice().getId() != null)
                        if (subscription.getDevice().getId().equals(deviceId))
                            sharedPref.SUBSCRIPTION_ID = subscription.getId().toString();
                }
                //Store names of users in database according to their usertype
                progressBar.setProgress(5);
                new StoreNames(sharedPref.USER_ID, roomName).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            }

            @Override
            public void onError(QBResponseException e) {
                runOnUiThread(() -> handleUI(INIT_CALL_INTERRUPTED));
            }
        });
    }*/

    private void getThisCallData() {
        Log.e("ConfDetailsFragment", "Getting call data");
        callData.removeAllElements();
       //  KenanteUser currentUser = db.getUserByQBID(currentuserid);
        callData.setCurrentUserId(currentUser.getKid());
        callData.setCurrentUserType(currentUser.getUser_type());
        callData.setCurrentUserName(currentUser.getName());
        callData.setRoomName(roomName);
        SharedPreferences.Editor editor=sharedPreferences.edit();
        editor.putInt(Constants.ROOM_ID,callId);
        editor.putString(Constants.ROOM,roomName);
        editor.apply();
        Runnable callOpponentDataRunnable = this::getCallOpponentData;
      //  Runnable getFirebaseDataRunnable = this::getFirebaseData;

        Thread thread1 = new Thread();
        ThreadExecuter.runButNotOn(callOpponentDataRunnable, thread1);
      //  Thread thread2 = new Thread();
     //   ThreadExecuter.runButNotOn(getFirebaseDataRunnable, thread2);
    }

    private void getCallOpponentData() {
        Log.i(TAG, "Getting call opponent data");
        //if (callData.getUsersToSubsribe().size() == 0)
        Log.i(TAG, "Got all call opponent data");
        progressBar.setProgress(6);
        /*if (callData.getFirebaseData().size() == 0) {

        }*/
    }


    private void handleJoinButton() {
      //  if(handleJoinButtonStarted)
     //       return;
        Log.i(TAG, "Handling join button");
       // handleJoinButtonStarted = true;
        kenanteSession = KenanteSession.Companion.getInstance();

        startCall();
    }
    private void startCall(){

        kenanteSession.createSession(new SessionEventListener() {
            @Override
            public void onSuccess(@NotNull String s) {
                //kenanteSession.initCall(roomId, opponentsIds);
               // handleJoinButtonStarted = false;
                CallActivity.Companion.start(getApplicationContext());
                finish();
            }

            @Override
            public void onError(@NotNull String s) {
               // handleJoinButtonStarted = false;
                Log.e("Error on link call",s);
            }
        });
    }


    /*private void startCall(String dialogId) {
        ConferenceClient client = ConferenceClient.getInstance(SetupCallWithLink.this);
        QBRTCTypes.QBConferenceType conferenceType = QBRTCTypes.QBConferenceType.QB_CONFERENCE_TYPE_VIDEO;

        client.createSession(currentUser.getQb_id(), conferenceType, new ConferenceEntityCallback<ConferenceSession>() {
            @Override
            public void onSuccess(ConferenceSession session) {
                progressBar.setProgress(8);
                runOnUiThread(() -> {
                    dynamicLinkStatusTV.setText("Completed");
                    //progressBar.setVisibility(View.GONE);
                });
                webRtcSessionManager.setCurrentSession(session);
                db.insertAttendedHistory(sharedPref.TAGS);
                Boolean isSecure = scheduleFlag.equals("1");
                CallActivity.start(SetupCallWithLink.this, dialogId, 5, isSecure, true);
                finish();
            }

            @Override
            public void onError(WsException responseException) {
                runOnUiThread(() -> handleUI(INIT_CALL_INTERRUPTED));
            }
        });
    }*/

    @Override
    protected void onStop() {
        super.onStop();
        unregisterReceiver(receiver);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        Boolean allPermissionsGranted = true;
        if (requestCode == Constants.REQUEST_PERMISSION_KEY) {
            for (int i = 0; i < grantResults.length; i++) {
                if (grantResults[i] != PackageManager.PERMISSION_GRANTED)
                    allPermissionsGranted = false;
            }
        }
        if (!allPermissionsGranted) {
            handleUI(NO_PERMISSION);
            permissionsGiven = false;
        } else {
            permissionsGiven = true;
            dynamicLinkPermissionFL.setVisibility(View.GONE);
            if (StaticMethods.isNetworkAvailable(this)) {
                runOnUiThread(() -> {
                    isInitCallOngoing = true;
                    handleUI(INIT_CALL);
                    initComponents(START);
                });

            } else
                runOnUiThread(() -> handleUI(INIT_CALL_INTERRUPTED));
        }
    }

    @Override
    public void onAvailable() {
        if (permissionsGiven)
            if (actionFlag == INIT_CALL_INTERRUPTED && !isInitCallOngoing) {
                isInitCallOngoing = true;
                runOnUiThread(() -> {
                    isInitCallOngoing = true;
                    handleUI(INIT_CALL);
                    initComponents(START);
                });

            }
    }

    @Override
    public void onLosing(int maxMsToLive) {

    }

    @Override
    public void onLost() {
        if (isInitCallOngoing) {
            isInitCallOngoing = false;
            runOnUiThread(() -> handleUI(INIT_CALL_INTERRUPTED));
        }
    }

    @Override
    public void onUnavailable() {

    }

    public class StartLogin extends AsyncTask<String, String, String> {

        String url, mobile, password, imei, fcmId;

        public StartLogin(String url, String mobile, String password, String imei, String fcmId) {
            this.url = url;
            this.mobile = mobile;
            this.password = password;
            this.imei = imei;
            this.fcmId = fcmId;
        }

        @Override
        protected String doInBackground(String... strings) {

          /*  Communicator communicator = new Communicator();
            RetrofitInterface service = communicator.initialization(Constants.LOGIN_URL);
            Call<UserLoginSR> call = service.loginUser(mobile, imei, fcmId);
            call.enqueue(new Callback<UserLoginSR>() {
                @Override
                public void onResponse(Call<UserLoginSR> call, Response<UserLoginSR> response) {

                    *//*On success of login
                    1. Get details of current user.
                    2. Get details of all the other users present in same room.*//*

                    int status = response.body().getStatus();
                    if (status == 1) {
                        //Login Successful
                        UserId userId = response.body().getUserId();
                        //String current_user_id = userId.getUser_id();
                        //sharedPref.USER_ID = current_user_id;
                        //sharedPref.updateSetting();
                        currentuserid=Integer.parseInt(userId.getUser_id());
                        SharedPreferences.Editor editor=sharedPreferences.edit();
                        editor.putString(Constants.KID, userId.getUser_id());
                        editor.putString(Constants.AWS_KEY, userId.getS3Client_key());
                        editor.putString(Constants.AWS_SECRET_KEY, userId.getS3Client_secretKey());
                        editor.apply();
                        //ConnectActivity.start(instance);
                        //Handle all the work of ConnectActivity on LoginActivity
                        progressBar.setProgress(2);
                      //  new SyncUsers(userId.getUser_id()).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

                    } else if (status == 2) {
                        Toast.makeText(SetupCallWithLink.this, getString(R.string.already_logged_in), Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(SetupCallWithLink.this, getString(R.string.failed_login_error), Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<UserLoginSR> call, Throwable t) {
                    runOnUiThread(() -> handleUI(INIT_CALL_INTERRUPTED));
                }
            });
*/
            return "";
        }
    }
/*

    public class SyncUsers extends AsyncTask<String, String, String> {

        String id;

        public SyncUsers(String id) {
            this.id = id;
        }

        @Override
        protected String doInBackground(String... strings) {

            Communicator communicator = new Communicator();
            RetrofitInterface service = communicator.initialization(Constants.LOGIN_URL);
            Call<SyncUsersSR> call = service.syncUsers(id);
            call.enqueue(new Callback<SyncUsersSR>() {
                @Override
                public void onResponse(Call<SyncUsersSR> call, Response<SyncUsersSR> response) {

                    if (response.body() == null)
                        return;
                    int status = response.body().getStatus();
                    if (status == 1) {
                        //Success in getting list.
                        //Store all the users in database.
                        ArrayList<KenanteUser> users = response.body().getResponse();
                        db = DBHelper.getInstance(getApplicationContext());
                        if (users != null) {
                            if (users.size() != 0) {
                                db.insertUsers(users);
                            }
                        }
                        //Now get details of current user and store it in shared preferences.

                        ArrayList<RuleModel> model = response.body().getRuleModel();

                        //Get Rule and store it in database
                        for (int i = 0; i < model.size(); i++) {
                            ArrayList<Integer> see = model.get(i).getSee();
                            ArrayList<Integer> talk = model.get(i).getTalk();
                            ArrayList<Integer> hear = model.get(i).getHear();
                            ArrayList<Integer> chat = model.get(i).getChat();
                            ScheduleModel schedule = model.get(i).getRoom_schedule();
                            int selfVideoOff = model.get(i).getSelf_video_off();
                            int selfAudioOff = model.get(i).getSelf_audio_off();
                            //Storing rule in database.
                            db.storeRule(schedule.getRoom(), see, talk, hear, chat, selfVideoOff, selfAudioOff);
                            db.insertSchedule(schedule);
                            if (model.get(i).getRoom_schedule().getId() == callId) {
                                roomName = model.get(i).getRoom_schedule().getRoom();
                                scheduleFlag = model.get(i).getRoom_schedule().getSecureFlag();
                            }
                        }
                        getCurrentUserDetails();
                        progressBar.setProgress(3);

                    }
                }

                @Override
                public void onFailure(Call<SyncUsersSR> call, Throwable t) {
                    runOnUiThread(() -> handleUI(INIT_CALL_INTERRUPTED));
                }
            });

            return "";
        }
    }
*/

}
