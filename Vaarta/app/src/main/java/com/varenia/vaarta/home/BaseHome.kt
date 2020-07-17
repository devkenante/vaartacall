package com.varenia.vaart

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.AsyncTask
import android.os.Bundle
import android.os.Handler
import android.os.Parcelable
import android.util.Base64
import android.util.Log
import android.util.Patterns
import android.util.TypedValue
import android.view.View
import android.widget.Button
import android.widget.ListView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.dynamiclinks.ktx.androidParameters
import com.google.firebase.dynamiclinks.ktx.dynamicLink
import com.google.firebase.dynamiclinks.ktx.dynamicLinks
import com.google.firebase.dynamiclinks.ktx.shortLinkAsync
import com.google.firebase.ktx.Firebase
import com.kenante.video.core.KenanteSession
import com.kenante.video.core.KenanteSession.Companion.getInstance
import com.kenante.video.interfaces.SessionEventListener
import com.varenia.kenante_core.core.KenanteSettings
import com.varenia.vaarta.R
import com.varenia.vaarta.activities.CallActivity
import com.varenia.vaarta.adapter.Contact
import com.varenia.vaarta.adapter.ContactsList
import com.varenia.vaarta.adapter.ContactsListAdapter
import com.varenia.vaarta.adapter.ContactsLoader
import com.varenia.vaarta.retrofit.Communicator
import com.varenia.vaarta.retrofit.RetrofitInterface
import com.varenia.vaarta.retrofit.response.MeetingInvitationResp
import com.varenia.vaarta.util.Constants
import com.varenia.vaarta.util.DylinkCreator
import com.varenia.vaarta.util.NewSharedPref
import com.varenia.vaarta.util.NewSharedPref.GetSharedPreferences
import com.varenia.vaarta.util.StaticMethods
import org.json.JSONArray
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.UnsupportedEncodingException


class BaseHome : AppCompatActivity(),View. OnClickListener{

    val TAG = "BASEHOME"
    private var kenanteSession: KenanteSession? = null
    var listView:ListView?=null
    var txtLoadInfo: TextView? = null
    var contactsListAdapter: ContactsListAdapter? = null
    var contactsLoader: ContactsLoader? = null
    var extended_videocall : ExtendedFloatingActionButton ?= null
    var  extButton :ExtendedFloatingActionButton ?=null
    var inputtextlayout: TextInputLayout?=null
    var edttextinput: TextInputEditText?=null
    var tag_group:ChipGroup?=null
    var add_invite_button:Button?=null
    var send_invite_button:Button?=null
    var CONTACT_PICK_REQUEST=100
    var sendinviteArraylist=ArrayList<Contact>()
    val handler = Handler(KenanteSettings.getInstance().getContext()?.mainLooper!!)

    companion object {
        fun start(context: Context) {
            val intent = Intent(context, BaseHome::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            context.startActivity(intent)
        }
    }
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
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_base_home2)
        GetSharedPreferences(applicationContext)
        handler.post() {
            checkAllpermissions()
        }
        intializeView()
        intializeClickListener()
        sendinviteArraylist= ArrayList()
        var contactsList:ContactsList?=ContactsList()

        contactsListAdapter = ContactsListAdapter(this, contactsList)
        listView!!.setAdapter(contactsListAdapter)

