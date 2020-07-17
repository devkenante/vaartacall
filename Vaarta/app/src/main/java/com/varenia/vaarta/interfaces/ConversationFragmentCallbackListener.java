package com.varenia.vaarta.interfaces;

import android.view.MenuItem;

/**
 * Created by tereha on 23.05.16.
 */
public interface ConversationFragmentCallbackListener {

    /*void addCurrentCallStateCallback (CallActivityOptimized.CurrentCallStateCallback currentCallStateCallback);
    void removeCurrentCallStateCallback (CallActivityOptimized.CurrentCallStateCallback currentCallStateCallback);*//*

    void addOnChangeDynamicToggle (CallActivityOptimized.OnChangeDynamicToggle onChangeDynamicCallback);
    void removeOnChangeDynamicToggle (CallActivityOptimized.OnChangeDynamicToggle onChangeDynamicCallback);

    void onSetAudioEnabled(boolean isAudioEnabled);

    void onSetVideoEnabled(boolean isNeedEnableCam);

    void onSwitchAudio();*/

    void onLeaveCurrentSession();

    void onStartJoinConference();

    void onStartScreenSharing(MenuItem menuItem, Boolean enable);
}
