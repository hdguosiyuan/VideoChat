package com.example.chat;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;

import com.example.chat.camerax.CameraxActivity;
import com.example.chat.client.ChatClientActivity;
import com.example.chat.video.ChatServiceActivity;

public class MainActivity extends AppCompatActivity {

    private String[] permissions = {Manifest.permission.CAMERA, Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE};
    private int permissionRequestCode = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
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
        switch (view.getId()){
            case R.id.camera:
                intent = new Intent(this, CameraxActivity.class);
                startActivity(intent);
                break;
            case R.id.call_server:
                intent = new Intent(this, ChatServiceActivity.class);
                startActivity(intent);
                break;
            case R.id.call_client:
                intent = new Intent(this, ChatClientActivity.class);
                startActivity(intent);
                break;
        }
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