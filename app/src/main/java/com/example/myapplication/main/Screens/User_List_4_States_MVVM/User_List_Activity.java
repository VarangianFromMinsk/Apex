package com.example.myapplication.main.Screens.User_List_4_States_MVVM;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.SearchView;

import com.example.myapplication.Common_Dagger_App_Class.App;
import com.example.myapplication.Services.Online_Offline_User_Service_To_Firebase;
import com.example.myapplication.databinding.UserListActivityBinding;
import com.example.myapplication.main.Models.Model_User;
import com.example.myapplication.main.Screens.Dashboard_MVP.Dashboard_Activity;
import com.example.myapplication.R;
import com.example.myapplication.main.Screens.Chat_Activity_MVP.Chat_Main_Activity;
import com.example.myapplication.main.Screens.Posts.Posts_By_Friends_MVVM.Post_Activity_Friends;
import com.example.myapplication.main.Screens.User_Profile_MVVM.User_Profile_Activity;
import com.example.myapplication.main.Screens.Music.Music_List_Activity_MVVM.Music_List_Activity;
import com.google.android.material.navigation.NavigationBarView;
import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;
import java.util.Objects;

import javax.inject.Inject;


public class User_List_Activity extends AppCompatActivity {

    //todo: all staff for repost
    private boolean isThisRepost = false;
    private String RePpDescription, RePuName, RePpImage, RePpTime, RePpId;

    //todo: all staff to share Image
    private boolean isThisShareImage = false;
    private String imageShare;

    private int countNumber;
    private String textCount, mainSwitch;
    private boolean isItRepostAction;
    private String myUid = "";

    private ArrayList<Model_User> userArrayList;

    @Inject
    User_List_Adapter userAdapter;

    @Inject
    LinearLayoutManager layoutManager;

    @Inject
    Online_Offline_User_Service_To_Firebase controller;

    private User_List_ViewModel viewModel;

