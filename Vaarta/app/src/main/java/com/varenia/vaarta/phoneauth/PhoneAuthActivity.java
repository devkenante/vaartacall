package com.varenia.vaarta.phoneauth;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.FirebaseException;
import com.google.firebase.FirebaseTooManyRequestsException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.varenia.vaart.BaseHome;
import com.varenia.vaarta.R;
import com.varenia.vaarta.models.Room;
import com.varenia.vaarta.models.UserId;
import com.varenia.vaarta.retrofit.Communicator;
import com.varenia.vaarta.retrofit.RetrofitInterface;
import com.varenia.vaarta.retrofit.response.UserLoginSR;
import com.varenia.vaarta.util.Constants;
import com.varenia.vaarta.util.NewSharedPref;
import com.varenia.vaarta.util.StaticMethods;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PhoneAuthActivity extends AppCompatActivity implements View.OnClickListener {
    private static final String TAG = "PhoneAuthActivity";

    private static final String KEY_VERIFY_IN_PROGRESS = "key_verify_in_progress";

    private static final int STATE_INITIALIZED = 1;
    private static final int STATE_CODE_SENT = 2;
    private static final int STATE_VERIFY_FAILED = 3;
    private static final int STATE_VERIFY_SUCCESS = 4;
    private static final int STATE_SIGNIN_FAILED = 5;
    private static final int STATE_SIGNIN_SUCCESS = 6;

    // [START declare_auth]
    private FirebaseAuth mAuth;
    // [END declare_auth]

    private boolean mVerificationInProgress = false;
    private String mVerificationId;
    private PhoneAuthProvider.ForceResendingToken mResendToken;
    private PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks;
    ProgressBar progressBar;
    TextInputEditText editText;
    AppCompatButton buttonSignIn;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_verification_code);
        mAuth=FirebaseAuth.getInstance();
        progressBar = findViewById(R.id.progressbar);
        editText = findViewById(R.id.editTextCode);
        buttonSignIn = findViewById(R.id.buttonSignIn);
        NewSharedPref.INSTANCE.GetSharedPreferences(getApplicationContext());

        String phoneNumber = getIntent().getStringExtra("phoneNumber");


        // save phone number
        SharedPreferences prefs = getApplicationContext().getSharedPreferences("USER_PREF",
                Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString("phoneNumber", phoneNumber);
        editor.apply();
        // [START phone_auth_callbacks]
        mCallbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

            @Override
            public void onVerificationCompleted(PhoneAuthCredential credential) {
                // This callback will be invoked in two situations:
                // 1 - Instant verification. In some cases the phone number can be instantly
                //     verified without needing to send or enter a verification code.
                // 2 - Auto-retrieval. On some devices Google Play services can automatically
                //     detect the incoming verification SMS and perform verification without
                //     user action.
                Log.d(TAG, "onVerificationCompleted:" + credential);
                // [START_EXCLUDE silent]
                mVerificationInProgress = false;
                // [END_EXCLUDE]

                // [START_EXCLUDE silent]
                // Update the UI and attempt sign in with the phone credential
                //updateUI(STATE_VERIFY_SUCCESS, credential);
                // [END_EXCLUDE]
                signInWithPhoneAuthCredential(credential);
            }



            @Override
            public void onVerificationFailed(@NonNull FirebaseException e) {
                // This callback is invoked in an invalid request for verification is made,
                // for instance if the the phone number format is not valid.
                Log.w(TAG, "onVerificationFailed", e);
                // [START_EXCLUDE silent]
                mVerificationInProgress = false;
                // [END_EXCLUDE]

                if (e instanceof FirebaseAuthInvalidCredentialsException) {
                    // Invalid request
                    // [START_EXCLUDE]
                    editText.setError("Invalid phone number.");
                    // [END_EXCLUDE]
                } else if (e instanceof FirebaseTooManyRequestsException) {
                    // The SMS quota for the project has been exceeded
                    // [START_EXCLUDE]
                    Snackbar.make(findViewById(android.R.id.content), "Quota exceeded.",
                            Snackbar.LENGTH_SHORT).show();
                    // [END_EXCLUDE]
                }

                // Show a message and update the UI
                // [START_EXCLUDE]
              //  updateUI(STATE_VERIFY_FAILED);
                // [END_EXCLUDE]
            }

            @Override
            public void onCodeSent(@NonNull String verificationId,
                                   @NonNull PhoneAuthProvider.ForceResendingToken token) {
                // The SMS verification code has been sent to the provided phone number, we
                // now need to ask the user to enter the code and then construct a credential
                // by combining the code with a verification ID.
                Log.d(TAG, "onCodeSent:" + verificationId);

                // Save verification ID and resending token so we can use them later
                mVerificationId = verificationId;
                mResendToken = token;

                // [START_EXCLUDE]
                // Update UI
              //  updateUI(STATE_CODE_SENT);
                // [END_EXCLUDE]
            }
        };

        startPhoneNumberVerification(phoneNumber);
    }
    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();
       // updateUI(currentUser);

        // [START_EXCLUDE]
       /* if (mVerificationInProgress && validatePhoneNumber()) {
            startPhoneNumberVerification(editText.getText().toString());
        }
       */ // [END_EXCLUDE]
    }


    private void startPhoneNumberVerification(String phoneNumber) {
        // [START start_phone_auth]
        PhoneAuthProvider.getInstance().verifyPhoneNumber(
                phoneNumber,        // Phone number to verify
                60,                 // Timeout duration
                TimeUnit.SECONDS,   // Unit of timeout
                this,               // Activity (for callback binding)
                mCallbacks);        // OnVerificationStateChangedCallbacks
        // [END start_phone_auth]

        mVerificationInProgress = true;
    }

    private void verifyPhoneNumberWithCode(String verificationId, String code) {
        // [START verify_with_code]
        PhoneAuthCredential credential = PhoneAuthProvider.getCredential(verificationId, code);
        // [END verify_with_code]
        signInWithPhoneAuthCredential(credential);
    }

    // [START resend_verification]
    private void resendVerificationCode(String phoneNumber,
                                        PhoneAuthProvider.ForceResendingToken token) {
        PhoneAuthProvider.getInstance().verifyPhoneNumber(
                phoneNumber,        // Phone number to verify
                60,                 // Timeout duration
                TimeUnit.SECONDS,   // Unit of timeout
                this,               // Activity (for callback binding)
                mCallbacks,         // OnVerificationStateChangedCallbacks
                token);             // ForceResendingToken from callbacks
    }
    // [END resend_verification]

    // [START sign_in_with_phone]
    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            //@Todo login values with
                            Log.d(TAG, "signInWithCredential:success");

                            FirebaseUser user = task.getResult().getUser();
                            JSONArray data = new JSONArray();
                            try {
                                JSONObject data_main=new JSONObject();
                                data_main.put(Constants.UID,user.getUid());

                                data_main.put(Constants.MOBILE,user.getPhoneNumber());
                                data_main.put(Constants.TOKEN,user.getProviderId());
                                data_main.put(Constants.NAME,user.getDisplayName());
                                data_main.put(Constants.CREATED_AT_LOGiN, StaticMethods.getCurrentTime());
                                data_main.put(Constants.LAST_LOGIN,StaticMethods.getCurrentTime());
                                data.put(0,data_main);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                            /*
                            *[
                                 {
                                  "uid": "hHcudXbquKZpP9pN017ig3hdXw93",
                                  "mobile": "+917665025951",
                                  "token": "not avaiable",
                                  "name": "navl",
                                  "created_at": "not disclosed",
                                  "last_login": "not disclose"
                                }
                                ]
                            * */

                            new StartLogin(data.toString()).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

                            // [START_EXCLUDE]
                           // updateUI(STATE_SIGNIN_SUCCESS, user);
                            // [END_EXCLUDE]
                        } else {
                            // Sign in failed, display a message and update the UI
                            Log.w(TAG, "signInWithCredential:failure", task.getException());
                            if (task.getException() instanceof FirebaseAuthInvalidCredentialsException) {
                                // The verification code entered was invalid
                                // [START_EXCLUDE silent]
                                editText.setError("Invalid code.");
                                // [END_EXCLUDE]
                            }
                            // [START_EXCLUDE silent]
                            // Update UI
                            //updateUI(STATE_SIGNIN_FAILED);
                            // [END_EXCLUDE]
                        }
                    }
                });
    }
    // [END sign_in_with_phone





    private boolean validatePhoneNumber() {
        String phoneNumber = editText.getText().toString();
        if (TextUtils.isEmpty(phoneNumber)) {
            editText.setError("Invalid phone number.");
            return false;
        }

        return true;
    }



    @Override
    public void onClick(View view) {
        switch (view.getId()) {

            case R.id.buttonSignIn:
                String code = editText.getText().toString();
                if (TextUtils.isEmpty(code)) {
                    editText.setError("Cannot be empty.");
                    return;
                }

                verifyPhoneNumberWithCode(mVerificationId, code);
                break;
            case R.id.buttonResend:
                resendVerificationCode(editText.getText().toString(), mResendToken);
                break;
            /*case R.id.signOutButton:
                signOut();
                break;*/
        }
    }

    //Login
    public class StartLogin extends AsyncTask<String, String, String> {

        String  data;

        public StartLogin(String data) {
            this.data = data;
            Log.i(TAG, "Start login service");
        }

        @Override
        protected String doInBackground(String... strings) {

            Communicator communicator = new Communicator();
            RetrofitInterface service = communicator.initialization();
            Call<UserLoginSR> call = service.loginUser(data);

            call.enqueue(new Callback<UserLoginSR>() {
                @Override
                public void onResponse(Call<UserLoginSR> call, Response<UserLoginSR> response) {

                    /*On success of login
                    1. Get details of current user.
                    2. Get details of all the other users present in same room.*/

                    int status = response.body().getStatus();
                    if (status == 1) {
                        Log.i(TAG, "Got login response successful");
                        //Login Successful
                        ArrayList<Room> room = response.body().getResponse();
                        if(room!=null){
                            if(room.size()!=0){
                                NewSharedPref.INSTANCE.setValue(Constants.ROOM_ID, room.get(0).getRoom_id());
                                Intent intent = new Intent(PhoneAuthActivity.this, BaseHome.class);
                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                startActivity(intent);
                            }
                        }
                    } else if (status == 2) {
                        Toast.makeText(PhoneAuthActivity.this, getString(R.string.existinguser), Toast.LENGTH_SHORT).show();
                        //Login Successful
                        ArrayList<Room> room = response.body().getResponse();
                        if(room!=null){
                            if(room.size()!=0){
                                Log.i("ROOM_ID",room.get(0).getRoom_id());
                                NewSharedPref.INSTANCE.setValue(Constants.ROOM_ID, room.get(0).getRoom_id());
                                Intent intent = new Intent(PhoneAuthActivity.this, BaseHome.class);
                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                startActivity(intent);
                            }
                        }
                    } else {
                        Toast.makeText(PhoneAuthActivity.this, getString(R.string.something_went_wrong), Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<UserLoginSR> call, Throwable t) {
                    //setWrongMobilePasswordUI();
                    t.printStackTrace();
                  //  new LogoutUser(NewSharedPref.INSTANCE.getStringValue(Constants.KID)).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                //    Toast.makeText(LoginActivity.this, getString(R.string.connection_failed), Toast.LENGTH_SHORT).show();
               //     dialog.cancel();
                }
            });

            return "";
        }
    }
}
