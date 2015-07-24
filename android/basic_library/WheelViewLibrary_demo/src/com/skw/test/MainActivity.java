package com.skw.test;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import cn.skw.widget.WheelView;

/**
 * WheelView测试程序
 *
 * @author skywang
 * @e-mail kuiwu-wang@163.com
 */
public class MainActivity extends Activity {
    private static final String TAG = "##skywang-Main";

    private static final String[] MONTH_ARRAY = {"1月", "2月", "3月", "4月", "5月", "6月", "7月", "8月", "9月", "10月", "11月", "12月"};

    private WheelView mWheelView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        mWheelView = (WheelView)findViewById(R.id.wheel_view);
        mWheelView.setOnWheelChangeListener(new WheelView.OnWheelChangeListener() {
            @Override
            public void onSelected(int index, String value) {
                Log.d(TAG, "select: "+index+", "+value);
            }
        }); 
        mWheelView.setDataArray(MONTH_ARRAY);
    }
}
