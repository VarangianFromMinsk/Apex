package com.example.myapplication.main.Screens.Posts.Posts_By_Recommendation_MVVM;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.SearchView;

import com.example.myapplication.Services.Check_Internet_Connection_Exist;
import com.example.myapplication.Services.Online_Offline_User_Service_To_Firebase;
import com.example.myapplication.databinding.PostActivityRecommendationsBinding;
import com.example.myapplication.main.Screens.Dashboard_MVP.Dashboard_Activity;
import com.example.myapplication.R;
import com.example.myapplication.main.Models.Model_Post;
import com.example.myapplication.main.Screens.Posts.Posts_By_Friends_MVP.Post_Activity_Friends;
import com.example.myapplication.Common_Dagger_App_Class.App;
import com.example.myapplication.main.Screens.User_List_4_States_MVVM.User_List_Activity;
import com.example.myapplication.main.Screens.User_Profile_MVVM.User_Profile_Activity;
import com.example.myapplication.main.Screens.Music.Music_List_Activity_MVVM.Music_List_Activity;
import com.google.android.material.navigation.NavigationBarView;
import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;
import java.util.Objects;

import javax.inject.Inject;

import static android.content.Intent.FLAG_ACTIVITY_NO_ANIMATION;

//TODO: created by Anton Sushchevich MVVM format

public class Post_Activity_Recommendations extends AppCompatActivity {

    @Inject
    GridLayoutManager layoutManager;

    @Inject
    Post_Adapter_Recommendations postAdapter;

    @Inject
    Check_Internet_Connection_Exist checkInternetService;

    @Inject
    Online_Offline_User_Service_To_Firebase controller;


    //TODO: create dataBinding
    private PostActivityRecommendationsBinding binding;

    //TODO: create viewModel
    private Post_Recomm_ViewModel viewModel;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.post_activity_recommendations);

        Objects.requireNonNull(getSupportActionBar()).hide();

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        initialization();

        updateNavigationMenu();

        checkNetworkState();

        startAnimation();

        initSearchUser();

        createRecyclerViewAndFirstLoadData();

        initFriendsBtn();
    }

    //TODO: main
    private void initialization() {
        binding = DataBindingUtil.setContentView(this, R.layout.post_activity_recommendations);

        //todo: предоставь (inject) все зависимости в этот класс
        ((App) getApplication()).getpostsRecComponent().inject(this);

        viewModel = new ViewModelProvider(this).get(Post_Recomm_ViewModel.class);

        //TODO: lifecycle
        getLifecycle().addObserver(controller);
    }

    private void checkNetworkState() {
        if(!checkInternetService.checkInternet(this)){
            showSnackBar("Internet disable", null, null);
        }
    }

    private void startAnimation() {
        binding.postRecommendationsTextBtn.animate().scaleX(1.14f).scaleY(1.14f).setDuration(300);
        binding.postRecToFriendsTextBtn.animate().scaleX(1).scaleY(1).setDuration(200);
    }

    private void createRecyclerViewAndFirstLoadData() {
        // part of recyclerview
       //  recyclerView = findViewById(R.id.postRecRecyclerView);
       // layoutManager = new GridLayoutManager(this,3);

        binding.postRecRecyclerView.setLayoutManager(layoutManager);

      //  postAdapter = new Post_Adapter_Recommendations(this);

        postAdapter.setPostListRec(new ArrayList<Model_Post>());
        binding.postRecRecyclerView.setAdapter(postAdapter);

        viewModel.getShowLoad().observe(this, new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean state) {
                if(state){
                    binding.progressbarPostRecActivity.setVisibility(View.VISIBLE);
                }
                else{
                    binding.progressbarPostRecActivity.setVisibility(View.GONE);
                }
            }
        });

        loadPosts();

        viewModel.getMutPostListFriends().observe(this, new Observer<ArrayList<Model_Post>>() {
            @Override
            public void onChanged(ArrayList<Model_Post> posts) {
                postAdapter.setPostListRec(posts);
            }
        });

    }

    private void initSearchUser() {
        binding.searchRec.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                //call when user press
                if(!TextUtils.isEmpty(query)){
                    searchPosts(query);
                }
                else{
                    loadPosts();
                }
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if(!TextUtils.isEmpty(newText)){
                    searchPosts(newText);
                }
                else{
                    loadPosts();
                }
                return false;
            }
        });
    }

    private void loadPosts() {
        viewModel.loadData("no");
    }

    private void searchPosts(String searchQuery){
        viewModel.loadData(searchQuery);
    }

    //TODO: part of come to "From friends"
    private void initFriendsBtn(){
        binding.postRecToFriendsTextBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startFriendsActivity();
            }
        });
    }

    private void startFriendsActivity(){
        Intent intent = new Intent(Post_Activity_Recommendations.this, Post_Activity_Friends.class);
        intent.setFlags(FLAG_ACTIVITY_NO_ANIMATION);
        startActivity(intent);
    }

    //TODO: another staff
    public void updateNavigationMenu(){
        //set Dashboard selected
        binding.bottomNavigation.setSelectedItemId(R.id.userPosts_nav);
        //PerformItemSelectedListener
        binding.bottomNavigation.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @SuppressLint("NonConstantResourceId")
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch(item.getItemId()){
                    case R.id.dashboard_nav:
                        startActivity(new Intent(getApplicationContext(), Dashboard_Activity.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
                        overridePendingTransition(0,0);
                        return true;

                    case R.id.userChat_nav:
                        Intent intentChat = new Intent(getApplicationContext(), User_List_Activity.class)
                                .putExtra("typeOfUserList", "all")
                                .setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(intentChat);
                        overridePendingTransition(0,0);
                        return true;

                    case R.id.userPosts_nav:
                        startActivity(new Intent(getApplicationContext(), Post_Activity_Friends.class));
                        overridePendingTransition(0,0);
                        return true;

                    case R.id.musicPlayer_nav:
                        startActivity(new Intent(getApplicationContext(), Music_List_Activity.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
                        overridePendingTransition(0,0);
                        return true;

                    case R.id.myProfile_nav:
                        startActivity(new Intent(getApplicationContext(), User_Profile_Activity.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
                        overridePendingTransition(0,0);
                        return true;
                }
                return false;
            }
        });

    }

    private void showSnackBar(final String mainText, final String action, View.OnClickListener listener) {
        Snackbar.make(findViewById(android.R.id.content), mainText, Snackbar.LENGTH_INDEFINITE).setAction(action, listener).show();
    }

    @Override
    public void onBackPressed() {
        //disable
    }


}