package com.example.myapplication.main.Screens.Music.Music_List_Activity_MVVM;


import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;

import com.example.myapplication.main.Models.Model_Song;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class Music_List_Repository {

    private static Music_List_Repository instance;

    //todo: music part
    private final ArrayList<Model_Song> myMusicRepArray = new ArrayList<>();
    private MutableLiveData<ArrayList<Model_Song>> music = new MutableLiveData<>();
    private boolean itsWorkMusic = false;


    //TODO: main constructor
    public static Music_List_Repository getInstance() {
        if(instance == null){
            instance = new Music_List_Repository();
        }
        return instance;
    }


    //TODO: get music
    public MutableLiveData<ArrayList<Model_Song>> getMusic(String search) {
        loadMusic(search);
        return music;
    }

    private void loadMusic(String search) {
        //todo: var to load only one time
        itsWorkMusic = true;

        myMusicRepArray.clear();

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Music");
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(itsWorkMusic){
                    for(DataSnapshot ds : snapshot.getChildren()){
                        Model_Song song = ds.getValue(Model_Song.class);
                        if(search.equals("no")){
                            myMusicRepArray.add(0,song);
                        }
                        else{
                            if(song.getSongMainTitle().toLowerCase().contains(search.toLowerCase())
                                    || song.getSongLastTitle().toLowerCase().contains(search.toLowerCase())){
                                myMusicRepArray.add(0,song);
                            }
                        }

                        itsWorkMusic = false;
                    }
                    music.postValue(myMusicRepArray);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }

}
