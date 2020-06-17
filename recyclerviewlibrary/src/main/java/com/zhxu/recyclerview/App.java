package com.zhxu.recyclerview;

import android.app.Application;
import android.support.multidex.MultiDexApplication;

/**
 * <p>Description:
 *
 * @author xzhang
 */

public class App extends MultiDexApplication {

    private static App context ;

    @Override
    public void onCreate() {
        super.onCreate();
        context = this ;
    }

    public static App getContext(){
        return context;
    }
}