    //todo: указываем тег <layout>, пересобираем проект и получаем сгенерированный класс как ниже
    private UserListActivityBinding userListActivityBinding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.user_list_activity);

        Objects.requireNonNull(getSupportActionBar()).hide();

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        initialization();

        getMainIntent();

        initSearchUser();

        createRecyclerViewAndFirstLoad();

        updateNavigationMenu();

        updateUpperNavigationMenu();

        initRefreshLayout();

        transDataForRepost();

        transDataForShareImage();
    }


    //todo: main methods
    private void initialization() {

        //todo: инициализируем Data Binding
        userListActivityBinding = DataBindingUtil.setContentView(this, R.layout.user_list_activity);
        userListActivityBinding.upNavigation.setVisibility(View.VISIBLE);

        ((App) getApplication()).getCommonComponent().inject(this);

        viewModel = new ViewModelProvider(this).get(User_List_ViewModel.class);

        getLifecycle().addObserver(controller);

        FirebaseAuth auth = FirebaseAuth.getInstance();
        myUid = auth.getUid();
    }

    private void getMainIntent() {
        Intent intent = getIntent();
        Bundle extras = intent.getExtras();
        if (extras != null) {
            mainSwitch = intent.getStringExtra("typeOfUserList");

            try {
                if (!intent.getStringExtra("imageURL").isEmpty()){
                    hideUpandDownBars();
                }
            } catch (Exception ignored) { }

            try {
                if (!intent.getStringExtra("pId").isEmpty()) {
                        hideUpandDownBars();
                    }
            } catch (Exception ignored) { }

        }
    }

    private void hideUpandDownBars(){
        isItRepostAction = true;
        userListActivityBinding.upNavigation.setVisibility(View.GONE);
        userListActivityBinding.bottomNavigation.setVisibility(View.GONE);
    }

    private void createRecyclerViewAndFirstLoad() {
        //TODO: part of recyclerview
       // layoutManager = new LinearLayoutManager(this);
        userListActivityBinding.userListRecyclerView.setLayoutManager(layoutManager);


        //userAdapter = new User_List_Adapter(this);
        userAdapter.setUserList(new ArrayList<Model_User>());
        userListActivityBinding.userListRecyclerView.setAdapter(userAdapter);


        userAdapter.setOnUserClickListener(new User_List_Adapter.OnUserClickListener() {
            @Override
            public void onUserClick(int position) {
                userArrayList = userAdapter.getUserList();
                goToChat(position);
            }
        });

        loadUser();

        viewModel.getMutUserList().observe(this, new Observer<ArrayList<Model_User>>() {
            @Override
            public void onChanged(ArrayList<Model_User> users) {
                userAdapter.setUserList(users);
                userListActivityBinding.progressbarInUserList.setVisibility(View.GONE);
                setCount(users);
            }
        });

    }

    private void initSearchUser() {
        userListActivityBinding.searchInUserList.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                if (!TextUtils.isEmpty(query)) {
                    Thread thread = new Thread() {
                        @Override
                        public void run() {
                            try {
                                sleep(500);
                            } catch (Exception ignored) {
                            } finally {
                                if (query.equals("")) {
                                    loadUser();
                                } else {
                                    searchUsers(query);
                                }
                            }
                        }
                    };
                    thread.start();
                } else {
                    loadUser();
                }
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if (!TextUtils.isEmpty(newText)) {
                    searchUsers(newText);
                } else {
                    loadUser();
                }
                return false;
            }
        });
    }

    private void searchUsers(String searchQuery) {
        viewModel.loadUsers(searchQuery, mainSwitch, myUid);
    }

    private void loadUser() {
        viewModel.loadUsers("no", mainSwitch, myUid);
    }

    private void goToChat(int position) {
        if (isThisRepost) {
            //todo: user list for repost
            Intent intent = new Intent(User_List_Activity.this, Chat_Main_Activity.class)
                    .putExtra("recipientUserId", userArrayList.get(position).getFirebaseId())
                    .putExtra("recipientAvatar", userArrayList.get(position).getAvatarMockUpResourse())
                    .putExtra("recipientUserName", userArrayList.get(position).getName())
                    .putExtra("status", userArrayList.get(position).getOnline())
                    .putExtra("Text", RePpDescription)
                    .putExtra("pUserName", RePuName)
                    .putExtra("pImage", RePpImage)
                    .putExtra("pTime", RePpTime)
                    .putExtra("pId", RePpId)
                    .putExtra("isIntentExist", "exist")
                    .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);

            isThisRepost = false;

            userListActivityBinding.upNavigation.setVisibility(View.GONE);
        } else if (isThisShareImage) {
            Intent intent = new Intent(User_List_Activity.this, Chat_Main_Activity.class)
                    .putExtra("shareImage", imageShare)
                    .putExtra("recipientUserId", userArrayList.get(position).getFirebaseId())
                    .putExtra("recipientAvatar", userArrayList.get(position).getAvatarMockUpResourse())
                    .putExtra("recipientUserName", userArrayList.get(position).getName());
            startActivity(intent);

            isThisShareImage = false;

            userListActivityBinding.upNavigation.setVisibility(View.GONE);
        } else {
            //todo: Normal user list
            Intent intent = new Intent(User_List_Activity.this, Chat_Main_Activity.class)
                    .putExtra("recipientUserId", userArrayList.get(position).getFirebaseId())
                    .putExtra("recipientAvatar", userArrayList.get(position).getAvatarMockUpResourse())
                    .putExtra("recipientUserName", userArrayList.get(position).getName());
            startActivity(intent);
        }

    }

    public void initRefreshLayout() {

        userListActivityBinding.swipeLayUserList.setColorSchemeResources(R.color.purple_500);

        userListActivityBinding.swipeLayUserList.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                Intent intent = getIntent();
                finish();
                overridePendingTransition(0, 0);
                startActivity(intent);
                userListActivityBinding.swipeLayUserList.setRefreshing(false);
            }
        });
    }

    //todo: part of count
    private void setCount(ArrayList<Model_User> users) {
        refreshCountNumber();
        countNumber = users.size();
        setCountText(countNumber);
    }

    private void refreshCountNumber() {
        if (countNumber != 0) {
            countNumber = 0;
        }
    }

    private void setCountText(int countNumber) {

        if (mainSwitch.equals("all")) {
            textCount = "All users:  " + countNumber;
        } else if (mainSwitch.equals("friends")) {
            textCount = "Friends:  " + countNumber;
        } else if (mainSwitch.equals("requests")) {
            textCount = "Requests:  " + countNumber;
        } else if (mainSwitch.equals("ban")) {
            textCount = "Blocked by you:  " + countNumber;
        }

        //todo: use Binding Instead of TextView with findViewById
        //countTv.setText(textCount);
        userListActivityBinding.numberUsersList.setText(textCount);
    }

    public void updateNavigationMenu() {
        userListActivityBinding.bottomNavigation.setSelectedItemId(R.id.userChat_nav);
        //PerformItemSelectedListener
        userListActivityBinding.bottomNavigation.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
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

    public void updateUpperNavigationMenu() {
        //todo: set Dashboard selected
        if (mainSwitch.equals("all")) {
            userListActivityBinding.upNavigation.setSelectedItemId(R.id.All_nav);
        } else if (mainSwitch.equals("friends")) {
            userListActivityBinding.upNavigation.setSelectedItemId(R.id.Friends_nav);
        } else if (mainSwitch.equals("requests")) {
            userListActivityBinding.upNavigation.setSelectedItemId(R.id.Request_nav);
        } else if (mainSwitch.equals("ban")) {
            userListActivityBinding.upNavigation.setSelectedItemId(R.id.Ban_nav);
        }
        //todo: PerformItemSelectedListener
        userListActivityBinding.upNavigation.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @SuppressLint("NonConstantResourceId")
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.All_nav:
                        Intent intentAll = new Intent(User_List_Activity.this, User_List_Activity.class);
                        intentAll.putExtra("typeOfUserList", "all");
                        startActivity(intentAll);
                        overridePendingTransition(0, 0);
                        finish();
                        return true;
                    case R.id.Friends_nav:
                        Intent intentFriends = new Intent(User_List_Activity.this, User_List_Activity.class);
                        intentFriends.putExtra("typeOfUserList", "friends");
                        startActivity(intentFriends);
                        overridePendingTransition(0, 0);
                        finish();
                        return true;
                    case R.id.Request_nav:
                        Intent intentRequest = new Intent(User_List_Activity.this, User_List_Activity.class);
                        intentRequest.putExtra("typeOfUserList", "requests");
                        startActivity(intentRequest);
                        overridePendingTransition(0, 0);
                        finish();
                        return true;
                    case R.id.Ban_nav:
                        Intent intentBan = new Intent(User_List_Activity.this, User_List_Activity.class);
                        intentBan.putExtra("typeOfUserList", "ban");
                        startActivity(intentBan);
                        overridePendingTransition(0, 0);
                        finish();
                        return true;
                }
                return true;
            }
        });
    }

    //todo: trans intents
    private void transDataForShareImage() {
        disableUpperMenuAndAnotherStaff();

        Intent intentFromShowImage = getIntent();
        if (intentFromShowImage != null) {
            String check = intentFromShowImage.getStringExtra("check");
            if (check != null) {
                isThisShareImage = true;
            }
            imageShare = intentFromShowImage.getStringExtra("imageURL");
        }
    }

    private void transDataForRepost() {
        disableUpperMenuAndAnotherStaff();

        Intent intentFromPost = getIntent();
        if (intentFromPost != null) {
            RePpDescription = intentFromPost.getStringExtra("Text");
            if (RePpDescription != null) {
                isThisRepost = true;
            }
            RePuName = intentFromPost.getStringExtra("pUserName");
            RePpImage = intentFromPost.getStringExtra("pImage");
            RePpTime = intentFromPost.getStringExtra("pTime");
            RePpId = intentFromPost.getStringExtra("pId");

        }
    }

    private void disableUpperMenuAndAnotherStaff() {
        userListActivityBinding.upNavigation.setEnabled(false);
        userListActivityBinding.bottomNavigation.setEnabled(false);
    }

    //todo: disable click back
    @Override
    public void onBackPressed() {
        if (isItRepostAction) {
            super.onBackPressed();
        }
        //disable
    }
}