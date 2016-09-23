package com.pumelotech.dev.e19_demo.BLE;

/**
 * Created by Administrator on 2016/9/22.
 */

public class CscValueParser {
    byte[] CSCValue;
    public CscValueParser(byte[] value){
        CSCValue = value;
    }
    public byte getFlag(){
        return CSCValue[0];
    }
    public long getCumulativeWheelRevolutions(){
        long value;
        value = CSCValue[4] & 0xFFL;
        value = (value << 8) | (CSCValue[3] & 0xFFL);
        value = (value << 8) | (CSCValue[2] & 0xFFL);
        value = (value << 8) | (CSCValue[1] & 0xFFL);
        return value;
    }
    public int getLastWheelEventTime(){
        int value;
        value = CSCValue[6] & 0xFF;
        value = (value << 8) | (CSCValue[5] & 0xFF);
        return value;
    }
    public int getCumulativeCrankRevolutions(){
        int value;
        value = CSCValue[8] & 0xFF;
        value = (value << 8) | (CSCValue[7] & 0xFF);
        return value;
    }

    public int getLastCrankEventTime(){
        int value;
        value = CSCValue[10] & 0xFF;
        value = (value << 8) | (CSCValue[9] & 0xFF);
        return value;
    }

}
