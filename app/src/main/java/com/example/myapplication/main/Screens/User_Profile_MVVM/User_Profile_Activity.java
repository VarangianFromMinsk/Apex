package com.example.myapplication.main.Screens.User_Profile_MVVM;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;

import com.example.myapplication.Common_Dagger_App_Class.App;
import com.example.myapplication.Services.Check_Internet_Connection_Exist;
import com.example.myapplication.Services.Online_Offline_User_Service_To_Firebase;
import com.example.myapplication.Services.Start_Check_GeoPosition_Service;
import com.example.myapplication.databinding.UserProfileActivityBinding;
import com.example.myapplication.main.Screens.Dashboard_MVP.Dashboard_Activity;
import com.example.myapplication.R;
import com.example.myapplication.main.Screens.Posts.Posts_By_Friends_MVVM.Post_Activity_Friends;
import com.example.myapplication.main.Screens.User_List_4_States_MVVM.User_List_Activity;
import com.example.myapplication.main.Screens.Music.Music_List_Activity_MVVM.Music_List_Activity;
import com.example.myapplication.main.Screens.User_Profile_MVVM.Fragments.Content_Control_Fragment;
import com.example.myapplication.main.Screens.User_Profile_MVVM.Fragments.User_Info_Fragment;
import com.example.myapplication.main.Screens.User_Profile_MVVM.Fragments.User_Music_Fragment;
import com.example.myapplication.main.Screens.User_Profile_MVVM.Fragments.User_Posts_Fragment;
import com.example.myapplication.main.Screens.User_Profile_MVVM.Fragments.User_UperBlock_Info_Fragment;

import com.example.myapplication.main.Screens.User_Profile_MVVM.Photo_From_Gallery.ItemListDialogFragment;
import com.example.myapplication.main.Screens.User_Profile_MVVM.Photo_From_Gallery.ActionFromGallery;
import com.google.android.material.navigation.NavigationBarView;
import com.google.android.material.snackbar.Snackbar;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;
import java.util.Objects;

import javax.inject.Inject;

