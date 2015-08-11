package com.skw.test;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.skw.java.util.Math;

public class MainActivity extends Activity {
    private static final String TAG = "##skywang-Main";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        initView();
    }
        
    private void initView() {
        int ret = Math.plus(5, 10);
        TextView tvShow = (TextView)findViewById(R.id.tv);
        tvShow.setText("ret: "+ret);
    }
}
