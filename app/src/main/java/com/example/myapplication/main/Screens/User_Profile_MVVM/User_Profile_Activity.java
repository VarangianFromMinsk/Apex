package com.example.myapplication.main.Screens.User_Profile_MVVM;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.Manifest;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.ActivityOptions;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.os.Looper;
import android.provider.MediaStore;
import android.text.InputType;
import android.util.Pair;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.myapplication.main.Screens.Dashboard_MVP.Dashboard_Activity;
import com.example.myapplication.R;
import com.example.myapplication.Services.App_Constants;
import com.example.myapplication.Services.Check_Permission_Service;
import com.example.myapplication.Services.Online_Offline_Service;
import com.example.myapplication.main.Screens.Settings.Settings_Activity;
import com.example.myapplication.main.Models.Model_Post;
import com.example.myapplication.main.Models.Model_User;
import com.example.myapplication.main.Screens.Posts.Posts_By_Friends_MVP.Post_Activity_Friends;
import com.example.myapplication.main.Screens.Posts.Posts_By_Friends_MVP.Post_Adapter_Friends;
import com.example.myapplication.main.Screens.Show_Image_MVP.Show_Image_Activity;
import com.example.myapplication.main.Screens.Sing_In_MVP.Sign_In_Activity;
import com.example.myapplication.main.Screens.User_List_4_States_MVVM.User_List_Activity;
import com.example.myapplication.main.Screens.Find_Selected_Or_My_User_Location.Users_FInd_Location;
import com.example.myapplication.main.Models.Model_Song;
import com.example.myapplication.main.Screens.Music.Music_List_Activity_MVVM.Music_Adapter;
import com.example.myapplication.main.Screens.Music.Music_List_Activity_MVVM.Music_List_Activity;
import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;

public class User_Profile_Activity extends AppCompatActivity {

    //todo: views to the 1st block
    private ImageView background;
    private TextView upUserName,upUserNickname,onOffLine,timeOnline;
    private CircleImageView upUserAvatar;
    private ImageButton addAvatarPhoto, changePassword, settings, SingOut, addBackground, buildLocationBtn;
    private TextView nickname;
    private RelativeLayout relLayWithLocBtn;

    //todo: views to the 2 block
    private TextView countLeft;
    private TextView countRight;

    //todo: views in edit profile info
    private String nameFromBase,emailFromBase,nickFromBase,telephone,avatarUrlFromBase, backgroundImage, statusOn, timeOnlineDb;
    private TextInputEditText nameInEdit,emailInEdit,nicknameInEdit,telephoneInEdit;
    private Button update;

    //todo: about user
    private String hisId, selectedUser, keyUser;
    private boolean isThatCurrentUser;
    private  FirebaseUser user;

    //image picked will be same in this uri
    private Uri image_rui = null;
    private ProgressDialog progressDialog;

    //all staff for recycler
    private RecyclerView recyclerViewPosts;
    private Post_Adapter_Friends postAdapter;

    //all staff to show/hide when check post
    private CardView cardPost;
    boolean isPost;
    private TextInputLayout nameInEditLay,emailInEditLay,nicknameInEditLay,telephoneInEditLay;
    private TextView noPostTv;

    //staff to add image
    private byte[] dataCompr;

    //staff to show/hide when check music
    private CardView cardSongs;
    private boolean isSongs;
    private TextView noSongsTv;
    private RecyclerView recyclerViewSongs;
    private Music_Adapter songAdapter;

    //progressbar for songs and posts
    private ProgressBar progressBar;
    private BottomNavigationView bottomNavigationView;

    //todo: values to update user location
    private FusedLocationProviderClient fusedLocationClient;
    private SettingsClient settingsClient;
    private LocationRequest locationRequest;
    private LocationSettingsRequest locationSettingsRequest;
    private LocationCallback locationCallback;
    private Location currentLocation;
    private boolean isLocationUpdateActive;

    //staff for hide up block
    private RelativeLayout upRelativeLayout;
    //refresh
    private SwipeRefreshLayout swipeRefreshLayout;
    //to load music
    private RelativeLayout backRecyclerLay;