public class User_Profile_Activity extends AppCompatActivity implements User_Info_Fragment.checkInterface,
        Content_Control_Fragment.contentController,
        User_Music_Fragment.musicFragmentWasCommitted,
        User_Posts_Fragment.PostsFragmentWasCommitted,
        User_UperBlock_Info_Fragment.showGalleryPhoto,
        ActionFromGallery,
        ItemListDialogFragment.loadImageFromGallerySelecter {


    @Inject
    Check_Internet_Connection_Exist checkInternetService;

    @Inject
    Online_Offline_User_Service_To_Firebase controller;

    private String selectedUser;
    private boolean isThatCurrentUser;
    private FirebaseUser user;

    private final Handler handler = new Handler();

    private ProgressDialog progressDialog;

    private Profile_ViewModel viewModel;
    private UserProfileActivityBinding binding;
    private Start_Check_GeoPosition_Service geoService;

    private ItemListDialogFragment itemListDialogFragment;

    private Fragment fragmentUperBlockUserInfo, fragmentUserInfo, fragmentController, fragmentUserPosts, fragmentUserMusic;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.user_profile_activity);

        Objects.requireNonNull(getSupportActionBar()).hide();

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        initialization();

        checkIsThatCurrentUserOrNot();

        createFragments(savedInstanceState);

        loadUserInfo();

        checkNetworkState();

        updateNavigationMenu();

        startUpdateGeoPosition();
    }

    //todo: main methods
    private void initialization() {
        user = FirebaseAuth.getInstance().getCurrentUser();

        ((App) getApplication()).getCommonComponent().inject(this);

        geoService = new Start_Check_GeoPosition_Service(this, this);

        binding = DataBindingUtil.setContentView(this, R.layout.user_profile_activity);

        viewModel = new ViewModelProvider(this).get(Profile_ViewModel.class);

        //TODO: lifecycle
        getLifecycle().addObserver(controller);

        progressDialog = new ProgressDialog(this);
    }

    private void checkIsThatCurrentUserOrNot() {
        Intent intent = getIntent();
        Bundle extras = intent.getExtras();
        if (extras != null) {
            //todo: about user
            selectedUser = intent.getStringExtra("hisId");
            isThatCurrentUser = false;
        } else {
            selectedUser = user.getUid();
            isThatCurrentUser = true;
        }
    }

    private void createFragments(Bundle savedInstanceState) {
        if (savedInstanceState == null) {
            fragmentUperBlockUserInfo = User_UperBlock_Info_Fragment.newInstance(selectedUser, isThatCurrentUser);
            fragmentController = Content_Control_Fragment.newInstance(selectedUser, isThatCurrentUser);
            fragmentUserInfo = User_Info_Fragment.newInstance(selectedUser, isThatCurrentUser);
            fragmentUserPosts = User_Posts_Fragment.newInstance(selectedUser);
            fragmentUserMusic = User_Music_Fragment.newInstance(selectedUser);
        }

        getSupportFragmentManager().beginTransaction()
                .add(R.id.upBlockUserInfoFragment, fragmentUperBlockUserInfo)
                .add(R.id.layFragmentCommonInfoAboutUser, fragmentUserInfo)
                .add(R.id.controllerFragmentView,fragmentController)
                .commit();
    }

    //TODO: test wo all wrok
    @Override
    public void checkingUserInfoFragmentFinished() {
        Log.d("UserProfile", "User_Info_Fragments_Works");
    }

    @Override
    public void musicFragmentWorks() {
        Log.d("UserProfile", "Music_Fragments_Works");
    }

    @Override
    public void PostsFragmentWorks() {
        Log.d("UserProfile", "Post_Fragments_Works");
    }


    @Override
    public void mainSwitchContent(boolean isPost, boolean isMusic) {
        Runnable runnable = new Runnable(){
            public void run() {
                if (isPost) {

                    loadPosts();

                    getSupportFragmentManager().beginTransaction()
                            .replace(R.id.layFragmentCommonInfoAboutUser, fragmentUserPosts)
                            .addToBackStack("Posts")
                            .commit();

                    binding.scrollViewProfile.transitionToEnd();
                    binding.backProfileAll.setBackgroundColor(getResources().getColor(R.color.profileNormal));

                } else if (isMusic) {

                    loadSongs();

                    getSupportFragmentManager().beginTransaction()
                            .replace(R.id.layFragmentCommonInfoAboutUser, fragmentUserMusic)
                            .addToBackStack("Music")
                            .commit();

                    binding.scrollViewProfile.transitionToEnd();
                    binding.backProfileAll.setBackgroundColor(getResources().getColor(R.color.profileGrey));

                } else {

                    getSupportFragmentManager().popBackStack();
                    getSupportFragmentManager().beginTransaction()
                            .replace(R.id.layFragmentCommonInfoAboutUser, fragmentUserInfo)
                            .commit();

                    binding.scrollViewProfile.transitionToStart();
                    binding.backProfileAll.setBackgroundColor(getResources().getColor(R.color.profileNormal));
                }
            }
        };

        int interval = 300;
        handler.postAtTime(runnable, System.currentTimeMillis()+ interval);
        handler.postDelayed(runnable, interval);

    }


    private void loadUserInfo(){
        viewModel.loadUserInfo(selectedUser);
    }

    private void loadPosts() {
        viewModel.loadPosts(selectedUser);
    }

    private void loadSongs() {
        viewModel.loadMusic(selectedUser);
    }

    @Override
    public void hideBottomNavigation() {

        binding.bottomNavigation.animate().alpha(0.0f).translationY(150).setDuration(250).setListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                binding.bottomNavigation.setVisibility(View.GONE);
            }
        });
    }

    @Override
    public void showBottomNavigation() {

        binding.bottomNavigation.setVisibility(View.VISIBLE);
        binding.bottomNavigation.animate().alpha(1).translationY(0).setDuration(500).setListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                binding.bottomNavigation.setVisibility(View.VISIBLE);
            }
        });
    }


    //todo: another staff
    @Override
    protected void onResume() {
        super.onResume();
        //geoService.startLocationUpdate();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if(isThatCurrentUser){
            geoService.stopLocationUpdates();
        }
    }

    private void updateNavigationMenu() {
        if (!isThatCurrentUser) {
            binding.bottomNavigation.setVisibility(View.GONE);
        } else {
            binding.bottomNavigation.setSelectedItemId(R.id.myProfile_nav);
            binding.bottomNavigation.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
                @SuppressLint("NonConstantResourceId")
                @Override
                public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                    switch (item.getItemId()) {
                        case R.id.dashboard_nav:
                            startActivity(new Intent(getApplicationContext(), Dashboard_Activity.class));
                            overridePendingTransition(0, 0);
                            return true;
                        case R.id.userChat_nav:
                            Intent intentChat = new Intent(getApplicationContext(), User_List_Activity.class);
                            intentChat.putExtra("typeOfUserList", "all");
                            startActivity(intentChat);
                            overridePendingTransition(0, 0);
                            return true;
                        case R.id.userPosts_nav:
                            startActivity(new Intent(getApplicationContext(), Post_Activity_Friends.class));
                            overridePendingTransition(0, 0);
                            return true;
                        case R.id.musicPlayer_nav:
                            startActivity(new Intent(getApplicationContext(), Music_List_Activity.class));
                            overridePendingTransition(0, 0);
                            return true;
                        case R.id.myProfile_nav:
                            startActivity(new Intent(getApplicationContext(), User_Profile_Activity.class));
                            overridePendingTransition(0, 0);
                            return true;
                    }
                    return true;
                }
            });
        }
    }

    private void startUpdateGeoPosition() {
        if(isThatCurrentUser){
            geoService.setSelectedUser(selectedUser);
            geoService.updateLocation();
        }
    }

    private void checkNetworkState() {
        if(!checkInternetService.checkInternet(this)){
            showSnackBar("Internet disable", null, null);
        }
    }

    private void showSnackBar(final String mainText, final String action, View.OnClickListener listener) {
        Snackbar.make(findViewById(android.R.id.content), mainText, Snackbar.LENGTH_INDEFINITE).setAction(action, listener).show();
    }

    @Override
    public void updateProfileAfterChangeAvatarOrBackground() {
        viewModel.loadUserInfo(selectedUser);
        itemListDialogFragment.dismiss();
    }

    @Override
    public void showGalleryPhotoInterface(String avatarOrBackground) {
        itemListDialogFragment = ItemListDialogFragment.newInstance(false);
        itemListDialogFragment.show(getSupportFragmentManager(), "dialog");
    }

    @Override
    public void hideAction() {
        itemListDialogFragment.dismiss();
    }

    @Override
    public void loadImage(ArrayList<String> images) {
        itemListDialogFragment.dismiss();
        Log.d("image", String.valueOf(images.size()));
        viewModel.shareImages(images);
        progressDialog.setMessage("Sending ...");
        progressDialog.show();
    }

    @Override
    public void disableProgressBar() {
        progressDialog.dismiss();
    }
}

