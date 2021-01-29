package com.example.chat.Socket;

public interface LiveSocket {

    void startCall();
    void sentData(byte[] data);
    void setCallBack(SocketCallback socketCallback);
    void reTry();
    void stopCall();
}
