package com.skw.test;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.skw.lib.widget.TrendView;

/**
 * FrameLayout测试程序
 *
 * @author skywang
 * @e-mail kuiwu-wang@163.com
 */
public class MainActivity extends Activity {
    private static final String TAG = "##skywang-Main";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        initView();
    }
        
    private void initView() {
        TrendView dayTrend = (TrendView)findViewById(R.id.trend_view_day);
        dayTrend.setData(new int[]{23, 26, 21, 17, 18, 19});
        dayTrend.setCurrentIndex(2);

        TrendView nightTrend = (TrendView)findViewById(R.id.trend_view_night);
        nightTrend.setData(new int[]{18, 20, 17, 10, 13, 14});
        nightTrend.setCurrentIndex(3);
    }
}
