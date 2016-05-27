package com.syro.powerpack.Activity;

import android.animation.ObjectAnimator;
import android.bluetooth.BluetoothGattCharacteristic;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.LinearInterpolator;
import android.widget.TextView;

import com.syro.pp_api.BleUtil;
import com.syro.powerpack.R;
import com.syro.pp_data.GattProfile;
import com.syro.pp_api.LogUtil;
import com.timqi.sectorprogressview.ColorfulRingProgressView;

import java.text.DecimalFormat;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by Syro on 2016-01-30.
 */
public class DisplayServiceActivity extends BaseActivity {
    private Toolbar mToolbar;
    private View mStatusLine;
    private ColorfulRingProgressView mRingProgress;
    private TextView mBattVolt;
    private TextView mBattPercent;
    private TextView mBattOutputCurt;
    private TextView mBattOutputVolt;
    private String mDvcName;
    private String mDvcAddr;
    private BluetoothGattCharacteristic mNotifiedCharacteristic;
    private BluetoothGattCharacteristic mTargetCharacteristic;
    private static Timer mTimerCnt;
    private boolean isConnected;
    private float percent;
    private double battVolt;
    private double battOutputVolt;
    private double battOutputCurt;
    public static final int REQUEST_CODE = 0x1;
    public static final int REFRESH_DATA = 0x2;

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case REFRESH_DATA:
                    mRingProgress.setPercent(Math.round(percent));
                    DecimalFormat decimalFormat = new DecimalFormat("0.000");
                    mBattPercent.setText(Math.round(percent) + "%");
                    mBattVolt.setText(decimalFormat.format(battVolt) + "V");
                    mBattOutputCurt.setText(decimalFormat.format(battOutputCurt) + "A");
                    mBattOutputVolt.setText(decimalFormat.format(battOutputVolt) + "V");
                    break;
            }
        }
    };
    private BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (BleUtil.GATT_CONNECTED.equals(action)) {
                isConnected = true;
                showToast("连接成功");
                mStatusLine.setBackgroundColor(Color.GREEN);
                invalidateOptionsMenu(); // 刷新Toolbar menu
            } else if (BleUtil.GATT_DISCONNECTED.equals(action)) {
                isConnected = false;
                showToast("连接失败");
                mStatusLine.setBackgroundColor(Color.RED);
                invalidateOptionsMenu(); // 刷新Toolbar menu
            } else if (BleUtil.GATT_SERVICE_DISCOVERED.equals(action)) {
                mTargetCharacteristic = mAppAction.searchCharacteristic(GattProfile.NORDIC_UART_RX);
                handleCharacteristic(mTargetCharacteristic);
            } else if (BleUtil.ACTION_CHARAC_VALUE_GET_SUCCESS.equals(action)) {
                byte[] uartData = intent.getByteArrayExtra(BleUtil.CHARACTERISTIC_VALUE);
                parseUartData(uartData);
                startTimer();
            } else if (BleUtil.ACTION_CHARAC_VALUE_WRITE_SUCCESS.equals(action)) {
                mBattVolt.setText("Data: write successfully");
            }
        }
    };

    private void parseUartData(byte[] uartData) {
        String uartDataStr = new String(uartData);
        uartDataStr = uartDataStr.replace("\r", "");
        uartDataStr = uartDataStr.replace("\n", "");
        String[] ppInfo = uartDataStr.split(",");
        int v0 = Integer.parseInt(ppInfo[0]);
        int a0 = Integer.parseInt(ppInfo[1]);
        int v1 = Integer.parseInt(ppInfo[2]);
        battVolt = v0 * 3.3 / 4096;
        battOutputCurt = a0 * 3.3 / (4096 * 50 * 0.1);
        battOutputVolt = v1 * 3.3 / 4096;
        percent = (float) ((battVolt - 3.2) / (4.1 - 3.2) * 100);
        if (percent < 0) {
            percent = 0;
        } else if (percent > 100) {
            percent = 100;
        }
    }

    private void handleCharacteristic(BluetoothGattCharacteristic targetCharac) {
        int characProper = targetCharac.getProperties();// 返回Characteristic的Properties值(read/write/notify)

        // Characteristic是Notify属性
        if ((characProper & BluetoothGattCharacteristic.PROPERTY_NOTIFY) > 0) {
            if (mNotifiedCharacteristic != null) {
                mAppAction.setCharacteristicNotification(mNotifiedCharacteristic, false);
                mNotifiedCharacteristic = null;
            }
            mNotifiedCharacteristic = targetCharac;
            mAppAction.setCharacteristicNotification(targetCharac, true);
            return;
        }

        // Characteristic是Read属性
        if ((characProper & BluetoothGattCharacteristic.PROPERTY_READ) > 0) {
            if (mNotifiedCharacteristic != null) {
                mAppAction.setCharacteristicNotification(mNotifiedCharacteristic, false);
                mNotifiedCharacteristic = null;
            }
            mAppAction.readCharacteristic(targetCharac);
            return;
        }

        // Characteristic是Write属性
        if ((characProper & BluetoothGattCharacteristic.PROPERTY_WRITE) > 0) {
            Intent intent = new Intent(DisplayServiceActivity.this, WriteCharacValueActivity.class);
            intent.putExtra("CharacName", GattProfile.getInfo(targetCharac.getUuid().toString(), "Unknown Characteristic"));
            startActivityForResult(intent, REQUEST_CODE);// 打开一个子Activity来接收输入
            return;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_disp_service);
        Intent intent = getIntent();
        mDvcName = intent.getStringExtra("name");
        mDvcAddr = intent.getStringExtra("addr");
        LogUtil.show("mDvcName = " + mDvcName);
        LogUtil.show("mDvcAddr = " + mDvcAddr);

        initToolbar(mToolbar, R.id.activity_service_toolbar, R.string.service_toolbar_title);
        initView();
        mAppAction.connectGattServer(mDvcAddr);
    }

    @Override
    protected void onResume() {
        super.onResume();
        initBroadcastReceiver();
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(mBroadcastReceiver);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mAppAction.disconnectGattServer();
        stopTimer();
    }

    // 生成Toolbar menu
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.bt_connect, menu);
        if (isConnected) {
            menu.findItem(R.id.item_bluetooth_disconnect).setVisible(true);
            menu.findItem(R.id.item_bluetooth_connect).setVisible(false);
        } else {
            menu.findItem(R.id.item_bluetooth_connect).setVisible(true);
            menu.findItem(R.id.item_bluetooth_disconnect).setVisible(false);
        }
        return super.onCreateOptionsMenu(menu);
    }

    // Toolbar menu item被选中时回调
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.item_bluetooth_connect:
                mAppAction.connectGattServer(mDvcAddr);
                break;
            case R.id.item_bluetooth_disconnect:
                mAppAction.disconnectGattServer();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    // 子Activity关闭后回调
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (resultCode) {
            case RESULT_OK:
                String str = data.getStringExtra("inputString");
                mAppAction.writeCharacteristic(mTargetCharacteristic, str);
                break;
        }
    }

    private void initView() {
        mStatusLine = findViewById(R.id.view_ble_con_status_line);
        mBattVolt = (TextView) findViewById(R.id.tv_batt_volt);
        mBattPercent = (TextView) findViewById(R.id.tv_batt_percent);
        mBattOutputCurt = (TextView) findViewById(R.id.tv_output_curt);
        mBattOutputVolt = (TextView) findViewById(R.id.tv_output_volt);
        mRingProgress = (ColorfulRingProgressView) findViewById(R.id.colorful_ring_progress);
        mRingProgress.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ObjectAnimator anim = ObjectAnimator.ofFloat(v, "percent", 0, ((ColorfulRingProgressView) v).getPercent());
                anim.setInterpolator(new LinearInterpolator());
                anim.setDuration(1000);
                anim.start();
            }
        });
    }

    // 注册符合intentfilter的广播接收器
    private void initBroadcastReceiver() {
        IntentFilter inf = new IntentFilter();
        inf.addAction(BleUtil.GATT_CONNECTED);
        inf.addAction(BleUtil.GATT_DISCONNECTED);
        inf.addAction(BleUtil.GATT_SERVICE_DISCOVERED);
        inf.addAction(BleUtil.ACTION_CHARAC_VALUE_GET_SUCCESS);
        inf.addAction(BleUtil.ACTION_CHARAC_VALUE_WRITE_SUCCESS);
        registerReceiver(mBroadcastReceiver, inf);
    }

    private void startTimer() {
        if (mTimerCnt == null) {
            mTimerCnt = new Timer();
            mTimerCnt.schedule(new TimerTask() {
                @Override
                public void run() {
                    mHandler.sendEmptyMessage(REFRESH_DATA);
                }
            }, 0, 1000);//每1000ms发送一次信号给handler
        }
    }

    private void stopTimer() {
        if (mTimerCnt != null) {
            mTimerCnt.cancel();
            mTimerCnt = null;
        }
    }
}
