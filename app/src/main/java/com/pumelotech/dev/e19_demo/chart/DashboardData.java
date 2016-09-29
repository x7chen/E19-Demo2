package com.pumelotech.dev.e19_demo.chart;

/**
 * Created by Administrator on 2016/9/20.
 */

public enum DashboardData {
    INSTANCE;
    public float speed;
    public int cadence;
    public float odometer;
    public int trip_second;
    public float average_speed;
    public float altitude;
    public long start_time;
    public boolean cadence_connection;
    public boolean wheel_connection;

    public void clear() {
        speed = 0;
        cadence = 0;
        odometer = 0;
        trip_second = 0;
        average_speed = 0;
        altitude = 0;
        start_time = 0;

    }
}
