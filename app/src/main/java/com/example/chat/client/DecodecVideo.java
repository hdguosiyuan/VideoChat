package com.example.chat.client;

import android.media.MediaCodec;
import android.media.MediaFormat;
import android.view.Surface;
import android.view.TextureView;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.concurrent.BlockingQueue;

public class DecodecVideo extends Thread{

    private MediaCodec mediaCodec;
    private MediaFormat mediaFormat;
    private Surface surface;
    private BlockingQueue<byte[]> blockingQueue;

    public DecodecVideo(Surface surface,BlockingQueue<byte[]> blockingQueue) {
        this.surface = surface;
        this.blockingQueue = blockingQueue;
    }

    public void initDecodec(){
        try {
            mediaCodec = MediaCodec.createDecoderByType(MediaFormat.MIMETYPE_VIDEO_HEVC);
            mediaFormat = MediaFormat.createVideoFormat(MediaFormat.MIMETYPE_VIDEO_HEVC, 1080, 1920);
            mediaFormat.setInteger(MediaFormat.KEY_BIT_RATE, 1080 * 1920);
            mediaFormat.setInteger(MediaFormat.KEY_FRAME_RATE, 10);
            mediaFormat.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, 5);
            mediaCodec.configure(mediaFormat,surface,null,0);
            mediaCodec.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        while (true){
            try {
                byte[] data = blockingQueue.take();
                int index= mediaCodec.dequeueInputBuffer(100000);
                if (index >= 0) {
                    ByteBuffer inputBuffer = mediaCodec.getInputBuffer(index);
                    inputBuffer.clear();
                    inputBuffer.put(data, 0, data.length);
                    mediaCodec.queueInputBuffer(index,
                            0, data.length, System.currentTimeMillis(), 0);
                }
                MediaCodec.BufferInfo bufferInfo = new MediaCodec.BufferInfo();
                int outputBufferIndex = mediaCodec.dequeueOutputBuffer(bufferInfo, 100000);
                while (outputBufferIndex >=0) {
                    mediaCodec.releaseOutputBuffer(outputBufferIndex, true);
                    outputBufferIndex = mediaCodec.dequeueOutputBuffer(bufferInfo, 0);
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
