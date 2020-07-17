package com.varenia.vaarta.activities;

import android.content.Context;
import android.content.Intent;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.varenia.vaarta.R;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Html;
import android.view.View;
import android.widget.TextView;


public class TermsAndConditions extends AppCompatActivity {

    public static void start(Context context){
        Intent i = new Intent(context,TermsAndConditions.class);
        context.startActivity(i);
    }
    private TermsAndConditions instance;
    private TextView tacTV, ppTV;
    private FloatingActionButton goBackFAB;
    private View.OnClickListener onClickListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_terms_and_conditions);
        initViewsAndComponents();
        updateUI();
        clickListener();
    }

    private void initViewsAndComponents(){
        instance = TermsAndConditions.this;
        tacTV = (TextView)findViewById(R.id.tacTV);
        ppTV = (TextView)findViewById(R.id.ppTV);
        goBackFAB = (FloatingActionButton)findViewById(R.id.goBackFAB);
    }

    private void updateUI(){

        tacTV.setText(Html.fromHtml(getString(R.string.tc_part1) + getString(R.string.tc_part2) + getString(R.string.tc_part3)));
        ppTV.setText(Html.fromHtml(getString(R.string.privacy_policy_text)));
    }

    private void clickListener(){
        onClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int id = v.getId();
                switch (id){
                    case R.id.goBackFAB:
                        finish();
                        break;
                }
            }
        };
        goBackFAB.setOnClickListener(onClickListener);
    }

}
