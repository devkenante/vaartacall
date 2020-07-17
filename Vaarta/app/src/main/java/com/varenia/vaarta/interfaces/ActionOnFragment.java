package com.varenia.vaarta.interfaces;

import android.os.Bundle;

public interface ActionOnFragment {
    
    void startFragment(int code, Bundle extras);
    void removeFragment(int code);
    void hideFragment(int code);
    
}
