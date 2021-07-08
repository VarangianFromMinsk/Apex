package com.example.myapplication.main.Screens.Show_Image_MVP;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.myapplication.R;
import com.example.myapplication.Services.Online_Offline_Service;
import com.example.myapplication.main.Screens.User_List_4_States_MVVM.User_List_Activity;

public class Show_Image_Activity extends AppCompatActivity implements Show_Image_view {

    private ImageView showImage;
    private String image;
    private Button shareImage, saveImageBtn;
    private ImageButton backBtn;

    private Show_Image_Presenter presenter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.show_image_activity);

        getSupportActionBar().hide();

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        presenter = new Show_Image_Presenter(this);

        initialization();

        initBackBtn();

        loadImage();

        initShareBtn();

        initSaveBtn();

        updateUserStatus("online");
    }

    private void initialization(){
        backBtn = findViewById(R.id.backBtn);
        shareImage = findViewById(R.id.shareImageChatChat);
        showImage = findViewById(R.id.showImageForChat);
        saveImageBtn = findViewById(R.id.saveImageInGallery);
    }

    private void initBackBtn() {
        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
    }

    private void loadImage() {
        Intent intent = getIntent();
        image = intent.getStringExtra("imageURL");

        if(intent!=null){
            Glide.with(showImage).load(image).into(showImage);
        }
    }

    private void initShareBtn() {
        shareImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Show_Image_Activity.this, User_List_Activity.class);
                intent.putExtra("check", "true");
                intent.putExtra("imageURL", image);
                startActivity(intent);
            }
        });
    }

    private void initSaveBtn() {
        saveImageBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveImageToGallery();
            }
        });
    }

    private void saveImageToGallery() {
        presenter.saveImageInGallery(showImage,this);
    }

    @Override
    public void saved() {
        Toast.makeText(this, "Saved", Toast.LENGTH_SHORT).show();
    }

    //TODO: Block online/offline
    public void updateUserStatus( String state){
        Online_Offline_Service service = new Online_Offline_Service();
        service.updateUserStatus(state, this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateUserStatus("online");
    }

    @Override
    protected void onPause() {
        super.onPause();
        updateUserStatus("offline");
    }
}