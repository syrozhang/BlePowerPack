package com.syro.pp_core;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGattCharacteristic;

import java.util.ArrayList;

/**
 * Created by Syro on 2016-01-30.
 */
public interface AppAction {
    public void initBle();
    public void startScan(AppActionCallbackListener<ArrayList<BluetoothDevice>> listener);
    public void stopScan();
    public boolean isScanning();
    public void enableBle();
    public void disableBle();
    public void connectGattServer(String rmtAddress);
    public void disconnectGattServer();
    public BluetoothGattCharacteristic searchCharacteristic(String characUuid);
    public void setCharacteristicNotification(BluetoothGattCharacteristic charac, boolean enabled);
    public void readCharacteristic(BluetoothGattCharacteristic charac);
    public void writeCharacteristic(final BluetoothGattCharacteristic charac, String inputMsg);
}
