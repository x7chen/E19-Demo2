package com.pumelotech.dev.e19_demo.BLE;

import android.util.Log;

import com.pumelotech.dev.e19_demo.MyApplication;

/**
 * Created by Administrator on 2016/9/22.
 */

public class CscValueParser {
    byte[] CSCValue;

    public CscValueParser(byte[] value) {
        CSCValue = value;
    }

    public void setData(byte[] value) {
        CSCValue = value;
    }

    public byte getFlag() {
        return CSCValue[0];
    }

    public long getCumulativeWheelRevolutions() {
        long value;
        if ((getFlag() & 0x01) == 0x01) {
            value = CSCValue[4] & 0xFFL;
            value = (value << 8) | (CSCValue[3] & 0xFFL);
            value = (value << 8) | (CSCValue[2] & 0xFFL);
            value = (value << 8) | (CSCValue[1] & 0xFFL);
        } else {
            value = 0;
        }
        return value;
    }

    public int getLastWheelEventTime() {
        int value;
        if ((getFlag() & (byte)0x01) == 0x01) {
            value = CSCValue[6] & 0xFF;
            value = (value << 8) | (CSCValue[5] & 0xFF);
        } else {
            value = 0;
        }
        return value;
    }

    public int getCumulativeCrankRevolutions() {
        int value;
        if ((getFlag() & 0x02) == 0x02) {
            if ((getFlag() & 0x01) == 0x01) {
                value = CSCValue[8] & 0xFF;
                value = (value << 8) | (CSCValue[7] & 0xFF);
            } else {
                value = CSCValue[2] & 0xFF;
                value = (value << 8) | (CSCValue[1] & 0xFF);
            }
        } else {
            value = 0;
        }
        return value;
    }

    public int getLastCrankEventTime() {
        int value;
        if ((getFlag() & 0x02) == 0x02) {
            if ((getFlag() & 0x01) == 0x01) {
                value = CSCValue[10] & 0xFF;
                value = (value << 8) | (CSCValue[9] & 0xFF);
            } else {
                value = CSCValue[4] & 0xFF;
                value = (value << 8) | (CSCValue[3] & 0xFF);
            }
        } else {
            value = 0;
        }
        return value;
    }

}
