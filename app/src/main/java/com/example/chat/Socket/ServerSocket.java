package com.example.chat.Socket;

import android.util.Log;

import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;

public class ServerSocket extends WebSocketServer implements LiveSocket{

    private WebSocket webSocket;
    private SocketCallback socketCallback;

    public ServerSocket(InetSocketAddress inetSocketAddress){
        super(inetSocketAddress);
    }

    @Override
    public void onOpen(WebSocket conn, ClientHandshake handshake) {
        webSocket = conn;
        Log.d("gsy","server onOpen");
    }

    @Override
    public void onClose(WebSocket conn, int code, String reason, boolean remote) {
        socketCallback.onClose(code,reason,remote);
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

    @Override
    public void startCall() {
        this.start();
    }

    @Override
    public void sentData(byte[] data) {
        this.sendData(data);
    }

    @Override
    public void setCallBack(SocketCallback socketCallback) {
        this.socketCallback = socketCallback;
    }

    @Override
    public void reTry() {
        start();
    }

    @Override
    public void stopCall() {
        try {
            stop();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
