package com.varenia.vaarta.util;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Typeface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.provider.CalendarContract;
import android.telephony.TelephonyManager;
import android.text.Spannable;
import android.text.SpannableString;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.ListAdapter;
import android.widget.ListView;

import androidx.core.app.ActivityCompat;

import com.varenia.vaarta.R;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Hashtable;
import java.util.UUID;

/**
 * Created by VCIMS-PC2 on 03-01-2018.
 */

public class StaticMethods {

    //Converting ArrayList<Integer> to String
    public static String arrayListToString(ArrayList<Integer> arrayList) {
        String s = "";
        for (int i = 0; i < arrayList.size(); i++) {
            if (i == 0) {
                s = String.valueOf(arrayList.get(i));
            } else {
                s = s + "," + String.valueOf(arrayList.get(i));
            }
        }
        return s;
    }

    //Converting String to ArrayList<Integer>
    public static ArrayList<Integer> stringToArrayList(String string) {
        ArrayList<Integer> arrayList = new ArrayList<>();
        String[] s = string.split("//,");
        for (int i = 0; i < s.length; i++) {
            arrayList.add(Integer.valueOf(s[i]));
        }
        return arrayList;
    }
public static String _url;
    public static String  getUrl(String base){
       _url= Constants.MAIN_DYNAMIC_URL+base;
       return _url;
    }
    //To check whether internet is available or not
    public static boolean isNetworkAvailable(Context context) {
        try {
            ConnectivityManager connectivityManager = (ConnectivityManager) context
                    .getSystemService(Context.CONNECTIVITY_SERVICE);

            NetworkInfo networkInfo = connectivityManager
                    .getActiveNetworkInfo();
            boolean connected = networkInfo != null && networkInfo.isAvailable()
                    && networkInfo.isConnected();
            return connected;

        } catch (Exception e) {
            System.out.println("CheckConnectivity Exception: " + e.getMessage());
            Log.v("connectivity", e.toString());
            return false;
        }
    }

