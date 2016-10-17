package com.medilab.avlib;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

/**
 * Created by Administrator on 2016/7/5.
 */

public class AVSurfaceView extends SurfaceView implements SurfaceHolder.Callback{
    private String TAG = "AVSurfaceView";

    private SurfaceHolder mSurfaceHolder;
    private boolean ThreadFlag;
    private boolean pathReady = false;
    private int counter;
    private int canWidth = 400;
    private int canHeight = 240;
    private Canvas canvas;
    private Bitmap mBitmap;
    private int frameNumber=90;

    private int zeroPointPosX=0;
    private int zeroPointPosY=0;

    public void init(String path){
        AVdecode.init(path);
        pathReady = true;
    }

    public void play(){

    }

    private Thread mThread = new Thread(new Runnable() {
        @Override
        public void run() {
            while (ThreadFlag && pathReady) {

                // 锁定画布，得到Canvas对象
                canvas = mSurfaceHolder.lockCanvas();
                canvas.drawColor(Color.WHITE);

                if(mBitmap!=null&&!mBitmap.isRecycled()){
                    mBitmap.recycle();
                    mBitmap = null;
                }

                mBitmap= (Bitmap) AVdecode.readFrame(frameNumber);
                frameNumber++;
                if(mBitmap!=null) {
//                    Log.d(TAG, "MySurfaceView   drawBitmap!!!");// 16ms的间隔时间
//                    canvas.drawBitmap(mBitmap, zeroPointPosX, zeroPointPosY, null);
                    canvas.drawBitmap(mBitmap,null,new Rect(0,0,canWidth,canHeight),null);
//                    mBitmap.recycle();
                }
                if (canvas != null) {
                    // 解除锁定，并提交修改内容，更新屏幕
                    mSurfaceHolder.unlockCanvasAndPost(canvas);
                }

                try {
                    Thread.sleep(30);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }

    });

    public AVSurfaceView(Context context) {
        super(context);
        // 通过SurfaceView获得SurfaceHolder对象
        mSurfaceHolder = this.getHolder();
        // 为SurfaceHolder添加回调结构SurfaceHolder.Callback
        mSurfaceHolder.addCallback(this);
    }

    public AVSurfaceView(Context context, AttributeSet attrs) {
        super(context, attrs);
        // 通过SurfaceView获得SurfaceHolder对象
        mSurfaceHolder = this.getHolder();
        // 为SurfaceHolder添加回调结构SurfaceHolder.Callback
        mSurfaceHolder.addCallback(this);
    }

    public AVSurfaceView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        // 通过SurfaceView获得SurfaceHolder对象
        mSurfaceHolder = this.getHolder();
        // 为SurfaceHolder添加回调结构SurfaceHolder.Callback
        mSurfaceHolder.addCallback(this);
    }

    public AVSurfaceView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        // 通过SurfaceView获得SurfaceHolder对象
        mSurfaceHolder = this.getHolder();
        // 为SurfaceHolder添加回调结构SurfaceHolder.Callback
        mSurfaceHolder.addCallback(this);
    }

    //Callback带来的接口
    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        // TODO Auto-generated method stub
        Log.i(TAG, "surfaceChanged");

    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        // TODO Auto-generated method stub
        Log.i(TAG, "surfaceCreated");
        counter = 0;
        ThreadFlag = true;
        mThread.start();
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        // TODO Auto-generated method stub
        Log.i(TAG, "surfaceDestroyed");
        ThreadFlag = false;
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        Log.i(TAG, "onDetachedFromWindow");
        pathReady=false;
    }
}