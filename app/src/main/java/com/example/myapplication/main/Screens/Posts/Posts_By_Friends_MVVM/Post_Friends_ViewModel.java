package com.example.myapplication.main.Screens.Posts.Posts_By_Friends_MVVM;

import android.app.Application;
import android.content.Context;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.myapplication.main.Models.Model_Post;

import java.util.ArrayList;

public class Post_Friends_ViewModel extends AndroidViewModel {

    private MutableLiveData<ArrayList<Model_Post>> mutPostListFriends = new MutableLiveData<>();
    private MutableLiveData<Boolean> showLoad = new MutableLiveData<>();
    private MutableLiveData<Boolean> showNewPosts = new MutableLiveData<>();

    public Post_Friends_ViewModel(@NonNull Application application) {
        super(application);
    }

    public void loadData(String searchText){
        mutPostListFriends =  Friends_Repository.instance.getPosts(searchText);
    }

    public LiveData<ArrayList<Model_Post>> getMutPostListFriends() {
        return mutPostListFriends;
    }

    public MutableLiveData<Boolean> getShowLoad() {
        showLoad = Friends_Repository.instance.getShowLoad();
        return showLoad;
    }

    public MutableLiveData<Boolean> getShowNewPosts() {
        showNewPosts = Friends_Repository.instance.getShowNewPosts();
        return showNewPosts;
    }
}
