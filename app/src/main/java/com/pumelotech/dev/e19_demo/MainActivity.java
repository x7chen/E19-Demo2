package com.pumelotech.dev.e19_demo;

import android.bluetooth.BluetoothProfile;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.WindowManager;
import android.widget.SeekBar;

import com.pumelotech.dev.e19_demo.BLE.BleProfiles;
import com.pumelotech.dev.e19_demo.BLE.CscValueParser;
import com.pumelotech.dev.e19_demo.BLE.LeConnector;
import com.pumelotech.dev.e19_demo.BLE.callbacks.BleProfileCallback;
import com.pumelotech.dev.e19_demo.BLE.callbacks.ConnectionCallback;
import com.pumelotech.dev.e19_demo.chart.DashboardData;
import com.pumelotech.dev.e19_demo.view.DashboardView;

import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

import static com.pumelotech.dev.e19_demo.R.id.dial_chart;

public class MainActivity extends AppCompatActivity {
    private String TAG = MyApplication.DebugTag;

    DashboardView dialChart;
    SeekBar seekBar;
    DashboardData mDashboardData = DashboardData.INSTANCE;
    BleProfiles bleProfiles;
    Timer timer = new Timer();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        dialChart = (DashboardView) findViewById(dial_chart);
        seekBar = (SeekBar) findViewById(R.id.seek_bar);
        seekBar.setMax(100);
        dialChart.setDashData(mDashboardData);
        LeConnector.getInstance().autoConnect("E19", connectionCallback);
        bleProfiles = BleProfiles.INSTANCE;
        bleProfiles.setCallback(bleProfileCallback);
        updateChart();

        if (mDashboardData.start_time == 0) {
            Date startDate = new Date();
            mDashboardData.start_time = startDate.getTime();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        timer.cancel();
    }

    float WHEEL_CIRCUMFERENCE_MM = 2000;
    int KPH_TO_MM_PER_SEC = 278;
    long lastWheelRevolutions = 0;
    int lastWheelEventTime = 0;
    long lastCumulativeCrankRevolutions = 0;
    int lastCrankEventTime = 0;
    float speedSmooth = 0;
    float speed;
    float odometer;
    float average_speed;
    long cadence = 0;
    BleProfileCallback bleProfileCallback = new BleProfileCallback() {
        @Override
        public void onSending() {

        }

        @Override
        public void onInitialized() {

        }

        @Override
        public void onReceived(byte[] data) {
            CscValueParser parser = new CscValueParser(data);
            if ((parser.getFlag() & 0x01) == 0x01) {
                int diff_time = parser.getLastWheelEventTime() - lastWheelEventTime;
                if (diff_time < 0) {
                    diff_time += 65536;
                }
                long diff_wheel_revolutions = parser.getCumulativeWheelRevolutions() - lastWheelRevolutions;
                if (diff_wheel_revolutions < 0) {
                    diff_wheel_revolutions = 0;
                }
                if (lastWheelRevolutions != 0) {
                    speed = diff_wheel_revolutions * WHEEL_CIRCUMFERENCE_MM * 1000 / (diff_time * KPH_TO_MM_PER_SEC);
                    odometer += diff_wheel_revolutions * WHEEL_CIRCUMFERENCE_MM / 1000000;
                }
                lastWheelRevolutions = parser.getCumulativeWheelRevolutions();
                lastWheelEventTime = parser.getLastWheelEventTime();
                Log.i(TAG, String.format("%2.1f", speed));
            }
            if ((parser.getFlag() & 0x02) == 0x02) {
                int diff_time = parser.getLastCrankEventTime() - lastCrankEventTime;
                if (diff_time < 0) {
                    diff_time += 65536;
                }
                long diff_crank_revolutions = parser.getCumulativeCrankRevolutions() - lastCumulativeCrankRevolutions;
                if (diff_crank_revolutions < 0) {
                    diff_crank_revolutions = 0;
                }
                if (lastCumulativeCrankRevolutions != 0) {
                    cadence = diff_crank_revolutions * 60 * 1000 / diff_time;
                }
                lastCumulativeCrankRevolutions = parser.getCumulativeCrankRevolutions();
                lastCrankEventTime = parser.getLastCrankEventTime();
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
                speedSmooth = speedSmooth * 9 / 10 + speed / 10;
                mDashboardData.speed = speedSmooth;
                mDashboardData.odometer = odometer;
                if (mDashboardData.trip_second != 0) {
                    average_speed = odometer * 3600 / mDashboardData.trip_second;
                }
                mDashboardData.average_speed = average_speed;
                mDashboardData.cadence = (int) cadence;
                dialChart.postInvalidate();

                Date current_date = new Date();
                mDashboardData.trip_second = (int) ((current_date.getTime() - mDashboardData.start_time) / 1000);

            }
        }, 1000, 200);
    }

}
