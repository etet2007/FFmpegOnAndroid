package com.medilab.avlib;

import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;

//import com.medilab.foolib.Bar;

public class MainActivity extends AppCompatActivity {
    AVSurfaceView av ;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        av = new AVSurfaceView(this);
        setContentView(av);
        av.init(Environment.getExternalStorageDirectory().getAbsolutePath() + "/demo/c1.h265");
    }
}
