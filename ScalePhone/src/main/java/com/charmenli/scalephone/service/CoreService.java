package com.charmenli.scalephone.service;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;

import com.charmenli.scalephone.manager.MonkeyManager;

import java.io.IOException;

public class CoreService extends Service {
    public CoreService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        new Thread(){
            @Override
            public void run() {
                try {
                    MonkeyManager.get().startMonkeyServer();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }.start();
        return super.onStartCommand(intent, flags, startId);
    }

    public static void startService(Context context) {
        Intent intent = new Intent(context, CoreService.class);
        context.startService(intent);
    }

    public static void stopService(Context context) {
        Intent intent = new Intent(context, CoreService.class);
        context.stopService(intent);
    }
}
