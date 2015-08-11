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

    private TrendView mTrendView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        initView();
    }
        
    private void initView() {
        mTrendView = (TrendView)findViewById(R.id.trend_view);
        mTrendView.setData(new int[]{23, 26, 21, 17, 18, 19});
        mTrendView.setCurrentIndex(2);
    }
}
