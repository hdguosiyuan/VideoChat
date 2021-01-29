package com.example.chat.codec;

import android.media.MediaCodecInfo;
import android.media.MediaFormat;

public class VideoMediaFormat {

    String mime;
    int width;
    int height;
    int bit_rate;
    int frame_rate;
    int color_format;
    int i_frame_interval;

    public VideoMediaFormat(){
        mime = MediaFormat.MIMETYPE_VIDEO_HEVC;
        width = 720;
        height = 1080;
        bit_rate = width*height;
        frame_rate = 10;
        color_format = MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420Flexible;
        i_frame_interval = 5;
    }

    public VideoMediaFormat setBit_rate(int bit_rate) {
        this.bit_rate = bit_rate;
        return this;
    }

    public VideoMediaFormat setFrame_rate(int frame_rate) {
        this.frame_rate = frame_rate;
        return this;
    }

    public VideoMediaFormat setColor_format(int color_format) {
        this.color_format = color_format;
        return this;
    }

    public VideoMediaFormat setI_frame_interval(int i_frame_interval) {
        this.i_frame_interval = i_frame_interval;
        return this;
    }

    public VideoMediaFormat setMime(String mime) {
        this.mime = mime;
        return this;
    }

    public VideoMediaFormat setWidth(int width) {
        this.width = width;
        return this;
    }

    public VideoMediaFormat setHeight(int height) {
        this.height = height;
        return this;
    }

    public MediaFormat bulid(){
        MediaFormat mediaFormat = MediaFormat.createVideoFormat(mime,width,height);
        mediaFormat.setInteger(MediaFormat.KEY_BIT_RATE,bit_rate);
        mediaFormat.setInteger(MediaFormat.KEY_FRAME_RATE,frame_rate);
        mediaFormat.setInteger(MediaFormat.KEY_COLOR_FORMAT,color_format);
        mediaFormat.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL,i_frame_interval);
        return mediaFormat;
    }

}
