package com.example.myapplication.main.Screens.Posts.Posts_By_Friends_MVP;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.LinearSnapHelper;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.SnapHelper;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.SearchView;
import android.widget.TextView;

import com.example.myapplication.main.Screens.Dashboard_MVP.Dashboard_Activity;
import com.example.myapplication.R;
import com.example.myapplication.Services.Online_Offline_Service;
import com.example.myapplication.main.Screens.Posts.Add_Change_Post_MVP.Add_Change_Post_Activity;
import com.example.myapplication.main.Models.Model_Post;

import com.example.myapplication.main.Screens.Posts.Posts_By_Recommendation_MVVM.Post_Activity_Recommendations;
import com.example.myapplication.main.Screens.User_List_4_States_MVVM.User_List_Activity;
import com.example.myapplication.main.Screens.User_Profile_MVVM.User_Profile_Activity;
import com.example.myapplication.main.Screens.Music.Music_List_Activity_MVVM.Music_List_Activity;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.snackbar.Snackbar;

import org.jetbrains.annotations.NotNull;
import java.util.ArrayList;
import java.util.Objects;

import static android.content.Intent.FLAG_ACTIVITY_NO_ANIMATION;

//TODO: created by Anton Sushchevich MVP format

public class Post_Activity_Friends extends AppCompatActivity implements Post_List_view{

    private RecyclerView recyclerView;
    private Post_Adapter_Friends postAdapter;
    private LinearLayoutManager layoutManager;

    private ProgressBar progressBar;
    private SearchView searchView;
    private RelativeLayout showNewPostBtn;

    //refresh
    private SwipeRefreshLayout swipeRefreshLayout;
    private TextView recommendTextBtn;
    private TextView friendsTextBtn;

    //TODO: create presenter
    private Post_Friends_Presenter presenter;

    Observer_On_LifeCycle controller = new Observer_On_LifeCycle();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.post_activity_friends);

        updateNavigationMenu();

        Objects.requireNonNull(getSupportActionBar()).hide();

        presenter = new Post_Friends_Presenter(this);

        //TODO: Android Architeckcher components - lifecycle
        getLifecycle().addObserver(controller);

        initialization();

        startAnimation();

        initSearchUser();

        createRecyclerView();

        loadPosts();

        initRefreshLayout();

        initRecommendBtn();

        updateUserStatus("online");
    }


    //TODO: main
    private void initialization() {
        swipeRefreshLayout = findViewById(R.id.swipeLayFriendsPost);
        searchView = findViewById(R.id.action_search);
        showNewPostBtn = findViewById(R.id.showNewPostBtn);
        recommendTextBtn = findViewById(R.id.postRecommendationsTextBtn);
        friendsTextBtn = findViewById(R.id.postFriendsTextBtn);
        progressBar = (ProgressBar) findViewById(R.id.progressbarPostActivity);
    }

    private void startAnimation() {
        friendsTextBtn.animate().scaleX(1.14f).scaleY(1.14f).setDuration(300);
        recommendTextBtn.animate().scaleX(1).scaleY(1).setDuration(200);
    }

    private void createRecyclerView() {
        recyclerView = findViewById(R.id.postRecyclerViewFrinds);
        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);

        postAdapter = new Post_Adapter_Friends(this);
        postAdapter.setPostList(new ArrayList<Model_Post>());
        recyclerView.setAdapter(postAdapter);

        //TODO: перелистывает как VIEWPAGER + тонко настроить растояние и анимацию
       // SnapHelper snapHelper = new LinearSnapHelper();
       // snapHelper.attachToRecyclerView(recyclerView);

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
                        swipeRefreshLayout.setEnabled(true);
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

                    swipeRefreshLayout.setEnabled(false);
                }
            }
        });



    }

    private void initSearchUser() {
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
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

    //TODO: part of come to "Recommendation"

    private void initRecommendBtn() {
        recommendTextBtn.setOnClickListener(new View.OnClickListener() {
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
        showNewPostBtn.setVisibility(View.VISIBLE);
        showNewPostBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadPosts();
            }
        });
    }

    @Override
    public void disableProgressBar() {
        progressBar.setVisibility(View.GONE);
    }

    @Override
    public void showSnackBarNoInternet(String text) {
        Snackbar.make(findViewById(android.R.id.content), text, Snackbar.LENGTH_INDEFINITE).show();
    }

    //TODO: Block online/offline
    public void updateUserStatus(String state) {
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
    public void updateNavigationMenu() {
        //initialization navigation button
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigation);
        //set Dashboard selected
        bottomNavigationView.setSelectedItemId(R.id.userPosts_nav);
        //PerformItemSelectedListner
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
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
                        overridePendingTransition(0,0);
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
                return false;
            }
        });
    }

    @Override
    public void onBackPressed() {
        finish();
    }

}
