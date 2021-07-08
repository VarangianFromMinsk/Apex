package com.example.myapplication.Services;


public class App_Constants {

    //TODO: staff for cloud messaging
    public static final String CHANNEL_ID_FIREBASE_CLOUD_MESSAGING = "17";
    public static final String NOTIFICATION_URL = "https://fcm.googleapis.com/fcm/send";;
    public static final String SERVER_KEY = "AAAA3F8hdzM:APA91bFKnqWRqsRnYciDh51dlaP-kuinLGXqy6OfLdSvbhI7Uf6r3fnb1aMAP_jEanq0ohjy-kQ9LqZcVfNrVm8fzOvSYd-IN4IMuBOlN2Gs0xOZoaaB8kJM5vWrsHGZQkUtkWeiXTjW";

    //TODO: Chat_Main_Activity
    public static final int READING_REQUEST_CODE = 5;
    public static final int CAMERA_REQUEST_CODE = 6;
    public static final int WRITE_REQUEST_CODE = 7;
    public static final int RECORDING_REQUEST_CODE = 8;
    //values for pick image
    //image pick constants
    public static final int IMAGE_PICK_CAMERA_CODE = 9;
    public static final int IMAGE_PICK_GALLERY_CODE = 10;

    //TODO: Add_Change_Post_Activity
    public static final int READING_REQUEST_CODE_POST = 11;
    public static final int CAMERA_REQUEST_CODE_POST = 12;
    public static final int WRITE_REQUEST_CODE_POST = 13;
    //image pick constants
    public static final int IMAGE_PICK_CAMERA_CODE_POST = 14;
    public static final int IMAGE_PICK_GALLERY_CODE_POST = 15;

    //TODO: User_Profile_Activity
    public static  final int IMAGE_PICK_BACKGROUND_CAMERA_PROFILE = 16;
    public static  final int IMAGE_PICK_BACKGROUND_GALLERY_PROFILE = 186;

    public static final int IMAGE_PICK_CAMERA_CODE_PROFILE = 17;
    public static final int IMAGE_PICK_GALLERY_CODE_PROFILE = 18;

    public static final int READING_REQUEST_CODE_PROFILE = 19;
    public static final int CAMERA_REQUEST_CODE_PROFILE = 20;
    public static final int WRITE_REQUEST_CODE_PROFILE = 21;

    public static final int REQUESTCODE_LOCATION_FIRST = 22;
    public static final int REQUESTCODE_LOCATION_LAST= 23;

    //TODO: Find_Location
    public static final int CHECK_SETTINGS_CODE = 51;
    public static final int REQEST_LOCATION_PERMISSION= 52;

    //TODO: MusicPlayer
    public static final String CHANNEL_ID = "channelMusic";
    public static final String ACTION_PREVIOUS = "actionPrevious";
    public static final String ACTION_PLAY = "actionPlay";
    public static final String ACTION_NEXT  = "actionNext";

    //TODO: Add_Music
    public static final int SONG_REQUEST_CODE = 293;
    public static final int ALBUM_REQUEST_CODE = 294;

    //TODO: weatherApi
    public static final String API_KEY_WEATHER = "93cc3b4fae08e22f2504523de02a6f20";
    public static final String API_WATCHER = "http://api.openweathermap.org/data/2.5/weather?q=@city&appid=93cc3b4fae08e22f2504523de02a6f20&lang=ru&units=metric";
    //images
    public static final String RAIN  = "https://firebasestorage.googleapis.com/v0/b/gachichat-88c3c.appspot.com/o/Weather_Icon%2Frain.jpg?alt=media&token=688a970d-1d76-4f83-8349-df2119efeea8";
    public static final String CLOUD_SUN = "https://firebasestorage.googleapis.com/v0/b/gachichat-88c3c.appspot.com/o/Weather_Icon%2Fsun_and_cloudy.jpg?alt=media&token=136db604-aa55-4cf2-97ce-45f9515f5f1f";
    public static final String CLOUD = "https://firebasestorage.googleapis.com/v0/b/gachichat-88c3c.appspot.com/o/Weather_Icon%2Fcloudy.jpg?alt=media&token=0d00f4e7-50c4-4a6a-8f51-0b64205e2314";
    public static final String SUN = "https://firebasestorage.googleapis.com/v0/b/gachichat-88c3c.appspot.com/o/Weather_Icon%2Fsunny.jpg?alt=media&token=2b64949c-a0f0-46bf-923d-a5f566008828";

    //TODO: MovieApi
    public static final String URL_KINO_DEF_REQUEST = "https://www.omdbapi.com/?apikey=e1299459&s=Attack on titan";
    public static final String URL_REQUEST_KINO = "https://www.omdbapi.com/?apikey=e1299459&s=";
    public static final String URL_REQUEST_SINGLE_TITLE = "https://www.omdbapi.com/?apikey=e1299459&i=";


}
