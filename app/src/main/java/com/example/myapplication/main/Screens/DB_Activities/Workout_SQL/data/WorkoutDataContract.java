package com.example.myapplication.main.Screens.DB_Activities.Workout_SQL.data;


import android.content.ContentResolver;
import android.net.Uri;
import android.provider.BaseColumns;


public final class WorkoutDataContract  {
    private WorkoutDataContract(){

    }
    public static final int DATABASE_VERSION = 2;
    public static final String DATABASE_NAME = "membersDB";

    public static final String SCHEME = "content://";
    public static final String AUTHORITY = "com.example.myapplication";
    public static final String PATH_MEMBERS = "members";

    public static final Uri BASE_CONTENT_URI = Uri.parse(SCHEME + AUTHORITY);

    public static final class MemberEntry implements BaseColumns {     // Создали отдельный класс, тут можно будет создать еще один класс
        // Обьявляем константы
        public static final String TABLE_NAME = "members";

        public static final String ID =BaseColumns._ID;          // Создали константы для колонок
        public static final String COLUMN_FIRST_NAME = "firstName";
        public static final String COLUMN_LAST_NAME = "lastName";
        public static final String COLUMN_GENDER= "gender";
        public static final String COLUMN_SPORT = "sport";
        public static final String COLUMN_AGE = "age";
        public static final String COLUMN_TIME = "time";
        public static final String COLUMN_WNOW = "wnow";
        public static final String COLUMN_WTAR = "wtar";

        public static final int GENDER_UNKNOW = 0;
        public static final int GENDER_MALE= 1;
        public static final int GENDER_FEMALE = 2;

        public static final Uri CONTENT_URI = Uri.withAppendedPath(BASE_CONTENT_URI, PATH_MEMBERS);

        // Создаем константы лоя метода GetType
        // для возвращение MIME для всей таблицы (MIME - стандартизированные формы всех типов данных типо JPEG, Wave и тд)
        public static final String CONTENT_MULTIPLE_ITEMS = ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + AUTHORITY + "/" + PATH_MEMBERS;
        // для возвращение MIME для конкретной строки
        public static final String CONTENT_SINGLE_ITEM = ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + AUTHORITY + "/" + PATH_MEMBERS;

    }
}
