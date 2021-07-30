package com.example.myapplication.main.Screens.Posts.Posts_By_Recommendation_MVVM.di;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;

import com.example.myapplication.main.Screens.Posts.Posts_By_Recommendation_MVVM.Post_Activity_Recommendations;
import javax.inject.Singleton;
import dagger.BindsInstance;
import dagger.Component;


//todo: мост между даггером и зависимостями
@Component(modules = {Post_Rec_Module.class,SecondModule.class})
@Singleton
public interface AppComponentDagger {

    //todo: должна быть сугобо активити/фрагмент которая получит зависимости
    void inject (Post_Activity_Recommendations postActivityRecommendations);

 }
