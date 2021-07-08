package com.example.myapplication.main.Screens.Music.Music_List_Activity_MVVM;

import android.app.Application;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;

import com.example.myapplication.main.Models.Model_Song;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;

public class Music_List_ViewModel extends AndroidViewModel {

    private MutableLiveData<ArrayList<Model_Song>> mutCurrentUserMusic = new MutableLiveData<>();

    private MutableLiveData<String> isThatAdmin = new MutableLiveData<>();

    //TODO: main constructor
    public Music_List_ViewModel(@NonNull Application application) {
        super(application);
    }

    //TODO: music part
    public void loadMusic(String search){
        mutCurrentUserMusic = Music_List_Repository.getInstance().getMusic(search);
    }

    public MutableLiveData<ArrayList<Model_Song>> getMutCurrentUserMusic() {
        return mutCurrentUserMusic;
    }


    //TODO: is that admin
    public MutableLiveData<String> getIsThatAdmin() {
        return isThatAdmin;
    }

    public void checkIsThatAdmin() {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("users");
        String uID = FirebaseAuth.getInstance().getCurrentUser().getUid();
        ref.child(uID).child("nick").get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DataSnapshot> task) {
                if (!task.isSuccessful()) {
                    Log.e("firebase", "Error getting data", task.getException());
                }
                else {
                    String youNick =  String.valueOf(task.getResult().getValue());
                    if(youNick.equals("Admin")){
                        isThatAdmin.postValue("true");
                    }
                    else{
                        isThatAdmin.postValue("false");
                    }
                }
            }
        });
    }

}
