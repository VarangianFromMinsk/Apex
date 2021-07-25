package com.example.myapplication.Notes_ROOM_MVVM.DataBase;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.example.myapplication.Notes_ROOM_MVVM.DataBase.Model_Note;

import java.util.List;

@Dao
public interface Notes_Dao {

    @Query("SELECT * FROM notes ORDER BY priority")
    LiveData<List<Model_Note>> getAllNotes();

    @Query("SELECT * FROM notes WHERE id = :id")
    Model_Note getById(int id);

    @Insert
    void insertNote(Model_Note note);

    @Delete
    void deleteNote(Model_Note note);

    @Update
    void updateNote(Model_Note note);

    @Query("DELETE FROM notes")
    void deleteAllNotes();
}
