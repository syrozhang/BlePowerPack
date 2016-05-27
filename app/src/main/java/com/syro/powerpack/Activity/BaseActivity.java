package com.syro.powerpack.Activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.widget.Toast;

import com.syro.pp_api.ToastUtil;
import com.syro.powerpack.MyApplication;
import com.syro.powerpack.R;
import com.syro.pp_core.AppAction;
import com.syro.pp_api.LogUtil;

/**
 * Created by Syro on 2016-01-30.
 */
public abstract class BaseActivity extends AppCompatActivity {
    public Context mContext;
    public MyApplication mApplication;// Application的全局实例
    public AppAction mAppAction;// 核心层AppAction的全局实例

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LogUtil.show("BaseActivity.onCreate()");
        mContext = this.getApplicationContext();
        mApplication = (MyApplication) this.getApplication();
        mAppAction = mApplication.getAppAction();
        LogUtil.show("mContext = " + mContext);
        LogUtil.show("mApplication = " + mApplication);
        LogUtil.show("mAppAction = " + mAppAction);
    }

    protected void initToolbar(Toolbar toolbar, int toolBarResId, int titleNameResId) {
        toolbar = (Toolbar) findViewById(toolBarResId);
        toolbar.setTitle(titleNameResId);
        toolbar.setLogo(R.drawable.jeckson);
        setSupportActionBar(toolbar);// 添加Toolbar到Activity
    }

    protected void initToolbar(Toolbar toolbar, int toolBarResId, String titleName) {
        toolbar = (Toolbar) findViewById(toolBarResId);
        toolbar.setTitle(titleName);
        toolbar.setLogo(R.drawable.jeckson);
        setSupportActionBar(toolbar);// 添加Toolbar到Activity
    }

    protected void toActivity(Class<? extends Activity> tarActivity) {
        Intent intent = new Intent(this, tarActivity);
        startActivity(intent);
    }

    protected void showToast(String msg) {
        ToastUtil.showToast(this, msg, Toast.LENGTH_SHORT);
    }
}
