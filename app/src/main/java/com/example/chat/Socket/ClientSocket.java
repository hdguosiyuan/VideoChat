package com.example.chat.Socket;

import android.util.Log;
import android.widget.Toast;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import java.net.URI;
import java.nio.ByteBuffer;

public class ClientSocket extends WebSocketClient implements LiveSocket{

    private SocketCallback socketCallback;
    private int code = -1;
    public ClientSocket(URI serverUri) {
        super(serverUri);
    }

    @Override
    public void onOpen(ServerHandshake handshakedata) {
        Log.d("wlw","clicent onOpen connect");
    }

    @Override
    public void onMessage(String message) {

    }

    @Override
    public void onMessage(ByteBuffer bytes) {
        super.onMessage(bytes);
        byte[] data = new byte[bytes.remaining()];
        bytes.get(data);
        socketCallback.callBack(data);
    }

    @Override
    public void onClose(int code, String reason, boolean remote) {
        this.code = code;
        socketCallback.onClose(code,reason,remote);
    }

    @Override
    public void onError(Exception ex) {
        if (code == 1000){
            return;
        }
        socketCallback.onError(ex);
    }

    @Override
    public void startCall() {
        this.connect();
    }

    @Override
    public void sentData(byte[] data) {
        if (isOpen()){
            this.send(data);
        }
    }

    @Override
    public void setCallBack(SocketCallback socketCallback) {
        this.socketCallback = socketCallback;
    }

    @Override
    public void reTry() {
        //reconnect();
    }

    @Override
    public void stopCall() {
        close();
    }

}
