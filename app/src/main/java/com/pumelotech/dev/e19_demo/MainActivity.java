package com.pumelotech.dev.e19_demo;

import android.bluetooth.BluetoothProfile;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.WindowManager;
import android.widget.Button;

import com.amap.api.maps.AMap;
import com.amap.api.maps.AMap.OnMapClickListener;
import com.amap.api.maps.CameraUpdateFactory;
import com.amap.api.maps.MapView;
import com.amap.api.maps.model.CameraPosition;
import com.amap.api.maps.model.LatLng;
import com.pumelotech.dev.e19_demo.BLE.BleProfiles;
import com.pumelotech.dev.e19_demo.BLE.CscValueParser;
import com.pumelotech.dev.e19_demo.BLE.LeConnector;
import com.pumelotech.dev.e19_demo.BLE.callbacks.BleProfileCallback;
import com.pumelotech.dev.e19_demo.BLE.callbacks.ConnectionCallback;
import com.pumelotech.dev.e19_demo.BLE.utils.SlidingFilter;
import com.pumelotech.dev.e19_demo.chart.DashboardData;
import com.pumelotech.dev.e19_demo.view.DashboardView;

import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

import static com.pumelotech.dev.e19_demo.R.id.dial_chart;

public class MainActivity extends AppCompatActivity implements OnMapClickListener {
    private String TAG = MyApplication.DebugTag;

    DashboardView dialChart;
    DashboardData mDashboardData = DashboardData.INSTANCE;
    BleProfiles bleProfiles;
    Timer timer = new Timer();
    private MapView mapView;
    private AMap aMap;
    private boolean firsttouch = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        dialChart = (DashboardView) findViewById(dial_chart);
        dialChart.setDashData(mDashboardData);
        bleProfiles = BleProfiles.getInstance();
        LeConnector.getInstance().autoConnect("E19", connectionCallback);
        bleProfiles.setCallback(bleProfileCallback);
        updateChart();

        if (mDashboardData.start_time == 0) {
            Date startDate = new Date();
            mDashboardData.start_time = startDate.getTime();
        }

        mapView = (MapView) findViewById(R.id.mapView);
        mapView.onCreate(savedInstanceState);// 此方法必须重写
        init();
    }

    /**
     * 初始化AMap对象
     */
    private void init() {
        if (aMap == null) {
            aMap = mapView.getMap();
        }
        aMap.setOnMapClickListener(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mapView.onPause();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
        timer.cancel();
    }

    float WHEEL_CIRCUMFERENCE_MM = 2000;
    int KPH_TO_MM_PER_SEC = 278;
    long lastWheelRevolutions = 0;
    int lastWheelEventTime = 0;
    long lastCumulativeCrankRevolutions = 0;
    int lastCrankEventTime = 0;
    float speed;
    float odometer;
    float average_speed;
    float cadence = 0;
    SlidingFilter cadenceFilter = new SlidingFilter(30);
    SlidingFilter speedFilter = new SlidingFilter(20);
    BleProfileCallback bleProfileCallback = new BleProfileCallback() {

        CscValueParser parser = new CscValueParser(null);

        @Override
        public void onSending() {

        }

        @Override
        public void onInitialized() {

        }

        @Override
        public void onReceived(byte[] data) {
            parser.setData(data);
            if ((parser.getFlag() & 0x01) == 0x01) {
                int diff_time = parser.getLastWheelEventTime() - lastWheelEventTime;
                if (diff_time < 0) {
                    diff_time += 65536;
                }
                if (diff_time == 0) {
                    return;
                }
                long diff_wheel_revolutions = parser.getCumulativeWheelRevolutions() - lastWheelRevolutions;
                if (diff_wheel_revolutions < 0) {
                    diff_wheel_revolutions = 0;
                }
                if (lastWheelRevolutions != 0) {
                    speed = speedFilter.updateFilter(diff_wheel_revolutions * WHEEL_CIRCUMFERENCE_MM * 1000 / (diff_time * KPH_TO_MM_PER_SEC));
                    odometer += diff_wheel_revolutions * WHEEL_CIRCUMFERENCE_MM / 1000000;
                }
                lastWheelRevolutions = parser.getCumulativeWheelRevolutions();
                lastWheelEventTime = parser.getLastWheelEventTime();
                Log.i(TAG, String.format("%2.1f", speed));
            } else if ((parser.getFlag() & 0x02) == 0x02) {
                int diff_time = parser.getLastCrankEventTime() - lastCrankEventTime;
                if (diff_time < 0) {
                    diff_time += 65536;
                }
                if (diff_time == 0) {
                    return;
                }
                long diff_crank_revolutions = parser.getCumulativeCrankRevolutions() - lastCumulativeCrankRevolutions;
                if (diff_crank_revolutions < 0) {
                    diff_crank_revolutions = 0;
                }
                if (lastCumulativeCrankRevolutions != 0) {
                    cadence = cadenceFilter.updateFilter(diff_crank_revolutions * 60 * 1000 / diff_time);
                }
                lastCumulativeCrankRevolutions = parser.getCumulativeCrankRevolutions();
                lastCrankEventTime = parser.getLastCrankEventTime();
                Log.i(TAG, String.format("%d", (int) cadence));
            }
        }
    };

    ConnectionCallback connectionCallback = new ConnectionCallback() {
        @Override
        public void onConnectionStateChange(int newState) {
            if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                bleProfiles.IS_Ready = false;
            }
        }

        @Override
        public void onError(String message, int errorCode) {

        }

        @Override
        public void onDeviceNotSupported() {

        }
    };

    void updateChart() {
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                mDashboardData.speed = speed;
                mDashboardData.odometer = odometer;
                if (mDashboardData.trip_second != 0) {
                    average_speed = odometer * 3600 / mDashboardData.trip_second;
                }
                mDashboardData.average_speed = average_speed;
                mDashboardData.cadence = (int) cadence;
                dialChart.postInvalidate();

                Date current_date = new Date();
                mDashboardData.trip_second = (int) ((current_date.getTime() - mDashboardData.start_time) / 1000);

//                if(!bleProfiles.IS_Ready){
//                    bleProfiles.refresh();
//                }
            }
        }, 1000, 200);
    }

    @Override
    public void onMapClick(LatLng latLng) {
        if (firsttouch) {
            firsttouch = false;
            aMap.animateCamera(CameraUpdateFactory.newCameraPosition(new CameraPosition(new LatLng(39.92463, 116.389139), 18, 0, 0)), 1500, new AMap.CancelableCallback() {

                @Override
                public void onFinish() {
                    aMap.moveCamera(CameraUpdateFactory.changeTilt(60));
                    try {
                        Thread.sleep(2000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    aMap.animateCamera(CameraUpdateFactory.changeBearing(90), 2000, null);

                }

                @Override
                public void onCancel() {

                }
            });

        }
    }
}
