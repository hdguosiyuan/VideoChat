package com.example.chat.video;

import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaFormat;
import android.util.Log;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.util.Arrays;

public class EncodecPush {

    private ServerSocketUtil socketUtil;
    private MediaCodec mediaCodec;
    private MediaFormat mediaFormat;
    private int frameIndex;
    public static final int NAL_I = 19;
    public static final int NAL_VPS = 32;
    private byte[] vps_sps_pps_buf;
    private boolean isInit = false;

    private int KEY_I_FRAME_INTERVAL = 5;
    private int KEY_FRAME_RATE = 10;

    public EncodecPush() {
        socketUtil = new ServerSocketUtil(new InetSocketAddress(11007));
        socketUtil.start();
    }

    public void initCodec(int width, int height) {
        try {
            mediaCodec = MediaCodec.createEncoderByType(MediaFormat.MIMETYPE_VIDEO_HEVC);
            mediaFormat = MediaFormat.createVideoFormat(MediaFormat.MIMETYPE_VIDEO_HEVC, width, height);
            mediaFormat.setInteger(MediaFormat.KEY_BIT_RATE, width * height);
            mediaFormat.setInteger(MediaFormat.KEY_FRAME_RATE, KEY_FRAME_RATE);
            mediaFormat.setInteger(MediaFormat.KEY_COLOR_FORMAT, MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420Flexible);
            mediaFormat.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, KEY_I_FRAME_INTERVAL); //IDR帧刷新时间
            mediaCodec.configure(mediaFormat, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE);
            mediaCodec.start();
            isInit = true;
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public boolean isInit(){
        return isInit;
    }

    private long computePresentationTime(long frameIndex) {
        return 132 + frameIndex * 1000000 / 15;
    }

    public void encodec(byte[] bytes) {
        if (mediaCodec == null){
            return;
        }
        int inputBufferIndex = mediaCodec.dequeueInputBuffer(10000);
        if (inputBufferIndex >= 0) {
            ByteBuffer inputBuffer = mediaCodec.getInputBuffer(inputBufferIndex);
            inputBuffer.clear();
            inputBuffer.put(bytes);
            long presentationTime = computePresentationTime(frameIndex);
            mediaCodec.queueInputBuffer(inputBufferIndex, 0, bytes.length, presentationTime, 0);
            frameIndex++;
        }
        MediaCodec.BufferInfo bufferInfo = new MediaCodec.BufferInfo();
        int outputBufferIndex = mediaCodec.dequeueOutputBuffer(bufferInfo, 10000);
        while (outputBufferIndex >= 0) {
            ByteBuffer outputBuffer = mediaCodec.getOutputBuffer(outputBufferIndex);
            dealFrame(outputBuffer,bufferInfo);
            mediaCodec.releaseOutputBuffer(outputBufferIndex,false);
            outputBufferIndex = mediaCodec.dequeueOutputBuffer(bufferInfo,0);
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
            socketUtil.sendData(newBuf);
        } else {
            final byte[] bytes = new byte[bufferInfo.size];
            bb.get(bytes);
            socketUtil.sendData(bytes);
        }
    }
}

