package com.varenia.vaarta.util;

import android.app.Application;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkRequest;
import android.os.Build;

import androidx.annotation.RequiresApi;

import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;


@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
public class NetworkConnectionCheck extends ConnectivityManager.NetworkCallback {

    final ConnectivityManager connectivityManager;
    Application context;
    Set<OnConnectivityChangedListener> listeners = new CopyOnWriteArraySet<>();

    public NetworkConnectionCheck(Application context) {
        this.context = context;
        NetworkRequest.Builder builder = new NetworkRequest.Builder();
        NetworkRequest request = builder.build();
        connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivityManager != null)
            connectivityManager.requestNetwork(request, this);

        IntentFilter intentFilter = new IntentFilter("android.intent.action.DEFAULT");
        context.registerReceiver(new NetworkChange(), intentFilter);

    }

    public void registerListener(OnConnectivityChangedListener listener) {
        listeners.add(listener);
    }

    public void unregisterListener(OnConnectivityChangedListener listener) {
        listeners.remove(listener);
    }

    @Override
    public void onAvailable(Network network) {
        super.onAvailable(network);
        context.sendBroadcast(getIntent(Constants.ON_AVAILABLE,-1));
    }

    @Override
    public void onLosing(Network network, int maxMsToLive) {
        super.onLosing(network, maxMsToLive);
        context.sendBroadcast(getIntent(Constants.ON_LOSING, maxMsToLive));
    }

    @Override
    public void onLost(Network network) {
        super.onLost(network);
        context.sendBroadcast(getIntent(Constants.ON_LOST,-1));
    }

    @Override
    public void onUnavailable() {
        super.onUnavailable();
        context.sendBroadcast(getIntent(Constants.ON_UNAVAILABLE,-1));
    }


    private Intent getIntent(String intentExtra, int maxMsToLive) {
        Intent intent = new Intent("android.intent.action.DEFAULT");
        intent.putExtra(intentExtra, intentExtra);
        if(maxMsToLive != -1)
            intent.putExtra(Constants.EXTRA_MAX_MS_TO_LIVE, maxMsToLive);
        return intent;
    }

    public interface OnConnectivityChangedListener {

        void onAvailable();

        void onLosing(int maxMsToLive);

        void onLost();

        void onUnavailable();

    }

    private class NetworkChange extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {

            for (OnConnectivityChangedListener listener : listeners) {
                if (intent.hasExtra(Constants.ON_AVAILABLE)) {
                    if (intent.getStringExtra(Constants.ON_AVAILABLE).equals(Constants.ON_AVAILABLE))
                        listener.onAvailable();
                }
                else if(intent.hasExtra(Constants.ON_LOSING)){
                    if (intent.getStringExtra(Constants.ON_LOSING).equals(Constants.ON_LOSING)){
                        int maxMsToLive = intent.getIntExtra(Constants.EXTRA_MAX_MS_TO_LIVE, -1);
                        listener.onLosing(maxMsToLive);
                    }
                }
                else if(intent.hasExtra(Constants.ON_LOST)){
                    if (intent.getStringExtra(Constants.ON_LOST).equals(Constants.ON_LOST)) {
                        listener.onLost();
                    }
                }
                else if(intent.hasExtra(Constants.ON_UNAVAILABLE)){
                    if (intent.getStringExtra(Constants.ON_UNAVAILABLE).equals(Constants.ON_UNAVAILABLE))
                        listener.onUnavailable();
                }

            }

        }
    }

}
