package com.varenia.vaarta.util;

import android.app.Application;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;

import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

public class BatteryStatus {

    Application context;
    Set<BatteryCallBack> listeners = new CopyOnWriteArraySet<>();

    public BatteryStatus(Application context) {
        this.context = context;
        IntentFilter intentFilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        context.registerReceiver(new BatteryPercentage(), intentFilter);
    }

    public void registerReceiver(BatteryCallBack listener) {
        listeners.add(listener);
    }

    public void unregisterReceiver(BatteryCallBack listener) {
        listeners.remove(listener);
    }

    public interface BatteryCallBack {
        void onBatteryChange(int percentage, boolean charing);
    }

    private class BatteryPercentage extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {

            int batteryPercentage = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, 0);
            int batteryStatus = intent.getIntExtra(BatteryManager.EXTRA_STATUS,-1);
            Boolean charging = false;

            if(batteryStatus == BatteryManager.BATTERY_STATUS_CHARGING)
                charging = true;

            if (batteryPercentage != 0)
                for (BatteryCallBack listener : listeners) {
                    listener.onBatteryChange(batteryPercentage, charging);
                }
        }
    }

}