    public static String getCurrentTime() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd_kk-mm-ss");
        Calendar c = Calendar.getInstance();
        return sdf.format(c.getTime());
    }

    public static int getAppVersion(Context context) {
        try {
            PackageInfo packageInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
            return packageInfo.versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            // should never happen
            throw new RuntimeException("Could not get package name: " + e);
        }
    }

    public static void shareApp(Context context) {
        Intent sharingIntent = new Intent(Intent.ACTION_SEND);
        sharingIntent.setType("text/plain");
        String shareBody = context.getString(R.string.share_text);
        String shareSub = "Klite";
        sharingIntent.putExtra(Intent.EXTRA_SUBJECT, shareSub);
        sharingIntent.putExtra(Intent.EXTRA_TEXT, shareBody);
        context.startActivity(Intent.createChooser(sharingIntent, "Share using"));
    }

   /* public static void loadChatHistory(QBChatDialog dialog, int skipPagination,
                                       final QBEntityCallback<ArrayList<QBChatMessage>> callback) {
        QBRequestGetBuilder customObjectRequestBuilder = new QBRequestGetBuilder();
        customObjectRequestBuilder.setSkip(skipPagination);
        customObjectRequestBuilder.setLimit(Constants.CHAT_HISTORY_ITEMS_PER_PAGE);
        customObjectRequestBuilder.sortDesc(Constants.CHAT_HISTORY_ITEMS_SORT_FIELD);

        QBRestChatService.getDialogMessages(dialog, customObjectRequestBuilder).performAsync(
                new QbEntityCallbackWrapper<ArrayList<QBChatMessage>>(callback) {
                    @Override
                    public void onSuccess(ArrayList<QBChatMessage> qbChatMessages, Bundle bundle) {

                        callback.onSuccess(qbChatMessages, bundle);

                        *//*Set<Integer> userIds = new HashSet<>();
                        for (QBChatMessage message : qbChatMessages) {
                            userIds.add(message.getSenderId());
                        }

                        if (!userIds.isEmpty()) {
                            getUsersFromMessages(qbChatMessages, userIds, callback);
                        } else {
                            callback.onSuccess(qbChatMessages, bundle);
                        }*//*
                        // Not calling super.onSuccess() because
                        // we're want to load chat users before triggering the callback
                    }

                    @Override
                    public void onError(QBResponseException error) {
                        super.onError(error);
                        callback.onError(error);
                    }
                });
    }

    private static void creatingDialog(Activity activity, ArrayList<Integer> occupantsToAdd, int currentUser, int opponentUser, int currentIteration, int lastIteration) {
        QBChatDialog dialog = DialogUtils.buildDialog("Dialog", QBDialogType.PRIVATE, occupantsToAdd);
        QBRestChatService.createChatDialog(dialog).performAsync(new QBEntityCallback<QBChatDialog>() {
            @Override
            public void onSuccess(QBChatDialog qbChatDialog, Bundle bundle) {
                String dialogId = qbChatDialog.getDialogId();
                DBHelper db = DBHelper.getInstance(activity);
                db.insertChatDialogDetails(currentUser, opponentUser, dialogId);
                if (currentIteration == lastIteration) {
                    //Send Broadcast to Dialogs Activity to proceed further
                    Intent i = new Intent("android.intent.action.MAIN");
                    activity.sendBroadcast(i);
                }
                //new StoreHistory(activity, currentIteration, lastIteration, qbChatDialog).execute();
            }

            @Override
            public void onError(QBResponseException e) {
                Toast.makeText(activity, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }*/

    public static String getDeviceId() {
        String deviceId;
        deviceId = UUID.randomUUID().toString();
        return deviceId;
    }

    public static boolean hasPermissions(Context context, String... permissions) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && context != null && permissions != null) {
            for (String permission : permissions) {
                if (ActivityCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                    return false;
                }
            }
        }
        return true;
    }

    public static void askPermission(Activity activity, String[] PERMISSIONS) {
        if (!hasPermissions(activity, PERMISSIONS)) {
            ActivityCompat.requestPermissions(activity, PERMISSIONS, Constants.REQUEST_PERMISSION_KEY);
        }
    }


    public static Typeface getTypeFaceRobotoBlack(Context context) {
        return Typeface.createFromAsset(context.getResources().getAssets(), "font/roboto_black.ttf");
    }

    public static Typeface getTypeFaceRobotoBold(Context context) {
        return Typeface.createFromAsset(context.getResources().getAssets(), "font/roboto_bold.ttf");
    }

    public static Typeface getTypeFaceRobotoLight(Context context) {
        return Typeface.createFromAsset(context.getResources().getAssets(), "font/roboto_light.ttf");
    }

    public static Typeface getTypeFaceRobotoMedium(Context context) {
        return Typeface.createFromAsset(context.getResources().getAssets(), "font/roboto_medium.ttf");
    }

    public static Typeface getTypeFaceRobotoRegular(Context context) {
        return Typeface.createFromAsset(context.getResources().getAssets(), "font/roboto_regular.ttf");
    }

    public static void changeActivityTitleTypeFace(Activity activity, int code) {
        //Code = 1 for Title
        //Code = 2 for Subtitle
        if (activity.getActionBar() != null) {
            SpannableString string = new SpannableString("");
            if (code == 1)
                string = new SpannableString(activity.getActionBar().getTitle());
            else if (code == 2)
                string = new SpannableString(activity.getActionBar().getSubtitle());
            string.setSpan(new CustomTypefaceSpan("", Typeface.createFromAsset(activity.getResources().getAssets(), "font/roboto_black.ttf")), 0, string.length(),
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            if (code == 1)
                activity.getActionBar().setTitle(string);
            else
                activity.getActionBar().setSubtitle(string);
        }
    }

    public static String getImei(Activity activity) {
        String imei = "";
        TelephonyManager telephonyManager = (TelephonyManager) activity.getSystemService(Context.TELEPHONY_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (ActivityCompat.checkSelfPermission(activity, Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED) {
                imei = telephonyManager.getImei();
            }
        }
        return imei;
    }

    @SuppressLint("MissingPermission")
    public static Hashtable<String, String> listCalendarId(Context c) {

        String projection[] = {"_id", "calendar_displayName"};
        Uri calendars = CalendarContract.Calendars.CONTENT_URI;

        ContentResolver contentResolver = c.getContentResolver();
        Cursor managedCursor = contentResolver.query(calendars, projection, null, null, null);

        if (managedCursor.moveToFirst()) {
            String calName;
            String calID;
            int cont = 0;
            int nameCol = managedCursor.getColumnIndex(projection[1]);
            int idCol = managedCursor.getColumnIndex(projection[0]);

            Hashtable<String, String> calenderIds = new Hashtable<>();
            do {
                calName = managedCursor.getString(nameCol);
                calID = managedCursor.getString(idCol);
                if (!calenderIds.contains(calName))
                    calenderIds.put(calName, calID);
                cont++;
            } while (managedCursor.moveToNext());
            managedCursor.close();

            return calenderIds;
        }

        return null;
    }

    public static Boolean addCalendarEvent(Context context, String roomName, long startDate, long endDate, String calendarId) {

        Boolean successful = false;

        ContentResolver cr = context.getContentResolver();
        ContentValues values = new ContentValues();
        values.put(CalendarContract.Events.CALENDAR_ID, calendarId); // XXX pick)
        values.put(CalendarContract.Events.TITLE, "Reminder for " + roomName);
        values.put(CalendarContract.Events.DTSTART, startDate);
        values.put(CalendarContract.Events.DTEND, endDate);
        values.put(CalendarContract.Events.HAS_ALARM, true);
        values.put(CalendarContract.Events.EVENT_TIMEZONE, CalendarContract.Calendars.CALENDAR_TIME_ZONE);
        values.put(CalendarContract.Events.DESCRIPTION, roomName);

        @SuppressLint("MissingPermission") final Uri uri = cr.insert(CalendarContract.Events.CONTENT_URI, values);

        long dbId = Long.parseLong(uri.getLastPathSegment());

        //Now create a reminder and attach to the reminder

        ContentValues reminders = new ContentValues();
        reminders.put(CalendarContract.Reminders.EVENT_ID, dbId);
        reminders.put(CalendarContract.Reminders.METHOD, CalendarContract.Reminders.METHOD_DEFAULT);
        reminders.put(CalendarContract.Reminders.MINUTES, 0);


        @SuppressLint("MissingPermission") final Uri reminder = cr.insert(CalendarContract.Reminders.CONTENT_URI, reminders);

        int added = Integer.parseInt(reminder.getLastPathSegment());

        if (added > 0) {
            successful = true;
            Intent view = new Intent(Intent.ACTION_VIEW);
            view.setData(uri); // enter the uri of the event not the reminder

            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.KITKAT_WATCH) {
                view.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                        | Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP
                        | Intent.FLAG_ACTIVITY_NO_HISTORY | Intent.FLAG_ACTIVITY_NEW_DOCUMENT);
            } else {
                view.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK |
                        Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP |
                        Intent.FLAG_ACTIVITY_NO_HISTORY |
                        Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
            }
            //view the event in calendar
            context.startActivity(view);
        } else
            successful = false;

        return successful;
    }


    public static int getWidth(int parentWidth, int size) {
        if (size == 1 || size == 2)
            return parentWidth;
        else if (size == 3 || size == 4 || size == 5 || size == 6)
            return (Integer) parentWidth / 2;
        else if (size == 7 || size == 8 || size == 9)
            return (Integer) parentWidth / 3;
        else if (size > 9)
            return (Integer) parentWidth / 4;

        return 0;
    }

    public static int getHeight(int parentHeight, int size) {
        if (size == 1)
            return parentHeight;
        else if (size == 2 || size == 3 || size == 4)
            return (Integer) parentHeight / 2;
        else if (size == 5 || size == 6)
            return (Integer) parentHeight / 3;
        else if (size == 7 || size == 8 || size == 9)
            return (Integer) parentHeight / 3;
        else if (size > 9)
            return (Integer) parentHeight / 4;

        return 0;
    }

    private static String getGMTFormat(String gmt) {
        if (gmt == null)
            return "";
        String returnValue = "GMT", first = "", second = "";
        if (gmt.contains("-")) {
            returnValue = returnValue + "-";
        } else if (gmt.contains("+")) {
            returnValue = returnValue + "+";
            gmt.replace("+", "");
        } else
            returnValue = returnValue + "+";

        String[] s1 = gmt.split("\\.");

        if (s1.length > 0) {
            if (s1[0].length() == 1)
                first = "0" + s1[0];
        }
        if (s1.length > 1) {
            if (s1[1].length() == 1) {
                second = s1[1] + "0";
            }
        } else {
            second = "00";
        }
        returnValue = returnValue + first + ":" + second;
        return returnValue;
    }

    public static void setListViewDynamicHeight(ListView listView) {
        ListAdapter adapter = listView.getAdapter();
        //check adapter if null
        if (adapter == null) {
            return;
        }
        int height = 0;
        int desiredWidth = View.MeasureSpec.makeMeasureSpec(listView.getWidth(), View.MeasureSpec.UNSPECIFIED);
        for (int i = 0; i < adapter.getCount(); i++) {
            View listItem = adapter.getView(i, null, listView);
            listItem.measure(desiredWidth, View.MeasureSpec.UNSPECIFIED);
            height += listItem.getMeasuredHeight();
        }
        ViewGroup.LayoutParams layoutParams = listView.getLayoutParams();
        layoutParams.height = height + (listView.getDividerHeight() * (adapter.getCount() - 1));
        listView.setLayoutParams(layoutParams);
        listView.requestLayout();
    }

    public static void setExpandableListViewDynamicHeight(ExpandableListView listView, int group) {
        ExpandableListAdapter listAdapter = (ExpandableListAdapter) listView.getExpandableListAdapter();
        int totalHeight = 0;
        int desiredWidth = View.MeasureSpec.makeMeasureSpec(listView.getWidth(),
                View.MeasureSpec.EXACTLY);
        for (int i = 0; i < listAdapter.getGroupCount(); i++) {
            View groupItem = listAdapter.getGroupView(i, false, null, listView);
            groupItem.measure(desiredWidth, View.MeasureSpec.UNSPECIFIED);

            totalHeight += groupItem.getMeasuredHeight();

            if (((listView.isGroupExpanded(i)) && (i != group))
                    || ((!listView.isGroupExpanded(i)) && (i == group))) {
                for (int j = 0; j < listAdapter.getChildrenCount(i); j++) {
                    View listItem = listAdapter.getChildView(i, j, false, null,
                            listView);
                    listItem.measure(desiredWidth, View.MeasureSpec.UNSPECIFIED);

                    totalHeight += listItem.getMeasuredHeight();

                }
            }
        }

        ViewGroup.LayoutParams params = listView.getLayoutParams();
        int height = totalHeight
                + (listView.getDividerHeight() * (listAdapter.getGroupCount() - 1));
        if (height < 10)
            height = 200;
        params.height = height;
        listView.setLayoutParams(params);
        listView.requestLayout();

    }

    /*public static class makeChatDialogs extends AsyncTask<String, String, String> {

        Activity activity;
        List<Integer> chatUsers;

        public makeChatDialogs(Activity activity, List<Integer> chatUsers) {
            this.activity = activity;
            this.chatUsers = chatUsers;
        }

        @Override
        protected String doInBackground(String... strings) {

            SharedPref sharedPref = SharedPref.getInstance();
            QBUser currentUser = sharedPref.getCurrentUser();
            int currentUserQBId = currentUser.getId();
            if (chatUsers.size() != 0) {
                for (int i = 0; i < chatUsers.size(); i++) {
                    new AddDialogIdToDatabase(activity, currentUserQBId, chatUsers.get(i), i, chatUsers.size() - 1).execute();
                }
            } else {
                //Send Broadcast to Dialogs Activity to proceed further
                Intent i = new Intent("android.intent.action.MAIN");
                activity.sendBroadcast(i);
            }


            return "";

        }
    }

    public static class AddDialogIdToDatabase extends AsyncTask<String, String, String> {

        Activity activity;
        int currentUser, opponentUser, currentIteration, lastIteration;

        public AddDialogIdToDatabase(Activity activity, int currentUser, int opponentUser, int currentIteration, int lastIteration) {
            this.activity = activity;
            this.currentUser = currentUser;
            this.opponentUser = opponentUser;
            this.currentIteration = currentIteration;
            this.lastIteration = lastIteration;
        }

        @Override
        protected String doInBackground(String... strings) {

            ArrayList<Integer> occupantsToAdd = new ArrayList<>();
            occupantsToAdd.add(currentUser);
            occupantsToAdd.add(opponentUser);
            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    creatingDialog(activity, occupantsToAdd, currentUser, opponentUser, currentIteration, lastIteration);
                }
            });

            return "";
        }
    }*/

    /*public static void downloadFile(Activity activity, InputStream inputStream, String fileName, FullChatAdapter.ViewHolder holder) {
        File storageDirectory = new File(Environment.getExternalStorageDirectory(), Constants.APP_NAME);
        if (!storageDirectory.exists()) {
            if (!storageDirectory.mkdirs()) {
                Log.d(Constants.APP_NAME, "Oops! Failed to create "
                        + Constants.APP_NAME + " directory");
            }
        }
        String fileDir = Environment.getExternalStorageDirectory() + "/" + Constants.APP_NAME + "/" + fileName;
        try {
            OutputStream outputStream = new FileOutputStream(fileDir);
            int read = 0;
            byte[] bytes = new byte[1024];
            while ((read = inputStream.read(bytes)) != -1) {
                outputStream.write(bytes, 0, read);
            }
            //Download done
            //Now update this file local url to database
            DBHelper db = DBHelper.getInstance(activity.getApplicationContext());
            db.insertAttachmentLocalURI(fileDir, fileName);
            activity.runOnUiThread(() -> {
                holder.getAttachmentPB().setVisibility(View.GONE);
                holder.getAttachmentDownloadedIV().setVisibility(View.VISIBLE);
                holder.setFileUrl(fileDir);
            });
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }*/

    /*public static Drawable getTintedDrawable(@NonNull Context context, @NonNull Drawable inputDrawable, @ColorInt int color) {
        Drawable wrapDrawable = DrawableCompat.wrap(inputDrawable);
        DrawableCompat.setTint(wrapDrawable, color);
        DrawableCompat.setTintMode(wrapDrawable, PorterDuff.Mode.SRC_IN);
        return wrapDrawable;
    }*/

}