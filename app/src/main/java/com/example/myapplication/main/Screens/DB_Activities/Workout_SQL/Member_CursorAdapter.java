package com.example.myapplication.main.Screens.DB_Activities.Workout_SQL;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.myapplication.R;
import com.example.myapplication.main.Screens.DB_Activities.Workout_SQL.data.WorkoutDataContract;

public class Member_CursorAdapter extends CursorAdapter {   // Делаем по тутуору

    public Member_CursorAdapter(Context context, Cursor c, boolean autoRequery) {
        super(context, c, autoRequery);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return LayoutInflater.from(context).inflate(R.layout.row_memver, parent, false);
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {

        TextView firstNameTextView = view.findViewById(R.id.firstNameTextView);
        TextView lastNameTextView = view.findViewById(R.id.lastNameTextView);
        TextView sportTextView = view.findViewById(R.id.sportTextView);
        TextView ageTextView = view.findViewById(R.id.ageTextView);
        TextView timeTextView = view.findViewById(R.id.timeTextView);
        TextView wnowTextView = view.findViewById(R.id.wNowTextView);
        TextView wtarTextView = view.findViewById(R.id.wTarTextView);
        ImageView imageGender = view.findViewById(R.id.imageViewWorkout);

        String firstName = cursor.getString(cursor.getColumnIndexOrThrow(WorkoutDataContract.MemberEntry.COLUMN_FIRST_NAME));    // получаем значения из курсора
        String lastName = cursor.getString(cursor.getColumnIndexOrThrow(WorkoutDataContract.MemberEntry.COLUMN_LAST_NAME));
        String sport = cursor.getString(cursor.getColumnIndexOrThrow(WorkoutDataContract.MemberEntry.COLUMN_SPORT));
        String age = cursor.getString(cursor.getColumnIndexOrThrow(WorkoutDataContract.MemberEntry.COLUMN_AGE));
        String time = cursor.getString(cursor.getColumnIndexOrThrow(WorkoutDataContract.MemberEntry.COLUMN_TIME));
        String wnow = cursor.getString(cursor.getColumnIndexOrThrow(WorkoutDataContract.MemberEntry.COLUMN_WNOW));
        String wtar = cursor.getString(cursor.getColumnIndexOrThrow(WorkoutDataContract.MemberEntry.COLUMN_WTAR));
        int gender = cursor.getInt(cursor.getColumnIndexOrThrow(WorkoutDataContract.MemberEntry.COLUMN_GENDER));

        firstNameTextView.setText(firstName + " .");
        lastNameTextView.setText(lastName + " .");
        sportTextView.setText(sport + " .");
        ageTextView.setText(age + " .");
        timeTextView.setText(time);
        wnowTextView.setText(wnow);
        wtarTextView.setText(wtar);

        if(gender==1){
            imageGender.setImageResource(R.drawable.male);
        }else if(gender==2){
            imageGender.setImageResource(R.drawable.female);
        }else if(gender==0){
            imageGender.setImageResource(R.drawable.search_gender);
        }

    }
}
