package com.pumelotech.dev.e19_demo;

import android.app.Application;
import android.content.Context;
import android.graphics.Typeface;

import com.pumelotech.dev.e19_demo.BLE.LeConnector;

/**
 * Created by Administrator on 2016/9/20.
 */

public class MyApplication extends Application {

    static public String DebugTag = "E19";
    static public Context context;
    public static LeConnector mLeConnector;
    static public Typeface fontPhotonicaRegular;
    static public Typeface fontPhotonicaStraight;
    @Override
    public void onCreate() {
        super.onCreate();
        fontPhotonicaRegular = Typeface.createFromAsset(getAssets(), "fonts/Photonica_regular.ttf");
        fontPhotonicaStraight = Typeface.createFromAsset(getAssets(), "fonts/Photonica_straight.ttf");
        context = this;
    }
}
