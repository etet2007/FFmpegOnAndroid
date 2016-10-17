package com.medilab.av;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.support.v4.view.GestureDetectorCompat;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import com.medilab.avlib.AVdecode;

/**
 * Created by Administrator on 2016/7/5.
 */

public class MySurfaceView extends SurfaceView implements SurfaceHolder.Callback,GestureDetector.OnGestureListener{
    private String TAG = "MySurfaceView";

    private SurfaceHolder mSurfaceHolder;
    private boolean ThreadFlag;
    private int counter;
    private Canvas canvas;
    private Bitmap mBitmap;
    private int frameNumber=90;

    private GestureDetectorCompat mDetector;
    private int zeroPointPosX=0;
    private int zeroPointPosY=0;

    public void setBitmap(Bitmap bitmap) {
        mBitmap = bitmap;
    }

    private Thread mThread = new Thread(new Runnable() {
        @Override
        public void run() {
            while (ThreadFlag) {

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
                    canvas.drawBitmap(mBitmap, zeroPointPosX, zeroPointPosY, null);
//                    canvas.drawBitmap(mBitmap,null,new Rect(0,0,400,400),null);
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

    public MySurfaceView(Context context) {
        super(context);
        // 通过SurfaceView获得SurfaceHolder对象
        mSurfaceHolder = this.getHolder();
        // 为SurfaceHolder添加回调结构SurfaceHolder.Callback
        mSurfaceHolder.addCallback(this);
    }

    public MySurfaceView(Context context, AttributeSet attrs) {
        super(context, attrs);
        // 通过SurfaceView获得SurfaceHolder对象
        mSurfaceHolder = this.getHolder();
        // 为SurfaceHolder添加回调结构SurfaceHolder.Callback
        mSurfaceHolder.addCallback(this);
    }

    public MySurfaceView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        // 通过SurfaceView获得SurfaceHolder对象
        mSurfaceHolder = this.getHolder();
        // 为SurfaceHolder添加回调结构SurfaceHolder.Callback
        mSurfaceHolder.addCallback(this);
    }

    public MySurfaceView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
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
        mDetector = new GestureDetectorCompat(this.getContext(),this);
        mThread.start();
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        // TODO Auto-generated method stub
        Log.i(TAG, "surfaceDestroyed");
        ThreadFlag = false;
    }


    //OnGestureListener
    @Override
    public boolean onDown(MotionEvent motionEvent) {
        Log.d(TAG,"onDown ");
        return false;
    }

    @Override
    public void onShowPress(MotionEvent motionEvent) {

    }

    @Override
    public boolean onSingleTapUp(MotionEvent motionEvent) {
        return false;
    }

    @Override
    public boolean onScroll(MotionEvent motionEvent, MotionEvent motionEvent1, float v, float v1) {
        return false;
    }

    @Override
    public void onLongPress(MotionEvent motionEvent) {
        Log.d(TAG,"onLongPress ");
    }

    @Override
    public boolean onFling(MotionEvent motionEvent, MotionEvent motionEvent1, float v, float v1) {
        Log.d(TAG,"onFling ");
        return false;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        mDetector.onTouchEvent(event);
        return super.onTouchEvent(event);
    }
}