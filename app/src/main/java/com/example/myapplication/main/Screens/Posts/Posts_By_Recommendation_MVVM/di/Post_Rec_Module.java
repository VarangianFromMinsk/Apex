package com.example.myapplication.main.Screens.Posts.Posts_By_Recommendation_MVVM.di;

import android.content.Context;

import com.example.myapplication.main.Screens.Posts.Posts_By_Recommendation_MVVM.Post_Activity_Recommendations;
import com.example.myapplication.main.Screens.Posts.Posts_By_Recommendation_MVVM.Post_Adapter_Recommendations;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;


@Module
public class Post_Rec_Module {

    Context context;

    public Post_Rec_Module(Context context) {
        this.context = context;
    }

    //todo: помечаем что эти методы и есть каие-то нужные нам зависимости
    @Provides
    @Singleton
    Context provideContext(){
        return this.context;
    }

}
