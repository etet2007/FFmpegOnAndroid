#include "ffmpegDecode.h"

//加入调用Android的Log函数的功能。
#ifdef ANDROID
#include <jni.h>
#include <android/log.h>
#define LOGE(format, ...)  __android_log_print(ANDROID_LOG_ERROR, "(>_<)", format, ##__VA_ARGS__) //log.e
#define LOGI(format, ...)  __android_log_print(ANDROID_LOG_INFO,  "(=_=)", format, ##__VA_ARGS__)//log.i
#else
#define LOGE(format, ...)  printf("(>_<) " format "\n", ##__VA_ARGS__)
#define LOGI(format, ...)  printf("(^_^) " format "\n", ##__VA_ARGS__)
#endif

ffmpegDecode :: ~ffmpegDecode()
{
    pCvMat->release();
    //释放本次读取的帧内存
    av_free_packet(packet);
    avcodec_close(pCodecCtx);
    avformat_close_input(&pFormatCtx);
}

//构造函数
ffmpegDecode :: ffmpegDecode(char * file)
{
    pAvFrame = NULL/**pFrameRGB = NULL*/;
    pFormatCtx  = NULL;
    pCodecCtx   = NULL;
    pCodec      = NULL;

    pCvMat = new cv::Mat();
    i=0;
    videoindex=0;

    ret = 0;
    got_picture = 0;
    img_convert_ctx = NULL;
    y_size = 0;
    packet = NULL;

    if (NULL == file)
    {
        filepath =  "opencv.h264";
    }
    else
    {
        filepath = file;
    }
    skippedFramesNum=0; // 跳过的帧数
    init();
    openDecode();
    prepare();

    return;
}

void ffmpegDecode :: init()
{
    //ffmpeg注册复用器，编码器等的函数av_register_all()。
    //该函数在所有基于ffmpeg的应用程序中几乎都是第一个被调用的。只有调用了该函数，才能使用复用器，编码器等。
    //这里注册了所有的文件格式和编解码器的库，所以它们将被自动的使用在被打开的合适格式的文件上。注意你只需要调用 av_register_all()一次，因此我们在主函数main()中来调用它。如果你喜欢，也可以只注册特定的格式和编解码器，但是通常你没有必要这样做。
    av_register_all();

    pFormatCtx = avformat_alloc_context();
    //打开视频文件,通过参数filepath来获得文件名。这个函数读取文件的头部并且把信息保存到我们给的AVFormatContext结构体中。
    //最后2个参数用来指定特殊的文件格式，缓冲大小和格式参数，但如果把它们设置为空NULL或者0，libavformat将自动检测这些参数。
    if(avformat_open_input(&pFormatCtx,filepath,NULL,NULL)!=0)
    {
        LOGE("cannot open the files.\n");

        return;
    }

    //查找文件的流信息,avformat_open_input函数只是检测了文件的头部，接着要检查在文件中的流的信息 av_find_stream_info --> avformat_find_stream_info
    if(avformat_find_stream_info(pFormatCtx,NULL)<0)
    {
        printf("Couldn't find stream information.\n");
        return;
    }
    return;
}

void ffmpegDecode :: openDecode()
{
    //遍历文件的各个流，找到第一个视频流，并记录该流的编码信息
    videoindex = -1;
    for(i=0; i<pFormatCtx->nb_streams; i++)
    {
        if(pFormatCtx->streams[i]->codec->codec_type==AVMEDIA_TYPE_VIDEO)
        {
            videoindex=i;
            break;
        }
    }
    if(videoindex==-1)
    {
        printf("Didn't find a video stream.\n");
        return;
    }
    pCodecCtx=pFormatCtx->streams[videoindex]->codec;


    //在库里面查找支持该格式的解码器
    pCodec=avcodec_find_decoder(pCodecCtx->codec_id);
    if(pCodec==NULL)
    {
        printf("Codec not found.\n");
        return;
    }

    //打开解码器
    if(avcodec_open2(pCodecCtx, pCodec,NULL) < 0)
    {
        printf("Could not open codec.\n");
        return;
    }
}

void ffmpegDecode :: prepare()
{
    //分配一个帧指针，指向解码后的原始帧
    pAvFrame=av_frame_alloc(); // avcodec_alloc_frame -->  av_frame_alloc()
    y_size = pCodecCtx->width * pCodecCtx->height;

    //分配帧内存
    packet=(AVPacket *)av_malloc(sizeof(AVPacket));
    av_new_packet(packet, y_size);

    m_video_avg_frame_rate=pFormatCtx->streams[videoindex]->avg_frame_rate; //赋值给平均帧率 !!!


    //输出一下信息-----------------------------
    printf("file information-----------------------------------------\n");
    //av_dump_format只是个调试函数，输出文件的音、视频流的基本信息了，帧率、分辨率、音频采样等等。
    av_dump_format(pFormatCtx,0,filepath,0);

    printf("avg_frame_rate.num-----> %d \n",pFormatCtx->streams[videoindex]->avg_frame_rate.num);  //avg_frame_rate：Average framerate  类型：AVRational 有理数 numerator/denominator
    printf("streams[videoindex] time_base  %d/ %d \n",pFormatCtx->streams[videoindex]->time_base.num,pFormatCtx->streams[videoindex]->time_base.den);
    printf(" time_base  %d/ %d \n",pCodecCtx->time_base.num,pCodecCtx->time_base.den);

    printf("-------------------------------------------------\n");
}


