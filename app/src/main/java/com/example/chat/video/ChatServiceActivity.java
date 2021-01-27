package com.example.chat.video;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.view.PreviewView;
import butterknife.BindView;
import butterknife.ButterKnife;

import android.graphics.SurfaceTexture;
import android.os.Bundle;
import android.util.Log;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;

import com.example.chat.R;
import com.example.chat.client.DecodecVideo;
import com.example.chatlibrary.CameraUtil;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class ChatServiceActivity extends AppCompatActivity implements SocketCallback, TextureView.SurfaceTextureListener{

    @BindView(R.id.pv_self)
    PreviewView pvSelf;
    @BindView(R.id.tv_opposite)
    TextureView textureView;
    private CameraUtil cameraUtil;
    private CameraAnalyzer cameraAnalyzer;
    private EncodecPush encodecPush;

    private DecodecVideo decodecVideo;
    private BlockingQueue<byte[]> blockingQueue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_service);
        ButterKnife.bind(this);
        init();
    }

    private void init() {
        cameraAnalyzer = new CameraAnalyzer();
        encodecPush = new EncodecPush(1);
        encodecPush.setSocketCallback(this);
        cameraAnalyzer.setEncodecPush(encodecPush);
        cameraUtil = new CameraUtil(pvSelf,this);
        cameraUtil.setAnalyzer(cameraAnalyzer);
        cameraUtil.openCamera();
        //cameraAnalyzer.getEncodecPush().initCodec(cameraUtil.getWith(),cameraUtil.getHeight());

        blockingQueue = new LinkedBlockingQueue<>();
        textureView.setSurfaceTextureListener(this);
    }

    public void onClick(View view) {

    }

    @Override
    public void onSurfaceTextureAvailable(@NonNull SurfaceTexture surfaceTexture, int width, int height) {
        decodecVideo = new DecodecVideo(new Surface(surfaceTexture),blockingQueue);
        decodecVideo.initDecodec();
        decodecVideo.start();
    }

    @Override
    public void onSurfaceTextureSizeChanged(@NonNull SurfaceTexture surface, int width, int height) {

    }

    @Override
    public boolean onSurfaceTextureDestroyed(@NonNull SurfaceTexture surface) {
        return false;
    }

    @Override
    public void onSurfaceTextureUpdated(@NonNull SurfaceTexture surface) {

    }

    @Override
    public void callBack(byte[] data) {
        Log.d("gsy","client callback");
        blockingQueue.offer(data);
    }
}