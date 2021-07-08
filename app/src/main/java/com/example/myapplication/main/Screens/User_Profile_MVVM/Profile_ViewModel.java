package com.example.myapplication.main.Screens.User_Profile_MVVM;

import android.app.Application;
import android.content.Context;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;

import com.example.myapplication.Services.Check_Internet_Connection_Exist;
import com.example.myapplication.main.Models.Model_Post;
import com.example.myapplication.main.Models.Model_User;
import com.example.myapplication.main.Models.Model_Song;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;

public class Profile_ViewModel extends AndroidViewModel {

    private MutableLiveData<ArrayList<Model_Post>> mutCurrentUserPostList = new MutableLiveData<>();
    private MutableLiveData<String> countOfPosts = new MutableLiveData<>();

    private MutableLiveData<ArrayList<Model_Song>> mutCurrentUserMusic = new MutableLiveData<>();
    private MutableLiveData<String> countOfSongs = new MutableLiveData<>();

    private MutableLiveData<Model_User> mutCurrentUser = new MutableLiveData<>();

    private MutableLiveData<String> checkChangePassword = new MutableLiveData<>();

    private MutableLiveData<String> isInternetGO = new MutableLiveData<>();

    //TODO: main constructor
    public Profile_ViewModel(@NonNull Application application) {
        super(application);
    }



    //TODO: posts part
    public void loadPosts(String selectedUser){
        mutCurrentUserPostList = Profile_Repository.getInstance().getPosts(selectedUser);
    }

    public MutableLiveData<ArrayList<Model_Post>> getMutCurrentUserPostList() {
        return mutCurrentUserPostList;
    }

    public void loadCountOfPost(String selectedUser){
        countOfPosts = Profile_Repository.getInstance().getCountOfPost(selectedUser);
    }

    public MutableLiveData<String> getCountOfPosts() {
        return countOfPosts;
    }



    //TODO: music part
    public void loadMusic(String selectedUser){
        mutCurrentUserMusic = Profile_Repository.getInstance().getMusic(selectedUser);
    }

    public MutableLiveData<ArrayList<Model_Song>> getMutCurrentUserMusic() {
        return mutCurrentUserMusic;
    }

    public void loadCountOfSongs(String selectedUser){
        countOfSongs = Profile_Repository.getInstance().getCountOfSongs(selectedUser);
    }

    public MutableLiveData<String> getCountOfSongs() {
        return countOfSongs;
    }



    //TODO: user part
    public void loadUserInfo(String selectedUser){
        mutCurrentUser = Profile_Repository.getInstance().getMutUserData(selectedUser);
    }

    public MutableLiveData<Model_User> getMutCurrentUser() {
        return mutCurrentUser;
    }



    //TODO: change user data
    public void changeUserData(String selectedUser, String path, String text){
        Profile_Repository.getInstance().changeUserData(selectedUser, path, text);
    }

    //TODO: change avatar or background
    public void updateCurrentAvatarOrBackground(String keyUser, String path, String imageUrl){
        Profile_Repository.getInstance().updateCurrentAvatarOrBackground(keyUser, path, imageUrl);
    }

    //TODO: change user password
    public void updatePassword(FirebaseUser user, String newPassword){
        checkChangePassword = Profile_Repository.getInstance().getCheckChangePassword(user,newPassword);
    }

    public MutableLiveData<String> getCheckChangePassword() {
        return checkChangePassword;
    }


    //TODO: check is internet go
    public void checkInternet(Context context){
        Check_Internet_Connection_Exist service = new Check_Internet_Connection_Exist();
        if(service.checkInternet(context)){
            isInternetGO.postValue("true");
        }
        else{
            isInternetGO.postValue("false");
        }
    }

    public MutableLiveData<String> getIsInternetGO() {
        return isInternetGO;
    }

}