        inputtextlayout!!.setEndIconOnClickListener(View.OnClickListener {
            Log.i("icon","clicked")
            loadContacts("");

        })
    }

    private fun checkAllpermissions() {
        //TODO("Not yet implemented")
        if (StaticMethods.hasPermissions(this@BaseHome, *PERMISSIONS)) {

        } else {
            StaticMethods.askPermission(this@BaseHome, PERMISSIONS)
        }
    }

    private fun intializeClickListener() {
        extended_videocall?.setOnClickListener(this)
        extButton?.setOnClickListener(this)
        add_invite_button?.setOnClickListener(this)
        send_invite_button?.setOnClickListener(this)
    }

    private fun intializeView() {
        extended_videocall=findViewById(R.id.extended_videocall)
        extButton=findViewById(R.id.textButton)
        inputtextlayout=findViewById(R.id.textinput_invite)
        edttextinput=findViewById(R.id.text_input_edt_invite)
        tag_group=findViewById(R.id.tag_group)
        add_invite_button=findViewById(R.id.add_invite_list)
        send_invite_button=findViewById(R.id.send_invite)
        listView=findViewById(R.id.list_contats)
        txtLoadInfo=findViewById(R.id.txtLoadInfo)
    }
    private fun loadContacts(filter: String) {
        var filter: String? = filter
        if (contactsLoader != null && contactsLoader!!.status != AsyncTask.Status.FINISHED) {
            try {
                contactsLoader!!.cancel(true)
            } catch (e: Exception) {
            }
        }
        if (filter == null) filter = ""
        try {
            //Running AsyncLoader with adapter and  filter
            contactsLoader = ContactsLoader(this, contactsListAdapter)
            contactsLoader!!.txtProgress = txtLoadInfo
            contactsLoader!!.execute(filter)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onClick(v: View?) {
        when(v?.id) {
            R.id.extended_videocall -> {
                startCall()
            }
            R.id.textButton -> {
               // GetDynamicLink()
            }
            R.id.add_invite_list -> {
                if (edttextinput!!.length() > 0 || !contactsListAdapter!!.selectedContactsList.contactArrayList.isEmpty()) {

                        if(edttextinput!!.length()==10 && isValidMobile(edttextinput!!.text.toString())){
                            var contact = Contact( "4001", "NA", edttextinput!!.text.toString(), "custom")
                            sendinviteArraylist.add(contact)

                        }else{
                            Toast.makeText(this,"No custom phone",Toast.LENGTH_LONG).show()
                        }
                    if (contactsListAdapter!!.selectedContactsList.contactArrayList.isEmpty() && sendinviteArraylist.isEmpty()) {
                        Toast.makeText(
                            applicationContext,
                            "No one to add please add any people from contact or custom",
                            Toast.LENGTH_LONG
                        ).show()
                    } else {
                        if(sendinviteArraylist.size>0) {
                            for (i in 0 until sendinviteArraylist.size)
                                if (! contactsListAdapter!!.selectedContactsList.contactArrayList.contains(sendinviteArraylist.get(i)))
                                    AddtoChips(contactsListAdapter!!.selectedContactsList.contactArrayList)
                                    else{
                                    Toast.makeText(
                                        this@BaseHome,
                                        "Already added to invitelist",
                                        Toast.LENGTH_LONG
                                    ).show()
                                    //contactsListAdapter!!.selectedContactsList.contactArrayList.remove(sendinviteArraylist.get(i))
                                }
                        } else {
                            AddtoChips(contactsListAdapter!!.selectedContactsList.contactArrayList)
                        }
                    }
                } else {
                    Toast.makeText(
                        applicationContext,
                        "No one to add please add any people from contact",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }

            R.id.send_invite -> {
                //Todo Calling Api
                var task=GetDynamicLink(this)
                task.execute()

            }
        }
    }
    private fun isValidMobile(phone: String): Boolean {
        return Patterns.PHONE.matcher(phone).matches()
    }
    private fun getContactJson(): String {
        var Completejson=""
        if(sendinviteArraylist.size>0){
            var jsonArray:JSONArray=JSONArray()
            for (i in 0 until sendinviteArraylist!!.size) {
                var jsonmobile:JSONObject= JSONObject()
                var value=sendinviteArraylist[i].phone.toString().replace(" ","").replace("+","").trimIndent().trim()
                jsonArray.put(jsonmobile.put(Constants.MOBILE,value))
            }
            Completejson=jsonArray.toString()
            Log.e("JSON CONTACT STRING",Completejson.toString())
        }else{
            Completejson="Nothing to add"

        }
        return Completejson
    }

    private fun AddtoChips(contactArrayList: ArrayList<Contact>?) {

        var display = ""
        tag_group!!.removeAllViews()
        if (!sendinviteArraylist.isEmpty()){
            for (i in 0 until sendinviteArraylist.size) {
                contactArrayList!!.add(sendinviteArraylist[i])
            }
        }
        sendinviteArraylist.clear()
        if (contactArrayList != null) {
            sendinviteArraylist=contactArrayList
        }
        for (i in 0 until contactArrayList!!.size) {
            val chip = Chip(this)
            val paddingDp = TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, 10f,
                resources.displayMetrics
            ).toInt()
            chip.setPadding(paddingDp, paddingDp, paddingDp, paddingDp)
            chip.setCloseIconResource(R.drawable.cross_icon)
            chip.isCloseIconEnabled = true
            //Added click listener on close icon to remove tag from ChipGroup
            //Added click listener on close icon to remove tag from ChipGroup
            chip.setOnCloseIconClickListener {
                // tagList.remove(tagName)
                //  chipGroup.removeView(chip)
            }
            display += """
                    ${i + 1}. ${contactArrayList[i].toString()}
                    
                    """.trimIndent()
            chip.setText(contactArrayList[i].toString())
            chip.id=i
            tag_group!!.addView(chip,i)
        }

        txtLoadInfo!!.setText("Selected Contacts : \n\n$display")

    }

    fun getBase64(input: String): String? {

        var data = input.toByteArray(Charsets.UTF_8)
        return  Base64.encodeToString(
            data,
            Base64.NO_WRAP or Base64.NO_PADDING
        )

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

    class GetDynamicLink(context: BaseHome) : AsyncTask<String?,String?,String>(){
        var act: BaseHome = context
        override fun doInBackground(vararg params: String?): String {

            var text = NewSharedPref.getStringValue(Constants.ROOM_ID)
            Log.i("String BASE64",text)

            var message=""
            var base64= text?.let { act.getBase64(it) }
            Log.i("ENCODED BASE64",base64)
            Log.i("DECODED BASE64",act.decodeString(base64.toString()))
            var url_value=Constants.MAIN_DYNAMIC_URL+ base64
            // var url_value="https://vareniacims.com/"+"siteadmin"
            var _uri=Uri.parse(url_value)
            //val uri=appendUri(Constants.MAIN_DYNAMIC_URL,base64.toString())
            Log.i("Created BASE64 url ",_uri.toString())

            Log.i("Creator Link",DylinkCreator.CreateLink(base64).toString())
            val dynamicLink = Firebase.dynamicLinks.dynamicLink {
                link = _uri
                domainUriPrefix = "https://vaartaz.page.link/"
                // Open links with this app on Android
                androidParameters (act.packageName){
                }
                // Open links with com.example.ios on iOS
                //iosParameters("com.example.ios") { }
            }

            val dynamicLinkUri = dynamicLink.uri
            Log.e("APP URI",dynamicLinkUri.toString())

            //  extButton?.setText(dynamicLinkUri.toString())
            val shortLinkTask = Firebase.dynamicLinks.shortLinkAsync {
                longLink = dynamicLinkUri
            }.addOnSuccessListener { result ->
                // Short link created
                val shortLink = result.shortLink
                val flowchartLink = result.previewLink

                Log.e(shortLink.toString(),flowchartLink.toString())
                message="Hello "+NewSharedPref.GetSharedPreferences(act.applicationContext).getString(Constants.ROOM_ID,"0")+ " is waiting to have a live video call with you. Enter Meeting ID: \n "+ "$base64 or click on link \n"+shortLink.toString()
                Log.e("final data link",message)

                /* val sendIntent: Intent = Intent().apply {
                     action = Intent.ACTION_SEND
                     putExtra(Intent.EXTRA_TEXT, _ExtraString)
                     type = "text/plain"
                 }
                 Log.e("final data link",_ExtraString)

                 val shareIntent = Intent.createChooser(sendIntent, null)
                 startActivity(shareIntent)
        */
                var contactjson=act.getContactJson()
                val task=SendMeetingInvite(act)
                task.execute(contactjson,message)
            }.addOnFailureListener {
                // Error
                // ...
                message="something went wrong"
            }
            return message
        }


        override fun onPostExecute(result: String?) {
            super.onPostExecute(result)
            if (result.equals("1")) {
            }
        }
    }


    private fun startCall() {
        kenanteSession = getInstance()
        var roomId= //NewSharedPref.getStringValue(Constants.ROOM_ID)!!
        kenanteSession!!.createSession(  object : SessionEventListener {
            override fun onSuccess(s: String) {
                     CallActivity.start(applicationContext)
            }
            override fun onError(s: String) {
                Log.i(TAG,s)
            }
        })
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if ( resultCode == Activity.RESULT_OK) {
            val selectedContacts: ArrayList<Parcelable>? =
                data!!.getParcelableArrayListExtra<Parcelable>("SelectedContacts")
            var display = ""
            val chip = Chip(this)
            val paddingDp = TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, 10f,
                resources.displayMetrics
            ).toInt()
            chip.setPadding(paddingDp, paddingDp, paddingDp, paddingDp)
            chip.setCloseIconResource(R.drawable.cross_icon)
            chip.isCloseIconEnabled = true
            //Added click listener on close icon to remove tag from ChipGroup
            //Added click listener on close icon to remove tag from ChipGroup
            chip.setOnCloseIconClickListener {
                 // tagList.remove(tagName)
                //  chipGroup.removeView(chip)
            }

            for (i in 0 until selectedContacts!!.size) {
                display += """
                    ${i + 1}. ${selectedContacts[i].toString()}
                    
                    """.trimIndent()
                chip.setText(selectedContacts[i].toString())
                tag_group!!.addView(chip)
            }

            txtLoadInfo!!.setText("Selected Contacts : \n\n$display")
        }
    }
    class SendMeetingInvite(context: BaseHome) : AsyncTask<String?,String?,String>(){
        var act: BaseHome = context
        override fun doInBackground(vararg params: String?): String {

            val communicator = Communicator()
            val service: RetrofitInterface = communicator.initialization()
            val call: Call<MeetingInvitationResp> = service.MEETING_INVITATATION_RESP_CALL(params[0],params[1])
            call.enqueue(object : Callback<MeetingInvitationResp> {
                override fun onResponse(
                    call: Call<MeetingInvitationResp>,
                    response: Response<MeetingInvitationResp>
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
                        Toast.makeText(act,"Sucessfully invitation Sent",Toast.LENGTH_LONG).show()

                    }else if(status==0){
                        Log.e("response","Status 0")
                    }
                }

                override fun onFailure(
                    call: Call<MeetingInvitationResp>,
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
