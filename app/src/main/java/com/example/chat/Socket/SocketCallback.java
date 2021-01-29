package com.example.chat.Socket;

public interface SocketCallback {
    //网络回调
    void callBack(byte[] data);
    //错误回调
    void onError(Exception ex);
    void onClose(int code, String reason, boolean remote);
}
