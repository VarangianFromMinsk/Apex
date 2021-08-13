package com.example.myapplication.main.Screens.Show_Image_MVP;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.myapplication.Common_Dagger_App_Class.App;
import com.example.myapplication.R;
import com.example.myapplication.Services.Online_Offline_User_Service_To_Firebase;
import com.example.myapplication.databinding.ShowImageActivityBinding;
import com.example.myapplication.main.Screens.User_List_4_States_MVVM.User_List_Activity;

import java.util.Objects;

import javax.inject.Inject;

public class Show_Image_Activity extends AppCompatActivity implements Show_Image_view {

    @Inject
    Online_Offline_User_Service_To_Firebase controller;

    private String image;

    private Show_Image_Presenter presenter;

    private ShowImageActivityBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.show_image_activity);

        Objects.requireNonNull(getSupportActionBar()).hide();

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        initialization();

        initBackBtn();

        loadImage();

        initShareBtn();

        initSaveBtn();
    }

    private void initialization(){

        ((App) getApplication()).getCommonComponent().inject(this);

        binding = DataBindingUtil.setContentView(this, R.layout.show_image_activity);

        presenter = new Show_Image_Presenter(this);

        //TODO: lifecycle
        getLifecycle().addObserver(controller);
    }

    private void initBackBtn() {
        binding.backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
    }

    private void loadImage() {
        Intent intent = getIntent();
        image = intent.getStringExtra("imageURL");


        try {
            Glide.with(binding.showImageForChat).load(image).into(binding.showImageForChat);
        } catch (Exception ignored) {}

    }

    private void initShareBtn() {
        binding.shareImageChatChat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Show_Image_Activity.this, User_List_Activity.class)
                        .putExtra("shareImage", "true")
                        .putExtra("check", "true")
                        .putExtra("imageURL", image)
                        .putExtra("typeOfUserList", "all");
                startActivity(intent);
            }
        });
    }

    private void initSaveBtn() {
        binding.saveImageInGallery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveImageToGallery();
            }
        });
    }

    private void saveImageToGallery() {
        presenter.saveImageInGallery(binding.showImageForChat,this);
    }

    @Override
    public void saved() {
        Toast.makeText(this, "Saved", Toast.LENGTH_SHORT).show();
    }

}