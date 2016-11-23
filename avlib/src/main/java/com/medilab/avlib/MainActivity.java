package com.medilab.avlib;

import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {
    AVSurfaceView mAVSurfaceView=null ;
    Button mPauseButton=null;
    Button mPlayButton=null;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);   //加载布局文件
        mAVSurfaceView = (AVSurfaceView) findViewById(R.id.avSurfaceView);
        mPauseButton = (Button) findViewById(R.id.pauseButton);
        mPauseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mAVSurfaceView.mIsPlaying=false;
            }
        });

        mPlayButton = (Button) findViewById(R.id.playButton);
        mPlayButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mAVSurfaceView.mIsPlaying=true;
            }
        });
    }
}
