package com.example.myapplication.main.Screens.User_List_4_States_MVVM;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;

import com.example.myapplication.main.Models.Model_User;

import java.util.ArrayList;

public class User_List_ViewModel extends AndroidViewModel {

    private MutableLiveData<ArrayList<Model_User>> mutUserList = new MutableLiveData<>();

    //TODO: main constructor
    public User_List_ViewModel(@NonNull Application application) {
        super(application);
    }


    public void  loadUsers(String search, String type, String myUid){
        mutUserList = User_List_Repository.getInstance().getPostList(search, type, myUid);
    }

    public MutableLiveData<ArrayList<Model_User>> getMutUserList() {
        return mutUserList;
    }


}
