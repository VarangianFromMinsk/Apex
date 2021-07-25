package com.example.myapplication.main.Screens.Posts.Add_Change_Post_MVP;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.myapplication.R;
import com.example.myapplication.Services.App_Constants;
import com.example.myapplication.Services.Check_Permission_Service;
import com.example.myapplication.Services.Online_Offline_Service;
import com.google.firebase.auth.FirebaseAuth;
import java.util.Objects;

public class Add_Change_Post_Activity extends AppCompatActivity implements Add_Change_Post_view {

    private EditText titleEt, descriptionEt;
    private ImageView imageIv;
    private Button uploadBtn;

    //all info
    private String name, email, avatar, myUid;
    private String editImage;

    //change or create posts
    private String isUpdateKey, editPostId;;

    private Uri image_rui = null;
    private ProgressDialog pd;

    //todo: create presenter
    private Add_Change_Post_Presenter presenter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.add_post_activity);

        Objects.requireNonNull(getSupportActionBar()).hide();

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        presenter = new Add_Change_Post_Presenter(this);

        initialization();

        changeOrCreatePost();

        getInfoOfCurrentUser();

        chooseImageBtn();

        uploadBtnMethod();

        updateUserStatus("online");
    }

    //TODO: main
    private void initialization(){
        myUid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        pd = new ProgressDialog(this);
        titleEt = findViewById(R.id.pTitleEt);
        descriptionEt = findViewById(R.id.pDescriptionEt);
        imageIv = findViewById(R.id.pImageIv);
        uploadBtn = findViewById(R.id.pUploadBtn);
    }

    private void changeOrCreatePost(){
        Intent intent = getIntent();
        isUpdateKey = "" + intent.getStringExtra("key");
        editPostId = "" + intent.getStringExtra("editPostId");

        //todo: validate if we come to change post or create post
        if(isUpdateKey.equals("editPost") && !editPostId.equals("")){
            uploadBtn.setText(R.string.change);
            loadPostData(editPostId);
        }
        else{
            uploadBtn.setText(R.string.publish);
        }
    }

    //TODO: part of load current post and user
    private void loadPostData(String editPostId) {
        presenter.loadData(editPostId);
    }

    @Override
    public void loadPostDataForChange(String title, String Description, String ImageUrl) {

        //todo: set data
        titleEt.setText(title);
        descriptionEt.setText(Description);
        if(!ImageUrl.equals("noImage")){
            try{
                Glide.with(imageIv.getContext()).load(ImageUrl).into(imageIv);
            }catch (Exception ignored){}
        }

        //todo: global var
        editImage = ImageUrl;
    }


    private void getInfoOfCurrentUser(){
        presenter.loadUserInfo(myUid);
    }

    @Override
    public void loadCurrentUserInfo(String namePr, String emailPr, String avatarPr) {
        name = namePr;
        email = emailPr;
        avatar = avatarPr;
    }


    private void uploadBtnMethod(){
        uploadBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                pd.setMessage("Publishing, waite ...");
                pd.show();

                String title = titleEt.getText().toString().trim();
                String description = descriptionEt.getText().toString().trim();
                if(TextUtils.isEmpty(title)){
                    Toast.makeText(Add_Change_Post_Activity.this, "Enter title", Toast.LENGTH_LONG).show();
                    pd.dismiss();
                    return;
                }

                if(TextUtils.isEmpty(description)){
                    Toast.makeText(Add_Change_Post_Activity.this, "Enter description", Toast.LENGTH_LONG).show();
                    pd.dismiss();
                    return;
                }

                if(isUpdateKey.equals("editPost")){
                    beginUpdate(title, description, editPostId);
                }
                else{
                    uploadData(title, description);
                }

            }
        });
    }

    //TODO: staff of choose image
    private void chooseImageBtn(){
        imageIv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(checkCurrentPermission()){
                    showImagePickDialog();
                }
            }
        });
    }

    private void showImagePickDialog() {
        String[] options = {"Camera","Gallery"};
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Choose image from");
        builder.setItems(options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if(which==0){
                    pickFromCamera();
                }
                if(which==1){
                    pickFromGallery();
                }
            }
        });
        builder.create().show();
    }

    private boolean checkCurrentPermission() {
        if(Check_Permission_Service.checkPermission(this,Add_Change_Post_Activity.this, Manifest.permission.CAMERA, App_Constants.CAMERA_REQUEST_CODE_POST)
        && Check_Permission_Service.checkPermission(this,Add_Change_Post_Activity.this, Manifest.permission.READ_EXTERNAL_STORAGE, App_Constants.READING_REQUEST_CODE_POST)
        && Check_Permission_Service.checkPermission(this,Add_Change_Post_Activity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE, App_Constants.WRITE_REQUEST_CODE_POST)){
            return true;
        }
        return false;
    }

    private void pickFromGallery() {
        try {
            Intent intent = new Intent(Intent.ACTION_PICK);
            intent.setType("image/*");
            startActivityForResult(intent, App_Constants.IMAGE_PICK_GALLERY_CODE_POST);
        }catch (Exception e){
            Toast.makeText(this, "Check permissions", Toast.LENGTH_SHORT).show();
        }
    }

    private void pickFromCamera() {
        ContentValues cv= new ContentValues();
        cv.put(MediaStore.Images.Media.TITLE, "Temp Pick");
        cv.put(MediaStore.Images.Media.DESCRIPTION, "Temp Descr");
        image_rui = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, cv);

        try {
            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            intent.putExtra(MediaStore.EXTRA_OUTPUT, image_rui);
            startActivityForResult(intent, App_Constants.IMAGE_PICK_CAMERA_CODE_POST);
        }catch (Exception e){
            Toast.makeText(this, "Check permissions", Toast.LENGTH_SHORT).show();
        }

    }

    //TODO: staff of update
    private void beginUpdate(String title, String description, String editPostId) {
        pd.setMessage("Changing Post");
        pd.show();

        if(!editImage.equals("noImage") && !editPostId.equals("")){
            //todo: with image
            updateWasWithImage(title, description, editPostId);
        }
        else if(imageIv.getDrawable() != null){
            //todo: with image
            updateWithNowImage(title, description, editPostId);
        }
        else{
            //TODO: without Image
            updateWithoutImage(title, description, editPostId);
        }

    }

    //TODO: loading part
    private void uploadData(String title, String description) {
        pd.setMessage("Publishing...");
        pd.show();
        //todo: for post-image name, post -id, post - publish - time
        String timeStamp = String.valueOf(System.currentTimeMillis());
        String filePathAndName = "Posts/" + "post_" + timeStamp;

        presenter.uploadData(imageIv, filePathAndName, myUid, name, email, avatar, timeStamp, title, description);
    }
    @Override
    public void uploadDataComplete(Boolean isSuccess, String postId) {
        pd.dismiss();
        if(isSuccess){
            Toast.makeText(Add_Change_Post_Activity.this, "Post published", Toast.LENGTH_LONG).show();
        }
        else{
            Toast.makeText(Add_Change_Post_Activity.this, "Error", Toast.LENGTH_LONG).show();
        }

        editPostId = postId;
        refreshActivity();
    }


    private void updateWasWithImage(String title, String description, String editPostId) {

        presenter.updateWasWithImage(editImage, imageIv, myUid, name, email, avatar, title, description, editPostId);
        pd.setMessage("Compress photo and send post");
        pd.show();
    }
    @Override
    public void updateWasWithImageComplete(Boolean isSuccess) {
        pd.dismiss();
        updateToast(isSuccess);
        refreshActivity();
    }


    private void updateWithNowImage(String title, String description, String editPostId) {

        //todo: image deleted, upload new image
        String timeStamp = String.valueOf(System.currentTimeMillis());
        String fileNameAndPath = "Posts/" + "post_" + timeStamp;

        pd.setMessage("Compress photo...");
        pd.show();
        presenter.updateWithNowImage(imageIv, fileNameAndPath, myUid, name, email, avatar, title, description, editPostId);
    }
    @Override
    public void updateWithNowImageComplete(Boolean isSuccess) {
        pd.dismiss();
        updateToast(isSuccess);
        refreshActivity();
    }


    private void updateWithoutImage(String title, String description, String editPostId) {
        pd.setMessage("updating");
        pd.show();
        presenter.updateWithoutImage(myUid, name, email, avatar, title, description, editPostId);
    }
    @Override
    public void updateWithoutImageComplete(Boolean isSuccess) {
        pd.dismiss();
        updateToast(isSuccess);
        refreshActivity();
    }


    //TODO: support methods
    private void refreshActivity(){
        Intent intent = getIntent();
        intent.putExtra("key", "editPost");
        intent.putExtra("editPostId", editPostId);
        finish();
        overridePendingTransition(0, 0);
        startActivity(intent);
    }

    public void updateToast(boolean isSuccess){
        if(isSuccess){
            Toast.makeText(Add_Change_Post_Activity.this, "Post updated", Toast.LENGTH_LONG).show();
        }
        else{
            Toast.makeText(Add_Change_Post_Activity.this, "Error", Toast.LENGTH_LONG).show();
        }
    }

    //TODO: Block online/offline
    public void updateUserStatus( String state){
        Online_Offline_Service.updateUserStatus(state, this);
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if(resultCode == RESULT_OK){
            if(requestCode == App_Constants.IMAGE_PICK_GALLERY_CODE_POST){
                //TODO: image is picked from gallery, get uri of image
               image_rui = data.getData();
               imageIv.setImageURI(image_rui);

            }
            else if(requestCode ==  App_Constants.IMAGE_PICK_CAMERA_CODE_POST){
                //TODO: image is picked from camera, get uri of image
                imageIv.setImageURI(image_rui);
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

}
