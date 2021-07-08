package com.example.myapplication.main.Screens.DB_Activities.Workout_SQL.data;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.util.Log;
import android.widget.Toast;


public class WorkoutContentProvider extends ContentProvider {            //!!! ПоСЛЕ СОЗДАНИЯ НУЖНО УКАЗАТЬ PROVIDER в ANDROID MANIFEST!!!

    WorkoutDataOpenHelper dataOpenHelper;  // создаем единицу данного класса

    private static final int MEMBERS = 111;
    private static final int MEMBER_ID = 222;


    // Создаем метод, который будет поределять, работаем с целой таблицей или строкой!
    private static final UriMatcher uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    static{

        uriMatcher.addURI(WorkoutDataContract.AUTHORITY,WorkoutDataContract.PATH_MEMBERS,MEMBERS);                 // для работы со всей таблицей
        uriMatcher.addURI(WorkoutDataContract.AUTHORITY,WorkoutDataContract.PATH_MEMBERS + "/#", MEMBER_ID);    // для работы с одной строкой

    }


    @Override
    public boolean onCreate() {
        dataOpenHelper=new WorkoutDataOpenHelper(getContext());
        return true;
    }

    @Override
    // content://com.example.myapplication/members/34
    // projection = {"lastName","gender"}   --- ЕСЛИ НУЖНО ВЫВЕСТИ ДАННЫЕ ИЗ ОТДЕЛЬНЫХ СТОЛБЦОВ ПО НАШЕМУ ID!
    public Cursor query( Uri uri,  String[] projection,  String selection,  String[] selectionArgs,  String sortOrder) {      //Read метод
        SQLiteDatabase db = dataOpenHelper.getReadableDatabase();
        Cursor cursor;

        int match = uriMatcher.match(uri);

        switch (match){
            case MEMBERS:    // : -- Обозначает что mach соответсвует
                cursor = db.query(WorkoutDataContract.MemberEntry.TABLE_NAME,projection,selection,selectionArgs,null,null,sortOrder);       // Работаем со всеми строками таблицы
                break;

            //selection="_id=?" -- указывается колонка по которой выделаем, и там у нас  BaseColumns._ID;  "ОТБОР"
            // selectionArgs = 34    т к ContentUris.parseId -- преобразует последнте данные после  /  в число, в данном случае 34
            case MEMBER_ID:                                                                                                                                 // Работаем со строкой по ID
                selection = WorkoutDataContract.MemberEntry.ID + "=?";       // Указываем, что будем выбирать запись по столбцу ID нашей таблицы
                selectionArgs = new String[] {String.valueOf(ContentUris.parseId(uri))};
                cursor = db.query(WorkoutDataContract.MemberEntry.TABLE_NAME,projection,selection,selectionArgs,null,null,sortOrder);
                break;

            default:
                Toast.makeText(getContext(), "Incorrect URI", Toast.LENGTH_LONG).show();
                throw new IllegalArgumentException("Can't query incorrect URI" + uri);  // УКАЖЕТ ОШИБКУ!
        }

        // КОГДА ДАННЫЕ БУДУТ ИЗМЕНЯТЬСЯ _ БУДЕМ ЗНАТЬ ЧТО НУЖНО ОБНОВИТЬ cursor
        cursor.setNotificationUri(getContext().getContentResolver(), uri);

        return cursor;
    }

