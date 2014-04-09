package com.charmenli.scalephone.activity;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.SharedPreferences;
import android.graphics.Point;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.charmenli.scalephone.R;
import com.charmenli.scalephone.util.PrefConf;
import com.charmenli.scalephone.util.SharedPreferenceUtils;


public class OperationWindowSettingActivity extends Activity {

    private static final String TAG = OperationWindowSettingActivity.class.getSimpleName();

    private ImageView mBackgroundImageView;
    private ImageView mCoverImageView;
    private ImageView mScaleWindowImageView;
    private SeekBar mWidthSeekBar;
    private SeekBar mHeightSeekBar;
    private CheckBox mFixWidthHeightScaleCheckBox;
    private TextView mTextView;

    private int mBackgroundImageViewWidth = 0;
    private int mBackgroundImageViewHeight = 0;

    private int mScaleWidth = 75;
    private int mScaleHeight = 75;
    private int mScaleOffsetX, mScaleOffsetY;
    private boolean mFixedScale = true;

    private Point mWindowRealSize = new Point();
    private SharedPreferences mPreferences = SharedPreferenceUtils.getSharedPreference();

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_operation_window_setting);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            getWindowManager().getDefaultDisplay().getRealSize(mWindowRealSize);
        } else {
            getWindowManager().getDefaultDisplay().getSize(mWindowRealSize);
        }

        init();
        initViews();
    }

    private void initViews() {
        mBackgroundImageView = (ImageView) findViewById(R.id.iv_background);
        mCoverImageView = (ImageView) findViewById(R.id.iv_conver);
        mScaleWindowImageView = (ImageView) findViewById(R.id.iv_scale_window);
        mWidthSeekBar = (SeekBar) findViewById(R.id.seekBar_width);
        mHeightSeekBar = (SeekBar) findViewById(R.id.seekBar_height);
        mFixWidthHeightScaleCheckBox = (CheckBox) findViewById(R.id.cb_fixed_scale);
        mTextView = (TextView) findViewById(R.id.tv_percent);

        //设置cover view大小
        final ViewTreeObserver vto = mBackgroundImageView.getViewTreeObserver();
        vto.addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
            @Override
            public boolean onPreDraw() {
                mBackgroundImageViewWidth = mBackgroundImageView.getMeasuredWidth();
                mBackgroundImageViewHeight = mBackgroundImageView.getMeasuredHeight();

                mCoverImageView.setMinimumWidth(mBackgroundImageViewWidth);
                mCoverImageView.setMinimumHeight(mBackgroundImageViewHeight);

                updateScaleWindow();
                mScaleWindowImageView.setVisibility(View.VISIBLE);
                addScaleWindowTouchListener();
                mBackgroundImageView.getViewTreeObserver().removeOnPreDrawListener(this);
                return true;
            }
        });

        mFixWidthHeightScaleCheckBox.setChecked(mFixedScale);
        mFixWidthHeightScaleCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    mHeightSeekBar.setVisibility(View.INVISIBLE);
                    mScaleHeight = mScaleWidth;
                } else {
                    mHeightSeekBar.setVisibility(View.VISIBLE);
                    mHeightSeekBar.setProgress(mScaleHeight);
                }
                updateScaleWindow();
            }
        });

        mWidthSeekBar.setProgress(mScaleWidth);
        mWidthSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                Log.d(TAG, "mWidthSeekBar onProgressChanged seekBar="+seekBar+", progress="+progress);
                mScaleWidth = progress;
                if (mFixWidthHeightScaleCheckBox.isChecked()) {
                    mScaleHeight = progress;
                }
                updateScaleWindow();
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        mHeightSeekBar.setProgress(mScaleHeight);
        mHeightSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                Log.d(TAG, "mHeightSeekBar onProgressChanged seekBar="+seekBar+", progress="+progress);
                mScaleHeight = progress;
                updateScaleWindow();
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        mTextView.setText(mScaleWidth+"%");
    }

    private void init() {
        mScaleWidth = mPreferences.getInt(PrefConf.SCALE_WIDTH, 75);
        mScaleHeight = mPreferences.getInt(PrefConf.SCALE_HEIGHT, 75);
        mScaleOffsetX = mPreferences.getInt(PrefConf.OFFSET_X, 0);
        mScaleOffsetY = mPreferences.getInt(PrefConf.OFFSET_Y, 0);
        mFixedScale = mPreferences.getBoolean(PrefConf.FIXED_SCALE, true);
    }

    private void addScaleWindowTouchListener() {
        mScaleWindowImageView.setOnTouchListener(new View.OnTouchListener() {
            private boolean mDown = false;
            private float mDownX = 0;
            private float mDownY = 0;
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) mScaleWindowImageView.getLayoutParams();
                        mScaleOffsetY = params.topMargin;
                        mScaleOffsetX = params.leftMargin;
                        mDown = true;
                        mDownX = event.getRawX() - mScaleOffsetX;
                        mDownY = event.getRawY() - mScaleOffsetY;
                        break;
                    case MotionEvent.ACTION_MOVE:
                        if (mDown) {
                            mScaleOffsetX = (int)(event.getRawX() - mDownX);
                            mScaleOffsetY = (int)(event.getRawY() - mDownY);
                        }
                        break;
                    case MotionEvent.ACTION_UP:
                        mDown = false;
                        mDownX = 0;
                        mDownY = 0;
                        break;
                }
                updateScaleWindow();
                return true;
            }
        });
    }

    private void updateScaleWindow() {
        FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) mScaleWindowImageView.getLayoutParams();
        params.width = (int) (mBackgroundImageViewWidth * (float)mScaleWidth/100);
        params.height = (int) (mBackgroundImageViewHeight * (float)mScaleHeight/100);
        params.topMargin = mScaleOffsetY;
        params.leftMargin = mScaleOffsetX;
        savePref();
        mTextView.setText(mScaleWidth+"%");
        mScaleWindowImageView.setLayoutParams(params);
    }

    private void savePref() {
        SharedPreferences.Editor editor = mPreferences.edit();
        editor.putInt(PrefConf.SCALE_WIDTH, mScaleWidth);
        editor.putInt(PrefConf.SCALE_HEIGHT, mScaleHeight);
        editor.putInt(PrefConf.OFFSET_X, getSaveOffsetX());
        editor.putInt(PrefConf.OFFSET_Y, getSaveOffsetY());
        editor.putBoolean(PrefConf.FIXED_SCALE, mFixWidthHeightScaleCheckBox.isChecked());
        editor.commit();
    }

    private int getSaveOffsetX() {
        double rate = (int) ((double)mWindowRealSize.x / mBackgroundImageViewWidth);
        return (int) (mScaleOffsetX * rate);
    }

    private int getSaveOffsetY() {
        double rate = (int) ((double)mWindowRealSize.y / mBackgroundImageViewHeight);
        return (int) (mScaleOffsetY * rate);
    }

    public void onResetClick(View view) {
        mFixWidthHeightScaleCheckBox.setChecked(true);
        mScaleWidth = mScaleHeight = 75;
        mWidthSeekBar.setProgress(mScaleWidth);
        mHeightSeekBar.setProgress(mScaleHeight);
        mScaleOffsetX = 0;
        mScaleOffsetY = 0;
        updateScaleWindow();
    }

}
