package com.example.chat.video;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.view.PreviewView;
import androidx.lifecycle.MutableLiveData;

import butterknife.BindView;
import butterknife.ButterKnife;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;

import com.example.chat.R;
import com.example.chat.codec.CallManager;
import com.example.chat.codec.SocketType;
import com.example.chat.util.LiveDataBus;

import java.net.URI;
import java.net.URISyntaxException;


public class ChatClientActivity extends AppCompatActivity {

    @BindView(R.id.pv_self)
    PreviewView pvSelf;
    @BindView(R.id.surfaceview)
    SurfaceView surfaceView;

    private CallManager callManager;
    private LiveDataBus liveDataBus;
    private MutableLiveData<String> ip;
    private MutableLiveData<String> port;
    private URI uri;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_client);
        ButterKnife.bind(this);
        init();
    }

    private void init() {
        liveDataBus = LiveDataBus.getInstance();
        ip = liveDataBus.with("ip", String.class);
        port = liveDataBus.with("port", String.class);
        String ipAddress = "ws://192.168.1."+ip.getValue()+":"+port.getValue();
        try {
            uri = new URI(ipAddress);
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
        callManager = new CallManager(SocketType.Client,this);
        callManager.setUri(uri);
        callManager.initCall(pvSelf);
        surfaceView.getHolder().addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(@NonNull SurfaceHolder holder) {
                callManager.initDecodec(holder.getSurface());
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
        switch (view.getId()){
            case R.id.hangup:
                callManager.close();
                finish();
                break;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        callManager.close();
    }

}