    private Profile_ViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.user_profile_activity);

        Objects.requireNonNull(getSupportActionBar()).hide();

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        viewModel = new ViewModelProvider(this).get(Profile_ViewModel.class);

        initialization();

        isNetworkConnected();

        showStrangerProfile();

        showUserData();

        preLoadPosts();

        preLoadSongs();

        checkCountOfYourPost();

        checkCountOfYourSongs();

        updateNavigationMenu();

        createPathLocationBtn();

        showBigAvatarAction();

        changePasswordBtn();

        initRefreshLayout();

        updateUserStatus("online");

        updateLocation();
    }

    private void initialization() {

        user = FirebaseAuth.getInstance().getCurrentUser();

        recyclerViewPosts = findViewById(R.id.postRecyclerViewUserProfile);
        recyclerViewSongs = findViewById(R.id.songsRecyclerViewUserProfile);

        background = findViewById(R.id.backInUserProfile);
        bottomNavigationView = findViewById(R.id.bottomNavigation);
        swipeRefreshLayout = findViewById(R.id.swipeLayUserProfile);
        relLayWithLocBtn = findViewById(R.id.relLayWithLocBtn);
        countLeft = findViewById(R.id.countLeft);
        countRight = findViewById(R.id.countRight);
        update = findViewById(R.id.updateProfile);
        changePassword = findViewById(R.id.passwordButtonInProfile);
        settings = findViewById(R.id.settingsButtonInProfile);
        SingOut = findViewById(R.id.singOutButtonInProfile);
        addBackground = findViewById(R.id.addBackground);
        buildLocationBtn = findViewById(R.id.locationButtonInProfile);
        nameInEdit = findViewById(R.id.nameInEdit);
        emailInEdit = findViewById(R.id.emailInEdit);
        nicknameInEdit = findViewById(R.id.nicknameInEdit);
        telephoneInEdit = findViewById(R.id.telephoneInEdit);
        upUserName = findViewById(R.id.profileScreenName);
        upUserNickname = findViewById(R.id.profileScreenNickname);
        upUserAvatar = findViewById(R.id.profileScreenAvatar);
        onOffLine = findViewById(R.id.onOffLineProfile);
        timeOnline = findViewById(R.id.timeOnlineProfile);
        progressBar = findViewById(R.id.progressBarInUserProfile);
        addAvatarPhoto = findViewById(R.id.addUserPhotoButtonInProfile);

        progressDialog = new ProgressDialog(this);

        //todo: changeable data of profile
        nameInEditLay = findViewById(R.id.textFieldName);
        emailInEditLay = findViewById(R.id.textFieldEmail);
        nicknameInEditLay = findViewById(R.id.textFieldNickName);
        telephoneInEditLay = findViewById(R.id.textFieldTelephone);

        //todo: view of get nothing
        noPostTv = findViewById(R.id.noPostsProfile);
        noPostTv.setVisibility(View.GONE);
        noSongsTv = findViewById(R.id.noSongsProfile);
        noSongsTv.setVisibility(View.GONE);

        upRelativeLayout = findViewById(R.id.backgroundFromAvatar);
        backRecyclerLay = findViewById(R.id.backProfileAll);

        cardPost = findViewById(R.id.profileCardPosts);
        cardSongs = findViewById(R.id.musicCardView);
    }

    //todo: strange profile
    private void showStrangerProfile() {
        Intent intent = getIntent();
        hisId = intent.getStringExtra("hisId");
        if(hisId != null){
            selectedUser = hisId;
            isThatCurrentUser = false;
            hideViewsIfIsStrangerProfile();

        }else{
            selectedUser = user.getUid();
            isThatCurrentUser = true;
        }
    }

    //todo: current user info part
    private void showUserData() {
        //todo: hide check staff
        noPostTv.setVisibility(View.GONE);
        noSongsTv.setVisibility(View.GONE);
        progressBar.setVisibility(View.VISIBLE);

        viewModel.loadUserInfo(selectedUser);

        if(!viewModel.getMutCurrentUser().hasObservers()){
            viewModel.getMutCurrentUser().observe(this, new Observer<Model_User>() {
                @Override
                public void onChanged(Model_User user) {
                    if(!isThatCurrentUser){
                        hideViewsIfIsStrangerProfile();
                    }

                    //TODO: get data
                    avatarUrlFromBase = user.getAvatarMockUpResourse();
                    nameFromBase = user.getName();
                    emailFromBase = user.getEmail();
                    statusOn = user.getOnline();
                    timeOnlineDb = user.getTimeonline();

                    //TODO: set data
                    nameInEdit.setText(nameFromBase);
                    emailInEdit.setText(emailFromBase);
                    onOffLine.setText(statusOn);
                    backgroundImage = user.getBackground();

                    try {
                        nickFromBase = user.getNick();
                        telephone = user.getTelephone();
                    }catch (Exception ignored){}

                    if(nickFromBase.equals("")){
                        nicknameInEdit.setTextSize(14);
                        nicknameInEdit.setAlpha((float) 0.8);
                    }else{
                        nicknameInEdit.setTextSize(16);
                        nicknameInEdit.setAlpha(1);
                        nicknameInEdit.setText(nickFromBase);
                    }

                    if(telephone.equals("")){
                        telephoneInEdit.setTextSize(14);
                        telephoneInEdit.setAlpha((float) 0.8);
                    }else{
                        nicknameInEdit.setTextSize(16);
                        telephoneInEdit.setAlpha(1);
                        telephoneInEdit.setText(telephone);
                    }

                    if(statusOn.equals("online")){
                        onOffLine.setTextColor(Color.parseColor("#7AC537"));
                        timeOnline.setVisibility(View.GONE);
                    }else {
                        onOffLine.setTextColor(Color.parseColor("#D32121"));
                        timeOnline.setVisibility(View.VISIBLE);
                        timeOnline.setText(timeOnlineDb);
                    }

                    try {
                        upUserName.setText(nameFromBase);
                        upUserNickname.setText(nickFromBase);
                    }catch (Exception ignored){}

                    //TODO: set images
                    try {
                        if(!backgroundImage.equals("")) {
                            Glide.with(background).load(backgroundImage).into(background);
                        }
                        else{
                            Glide.with(background).load(R.drawable.wallpaper).into(background);
                        }
                        if(!avatarUrlFromBase.equals("")) {
                            Glide.with(upUserAvatar).load(avatarUrlFromBase).placeholder(R.drawable.default_avatar).into(upUserAvatar);
                        }
                    }catch (Exception ignored){}

                    progressBar.setVisibility(View.GONE);
                }
            });
        }
    }

    private void hideViewsIfIsStrangerProfile(){
        //TODO: disable main line
        nameInEdit.setEnabled(false);
        emailInEdit.setEnabled(false);
        nicknameInEdit.setEnabled(false);
        telephoneInEdit.setEnabled(false);

        //TODO: hide functions
        update.setVisibility(View.GONE);
        changePassword.setVisibility(View.GONE);
        settings.setVisibility(View.GONE);
        SingOut.setVisibility(View.GONE);
        addBackground.setVisibility(View.GONE);
        addAvatarPhoto.setVisibility(View.GONE);

        RelativeLayout.LayoutParams newParams = new  RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.WRAP_CONTENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT);

        newParams.addRule(RelativeLayout.ALIGN_PARENT_START,RelativeLayout.ALIGN_PARENT_TOP);

        relLayWithLocBtn.setLayoutParams(newParams);
    }

    //todo: block posts
    private void preLoadPosts(){
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerViewPosts.setLayoutManager(layoutManager);

        postAdapter = new Post_Adapter_Friends(this);
        postAdapter.setPostList(new ArrayList<Model_Post>());
        recyclerViewPosts.setAdapter(postAdapter);

        isPost = false;

        cardPost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!isPost){
                    isPost = true;
                    //todo: change bottom nav bar colour
                    bottomNavigationView.animate().alpha(0.0f).translationY(150).setDuration(250).setListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            super.onAnimationEnd(animation);
                            bottomNavigationView.setVisibility(View.GONE);
                        }
                    });

                    mainSwitchContent();
                }
                else{
                    isPost=false;
                    cardSongs.setVisibility(View.VISIBLE);
                    cardPost.setVisibility(View.VISIBLE);
                    //todo: change bottom nav bar colour
                    if(isThatCurrentUser){
                        bottomNavigationView.setVisibility(View.VISIBLE);
                        bottomNavigationView.animate().alpha(1).translationY(0).setDuration(500).setListener(new AnimatorListenerAdapter() {
                            @Override
                            public void onAnimationEnd(Animator animation) {
                                super.onAnimationEnd(animation);
                                bottomNavigationView.setVisibility(View.VISIBLE);
                            }
                        });
                    }

                    mainSwitchContent();
                }

            }
        });
    }

    private void loadPosts() {
        noPostTv.setVisibility(View.GONE);
        progressBar.setVisibility(View.VISIBLE);

        viewModel.loadPosts(selectedUser);
    }

    private void checkCountOfYourPost(){
        viewModel.loadCountOfPost(selectedUser);
        viewModel.getCountOfPosts().observe(this, new Observer<String>() {
            @Override
            public void onChanged(String s) {
                countLeft.setText(s);
            }
        });
    }

    //todo: block songs
    private void preLoadSongs(){
        LinearLayoutManager layoutManagerSongs = new LinearLayoutManager(this);
        recyclerViewSongs.setLayoutManager(layoutManagerSongs);

        songAdapter = new Music_Adapter(this);
        songAdapter.setMusicList(new ArrayList<Model_Song>());
        recyclerViewSongs.setAdapter(songAdapter);

        isSongs = false;

        cardSongs.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!isSongs){
                    isSongs = true;
                    //change back colour
                    backRecyclerLay.setBackgroundColor(getResources().getColor(R.color.profileGrey));
                    //change bottom nav bar colour
                    bottomNavigationView.animate().alpha(0.0f).translationY(250).setDuration(500).setListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            super.onAnimationEnd(animation);
                            bottomNavigationView.setVisibility(View.GONE);
                        }
                    });

                    mainSwitchContent();
                }
                else{
                    isSongs = false;
                    //change back colour
                    backRecyclerLay.setBackgroundColor(getResources().getColor(R.color.profileNormal));
                    cardPost.setVisibility(View.VISIBLE);

                    if(isThatCurrentUser){
                        bottomNavigationView.setVisibility(View.VISIBLE);
                        bottomNavigationView.animate().alpha(1).translationY(0).setDuration(500).setListener(new AnimatorListenerAdapter() {
                            @Override
                            public void onAnimationEnd(Animator animation) {
                                super.onAnimationEnd(animation);
                                bottomNavigationView.setVisibility(View.VISIBLE);
                            }
                        });
                    }

                    mainSwitchContent();
                }

            }
        });
    }

    private void loadSongs(){
        noSongsTv.setVisibility(View.GONE);
        progressBar.setVisibility(View.VISIBLE);

        viewModel.loadMusic(selectedUser);
    }

    private void checkCountOfYourSongs(){
        viewModel.loadCountOfSongs(selectedUser);
        viewModel.getCountOfSongs().observe(this, new Observer<String>() {
            @Override
            public void onChanged(String s) {
                countRight.setText(s);
            }
        });
    }

    //todo: main switch
    private void mainSwitchContent(){
        if(isPost){
            swipeRefreshLayout.setEnabled(false);
            //todo: hide up block
            upRelativeLayout.setVisibility(View.GONE);
            //todo: check
            cardPost.setVisibility(View.VISIBLE);
            recyclerViewSongs.setVisibility(View.GONE);
            recyclerViewPosts.setVisibility(View.VISIBLE);
            //todo: hide one cardView
            cardSongs.setVisibility(View.GONE);
            //todo: hide user info
            hideUserInfo(true);

            loadPosts();

            if(!viewModel.getMutCurrentUserPostList().hasObservers()){
                viewModel.getMutCurrentUserPostList().observe(this, new Observer<ArrayList<Model_Post>>() {
                    @Override
                    public void onChanged(ArrayList<Model_Post> posts) {
                        progressBar.setVisibility(View.GONE);
                        if(posts.size() == 0){
                            noPostTv.setVisibility(View.VISIBLE);
                        }
                        else{
                            postAdapter.setPostList(posts);
                        }
                    }
                });
            }

        }
        else if(isSongs){
            swipeRefreshLayout.setEnabled(false);
            //todo: hide up block
            upRelativeLayout.setVisibility(View.GONE);
            //todo: check
            cardSongs.setVisibility(View.VISIBLE);
            recyclerViewPosts.setVisibility(View.GONE);
            recyclerViewSongs.setVisibility(View.VISIBLE);
            //todo: hide one cardView
            cardPost.setVisibility(View.GONE);
            //todo: hide user info
            hideUserInfo(true);

            loadSongs();

            if(!viewModel.getMutCurrentUserMusic().hasObservers()){
                viewModel.getMutCurrentUserMusic().observe(this, new Observer<ArrayList<Model_Song>>() {
                    @Override
                    public void onChanged(ArrayList<Model_Song> songs) {
                        progressBar.setVisibility(View.GONE);
                        if(songs.size() == 0){
                            noSongsTv.setVisibility(View.VISIBLE);
                        }
                        else{
                            songAdapter.setMusicList(songs);
                        }
                    }
                });
            }

        }
        else{
            swipeRefreshLayout.setEnabled(true);
            //todo: show up block
            upRelativeLayout.setVisibility(View.VISIBLE);

            //todo: hide recyclers
            recyclerViewPosts.setVisibility(View.GONE);
            recyclerViewSongs.setVisibility(View.GONE);

            //todo: show userInfo
            hideUserInfo(false);

            showUserData();
        }

    }

    public void hideUserInfo(boolean isHide){
        if(isHide){
            nameInEditLay.setVisibility(View.GONE);
            emailInEditLay.setVisibility(View.GONE);
            nicknameInEditLay.setVisibility(View.GONE);
            telephoneInEditLay.setVisibility(View.GONE);
            update.setVisibility(View.GONE);
        }
        else{
            nameInEditLay.setVisibility(View.VISIBLE);
            emailInEditLay.setVisibility(View.VISIBLE);
            nicknameInEditLay.setVisibility(View.VISIBLE);
            telephoneInEditLay.setVisibility(View.VISIBLE);
            update.setVisibility(View.VISIBLE);
        }
    }

    //todo: change userDara
    public void update(View view) {

        if(isThatCurrentUser){
            if(isNameChanged() || isEmailChanged() || isNickNameCahnged() || isTelephoneChanged() ){
                //name
                if(!nameFromBase.equals(nameInEdit.getText().toString())){
                    viewModel.changeUserData(selectedUser, "name", nameInEdit.getText().toString().trim() );
                }
                //telephone
                if(!telephone.equals(telephoneInEdit.getText().toString())){
                    viewModel.changeUserData(selectedUser, "telephone", telephoneInEdit.getText().toString().trim());
                }
                //nick
                if(!nickFromBase.equals(nicknameInEdit.getText().toString())){
                    viewModel.changeUserData(selectedUser, "nick", nicknameInEdit.getText().toString().trim());
                }

                if(!emailFromBase.equals(emailInEdit.getText().toString())){
                    Toast.makeText(this, "You can't change email there", Toast.LENGTH_SHORT).show();
                }
                else{
                    Toast.makeText(this, "Data has been updated", Toast.LENGTH_LONG).show();
                }

                showUserData();

            }else{
                Toast.makeText(this, "Data is same and cant be updated", Toast.LENGTH_LONG).show();
            }
        }
    }

    private boolean isTelephoneChanged() {
        return !telephone.equals(telephoneInEdit.getText().toString());
    }

    private boolean isNickNameCahnged() {
        return !nickFromBase.equals(nicknameInEdit.getText().toString());
    }

    private boolean isEmailChanged() {
        return !emailFromBase.equals(emailInEdit.getText().toString());
    }

    private boolean isNameChanged() {
        return !nameFromBase.equals(nameInEdit.getText().toString());
    }

    //todo: trans btn
    public void GoToSettingsFromProfile(View view) {
        Intent GoToSettingsFromProfile = new Intent(User_Profile_Activity.this, Settings_Activity.class);
        startActivity(GoToSettingsFromProfile);
    }

    public void singOutFromUserProfile(View view) {
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
        builder.setMessage("Sing Out?");
        //delete button
        builder.setPositiveButton("exit", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                FirebaseAuth.getInstance().signOut();    // Выходим из аккаунта
                updateUserStatus("offline");
                startActivity(new Intent(User_Profile_Activity.this, Sign_In_Activity.class));
            }
        });
        builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.create().show();
    }

    //todo: show avatar in show_image
    private void showBigAvatarAction(){
        upUserAvatar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!avatarUrlFromBase.equals("")){
                    Intent intent = new Intent(getApplicationContext(), Show_Image_Activity.class);
                    intent.putExtra("imageURL", avatarUrlFromBase);

                    Pair<View, String> pair1 = Pair.create(findViewById(R.id.profileScreenAvatar), "avatar");

                    ActivityOptions activityOptions = null;
                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
                        activityOptions = ActivityOptions.makeSceneTransitionAnimation(User_Profile_Activity.this, pair1);
                        startActivity(intent, activityOptions.toBundle());
                    }
                    else{
                        startActivity(intent);
                    }
                }

            }
        });
    }

    //todo: load avatar and background
    public void AddBackGround(View view) {
        if(checkCurrentPermission()){
            int camera = App_Constants.IMAGE_PICK_BACKGROUND_CAMERA_PROFILE;
            int gallery = App_Constants.IMAGE_PICK_BACKGROUND_GALLERY_PROFILE;
            showImagePickDialogForAvatar(camera, gallery);
        }
    }

    public void AddAvatar(View view) {
        if(checkCurrentPermission()){
            int camera = App_Constants.IMAGE_PICK_CAMERA_CODE_PROFILE;
            int gallery = App_Constants.IMAGE_PICK_GALLERY_CODE_PROFILE;
            showImagePickDialogForAvatar(camera, gallery);
        }
    }

    private boolean checkCurrentPermission() {
        if(Check_Permission_Service.checkPermission(this, User_Profile_Activity.this, Manifest.permission.CAMERA, App_Constants.CAMERA_REQUEST_CODE_PROFILE)
                && Check_Permission_Service.checkPermission(this,User_Profile_Activity.this, Manifest.permission.READ_EXTERNAL_STORAGE, App_Constants.READING_REQUEST_CODE_PROFILE)
                && Check_Permission_Service.checkPermission(this,User_Profile_Activity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE, App_Constants.WRITE_REQUEST_CODE_PROFILE)){
            return true;
        }
        return false;
    }

    private void showImagePickDialogForAvatar(int camera, int gallery) {
        String[] options = {"Camera","Gallery"};
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Choose image from");
        builder.setItems(options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if(which==0){
                    pickFromCamera(camera);
                }
                if(which==1){
                    pickFromGallery(gallery);
                }
            }
        });
        builder.create().show();
    }

    private void pickFromGallery(int requestCodeGallery) {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, requestCodeGallery);
    }

    private void pickFromCamera(int requestCodeCamera) {
        ContentValues cv= new ContentValues();
        cv.put(MediaStore.Images.Media.TITLE, "Temp Pick");
        cv.put(MediaStore.Images.Media.DESCRIPTION, "Temp Descr");
        image_rui = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, cv);

        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, image_rui);
        startActivityForResult(intent, requestCodeCamera);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Uri selectedImageUri;
        //todo: avatar part
        if (resultCode == RESULT_OK) {
            if (requestCode == App_Constants.IMAGE_PICK_GALLERY_CODE_PROFILE || requestCode == App_Constants.IMAGE_PICK_CAMERA_CODE_PROFILE) {

                progressDialog.setMessage("Sending avatar...");
                progressDialog.show();

                if (requestCode == App_Constants.IMAGE_PICK_CAMERA_CODE_PROFILE) {
                    selectedImageUri = image_rui;
                } else {
                    selectedImageUri = data.getData();
                }

                //todo: Compress image
                Bitmap bitmap = null;
                try {
                    bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), selectedImageUri);
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 50, baos );
                    dataCompr = baos.toByteArray();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                StorageReference userAvatarStorageReference = FirebaseStorage.getInstance().getReference().child("users_images");
                final StorageReference imageReference = userAvatarStorageReference.child("image " + selectedImageUri.getLastPathSegment()).child(selectedImageUri.getLastPathSegment());

                // todo: load on server
                UploadTask uploadTask = imageReference.putFile(selectedImageUri);
                uploadTask = imageReference.putBytes(dataCompr);

                Task<Uri> urlTask = uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                    @Override
                    public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                        if (!task.isSuccessful()) {
                            progressDialog.dismiss();
                            throw task.getException();
                        }
                        //todo: Continue with the task to get the download URL
                        progressDialog.dismiss();
                        return imageReference.getDownloadUrl();
                    }
                }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                    @Override
                    public void onComplete(@NonNull Task<Uri> task) {
                        if (task.isSuccessful()) {
                            Uri downloadUri = task.getResult();
                            viewModel.updateCurrentAvatarOrBackground(keyUser, "avatarMockUpResourse", downloadUri.toString());
                        } else {
                            Toast.makeText(User_Profile_Activity.this, "Loading failed", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        }
        //todo: background part
        if (resultCode == RESULT_OK) {
            if (requestCode == App_Constants.IMAGE_PICK_BACKGROUND_CAMERA_PROFILE || requestCode == App_Constants.IMAGE_PICK_BACKGROUND_GALLERY_PROFILE ) {

                progressDialog.setMessage("Sending background...");
                progressDialog.show();

                if (requestCode == App_Constants.IMAGE_PICK_BACKGROUND_CAMERA_PROFILE) {
                    selectedImageUri = image_rui;
                } else {
                    selectedImageUri = data.getData();
                }

                //todo: Compress image
                Bitmap bitmap = null;
                try {
                    bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), selectedImageUri);
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 50, baos );
                    dataCompr = baos.toByteArray();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                StorageReference userBackgroudStorageReference = FirebaseStorage.getInstance().getReference().child("users_backgrounds");
                final StorageReference imageBackReference = userBackgroudStorageReference.child("image " + selectedImageUri.getLastPathSegment()).child(selectedImageUri.getLastPathSegment());

                //todo: upload on server
                UploadTask uploadTask = imageBackReference.putFile(selectedImageUri);
                uploadTask = imageBackReference.putBytes(dataCompr);

                Task<Uri> urlTask = uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                    @Override
                    public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                        if (!task.isSuccessful()) {
                            progressDialog.dismiss();
                            throw task.getException();
                        }
                        //todo: Continue with the task to get the download URL
                        progressDialog.dismiss();
                        return imageBackReference.getDownloadUrl();
                    }
                }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                    @Override
                    public void onComplete(@NonNull Task<Uri> task) {
                        if (task.isSuccessful()) {
                            Uri downloadUri = task.getResult();
                            viewModel.updateCurrentAvatarOrBackground(keyUser, "background", downloadUri.toString());
                        } else {
                            Toast.makeText(User_Profile_Activity.this, "Loading failed", Toast.LENGTH_SHORT).show();
                        }
                    }
                });

            }
        }
    }

    //todo: block online/offline and location
    public void updateUserStatus( String state){
        Online_Offline_Service.updateUserStatus(state, this);
    }

    @Override
    protected void onResume() {
        super.onResume();
       // startLocationUpdate();
        updateUserStatus("online");
    }

    @Override
    protected void onPause() {
        super.onPause();
        updateUserStatus("offline");
        stopLocationUpdates();
    }

    @Override
    protected void onStop() {
        stopLocationUpdates();
        super.onStop();
    }

    //todo: another staff
    private void updateNavigationMenu(){
        if(!isThatCurrentUser){
            bottomNavigationView.setVisibility(View.GONE);
        }else{
            //set Dashboard selected
            bottomNavigationView.setSelectedItemId(R.id.myProfile_nav);
            //PerformItemSelectedListner
            bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
                @Override
                public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                    switch(item.getItemId()){
                        case R.id.dashboard_nav:
                            startActivity(new Intent(getApplicationContext(), Dashboard_Activity.class));
                            overridePendingTransition(0,0);
                            return true;
                        case R.id.userChat_nav:
                            Intent intentChat = new Intent(getApplicationContext(), User_List_Activity.class);
                            intentChat.putExtra("typeOfUserList", "all");
                            startActivity(intentChat);
                            overridePendingTransition(0,0);
                            return true;
                        case R.id.userPosts_nav:
                            startActivity(new Intent(getApplicationContext(), Post_Activity_Friends.class));
                            overridePendingTransition(0,0);
                            return true;
                        case R.id.musicPlayer_nav:
                            startActivity(new Intent(getApplicationContext(), Music_List_Activity.class));
                            overridePendingTransition(0,0);
                            return true;
                        case R.id.myProfile_nav:
                            startActivity(new Intent(getApplicationContext(), User_Profile_Activity.class));
                            overridePendingTransition(0,0);
                            return true;
                    }
                    return false;
                }
            });
        }
    }

    //todo: function Btn
    private void changePasswordBtn(){
        changePassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(isThatCurrentUser){
                    ChangePassword();
            }
            }
        });
    }

    private void ChangePassword() {
        final String email = user.getEmail();

        //todo: dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Change Password");

        RelativeLayout relativeLayout = new RelativeLayout(this);

        EditText prevPass = new EditText(this);
        prevPass.setInputType(InputType.TYPE_TEXT_VARIATION_PASSWORD);
        prevPass.setId(View.generateViewId());
        prevPass.setHint("  Previous password                                                     ");
        relativeLayout.addView(prevPass);

        EditText newPass = new EditText(this);
        newPass.setHint(" New password                                                       ");
        newPass.setInputType(InputType.TYPE_TEXT_VARIATION_PASSWORD);

        RelativeLayout.LayoutParams newPassLayoutParams = new  RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.WRAP_CONTENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT);

        //todo: Also, we want tv2 to appear below tv1, so we are adding rule to tv2LayoutParams.
        newPassLayoutParams.addRule(RelativeLayout.BELOW, prevPass.getId());

        relativeLayout.addView(newPass,newPassLayoutParams);
        relativeLayout.setPadding(50,10,50,10);

        builder.setView(relativeLayout);

        builder.setPositiveButton("Change", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String oldpass = prevPass.getText().toString().trim();
                AuthCredential credential = EmailAuthProvider.getCredential(email,oldpass);

                user.reauthenticate(credential).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful()){
                            String newPassword = newPass.getText().toString().trim();

                            viewModel.updatePassword(user, newPassword);
                            viewModel.getCheckChangePassword().observe(User_Profile_Activity.this, new Observer<String>() {
                                @Override
                                public void onChanged(String s) {
                                    Toast.makeText(User_Profile_Activity.this, s, Toast.LENGTH_LONG).show();
                                }
                            });
                        }else {
                            Toast.makeText(User_Profile_Activity.this, "Wrong previous Password", Toast.LENGTH_LONG).show();
                        }
                    }
                });
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.create().show();
    }

    private void createPathLocationBtn(){
        buildLocationBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent location = new Intent(User_Profile_Activity.this, Users_FInd_Location.class);
                location.putExtra("selectedUser", selectedUser );
                startActivity(location);
            }
        });
    }

    //todo: refresh
    public  void initRefreshLayout() {

        swipeRefreshLayout.setColorSchemeResources(R.color.purple_500);

        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                Intent intent = getIntent();
                finish();
                overridePendingTransition(0, 0);
                startActivity(intent);
                swipeRefreshLayout.setRefreshing(false);
            }
        });
    }


    //todo: update users location in back (1 - 10 sec) check power?
    private void updateLocation(){
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        settingsClient = LocationServices.getSettingsClient(this);

        buildLocationRequest();

        buildLocationCallback();

        buildLocationSettingsRequest();

        startLocationUpdate();
    }

    private void buildLocationRequest() {
        locationRequest = LocationRequest.create().setInterval(10000).setFastestInterval(6000).setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    private void buildLocationCallback() {

        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(@NonNull LocationResult locationResult) {
                super.onLocationResult(locationResult);
                currentLocation = locationResult.getLastLocation();
                updateLocationUi();
            }
        };
    }

    private void buildLocationSettingsRequest() {
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder();
        builder.addLocationRequest(locationRequest);
        locationSettingsRequest = builder.build();

    }

    private void startLocationUpdate() {

        if(checkPermForTakeLocation()){
            isLocationUpdateActive = true;

            settingsClient.checkLocationSettings(locationSettingsRequest).addOnSuccessListener(this, new OnSuccessListener<LocationSettingsResponse>() {
                @Override
                public void onSuccess(LocationSettingsResponse locationSettingsResponse) {
                    if (ActivityCompat.checkSelfPermission(User_Profile_Activity.this,
                            Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission
                            (User_Profile_Activity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                        return;
                    }
                    fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.myLooper());
                    updateLocationUi();
                }
            });
        }

    }

    private void stopLocationUpdates() {

        if(!isLocationUpdateActive){
            return;
        }

        fusedLocationClient.removeLocationUpdates(locationCallback).addOnCompleteListener(this,new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                isLocationUpdateActive = false;
            }
        });

    }

    private boolean checkPermForTakeLocation(){
        if(ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){
            return true;
        }
        else{
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, App_Constants.REQUESTCODE_LOCATION_FIRST);
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION},App_Constants.REQUESTCODE_LOCATION_LAST);
            return true;
        }
    }

    private void updateLocationUi() {
        if(currentLocation != null){
            DatabaseReference userDataLocation = FirebaseDatabase.getInstance().getReference().child("Users_Locations");
            GeoFire geoFire = new GeoFire(userDataLocation);
            geoFire.setLocation(selectedUser, new GeoLocation(currentLocation.getLatitude(), currentLocation.getLongitude()));
        }
    }

    //todo: additional staff
    private void isNetworkConnected() {
        viewModel.checkInternet(this);
        viewModel.getIsInternetGO().observe(this, new Observer<String>() {
            @Override
            public void onChanged(String s) {
                if(s.equals("false")){
                    showSnackBar("Internet disable", null, null);
                }
            }
        });
    }

    private void showSnackBar(final String mainText, final String action, View.OnClickListener listener) {
        Snackbar.make(findViewById(android.R.id.content), mainText, Snackbar.LENGTH_INDEFINITE).setAction(action, listener).show();
    }

}

