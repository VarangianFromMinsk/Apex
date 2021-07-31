package com.example.myapplication.main.Screens.Posts.Posts_By_Friends_MVP.di;


import com.example.myapplication.main.Screens.Posts.Posts_By_Friends_MVP.Post_Activity_Friends;

import javax.inject.Singleton;

import dagger.Component;

@Singleton
@Component(modules = {Post_Friend_Module.class})
public interface Post_Friend_Component {

    void inject(Post_Activity_Friends postActivityFriends);
}
