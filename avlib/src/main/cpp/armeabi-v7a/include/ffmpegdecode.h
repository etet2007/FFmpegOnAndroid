#ifndef __FFMPEG_DECODE_H__
#define __FFMPEG_DECODE_H__

#include <jni.h>


extern "C"
{
#include "libavcodec/avcodec.h"
#include "libavformat/avformat.h"
//图像转换结构需要引入的头文件
#include "libswscale/swscale.h"

#include "libavutil/rational.h"
#include "libavutil/frame.h"
};

class ffmpegDecode
{
public:
    ffmpegDecode(char * file = NULL);
    ~ffmpegDecode();

//     getDecodedFrame();
//     getLastFrame();
//    int readOneFrame();
    jobject readFrame(JNIEnv *pEnv);

    int getAvg_frame_rate() const;
    int getSkippedFramesNum() const;
    void seekFrameBySec(int secs);
//帧原始宽高
    int width;
    int height;

    bool isOpened= true;
private:
    AVFormatContext    *pFormatCtx;
    AVCodecContext  *pCodecCtx;
    AVCodec            *pCodec;
    AVFrame    *pAvFrame;
    AVFrame         *pFrameRGBA ;
    AVPacket *packet;

    int i;
    int videoStream;
    int currentFrameNumber=0;
    SwsContext *sws_ctx;

    char *filepath;  //文件路径
    int ret, got_picture;
    int y_size;

    jobject	  bitmap;
    uint8_t*  buffer;


    AVRational m_video_avg_frame_rate; //平均帧率
    int skippedFramesNum; //跳过的帧数

    void init();
    void openDecode();
    void prepare();
//    void get(AVCodecContext *pCodecCtx, SwsContext *img_convert_ctx,AVFrame    *pFrame);
};

#endif
