package com.pumelotech.dev.e19_demo.BLE.callbacks;

/**
 * Created by Administrator on 2016/7/15.
 */
public interface BleProfileCallback {
    void onSending();
    void onInitialized();
    void onCscsUpdate(byte[] data);
    void onPressureUpdate(long pressure);
}
