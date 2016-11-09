package com.medilab.avlib;

import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {
    AVSurfaceView av ;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        av = new AVSurfaceView(this);
        setContentView(av);
    }
}
