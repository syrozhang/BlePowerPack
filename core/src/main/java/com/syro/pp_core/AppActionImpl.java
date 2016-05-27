package com.syro.pp_core;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;

import com.syro.pp_api.BleUtil;
import com.syro.pp_data.GattProfile;
import com.syro.pp_api.LogUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Syro on 2016-01-30.
 */
public class AppActionImpl implements AppAction {
    private Context mContext;
    private BleUtil mBleUtil;
    private BluetoothGattCharacteristic mTargetCharacteristic;
    public static final String ACTION_REFRESH_MENU = "com.syro.core.ACTION_REFRESH_MENU";
    public static final int BLE_SCAN_TIME = 1000;

    public AppActionImpl(Context context) {
        this.mContext = context;
        this.mBleUtil = new BleUtil(context);
    }

    @Override
    public void initBle() {
        LogUtil.show("AppActionImpl.initBle()");
        mBleUtil.initBle();
    }

    @Override
    public void startScan(final AppActionCallbackListener<ArrayList<BluetoothDevice>> listener) {
        LogUtil.show("AppActionImpl.startScan()");
        mBleUtil.startScanBleDevice();
        Intent intent = new Intent(ACTION_REFRESH_MENU);
        mContext.sendBroadcast(intent);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                LogUtil.show("Handler().postDelayed(" + BLE_SCAN_TIME + "ms): stopLeScan()");
                mBleUtil.stopScanBleDevice();
                Intent intent = new Intent(ACTION_REFRESH_MENU);
                mContext.sendBroadcast(intent);

                if (mBleUtil.getResponseData().getObjList().size() > 0) {
                    listener.onSuccess(mBleUtil.getResponseData().getObjList());
                } else {
                    listener.onFailure("No BLE devices founded...");
                }
            }
        }, BLE_SCAN_TIME);
    }

    @Override
    public void stopScan() {
        LogUtil.show("AppActionImpl.stopScan()");
        mBleUtil.stopScanBleDevice();
        Intent intent = new Intent(ACTION_REFRESH_MENU);
        mContext.sendBroadcast(intent);
    }

    @Override
    public boolean isScanning() {
        return mBleUtil.isScanning();
    }

    @Override
    public void enableBle() {
        LogUtil.show("AppActionImpl.enableBle()");
        mBleUtil.enableBle();
    }

    @Override
    public void disableBle() {
        LogUtil.show("AppActionImpl.disableBle()");
        mBleUtil.disableBle();
    }

    @Override
    public void connectGattServer(String rmtAddress) {
        LogUtil.show("AppActionImpl.connectGattServer()");
        mBleUtil.connect(rmtAddress);
    }

    @Override
    public void disconnectGattServer() {
        LogUtil.show("AppActionImpl.disconnectGattServer()");
        mBleUtil.disconnect();
        mBleUtil.close();
    }

    @Override
    public BluetoothGattCharacteristic searchCharacteristic(String characUuid) {
        LogUtil.show("AppActionImpl.searchCharacteristic()");
        String uuid;
        List<BluetoothGattService> allGattServices = mBleUtil.getAllServices();
        for (BluetoothGattService gattService : allGattServices) {
            uuid = gattService.getUuid().toString();
            if (GattProfile.NORDIC_UART.equals(uuid)) {
                List<BluetoothGattCharacteristic> allCharacs = gattService.getCharacteristics(); // 获取Service的所有characteristics
                for (BluetoothGattCharacteristic charac : allCharacs) {
                    uuid = charac.getUuid().toString();
                    if (characUuid.equals(uuid)) {
                        mTargetCharacteristic = charac;
                    }
                }
            }
        }
        return mTargetCharacteristic;
    }

    @Override
    public void setCharacteristicNotification(BluetoothGattCharacteristic charac, boolean enabled) {
        LogUtil.show("AppActionImpl.setCharacteristicNotification()");
        mBleUtil.setCharacteristicNotification(charac, enabled);
    }

    @Override
    public void readCharacteristic(BluetoothGattCharacteristic charac) {
        LogUtil.show("AppActionImpl.readCharacteristic()");
        mBleUtil.readCharacteristic(charac);
    }

    @Override
    public void writeCharacteristic(BluetoothGattCharacteristic characteristic, String inputMsg) {
        LogUtil.show("AppActionImpl.writeCharacteristic()");
        mBleUtil.writeCharacteristic(characteristic, inputMsg);
    }

}
