package com.example.chat.video;

import android.graphics.ImageFormat;
import android.util.Log;
import android.util.Size;

import java.nio.ByteBuffer;
import java.util.concurrent.locks.ReentrantLock;

import androidx.annotation.NonNull;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageProxy;

import com.example.chat.util.ImageUtil;

public class CameraAnalyzer implements ImageAnalysis.Analyzer {

    private ReentrantLock lock = new ReentrantLock();
    private byte[] arrayY;
    private byte[] arrayU;
    private byte[] arrayV;
    private Size size;
    private int realWidth;
    private int realHeight;
    private int factor = 3 / 2;

    private byte[] nv21;
    private byte[] nv21_rotated;

    private CameraCallback cameraCallback;
    private boolean isReady = false;

    public CameraAnalyzer() {

    }

    public void setCameraCallback(CameraCallback cameraCallback){
        this.cameraCallback = cameraCallback;
    }

    @Override
    public void analyze(@NonNull ImageProxy image) {
        lock.lock();
        try {
            ImageProxy.PlaneProxy[] planes = image.getPlanes();
            ByteBuffer bufferY = planes[0].getBuffer();
            ByteBuffer bufferU = planes[1].getBuffer();
            ByteBuffer bufferV = planes[2].getBuffer();
            if (arrayY == null) {
                arrayY = new byte[bufferY.limit() - bufferY.position()];
                arrayU = new byte[bufferU.limit() - bufferU.position()];
                arrayV = new byte[bufferV.limit() - bufferV.position()];
                size = new Size(image.getWidth(), image.getHeight());
                realWidth = image.getHeight();
                realHeight = image.getWidth();
            }

            if (bufferY.remaining() == arrayY.length) {
                bufferY.get(arrayY);
                bufferU.get(arrayU);
                bufferV.get(arrayV);
                if (nv21 == null) {
                    nv21 = new byte[realHeight * realWidth * 3 / 2];
                }
                if (nv21_rotated == null) {
                    nv21_rotated = new byte[realHeight * realWidth * 3 / 2];
                }
                if (!isReady && cameraCallback != null) {
                    cameraCallback.cameraReady(size);
                    isReady = true;
                }

                ImageUtil.yuvToNv21(arrayY, arrayU, arrayV, nv21, realWidth, realHeight);
                nv21_rotated = ImageUtil.rotateYUV420Degree90(nv21, image.getWidth(), image.getHeight(), 90);
                byte[] temp = ImageUtil.nv21ToNv12(nv21_rotated);

                if (cameraCallback != null) {
                    cameraCallback.getCameraFrame(temp);
                }

            }
        } finally {
            lock.unlock();
        }
        image.close();
    }

    /**
     * getCameraFrame  拍出一帧回调
     * cameraReady camera打开回调
     */
    public interface CameraCallback {
        void getCameraFrame(byte[] data);
        void cameraReady(Size size);
    }
}
