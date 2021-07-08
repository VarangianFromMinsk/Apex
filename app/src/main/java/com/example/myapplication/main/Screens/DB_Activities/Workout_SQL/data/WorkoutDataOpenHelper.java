package com.example.myapplication.main.Screens.DB_Activities.Workout_SQL.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;


public class WorkoutDataOpenHelper extends SQLiteOpenHelper {
    public WorkoutDataOpenHelper(Context context) {
        super(context, WorkoutDataContract.DATABASE_NAME,null, WorkoutDataContract.DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_MEMBERS_TABLE = "CREATE TABLE " + WorkoutDataContract.MemberEntry.TABLE_NAME + "("
                + WorkoutDataContract.MemberEntry.ID + " INTEGER PRIMARY KEY, "
                + WorkoutDataContract.MemberEntry.COLUMN_FIRST_NAME + " TEXT, "
                + WorkoutDataContract.MemberEntry.COLUMN_LAST_NAME + " TEXT, "
                + WorkoutDataContract.MemberEntry.COLUMN_GENDER + " INTEGER NOT NULL, "   // в гендере указываем, что он содержит INT и int !=0
                + WorkoutDataContract.MemberEntry.COLUMN_AGE + " INTEGER NOT NULL, "
                + WorkoutDataContract.MemberEntry.COLUMN_TIME + " INTEGER NOT NULL, "
                + WorkoutDataContract.MemberEntry.COLUMN_WNOW + " INTEGER NOT NULL, "
                + WorkoutDataContract.MemberEntry.COLUMN_WTAR + " INTEGER NOT NULL, "
                + WorkoutDataContract.MemberEntry.COLUMN_SPORT + " TEXT " + ")";

        db.execSQL(CREATE_MEMBERS_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + WorkoutDataContract.DATABASE_NAME);
        onCreate(db);
    }
}