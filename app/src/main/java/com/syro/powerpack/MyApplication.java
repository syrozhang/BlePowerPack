package com.syro.powerpack;

import android.app.Application;

import com.syro.pp_core.AppAction;
import com.syro.pp_core.AppActionImpl;
import com.syro.pp_api.LogUtil;

/**
 * Created by Syro on 2016-01-30.
 */
public class MyApplication extends Application {
    private AppAction mAppAction;

    @Override
    public void onCreate() {
        super.onCreate();
        LogUtil.show("MyApplication.onCreate()");
        mAppAction = new AppActionImpl(this);
    }

    public AppAction getAppAction() {
        return mAppAction;
    }
}
