package com.example.myapplication.Notes_ROOM_MVVM;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

@Database(entities = {Model_Note.class} , version = 1, exportSchema = false)
public abstract class Notes_Database extends RoomDatabase {
    private static Notes_Database database;
    private static final String DB_NAME = "notes_big_db";
    private static final Object LOCK = new Object();

    public static Notes_Database getInstance(Context context){
        synchronized (LOCK) {
            if (database == null) {
                database = Room.databaseBuilder(context, Notes_Database.class, DB_NAME).build();
            }
        }
        return database;
    }

    public abstract Notes_Dao notes_dao();
}
