package com.example.myapplication.main.Screens.DB_Activities.Workout_SQL;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NavUtils;
import androidx.loader.app.LoaderManager;
import androidx.loader.content.CursorLoader;
import androidx.loader.content.Loader;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.example.myapplication.R;
import com.example.myapplication.main.Screens.DB_Activities.Workout_SQL.data.WorkoutDataContract;

import java.util.ArrayList;

public class Workout_Add_Member_Activity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {
    private EditText firstNameEditText;
    private EditText lastNameEditText;
    private EditText sportEditText;
    private EditText ageEditText;
    private EditText timeEditText;
    private EditText wNowEditText;
    private EditText wTarEditText;
    private Spinner genderSpinner;
    private int gender = 0;                                  // 0 - Unknow, 1 -Male, 2 - Female.
    private ArrayAdapter spinnerAdapter;
    private ArrayList spinnerArrayList;

    private static final int EDIT_MEMBER_LOADER = 124;

    Uri currentMemberUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.workout_add_member_activity);

        Intent intent = getIntent();
        currentMemberUri = intent.getData();
        if(currentMemberUri == null){
            setTitle("Add a member");
            invalidateOptionsMenu();  // отключаем
        }else{
            setTitle("Edit the member");
            // ИНИЦИАЛИЗИРУЕМ LOADER
            getSupportLoaderManager().initLoader(EDIT_MEMBER_LOADER,null,this);
        }

        firstNameEditText=findViewById(R.id.firstNameEditText);
        lastNameEditText=findViewById(R.id.lastNameEditText);
        sportEditText=findViewById(R.id.sportEditText);
        genderSpinner=findViewById(R.id.spinner);

        ageEditText=findViewById(R.id.ageEditText);
        timeEditText=findViewById(R.id.timeEditText);
        wNowEditText=findViewById(R.id.wNowEditText);
        wTarEditText=findViewById(R.id.wtarEditText);


        spinnerArrayList = new ArrayList();
        spinnerArrayList.add("Unknow");
        spinnerArrayList.add("Male");
        spinnerArrayList.add("Female");

        spinnerAdapter=new ArrayAdapter(this,android.R.layout.simple_spinner_item,spinnerArrayList);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        genderSpinner.setAdapter(spinnerAdapter);

        genderSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedGender = (String) parent.getItemAtPosition(position);     // (String) - приравниваем значени к String.
                if(!TextUtils.isEmpty(selectedGender)){
                    if(selectedGender.equals("Male")){
                        gender = WorkoutDataContract.MemberEntry.GENDER_MALE;    // ПРИСВАЕВАЕМ ЗНАЧЕНИЕ ИЗ КОНСТАНТ!
                    }else if(selectedGender.equals("Female")){
                        gender = WorkoutDataContract.MemberEntry.GENDER_FEMALE;
                    }else {
                        gender = WorkoutDataContract.MemberEntry.GENDER_UNKNOW;
                    }
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                gender = 0;
            }
        });


    }

    // Создаем вопрос "Точно удалить?"
    private void showDeleteMemberDialog(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Do you want delete the member?");
        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                deleteMember();
            }
        });
        builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if(dialog != null){
                    dialog.dismiss();
                }
            }
        });
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    private void deleteMember(){
        if(currentMemberUri != null){
            int rowsDeleted = getContentResolver().delete(currentMemberUri,null,null);
            if(rowsDeleted == 0){
                Toast.makeText(this, "Fail on delete row in the table", Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(this, "Member is deleted", Toast.LENGTH_LONG).show();
            }
            finish();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.member_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){
            case R.id.save_member:
                saveMember();
                return true;

            case R.id.delete_member:
                showDeleteMemberDialog();
                return true;

            case android.R.id.home:
                NavUtils.navigateUpFromSameTask(this);
                return true;

        }
        return super.onOptionsItemSelected(item);
    }

    private void saveMember(){
        String firstName = firstNameEditText.getText().toString().trim();                                     // trim() -- обрезает все пробелы вначале и в конце строки
        String lastName = lastNameEditText.getText().toString().trim();
        String sport = sportEditText.getText().toString().trim();
        String age = ageEditText.getText().toString().trim();
        String time = timeEditText.getText().toString().trim();
        String wnow = wNowEditText.getText().toString().trim();
        String wtar = wTarEditText.getText().toString().trim();

        //Проверяемб не пустые ли EditText
        if(TextUtils.isEmpty(firstName)){
            Toast.makeText(this, "Enter the first name bl!", Toast.LENGTH_LONG).show();
            return;
        }
        if(TextUtils.isEmpty(lastName)){
            Toast.makeText(this, "Enter the last name bl!", Toast.LENGTH_LONG).show();
            return;
        }
        if(TextUtils.isEmpty(sport)){
            Toast.makeText(this, "Enter the sport bl!", Toast.LENGTH_LONG).show();
            return;
        }
        if(TextUtils.isEmpty(age)){
            Toast.makeText(this, "Enter the age bl!", Toast.LENGTH_LONG).show();
            return;
        }
        //Проверяемб не пустые ли EditText


        ContentValues contentValues = new ContentValues();
        contentValues.put(WorkoutDataContract.MemberEntry.COLUMN_FIRST_NAME,firstName);          // расписываем значения по колонкам
        contentValues.put(WorkoutDataContract.MemberEntry.COLUMN_LAST_NAME,lastName);
        contentValues.put(WorkoutDataContract.MemberEntry.COLUMN_SPORT,sport);
        contentValues.put(WorkoutDataContract.MemberEntry.COLUMN_GENDER,gender);
        contentValues.put(WorkoutDataContract.MemberEntry.COLUMN_AGE,age);
        contentValues.put(WorkoutDataContract.MemberEntry.COLUMN_TIME,time);
        contentValues.put(WorkoutDataContract.MemberEntry.COLUMN_WNOW,wnow);
        contentValues.put(WorkoutDataContract.MemberEntry.COLUMN_WTAR,wtar);

        //Делаем проверку. Если есть такой - обновляем, если НЕТ - вписываем новую строку
        if(currentMemberUri == null){
            ContentResolver contentResolver = getContentResolver();        // определяем какой contentprovider использовать в зависимости от authority
            Uri uri = contentResolver.insert(WorkoutDataContract.MemberEntry.CONTENT_URI,contentValues);

            if (uri == null){
                Toast.makeText(this, "insert data error", Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(this, "Data saved", Toast.LENGTH_LONG).show();
            }
        } else {
            int rowsChanged = getContentResolver().update(currentMemberUri,contentValues,null,null);
            if(rowsChanged == 0){   // Если ничего не было изменено
                Toast.makeText(this, "Saving data failed", Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(this, "Member updated", Toast.LENGTH_LONG).show();
            }
        }
    }

    // Пишем метод для извечения данных и заполнение полей при изменении существующей строки
    @NonNull
    @Override
    public Loader<Cursor> onCreateLoader(int id, @Nullable Bundle args) {

        String[] projection ={
                WorkoutDataContract.MemberEntry.ID,
                WorkoutDataContract.MemberEntry.COLUMN_FIRST_NAME,
                WorkoutDataContract.MemberEntry.COLUMN_LAST_NAME,
                WorkoutDataContract.MemberEntry.COLUMN_GENDER,
                WorkoutDataContract.MemberEntry.COLUMN_AGE,
                WorkoutDataContract.MemberEntry.COLUMN_TIME,
                WorkoutDataContract.MemberEntry.COLUMN_WNOW,
                WorkoutDataContract.MemberEntry.COLUMN_WTAR,
                WorkoutDataContract.MemberEntry.COLUMN_SPORT
        };

        return  new CursorLoader(
                this,
                currentMemberUri,
                projection,
                null,
                null,
                null);
    }

    @Override
    public void onLoadFinished(@NonNull Loader<Cursor> loader, Cursor data) {
        if(data.moveToFirst()){
            // Извлекаем значения
            int firstNameColumnIndex = data.getColumnIndex(WorkoutDataContract.MemberEntry.COLUMN_FIRST_NAME);
            int lastNameColumnIndex = data.getColumnIndex(WorkoutDataContract.MemberEntry.COLUMN_LAST_NAME);
            int genderColumnIndex = data.getColumnIndex(WorkoutDataContract.MemberEntry.COLUMN_GENDER);
            int ageColumnIndex = data.getColumnIndex(WorkoutDataContract.MemberEntry.COLUMN_AGE);
            int timeColumnIndex = data.getColumnIndex(WorkoutDataContract.MemberEntry.COLUMN_TIME);
            int wnowColumnIndex = data.getColumnIndex(WorkoutDataContract.MemberEntry.COLUMN_WNOW);
            int wtarColumnIndex = data.getColumnIndex(WorkoutDataContract.MemberEntry.COLUMN_WTAR);
            int sportColumnIndex = data.getColumnIndex(WorkoutDataContract.MemberEntry.COLUMN_SPORT);

            // Переводим значения в строки
            String firstName = data.getString(firstNameColumnIndex);
            String lastName = data.getString(lastNameColumnIndex);
            int genderName = data.getInt(genderColumnIndex);
            String age = String.valueOf(data.getInt(ageColumnIndex));
            String time = String.valueOf(data.getInt(timeColumnIndex));
            String wnow = String.valueOf(data.getInt(wnowColumnIndex));
            String wtar = String.valueOf(data.getInt(wtarColumnIndex));
            String sportName = data.getString(sportColumnIndex);

            //Устанавливаем поля в EditText
            firstNameEditText.setText(firstName);
            lastNameEditText.setText(lastName);
            sportEditText.setText(sportName);
            ageEditText.setText(age);
            if(time != "0"){
                timeEditText.setText(time);
            }
            if(wnow != "0"){
                wNowEditText.setText(wnow);
            }
            if(wtar != "0"){
                wTarEditText.setText(wtar);
            }

            switch(genderName){
                case WorkoutDataContract.MemberEntry.GENDER_MALE:
                    genderSpinner.setSelection(1);
                    break;
                case WorkoutDataContract.MemberEntry.GENDER_FEMALE:
                    genderSpinner.setSelection(2);
                    break;
                case WorkoutDataContract.MemberEntry.GENDER_UNKNOW:
                    genderSpinner.setSelection(0);
                    break;
            }
        }
    }

    @Override
    public void onLoaderReset(@NonNull Loader<Cursor> loader) {

    }
    // Пишем метод для извечения данных и заполнение полей при изменении существующей строки
}