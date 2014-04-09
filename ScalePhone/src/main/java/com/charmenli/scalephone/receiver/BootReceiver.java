package com.charmenli.scalephone.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.charmenli.scalephone.service.CoreService;
import com.charmenli.scalephone.util.SharedPreferenceUtils;

public class BootReceiver extends BroadcastReceiver {
    private static final String TAG = BootReceiver.class.getSimpleName();

    public BootReceiver() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        // TODO: This method is called when the BroadcastReceiver is receiving
        // an Intent broadcast.
        Log.d(TAG, "onReceive " + intent);
        if (SharedPreferenceUtils.isAutoBoot()) {
            CoreService.startService(context);
        } else {
            CoreService.stopService(context);
        }
    }
}
