package com.example.myapplication.di;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;

import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.myapplication.Services.Check_Internet_Connection_Exist;
import com.example.myapplication.Services.Online_Offline_User_Service_To_Firebase;
import com.example.myapplication.main.Screens.Posts.Posts_By_Friends_MVP.Post_Adapter_Friends;
import com.example.myapplication.main.Screens.Posts.Posts_By_Recommendation_MVVM.Post_Adapter_Recommendations;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

@Module
public class Common_App_Module {

    Application application;
    Activity activity;
    Context context;

    SharedPreferences preferences;

    public Common_App_Module(Application application) {
        this.application = application;
        activity = (Activity) context;
        context = application.getApplicationContext();
        preferences = PreferenceManager.getDefaultSharedPreferences(context);
    }

    //todo: помечаем что эти методы и есть каие-то нужные нам зависимости

    @Provides
    @Singleton
    Context provideContext(){
        return this.context;
    }

    @Provides
    @Singleton
    Application provideApplication(){
        return this.application;
    }

    @Provides
    @Singleton
    Activity provideActivity(){
        return this.activity;
    }

    @Provides
    @Singleton
    SharedPreferences providePreference(){
        return this.preferences;
    }



    @Provides
    @Singleton
    Check_Internet_Connection_Exist provideCheckInternetConnection(){
        return new Check_Internet_Connection_Exist();
    }

    @Provides
    @Singleton
    Online_Offline_User_Service_To_Firebase provideObserverLifeCycle(SharedPreferences preferences, Context context){
        return new Online_Offline_User_Service_To_Firebase(preferences, context);
    }

    @Provides
    @Singleton
    Post_Adapter_Friends provideFriendsAdapter(Context context){
        return new Post_Adapter_Friends(context);
    }

    @Provides
    @Singleton
    Post_Adapter_Recommendations provideRecAdapter(Context context){
        return new Post_Adapter_Recommendations(context);
    }

    @Provides
        //todo: ту без синглтона, тк нужен каждый раз новый обьект
    GridLayoutManager provideGridLayout(Context context){
        return new GridLayoutManager(context,3);
    }

    @Provides
    LinearLayoutManager provideLinearLayout(Context context){
        return new LinearLayoutManager(context);
    }
}
