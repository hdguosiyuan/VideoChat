package com.example.chat.codec;

import android.app.Activity;
import android.content.Context;
import android.media.MediaCodec;
import android.media.MediaFormat;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.util.Size;
import android.view.Surface;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.camera.view.PreviewView;

import com.example.chat.Socket.ClientSocket;
import com.example.chat.Socket.LiveSocket;
import com.example.chat.Socket.ServerSocket;
import com.example.chat.Socket.SocketCallback;
import com.example.chat.video.CameraAnalyzer;
import com.example.chatlibrary.CameraUtil;

import java.net.InetSocketAddress;
import java.net.URI;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class CallManager implements CodecCallback, CameraAnalyzer.CameraCallback, SocketCallback {

    private MediaFormat encodecFormat;
    private EncodecVideo encodecVideo;
    private CameraUtil cameraUtil;
    private CameraAnalyzer cameraAnalyzer;

    //工厂模式创建socket客户端和服务端
    private LiveSocket liveSocket;
    private SocketType codecType;
    //观察者模式和解码器通信
    private BlockingQueue<byte[]> blockingQueue;
    //解码相关类
    private DecodecVideo decodecVideo;
    //服务端的uri
    private URI uri;
    private Context context;
    //服务端端口号
    private int port;

    private final int msg_retry = 100;
    private final int msg_error = 101;
    //重连时间间隔
    long retryTime = 5000;

    Handler handler = new Handler(Looper.getMainLooper()){
        @Override
        public void handleMessage(@NonNull Message msg) {
            switch (msg.what){
                case msg_retry:
                    Toast.makeText(context,"重连中",Toast.LENGTH_SHORT).show();
                    liveSocket.reTry();
                    break;
                case  msg_error:
                    Toast.makeText(context,"连接错误",Toast.LENGTH_SHORT).show();
                    Message obtain = Message.obtain(handler, msg_retry);
                    handler.sendMessageDelayed(obtain,retryTime);
                    break;
                default:
                    break;
            }
        }
    };

    public CallManager(SocketType codecType,Context context) {
        this.context = context;
        this.codecType = codecType;
    }

    public void initCall(PreviewView previewView) {
        initCamera(previewView, context);
        initSocket();
    }

    public void setPort(int port) {
        this.port = port;
    }

    /**
     * 打开socket客户端或者服务端
     */
    private void initSocket() {
        if (codecType == SocketType.Server) {
            liveSocket = new ServerSocket(new InetSocketAddress(port));
            liveSocket.startCall();
        } else {
            liveSocket = new ClientSocket(uri);
            liveSocket.startCall();
        }
        liveSocket.setCallBack(this);
    }

    public void setUri(URI uri) {
        this.uri = uri;
    }

    /**
     * 初始化解码器
     *
     * @param surface
     */
    public void initDecodec(Surface surface) {
        this.blockingQueue = new LinkedBlockingQueue<>();
        decodecVideo = new DecodecVideo(surface, blockingQueue);
        decodecVideo.initDecodec();
        decodecVideo.start();
    }

    /**
     * 打开camera
     * @param previewView
     * @param context
     */
    private void initCamera(PreviewView previewView, Context context) {
        cameraAnalyzer = new CameraAnalyzer();
        cameraAnalyzer.setCameraCallback(this);
        cameraUtil = new CameraUtil(previewView, context);
        cameraUtil.setAnalyzer(cameraAnalyzer);
        cameraUtil.openCamera();
    }


    /**
     * 初始化编码器
     *
     * @param size
     */
    private void initEncodeVide(Size size) {
        encodecFormat = new VideoMediaFormat()
                .setMime(MediaFormat.MIMETYPE_VIDEO_HEVC)
                .setWidth(size.getWidth())
                .setHeight(size.getHeight())
                .bulid();
        encodecVideo = new EncodecVideo.Builder()
                .setMediaFormat(encodecFormat)
                .setVideType(MediaFormat.MIMETYPE_VIDEO_HEVC)
                .setFlags(MediaCodec.CONFIGURE_FLAG_ENCODE)
                .setCodecCallback(this)
                .build();
        encodecVideo.init();
    }


    /**
     * 编码完成的一帧数据
     *
     * @param data
     */
    @Override
    public void finishFrameCodec(byte[] data) {
        //交给socket发送
        liveSocket.sentData(data);
    }

    /**
     * Camera一帧的数据
     *
     * @param data
     */
    @Override
    public void getCameraFrame(byte[] data) {
        //交给编码器编码
        encodecVideo.enCodec(data);
    }

    /**
     * camera已经打开
     *
     * @param size
     */
    @Override
    public void cameraReady(Size size) {
        initEncodeVide(size);
    }

    /**
     * Server端收到客户端的视频码流
     * 生产者 产生数据 交给消费者(解码器)
     *
     * @param data
     */
    @Override
    public void callBack(byte[] data) {
        blockingQueue.offer(data);
    }

    /**
     * 连接错误
     * @param ex
     */
    @Override
    public void onError(Exception ex) {
        Message error = Message.obtain(handler, msg_error);
        handler.sendEmptyMessage(error.what);
    }

    /**
     * 对方挂断
     * @param code
     * @param reason
     * @param remote
     */
    @Override
    public void onClose(int code, String reason, boolean remote) {
        //cameraAnalyzer;
        //cameraUtil
        cameraUtil.stop();
        liveSocket.stopCall();
        decodecVideo.stop();
        ((Activity)context).finish();
    }

    public void close(){
        cameraUtil.stop();
        liveSocket.stopCall();
        decodecVideo.close();
        ((Activity)context).finish();
    }

}
