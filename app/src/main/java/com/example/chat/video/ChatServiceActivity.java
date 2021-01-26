package com.example.chat.video;

import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.view.PreviewView;
import butterknife.BindView;
import butterknife.ButterKnife;

import android.os.Bundle;
import android.view.View;

import com.example.chat.R;
import com.example.chatlibrary.CameraUtil;

public class ChatServiceActivity extends AppCompatActivity {

    @BindView(R.id.pv_self)
    PreviewView pvSelf;

    private CameraUtil cameraUtil;
    private CameraAnalyzer cameraAnalyzer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_service);
        ButterKnife.bind(this);
        init();
    }

    private void init() {
        cameraAnalyzer = new CameraAnalyzer();
        cameraUtil = new CameraUtil(pvSelf,this);
        cameraUtil.setAnalyzer(cameraAnalyzer);
        cameraUtil.openCamera();
        //cameraAnalyzer.getEncodecPush().initCodec(cameraUtil.getWith(),cameraUtil.getHeight());
    }

    public void onClick(View view) {

    }
}