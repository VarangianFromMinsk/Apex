package com.example.myapplication.main.Screens.DB_Activities.Workout_SQL;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.loader.app.LoaderManager;
import androidx.loader.content.CursorLoader;
import androidx.loader.content.Loader;

import android.content.ContentUris;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.example.myapplication.R;
import com.example.myapplication.main.Screens.DB_Activities.Workout_SQL.data.WorkoutDataContract;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class Workout_Register_Activity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {      // имплементируем возможность многопоточности

    private  final static int MEMBER_LOADER = 123;
    Member_CursorAdapter memberCursorAdapter;

    ListView dataListView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.workout_register_activity);

        setTitle("Workout Log");

        dataListView=findViewById(R.id.dataListView);

        FloatingActionButton floatingActionButton = findViewById(R.id.floatingActionButton);
        floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent GoToAddWorkout =new Intent(Workout_Register_Activity.this, Workout_Add_Member_Activity.class);
                startActivity(GoToAddWorkout);
            }
        });

        memberCursorAdapter = new Member_CursorAdapter(this,null,false);
        dataListView.setAdapter(memberCursorAdapter);
        // по нажатию по строчке из ListView
        dataListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                Intent intent = new Intent(Workout_Register_Activity.this, Workout_Add_Member_Activity.class);
                // Указываем путь к изменяемой строке
                Uri currentMemberUri = ContentUris.withAppendedId(WorkoutDataContract.MemberEntry.CONTENT_URI, id);
                // Передаем в интент
                intent.setData(currentMemberUri);
                startActivity(intent);
            }
        });

        // ИНИЦИАЛИЗИРУЕМ LOADER
        getSupportLoaderManager().initLoader(MEMBER_LOADER,null,this);
    }


    // Реализуем многопотомночть, что бы данные загружались в отедьном потоке,нежели сам user interface
    @NonNull
    @Override
    public Loader<Cursor> onCreateLoader(int id, @Nullable Bundle args) {   // Работа тут производиться в ФОНОВОМ РЕЖИМЕ!

        String[] projection = {
                WorkoutDataContract.MemberEntry.ID,
                WorkoutDataContract.MemberEntry.COLUMN_FIRST_NAME,
                WorkoutDataContract.MemberEntry.COLUMN_LAST_NAME,
                WorkoutDataContract.MemberEntry.COLUMN_SPORT,
                WorkoutDataContract.MemberEntry.COLUMN_AGE,
                WorkoutDataContract.MemberEntry.COLUMN_TIME,
                WorkoutDataContract.MemberEntry.COLUMN_WNOW,
                WorkoutDataContract.MemberEntry.COLUMN_WTAR,
                WorkoutDataContract.MemberEntry.COLUMN_GENDER
        };

        CursorLoader cursorLoader = new CursorLoader(
                this,
                WorkoutDataContract.MemberEntry.CONTENT_URI,
                projection,
                null,
                null,
                null);

        return cursorLoader;
    }

    @Override
    public void onLoadFinished(@NonNull Loader<Cursor> loader, Cursor data) {

        memberCursorAdapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(@NonNull Loader<Cursor> loader) {    // удаляет ссылки на невалидные значения из таблицы!

        memberCursorAdapter.swapCursor(null);
    }
}