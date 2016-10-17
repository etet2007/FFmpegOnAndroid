package com.medilab.avlib;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

//import com.medilab.foolib.Bar;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(new AVSurfaceView(this));

    }
}