int ffmpegDecode :: readOneFrame()
{
    int result = 0;

    //av_read_frame()读取一个包并且把它保存到AVPacket结构体中。这些数据可以在后面通过av_free_packet()来释放。
    result = av_read_frame(pFormatCtx, packet);
    printf("result:%d \n",result);
    return result;
}

AVRational ffmpegDecode::getAvg_frame_rate() const
{
    return m_video_avg_frame_rate;
}
int ffmpegDecode::getSkippedFramesNum() const
{
    return skippedFramesNum;
}

cv::Mat ffmpegDecode :: getDecodedFrame(){
    bool isVideo=false;

    //是视频的Packet
    if(packet->stream_index==videoindex){
        isVideo=true;

        printf("dts %lld   pts %lld \n",packet->dts,packet->pts); //每一个packet中都有

        //函数avcodec_decode_video()把包转换为帧。
        //然而当解码一个包的时候，我们可能没有得到我们需要的关于帧的信息。
       ret = avcodec_decode_video2(pCodecCtx, pAvFrame, &got_picture, packet);
        if(ret < 0){
            LOGE("decode error.\n");
            return cv::Mat();
        }

        if(got_picture){
            //根据编码信息设置渲染格式
            if(img_convert_ctx == NULL){
                img_convert_ctx = sws_getContext(pCodecCtx->width, pCodecCtx->height,
                    pCodecCtx->pix_fmt, pCodecCtx->width, pCodecCtx->height,
                    AV_PIX_FMT_BGR24, SWS_BICUBIC, NULL, NULL, NULL);
            }
            //----------------------opencv
            if (pCvMat->empty()){
                pCvMat->create(cv::Size(pCodecCtx->width, pCodecCtx->height),CV_8UC3);
                LOGI("pCodecCtx-> %d,%d.\n",pCodecCtx->width,pCodecCtx->height);//测试宽高
            }
            if(img_convert_ctx != NULL){
                get(pCodecCtx, img_convert_ctx, pAvFrame);
            }
        }else{
            skippedFramesNum++;
        }
    }
    av_free_packet(packet);
    if(!isVideo){
        return emptyMat;
    }
    return *pCvMat;
}

cv::Mat ffmpegDecode :: getLastFrame()
{
    ret = avcodec_decode_video2(pCodecCtx, pAvFrame, &got_picture, packet);

    if(got_picture){
        //根据编码信息设置渲染格式  PIX_FMT_RGB24 --> AV_PIX_FMT_RGB24
        img_convert_ctx = sws_getContext(pCodecCtx->width, pCodecCtx->height, pCodecCtx->pix_fmt, pCodecCtx->width, pCodecCtx->height,  AV_PIX_FMT_BGR24, SWS_BICUBIC, NULL, NULL, NULL);

        if(img_convert_ctx != NULL){
            get(pCodecCtx, img_convert_ctx,pAvFrame);
        }
    }
    return *pCvMat;
}


void ffmpegDecode :: get(AVCodecContext * pCodecCtx, SwsContext * img_convert_ctx, AVFrame * pFrame)
{
    if (pCvMat->empty())
    {
        pCvMat->create(cv::Size(pCodecCtx->width, pCodecCtx->height),CV_8UC3);
    }

    AVFrame    *pFrameRGB = NULL;
    uint8_t  *out_bufferRGB = NULL;
    pFrameRGB = av_frame_alloc(); // avcodec_alloc_frame -->  av_frame_alloc()

    //给pFrameRGB帧加上分配的内存;
    int size = avpicture_get_size(AV_PIX_FMT_BGR24, pCodecCtx->width, pCodecCtx->height);
    out_bufferRGB = new uint8_t[size];
    avpicture_fill((AVPicture *)pFrameRGB, out_bufferRGB, AV_PIX_FMT_BGR24, pCodecCtx->width, pCodecCtx->height);

    //YUV to RGB
    sws_scale(img_convert_ctx, pFrame->data, pFrame->linesize, 0, pCodecCtx->height, pFrameRGB->data, pFrameRGB->linesize);

    memcpy(pCvMat->data,out_bufferRGB,size);//copy 数据到pCvMat

    delete[] out_bufferRGB;
    av_free(pFrameRGB);
}
