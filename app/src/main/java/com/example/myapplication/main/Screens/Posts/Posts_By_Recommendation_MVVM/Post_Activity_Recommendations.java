package com.example.myapplication.main.Screens.Posts.Posts_By_Recommendation_MVVM;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.SearchView;
import android.widget.TextView;

import com.example.myapplication.databinding.PostActivityRecommendationsBinding;
import com.example.myapplication.main.Screens.Dashboard_MVP.Dashboard_Activity;
import com.example.myapplication.R;
import com.example.myapplication.Services.Online_Offline_Service;
import com.example.myapplication.main.Models.Model_Post;
import com.example.myapplication.main.Screens.Posts.Posts_By_Friends_MVP.Post_Activity_Friends;
import com.example.myapplication.main.Screens.Posts.Posts_By_Recommendation_MVVM.di.App;
import com.example.myapplication.main.Screens.User_List_4_States_MVVM.User_List_Activity;
import com.example.myapplication.main.Screens.User_Profile_MVVM.User_Profile_Activity;
import com.example.myapplication.main.Screens.Music.Music_List_Activity_MVVM.Music_List_Activity;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;

import org.jetbrains.annotations.NotNull;

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

    //TODO: create dataBinding
    private PostActivityRecommendationsBinding binding;

    //TODO: create viewModel
    Post_Recomm_ViewModel viewModel;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.post_activity_recommendations);

        Objects.requireNonNull(getSupportActionBar()).hide();

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        initialization();

        updateNavigationMenu();

        startAnimation();

        initSearchUser();

        createRecyclerViewAndFirstLoadData();

        initFriendsBtn();

        updateUserStatus("online");
    }

    private void initialization() {
        binding = DataBindingUtil.setContentView(this, R.layout.post_activity_recommendations);

        //todo: предоставь (inject) все зависимости в этот класс
        ((App) getApplication()).getAppComponent().inject(this);

        viewModel = new ViewModelProvider(this).get(Post_Recomm_ViewModel.class);
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

        loadPosts();

        viewModel.getMutPostListFriends().observe(this, new Observer<ArrayList<Model_Post>>() {
            @Override
            public void onChanged(ArrayList<Model_Post> posts) {
                postAdapter.setPostListRec(posts);
                binding.progressbarPostRecActivity.setVisibility(View.GONE);
            }
        });

        createScrollListener();
    }

    private void createScrollListener() {


        binding.postRecRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NotNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);

                if (!recyclerView.canScrollVertically(1) && newState == RecyclerView.SCROLL_STATE_IDLE) {

                }

                if (!recyclerView.canScrollVertically(-1) && newState == RecyclerView.SCROLL_STATE_DRAGGING)  {
                    if(layoutManager.findFirstVisibleItemPosition()==0){
                       // searchView.setVisibility(View.VISIBLE);
                      //  searchView.animate().alpha(1).translationY(0).setDuration(200).scaleY(1).scaleX(1);
                    }
                }
                if(newState == RecyclerView.SCROLL_STATE_DRAGGING){
                    //scrolling
                    binding.searchRec.animate().alpha(0.0f).translationY(-200).setDuration(400).scaleY(0.7f).scaleX(0.7f).setListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            super.onAnimationEnd(animation);
                            binding.searchRec.setVisibility(View.GONE);
                        }
                    });

                }
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
                        Intent intentChat = new Intent(getApplicationContext(), User_List_Activity.class);
                        intentChat.putExtra("typeOfUserList", "all");
                        intentChat.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
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

    @Override
    public void onBackPressed() {
        //disable
    }

}