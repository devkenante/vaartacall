package com.varenia.vaarta.activities

import android.content.Intent
import android.content.SharedPreferences
import android.os.AsyncTask
import android.os.Bundle
import android.util.Base64
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.snackbar.Snackbar
import com.kenante.video.core.KenanteSession
import com.kenante.video.interfaces.SessionEventListener
import com.varenia.vaarta.R
import com.varenia.vaarta.phoneauth.MainActivity
import com.varenia.vaarta.retrofit.Communicator
import com.varenia.vaarta.retrofit.RetrofitInterface
import com.varenia.vaarta.retrofit.response.MeetingValidationResp
import com.varenia.vaarta.util.Constants
import com.varenia.vaarta.util.NewSharedPref
import com.varenia.vaarta.util.NewSharedPref.GetSharedPreferences
import com.varenia.vaarta.util.NewSharedPref.setValue
import org.json.JSONArray
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.UnsupportedEncodingException

class StartJoinwithMeetingId : AppCompatActivity(),View.OnClickListener {

    private var startcall : Button?=null

    val TAG = "BASEHOME"
    private var kenanteSession: KenanteSession? = null


    private var joincall : Button?=null
    private var pref :SharedPreferences?=null
    private var edt_meeting_id : EditText?=null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_start_joinwith_meeting_id)
        InitViews()
        InitListeners()
        pref=GetSharedPreferences(applicationContext)

    }

    fun InitViews(){
        startcall=findViewById(R.id.start_call)
        joincall=findViewById(R.id.btn_join_call)
        edt_meeting_id=findViewById(R.id.edt_meeting_id)

    }

    fun InitListeners(){
        startcall?.setOnClickListener(this)

        joincall?.setOnClickListener(this)
    }
    override fun onClick(v: View?) {
        when (v?.id){
            R.id.start_call ->{
                var intent =Intent(applicationContext,MainActivity::class.java)
                intent.flags= Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                startActivity(intent)
            }
            R.id.btn_join_call->{
                if(edt_meeting_id!!.text!=null && !edt_meeting_id!!.text.isEmpty()){
                    var  jsonarray:JSONArray =JSONArray()
                    var jsonObject:JSONObject = JSONObject()
                    jsonObject.put("code", decodeString(edt_meeting_id!!.text.toString()))
                    jsonarray.put(jsonObject)
                    val task =ValidateMeetingID(this)
                    task.execute(jsonarray.toString())
                }else{
                    joincall?.let { Snackbar.make(it,"Meeting id can't left empty",Snackbar.LENGTH_LONG).show() }
                }
            }
        }
    }

    public fun decodeString(encoded: String): String? {
        val dataDec: ByteArray = Base64.decode(encoded, Base64.NO_WRAP or Base64.NO_PADDING)
        var decodedString = ""
        try {
            decodedString = String(dataDec, Charsets.UTF_8)
        } catch (e: UnsupportedEncodingException) {
            e.printStackTrace()
        } finally {
            return decodedString
        }
    }
    fun startCall() {
        kenanteSession = KenanteSession.getInstance()
        kenanteSession!!.createSession(  object : SessionEventListener {
            override fun onSuccess(s: String) {
                CallActivity.start(applicationContext)
            }
            override fun onError(s: String) {
                Log.i("AppCompatActivity",s)
            }
        })
    }
     class ValidateMeetingID(context: StartJoinwithMeetingId) : AsyncTask<String?,String?,String>(){
         var act: StartJoinwithMeetingId = context
         override fun doInBackground(vararg params: String?): String {

            val communicator = Communicator()
            val service: RetrofitInterface = communicator.initialization()
            val call: Call<MeetingValidationResp> = service.MEETING_VALIDATION_RESP_CALL(params[0])
            call.enqueue(object : Callback<MeetingValidationResp> {
                override fun onResponse(
                    call: Call<MeetingValidationResp>,
                    response: Response<MeetingValidationResp>
                ) {

                    /*On success of login
                    1. Get details of current user.
                    2. Get details of all the other users present in same room.*/
                    Log.e("response",response.toString())

                    val status: Int = response.body()!!.getStatus()
                    if (status == 1) {
                        //Login Successful
                        Log.e("response","Status 1")
                        //Login Successful
                        val room = response.body()!!.response
                        if (room != null) {
                            if (room.size != 0) {
                                NewSharedPref.setValue(
                                    Constants.ROOM_ID,
                                    room[0].room_id
                                )
                                act.startCall()
                            }
                        }
                    }else if(status==0){
                        Log.e("response","Status 0")
                    }
                }

                override fun onFailure(
                    call: Call<MeetingValidationResp>,
                    t: Throwable
                ) {
                    Log.e("Response Failure","Validation inturrupted")
                }
            })
            return status.toString()
        }


         override fun onPostExecute(result: String?) {
             super.onPostExecute(result)
             if (result.equals("1")) {
             }
         }
    }

}
