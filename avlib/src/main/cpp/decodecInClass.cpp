#include <jni.h>
#include "malloc.h"

#include <wchar.h>
#include <ffmpegdecode.h>

#include <libavcodec/avcodec.h>
#include <libavformat/avformat.h>
#include <libswscale/swscale.h>

/*for android logs*/
#include <android/log.h>
#include <android/bitmap.h>


#define LOG_TAG "ffmpeg"
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO, LOG_TAG, __VA_ARGS__);
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, __VA_ARGS__);

jobject createBitmap(JNIEnv *pEnv, int pWidth, int pHeight) {
    int i;
    //get Bitmap class and createBitmap method ID
    jclass javaBitmapClass = (jclass)(*pEnv)->FindClass(pEnv, "android/graphics/Bitmap");
    jmethodID mid = (*pEnv)->GetStaticMethodID(pEnv, javaBitmapClass, "createBitmap", "(IILandroid/graphics/Bitmap$Config;)Landroid/graphics/Bitmap;");
    //create Bitmap.Config
    //reference: https://forums.oracle.com/thread/1548728
    const wchar_t* configName = L"ARGB_8888";
    int len = wcslen(configName);
    jstring jConfigName;
    if (sizeof(wchar_t) != sizeof(jchar)) {
        //wchar_t is defined as different length than jchar(2 bytes)
        jchar* str = (jchar*)malloc((len+1)*sizeof(jchar));
        for (i = 0; i < len; ++i) {
            str[i] = (jchar)configName[i];
        }
        str[len] = 0;
        jConfigName = (*pEnv)->NewString(pEnv, (const jchar*)str, len);
    } else {
        //wchar_t is defined same length as jchar(2 bytes)
        jConfigName = (*pEnv)->NewString(pEnv, (const jchar*)configName, len);
    }
    jclass bitmapConfigClass = (*pEnv)->FindClass(pEnv, "android/graphics/Bitmap$Config");
    jobject javaBitmapConfig = (*pEnv)->CallStaticObjectMethod(pEnv, bitmapConfigClass,
                                                               (*pEnv)->GetStaticMethodID(pEnv, bitmapConfigClass, "valueOf", "(Ljava/lang/String;)Landroid/graphics/Bitmap$Config;"), jConfigName);
    //create the bitmap
    return (*pEnv)->CallStaticObjectMethod(pEnv, javaBitmapClass, mid, pWidth, pHeight, javaBitmapConfig);
}

JNIEXPORT jlong JNICALL
Java_com_medilab_avlib_AVdecodeInClass_init(JNIEnv *env, jclass type, jstring videoFileName_) {
    const char *videoFileName = env->GetStringUTFChars(videoFileName_, 0);

    // TODO
    ffmpegDecode ffmpegDecodeObject=new ffmpegDecode(videoFileName);

//    env->ReleaseStringUTFChars(videoFileName_, videoFileName);
}


JNIEXPORT jobject JNICALL
                  Java_com_medilab_avlib_AVdecodeInClass_readFrame(JNIEnv *env, jclass type, jint frameNumber) {

    // TODO

}

JNIEXPORT void JNICALL
Java_com_medilab_avlib_AVdecodeInClass_destroy(JNIEnv *env, jclass type) {

// TODO

}