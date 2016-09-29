package com.pumelotech.dev.e19_demo.chart;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;

import com.pumelotech.dev.e19_demo.MyApplication;

import org.xclcharts.renderer.IRender;

import java.util.Locale;

/**
 * Created by Administrator on 2016/9/20.
 */

public class DashboardChart implements IRender {

    private DashboardData dashboardData;
    private float SPEED_BASE_Y = 20;

    private float CADENCE_BASE_X = 0;
    private float CADENCE_BASE_Y = 460;

    private float ODOMETER_BASE_X = 560;
    private float ODOMETER_BASE_Y = 20;

    private float TRIP_TIME_BASE_X = 560;
    private float TRIP_TIME_BASE_Y = 240;

    private float AVERAGE_SPEED_BASE_X = 560;
    private float AVERAGE_SPEED_BASE_Y = 460;

    private float ALTITUDE_BASE_X = 560;
    private float ALTITUDE_BASE_Y = 680;
    public DashboardChart() {
        initialize();
    }

    void initialize() {
        dashboardData = DashboardData.INSTANCE;
    }

    public void setDashboardData(DashboardData data) {
        dashboardData = data;
    }

    void drawSpeed(Canvas canvas) {
        //绘制边框
        Paint rangePaint = new Paint();
        Path path = new Path();
        float SPEED_BASE_X = 0;
        path.moveTo(SPEED_BASE_X + 80, SPEED_BASE_Y + 40);
        path.lineTo(SPEED_BASE_X + 40, SPEED_BASE_Y + 40);
        path.lineTo(SPEED_BASE_X + 40, SPEED_BASE_Y + 400);
        path.lineTo(SPEED_BASE_X + 600, SPEED_BASE_Y + 400);
        path.lineTo(SPEED_BASE_X + 600, SPEED_BASE_Y + 40);
        path.lineTo(SPEED_BASE_X + 250, SPEED_BASE_Y + 40);
        rangePaint.setStyle(Paint.Style.STROKE);
        rangePaint.setColor(Color.RED);
        rangePaint.setStrokeWidth(2);
        canvas.drawPath(path, rangePaint);
        //绘制标题
        Paint titlePaint = new Paint();
        titlePaint.setTypeface(MyApplication.fontPhotonicaRegular);
        titlePaint.setColor(Color.RED);
        titlePaint.setTextSize(30);
        canvas.drawText("speed", SPEED_BASE_X + 90, SPEED_BASE_Y + 50, titlePaint);
        //绘制数字
        Paint numPaint = new Paint();
        numPaint.setTypeface(MyApplication.fontPhotonicaRegular);
        numPaint.setColor(Color.RED);
        numPaint.setTextSize(150);
        canvas.drawText(String.format(Locale.CHINA, "%2.1f", dashboardData.speed), SPEED_BASE_X + 70, SPEED_BASE_Y + 250, numPaint);
        //绘制单位
        Paint unitPaint = new Paint();
        unitPaint.setColor(Color.RED);
        unitPaint.setTextSize(60);
        unitPaint.setFakeBoldText(true);
        canvas.drawText("Km/h", SPEED_BASE_X + 400, SPEED_BASE_Y + 350, unitPaint);

        Paint barPaint = new Paint();
        if (dashboardData.speed < 15) {
            barPaint.setColor(0xFF00B000);
        } else if (dashboardData.speed < 30) {
            barPaint.setColor(0xFFF0C000);
        } else {
            barPaint.setColor(0xFFE00000);
        }
        barPaint.setStrokeWidth(50);
        canvas.drawLine(SPEED_BASE_X + 100, SPEED_BASE_Y + 330, SPEED_BASE_X + 100 + dashboardData.speed * 5, SPEED_BASE_Y + 330, barPaint);

        Paint connectionPaint = new Paint();
        connectionPaint.setStrokeMiter(1);
        connectionPaint.setStyle(Paint.Style.FILL);
        if(dashboardData.wheel_connection){
            connectionPaint.setColor(0xFF00A000);
        }else {
            connectionPaint.setColor(Color.RED);
        }
        canvas.drawCircle(SPEED_BASE_X+550,SPEED_BASE_Y+100,20,connectionPaint);
    }

