package com.example.myapplication.Common_Dagger_App_Class;

import android.app.Application;


import com.example.myapplication.main.Screens.Posts.Posts_By_Friends_MVP.di.DaggerPost_Friend_Component;
import com.example.myapplication.main.Screens.Posts.Posts_By_Friends_MVP.di.Post_Friend_Component;
import com.example.myapplication.main.Screens.Posts.Posts_By_Friends_MVP.di.Post_Friend_Module;
import com.example.myapplication.main.Screens.Posts.Posts_By_Recommendation_MVVM.di.DaggerPosts_Rec_Component;
import com.example.myapplication.main.Screens.Posts.Posts_By_Recommendation_MVVM.di.Post_Rec_Module;
import com.example.myapplication.main.Screens.Posts.Posts_By_Recommendation_MVVM.di.Posts_Rec_Component;

public class App extends Application {

    private Posts_Rec_Component postsRecComponent;
    private Post_Friend_Component postsFriendComponent;

    @Override
    public void onCreate() {
        super.onCreate();

        postsRecComponent = DaggerPosts_Rec_Component.builder()
                .post_Rec_Module(new Post_Rec_Module(this))
                .build();


        postsFriendComponent = DaggerPost_Friend_Component.builder()
                .post_Friend_Module(new Post_Friend_Module(this))
                .build();
    }

    public Posts_Rec_Component getpostsRecComponent() {
        return postsRecComponent;
    }

    public Post_Friend_Component getPostsFriendComponent() {
        return postsFriendComponent;
    }
}
