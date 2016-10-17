package com.medilab.avlib;

/**
 * Created by Lyan200 on 2016/10/8.
 */

public class AVdecode {
    static {
//        System.loadLibrary("ffmpeg");
//        System.loadLibrary("avlib");
        System.loadLibrary("libdecodec");
    }
    //初始化函数
    public static native int init(String videoFileName);
    //向后读取frameNumber帧
    public static native Object readFrame(int frameNumber);
    //析构
    public static native void destroy();
}
