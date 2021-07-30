package com.example.myapplication.main.Screens.Posts.Posts_By_Recommendation_MVVM.di;

import android.app.Application;

public class App extends Application {

    private AppComponentDagger appComponent;

    @Override
    public void onCreate() {
        super.onCreate();

        appComponent = DaggerAppComponentDagger.builder()
                .post_Rec_Module(new Post_Rec_Module(this))
                .build();

    }

    public AppComponentDagger getAppComponent() {
        return appComponent;
    }
}
