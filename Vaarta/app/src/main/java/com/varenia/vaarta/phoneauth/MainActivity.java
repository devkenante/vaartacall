package com.varenia.vaarta.phoneauth;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;

import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.hbb20.CountryCodePicker;
import com.varenia.vaart.BaseHome;
import com.varenia.vaarta.R;

public class MainActivity extends AppCompatActivity  {

    TextInputEditText editTextPhone;
    CountryCodePicker ccp;
    AppCompatButton buttonContinue;
    boolean isvalid=false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ccp = findViewById(R.id.editTextCountryCode);
        editTextPhone = findViewById(R.id.editTextPhone);
        buttonContinue = findViewById(R.id.buttonContinue);
        ccp.registerCarrierNumberEditText(editTextPhone);

        //buttonContinue.setEnabled(false);
        ccp.setPhoneNumberValidityChangeListener(new CountryCodePicker.PhoneNumberValidityChangeListener() {
            @Override
            public void onValidityChanged(boolean isValidNumber) {
                // your code
                isvalid=isValidNumber;
                if(isvalid){
             //       startVerify();
                }
            }
        });
        buttonContinue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startVerify();
            }
        });

    }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseApp.initializeApp(this);

        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            Intent intent = new Intent(this, BaseHome.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        }
    }

    private void startVerify() {
        String phoneNumber = ccp.getFullNumberWithPlus().trim();

        if (phoneNumber.isEmpty() || !isvalid) {
            editTextPhone.setError("Valid number is required");
            editTextPhone.requestFocus();
            return;
        }


        Intent intent = new Intent(MainActivity.this, PhoneAuthActivity.class);
        intent.putExtra("phoneNumber", phoneNumber);
        startActivity(intent);
    }
}
