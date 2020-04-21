package com.example.jchat;

import android.app.Application;
import android.content.Context;

import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleObserver;
import androidx.lifecycle.OnLifecycleEvent;
import androidx.lifecycle.ProcessLifecycleOwner;

public class MyApplication extends Application implements LifecycleObserver {
    private static Context appcontext;
    public static boolean wasInBg;

    @Override
    public void onCreate() {
        super.onCreate();
        appcontext=this;
        ProcessLifecycleOwner.get().getLifecycle().addObserver(this);
    }

    public static Context getAppcontext()
    {
        return appcontext;
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    public void onMoveToForeground()
    {
        wasInBg=true;
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    public void onMoveToBackground()
    {
        wasInBg=false;
    }

}
