package com.pumelotech.dev.e19_demo.BLE.utils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2016/9/23.
 */

public class SlidingFilter {
    List<Float> filter = new ArrayList<>();
    int size;

    public SlidingFilter(int size) {
        this.size = size;
    }

    public float updateFilter(float value) {
        float output = 0;
        filter.add(value);
        if (filter.size() > size) {
            filter.remove(0);
        }
        for (float f : filter) {
            output += f / filter.size();
        }
        return output;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }
}