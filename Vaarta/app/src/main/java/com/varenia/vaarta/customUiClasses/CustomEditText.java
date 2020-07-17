package com.varenia.vaarta.customUiClasses;

import android.content.Context;
import android.util.AttributeSet;
import android.view.KeyEvent;

import com.varenia.vaarta.interfaces.EditTextBackPress;


public class CustomEditText extends androidx.appcompat.widget.AppCompatEditText {

    private static EditTextBackPress backPress;

    public CustomEditText(Context context) {
        super(context);
    }

    public CustomEditText(Context context, AttributeSet attribute_set) {
        super(context, attribute_set);
    }

    public CustomEditText(Context context, AttributeSet attribute_set, int def_style_attribute) {
        super(context, attribute_set, def_style_attribute);
    }

    @Override
    public boolean onKeyPreIme(int key_code, KeyEvent event) {
        if (key_code == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_DOWN) {
            backPress.onEditTextBackPressed();
            return true;
        }
        return super.onKeyPreIme(key_code, event);
    }

    public static void setListener(EditTextBackPress listener){
        backPress = listener;
    }

}
