package com.example.myapplication.main.Screens.Posts.Posts_By_Friends_MVP;

import com.example.myapplication.main.Models.Model_Post;
import com.google.android.datatransport.runtime.dagger.Component;

import java.util.ArrayList;


public interface Post_List_view {

    void showData(ArrayList<Model_Post> posts);

    void initShowNewPost();

    void disableProgressBar();

    void showSnackBarNoInternet(String text);

}
