package com.varenia.vaarta.activities;

import android.content.res.Configuration;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

import com.varenia.vaarta.R;
import com.varenia.vaarta.util.SharedPref;

import java.util.Locale;



public class SplashActivity extends AppCompatActivity {

    private SharedPref sharedPref;
    private Boolean isDynamicCall = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        initialize();
        checkIfDynamicLinkCall();

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (isDynamicCall) {
                    if (getIntent().getData() != null)
                        SetupCallWithLink.start(SplashActivity.this, getIntent().getData());
                } else {
                    if (sharedPref.LOGIN_STATUS) {
                       // DashboardScreen.start(SplashActivity.this);
                    } else Log.i("ad","Af");
                    //    LoginActivity.start(SplashActivity.this);
                }
            }
        }, 1500);

    }

    private void initialize() {
        sharedPref = SharedPref.getInstance();
        initPrefLanguage();
    }

    private void checkIfDynamicLinkCall() {
        if (getIntent() != null) {
            Uri uri = getIntent().getData();
            if (uri != null)
                isDynamicCall = true;
        }
    }

    private void initPrefLanguage() {
        String locale = sharedPref.PREF_LANGUAGE;
        if (locale == null)
            return;
        Locale newLocale;
        if (locale.contains("_")) {
            String[] loc = locale.split("_");
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                newLocale = new Locale.Builder().setLanguage(loc[0]).setRegion(loc[1]).build();
            } else {
                newLocale = new Locale(loc[0], loc[1]);
            }
        } else
            newLocale = new Locale(locale);
        Locale.setDefault(newLocale);

        Resources res = getBaseContext().getResources();
        Configuration config = res.getConfiguration();
        config.setLocale(newLocale);
        getBaseContext().createConfigurationContext(config);
        getResources().updateConfiguration(config, getBaseContext().getResources().getDisplayMetrics());
    }

}
