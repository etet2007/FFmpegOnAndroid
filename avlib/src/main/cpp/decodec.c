#include <jni.h>
#include "malloc.h"

#include <libavcodec/avcodec.h>
#include <libavformat/avformat.h>
#include <libswscale/swscale.h>
#include <libavutil/pixfmt.h>

#include <stdio.h>
#include <wchar.h>


/*for android logs*/
#include <android/log.h>
#include <android/bitmap.h>

#define LOG_TAG "ffmpeg"
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO, LOG_TAG, __VA_ARGS__);
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, __VA_ARGS__);


AVFormatContext *pFormatCtx = NULL;
int             i=0, videoStream;
AVCodecContext  *pCodecCtx = NULL;
AVCodec         *pCodec = NULL;
AVFrame         *pFrame = NULL;
AVFrame         *pFrameRGBA = NULL;
AVPacket        packet;
int             frameFinished;
jobject			bitmap;
void* 			buffer;
int ret;
AVDictionary    *optionsDict = NULL;
struct SwsContext      *sws_ctx = NULL;
char *videoFileName;
int currentFrameNumber=0;

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


JNIEXPORT jint JNICALL
Java_com_medilab_avlib_AVdecode_init(JNIEnv *pEnv, jclass type, jstring pFileName) {
    // Register all formats and codecs
    av_register_all();
    //get C string from JNI jstring
    videoFileName = (char *)(*pEnv)->GetStringUTFChars(pEnv, pFileName, NULL);
    // Open video file
    if(avformat_open_input(&pFormatCtx, videoFileName, NULL, NULL)!=0)
        return -1; // Couldn't open file
    // Retrieve stream information
    if(avformat_find_stream_info(pFormatCtx, NULL)<0)
        return -1; // Couldn't find stream information
    // Dump information about file onto standard error
    av_dump_format(pFormatCtx, 0, videoFileName, 0);
    // Find the first video stream
    videoStream=-1;
    for(i=0; i<pFormatCtx->nb_streams; i++) {
        if(pFormatCtx->streams[i]->codec->codec_type==AVMEDIA_TYPE_VIDEO) {
            videoStream=i;
            break;
        }
    }
    if(videoStream==-1)
        return -1; // Didn't find a video stream
    // Get a pointer to the codec context for the video stream
    pCodecCtx=pFormatCtx->streams[videoStream]->codec;
    // Find the decoder for the video stream
    pCodec=avcodec_find_decoder(pCodecCtx->codec_id);
    if(pCodec==NULL) {
        fprintf(stderr, "Unsupported codec!\n");
        return -1; // Codec not found
    }
    // Open codec
    if(avcodec_open2(pCodecCtx, pCodec, &optionsDict)<0)
        return -1; // Could not open codec

    //get the scaling context
    sws_ctx = sws_getContext(pCodecCtx->width,pCodecCtx->height,pCodecCtx->pix_fmt,
                             pCodecCtx->width,pCodecCtx->height,AV_PIX_FMT_RGBA,SWS_BILINEAR,
                             NULL,NULL,NULL);
}

JNIEXPORT jobject JNICALL
Java_com_medilab_avlib_AVdecode_readFrame(JNIEnv *pEnv, jclass type, jint frameNumber) {

    clock_t time_start, time_finish;
    long  time_duration = 0;

    // Read frames and save first five frames to disk

    while(av_read_frame(pFormatCtx, &packet)>=0) {
        // Is this a packet from the video stream?
        if(packet.stream_index==videoStream) {
            time_start=clock();//开始时间
            // Allocate video frame
            pFrame=av_frame_alloc();//av_frame_alloc  avcodec_alloc_frame

            // Decode video frame
            ret = avcodec_decode_video2(pCodecCtx, pFrame, &frameFinished, &packet);//解码
            time_finish= clock();//结束时间
            time_duration=(time_finish - time_start);//计算时间差
            LOGI("avcodec_decode : %ld",time_duration);//10-08 14:38:53.271 17578-17910/com.example.wangalbert.prac_2 I/ffmpeg: time_duration : 55271

            if(ret < 0){
                LOGE("Decode Error.\n");
                return NULL;
            }
            // Did we get a video frame?
            if(frameFinished) {
                if(++currentFrameNumber >= frameNumber) {
//                    LOGI("frame %d", i);
                    time_start=clock();
                    //create a bitmap as the buffer for pFrameRGBA
                    bitmap = createBitmap(pEnv, pCodecCtx->width, pCodecCtx->height);
                    if (AndroidBitmap_lockPixels(pEnv, bitmap, &buffer) < 0)
                        return NULL;
                    // Allocate an AVFrame structure
                    pFrameRGBA=av_frame_alloc();//avcodec_alloc_frame
                    if(pFrameRGBA==NULL)
                        return NULL;

                    // Assign appropriate parts of bitmap to image planes in pFrameRGBA
                    // Note that pFrameRGBA is an AVFrame, but AVFrame is a superset of AVPicture
                    avpicture_fill((AVPicture *)pFrameRGBA, buffer, AV_PIX_FMT_RGBA, pCodecCtx->width, pCodecCtx->height);
                    // Convert the image from its native format to RGBA
                    sws_scale(sws_ctx,(uint8_t const * const *)pFrame->data,pFrame->linesize,0,pCodecCtx->height,pFrameRGBA->data,pFrameRGBA->linesize);
                    time_finish= clock();
                    time_duration=(time_finish - time_start);
                    LOGI("sws_scale : %ld",time_duration);//10-08 14:38:53.271 17578-17910/com.example.wangalbert.prac_2 I/ffmpeg: sws_scale : 7782

                    AndroidBitmap_unlockPixels(pEnv, bitmap);
                    // Free the packet that was allocated by av_read_frame
                    av_free_packet(&packet);
                    // Free the RGB image
                    av_free(pFrameRGBA);
                    // Free the YUV frame
                    av_free(pFrame);

                    return bitmap;


                }
//                currentFrameNumber++;
                //得到帧且大于某次则返回
//                if(frameFinished && (currentFrameNumber>=frameNumber)){

                //unlock the bitmap

//                }
                /*
                LOGI("getBitmapFromNative() call");
                jmethodID getBitmapFromNativeMID;
                jclass mainActCls;
                mainActCls = (*pEnv)->GetObjectClass(pEnv, pObj);
                getBitmapFromNativeMID = (*pEnv)->GetMethodID(pEnv, mainActCls,"getBitmapFromNative","(Landroid/graphics/Bitmap;)V");//java方法名
                (*pEnv)->CallVoidMethod(pEnv, pObj, getBitmapFromNativeMID, bitmap);
                (*pEnv)->DeleteLocalRef(pEnv,mainActCls);
                */

            }
            // Free the packet that was allocated by av_read_frame
            av_free_packet(&packet);
            // Free the RGB image
            av_free(pFrameRGBA);
            // Free the YUV frame
            av_free(pFrame);
        }


    }

    return NULL;

}

JNIEXPORT void JNICALL
Java_com_medilab_avlib_AVdecode_destroy(JNIEnv *env, jclass type) {

    // Close the codec
    avcodec_close(pCodecCtx);
    // Close the video file
    avformat_close_input(&pFormatCtx);

//    (*env)->ReleaseStringUTFChars(env, pVideoFileName_, pVideoFileName);

}
