package com.example.chat.codec;

import android.media.MediaCodec;
import android.media.MediaCrypto;
import android.media.MediaFormat;
import android.view.Surface;

import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * 负责硬编码的类
 */
public class EncodecVideo {
    //mediaCodec
    private MediaCodec mediaCodec;
    //编码格式
    private String videType;
    //MediaFormat
    private MediaFormat mediaFormat;
    private Surface surface;
    //加密相关
    private MediaCrypto mediaCrypto;
    private int flags;

    private CodecCallback codecCallback;

    private int frameIndex;
    private long timeOut = 10000L;

    public static final int NAL_I = 19;
    public static final int NAL_VPS = 32;
    private byte[] vps_sps_pps_buf;

    public EncodecVideo(Builder builder){
        videType = builder.videType;
        mediaFormat = builder.mediaFormat;
        surface = builder.surface;
        mediaCrypto = builder.mediaCrypto;
        flags = builder.flags;
        this.codecCallback = builder.codecCallback;
    }

    public void init(){
        try {
            mediaCodec = MediaCodec.createEncoderByType(videType);
            mediaCodec.configure(mediaFormat,surface,mediaCrypto,flags);
            mediaCodec.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void enCodec(byte[] bytes){
        int inputBufferIndex = mediaCodec.dequeueInputBuffer(timeOut);
        if (inputBufferIndex >= 0) {
            ByteBuffer inputBuffer = mediaCodec.getInputBuffer(inputBufferIndex);
            inputBuffer.clear();
            inputBuffer.put(bytes);
            long presentationTime = computePresentationTime(frameIndex);
            mediaCodec.queueInputBuffer(inputBufferIndex, 0, bytes.length, presentationTime, 0);
            frameIndex++;
        }
        MediaCodec.BufferInfo bufferInfo = new MediaCodec.BufferInfo();
        int outputBufferIndex = mediaCodec.dequeueOutputBuffer(bufferInfo, timeOut);
        while (outputBufferIndex >= 0) {
            ByteBuffer outputBuffer = mediaCodec.getOutputBuffer(outputBufferIndex);
            dealFrame(outputBuffer,bufferInfo);
            mediaCodec.releaseOutputBuffer(outputBufferIndex,false);
            outputBufferIndex = mediaCodec.dequeueOutputBuffer(bufferInfo,timeOut);
        }
    }

    private void dealFrame(ByteBuffer bb, MediaCodec.BufferInfo bufferInfo){
        int offset = 4;
        if (bb.get(2) == 0x01) {
            offset = 3;
        }
        int type = (bb.get(offset) & 0x7E) >> 1;
        if (type == NAL_VPS) {
            vps_sps_pps_buf = new byte[bufferInfo.size];
            bb.get(vps_sps_pps_buf);
        } else if (type == NAL_I) {
            final byte[] bytes = new byte[bufferInfo.size];
            bb.get(bytes);
            byte[] newBuf = new byte[vps_sps_pps_buf.length + bytes.length];
            System.arraycopy(vps_sps_pps_buf, 0, newBuf, 0, vps_sps_pps_buf.length);
            System.arraycopy(bytes, 0, newBuf, vps_sps_pps_buf.length, bytes.length);
            codecCallback.finishFrameCodec(newBuf);
        } else {
            final byte[] bytes = new byte[bufferInfo.size];
            bb.get(bytes);
            codecCallback.finishFrameCodec(bytes);
        }
    }

    private long computePresentationTime(long frameIndex) {
        return 132 + frameIndex * 1000000 / 15;
    }

    public void setMediaFormat(MediaFormat mediaFormat){
        this.mediaFormat = mediaFormat;
    }

    public void setVideType(String videType) {
        this.videType = videType;
    }

    public void setSurface(Surface surface) {
        this.surface = surface;
    }

    public void setMediaCrypto(MediaCrypto mediaCrypto) {
        this.mediaCrypto = mediaCrypto;
    }

    public void setFlags(int flags) {
        this.flags = flags;
    }

    public static class Builder{

        private String videType;
        private MediaFormat mediaFormat;
        private Surface surface;
        private MediaCrypto mediaCrypto;
        private int flags;
        private CodecCallback codecCallback;

        public Builder(){
            videType = MediaFormat.MIMETYPE_VIDEO_AVC;
            mediaFormat = new VideoMediaFormat()
                    .bulid();
            surface = null;
            mediaCrypto = null;
            flags = MediaCodec.CONFIGURE_FLAG_ENCODE;
        }

        public Builder setVideType(String videType) {
            this.videType = videType;
            return this;
        }

        public Builder setMediaFormat(MediaFormat mediaFormat) {
            this.mediaFormat = mediaFormat;
            return this;
        }

        public Builder setSurface(Surface surface) {
            this.surface = surface;
            return this;
        }

        public Builder setMediaCrypto(MediaCrypto mediaCrypto) {
            this.mediaCrypto = mediaCrypto;
            return this;
        }

        public Builder setFlags(int flags) {
            this.flags = flags;
            return this;
        }

        public Builder setCodecCallback(CodecCallback callback){
            this.codecCallback = callback;
            return this;
        }

        public EncodecVideo build(){
            return new EncodecVideo(this);
        }

    }

}
