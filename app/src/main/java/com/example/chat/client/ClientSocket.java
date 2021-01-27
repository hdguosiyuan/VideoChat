package com.example.chat.client;

import android.util.Log;

import com.example.chat.video.SocketCallback;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import java.net.URI;
import java.nio.ByteBuffer;

public class ClientSocket extends WebSocketClient {

    private SocketCallback socketCallback;

    public ClientSocket(URI serverUri) {
        super(serverUri);
    }

    public void setSocketCallback(SocketCallback socketCallback) {
        this.socketCallback = socketCallback;
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
        Log.d("gsy","client onMessage");
        byte[] data = new byte[bytes.remaining()];
        bytes.get(data);
        socketCallback.callBack(data);
    }

    @Override
    public void onClose(int code, String reason, boolean remote) {

    }

    @Override
    public void onError(Exception ex) {

    }

}
