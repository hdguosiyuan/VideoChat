package com.example.chat.codec;

public interface CodecCallback {
    //编码完成一帧回调
    void finishFrameCodec(byte[] data);
}
