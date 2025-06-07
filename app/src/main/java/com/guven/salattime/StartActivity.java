package com.guven.salattime;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.QuickContactBadge;
import android.widget.VideoView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class StartActivity extends AppCompatActivity {

    VideoView view;
    Handler handler;
    Runnable runnable;
    Button manBtn, womanBtn;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_start);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        SharedPreferences preferences = getSharedPreferences("myPrefs", MODE_PRIVATE);
        boolean firstTime = preferences.getBoolean("firstTime", true);
        SharedPreferences.Editor editor = preferences.edit();

        //İlk giriş kontrolü
       /* if(!firstTime){
            startActivity(new Intent(this, MainActivity.class));
            finish();
        }*/

        manBtn = findViewById(R.id.manButton);
        womanBtn = findViewById(R.id.womanButton);

        view = findViewById(R.id.videoView);
        Uri uri = Uri.parse("android.resource://" + getPackageName()+ "/" + R.raw.startsalattime);
        view.setVideoURI(uri);
        view.start();

        //Her 10sn tekrar
        handler = new Handler();
        runnable = new Runnable() {
            @Override
            public void run() {
                view.seekTo(0); // başa sar
                view.start();
                handler.postDelayed(this,10000); // 10sn bir tekrar
            }
        };
        handler.postDelayed(runnable,10000); // İlk 10 sn sonra başla

        manBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handler.removeCallbacks(runnable); // durdurma
                editor.putBoolean("firstTime",false);
                editor.putString("gender","Man");
                editor.apply();
                Intent i = new Intent(StartActivity.this, InfoActivity.class);
                startActivity(i);
                finish();
            }
        });

        womanBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handler.removeCallbacks(runnable); // durdurma
                editor.putBoolean("firstTime",false);
                editor.putString("gender","Woman");
                editor.apply();
                Intent i = new Intent(StartActivity.this, InfoActivity.class);
                startActivity(i);
                finish();
            }
        });
    }
}