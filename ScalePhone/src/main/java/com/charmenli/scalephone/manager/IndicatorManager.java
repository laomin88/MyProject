package com.charmenli.scalephone.manager;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.PixelFormat;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;

import com.charmenli.scalephone.BaseApp;
import com.charmenli.scalephone.activity.MainActivity;
import com.charmenli.scalephone.activity.OperationWindowActivity;
import com.charmenli.scalephone.R;
import com.charmenli.scalephone.notification.UsingSensorModeNotification;
import com.charmenli.scalephone.util.PrefConf;
import com.charmenli.scalephone.util.ScreenCaptureUtils;
import com.charmenli.scalephone.util.SharedPreferenceUtils;
import com.charmenli.scalephone.util.Waiter;

import android.os.Handler;
import android.view.ViewConfiguration;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;

import java.util.ArrayList;

/**
 * Created by charmenli on 2014/3/27.
 */
public class IndicatorManager {

    private static final String TAG = IndicatorManager.class.getSimpleName();

    private static IndicatorManager indicatorManager;
    private Context mContext;
    private SharedPreferences mPreferences;
    private int mScreenWidth = 0;
    private int mScreenHeight = 0;
    private int mScaleTouchSlop = 0;
    private float density;
    private OnIndicatorEventListener mOnIndicatorEventListener = new OnIndicatorEventListener() {
//        <item>缩放操作</item>
//        <item>应用设置</item>
//        <item>传感器模式</item>
//        <item>截屏</item>
//        <item>锁屏</item>
        final static int FUNCTION_OPEN_OPERATION_WINDOW = 0;
        final static int FUNCTION_OPEN_OPERATION_SETTING = 1;
        final static int FUNCTION_USE_SENSOR_MODE = 2;
        final static int FUNCTION_SCREEN_CAPTURE = 3;
        final static int FUNCTION_LOCK_SCREEN = 4;
        Handler handler = new Handler(Looper.getMainLooper()) {
            @Override
            public void handleMessage(Message msg) {
                int function = -1;
                switch (msg.what) {
                    case MSG_CLICK:
                        function = SharedPreferenceUtils.getSingleClickFunction();
                        break;
                    case MSG_DOUBLE_CLICK:
                        function = SharedPreferenceUtils.getDoubleClickFunction();
                        break;
                    case MSG_LONG_PRESS:
                        function = SharedPreferenceUtils.getLongPressFunction();
                        break;
                }

                switch (function) {
                    case FUNCTION_OPEN_OPERATION_WINDOW:
                        openOperationWindow();
                        break;
                    case FUNCTION_OPEN_OPERATION_SETTING:
                        openOperationSetting();
                        break;
                    case FUNCTION_USE_SENSOR_MODE:
                        useSensorMode();
                        break;
                    case FUNCTION_SCREEN_CAPTURE:
                        screenCapture();
                        break;
                    case FUNCTION_LOCK_SCREEN:
                        lockScreen();
                        break;
                    default:
                        break;
                }
            }

            void startActivity(Class c) {
                Intent intent = new Intent(mContext, c);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                mContext.startActivity(intent);
            }

            void openOperationWindow() {
                Bitmap bitmap = ScreenCaptureUtils.getScreenBitmap();
                BaseApp.get().setScreenBitmap(bitmap);
                startActivity(OperationWindowActivity.class);
            }

            void screenCapture() {

            }

            void openOperationSetting() {
                startActivity(MainActivity.class);
            }

            void useSensorMode() {
                hideIndicator();
                SensorController.get().init(mContext).start();
                UsingSensorModeNotification.notify(mContext, mContext.getString(R.string.using_sensor_mode_notification_title), 1);
            }

            void lockScreen() {
                MonkeyManager.get().getMonkeyActionExecutor().submit("press power");
            }
        };
        @Override
        public void onClick(View v) {
            hideIndicator();
            handler.sendEmptyMessageDelayed(MSG_CLICK, 10);
        }

        @Override
        public void onLongPress(View v) {
            handler.sendEmptyMessageDelayed(MSG_LONG_PRESS, 10);
        }

        @Override
        public void onDoubleClick(View v) {
            handler.sendEmptyMessageDelayed(MSG_DOUBLE_CLICK, 10);
        }
    };

    private IndicatorManager() {
    }

    public static IndicatorManager getInstance() {
        if (indicatorManager == null) {
            indicatorManager = new IndicatorManager();
        }
        return indicatorManager;
    }

