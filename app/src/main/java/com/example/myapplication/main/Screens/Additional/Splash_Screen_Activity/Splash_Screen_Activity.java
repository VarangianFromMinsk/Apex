package com.example.myapplication.main.Screens.Additional.Splash_Screen_Activity;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.view.WindowManager;

import com.example.myapplication.R;
import com.example.myapplication.main.Screens.Sing_In_MVP.Sign_In_Activity;

import java.util.Objects;

public class Splash_Screen_Activity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //todo: fullScreen
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.splash_screen_activity);

        // TODO: действие в другой потоке
        Thread thread = new Thread(){
            @Override
            public void run(){
                //TODO: само действие в другом потоке
                try{
                    sleep(2000);
                }catch (Exception ignored){
                }finally {
                    Intent intent = new Intent(Splash_Screen_Activity.this, Sign_In_Activity.class)
                            .addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                    overridePendingTransition(0, 0);
                    startActivity(intent);
                }
            }
        };
        thread.start();
    }

    @Override
    protected void onPause() {
        super.onPause();
        finish();
    }
}