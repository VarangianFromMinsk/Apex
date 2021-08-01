package com.example.myapplication.main.Screens.Posts.Add_Change_Post_MVP.di;

import android.app.Activity;
import android.app.Application;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;

import androidx.preference.PreferenceManager;

import com.example.myapplication.Services.Check_Internet_Connection_Exist;
import com.example.myapplication.Services.Online_Offline_User_Service_To_Firebase;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

@Module
public class Post_AddChange_Module {

    Application application;
    Activity activity;
    Context context;

    SharedPreferences preferences;

    public Post_AddChange_Module(Application application) {
        this.application = application;
        activity = (Activity) context;
        context = application.getApplicationContext();
        preferences = PreferenceManager.getDefaultSharedPreferences(context);
    }

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

}
