package com.example.chat;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.MutableLiveData;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

import com.example.chat.camerax.CameraxActivity;
import com.example.chat.util.LiveDataBus;
import com.example.chat.video.ChatClientActivity;
import com.example.chat.video.ChatServiceActivity;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MainActivity extends AppCompatActivity {

    private String[] permissions = {Manifest.permission.CAMERA, Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE};
    private int permissionRequestCode = 100;
    @BindView(R.id.et_ip)
    EditText etIp;
    @BindView(R.id.et_port)
    EditText etPort;
    @BindView(R.id.et_server_port)
    EditText serverProt;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        checkPermissions();
    }

    private void checkPermissions() {
        if (!hasPermission(permissions)) {
            requestPermissions(permissions, permissionRequestCode);
        }
    }

    private boolean hasPermission(String[] permissions) {
        for (String permission : permissions) {
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

    public void onClick(View view) {
        Intent intent;
        switch (view.getId()) {
            case R.id.camera:
                intent = new Intent(this, CameraxActivity.class);
                startActivity(intent);
                break;
            case R.id.call_server:
                startServer();
                break;
            case R.id.call_client:
                startClient();
                break;
        }
    }

    private void startServer(){
        String port = serverProt.getText().toString();
        port = port.length() == 0 ? etPort.getHint().toString() : port;
        int portNum = Integer.valueOf(port);
        LiveDataBus liveDataBus = LiveDataBus.getInstance();
        MutableLiveData<Integer> ipLiveData = liveDataBus.with("serverPort", Integer.class);
        ipLiveData.setValue(portNum);
        Intent intent = new Intent(this, ChatServiceActivity.class);
        startActivity(intent);
    }

    private void startClient() {
        String ip = etIp.getText().toString();
        ip = ip.length() == 0 ? etIp.getHint().toString() : ip;
        String port = etPort.getText().toString();
        port = port.length() == 0 ? etPort.getHint().toString() : port;
        LiveDataBus liveDataBus = LiveDataBus.getInstance();
        MutableLiveData<String> ipLiveData = liveDataBus.with("ip", String.class);
        ipLiveData.setValue(ip);
        MutableLiveData<String> portLiveData = liveDataBus.with("port", String.class);
        portLiveData.setValue(port);
        Intent intent = new Intent(this, ChatClientActivity.class);
        startActivity(intent);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == permissionRequestCode) {
            for (int i : grantResults) {
                if (i != PackageManager.PERMISSION_GRANTED) {
                    return;
                }
            }
        }
    }
}