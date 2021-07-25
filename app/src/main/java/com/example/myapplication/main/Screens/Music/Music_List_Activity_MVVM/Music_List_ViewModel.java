package com.example.myapplication.main.Screens.Music.Music_List_Activity_MVVM;

import android.app.Application;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.myapplication.Notes_ROOM_MVVM.DataBase.Model_Note;
import com.example.myapplication.Notes_ROOM_MVVM.Notes_ViewModel;
import com.example.myapplication.main.Models.Model_Song;
import com.example.myapplication.main.Screens.Music.Music_List_Activity_MVVM.DataBase.Music_Database;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class Music_List_ViewModel extends AndroidViewModel {

    private static Music_Database database;
    private final LiveData<List<Model_Song>> songs;
    private boolean itsWorkMusic = false;


    private final MutableLiveData<String> isThatAdmin = new MutableLiveData<>();

    //TODO: main constructor
    public Music_List_ViewModel(@NonNull Application application) {
        super(application);
        database = Music_Database.getInstance(getApplication());
        songs = database.music_dao().getAllSongs();
        Log.d("checkLoadMusicInDB", String.valueOf(songs));
    }


    //TODO: music part
    public LiveData<List<Model_Song>> getSongs() {
        return songs;
    }

    public void loadDataInDb(String search) {

        new DeleteAllTask().doInBackground();

        //todo: var to load only one time
        itsWorkMusic = true;

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Music");
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(itsWorkMusic){
                    for(DataSnapshot ds : snapshot.getChildren()){
                        Model_Song song = ds.getValue(Model_Song.class);
                        if(search.equals("no")){
                            new InsertTask().execute(song);

                        }
                        else{
                            if(song.getSongMainTitle().toLowerCase().contains(search.toLowerCase())
                                    || song.getSongLastTitle().toLowerCase().contains(search.toLowerCase())){
                                new InsertTask().execute(song);
                            }
                        }

                        itsWorkMusic = false;
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });

    }


    private static class InsertTask extends AsyncTask<Model_Song, Void, Void> {
        @Override
        protected Void doInBackground(Model_Song... model_songs) {
            if(model_songs != null && model_songs.length > 0){
                database.music_dao().insertSong(model_songs [0]);
            }
            return null;
        }
    }

    private static class DeleteAllTask extends AsyncTask<Void, Void, Void>{
        @Override
        protected Void doInBackground(Void... model_notes) {
            database.music_dao().deleteAllSongs();
            return null;
        }
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