    void drawCadence(Canvas canvas) {
        //绘制边框
        Paint rangePaint = new Paint();
        Path path = new Path();
        path.moveTo(CADENCE_BASE_X + 80, CADENCE_BASE_Y + 40);
        path.lineTo(CADENCE_BASE_X + 40, CADENCE_BASE_Y + 40);
        path.lineTo(CADENCE_BASE_X + 40, CADENCE_BASE_Y + 400);
        path.lineTo(CADENCE_BASE_X + 600, CADENCE_BASE_Y + 400);
        path.lineTo(CADENCE_BASE_X + 600, CADENCE_BASE_Y + 40);
        path.lineTo(CADENCE_BASE_X + 310, CADENCE_BASE_Y + 40);
        rangePaint.setStyle(Paint.Style.STROKE);
        rangePaint.setColor(Color.RED);
        rangePaint.setStrokeWidth(2);
        canvas.drawPath(path, rangePaint);
        //绘制标题
        Paint titlePaint = new Paint();
        titlePaint.setTypeface(MyApplication.fontPhotonicaRegular);
        titlePaint.setColor(Color.RED);
        titlePaint.setTextSize(30);
        canvas.drawText("cadence", CADENCE_BASE_X + 90, CADENCE_BASE_Y + 50, titlePaint);
        //绘制数字
        Paint numPaint = new Paint();
        numPaint.setTypeface(MyApplication.fontPhotonicaRegular);
        numPaint.setColor(Color.RED);
        numPaint.setTextSize(150);
        canvas.drawText(String.format(Locale.CHINA, "%d", dashboardData.cadence), CADENCE_BASE_X + 70, CADENCE_BASE_Y + 250, numPaint);
        //绘制单位
        Paint unitPaint = new Paint();
        unitPaint.setColor(Color.RED);
        unitPaint.setTextSize(60);
        unitPaint.setFakeBoldText(true);
        canvas.drawText("RPM", CADENCE_BASE_X + 400, CADENCE_BASE_Y + 350, unitPaint);

        Paint connectionPaint = new Paint();
        connectionPaint.setStrokeMiter(1);
        connectionPaint.setStyle(Paint.Style.FILL);
        if(dashboardData.cadence_connection){
            connectionPaint.setColor(0xFF00A000);
        }else {
            connectionPaint.setColor(Color.RED);
        }
        canvas.drawCircle(CADENCE_BASE_X+550,CADENCE_BASE_Y+100,20,connectionPaint);
    }
    void drawOdometer(Canvas canvas) {
        //绘制标题
        Paint titlePaint = new Paint();
        titlePaint.setTypeface(MyApplication.fontPhotonicaRegular);
        titlePaint.setColor(Color.RED);
        titlePaint.setTextSize(30);
        canvas.drawText("odometer", ODOMETER_BASE_X + 90, ODOMETER_BASE_Y + 50, titlePaint);
        //绘制数字
        Paint numPaint = new Paint();
        numPaint.setTypeface(MyApplication.fontPhotonicaStraight);
        numPaint.setColor(Color.RED);
        numPaint.setTextSize(60);
        canvas.drawText(String.format(Locale.CHINA, "%6.2f", dashboardData.odometer), ODOMETER_BASE_X + 70, ODOMETER_BASE_Y + 140, numPaint);
        //绘制单位
        Paint unitPaint = new Paint();
        unitPaint.setColor(Color.RED);
        unitPaint.setTextSize(40);
        unitPaint.setFakeBoldText(true);
        canvas.drawText("Km", ODOMETER_BASE_X + 400, ODOMETER_BASE_Y + 140, unitPaint);
    }
    void drawTripTime(Canvas canvas) {
        //绘制标题
        Paint titlePaint = new Paint();
        titlePaint.setTypeface(MyApplication.fontPhotonicaRegular);
        titlePaint.setColor(Color.RED);
        titlePaint.setTextSize(30);
        canvas.drawText("trip time", TRIP_TIME_BASE_X + 90, TRIP_TIME_BASE_Y + 50, titlePaint);
        //绘制数字
        Paint numPaint = new Paint();
        numPaint.setTypeface(MyApplication.fontPhotonicaStraight);
        numPaint.setColor(Color.RED);
        numPaint.setTextSize(60);
        canvas.drawText(String.format(Locale.CHINA, "%02d:%02d:%02d", dashboardData.trip_second/3600,
                dashboardData.trip_second/60%60,dashboardData.trip_second%60),
                TRIP_TIME_BASE_X + 100, TRIP_TIME_BASE_Y + 140, numPaint);
    }
    void drawAverageSpeed(Canvas canvas) {
        //绘制标题
        Paint titlePaint = new Paint();
        titlePaint.setTypeface(MyApplication.fontPhotonicaRegular);
        titlePaint.setColor(Color.RED);
        titlePaint.setTextSize(30);
        canvas.drawText("average speed", AVERAGE_SPEED_BASE_X + 90, AVERAGE_SPEED_BASE_Y + 50, titlePaint);
        //绘制数字
        Paint numPaint = new Paint();
        numPaint.setTypeface(MyApplication.fontPhotonicaStraight);
        numPaint.setColor(Color.RED);
        numPaint.setTextSize(60);
        canvas.drawText(String.format(Locale.CHINA, "%4.1f", dashboardData.average_speed), AVERAGE_SPEED_BASE_X + 150, AVERAGE_SPEED_BASE_Y + 140, numPaint);
        //绘制单位
        Paint unitPaint = new Paint();
        unitPaint.setColor(Color.RED);
        unitPaint.setTextSize(40);
        unitPaint.setFakeBoldText(true);
        canvas.drawText("Km/h", AVERAGE_SPEED_BASE_X + 400, AVERAGE_SPEED_BASE_Y + 140, unitPaint);
    }
    void drawAltitude(Canvas canvas) {
        //绘制标题
        Paint titlePaint = new Paint();
        titlePaint.setTypeface(MyApplication.fontPhotonicaRegular);
        titlePaint.setColor(Color.RED);
        titlePaint.setTextSize(30);
        canvas.drawText("altitude", ALTITUDE_BASE_X + 90, ALTITUDE_BASE_Y + 50, titlePaint);
        //绘制数字
        Paint numPaint = new Paint();
        numPaint.setTypeface(MyApplication.fontPhotonicaStraight);
        numPaint.setColor(Color.RED);
        numPaint.setTextSize(60);
        canvas.drawText(String.format(Locale.CHINA, "%4.1f", dashboardData.altitude), ALTITUDE_BASE_X + 150, ALTITUDE_BASE_Y + 140, numPaint);
        //绘制单位
        Paint unitPaint = new Paint();
        unitPaint.setColor(Color.RED);
        unitPaint.setTextSize(40);
        unitPaint.setFakeBoldText(true);
        canvas.drawText("Meter", ALTITUDE_BASE_X + 400, ALTITUDE_BASE_Y + 140, unitPaint);
    }

    @Override
    public boolean render(Canvas canvas) throws Exception {
        drawSpeed(canvas);
        drawCadence(canvas);
        drawOdometer(canvas);
        drawTripTime(canvas);
        drawAverageSpeed(canvas);
        drawAltitude(canvas);
        return true;
    }
}
