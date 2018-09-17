package com.dandan.xmpermission.xmpermission;

import android.app.Application;
import android.util.Log;

/**
 * Created by Tanzhenxing
 * Date: 2018/9/10 上午11:27
 * Description:
 */
public class App extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        Log.d("tanzhenxing", "onCreate");
    }
}
