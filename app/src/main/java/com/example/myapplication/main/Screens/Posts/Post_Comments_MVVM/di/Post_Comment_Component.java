package com.example.myapplication.main.Screens.Posts.Post_Comments_MVVM.di;

import com.example.myapplication.main.Screens.Posts.Post_Comments_MVVM.Post_Comment_Activity;
import com.example.myapplication.main.Screens.Posts.Posts_By_Friends_MVP.di.Post_Friend_Module;

import javax.inject.Singleton;

import dagger.Component;

@Singleton
@Component(modules = {Post_Comment_Module.class})
public interface Post_Comment_Component {

    void inject(Post_Comment_Activity postCommentActivity);

}
