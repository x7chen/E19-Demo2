package com.pumelotech.dev.e19_demo.view;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;

import com.pumelotech.dev.e19_demo.chart.DashboardChart;
import com.pumelotech.dev.e19_demo.chart.DashboardData;

import org.xclcharts.view.GraphicalView;

/**
 * Created by Administrator on 2016/9/20.
 */

public class DashboardView extends GraphicalView {

    DashboardChart dashboardChart = new DashboardChart();
    DashboardData mDashboarData;

    public DashboardView(Context context) {
        super(context);
        initChartView();
    }

    public DashboardView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initChartView();
    }


    public DashboardView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        initChartView();
    }

    public void setDashData(DashboardData data) {
        mDashboarData = data;
        dashboardChart.setDashboardData(data);
    }

    protected void initChartView() {
        // dashboardChart = new DashboardChart();
    }

    @Override
    public void render(Canvas canvas) {
        try {
            dashboardChart.render(canvas);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
