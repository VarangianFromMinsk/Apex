package com.example.myapplication.main.Screens.Posts.Posts_By_Recommendation_MVVM.di;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;

import androidx.preference.PreferenceManager;

import com.example.myapplication.main.Screens.Posts.Posts_By_Recommendation_MVVM.Post_Activity_Recommendations;
import com.example.myapplication.main.Screens.Posts.Posts_By_Recommendation_MVVM.Post_Adapter_Recommendations;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;


@Module
public class Post_Rec_Module {

    Application application;
    Context context;

    SharedPreferences preferences;

    public Post_Rec_Module(Application application) {
        this.application = application;
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
    SharedPreferences providePreference(){
        return this.preferences;
    }

}