    @Override
    public Uri insert( Uri uri, ContentValues values) {          //Create метод

        // проверка вводимых  данных по типу !
        String firstName = values.getAsString(WorkoutDataContract.MemberEntry.COLUMN_FIRST_NAME);
        if (firstName == null){
            throw new IllegalArgumentException("You have to input first name");
        }

        String lastName = values.getAsString(WorkoutDataContract.MemberEntry.COLUMN_LAST_NAME);
        if (lastName == null){
            throw new IllegalArgumentException("You have to input last name");
        }

        Integer gender = values.getAsInteger(WorkoutDataContract.MemberEntry.COLUMN_GENDER);
        if(gender == null || !(gender == WorkoutDataContract.MemberEntry.GENDER_UNKNOW || gender == WorkoutDataContract.MemberEntry.GENDER_MALE ||gender == WorkoutDataContract.MemberEntry.GENDER_FEMALE)){                                                                                          //     || -- ЗНАЧИТ "ИЛИ"   !!!!
            throw new IllegalArgumentException("You have to input correct gender");
        }

        Integer age = values.getAsInteger(WorkoutDataContract.MemberEntry.COLUMN_AGE);
        if(age == null){
            throw new IllegalArgumentException("You have to input correct age");
        }

        String sportName = values.getAsString(WorkoutDataContract.MemberEntry.COLUMN_SPORT);
        if (sportName == null){
            throw new IllegalArgumentException("You have to input correct sport");
        }
        // проверка вводимых  данных по типу !


        SQLiteDatabase db = dataOpenHelper.getWritableDatabase();

        int match = uriMatcher.match(uri);

        switch (match){
            case MEMBERS:    // : -- Обозначает что mach соответсвует
                long id = db.insert(WorkoutDataContract.MemberEntry.TABLE_NAME,null,values);     // long id -- потому что метод возвращает id числом
                if(id == -1){                                                                                   // -1 -- это если строка не была вставлена,то вернеться значение -1
                    Log.e("insertMethod","insert data error for " + uri);
                    return null;
                }

                // Сигнал для обновления ListView ( через read метод)
                getContext().getContentResolver().notifyChange(uri,null);

                return ContentUris.withAppendedId(uri,id);    // если все правильно врзвращаем это

            default:
                throw new IllegalArgumentException("insert data error for" + uri);  // УКАЖЕТ ОШИБКУ!
        }

    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {

        SQLiteDatabase db = dataOpenHelper.getWritableDatabase();

        int match = uriMatcher.match(uri);

        int rowsDeleted;

        switch (match){
            case MEMBERS:    // : -- Обозначает что mach соответсвует

                rowsDeleted = db.delete(WorkoutDataContract.MemberEntry.TABLE_NAME, selection, selectionArgs);
                break;

            case MEMBER_ID:                                                                                                                                 // Работаем со строкой по ID
                selection = WorkoutDataContract.MemberEntry.ID + "=?";       // Указываем, что будем выбирать запись по столбцу ID нашей таблицы
                selectionArgs = new String[] {String.valueOf(ContentUris.parseId(uri))};
                rowsDeleted = db.delete(WorkoutDataContract.MemberEntry.TABLE_NAME, selection, selectionArgs);
                break;

            default:
                Toast.makeText(getContext(), "Incorrect URI", Toast.LENGTH_LONG).show();
                throw new IllegalArgumentException("Can't delete this URI" + uri);  // УКАЖЕТ ОШИБКУ!
        }

        if(rowsDeleted != 0){
            // Сигнал для обновления ListView ( через read метод)
            getContext().getContentResolver().notifyChange(uri,null);
        }
        return rowsDeleted;
    }

    @Override
    public int update( Uri uri, ContentValues values, String selection, String[] selectionArgs) {

        // проверка вводимых  данных по типу !
        if(values.containsKey(WorkoutDataContract.MemberEntry.COLUMN_FIRST_NAME)){
            String firstName = values.getAsString(WorkoutDataContract.MemberEntry.COLUMN_FIRST_NAME);
            if (firstName == null){
                throw new IllegalArgumentException("You have to input first name");
            }
        }

        if(values.containsKey(WorkoutDataContract.MemberEntry.COLUMN_LAST_NAME)){
            String lastName = values.getAsString(WorkoutDataContract.MemberEntry.COLUMN_LAST_NAME);
            if (lastName == null){
                throw new IllegalArgumentException("You have to input last name");
            }
        }

        if(values.containsKey(WorkoutDataContract.MemberEntry.COLUMN_GENDER)){
            Integer gender = values.getAsInteger(WorkoutDataContract.MemberEntry.COLUMN_GENDER);
            if(gender == null || !(gender == WorkoutDataContract.MemberEntry.GENDER_UNKNOW || gender == WorkoutDataContract.MemberEntry.GENDER_MALE ||gender == WorkoutDataContract.MemberEntry.GENDER_FEMALE)){                                                                                          //     || -- ЗНАЧИТ "ИЛИ"   !!!!
                throw new IllegalArgumentException("You have to input correct gender");
            }
        }

        Integer age = values.getAsInteger(WorkoutDataContract.MemberEntry.COLUMN_AGE);
        if(age == null){
            throw new IllegalArgumentException("You have to input correct age");
        }


        if(values.containsKey(WorkoutDataContract.MemberEntry.COLUMN_SPORT)){
            String sportName = values.getAsString(WorkoutDataContract.MemberEntry.COLUMN_SPORT);
            if (sportName == null){
                throw new IllegalArgumentException("You have to input correct sport");
            }
        }
        // проверка вводимых  данных по типу !


        SQLiteDatabase db = dataOpenHelper.getWritableDatabase();

        int match = uriMatcher.match(uri);

        int rowsUpdated;

        switch (match){
            case MEMBERS:    // : -- Обозначает что mach соответсвует

                // Проверяем были ли обновлены строки
                rowsUpdated= db.update(WorkoutDataContract.MemberEntry.TABLE_NAME, values, selection, selectionArgs);
                break;

            case MEMBER_ID:                                                                                                                                 // Работаем со строкой по ID
                selection = WorkoutDataContract.MemberEntry.ID + "=?";       // Указываем, что будем выбирать запись по столбцу ID нашей таблицы
                selectionArgs = new String[] {String.valueOf(ContentUris.parseId(uri))};

                // Проверяем были ли обновлены строки
                rowsUpdated= db.update(WorkoutDataContract.MemberEntry.TABLE_NAME, values, selection, selectionArgs);
                break;

            default:
                Toast.makeText(getContext(), "Incorrect URI", Toast.LENGTH_LONG).show();
                throw new IllegalArgumentException("Can't update this URI" + uri);  // УКАЖЕТ ОШИБКУ!
        }

        if(rowsUpdated != 0){
            // Сигнал для обновления ListView ( через read метод)
            getContext().getContentResolver().notifyChange(uri,null);
        }

        return rowsUpdated;

    }

    @Override
    public String getType(Uri uri) {

        int match = uriMatcher.match(uri);

        switch (match){
            case MEMBERS:
                return WorkoutDataContract.MemberEntry.CONTENT_MULTIPLE_ITEMS;

            case MEMBER_ID:
                return WorkoutDataContract.MemberEntry.CONTENT_SINGLE_ITEM;

            default:
                Toast.makeText(getContext(), "Incorrect URI", Toast.LENGTH_LONG).show();
                throw new IllegalArgumentException("Error in gatType" + uri);  // УКАЖЕТ ОШИБКУ!
        }
    }
}


// Uri - Unified Resource Indentifier - ПОСТОЯННЫЙ УНИКАЛЬНЫЙ ИНДИФИКАТОР РЕСУРСА
// content://com.example.myapplication/members       code 1
// content://com.example.myapplication/members/34     code 2

//URL - Unified Resource Locator -
//http://google.com

// content://com.android.contacts/contacts     МОЖЕМ ОБРАЩТЬСЯ К РАЗНЫМ КОРНЕВЫМ УТИЛИТАМ АНДРОЙД!
// content://com.android.calendar/events
// content://user_dictionary/words   -- получаем доступ к пользотель
// content:// - scheme