package com.medilab.avlib;

import android.graphics.Bitmap;
import android.graphics.Rect;
import android.view.SurfaceHolder;

/**
 * 创建新的类AVdecodeInClass，对应的native文件decodecInClass.c，在decodecInClass.c中调用类ffmpegDecode
 */

public class AVdecodeInClass {
    static {
        System.loadLibrary("ffmpeg");
        System.loadLibrary("avcodecInClass");
    }

//    SurfaceHolder
    //初始化函数，返回初始化的对象。
    public static native long init(String videoFileName);//不知道如果加载错误如何控制
    //读取某一帧图像，返回Bitmap对象。
    public static native Object readFrame(long object);

    public static native int getWidth(long object);
    public static native int getHeight(long object);
    public static native int getFrameRateMils(long object);
    //根据秒数跳转到某时间戳
    public static native void seekFrameBySec(long object,int secs);
    //析构
    public static native void destroy(long object);

    public void setDisplay(SurfaceHolder sh) {

    }


    }
//接口
interface DecodeEvent{
    void onNewFrame(Bitmap bitmap);
}