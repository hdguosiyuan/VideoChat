package com.example.chat.client;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import butterknife.BindView;
import butterknife.ButterKnife;

import android.graphics.SurfaceTexture;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;

import com.example.chat.R;
import com.example.chat.video.SocketCallback;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class ChatClientActivity extends AppCompatActivity implements SocketCallback, TextureView.SurfaceTextureListener {

    @BindView(R.id.tv_opposite)
    TextureView textureView;
    private DecodecVideo decodecVideo;
    private BlockingQueue<byte[]> blockingQueue;
    private ClientSocket clientSocket;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_client);
        ButterKnife.bind(this);
        init();
    }

    private void init() {
        try {
            URI uri = new URI("ws://192.168.1.101:11006");
            clientSocket = new ClientSocket(uri);
            clientSocket.setSocketCallback(this);
            clientSocket.connect();
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }

        blockingQueue = new LinkedBlockingQueue<>();
        textureView.setSurfaceTextureListener(this);
    }


    public void onClick(View view) {

    }

    @Override
    public void callBack(byte[] data) {
        Log.d("gsy","client callback");
        blockingQueue.offer(data);
    }

    @Override
    public void onSurfaceTextureAvailable(@NonNull SurfaceTexture surfaceTexture, int i, int i1) {
        decodecVideo = new DecodecVideo(new Surface(surfaceTexture),blockingQueue);
        decodecVideo.initDecodec();
        decodecVideo.start();
    }

    @Override
    public void onSurfaceTextureSizeChanged(@NonNull SurfaceTexture surfaceTexture, int i, int i1) {

    }

    @Override
    public boolean onSurfaceTextureDestroyed(@NonNull SurfaceTexture surfaceTexture) {
        return false;
    }

    @Override
    public void onSurfaceTextureUpdated(@NonNull SurfaceTexture surfaceTexture) {

    }
}