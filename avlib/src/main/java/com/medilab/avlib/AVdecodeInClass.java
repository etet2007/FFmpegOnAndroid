package com.medilab.avlib;

/**
 * 创建新的类AVdecodeInClass，对应的c文件decodecInClass.c，在decodecInClass.c调用类ffmpegDecode
 */

public class AVdecodeInClass {
    static {
        System.loadLibrary("ffmpeg");
        System.loadLibrary("avcodecInClass");
    }
    //初始化函数
    public static native long init(String videoFileName);
    //从第frameNumber帧后开始返回
    public static native Object readFrame(long object,int frameNumber);

    public static native int getWidth(long object);
    public static native int getHeight(long object);
    public static native int getFramerateMils(long object);
    //根据秒数跳转到某时间戳
    public static native void seekFrameBySec(long object,int secs);
    //析构
    public static native void destroy(long object);
}