    public IndicatorManager init(Context context) {
        mContext = context;
        mPreferences = SharedPreferenceUtils.getSharedPreference();
        mScreenWidth = getWindowManager().getDefaultDisplay().getWidth();
        mScreenHeight = getWindowManager().getDefaultDisplay().getHeight();
        mScaleTouchSlop = ViewConfiguration.get(mContext).getScaledTouchSlop();
        density = mContext.getResources().getDisplayMetrics().density;
        return indicatorManager;
    }

    private View mIndicatorView;
    private Handler mIndicatorClickHandler;
    private final static int MSG_CLICK = 1;
    private final static int MSG_DOUBLE_CLICK = 2;
    private final static int MSG_LONG_PRESS = 3;
    public View getIndicatorView() {
        if (mIndicatorView == null) {
            mIndicatorView = View.inflate(mContext, R.layout.layout_indicator, null);
            initTouchListener();
            mIndicatorClickHandler = new Handler(){
                @Override
                public void handleMessage(Message msg) {
                    switch (msg.what) {
                        case MSG_CLICK:
                            if (mOnIndicatorEventListener != null) {
                                mOnIndicatorEventListener.onClick(mIndicatorView);
                            }
                            break;
                        case MSG_DOUBLE_CLICK:
                            if (mOnIndicatorEventListener != null) {
                                mOnIndicatorEventListener.onDoubleClick(mIndicatorView);
                            }
                            break;
                        case MSG_LONG_PRESS:
                            if (mOnIndicatorEventListener != null) {
                                mOnIndicatorEventListener.onLongPress(mIndicatorView);
                            }
                            break;
                    }
                }
            };
        }
        return mIndicatorView;
    }

