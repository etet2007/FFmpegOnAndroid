package com.medilab.avlib;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Rect;
import android.os.Environment;
import android.util.AttributeSet;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

/**
 * bug:旋转屏幕时崩溃。
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
    private Bitmap mBitmap2;
    private int frameNumber=1;

    private int zeroPointPosX=0;
    private int zeroPointPosY=0;

    private long object=0;
    private long object2=0;

    private Thread mThread = new Thread(new Runnable() {
        @Override
        public void run() {
            while (ThreadFlag && pathReady) {
                // 锁定画布，得到Canvas对象
                canvas = mSurfaceHolder.lockCanvas();
                canvas.drawColor(Color.WHITE);
                //对mBitmap进行回收
                if(mBitmap!=null&&!mBitmap.isRecycled()){
                    mBitmap.recycle();
                    mBitmap = null;
                }

                mBitmap= (Bitmap) AVdecodeInClass.readFrame(object,50);
                mBitmap2= (Bitmap) AVdecodeInClass.readFrame(object2,50);
                frameNumber++;

                if(mBitmap!=null) {
//                    Log.d(TAG, "MySurfaceView   drawBitmap!!!");// 16ms的间隔时间
                    canvas.drawBitmap(mBitmap,null,new Rect(0,0,canWidth,canHeight),null);
                    canvas.drawBitmap(mBitmap2,null,new Rect(100,100,canWidth+100,canHeight+100),null);
//                    mBitmap.recycle();
                }

                if (canvas != null) {
                    // 解除锁定，并提交修改内容，更新屏幕
                    mSurfaceHolder.unlockCanvasAndPost(canvas);
                }
                //延时
                try {
                    Thread.sleep(10);
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

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        Log.i(TAG, "onDetachedFromWindow");
        pathReady=false;
    }

    //在surface的大小发生改变时调用
    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        Log.i(TAG, "surfaceChanged");
    }

    //在创建时调用，一般在这里调用画图的线程。
    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        // TODO Auto-generated method stub
        object=AVdecodeInClass.init(Environment.getExternalStorageDirectory().getAbsolutePath() + "/demo/sintel.mp4");
//        object=AVdecodeInClass.init("rtsp://184.72.239.149/vod/mp4://BigBuckBunny_175k.mov");//test trsp流
        object2=AVdecodeInClass.init(Environment.getExternalStorageDirectory().getAbsolutePath() + "/demo/record.264");

        AVdecodeInClass.seekFrameBySec(object,15);//test seekFrame

        Log.i(TAG, "object width height"+AVdecodeInClass.getWidth(object)+"*"+AVdecodeInClass.getHeight(object));
        Log.i(TAG, "object2 width height"+AVdecodeInClass.getWidth(object2)+"*"+AVdecodeInClass.getHeight(object2));
        Log.i(TAG, "object getFramerateMils"+AVdecodeInClass.getFramerateMils(object));
        Log.i(TAG, "object2 getFramerateMils"+AVdecodeInClass.getFramerateMils(object2));
        pathReady = true;
        ThreadFlag = true;
        mThread.start();

    }

    //销毁时调用，一般在这里将画图的线程停止、释放。
    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        // TODO Auto-generated method stub
        Log.i(TAG, "surfaceDestroyed");
        ThreadFlag = false;

        AVdecodeInClass.destroy(object);
        AVdecodeInClass.destroy(object2);
    }

}