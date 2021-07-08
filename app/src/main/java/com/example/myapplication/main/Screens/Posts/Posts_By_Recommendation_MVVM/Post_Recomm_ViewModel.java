package com.example.myapplication.main.Screens.Posts.Posts_By_Recommendation_MVVM;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.myapplication.main.Models.Model_Post;

import java.util.ArrayList;

public class Post_Recomm_ViewModel extends AndroidViewModel {

    private MutableLiveData<ArrayList<Model_Post>> mutPostListFriends = new MutableLiveData<>();

    //TODO: main constructor
    public Post_Recomm_ViewModel(@NonNull Application application) {
        super(application);
    }

    public void loadData(String searchText){
        mutPostListFriends =  Recommendation_Repository.getInstance().getPostList(searchText);
    }

    public LiveData<ArrayList<Model_Post>> getMutPostListFriends() {
        return mutPostListFriends;
    }

}