    private void initTouchListener() {
        mIndicatorView.setOnTouchListener(new View.OnTouchListener() {
            MotionEvent preUpEvent;
            MotionEvent downEvent;
            boolean isMoved = false;
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        isMoved = false;
                        mIndicatorClickHandler.sendEmptyMessageDelayed(MSG_LONG_PRESS, ViewConfiguration.getLongPressTimeout());
                        downEvent = MotionEvent.obtain(event);
                        break;
                    case MotionEvent.ACTION_MOVE:
                        onMove(event);
                        break;
                    case MotionEvent.ACTION_UP:
                        mIndicatorClickHandler.removeMessages(MSG_LONG_PRESS);
                        mIndicatorClickHandler.removeMessages(MSG_DOUBLE_CLICK);
                        mIndicatorClickHandler.removeMessages(MSG_CLICK);
                        if (isDoubleClick(event)) {
                            mIndicatorClickHandler.sendEmptyMessage(MSG_DOUBLE_CLICK);
                        } else if (!isMove() && !isLongClick(event)){
                            mIndicatorClickHandler.sendEmptyMessageDelayed(MSG_CLICK, ViewConfiguration.getDoubleTapTimeout());
                        }
                        if (isMove() && !SharedPreferenceUtils.isLockIndicatorPosition())onMoveFinish();
                        preUpEvent = MotionEvent.obtain(event);
                        break;
                }
                return true;
            }
            Thread thread = null;
            private void onMoveFinish() {
                if (thread != null && thread.isAlive()) {
                    thread.interrupt();
                    thread = null;
                }
                thread = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        int toDeltaX = smallWindowParams.x > mScreenWidth/2 ? mScreenWidth-smallWindowParams.x : -smallWindowParams.x;
                        int stepLen = (int)Math.ceil(toDeltaX/10.0);
                        for (int moveTimes = (int)Math.ceil(Math.abs(toDeltaX)/10.0); moveTimes>=0; moveTimes--) {
                            smallWindowParams.x += stepLen;
                            updateIndicatorView();
                            Waiter.sleep(50);
                        }
                        saveIndicatorPosition();
                        thread = null;
                    }
                });
                thread.start();
            }

            private void saveIndicatorPosition() {
                SharedPreferences.Editor editor = mPreferences.edit();
                editor.putInt(PrefConf.INDICATOR_X, smallWindowParams.x);
                editor.putInt(PrefConf.INDICATOR_Y, smallWindowParams.y);
                editor.commit();
            }

            private boolean isLongClick(MotionEvent curEvent) {
                return !isMove() && curEvent.getEventTime()-curEvent.getDownTime()>=ViewConfiguration.getLongPressTimeout();
            }

            private boolean isDoubleClick(MotionEvent curEvent) {
                if (preUpEvent != null && curEvent != null) {
                    return curEvent.getEventTime() - preUpEvent.getEventTime() < ViewConfiguration.getDoubleTapTimeout()-50;
                }
                return false;
            }

            private boolean isMove() {
                return isMoved;
            }

            private void onMove(MotionEvent event) {
//                Log.d(TAG, "onMove curEvent="+event+", downEvent="+downEvent);
                float deltaX = event.getRawX();// - downEvent.getRawX();
                float deltaY = event.getRawY();// - downEvent.getRawY();
                if (Math.abs(deltaX) > mScaleTouchSlop || Math.abs(deltaY) > mScaleTouchSlop) {
                    if (!isMoved) {
                        mIndicatorClickHandler.removeMessages(MSG_LONG_PRESS);
                    }
                    isMoved = true;
                    if (!SharedPreferenceUtils.isLockIndicatorPosition()) {
                        smallWindowParams.x = (int)deltaX;
                        smallWindowParams.y = (int)deltaY;
                        updateIndicatorView();
                    }
                }
            }
        });
    }

    private WindowManager.LayoutParams smallWindowParams;
    public void attachToWindow() {
        View view = getIndicatorView();
        WindowManager windowManager = getWindowManager();

        int screenHeight = windowManager.getDefaultDisplay().getHeight();
        if (smallWindowParams == null) {
            smallWindowParams = new WindowManager.LayoutParams();
            smallWindowParams.type = LayoutParams.TYPE_SYSTEM_ALERT;
            smallWindowParams.format = PixelFormat.RGBA_8888;
            smallWindowParams.flags = LayoutParams.FLAG_NOT_FOCUSABLE;
            smallWindowParams.gravity = Gravity.LEFT | Gravity.TOP;
            float indicatorScale = SharedPreferenceUtils.getIndicatorScale();
            smallWindowParams.width = (int) (mContext.getResources().getDrawable(R.drawable.indicator).getIntrinsicWidth() * indicatorScale);
            smallWindowParams.height = (int) (mContext.getResources().getDrawable(R.drawable.indicator).getIntrinsicHeight() * indicatorScale);
            smallWindowParams.x = mPreferences.getInt(PrefConf.INDICATOR_X, 0);
            smallWindowParams.y = mPreferences.getInt(PrefConf.INDICATOR_Y, screenHeight * 2 / 3);
        }
//        view.setLayoutParams(smallWindowParams);
        windowManager.addView(view, smallWindowParams);
    }

    public static interface OnIndicatorEventListener {
        void onClick(View v);
        void onLongPress(View v);
        void onDoubleClick(View v);
    }

    private WindowManager getWindowManager() {
        WindowManager windowManager = (WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE);
        return windowManager;
    }

    public void updateIndicatorView() {
        if (mIndicatorView != null && smallWindowParams != null) {
//            getWindowManager().updateViewLayout(mIndicatorView, smallWindowParams);
            mIndicatorHandler.removeMessages(MSG_INDICATOR_UPDATE);
            mIndicatorHandler.sendEmptyMessage(MSG_INDICATOR_UPDATE);
        }
    }

    public void setIndicatorViewSize(float scale) {
        smallWindowParams.width = (int) (mContext.getResources().getDrawable(R.drawable.indicator).getIntrinsicWidth() * scale);
        smallWindowParams.height = (int) (mContext.getResources().getDrawable(R.drawable.indicator).getIntrinsicHeight() * scale);
        updateIndicatorView();
    }

    public static final int MSG_INDICATOR_SHOW = 0;
    public static final int MSG_INDICATOR_HIDDEN = 1;
    public static final int MSG_INDICATOR_UPDATE = 2;
    private Handler mIndicatorHandler = new Handler(Looper.getMainLooper()){
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_INDICATOR_SHOW:
                    getIndicatorView().setVisibility(View.VISIBLE);
                    break;
                case MSG_INDICATOR_HIDDEN:
                    getIndicatorView().setVisibility(View.GONE);
                    break;
                case MSG_INDICATOR_UPDATE:
                    if (smallWindowParams.x < 0) {
                        smallWindowParams.x = 0;
                    } else if(smallWindowParams.x + smallWindowParams.width > mScreenWidth) {
                        smallWindowParams.x = mScreenWidth - smallWindowParams.width;
                    }
                    getWindowManager().updateViewLayout(mIndicatorView, smallWindowParams);
                    break;
            }
        }
    };
    public void showIndicator() {
        mIndicatorHandler.sendEmptyMessage(MSG_INDICATOR_SHOW);
    }

    public void hideIndicator() {
        mIndicatorHandler.sendEmptyMessage(MSG_INDICATOR_HIDDEN);
    }

    public void removeIndicator() {
        if (mIndicatorView != null) {
            getWindowManager().removeView(mIndicatorView);
            mIndicatorView = null;
        }
    }

}
