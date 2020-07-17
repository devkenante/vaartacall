package com.varenia.vaarta.interfaces;


public interface FragmentCallbacks {

    //Full Screen Chat
    void onChangeChatOpponent(int opponentId);

    void onStartChat(boolean start);

    void localVideoExists();


    void onFragmentStarted(int type, Boolean started);

    void onTranslatorPresent(Boolean present);

    void onRemoveFragmentFromStack();

    void onCameraSwitched(Boolean frontCamera);

}
