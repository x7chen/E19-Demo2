package com.pumelotech.dev.e19_demo;

import android.bluetooth.BluetoothProfile;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationListener;
import com.amap.api.maps.AMap;
import com.amap.api.maps.AMap.OnMapClickListener;
import com.amap.api.maps.CameraUpdateFactory;
import com.amap.api.maps.LocationSource;
import com.amap.api.maps.MapView;
import com.amap.api.maps.UiSettings;
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

public class MainActivity extends AppCompatActivity implements OnMapClickListener, LocationSource,
        AMapLocationListener {
    private String TAG = MyApplication.DebugTag;

    DashboardView dialChart;
    DashboardData mDashboardData = DashboardData.INSTANCE;
    BleProfiles bleProfiles;
    Timer timer = new Timer();
    private MapView mapView;
    private AMap aMap;
    private boolean firsttouch = true;

    private OnLocationChangedListener mListener;
    private AMapLocationClient mlocationClient;
    private AMapLocationClientOption mLocationOption;
    private UiSettings mUiSettings;

    private TextView textView;
    private ImageButton btSetting;

    float BASE_ALTITUDE;
    boolean altitude_is_cal = false;


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
        if (mDashboardData.start_time == 0) {
            Date startDate = new Date();
            mDashboardData.start_time = startDate.getTime();
        }
        btSetting = (ImageButton) findViewById(R.id.bt_setting);
        btSetting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, SettingsActivity.class));
            }
        });
        ImageButton btReset = (ImageButton) findViewById(R.id.bt_reset);
        textView = (TextView) findViewById(R.id.textView);
        btReset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mDashboardData.clear();
                Date startDate = new Date();
                mDashboardData.start_time = startDate.getTime();
                cadenceFilter.clear();
                speedFilter.clear();
                cadence = 0;
                odometer = 0;
                speed = 0;
            }
        });
        mapView = (MapView) findViewById(R.id.mapView);
        mapView.onCreate(savedInstanceState);// 此方法必须重写

        updateChart();

        Toast.makeText(this,"请校准海拔",Toast.LENGTH_SHORT).show();
    }

    /**
     * 初始化AMap对象
     */
    private void init() {
        if (aMap == null) {
            aMap = mapView.getMap();
            mUiSettings = aMap.getUiSettings();
        }
        aMap.setOnMapClickListener(this);
        aMap.setLocationSource(this);// 设置定位监听
        mUiSettings.setMyLocationButtonEnabled(true); // 是否显示默认的定位按钮
        aMap.setMyLocationEnabled(true);// 是否可触发定位并显示定位层
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        int wheel_type = Integer.parseInt(preferences.getString("wheel_circumference", "1"));
        switch (wheel_type) {
            case 0:
                WHEEL_CIRCUMFERENCE_MM = (float) (25 * 25.4 * 3.14);
                break;
            case 1:
                WHEEL_CIRCUMFERENCE_MM = (float) (26 * 25.4 * 3.14);
                break;
            case 2:
                WHEEL_CIRCUMFERENCE_MM = (float) (27.5 * 25.4 * 3.14);
                break;
            case 3:
                WHEEL_CIRCUMFERENCE_MM = (float) (28 * 25.4 * 3.14);
                break;
            default:
                WHEEL_CIRCUMFERENCE_MM = 2000;
                break;
        }
        altitude_is_cal = false;
        current_altitude = Float.parseFloat(preferences.getString("current_altitude", "1"));
        preferences.registerOnSharedPreferenceChangeListener(new SharedPreferences.OnSharedPreferenceChangeListener() {
            @Override
            public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
                if (key.equals("current_altitude")) {

                    current_altitude = Float.parseFloat(sharedPreferences.getString(key, "1"));

                }
                Log.i(TAG,"new CAl");
            }
        });

    }

    float current_altitude;

    @Override
    protected void onResume() {
        super.onResume();
        mapView.onResume();
        init();
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
    SlidingFilter cadenceFilter = new SlidingFilter(20);
    SlidingFilter speedFilter = new SlidingFilter(15);
    BleProfileCallback bleProfileCallback = new BleProfileCallback() {

        CscValueParser parser = new CscValueParser(null);

        @Override
        public void onSending() {

        }

        @Override
        public void onInitialized() {

        }

        @Override
        public void onCscsUpdate(byte[] data) {
            parser.setData(data);
            if ((parser.getFlag() & 0x01) == 0x01) {
                mDashboardData.wheel_connection = true;
                int diff_time = parser.getLastWheelEventTime() - lastWheelEventTime;
                if (diff_time < 0) {
                    diff_time += 65536;
                }
                if (diff_time == 0) {
                    return;
                }
                long diff_wheel_revolutions = parser.getCumulativeWheelRevolutions()
                        - lastWheelRevolutions;
                if (diff_wheel_revolutions < 0) {
                    diff_wheel_revolutions = 0;
                }
                if (lastWheelRevolutions != 0) {
                    speed = speedFilter.updateFilter(diff_wheel_revolutions * WHEEL_CIRCUMFERENCE_MM
                            * 1000 / (diff_time * KPH_TO_MM_PER_SEC));
                    odometer += diff_wheel_revolutions * WHEEL_CIRCUMFERENCE_MM / 1000000;
                }
                lastWheelRevolutions = parser.getCumulativeWheelRevolutions();
                lastWheelEventTime = parser.getLastWheelEventTime();
                Log.i(TAG, String.format("%2.1f", speed));
            } else if ((parser.getFlag() & 0x02) == 0x02) {
                mDashboardData.cadence_connection = true;
                int diff_time = parser.getLastCrankEventTime() - lastCrankEventTime;
                if (diff_time < 0) {
                    diff_time += 65536;
                }
                if (diff_time == 0) {
                    return;
                }
                long diff_crank_revolutions = parser.getCumulativeCrankRevolutions()
                        - lastCumulativeCrankRevolutions;
                if (diff_crank_revolutions < 0) {
                    diff_crank_revolutions = 0;
                }
                if (lastCumulativeCrankRevolutions != 0) {
                    cadence = cadenceFilter.updateFilter(diff_crank_revolutions * 60 * 1000
                            / diff_time);
                }
                lastCumulativeCrankRevolutions = parser.getCumulativeCrankRevolutions();
                lastCrankEventTime = parser.getLastCrankEventTime();
                Log.i(TAG, String.format("%d", (int) cadence));
            }
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    textView.setText("前轮：" + lastWheelRevolutions + " 脚踏：" + lastCumulativeCrankRevolutions);
                }
            });

        }

        @Override
        public void onPressureUpdate(long pressure) {
            if (!altitude_is_cal) {
                BASE_ALTITUDE = current_altitude - (1000 - (float) pressure / 100) * 9;
                altitude_is_cal = true;
            }
            mDashboardData.altitude = BASE_ALTITUDE + (1000 - (float) pressure / 100) * 9;
        }
    };

    ConnectionCallback connectionCallback = new ConnectionCallback() {
        @Override
        public void onConnectionStateChange(int newState) {
            if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                mDashboardData.cadence_connection = false;
                mDashboardData.wheel_connection = false;
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

    /**
     * 激活定位
     */
    @Override
    public void activate(LocationSource.OnLocationChangedListener listener) {
        mListener = listener;
        if (mlocationClient == null) {
            mlocationClient = new AMapLocationClient(this);
            mLocationOption = new AMapLocationClientOption();
            //设置定位监听
            mlocationClient.setLocationListener(this);
            //设置为高精度定位模式
            mLocationOption.setLocationMode(AMapLocationClientOption.AMapLocationMode.Hight_Accuracy);
            mLocationOption.setInterval(2000);
            //设置定位参数
            mlocationClient.setLocationOption(mLocationOption);
            // 此方法为每隔固定时间会发起一次定位请求，为了减少电量消耗或网络流量消耗，
            // 注意设置合适的定位时间的间隔（最小间隔支持为2000ms），并且在合适时间调用stopLocation()方法来取消定位请求
            // 在定位结束后，在合适的生命周期调用onDestroy()方法
            // 在单次定位情况下，定位无论成功与否，都无需调用stopLocation()方法移除请求，定位sdk内部会移除
            mlocationClient.startLocation();
        }

    }

    /**
     * 停止定位
     */
    @Override
    public void deactivate() {
        mListener = null;
        if (mlocationClient != null) {
            mlocationClient.stopLocation();
            mlocationClient.onDestroy();
        }
        mlocationClient = null;
    }

    @Override
    public void onLocationChanged(AMapLocation aMapLocation) {
        if (mListener != null) {
            mListener.onLocationChanged(aMapLocation);// 显示系统小蓝点

//            Toast.makeText(this,aMapLocation.getProvince()+//省信息
//            aMapLocation.getCity()+//城市信息
//            aMapLocation.getDistrict()+//城区信息
//            aMapLocation.getStreet()+//街道信息
//            aMapLocation.getStreetNum(),Toast.LENGTH_SHORT).show();//街道门牌号信息)
//            mDashboardData.altitude = (float) aMapLocation.getAltitude(); //仅在AMapLocation.getProvider()是gps时有效
        }
        if (aMapLocation.getErrorCode() != 0) {
            Log.e(TAG, "location Error, ErrCode:" + aMapLocation.getErrorCode() +
                    ", errInfo:" +
                    aMapLocation.getErrorInfo());
        }
    }
}
