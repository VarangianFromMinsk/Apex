package com.example.myapplication.main.Screens.Posts.Add_Change_Post_MVP;


import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.databinding.DataBindingUtil;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.myapplication.Common_Dagger_App_Class.App;
import com.example.myapplication.R;
import com.example.myapplication.Services.App_Constants;
import com.example.myapplication.Services.Check_Internet_Connection_Exist;
import com.example.myapplication.Services.Check_Permission_Service;
import com.example.myapplication.Services.Online_Offline_User_Service_To_Firebase;
import com.example.myapplication.databinding.AddPostActivityBinding;
import com.example.myapplication.main.Screens.Posts.Posts_By_Friends_MVP.Post_Activity_Friends;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;

import java.util.Objects;

import javax.inject.Inject;

public class Add_Change_Post_Activity extends AppCompatActivity implements Add_Change_Post_view {

    //todo: all info
    private String name, email, avatar, myUid;
    private String editImage;

    //todo: change or create posts
    private String updateKey, editPostId;

    private Uri image_rui = null;
    private ProgressDialog pd;

    @Inject
    Check_Internet_Connection_Exist checkInternetService;

    @Inject
    Online_Offline_User_Service_To_Firebase controller;


    //todo: create presenter
    private Add_Change_Post_Presenter presenter;

