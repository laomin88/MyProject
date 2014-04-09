package com.charmenli.scalephone.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;

import com.charmenli.scalephone.manager.SensorController;

public class ScreenEventReceiver extends BroadcastReceiver {
    private static final String TAG = ScreenEventReceiver.class.getSimpleName();
    private static ScreenEventReceiver sReceiver;
    private ScreenEventReceiver() {
    }

    public static ScreenEventReceiver getInstance() {
        if (sReceiver == null) {
            sReceiver = new ScreenEventReceiver();
        }
        return sReceiver;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        // TODO: This method is called when the BroadcastReceiver is receiving
        // an Intent broadcast.
        String action = intent.getAction();
        Log.d(TAG, "onReceive action = " + action);
        if (Intent.ACTION_SCREEN_OFF.equals(action)) {
            SensorController.get().onPause();
        } else {
            SensorController.get().onResume();
        }
    }

    public static void register(Context context) {
        if (sReceiver == null) {
            IntentFilter filter = new IntentFilter();
            filter.addAction(Intent.ACTION_SCREEN_OFF);
            filter.addAction(Intent.ACTION_SCREEN_ON);
            context.registerReceiver(getInstance(), filter);
        }
    }

    public static void unregister(Context context) {
        if (sReceiver != null) {
            context.unregisterReceiver(sReceiver);
            sReceiver = null;
        }
    }
}
