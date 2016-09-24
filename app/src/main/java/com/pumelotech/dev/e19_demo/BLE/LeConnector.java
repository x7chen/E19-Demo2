/*
 * Copyright (C) 2013 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.pumelotech.dev.e19_demo.BLE;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.le.ScanFilter;
import android.content.Context;
import android.util.Log;

import com.pumelotech.dev.e19_demo.BLE.callbacks.ConnectionCallback;
import com.pumelotech.dev.e19_demo.BLE.callbacks.NullConnectionCallback;
import com.pumelotech.dev.e19_demo.BLE.callbacks.NulltransferCallback;
import com.pumelotech.dev.e19_demo.BLE.callbacks.TransferCallback;
import com.pumelotech.dev.e19_demo.MyApplication;

import java.util.ArrayList;
import java.util.List;

import static android.bluetooth.BluetoothProfile.GATT;

/**
 * Service for managing connection and data communication with a GATT server hosted on a
 * given Bluetooth LE device.
 */
public class LeConnector extends BluetoothGattCallback {

    public static final String TAG = MyApplication.DebugTag;

    static private LeConnector mLeConnector = new LeConnector();
    BluetoothManager bluetoothManager;
    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothGatt mBluetoothGatt;
    private List<BluetoothGatt> bluetoothGattList = new ArrayList<>();
    private String mName;
    private ConnectionCallback mConnectionCallback = new NullConnectionCallback();
    private TransferCallback mTransferCallback = new NulltransferCallback();
    public static final int STATE_DISCONNECTED = 0;
    public static final int STATE_CONNECTING = 1;
    public static final int STATE_CONNECTED = 2;
    private int mConnectionState = STATE_DISCONNECTED;

    public final static String ERROR_DISCOVERY_SERVICE = "Error on discovering services";
    public final static String ERROR_WRITE_CHARACTERISTIC = "Error on writing characteristic";
    public final static String ERROR_WRITE_DESCRIPTOR = "Error on writing descriptor";


    // Implements callback methods for GATT events that the app cares about.  For example,
    // connection change and services discovered.

    @Override
    public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
        if (newState == BluetoothProfile.STATE_CONNECTED) {
            mConnectionState = STATE_CONNECTED;
            for (BluetoothGatt gat : bluetoothGattList) {
                if (gat.getDevice().equals(gat.getDevice())) {
                    bluetoothGattList.remove(gat);
                }
            }
            bluetoothGattList.add(gatt);
            mConnectionCallback.onConnectionStateChange(newState);
            Log.i(TAG, "Connected to GATT server.");
            // Attempts to discover services after successful connection.
            Log.i(TAG, "Attempting to start service discovery:" +
                    gatt.discoverServices());

        } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
            mConnectionState = STATE_DISCONNECTED;
            mConnectionCallback.onConnectionStateChange(newState);

            bluetoothGattList.remove(gatt);
//            autoConnect(mName);
        } else if (newState == BluetoothGatt.STATE_CONNECTING) {
        }
    }

    @Override
    public void onServicesDiscovered(BluetoothGatt gatt, int status) {
        if (status == BluetoothGatt.GATT_SUCCESS) {
            mTransferCallback.onServicesDiscovered();
        } else {
            mConnectionCallback.onError(ERROR_DISCOVERY_SERVICE, status);
        }
    }

    @Override
    public void onCharacteristicRead(BluetoothGatt gatt,
                                     BluetoothGattCharacteristic characteristic,
                                     int status) {
        if (status == BluetoothGatt.GATT_SUCCESS) {
            mTransferCallback.onCharacteristicRead(characteristic);
        }
    }

    @Override
    public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
        mTransferCallback.onCharacteristicWrite(characteristic);
        super.onCharacteristicWrite(gatt, characteristic, status);
    }

    @Override
    public void onCharacteristicChanged(BluetoothGatt gatt,
                                        BluetoothGattCharacteristic characteristic) {
        mTransferCallback.onCharacteristicChanged(characteristic);
    }

    @Override
    public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
        super.onDescriptorWrite(gatt, descriptor, status);
        if (status == BluetoothGatt.GATT_SUCCESS) {

        } else {
            descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
            gatt.writeDescriptor(descriptor);
        }
    }

    public List<BluetoothGatt> getBluetoothGatts() {
        return bluetoothGattList;
    }

    public int getConnectionState() {
        return mConnectionState;
    }

    public void setTransferCallback(TransferCallback callback) {
        mTransferCallback = callback;
    }

    public void autoConnect(String name, ConnectionCallback callBacks) {
        mConnectionCallback = callBacks;
        autoConnect(name);
        Log.i(TAG, "autoConnect:" + name);
    }

    private void Connect(String name) {
        final String dName = name;
        List<ScanFilter> scanFilters = new ArrayList<>();
    }

    private void autoConnect(String name) {
        if (mName == null || (!mName.equals(name))) {
            mName = name;
            mBluetoothAdapter.startLeScan(leScanCallback);
            Log.d(TAG, "Start Scan");
        }
    }

    BluetoothAdapter.LeScanCallback leScanCallback = new BluetoothAdapter.LeScanCallback() {
        @Override
        public void onLeScan(BluetoothDevice device, int rssi, byte[] scanRecord) {
            String deviceName = device.getName();

            if (deviceName == null) {
                deviceName = LeAdvertiseParser.parseAdertisedData(scanRecord).getName();

            }
            if (deviceName != null && deviceName.equals(mName)) {
//                if (mConnectionState == STATE_DISCONNECTED) {
//                    mBluetoothAdapter.stopLeScan(this);
//
//                    connect(MyApplication.context, device);
//
//                    mConnectionState = STATE_CONNECTING;
//                }
                connect(MyApplication.context, device);
            }

            Log.d(TAG, "NAME:" + deviceName + "  RSSI:" + rssi);
            for (BluetoothDevice device1 : bluetoothManager.getConnectedDevices(GATT)) {
                Log.i(TAG, "Msg:" + device1.getName());
            }
        }
    };

    private void connect(Context context, BluetoothDevice device) {
        if (device == null) {
            return;
        }
        Log.i(TAG, "connect:" + device.getAddress() + "   gatts:" + bluetoothGattList.size());
        device.connectGatt(context, true, this);

    }

    public void disconnect() {
        Log.d(TAG, "Disconnecting device");
        if (mBluetoothGatt != null) {
            mBluetoothGatt.disconnect();
        }
    }

    /**
     * After using a given BLE device, the app must call this method to ensure resources are
     * released properly.
     */
    public void close() {
        if (mBluetoothGatt == null) {
            return;
        }
        mBluetoothGatt.close();
        mBluetoothGatt = null;
    }

    public static LeConnector getInstance() {
        return mLeConnector;
    }

    private LeConnector() {
        // Initializes a Bluetooth adapter.  For API level 18 and above, get a reference to
        // BluetoothAdapter through BluetoothManager.
        bluetoothManager =
                (BluetoothManager) MyApplication.context.getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = bluetoothManager.getAdapter();
        // Checks if Bluetooth is supported on the device.
        if (mBluetoothAdapter == null) {
            Log.i(TAG, "BLE is not support!");
        }
    }
}
