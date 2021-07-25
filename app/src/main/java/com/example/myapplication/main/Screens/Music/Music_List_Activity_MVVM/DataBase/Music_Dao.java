package com.example.myapplication.main.Screens.Music.Music_List_Activity_MVVM.DataBase;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import com.example.myapplication.Notes_ROOM_MVVM.DataBase.Model_Note;
import com.example.myapplication.main.Models.Model_Song;
import java.util.List;

@Dao
public interface Music_Dao {

    @Query("SELECT * FROM songs")
    LiveData<List<Model_Song>> getAllSongs();

    @Query("SELECT * FROM songs WHERE id = :id")
    Model_Song getById(int id);

    @Query("DELETE FROM songs")
    void deleteAllSongs();

    @Insert
    void insertSong(Model_Song song);
}
