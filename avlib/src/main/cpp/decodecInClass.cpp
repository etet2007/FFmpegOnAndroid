#include <jni.h>
#include "malloc.h"

#include <ffmpegdecode.h>


/*for android logs*/
#include <android/log.h>
#include <android/bitmap.h>


#define LOG_TAG "ffmpeg"
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO, LOG_TAG, __VA_ARGS__);
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, __VA_ARGS__);



extern "C"
{
JNIEXPORT jint JNICALL
Java_com_medilab_avlib_AVdecodeInClass_getFramerateMils(JNIEnv *env, jclass type, jlong object) {
    if(object!= NULL){
        ffmpegDecode* ffmpegDecodeObject= (ffmpegDecode *) object;
        return ffmpegDecodeObject->getAvg_frame_rate();
    }
    return NULL;
}

JNIEXPORT jint JNICALL
Java_com_medilab_avlib_AVdecodeInClass_getWidth(JNIEnv *env, jclass type, jlong object) {
    if(object!= NULL) {
        ffmpegDecode *ffmpegDecodeObject = (ffmpegDecode *) object;
        return ffmpegDecodeObject->width;
    }
    return NULL;
}

JNIEXPORT jint JNICALL
Java_com_medilab_avlib_AVdecodeInClass_getHeight(JNIEnv *env, jclass type, jlong object) {
    if(object!= NULL) {
        ffmpegDecode *ffmpegDecodeObject = (ffmpegDecode *) object;
        return ffmpegDecodeObject->height;
    }
    return NULL;
}

JNIEXPORT jlong JNICALL
Java_com_medilab_avlib_AVdecodeInClass_init(JNIEnv *env, jclass type, jstring videoFileName_) {
    const char *videoFileName = env->GetStringUTFChars(videoFileName_, 0);
    //make a copy to tempStr
    char *tempStr = new char[strlen(videoFileName)+1];
    strcpy(tempStr, videoFileName);
    //new 一个解码器对象
    ffmpegDecode *ffmpegDecodeObject=new ffmpegDecode(tempStr);

    env->ReleaseStringUTFChars(videoFileName_, videoFileName);
    return  (jlong )ffmpegDecodeObject;
}

JNIEXPORT jobject JNICALL
    Java_com_medilab_avlib_AVdecodeInClass_readFrame(JNIEnv *env, jclass type,jlong object, jint frameNumber) {
    if(object!= NULL) {
        // TODO
        ffmpegDecode *ffmpegDecodeObject = (ffmpegDecode *) object;
        return ffmpegDecodeObject->readFrame(env, frameNumber);
    }
    return NULL;
}

    JNIEXPORT void JNICALL
    Java_com_medilab_avlib_AVdecodeInClass_destroy(JNIEnv *env, jclass type,jlong object) {
        if(object!= NULL) {
            delete (ffmpegDecode *) object;
        }
        return;
    }

JNIEXPORT void JNICALL
Java_com_medilab_avlib_AVdecodeInClass_seekFrameBySec(JNIEnv *env, jclass type, jlong object,jint secs) {
    if(object!= NULL) {
        // TODO
        ffmpegDecode *ffmpegDecodeObject = (ffmpegDecode *) object;
        ffmpegDecodeObject->seekFrameBySec(secs);
        return ;
    }
    return ;

}
}