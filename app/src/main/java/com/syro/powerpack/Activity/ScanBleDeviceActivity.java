package com.syro.powerpack.Activity;

import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.syro.powerpack.Adapter.DeviceListAdapter;
import com.syro.pp_core.AppActionCallbackListener;
import com.syro.pp_core.AppActionImpl;
import com.syro.pp_api.LogUtil;
import com.syro.powerpack.R;

import java.util.ArrayList;

public class ScanBleDeviceActivity extends BaseActivity {
    private Toolbar mToolbar;
    private ListView mDeviceListView;
    private ArrayList<BluetoothDevice> mDeviceList = new ArrayList<BluetoothDevice>();
    private DeviceListAdapter mDeviceListAdapter;
    private BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();// 获取intent的类型
            if (AppActionImpl.ACTION_REFRESH_MENU.equals(action)) {
                invalidateOptionsMenu(); // 刷新Toolbar menu
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LogUtil.show("ScanBleDeviceActivity.onCreate()");
        setContentView(R.layout.activity_scan_device);
        initToolbar(mToolbar, R.id.activity_main_toolbar, R.string.device_toolbar_title);
        initView();
        mAppAction.initBle();
    }

    @Override
    protected void onResume() {
        super.onResume();
        LogUtil.show("ScanBleDeviceActivity.onResume()");
        initBroadcastReceiver();
        mAppAction.enableBle();
        mDeviceListAdapter.clearItems();

        mAppAction.startScan(new AppActionCallbackListener<ArrayList<BluetoothDevice>>() {
            @Override
            public void onSuccess(ArrayList<BluetoothDevice> data) {
                // 发现BLE设备后的回调函数
                mDeviceListAdapter.addItems(data);
            }

            @Override
            public void onFailure(String errMsg) {
                LogUtil.show(errMsg);
            }
        });
    }

    @Override
    protected void onPause() {
        super.onPause();
        LogUtil.show("ScanBleDeviceActivity.onPause()");
        unregisterReceiver(mBroadcastReceiver);// 注销广播接收器
        mAppAction.stopScan();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        LogUtil.show("ScanBleDeviceActivity.onDestroy()");
        mAppAction.disableBle();
    }

    // 生成Toolbar menu
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.bt_scan, menu);
        if (mAppAction.isScanning()) {
            menu.findItem(R.id.action_bluetooth_stop).setVisible(true);
            menu.findItem(R.id.action_bluetooth_scan).setVisible(false);
            menu.findItem(R.id.action_bluetooth_refresh).setActionView(R.layout.progress_bar);
        } else {
            menu.findItem(R.id.action_bluetooth_scan).setVisible(true);
            menu.findItem(R.id.action_bluetooth_stop).setVisible(false);
//            menu.findItem(R.id.action_bluetooth_refresh).setActionView(null);
            menu.findItem(R.id.action_bluetooth_refresh).setVisible(false);
        }
        return super.onCreateOptionsMenu(menu);
    }

    // Toolbar menu item被选中时回调
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_bluetooth_scan:
                mDeviceListAdapter.clearItems();
                // 开始扫描BLE设备
                mAppAction.startScan(new AppActionCallbackListener<ArrayList<BluetoothDevice>>() {
                    // 发现BLE设备后的回调函数
                    @Override
                    public void onSuccess(ArrayList<BluetoothDevice> data) {
                        mDeviceListAdapter.addItems(data);
                    }

                    @Override
                    public void onFailure(String errMsg) {
                        LogUtil.show(errMsg);
                    }
                });
                break;
            case R.id.action_bluetooth_stop:
                mAppAction.stopScan();// 停止扫描BLE设备
                break;
        }
        return super.onOptionsItemSelected(item);
    }

//    private void initToolbar() {
//        mToolbar = (Toolbar) findViewById(R.id.activity_main_toolbar);
//        mToolbar.setTitle(R.string.device_toolbar_title);
//        mToolbar.setLogo(R.drawable.jeckson);
//        setSupportActionBar(mToolbar);// 添加Toolbar到Activity
//    }

    private void initView() {
        mDeviceListView = (ListView) findViewById(R.id.lv_device_found);
        mDeviceListAdapter = new DeviceListAdapter(this, R.layout.listview_device_found);
        mDeviceListView.setAdapter(mDeviceListAdapter);
        mDeviceListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                mDeviceList.clear();
                mDeviceList.addAll(mDeviceListAdapter.getList());
                BluetoothDevice bluetoothDevice = mDeviceList.get(position);
                if (bluetoothDevice != null) {
                    Intent intent = new Intent(ScanBleDeviceActivity.this, DisplayServiceActivity.class);
                    intent.putExtra("name", bluetoothDevice.getName());
                    intent.putExtra("addr", bluetoothDevice.getAddress());
                    startActivity(intent);
                }
            }
        });
    }

    // 注册符合intentfilter的广播接收器
    private void initBroadcastReceiver() {
        IntentFilter inf = new IntentFilter();
        inf.addAction(AppActionImpl.ACTION_REFRESH_MENU);
        registerReceiver(mBroadcastReceiver, inf);
    }
}