    private AddPostActivityBinding binding;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.add_post_activity);

        Objects.requireNonNull(getSupportActionBar()).hide();

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        initialization();

        checkNetworkState();

        changeOrCreatePost();

        getInfoOfCurrentUser();

        chooseImageBtn();

        uploadBtnMethod();
    }

    //TODO: main
    private void initialization() {

        ((App) getApplication()).getPostAddChangeComponent().inject(this);

        binding = DataBindingUtil.setContentView(this, R.layout.add_post_activity);

        presenter = new Add_Change_Post_Presenter(this);

        //TODO: lifecycle
        getLifecycle().addObserver(controller);

        pd = new ProgressDialog(this);
        myUid = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid();
    }

    private void checkNetworkState() {
        if(!checkInternetService.checkInternet(this)){
            showSnackBar("Internet disable", null, null);
        }
    }

    private void changeOrCreatePost() {
        Intent intent = getIntent();
        Bundle extras = intent.getExtras();
        if(extras != null){
            updateKey = intent.getStringExtra("key");
            editPostId = intent.getStringExtra("editPostId");
        }

        //todo: validate if we come to change post or create post
        try {
            if (updateKey.equals("editPost") && !editPostId.equals("")) {
                binding.pUploadBtn.setText(R.string.change);
                loadPostData(editPostId);
            } else {
                binding.pUploadBtn.setText(R.string.publish);
            }
        } catch (Exception ignored) {
        }
    }


    //TODO: part of load current post and user
    private void loadPostData(String editPostId) {
        presenter.loadData(editPostId);
    }

    @Override
    public void loadPostDataForChange(String title, String Description, String ImageUrl) {

        //todo: set data
        binding.pTitleEt.setText(title);
        binding.pDescriptionEt.setText(Description);
        if (!ImageUrl.equals("noImage")) {
            try {
                Glide.with(binding.pImageIv.getContext()).load(ImageUrl).into(binding.pImageIv);
            } catch (Exception ignored) {
            }
        }

        //todo: global var
        editImage = ImageUrl;
    }

    private void getInfoOfCurrentUser() {
        presenter.loadUserInfo(myUid);
    }

    @Override
    public void loadCurrentUserInfo(String namePr, String emailPr, String avatarPr) {
        name = namePr;
        email = emailPr;
        avatar = avatarPr;
    }

    private void uploadBtnMethod() {
        binding.pUploadBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                pd.setMessage("Publishing, waite ...");
                pd.show();

                String title = binding.pTitleEt.getText().toString().trim();
                String description = binding.pDescriptionEt.getText().toString().trim();
                if (TextUtils.isEmpty(title)) {
                    Toast.makeText(Add_Change_Post_Activity.this, "Enter title", Toast.LENGTH_LONG).show();
                    pd.dismiss();
                    return;
                }

                if (TextUtils.isEmpty(description)) {
                    Toast.makeText(Add_Change_Post_Activity.this, "Enter description", Toast.LENGTH_LONG).show();
                    pd.dismiss();
                    return;
                }

                if (updateKey.equals("editPost")) {
                    updatePost(title, description, editPostId);
                } else {
                    uploadData(title, description);
                }

            }
        });
    }


    //TODO: staff of choose image
    private void chooseImageBtn() {
        binding.pImageIv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (checkCurrentPermission()) {
                    showImagePickDialog();
                }
            }
        });
    }

    private void showImagePickDialog() {

        final BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(Add_Change_Post_Activity.this, R.style.BottomSheetDialogTheme);
        View bottomSheetView = LayoutInflater.from(this).inflate(R.layout.bottom_sheet_select_way_to_pick_photo, (ConstraintLayout) findViewById(R.id.bottom_sheet_photo));
        bottomSheetView.findViewById(R.id.sheetGallery).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pickFromGallery();
                bottomSheetDialog.dismiss();
            }
        });
        bottomSheetView.findViewById(R.id.sheetCamera).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pickFromCamera();
                bottomSheetDialog.dismiss();
            }
        });
        bottomSheetDialog.setContentView(bottomSheetView);
        bottomSheetDialog.show();


     /*   String[] options = {"Camera", "Gallery"};
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Choose image from");
        builder.setItems(options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (which == 0) {
                    pickFromCamera();
                }
                if (which == 1) {
                    pickFromGallery();
                }
            }
        });
        builder.create().show();

      */
    }

    private boolean checkCurrentPermission() {
        return Check_Permission_Service.checkPermission(this, Add_Change_Post_Activity.this, Manifest.permission.CAMERA, App_Constants.CAMERA_REQUEST_CODE_POST)
                && Check_Permission_Service.checkPermission(this, Add_Change_Post_Activity.this, Manifest.permission.READ_EXTERNAL_STORAGE, App_Constants.READING_REQUEST_CODE_POST)
                && Check_Permission_Service.checkPermission(this, Add_Change_Post_Activity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE, App_Constants.WRITE_REQUEST_CODE_POST);
    }

    private void pickFromGallery() {
        try {
            Intent intent = new Intent(Intent.ACTION_PICK);
            intent.setType("image/*");
            startActivityForResult(intent, App_Constants.IMAGE_PICK_GALLERY_CODE_POST);
        } catch (Exception e) {
            Toast.makeText(this, "Check permissions", Toast.LENGTH_SHORT).show();
        }
    }

    private void pickFromCamera() {
        ContentValues cv = new ContentValues();
        cv.put(MediaStore.Images.Media.TITLE, "Temp Pick");
        cv.put(MediaStore.Images.Media.DESCRIPTION, "Temp Descr");
        image_rui = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, cv);

        try {
            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            intent.putExtra(MediaStore.EXTRA_OUTPUT, image_rui);
            startActivityForResult(intent, App_Constants.IMAGE_PICK_CAMERA_CODE_POST);
        } catch (Exception e) {
            Toast.makeText(this, "Check permissions", Toast.LENGTH_SHORT).show();
        }

    }


    //TODO: staff of update
    private void updatePost(String title, String description, String editPostId) {
        pd.setMessage("Changing Post");
        pd.show();

        if (!editImage.equals("noImage") && !editPostId.equals("")) {
            //todo: with image
            updateWasWithImage(title, description, editPostId);
        } else if (binding.pImageIv.getDrawable() != null) {
            //todo: with image
            updateWithNowImage(title, description, editPostId);
        } else {
            //TODO: without Image
            updateWithoutImage(title, description, editPostId);
        }

    }

    private void uploadData(String title, String description) {
        pd.setMessage("Publishing...");
        pd.show();
        //todo: for post-image name, post -id, post - publish - time
        String timeStamp = String.valueOf(System.currentTimeMillis());
        String filePathAndName = "Posts/" + "post_" + timeStamp;

        presenter.uploadData(binding.pImageIv, filePathAndName, myUid, name, email, avatar, timeStamp, title, description);
    }

    @Override
    public void uploadDataComplete(Boolean isSuccess, String postId) {
        pd.dismiss();
        if (isSuccess) {
            Toast.makeText(Add_Change_Post_Activity.this, "Post published", Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(Add_Change_Post_Activity.this, "Error", Toast.LENGTH_LONG).show();
        }

        editPostId = postId;
        refreshActivity();
    }

    private void updateWasWithImage(String title, String description, String editPostId) {

        presenter.updateWasWithImage(editImage, binding.pImageIv, myUid, name, email, avatar, title, description, editPostId);
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
        presenter.updateWithNowImage(binding.pImageIv, fileNameAndPath, myUid, name, email, avatar, title, description, editPostId);
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
    private void refreshActivity() {
        try {
            Intent intent = new Intent()
                    .putExtra("key", "editPost")
                    .putExtra("editPostId", editPostId);
            overridePendingTransition(0, 0);
            startActivity(intent);
            finish();
        } catch (Exception e) {
            e.printStackTrace();
            startActivity(new Intent(Add_Change_Post_Activity.this, Post_Activity_Friends.class));
        }
    }

    public void updateToast(boolean isSuccess) {
        if (isSuccess) {
            Toast.makeText(Add_Change_Post_Activity.this, "Post updated", Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(Add_Change_Post_Activity.this, "Error", Toast.LENGTH_LONG).show();
        }
    }

    private void showSnackBar(final String mainText, final String action, View.OnClickListener listener) {
        Snackbar.make(findViewById(android.R.id.content), mainText, Snackbar.LENGTH_INDEFINITE).setAction(action, listener).show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (resultCode == RESULT_OK) {
            if (requestCode == App_Constants.IMAGE_PICK_GALLERY_CODE_POST) {
                //TODO: image is picked from gallery, get uri of image
                assert data != null;
                image_rui = data.getData();
                binding.pImageIv.setImageURI(image_rui);

            } else if (requestCode == App_Constants.IMAGE_PICK_CAMERA_CODE_POST) {
                //TODO: image is picked from camera, get uri of image
                binding.pImageIv.setImageURI(image_rui);
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
}
