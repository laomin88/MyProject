package com.charmenli.scalephone.manager;

import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.charmenli.scalephone.util.PrefConf;
import com.charmenli.scalephone.util.SharedPreferenceUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by charmenli on 2014/4/2.
 */
public class SensorController {

    private static final String TAG = SensorController.class.getSimpleName();

    private static SensorController sInstance;
    private SensorManager mSensorManager;
    private List<SensorListenerHolder> mRegisterListenerHolders = new LinkedList<SensorListenerHolder>();

    private Context mContext;

    private SensorController() {

    }

    public static SensorController get() {
        if (sInstance == null) {
            sInstance = new SensorController();
        }
        return sInstance;
    }

    public SensorController init(Context context) {
        mContext = context;
        initialSensors();
        return sInstance;
    }

    private void checkInit() {
        if (mContext == null) {
            throw new IllegalStateException("Please call init first!");
        }
    }

    private void initialSensors() {
        mSensorManager = (SensorManager) mContext.getSystemService(Context.SENSOR_SERVICE);
    }

    public void stop() {
        unregisterAllListeners();
    }

    public void start() {
        registerAccelerateListener(mAccelerateEventListener);
    }

    public void onPause() {
        for (SensorListenerHolder holder : mRegisterListenerHolders) {
            mSensorManager.unregisterListener(holder.listener);
        }
    }

    public void onResume() {
        for (SensorListenerHolder holder : mRegisterListenerHolders) {
            mSensorManager.registerListener(holder.listener, mSensorManager.getDefaultSensor(holder.type), holder.rateUs);
        }
    }

    public void registerListener(SensorEventListener listener, int type, int rateUs) {
        mRegisterListenerHolders.add(new SensorListenerHolder(type, listener, rateUs));
        mSensorManager.registerListener(listener, mSensorManager.getDefaultSensor(type), rateUs);
    }

    public void registerListener(SensorEventListener listener, int type) {
        registerListener(listener, type, SensorManager.SENSOR_DELAY_NORMAL);
    }

    public void registerAccelerateListener(SensorEventListener listener, int rateUs) {
        registerListener(listener, Sensor.TYPE_ACCELEROMETER, rateUs);
    }

    public void registerAccelerateListener(SensorEventListener listener) {
        registerAccelerateListener(listener, SensorManager.SENSOR_DELAY_NORMAL);
    }

    public void unregisterListener(SensorEventListener listener) {
        int index = mRegisterListenerHolders.indexOf(listener);
        if (index >= 0) {
            mRegisterListenerHolders.remove(index);
        }
        mSensorManager.unregisterListener(listener);
    }

    public void unregisterAllListeners() {
        for (SensorListenerHolder holder : mRegisterListenerHolders) {
            mSensorManager.unregisterListener(holder.listener);
        }
        mRegisterListenerHolders.clear();
    }


    class MySensorEventListener implements SensorEventListener {
        long eventTime = -1;
        final int INVALID = -9999;
        final int SAMPLES = 7;
        final float PAGE_EVENT_VAL = 4;
        final float PAGE_RECORD_VAL = 3;
        final long EVENT_INTERVAL_TIME = 2000;
        final long EVENT_STATISTICS_TIME = 800;

        SensorEvent recordEvent = null;
        List<SensorEvent> historySensorEvents = new ArrayList<SensorEvent>(SAMPLES);
        Handler handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                float eventVal = (Float) msg.obj;
                float recordVal = recordEvent.values[0];
                Log.d(TAG, "eventVal = " + eventVal + ", recordVal = " + Arrays.toString(recordEvent.values));
                String pkgName = getCurAppPackageName();
                if (pkgName != null) {
                    int turnPageType = SharedPreferenceUtils.getTurnPageType(pkgName);
                    if (eventVal > PAGE_EVENT_VAL && recordVal < PAGE_RECORD_VAL) {
                        Log.d(TAG, "后翻页");
                        if (turnPageType == PrefConf.VAL_TURN_PAGE_DOWN_UP) {
                            MonkeyManager.get().getMonkeyActionExecutor().turnDown(mContext);
                        } else {
                            MonkeyManager.get().getMonkeyActionExecutor().turnLeft(mContext);
                        }

                    } else if (eventVal < -PAGE_EVENT_VAL && recordVal > -PAGE_RECORD_VAL) {
                        Log.d(TAG, "前翻页");
                        if (turnPageType == PrefConf.VAL_TURN_PAGE_DOWN_UP) {
                            MonkeyManager.get().getMonkeyActionExecutor().turnUp(mContext);
                        } else {
                            MonkeyManager.get().getMonkeyActionExecutor().turnRight(mContext);
                        }
                    }
                }

            }
        };

        @Override
        public void onSensorChanged(SensorEvent event) {
            if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
//                Log.d(TAG, "onSensorChanged values = " + Arrays.toString(event.values));
                recordEvent = event;
                if (System.currentTimeMillis() - eventTime > EVENT_INTERVAL_TIME) {
                    float average = calc(event);
                    if (Math.abs(average) >= PAGE_RECORD_VAL && average != INVALID) {
                        eventTime = System.currentTimeMillis();
                        Message msg = Message.obtain();
                        msg.obj = average;
                        Log.d(TAG, "msg obj = " + msg.obj + ", happend values = " + Arrays.toString(event.values));
                        handler.sendMessageDelayed(msg, EVENT_STATISTICS_TIME);
                    }
                }
            }
        }

        private float calc(SensorEvent event) {
            float ret = INVALID;
            historySensorEvents.add(event);
            if (historySensorEvents.size() == SAMPLES) {
                float total = 0;
                float max = -Integer.MAX_VALUE;
                SensorEvent maxEvent = null;
                float min = Integer.MAX_VALUE;
                SensorEvent minEvent = null;
                for (SensorEvent e : historySensorEvents) {
                    if (e.values[0] > max) {
                        max = e.values[0];
                        maxEvent = e;
                    }
                    if (e.values[0] < min) {
                        min = e.values[0];
                        minEvent = e;
                    }
                }
                historySensorEvents.remove(maxEvent);
                historySensorEvents.remove(minEvent);
                for (SensorEvent e : historySensorEvents) {
                    total += e.values[0];
                }
                float average = total / historySensorEvents.size();
                ret = average;
                historySensorEvents.clear();
            }
            return ret;
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {
            Log.d(TAG, "onAccuracyChanged accuracy = " + accuracy);
        }
    }

    private MySensorEventListener mAccelerateEventListener = new MySensorEventListener();


    class SensorListenerHolder {
        int type;
        SensorEventListener listener;
        int rateUs;

        SensorListenerHolder(int type, SensorEventListener listener, int rateUs) {
            if (listener == null) {
                throw new IllegalArgumentException("listener is null!");
            }
            this.type = type;
            this.listener = listener;
            this.rateUs = rateUs;
        }

        SensorListenerHolder() {
        }


        @Override
        public boolean equals(Object o) {
            return listener.equals(o);
        }
    }

    private String getCurAppPackageName() {
        ActivityManager am = (ActivityManager) mContext.getSystemService(Context.ACTIVITY_SERVICE);
        ComponentName componentName = am.getRunningTasks(1).get(0).topActivity;
        if (componentName != null) {
            return componentName.getPackageName();
        }
        return null;
    }
}
