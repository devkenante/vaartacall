package com.varenia.vaarta.adapter;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashSet;

public class ContactsLoader  extends AsyncTask<String,Void,Void> {

    ContactsListAdapter contactsListAdapter;
    Context context;
    private ArrayList<Contact> tempContactHolder;
    public TextView txtProgress;
    int totalContactsCount,loadedContactsCount;


    public ContactsLoader(Context context,ContactsListAdapter contactsListAdapter){
        this.context = context;
        this.contactsListAdapter = contactsListAdapter;
        this.tempContactHolder= new ArrayList<>();
        loadedContactsCount=0;
        totalContactsCount=0;
        txtProgress=null;
    }

    @Override
    protected Void doInBackground(String[] filters) {


        String filter = filters[0];

        ContentResolver contentResolver = context.getContentResolver();

        Uri uri = ContactsContract.Contacts.CONTENT_URI;

        String[] projection = new String[]{
                ContactsContract.Contacts._ID,
                ContactsContract.Contacts.DISPLAY_NAME,
                ContactsContract.Contacts.HAS_PHONE_NUMBER
        };
        Cursor cursor;
        if(filter.length()>0) {
            cursor = contentResolver.query(
                    uri,
                    projection,
                    ContactsContract.Contacts.DISPLAY_NAME + " LIKE ?",
                    new String[]{"%"+filter+"%"},
                    ContactsContract.Contacts.DISPLAY_NAME + " ASC"
            );
        }else {
            //Code changed due to duplicate issue
           /* cursor = contentResolver.query(
                    uri,
                    projection,
                    null,
                    null,
                    ContactsContract.Contacts.DISPLAY_NAME + " ASC"
            );*/
            String selection = ContactsContract.Contacts.IN_VISIBLE_GROUP + " = '"
                    + ("1") + "'";
            String sortOrder = ContactsContract.Contacts.DISPLAY_NAME
                    + " COLLATE LOCALIZED ASC";
            //ContactsContract.Contacts.DISPLAY_NAME + " ASC"
            cursor = contentResolver.query(
                    uri,
                    projection,
                    selection
                            + " AND " + ContactsContract.Contacts.HAS_PHONE_NUMBER
                            + "=1",
                    null,
                    sortOrder
            );
        }
        totalContactsCount = cursor.getCount();
        if(cursor!=null && cursor.getCount()>0){
            HashSet<String> phonehash=new HashSet<>();
            while(cursor.moveToNext()) {
                if (Integer.parseInt(cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER))) > 0) {
                    String id = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts._ID));
                    String name = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
                    Cursor phoneCursor = contentResolver.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                            null,
                            ContactsContract.CommonDataKinds.Phone.CONTACT_ID + "=?",
                            new String[]{id},
                            null
                    );
                    if (phoneCursor != null && phoneCursor.getCount() > 0) {
                        while (phoneCursor.moveToNext()) {
                            String phId = phoneCursor.getString(phoneCursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone._ID));

                            String customLabel = phoneCursor.getString(phoneCursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.LABEL));

                            String label = (String) ContactsContract.CommonDataKinds.Phone.getTypeLabel(context.getResources(),
                                    phoneCursor.getInt(phoneCursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.TYPE)),
                                    customLabel
                            );

                            String phNo = phoneCursor.getString(phoneCursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)).replace(" ","").replace("+","").trim();;
                            Log.d("Value of variable",phNo);
                                if(tempContactHolder.size()>0) {
                                    boolean duplitate=false;
                                    for (int i = 0; i < tempContactHolder.size(); i++) {
                                        if (tempContactHolder.get(i).phone.equalsIgnoreCase(phNo)) {
                                            Log.d("Duplicate contact", tempContactHolder.get(i).phone);
                                            duplitate=true;
                                        }
                                    }
                                    if(!duplitate){
                                        tempContactHolder.add(new Contact(phId, name, phNo, label));
                                    }
                                }else {
                                    tempContactHolder.add(new Contact(phId, name, phNo, label));
                                }
                        }
                        phoneCursor.close();

                    }

                }
                loadedContactsCount++;

                publishProgress();


            }
            cursor.close();
        }

        return null;
    }

    @Override
    protected void onProgressUpdate(Void[] v){

        if(this.tempContactHolder.size()>0) {

            contactsListAdapter.addContacts(tempContactHolder);

            this.tempContactHolder.clear();

            if(txtProgress!=null){
                txtProgress.setVisibility(View.VISIBLE);
                String progressMessage = "Loading...("+loadedContactsCount+"/"+totalContactsCount+")";
                txtProgress.setText(progressMessage);
            }
        }

    }

    @Override
    protected void onPostExecute(Void v){

        contactsListAdapter.addContacts(tempContactHolder);
        tempContactHolder.clear();

        if(txtProgress!=null) {
            txtProgress.setText("");
            txtProgress.setVisibility(View.GONE);
        }
    }
}