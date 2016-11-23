package com.medilab.avlib;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Rect;
import android.os.Build;
import android.os.Environment;
import android.util.AttributeSet;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

/**
 * bug:
    rtsp流崩溃。
 视频解码速度无法达到实时时只会向后延迟。
  * 11-16 08:21:56.861 2119-2119/com.medilab.avlib I/AVSurfaceView: surfaceDestroyed
 11-16 08:21:56.873 2119-2138/com.medilab.avlib A/libc: Fatal signal 11 (SIGSEGV), code 1, fault addr 0x0 in tid 2138 (Thread-177)

 为mThread里的空指针调用。
 */

public class AVSurfaceView extends SurfaceView implements SurfaceHolder.Callback{
    private String TAG = "AVSurfaceView";
    private SurfaceHolder mSurfaceHolder;//mSurfaceHolder 用于管理Surface
    private volatile boolean mThreadFlag; //线程是否运行，为false线程结束。
    public boolean mIsPlaying=false;//播放与暂停

//    private boolean pathReady = false;
//    private int counter;

    private int drawBitmapWidth = 400;
    private int drawBitmapHeight = 200;

    private Canvas canvas;

    private Bitmap mBitmap;
    private Bitmap mBitmap2;

    private int frameRateMils=0;//帧率
    //解码器对象
    private long object=0;
    private long object2=0;

    private final Object lock=new Object();

    private Thread mThread;
    private Runnable mRunnable=new Runnable() {
        @Override
        public void run() {

                while (true) {
                    synchronized (lock){

                        if(!mThreadFlag)
                            break;

                        if (!mIsPlaying)
                            continue;

                        long time1 = System.currentTimeMillis();

                        // 锁定画布，得到Canvas对象
                        canvas = mSurfaceHolder.lockCanvas();
                        if (canvas != null)
                            canvas.drawColor(Color.WHITE);
                        //对mBitmap进行回收
                        if (mBitmap != null && !mBitmap.isRecycled()) {
                            mBitmap.recycle();
                            mBitmap = null;
                        }

                        mBitmap = (Bitmap) AVdecodeInClass.readFrame(object);
//                mBitmap2= (Bitmap) AVdecodeInClass.readFrame(object2);

                        if (mBitmap != null && canvas!=null) {
                            canvas.drawBitmap(mBitmap, null, new Rect(0, 0, drawBitmapWidth, drawBitmapHeight), null);
//                      canvas.drawBitmap(mBitmap2,null,new Rect(100,100,drawBitmapWidth+100,drawBitmapHeight+100),null);
                        }

                        if (canvas != null) {
                            // 解除锁定，并提交修改内容，更新屏幕
                            mSurfaceHolder.unlockCanvasAndPost(canvas);
                        }

                        long time2 = System.currentTimeMillis();
                        long timePast = time2 - time1;

                        Log.i(TAG, "timePast " + timePast);
                        //Log.i(TAG, Thread.currentThread().toString());

                        //延时
                        if (timePast < frameRateMils) {
                            try {
                                Thread.sleep(frameRateMils - timePast);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                    }


            }

        }
    };

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

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public AVSurfaceView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        // 通过SurfaceView获得SurfaceHolder对象
        mSurfaceHolder = this.getHolder();
        // 为SurfaceHolder添加回调结构SurfaceHolder.Callback
        mSurfaceHolder.addCallback(this);
    }


    //在surface的大小发生改变时调用
    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        Log.d(TAG, "surfaceChanged");
    }

    //在创建时调用，一般在这里调用画图的线程。
    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        Log.d(TAG, "surfaceCreated");
        //获取解码器对象
//        object=AVdecodeInClass.init(Environment.getExternalStorageDirectory().getAbsolutePath() + "/demo/sintel.mp4");
        object=AVdecodeInClass.init("rtsp://184.72.239.149/vod/mp4://BigBuckBunny_175k.mov");//test trsp流


        drawBitmapWidth=AVdecodeInClass.getWidth(object);
        drawBitmapHeight=AVdecodeInClass.getHeight(object);
        Log.i(TAG, "object width height "+AVdecodeInClass.getWidth(object)+"*"+AVdecodeInClass.getHeight(object));
        Log.i(TAG, "object getFrameRateMils "+AVdecodeInClass.getFrameRateMils(object));


        //按秒数跳转
        AVdecodeInClass.seekFrameBySec(object,15);
        //获得帧率
        frameRateMils=AVdecodeInClass.getFrameRateMils(object);

//        object2=AVdecodeInClass.init(Environment.getExternalStorageDirectory().getAbsolutePath() + "/demo/record.264");
//        Log.i(TAG, "object2 width height "+AVdecodeInClass.getWidth(object2)+"*"+AVdecodeInClass.getHeight(object2));
//        Log.i(TAG, "object2 getFramerateMils "+AVdecodeInClass.getFrameRateMils(object2));

        mThreadFlag = true;
        mThread=new Thread(mRunnable);
        mThread.start();
    }

    //销毁时调用，一般在这里将画图的线程停止、释放。
    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        Log.d(TAG, "surfaceDestroyed");
        synchronized (lock) {
            mThreadFlag = false;
        }
        mIsPlaying=false;

        AVdecodeInClass.destroy(object);
        AVdecodeInClass.destroy(object2);

    }

}