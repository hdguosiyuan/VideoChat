package com.example.chat.video;

import android.net.Uri;
import android.util.Log;

import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;

public class ServerSocketUtil extends WebSocketServer {

    private WebSocket webSocket;
    private SocketCallback socketCallback;

    public ServerSocketUtil(){

    }

    public ServerSocketUtil(InetSocketAddress inetSocketAddress){
        super(inetSocketAddress);
    }


    public void setSocketCallback(SocketCallback socketCallback){
        this.socketCallback = socketCallback;
    }

    @Override
    public void onOpen(WebSocket conn, ClientHandshake handshake) {
        webSocket = conn;
        Log.d("gsy","server onOpen");
    }

    @Override
    public void onClose(WebSocket conn, int code, String reason, boolean remote) {

    }

    @Override
    public void onMessage(WebSocket conn, String message) {

    }

    @Override
    public void onMessage(WebSocket conn, ByteBuffer message) {
        Log.d("gsy","onMessage receive from client");
        byte[] data = new byte[message.remaining()];
        message.get(data);
        socketCallback.callBack(data);
    }

    @Override
    public void onError(WebSocket conn, Exception ex) {

    }

    @Override
    public void onStart() {

    }

    public void sendData(byte[] bytes){
        if(webSocket != null && webSocket.isOpen()){
            //通过WebSocket 发送数据
            Log.d("gsy","Server sendData");
            webSocket.send(bytes);
        }
    }
}
