package com.example.facerecognitionmulltiplefaces;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {
    boolean mPermissionReady;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Button recognizebutton = findViewById(R.id.recognizeButton);
        Button trainingbutton = findViewById(R.id.trainingButton);
        Button detectionbutton = findViewById(R.id.detectionButton);
        detectionbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(mPermissionReady)
                {
                    startActivity(new Intent(MainActivity.this,DetectionActivity.class));
                }
            }
        });
        trainingbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(mPermissionReady)
                {
                    startActivity(new Intent(MainActivity.this,NameActivity.class));
                }
            }
        });
        recognizebutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(mPermissionReady)
                {
                    Intent intent = new Intent(MainActivity.this,Training.class);
                    intent.putExtra("recognize",1);
                    intent.putExtra("reset",0);
                    intent.putExtra("name","");
                    startActivity(intent);
                }
            }
        });
        int cameraPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA);                    //Permission for camera
        int storagePermssion = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);    //Permission to write external storage
        mPermissionReady = cameraPermission == PackageManager.PERMISSION_GRANTED                                              //Checking if both permissions are granted or not
                && storagePermssion == PackageManager.PERMISSION_GRANTED;
        if (!mPermissionReady)                                                                                                //If permissions are not granted
            requirePermissions();
    }

    private void requirePermissions() {
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE}, 11);  //Aquiring permissions
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        Map<String, Integer> perm = new HashMap<>();                                                //Creating a hashmap
        perm.put(Manifest.permission.CAMERA, PackageManager.PERMISSION_DENIED);                     //Putting required permissions with false in there
        perm.put(Manifest.permission.WRITE_EXTERNAL_STORAGE, PackageManager.PERMISSION_DENIED);
        for (int i = 0; i < permissions.length; i++) {
            perm.put(permissions[i], grantResults[i]);                                              //Now checking permissions and entering it into hashmap to see if they are granted or not
        }
        if (perm.get(Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
                && perm.get(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            mPermissionReady = true;                                                                                    //if both are granted then we have both the permissions
        } else {
            if (!ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.CAMERA)
                    || !ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                new AlertDialog.Builder(this)
                        .setMessage(R.string.permission_warning)
                        .setPositiveButton(R.string.dismiss, null)
                        .show();
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }
}