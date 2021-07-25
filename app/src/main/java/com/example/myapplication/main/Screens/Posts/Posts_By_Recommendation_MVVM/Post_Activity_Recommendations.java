package com.example.myapplication.main.Screens.Posts.Posts_By_Recommendation_MVVM;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.SearchView;
import android.widget.TextView;

import com.example.myapplication.main.Screens.Dashboard_MVP.Dashboard_Activity;
import com.example.myapplication.R;
import com.example.myapplication.Services.Online_Offline_Service;
import com.example.myapplication.main.Models.Model_Post;
import com.example.myapplication.main.Screens.Posts.Posts_By_Friends_MVP.Post_Activity_Friends;
import com.example.myapplication.main.Screens.User_List_4_States_MVVM.User_List_Activity;
import com.example.myapplication.main.Screens.User_Profile_MVVM.User_Profile_Activity;
import com.example.myapplication.main.Screens.Music.Music_List_Activity_MVVM.Music_List_Activity;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Objects;

import static android.content.Intent.FLAG_ACTIVITY_NO_ANIMATION;

//TODO: created by Anton Sushchevich MVVM format

public class Post_Activity_Recommendations extends AppCompatActivity {

    private RecyclerView recyclerView;
    private Post_Adapter_Recommendations postAdapter;
    private GridLayoutManager layoutManager;

    private ProgressBar progressBar;
    private SearchView searchView;

    private TextView fromFriendsTextBtn;
    private TextView recommendationTextBtn;

    //TODO: create viewModel
    private Post_Recomm_ViewModel viewModel;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.post_activity_recommendations);

        updateNavigationMenu();

        Objects.requireNonNull(getSupportActionBar()).hide();

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        viewModel = new ViewModelProvider(this).get(Post_Recomm_ViewModel.class);

        initialization();

        startAnimation();

        initSearchUser();

        createRecyclerViewAndFirstLoadData();

        initFriendsBtn();

        updateUserStatus("online");
    }

    private void initialization() {
        searchView = findViewById(R.id.action_search_rec);
        fromFriendsTextBtn = findViewById(R.id.postRecToFriendsTextBtn);
        recommendationTextBtn = findViewById(R.id.postRecommendationsTextBtn);
        progressBar = (ProgressBar) findViewById(R.id.progressbarPostRecActivity);
    }

    private void startAnimation() {
        recommendationTextBtn.animate().scaleX(1.14f).scaleY(1.14f).setDuration(300);
        fromFriendsTextBtn.animate().scaleX(1).scaleY(1).setDuration(200);
    }

    private void createRecyclerViewAndFirstLoadData() {
        //TODO: part of recyclerview
        recyclerView = findViewById(R.id.postRecRecyclerView);
        layoutManager = new GridLayoutManager(this,3);
        recyclerView.setLayoutManager(layoutManager);

        postAdapter = new Post_Adapter_Recommendations(this);
        postAdapter.setPostListRec(new ArrayList<Model_Post>());
        recyclerView.setAdapter(postAdapter);

        loadPosts();

        viewModel.getMutPostListFriends().observe(this, new Observer<ArrayList<Model_Post>>() {
            @Override
            public void onChanged(ArrayList<Model_Post> posts) {
                postAdapter.setPostListRec(posts);
                progressBar.setVisibility(View.GONE);
            }
        });

        createScrollListener();
    }

    private void createScrollListener() {

        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NotNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                if (!recyclerView.canScrollVertically(1) && newState == RecyclerView.SCROLL_STATE_IDLE) {

                }

                if (!recyclerView.canScrollVertically(-1) && newState == RecyclerView.SCROLL_STATE_IDLE)  {
                    //reached top
                    if(layoutManager.findFirstCompletelyVisibleItemPosition() == 0){
                        // Its at top

                    }

                }
                if(newState == RecyclerView.SCROLL_STATE_DRAGGING){
                    //scrolling
                    searchView.animate().alpha(0.0f).translationY(-200).setDuration(500).setListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            super.onAnimationEnd(animation);
                            searchView.setVisibility(View.GONE);
                        }
                    });

                }
            }
        });



    }

    private void initSearchUser() {
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
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
        fromFriendsTextBtn.setOnClickListener(new View.OnClickListener() {
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
        //initialization navigation button
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigation);
        //set Dashboard selected
        bottomNavigationView.setSelectedItemId(R.id.userPosts_nav);
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

    @Override
    public void onBackPressed() {
        finish();
    }

}