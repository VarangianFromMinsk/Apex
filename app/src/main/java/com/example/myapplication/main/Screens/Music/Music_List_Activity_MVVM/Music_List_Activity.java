package com.example.myapplication.main.Screens.Music.Music_List_Activity_MVVM;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.SearchView;
import android.widget.Toast;

import com.example.myapplication.Notes_ROOM_MVVM.DataBase.Model_Note;
import com.example.myapplication.main.Screens.Dashboard_MVP.Dashboard_Activity;
import com.example.myapplication.R;
import com.example.myapplication.Services.Online_Offline_Service;
import com.example.myapplication.main.Models.Model_Song;
import com.example.myapplication.main.Screens.Posts.Posts_By_Friends_MVP.Post_Activity_Friends;
import com.example.myapplication.main.Screens.User_List_4_States_MVVM.User_List_Activity;
import com.example.myapplication.main.Screens.User_Profile_MVVM.User_Profile_Activity;
import com.example.myapplication.main.Screens.Music.Add_Music_Activity_MVP.Add_Music_Activity;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Music_List_Activity extends AppCompatActivity {

    //todo: main staff
    private RecyclerView recyclerView;
    private Music_Adapter adapter;
    private SearchView searchMusic;

    //todo: additional staff
    private ProgressBar progressBar;
    private FloatingActionButton floatingActionButton;

    private Music_List_ViewModel viewModel;
    private LiveData<List<Model_Song>> songsFromDb;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.music_list_activity);

        Objects.requireNonNull(getSupportActionBar()).hide();

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        viewModel = new ViewModelProvider(this).get(Music_List_ViewModel.class);

        initialization();

        updateNavigationMenu();

        initSearchUser();

        createRecyclerViewAndLoadData();

       // loadMusic();

        addMusicForAdmin();

    }

    private void initialization() {
        searchMusic = findViewById(R.id.searchTextMusic);
        recyclerView = findViewById(R.id.RecyclerView);
        progressBar = findViewById(R.id.progressbarInMusicList);
        floatingActionButton = findViewById(R.id.floatingBtnAddMusic);
    }

    private void initSearchUser(){
        searchMusic.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                //call when user press
                if(!TextUtils.isEmpty(query)){
                    searchMusic(query);
                }
                else{
                    loadMusic();
                }
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if(!TextUtils.isEmpty(newText)){
                    searchMusic(newText);
                }
                else{
                    loadMusic();
                }
                return false;
            }
        });
    }

    private void createRecyclerViewAndLoadData(){

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);

        adapter = new Music_Adapter(this);
        adapter.setMusicList(new ArrayList<Model_Song>());
        recyclerView.setAdapter(adapter);

        viewModel.loadDataInDb("no");

        LiveData <List<Model_Song>> songsFromDb = viewModel.getSongs();

        songsFromDb.observe(this, new Observer<List<Model_Song>>() {
            @Override
            public void onChanged(List<Model_Song> songs) {
                progressBar.setVisibility(View.GONE);
                adapter.setMusicList(songs);
            }
        });

    }

    private void loadMusic(){
        viewModel.loadDataInDb("no");
    }

    private void searchMusic(String searchQuery){
        viewModel.loadDataInDb(searchQuery);
    }

    private void addMusicForAdmin() {

        floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                viewModel.checkIsThatAdmin();
                viewModel.getIsThatAdmin().observe(Music_List_Activity.this, new Observer<String>() {
                    @Override
                    public void onChanged(String s) {
                        if(s.equals("true")){
                            transAddMusic();
                        }
                        else{
                            Toast.makeText(Music_List_Activity.this, "No access rights", Toast.LENGTH_LONG).show();
                            floatingActionButton.setEnabled(false);
                        }
                    }
                });
            }
        });

    }

    private void transAddMusic(){
        Intent intent = new Intent(this, Add_Music_Activity.class);
        startActivity(intent);

    }

    private void updateNavigationMenu(){
        //initialization navigation button
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigation);
        //set Dashboard selected
        bottomNavigationView.setSelectedItemId(R.id.musicPlayer_nav);
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

    //todo: block online/offline and location
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
    public void onBackPressed() {
        finish();
    }
}