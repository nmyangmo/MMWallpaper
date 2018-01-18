package com.yangmo.mmwallpaper;

import android.Manifest;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import com.yangmo.mmwallpaper.fragment.ImageFlowFragment;
import com.yangmo.mmwallpaper.fragment.MainFragment;

public class MainActivity extends AppCompatActivity implements MainFragment.OnFragmentInteractionListener ,ImageFlowFragment.OnFragmentInteractionListener{
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)!=PackageManager.PERMISSION_GRANTED||
                    ContextCompat.checkSelfPermission(this,Manifest.permission.WRITE_EXTERNAL_STORAGE)!=PackageManager.PERMISSION_GRANTED){
                requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE,Manifest.permission.READ_EXTERNAL_STORAGE},2053);
            }else {
                getSupportFragmentManager().beginTransaction().replace(R.id.frame_layout, MainFragment.newInstance("","")).commit();
            }

        }else {
            getSupportFragmentManager().beginTransaction().replace(R.id.frame_layout, MainFragment.newInstance("","")).commit();
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode==2053){
            for (int i=0;i<grantResults.length;i++){
                if (grantResults[i]!=PackageManager.PERMISSION_GRANTED){
                    Toast.makeText(MainActivity.this,"请授予权限",Toast.LENGTH_SHORT).show();
                    return;
                }
            }
            getSupportFragmentManager().beginTransaction().replace(R.id.frame_layout, MainFragment.newInstance("","")).commit();

        }
    }

    @Override
    public void onFragmentInteraction(Uri uri) {

    }
}
