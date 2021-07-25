package com.example.myapplication.main.Screens.Music.Music_List_Activity_MVVM.DataBase;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.example.myapplication.Notes_ROOM_MVVM.DataBase.Model_Note;
import com.example.myapplication.main.Models.Model_Song;

@Database(entities = {Model_Song.class} , version = 1, exportSchema = false)
public abstract class Music_Database extends RoomDatabase {

    private static Music_Database database;

    private static final String DB_NAME = "music_db";
    private static final Object LOCK = new Object();

    //TODO: ленивый сигнлтон, но с синхронайз ( дает потокобезопасноть, но достаточно медленно)
    public static Music_Database getInstance(Context context){
        synchronized (LOCK) {
            if (database == null) {
                database = Room.databaseBuilder(context, Music_Database.class, DB_NAME)
                        .fallbackToDestructiveMigration()
                        .allowMainThreadQueries()
                        .build();
            }
        }
        return database;
    }

    public abstract Music_Dao music_dao();
}
