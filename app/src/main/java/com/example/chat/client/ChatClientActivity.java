package com.example.chat.client;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.view.PreviewView;

import butterknife.BindView;
import butterknife.ButterKnife;

import android.graphics.SurfaceTexture;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.TextureView;
import android.view.View;

import com.example.chat.R;
import com.example.chat.video.CameraAnalyzer;
import com.example.chat.video.EncodecPush;
import com.example.chat.video.SocketCallback;
import com.example.chatlibrary.CameraUtil;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class ChatClientActivity extends AppCompatActivity implements SocketCallback, TextureView.SurfaceTextureListener {

//    @BindView(R.id.tv_opposite)
//    TextureView textureView;
    @BindView(R.id.pv_self)
    PreviewView pvSelf;
    @BindView(R.id.surfaceview)
    SurfaceView surfaceView;

    private DecodecVideo decodecVideo;
    private BlockingQueue<byte[]> blockingQueue;


    private CameraUtil cameraUtil;
    private CameraAnalyzer cameraAnalyzer;

    private EncodecPush encodecPush;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_client);
        ButterKnife.bind(this);
        pvSelf.bringToFront();
        init();
        startService(null);
    }

    private void init() {
        encodecPush = new EncodecPush(2);
        encodecPush.setSocketCallback(this);
        cameraAnalyzer = new CameraAnalyzer();
        cameraAnalyzer.setEncodecPush(encodecPush);
        cameraUtil = new CameraUtil(pvSelf,this);

        cameraUtil.setAnalyzer(cameraAnalyzer);
        cameraUtil.openCamera();


        blockingQueue = new LinkedBlockingQueue<>();
        //textureView.setSurfaceTextureListener(this);
        surfaceView.getHolder().addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(@NonNull SurfaceHolder holder) {
                decodecVideo = new DecodecVideo(holder.getSurface(),blockingQueue);
                decodecVideo.initDecodec();
                decodecVideo.start();
            }

            @Override
            public void surfaceChanged(@NonNull SurfaceHolder holder, int format, int width, int height) {

            }

            @Override
            public void surfaceDestroyed(@NonNull SurfaceHolder holder) {

            }
        });
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