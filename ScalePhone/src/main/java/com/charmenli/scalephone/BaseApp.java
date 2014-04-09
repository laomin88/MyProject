package com.charmenli.scalephone;

import android.app.Application;
import android.graphics.Bitmap;

import com.charmenli.scalephone.manager.IndicatorManager;
import com.charmenli.scalephone.service.CoreService;
import com.charmenli.scalephone.util.SharedPreferenceUtils;

import java.lang.ref.WeakReference;

/**
 * Created by charmenli on 2014/3/27.
 */
public class BaseApp extends Application {

    private static BaseApp sInstance;

    public BaseApp() {
        sInstance = this;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        CoreService.startService(this);
        SharedPreferenceUtils.init(this);
        IndicatorManager.getInstance().init(this).attachToWindow();
    }

    private Bitmap mScreenBitmap;
    public void setScreenBitmap(Bitmap bitmap) {
        mScreenBitmap = bitmap;
    }

    public Bitmap getScreenBitmap() {
        return mScreenBitmap;
    }

    public static BaseApp get() {
        return sInstance;
    }
}
