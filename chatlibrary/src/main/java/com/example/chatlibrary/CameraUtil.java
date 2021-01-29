package com.example.chatlibrary;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Environment;
import android.util.DisplayMetrics;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.AspectRatio;
import androidx.camera.core.Camera;
import androidx.camera.core.CameraInfoUnavailableException;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureException;
import androidx.camera.core.ImageProxy;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.content.ContextCompat;

import com.google.common.util.concurrent.ListenableFuture;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Locale;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class CameraUtil {

    private PreviewView viewFinder;
    private Context context;
    Preview preview;
    private int lensFacing;
    private int displayId;
    private Camera camera;
    private ProcessCameraProvider cameraProvider;
    private ExecutorService cameraExecutor;

    private final String FILENAME = "yyyy-MM-dd-HH-mm-ss";
    private final String PHOTO_EXTENSION = ".jpg";
    private final String PHOTO_PREFIX = "/IMG-";

    private double RATIO_4_3_VALUE = 4.0 / 3.0;
    private double RATIO_16_9_VALUE = 16.0 / 9.0;
    private final Long ANIMATION_SLOW_MILLIS = 100L;
    private final Long ANIMATION_FAST_MILLIS = 50L;

    private ImageCapture imageCapture;
    private ViewGroup parent;

    private ImageAnalysis imageAnalysis;
    private ImageAnalysis.Analyzer analyzer;

    private ImageCapture.OnImageSavedCallback imageSavedCallback;

    private int with,height;

    public CameraUtil(PreviewView viewFinder, Context context) {
        this.viewFinder = viewFinder;
        this.context = context;
        cameraExecutor = Executors.newSingleThreadExecutor();
    }

    /**
     * 打开camera
     */
    public void openCamera() {
        viewFinder.post(() -> {
            displayId = viewFinder.getDisplay().getDisplayId();
            updateTransform();
            //设置camera和use cases
            setCamera();
        });
    }

    public void setImageSavedCallback(ImageCapture.OnImageSavedCallback imageSavedCallback) {
        this.imageSavedCallback = imageSavedCallback;
    }

    /**
     * 初始化CameraX, 准备camera use cases
     */
    private void setCamera() {
        //拿到当前进程关联的ProcessCameraProvider
        ListenableFuture<ProcessCameraProvider> instance = ProcessCameraProvider.getInstance(context);
        instance.addListener(() -> {
            try {
                // CameraProvider
                cameraProvider = instance.get();
                lensFacing = CameraSelector.LENS_FACING_BACK;
                bindCameraUseCases();
            } catch (ExecutionException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }, ContextCompat.getMainExecutor(context));
    }

    public int getWith(){
        return with;
    }

    public int getHeight(){
        return height;
    }

    /**
     * 声明并且绑定preview,capture,analysis
     */
    private void bindCameraUseCases() {
        //获取屏幕分辨率用于设置设置camera
        DisplayMetrics displayMetrics = new DisplayMetrics();
        viewFinder.getDisplay().getRealMetrics(displayMetrics);
        int screenAspectRatio = aspectRatio(displayMetrics.widthPixels, displayMetrics.heightPixels);
        with = displayMetrics.widthPixels;
        height = displayMetrics.heightPixels;
        int rotation = viewFinder.getDisplay().getRotation();
        CameraSelector cameraSelector = new CameraSelector.Builder()
                //设置打开前摄还是后摄
                .requireLensFacing(lensFacing)
                .build();
        //预览
        preview = new Preview.Builder()
                //设置预览图像的宽高比
                .setTargetAspectRatio(screenAspectRatio)
                //设置方向
                .setTargetRotation(rotation)
                .build();
        //拍照
        imageCapture = new ImageCapture.Builder()
                .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                .setTargetAspectRatio(screenAspectRatio)
                .setTargetRotation(rotation)
                .build();

        imageAnalysis = new ImageAnalysis.Builder()
                .setTargetAspectRatio(screenAspectRatio)
                .setTargetRotation(rotation)
                .build();
        if (analyzer == null){
            analyzer = new DefaultAnalyzer();
        }
        imageAnalysis.setAnalyzer(cameraExecutor,analyzer);

        //重新bind前必须先unbind
        cameraProvider.unbindAll();
        //只要可用的use-case都可以添加进来
        camera = cameraProvider.bindToLifecycle((AppCompatActivity) context, cameraSelector, preview, imageCapture,imageAnalysis);
        //将viewFinder的surface provider和preview绑定
        preview.setSurfaceProvider(viewFinder.createSurfaceProvider());
    }

    public void setAnalyzer(ImageAnalysis.Analyzer analyzer){
        this.analyzer = analyzer;
    }

    public void updateTransform() {
        parent = (ViewGroup) viewFinder.getParent();
        //移除之前的viewFinder,重新添加
        parent.removeView(viewFinder);
        parent.addView(viewFinder, 0);
    }

    /**
     * 计算传入的宽高，得到一个最合适的预览尺寸比率
     *
     * @param width
     * @param height
     * @return
     */
    private int aspectRatio(int width, int height) {
        double previewRatio = (double) Math.max(width, height) / Math.min(width, height);
        if (Math.abs(previewRatio - RATIO_4_3_VALUE) <= Math.abs(previewRatio - RATIO_16_9_VALUE)) {
            return AspectRatio.RATIO_4_3;
        }
        return AspectRatio.RATIO_16_9;
    }

    public void takePhoto(String path) {
        File outFile = createFile(path);
        ImageCapture.Metadata metadata = new ImageCapture.Metadata();
        //如果是前摄需要设置左右翻转
        metadata.setReversedHorizontal(lensFacing == CameraSelector.LENS_FACING_FRONT);
        //创建OutputFileOptions对象 包含文件和metadata
        ImageCapture.OutputFileOptions outputOptuins = new ImageCapture.OutputFileOptions.Builder(outFile)
                .setMetadata(metadata)
                .build();
        //调用拍照，此时OnImageSavedCallback是没有运行在主线程的，所以如果要更改ui记得切换线程 imageSavedCallback
        imageCapture.takePicture(outputOptuins, cameraExecutor, imageSavedCallback);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            playAnimation();
        }
    }

    /**
     * 拍照时屏幕闪一下
     */
    private void playAnimation() {
        parent.postDelayed(() -> {
            parent.setForeground(new ColorDrawable(Color.WHITE));
            //记得要设置回来
            parent.postDelayed(() -> {
                parent.setForeground(null);
            }, ANIMATION_FAST_MILLIS);
        }, ANIMATION_SLOW_MILLIS);
    }

    /**
     * 创建jpg文件
     *
     * @return
     */
    private File createFile(String path) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(FILENAME, Locale.CHINA);
        //path+时间.jpg
        File outFile = new File(path + PHOTO_PREFIX + simpleDateFormat.format(System.currentTimeMillis()) + PHOTO_EXTENSION);
        return outFile;
    }

    /**
     * 判断是否有前摄
     *
     * @return
     */
    public boolean hasFrontCamera() {
        try {
            return cameraProvider.hasCamera(CameraSelector.DEFAULT_FRONT_CAMERA);
        } catch (CameraInfoUnavailableException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * 判断是否有后摄
     *
     * @return
     */
    public boolean hasBackCamera() {
        try {
            return cameraProvider.hasCamera(CameraSelector.DEFAULT_BACK_CAMERA);
        } catch (CameraInfoUnavailableException e) {
            e.printStackTrace();
        }
        return false;
    }

    public void switchCamera(){
        if(lensFacing == CameraSelector.LENS_FACING_FRONT){
            lensFacing = CameraSelector.LENS_FACING_BACK;
        }else{
            lensFacing = CameraSelector.LENS_FACING_FRONT;
        }
        bindCameraUseCases();
    }

    /**
     * 关闭camera
     */
    public void stop(){
        cameraProvider.unbindAll();
        cameraExecutor.shutdown();
    }

}
