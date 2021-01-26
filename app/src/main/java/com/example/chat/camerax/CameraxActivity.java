package com.example.chat.camerax;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureException;
import androidx.camera.view.PreviewView;
import butterknife.BindView;
import butterknife.ButterKnife;

import android.app.Activity;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.example.chat.R;
import com.example.chatlibrary.CameraUtil;

import java.io.File;

public class CameraxActivity extends AppCompatActivity implements ImageCapture.OnImageSavedCallback{

    @BindView(R.id.view_finder)
    PreviewView viewFinder;
    private CameraUtil cameraUtil;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camerax);
        ButterKnife.bind(this);
        init();
    }

    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.take_photo:
                takePhoto();
                break;
            case R.id.change_camera:
                cameraUtil.switchCamera();
                break;
        }
    }

    private void init() {
        cameraUtil = new CameraUtil(viewFinder, this);
        cameraUtil.setImageSavedCallback(this);
        cameraUtil.openCamera();
    }

    private void takePhoto() {
        File rootFile = Environment.getExternalStorageDirectory();
        String path = rootFile + "/DCIM";
        cameraUtil.takePhoto(path);
    }

    @Override
    public void onImageSaved(@NonNull ImageCapture.OutputFileResults outputFileResults) {
        this.runOnUiThread(()->{
            Toast.makeText(this, "take picture success", Toast.LENGTH_SHORT).show();
        });
    }

    @Override
    public void onError(@NonNull ImageCaptureException exception) {
        Log.d("gsy","onError ="+exception);
        this.runOnUiThread(()-> {
            Toast.makeText(this, "onError", Toast.LENGTH_SHORT).show();
        });
    }
}