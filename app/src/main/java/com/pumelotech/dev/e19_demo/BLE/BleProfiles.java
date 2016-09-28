package com.pumelotech.dev.e19_demo.BLE;

import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.util.Log;

import com.pumelotech.dev.e19_demo.BLE.callbacks.BleProfileCallback;
import com.pumelotech.dev.e19_demo.BLE.callbacks.NullBleProfileCallback;
import com.pumelotech.dev.e19_demo.BLE.callbacks.TransferCallback;
import com.pumelotech.dev.e19_demo.MyApplication;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

/**
 * Created by x7che on 2016/7/14.
 */
public class BleProfiles implements TransferCallback {
    private final static String TAG = MyApplication.DebugTag;
    private static final UUID CSC_SERVICE_UUID = UUID.fromString("00001816-0000-1000-8000-00805f9b34fb");
    private static final UUID CSC_MEASURE_CHAR_UUID = UUID.fromString("00002A5B-0000-1000-8000-00805f9b34fb");
    private static final UUID ENV_SERVICE_UUID = UUID.fromString("da1a1200-af00-40c6-bcda-e093af5a45db");
    private static final UUID PRESSURE_CHAR_UUID = UUID.fromString("da1a1202-af00-40c6-bcda-e093af5a45db");
    private BluetoothGattCharacteristic CRC_MEASURE_CHAR;
    public boolean IS_Ready = false;
    boolean navCharacteristicBusy = false;
    LeConnector leConnector;
    BleProfileCallback mCallback = new NullBleProfileCallback();
    static BleProfiles mBleProfiles;

    BleProfiles() {
        leConnector = LeConnector.getInstance();
        leConnector.setTransferCallback(this);

    }

    static public BleProfiles getInstance() {
        if (mBleProfiles == null) {
            mBleProfiles = new BleProfiles();
        }
        return mBleProfiles;
    }

    public void setCallback(BleProfileCallback callback) {
        mCallback = callback;
    }


    public void writeWorkCharacteristic(byte[] data) {

//            CRC_MEASURE_CHAR.setValue(data);
//            leConnector.getBluetoothGatt().writeCharacteristic(CRC_MEASURE_CHAR);
        Log.i(TAG, "CRC_MEASURE_CHAR is not null");

    }

    public void refresh() {
        if (leConnector != null) {
            if (leConnector.getConnectionState() == LeConnector.STATE_CONNECTED) {
                List<BluetoothGatt> gatts = leConnector.getBluetoothGatts();
                if (gatts == null || gatts.isEmpty()) {
                    return;
                }
                for (BluetoothGatt gatt : gatts) {
                    for (BluetoothGattService service : gatt.getServices()) {
                        if (service.getUuid().equals(CSC_SERVICE_UUID)) {
                            final List<BluetoothGattCharacteristic> characteristics = service.getCharacteristics();
                            for (final BluetoothGattCharacteristic characteristic : characteristics) {
                                Log.i(TAG, "CHAR:" + characteristic.getUuid().toString());
                                if (characteristic.getUuid().equals(CSC_MEASURE_CHAR_UUID)) {
//                                    IS_Ready = true;
                                    CRC_MEASURE_CHAR = characteristic;
                                    gatt.setCharacteristicNotification(characteristic, true);
                                    List<BluetoothGattDescriptor> descriptors = characteristic.getDescriptors();
                                    for (BluetoothGattDescriptor dp : descriptors) {
                                        dp.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
                                        gatt.writeDescriptor(dp);
                                    }
                                }
                                mCallback.onInitialized();
                                Log.i(TAG, "the characteristic is found");
                            }
                        } else if(service.getUuid().equals(ENV_SERVICE_UUID)){
                            final List<BluetoothGattCharacteristic> characteristics = service.getCharacteristics();
                            for (final BluetoothGattCharacteristic characteristic : characteristics) {
                                if (characteristic.getUuid().equals(PRESSURE_CHAR_UUID)) {
                                    final BluetoothGatt fgatt = gatt;
                                    new Timer().schedule(new TimerTask() {
                                        @Override
                                        public void run() {
                                            fgatt.setCharacteristicNotification(characteristic, true);
                                            List<BluetoothGattDescriptor> descriptors = characteristic.getDescriptors();
                                            for (BluetoothGattDescriptor dp : descriptors) {
                                                dp.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
                                                fgatt.writeDescriptor(dp);
                                            }
                                        }
                                    },2000);

                                }
                                mCallback.onInitialized();
                                Log.i(TAG, "the characteristic is found");
                            }
                        }
                    }
                }
            }
        }
    }


    @Override
    public void onCharacteristicWrite(BluetoothGattCharacteristic characteristic) {
        if (characteristic.equals(CRC_MEASURE_CHAR)) {

        }
        navCharacteristicBusy = false;
        Log.i(TAG, "new data wrote");
    }

    @Override
    public void onCharacteristicRead(BluetoothGattCharacteristic characteristic) {

        if (characteristic.equals(CRC_MEASURE_CHAR)) {
            mCallback.onCscsUpdate(CRC_MEASURE_CHAR.getValue());

        }
        Log.i(TAG, "new data read");
    }

    @Override
    public void onCharacteristicChanged(BluetoothGattCharacteristic characteristic) {

        if (characteristic.getUuid().equals(CSC_MEASURE_CHAR_UUID)) {
            mCallback.onCscsUpdate(characteristic.getValue());

        }else if(characteristic.getUuid().equals(PRESSURE_CHAR_UUID)){
            mCallback.onPressureUpdate(parserPressure(characteristic.getValue()));
        }
        Log.i(TAG, "new data notify:" + characteristic.getUuid().toString());
        StringBuilder stringBuilder = new StringBuilder();
        for (byte b : characteristic.getValue()) {
            stringBuilder.append(String.format("%02X ", b));
        }
        Log.i(TAG, stringBuilder.toString());
    }

    @Override
    public void onServicesDiscovered() {
        refresh();
    }

    private long parserPressure(byte[] data) {
        long value;
        value = data[3] & 0xFFL;
        value = (value << 8) | (data[2] & 0xFFL);
        value = (value << 8) | (data[1] & 0xFFL);
        value = (value << 8) | (data[0] & 0xFFL);
        return value;
    }
}
