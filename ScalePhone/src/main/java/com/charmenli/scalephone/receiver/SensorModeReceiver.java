package com.charmenli.scalephone.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.charmenli.scalephone.manager.IndicatorManager;
import com.charmenli.scalephone.manager.SensorController;
import com.charmenli.scalephone.notification.UsingSensorModeNotification;

public class SensorModeReceiver extends BroadcastReceiver {
    public SensorModeReceiver() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        // TODO: This method is called when the BroadcastReceiver is receiving
        // an Intent broadcast.
        UsingSensorModeNotification.cancel(context);
        SensorController.get().stop();
        IndicatorManager.getInstance().showIndicator();
    }
}
