package com.example.myapplication.main.Screens.Posts.Posts_By_Friends_MVP;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.SearchView;

import com.example.myapplication.Common_Dagger_App_Class.App;
import com.example.myapplication.Services.Check_Internet_Connection_Exist;
import com.example.myapplication.Services.Online_Offline_User_Service_To_Firebase;
import com.example.myapplication.databinding.PostActivityFriendsBinding;
import com.example.myapplication.main.Screens.Dashboard_MVP.Dashboard_Activity;
import com.example.myapplication.R;
import com.example.myapplication.main.Screens.Posts.Add_Change_Post_MVP.Add_Change_Post_Activity;
import com.example.myapplication.main.Models.Model_Post;

import com.example.myapplication.main.Screens.Posts.Posts_By_Recommendation_MVVM.Post_Activity_Recommendations;
import com.example.myapplication.main.Screens.User_List_4_States_MVVM.User_List_Activity;
import com.example.myapplication.main.Screens.User_Profile_MVVM.User_Profile_Activity;
import com.example.myapplication.main.Screens.Music.Music_List_Activity_MVVM.Music_List_Activity;
import com.google.android.material.navigation.NavigationBarView;
import com.google.android.material.snackbar.Snackbar;

import org.jetbrains.annotations.NotNull;
import java.util.ArrayList;
import java.util.Objects;

import javax.inject.Inject;

import static android.content.Intent.FLAG_ACTIVITY_NO_ANIMATION;

//TODO: created by Anton Sushchevich MVP format

public class Post_Activity_Friends extends AppCompatActivity implements Post_List_view{

    @Inject
    Post_Adapter_Friends postAdapter;

    @Inject
    LinearLayoutManager layoutManager;

    @Inject
    Check_Internet_Connection_Exist checkInternetService;

    @Inject
    Online_Offline_User_Service_To_Firebase controller;

    //TODO: create presenter
    private Post_Friends_Presenter presenter;

    private PostActivityFriendsBinding postBinding;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.post_activity_friends);

        Objects.requireNonNull(getSupportActionBar()).hide();

        initialization();

        updateNavigationMenu();

        startAnimation();

        checkNetworkState();

        initSearchUser();

        createRecyclerView();

        loadPosts();

        initRefreshLayout();

        initRecommendBtn();
    }


    //TODO: main
    private void initialization() {

        ((App) getApplication()).getPostsFriendComponent().inject(this);

        presenter = new Post_Friends_Presenter(this);

        postBinding = DataBindingUtil.setContentView(this, R.layout.post_activity_friends);

        //TODO: lifecycle
        getLifecycle().addObserver(controller);
    }

    private void startAnimation() {
        postBinding.postFriendsTextBtn.animate().scaleX(1.14f).scaleY(1.14f).setDuration(300);
        postBinding.postRecommendationsTextBtn.animate().scaleX(1).scaleY(1).setDuration(200);
    }

    private void checkNetworkState() {
        if(!checkInternetService.checkInternet(this)){
            showSnackBar("Internet disable", null, null);
        }
    }

    private void createRecyclerView() {
       // layoutManager = new LinearLayoutManager(this);
        postBinding.postRecyclerViewFrinds.setLayoutManager(layoutManager);

       // postAdapter = new Post_Adapter_Friends(this);
        postAdapter.setPostList(new ArrayList<Model_Post>());
        postBinding.postRecyclerViewFrinds.setAdapter(postAdapter);

        //TODO: перелистывает как VIEWPAGER + тонко настроить растояние и анимацию
       // SnapHelper snapHelper = new LinearSnapHelper();
       // snapHelper.attachToRecyclerView(recyclerView);

        createScrollListener();
    }

    private void createScrollListener() {

        postBinding.postRecyclerViewFrinds.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NotNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                if (!recyclerView.canScrollVertically(1) && newState == RecyclerView.SCROLL_STATE_IDLE) {

                }

                if (!recyclerView.canScrollVertically(-1) && newState == RecyclerView.SCROLL_STATE_IDLE)  {
                    //reached top
                    if(layoutManager.findFirstCompletelyVisibleItemPosition() == 0){
                        // Its at top
                        postBinding.swipeLayFriendsPost.setEnabled(true);
                    }

                }
                if(newState == RecyclerView.SCROLL_STATE_DRAGGING){
                    //scrolling
                    postBinding.actionSearch.animate().alpha(0.0f).translationY(-200).setDuration(500).setListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            super.onAnimationEnd(animation);
                            postBinding.actionSearch.setVisibility(View.GONE);
                        }
                    });

                    postBinding.swipeLayFriendsPost.setEnabled(false);
                }
            }
        });

    }

    private void initSearchUser() {
        postBinding.actionSearch.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                //call when user press
                if (!TextUtils.isEmpty(query)) {
                    searchPosts(query);
                } else {
                    loadPosts();
                }
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if (!TextUtils.isEmpty(newText)) {
                    searchPosts(newText);
                } else {
                    loadPosts();
                }
                return false;
            }
        });
    }

    private void loadPosts() {
         presenter.loadData("no");
    }

    private void searchPosts(String searchQuery) {
        presenter.loadData(searchQuery);
    }

    public  void initRefreshLayout() {

        postBinding.swipeLayFriendsPost.setColorSchemeResources(R.color.purple_500);
        postBinding.swipeLayFriendsPost.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                Intent intent = getIntent();
                finish();
                overridePendingTransition(0, 0);
                startActivity(intent);
                postBinding.swipeLayFriendsPost.setRefreshing(false);
            }
        });
    }

    //TODO: part of come to "Recommendation"
    private void initRecommendBtn() {
        postBinding.postRecommendationsTextBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startRecActivity();
            }
        });
    }

    private void startRecActivity() {
        Intent intent = new Intent(Post_Activity_Friends.this, Post_Activity_Recommendations.class);
        intent.setFlags(FLAG_ACTIVITY_NO_ANIMATION);
        startActivity(intent);
    }

    @Override
    public void showData(ArrayList<Model_Post> posts) {
        postAdapter.setPostList(posts);
    }

    public void startActAddPost(View view) {
        startActivity(new Intent(Post_Activity_Friends.this, Add_Change_Post_Activity.class));
    }

    @Override
    public void initShowNewPost() {
        postBinding.showNewPostBtn.setVisibility(View.VISIBLE);
        postBinding.showNewPostBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadPosts();
            }
        });
    }

    @Override
    public void disableProgressBar() {
        postBinding.progressbarPostActivity.setVisibility(View.GONE);
    }

    //TODO: another staff
    public void updateNavigationMenu() {
        //set Dashboard selected
        postBinding.bottomNavigation.setSelectedItemId(R.id.userPosts_nav);
        //PerformItemSelectedListner
        postBinding.bottomNavigation.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @SuppressLint("NonConstantResourceId")
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.dashboard_nav:
                        startActivity(new Intent(getApplicationContext(), Dashboard_Activity.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
                        overridePendingTransition(0, 0);
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
                        overridePendingTransition(0, 0);
                        return true;
                    case R.id.musicPlayer_nav:
                        startActivity(new Intent(getApplicationContext(), Music_List_Activity.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
                        overridePendingTransition(0, 0);
                        return true;
                    case R.id.myProfile_nav:
                        startActivity(new Intent(getApplicationContext(), User_Profile_Activity.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
                        overridePendingTransition(0, 0);
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
