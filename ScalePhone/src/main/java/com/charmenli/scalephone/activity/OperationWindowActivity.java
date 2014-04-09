package com.charmenli.scalephone.activity;

import android.app.Activity;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.charmenli.scalephone.BaseApp;
import com.charmenli.scalephone.R;
import com.charmenli.scalephone.manager.IndicatorManager;
import com.charmenli.scalephone.manager.MonkeyManager;
import com.charmenli.scalephone.util.PrefConf;
import com.charmenli.scalephone.util.SharedPreferenceUtils;

import java.util.ArrayList;

public class OperationWindowActivity extends Activity {

    private ImageView mScaleWindowImageView;
    private SharedPreferences mPreferences = SharedPreferenceUtils.getSharedPreference();

    private final float SCALE_WIDTH = mPreferences.getInt(PrefConf.SCALE_WIDTH, 75) / 100.0f;
    private final float SCALE_HEIGHT = mPreferences.getInt(PrefConf.SCALE_HEIGHT, 75) / 100.0f;
    private final int OFFSET_X = mPreferences.getInt(PrefConf.OFFSET_X, 10);
    private final int OFFSET_Y = mPreferences.getInt(PrefConf.OFFSET_Y, 200);

    private ArrayList<String> mActions = new ArrayList<String>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_operation_window);
        mActions.clear();
        init();
    }

    private void init() {
        Bitmap bitmap = BaseApp.get().getScreenBitmap();
        mScaleWindowImageView = (ImageView) findViewById(R.id.iv_scale_window);
        mScaleWindowImageView.setImageBitmap(bitmap);

        FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) mScaleWindowImageView.getLayoutParams();
        params.width = (int) (bitmap.getWidth() * SCALE_WIDTH);
        params.height = (int) (bitmap.getHeight() * SCALE_HEIGHT);
        params.leftMargin = OFFSET_X;
        params.topMargin = OFFSET_Y;
        mScaleWindowImageView.setLayoutParams(params);

        mScaleWindowImageView.setOnTouchListener(sMyTouchListener);

    }

    private View.OnTouchListener sMyTouchListener = new View.OnTouchListener() {
        MotionEvent preEvent;
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            String action = null;
            String waitAction = null;
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    preEvent = MotionEvent.obtain(event);
                    action = String.format("touch down %s %s", getX(event.getX()), getY(event.getY()));
                    break;
                case MotionEvent.ACTION_MOVE:
                    action = String.format("touch move %s %s", getX(event.getX()), getY(event.getY()));
                    if (preEvent != null) waitAction = "sleep " + (event.getEventTime() - preEvent.getEventTime());
                    preEvent = MotionEvent.obtain(event);
                    break;
                case MotionEvent.ACTION_UP:
                    waitAction = "sleep " + (event.getEventTime() - preEvent.getEventTime());
                    action = String.format("touch up %s %s", getX(event.getX()), getY(event.getY()));
                    break;
            }
            if (waitAction != null) mActions.add(waitAction);
            if (action != null) mActions.add(action);
            if (event.getAction() == MotionEvent.ACTION_UP) {
                MonkeyManager.get().getMonkeyActionExecutor().submit(mActions);
                finish();
            }
            return true;
        }

        private int getX(float x) {
            return (int) (x / SCALE_WIDTH);
        }

        private int getY(float y) {
            return (int) (y / SCALE_HEIGHT);
        }
    };

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (event.getKeyCode() == KeyEvent.KEYCODE_BACK) {
            showIndicator();
        }
        return super.onKeyDown(keyCode, event);
    }

    private void showIndicator() {
        IndicatorManager.getInstance().showIndicator();
        finish();
    }

    public void onWhiteSpaceClick(View view) {
        showIndicator();
    }
}
