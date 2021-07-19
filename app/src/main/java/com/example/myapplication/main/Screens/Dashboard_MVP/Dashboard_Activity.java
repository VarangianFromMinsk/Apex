package com.example.myapplication.main.Screens.Dashboard_MVP;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.preference.PreferenceManager;
import androidx.viewpager.widget.ViewPager;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.myapplication.main.Models.Model_ViewPager;
import com.example.myapplication.R;
import com.example.myapplication.Services.Online_Offline_Service;
import com.example.myapplication.main.Screens.Dashboard_MVP.View_Pager_And_Shop_Activity.ViewPager_Adapter;
import com.example.myapplication.main.Screens.Find_Selected_Or_My_User_Location.Users_FInd_Location;
import com.example.myapplication.main.Screens.Posts.Posts_By_Friends_MVP.Post_Activity_Friends;
import com.example.myapplication.main.Screens.User_List_4_States_MVVM.User_List_Activity;
import com.example.myapplication.main.Screens.User_Profile_MVVM.User_Profile_Activity;
import com.example.myapplication.main.Screens.Additional.Kino_Json.Kino_Search_Info_Activity;
import com.example.myapplication.main.Screens.Music.Music_List_Activity_MVVM.Music_List_Activity;
import com.example.myapplication.Notes_ROOM_MVVM.Notes_Activity;
import com.example.myapplication.main.Screens.DB_Activities.Workout_SQL.Workout_Register_Activity;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Objects;

public class Dashboard_Activity extends AppCompatActivity implements Dashboard_view {

    private BottomNavigationView bottomNavigationView;
    private String myUid;

    //staff for kino block
    private ImageView kinoIv;
    private TextView kinoTitle;

    //view Pager
    private ViewPager viewPager;

    public ImageView stateWeather;
    public  TextView city, temp, humidity;
    public  EditText editSearch;
    private String city_default = "Минск";

    private static final DecimalFormat df = new DecimalFormat("0.00");

    private Dashboard_Presenter presenter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dashboard_activity);

        Objects.requireNonNull(getSupportActionBar()).hide();

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        presenter = new Dashboard_Presenter(this);

        initialization();

        initWeatherCard();

        checkCurrentCityFromSave();

        loadWeather(city_default);

        checkLastKinoState();

        initNotes();

        loadCards();

        cardsListener();

        updateNavigationMenu();

        getAndWriteToken();

        updateUserStatus("online");

    }

    private void initialization() {
        stateWeather = findViewById(R.id.stateWeather);
        city = findViewById(R.id.cityWeather);
        temp = findViewById(R.id.tempWeather);
        humidity = findViewById(R.id.humidityWeather);
        editSearch = findViewById(R.id.search_weather);

        kinoIv = findViewById(R.id.kinoLastSerachIv);
        kinoTitle = findViewById(R.id.kinoTitle);

        bottomNavigationView = findViewById(R.id.bottomNavigation);

        myUid = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid();
    }

    //todo: methods about weather
    private void  initWeatherCard(){

        editSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                loadWeather(s);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

    }

    private void checkCurrentCityFromSave() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        city_default = preferences.getString("city", "Minsk");
    }

    private void loadWeather(CharSequence s){
        city_default = String.valueOf(s);
        presenter.loadWeather(city_default);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        presenter.removeApiObserver();
    }

    @Override
    public void loadWeatherComplete(String name_from_Api, String correct_temp, String description_from_Api, String currentWeatherImage) {
        city.setText(name_from_Api);
        temp.setText(correct_temp);
        humidity.setText(description_from_Api);
        editSearch.setHint(name_from_Api);
        Glide.with(stateWeather.getContext()).load(currentWeatherImage).into(stateWeather);
    }

    @Override
    public void saveCurrentTown(String name_from_Api) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        preferences.edit().putString("city", name_from_Api ).apply();
    }

    //todo: methods about search kino
    private void checkLastKinoState() {

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        String poster = preferences.getString("last_movie_search_poster", "nope");
        String title = preferences.getString("last_movie_search_name", "Kino");

        if(poster.equals("nope")){
            kinoIv.setImageResource(R.drawable.kino);
        }
        else{
            Glide.with(kinoIv.getContext()).load(poster).into(kinoIv);
        }

        if(title.equals("Kino")){
            kinoTitle.setText(R.string.no_search);
        }
        else{
            kinoTitle.setText(String.valueOf("Last search: " + "\n" + title));
        }

    }

    //todo: notes
    private void initNotes() {
        //staff for notes
        CardView notes = findViewById(R.id.notesCard);
        notes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent notes = new Intent(Dashboard_Activity.this, Notes_Activity.class);
                startActivity(notes);
            }
        });
    }

    //todo: viewPager part
    private void loadCards() {

        viewPager = findViewById(R.id.viewPagerShop);
        //init list
        ArrayList<Model_ViewPager> shopList = new ArrayList<>();

        //add item
        shopList.add(new Model_ViewPager(" Tactical helmet s2180",
                " New generation tactical helmet",
                R.drawable.helmet,98));

        shopList.add(new Model_ViewPager(" Tactical armour e1210",
                " New generation tactical armour",
                R.drawable.armour,144));

        shopList.add(new Model_ViewPager(" Tactical kneepads u808",
                " New generation tactical kneepads",
                R.drawable.kneepads,32));

        shopList.add(new Model_ViewPager(" Tactical gloves us218",
                " New generation tactical gloves",
                R.drawable.tacticalgloves,20));

        //setup adapter
        ViewPager_Adapter viewPagerAdapter = new ViewPager_Adapter(this, shopList);
        //set adapter
        viewPager.setAdapter(viewPagerAdapter);
        viewPager.setCurrentItem(1);
        //set default padding from left/right
        viewPager.setPadding(100,0,100,0);
    }

    private void cardsListener(){
        //TODO:set viewPager listener
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {

            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
    }

    //todo: write token for cloud message
    private void getAndWriteToken() {
        presenter.getAndLoadToken();
    }

    public void updateNavigationMenu(){

        bottomNavigationView.setSelectedItemId(R.id.dashboard_nav);
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

    //TODO: block online/offline
    public void updateUserStatus( String state){
        Online_Offline_Service service = new Online_Offline_Service();
        service.updateUserStatus(state, this);
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
    //TODO:  block online/offline

    //todo: disable back btn
    @Override
    public void onBackPressed() {

    }

    public void PathToRecycleView(View view) {
        Intent PathToRecycleView = new Intent(Dashboard_Activity.this, Music_List_Activity.class);
        startActivity(PathToRecycleView);
    }

    public void PathToWregister(View view) {
        Intent PathToWregister = new Intent(Dashboard_Activity.this, Workout_Register_Activity.class);
        startActivity( PathToWregister);
    }

    public void PathToKino(View view) {
        Intent PathToKino = new Intent(Dashboard_Activity.this, Kino_Search_Info_Activity.class);
        startActivity(PathToKino);
    }

    public void PathToLocation(View view) {
        Intent PathToLocation = new Intent(Dashboard_Activity.this, Users_FInd_Location.class);
        PathToLocation.putExtra("selectedUser", myUid);
        startActivity(PathToLocation);
    }

}