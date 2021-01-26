package com.example.chat.video;

import android.util.Log;

import java.nio.ByteBuffer;

import androidx.annotation.NonNull;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageProxy;

public class CameraAnalyzer implements ImageAnalysis.Analyzer {

    private EncodecPush encodecPush;

    public CameraAnalyzer(){
        encodecPush = new EncodecPush();
    }

    public EncodecPush getEncodecPush(){
        return encodecPush;
    }

    @Override
    public void analyze(@NonNull ImageProxy image) {
        Log.d("gsy","CameraAnalyzer ="+Thread.currentThread().getName()+"width ="+image.getWidth()+",height ="+image.getHeight());
        ImageProxy.PlaneProxy[] planes = image.getPlanes();
        planes[0].getBuffer().limit();
        image.close();
    }
}
