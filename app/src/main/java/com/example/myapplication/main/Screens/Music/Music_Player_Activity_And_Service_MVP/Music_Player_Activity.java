package com.example.myapplication.main.Screens.Music.Music_Player_Activity_And_Service_MVP;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.palette.graphics.Palette;
import androidx.preference.PreferenceManager;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.myapplication.main.Screens.Dashboard_MVP.Dashboard_Activity;
import com.example.myapplication.R;
import com.example.myapplication.Services.App_Constants;
import com.example.myapplication.Services.Download_Image_Task;
import com.example.myapplication.Services.Online_Offline_Service;
import com.example.myapplication.main.Screens.Settings.Settings_Activity;
import com.example.myapplication.main.Models.Model_Song;
import com.example.myapplication.main.Screens.Music.Music_List_Activity_MVVM.Music_List_Activity;
import com.example.myapplication.Services.Create_Music_Notification;
import com.google.firebase.auth.FirebaseAuth;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Objects;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

public class Music_Player_Activity extends AppCompatActivity implements SharedPreferences.OnSharedPreferenceChangeListener, Playable, Player_view{

    //TODO: временно
    private ArrayList<Model_Song> songList = new ArrayList<>();

    //todo: main
    private MediaPlayer MusicPlayer;
    private SeekBar seekbar;
    private int number;
    private final String[] addresses = { "VarangianMinsk@yandex.ru"};
    private Uri attachment;
    private TextView title, littletitle;
    private ImageView mainAlbumImage;
    private TextView timeUpDate, timeDuration;
    private ImageButton startPauseBtn, prevSong, nextSong, loop, likeButton;

    //todo: support btns
    private ImageButton homeBtn, settingsBtn, randomSongBtn, backToListBtn, shareSongBtn;
    private Button hqBtn;

    private SharedPreferences sharedPreferences, mSettings;

    private boolean randomSongBoolean = false, pauseSong = false, isLooping = false;

    //todo: values for current song
    private Intent intent;
    private String currentAlbumUrl, currentMusicUrl, mainTitle, lastTitle, currentMusicDuration;

    //todo: staff for like
    private String myUid;


    //todo: notification
    private NotificationManager notificationManager;
    boolean isPlaying = false;
    int currentStateBtn;

    //todo: on deleting
    public static final String APP_PREFERENCES = "mySettingsInPlayer";

    private Player_Presenter presenter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.music_player_activity);

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        Objects.requireNonNull(getSupportActionBar()).hide();

        presenter = new Player_Presenter(this);

        initialization();

        initSharedPreferences();

        getMainIntent();

        loadDataAndCreatePlayer();

        initNextSongBtn();

        initPrevSongBtn();

        initPlayPauseBtn();

        SeekBar();

        likeSong();

        //todo: init support Btns
        GoToSettings();

        GoToHomePage();

        initLoopSongBtn();

        HQBtn();

        randomSong();

        ShareBtn();

        toMusicList();

        initNotification();

        updateUserStatus("online");
    }

    private void initialization(){

        title = findViewById(R.id.textMainTitle);
        littletitle = findViewById(R.id.textLastTitle);
        loop=findViewById(R.id.loopBtn);
        mainAlbumImage = findViewById(R.id.imageAlbum);
        likeButton=findViewById(R.id.likeBtnMusic);
        prevSong=findViewById(R.id.prevBtn);
        nextSong=findViewById(R.id.nextBtn);
        startPauseBtn= findViewById(R.id.startPauseBtn);
        homeBtn = findViewById(R.id.ActBarBut);
        settingsBtn = findViewById(R.id.ActBarSett);
        hqBtn = findViewById(R.id.HQbtn);
        randomSongBtn = findViewById(R.id.randomBtn);
        backToListBtn = findViewById(R.id.listBtn);
        shareSongBtn = findViewById(R.id.shareMusicBtn);
        timeUpDate = findViewById(R.id.updateTime);
        timeDuration = findViewById(R.id.AllTime);
        seekbar = findViewById(R.id.seekBarInMusic);

        intent = getIntent();

        myUid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        mSettings = getSharedPreferences(APP_PREFERENCES, Context.MODE_PRIVATE);
    }

    private void initSharedPreferences(){
        //todo:  РЕГИСТРИРУЕМ наш SharedPreferencesListner и звук для SettingsFragment!
        sharedPreferences= PreferenceManager.getDefaultSharedPreferences(this);
        sharedPreferences.registerOnSharedPreferenceChangeListener(this);
        sharedPreferences= PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
    }

    //todo:  main music method
    public void getMainIntent() {

        Bundle extras = intent.getExtras();
        if (extras != null) {
            if (intent != null) {
                number = intent.getIntExtra("Position", 0);
                currentAlbumUrl = intent.getStringExtra("ImageUrl");
                mainTitle = intent.getStringExtra("mainTitle");
                lastTitle = intent.getStringExtra("lastTitle");
                currentMusicUrl = intent.getStringExtra("songUrl");
                currentMusicDuration = intent.getStringExtra("songDuration");
            }
        }
        else{
            startActivity(new Intent(Music_Player_Activity.this, Music_List_Activity.class));
        }


    }

    private void loadDataAndCreatePlayer() {

        presenter.loadMusicList();

        setBackColour(currentAlbumUrl);

        //todo: load album banner
        try{
            Glide.with(mainAlbumImage.getContext()).load(currentAlbumUrl).into(mainAlbumImage);
        }catch (Exception ignored){}

        title.setText(mainTitle);
        littletitle.setText(lastTitle);
        timeDuration.setText(currentMusicDuration) ;

        //todo: player check
        if(MusicPlayer != null){
            MusicPlayer.stop();
            MusicPlayer.release();
            MusicPlayer = null;
        }

        MusicPlayer = new MediaPlayer();
        try {
            MusicPlayer.setDataSource(currentMusicUrl);
        }catch (Exception ignored){}

        MusicPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                try {
                    seekbar.setMax(MusicPlayer.getDuration());
                }catch (Exception ignored){}
            }
        });
        MusicPlayer.prepareAsync();

        MusicPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                NextSong();
            }
        });

        saveCurrentTrackSharedPreference();
    }

    @Override
    public void dataLoadComplete(ArrayList<Model_Song> songListPresenter) {

        //todo: main equation!!!
        songList = songListPresenter;
        //todo: main equation!!!

        //todo: like path
        likeButton.setImageResource(R.drawable.likeoff);
        String id = songList.get(number).getUploadId();
        setOrGetLikes(id);

        //todo: notification
        notificationPlayPauseBtn();
        Create_Music_Notification.createNotification(Music_Player_Activity.this, songList.get(number),currentStateBtn, number, songList.size()-1);

        // saveCurrentTrackSharedPreference();
    }

    private void initNextSongBtn(){
        nextSong.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                NextSong();
            }
        });
    }

    private void initPrevSongBtn() {
        prevSong.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PrevSong();
            }
        });

    }

    private void initPlayPauseBtn(){
        startPauseBtn.setEnabled(false);

        //wait 1 sec
        Thread thread = new Thread() {
            @Override
            public void run() {
                try {
                    synchronized (this) {
                        wait(500);

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                startPauseBtn.setEnabled(true);
                            }
                        });

                    }
                } catch (InterruptedException ignored) {

                }
            }

        };
        thread.start();


        startPauseBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    if (MusicPlayer.isPlaying()) {
                        pauseSong = true;
                        MusicPlayer.pause();

                        //notification part
                        supportTrackPause();

                    } else {
                        pauseSong = false;
                        startPauseBtn.setImageResource(R.drawable.stop);
                        if (MusicPlayer != null) {
                            MusicPlayer.start();
                        }
                        //notification part
                        supportTrackPlay();
                    }
                } catch (Exception ignored) {
                }
            }
        });
    }

    private void NextSong() {

        //todo; first part of player
        if(MusicPlayer != null){
            MusicPlayer.stop();
            MusicPlayer.release();
            nextSong.setEnabled(true);
            prevSong.setEnabled(true);
        }

        //todo: list
        if(randomSongBoolean){
            final int min = 0;
            final int max = 3;
            final int random = new Random().nextInt((max - min) + 1) + min;
            number=number+random;
        }
        if(number==songList.size()-1 && !sharedPreferences.getBoolean("player_switch_2",false)){
            Toast.makeText(this, "Its end, check settings", Toast.LENGTH_SHORT).show();
            nextSong.setEnabled(false);
            AnimateButton();
            startPauseBtn.setImageResource(R.drawable.start);
            String last=songList.get(songList.size()-1).getSongUrl();
            try {
                MusicPlayer.setDataSource(last);
                MusicPlayer.prepareAsync();
            } catch (IOException e) {
                e.printStackTrace();
            }

            changePlayPauseBtn();

            saveCurrentTrackSharedPreference();
        }else{

            //todo: get next position from list
            number=((number+1) % songList.size());

            //todo: update UI
            String currentAlbumUrlCD, currentMusicUrlCD, mainTitleCD, lastTitleCD, currentMusicDuration;
            currentAlbumUrlCD = songList.get(number).getAlbumUrl();
            currentMusicUrlCD = songList.get(number).getSongUrl();
            mainTitleCD = songList.get(number).getSongMainTitle();
            lastTitleCD = songList.get(number).getSongLastTitle();
            currentMusicDuration = songList.get(number).getSongDuration();

            title.setText(mainTitleCD);
            littletitle.setText(lastTitleCD);
            try {
                timeDuration.setText(currentMusicDuration) ;
            }catch (Exception ignored){}
            try{
                Glide.with(mainAlbumImage.getContext()).load(currentAlbumUrlCD).into(mainAlbumImage);
            }catch (Exception ignored){}

            setBackColour(currentAlbumUrlCD);

            nextSong.setEnabled(true);

            AnimateButton();

            //todo: music player second part
            MusicPlayer = new MediaPlayer();
            try {
                MusicPlayer.setDataSource(currentMusicUrlCD);
            } catch (IOException e) {
                e.printStackTrace();
            }
            MusicPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mp) {
                    seekbar.setMax(MusicPlayer.getDuration());
                    if(pauseSong){
                        startPauseBtn.setImageResource(R.drawable.start);
                    }
                    else{
                        mp.start();
                    }
                }
            });
            MusicPlayer.prepareAsync();

            MusicPlayer.setLooping(isLooping);

            MusicPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    if(seekbar.getProgress() == seekbar.getMax()){
                        NextSong();
                    }
                }
            });

            changePlayPauseBtn();

            //todo: first part for likes
            likeButton.setImageResource(R.drawable.likeoff);
            String id = songList.get(number).getUploadId();
            //todo: second part to likes
            setOrGetLikes(id);

            //todo: part for notification
            supportTrackNext();

            saveCurrentTrackSharedPreference();
        }

    }

    public void PrevSong() {

        if(number == 0){
            Toast.makeText(this, "Its First track", Toast.LENGTH_SHORT).show();
            MusicPlayer.pause();
            startPauseBtn.setImageResource(R.drawable.start);
            prevSong.setEnabled(false);
            AnimateButton();

            saveCurrentTrackSharedPreference();

        }else {
            //todo: first part of player
            if(MusicPlayer != null){
                MusicPlayer.stop();
                MusicPlayer.release();
                nextSong.setEnabled(true);
                prevSong.setEnabled(true);
            }

            number=((number-1) % songList.size());

            //todo: update UI
            String currentAlbumUrlCD, currentMusicUrlCD, mainTitleCD, lastTitleCD;
            currentAlbumUrlCD = songList.get(number).getAlbumUrl();
            currentMusicUrlCD = songList.get(number).getSongUrl();
            mainTitleCD = songList.get(number).getSongMainTitle();
            lastTitleCD = songList.get(number).getSongLastTitle();

            title.setText(mainTitleCD);
            littletitle.setText(lastTitleCD);
            try {
                timeDuration.setText(currentMusicDuration) ;
            }catch (Exception ignored){}
            try{
                Glide.with(mainAlbumImage.getContext()).load(currentAlbumUrlCD).into(mainAlbumImage);
            }catch (Exception ignored){}

            setBackColour(currentAlbumUrlCD);

            prevSong.setEnabled(true);

            AnimateButton();

            //todo: music player second part
            MusicPlayer = new MediaPlayer();
            try {
                MusicPlayer.setDataSource(currentMusicUrlCD);
            } catch (IOException e) {
                e.printStackTrace();
            }

            MusicPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mp) {
                    seekbar.setMax(MusicPlayer.getDuration());
                    if(pauseSong){
                        startPauseBtn.setImageResource(R.drawable.start);
                    }
                    else{
                        mp.start();
                    }
                }
            });

            MusicPlayer.prepareAsync();

            MusicPlayer.setLooping(isLooping);

            MusicPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    if(seekbar.getProgress() == seekbar.getMax()){
                        NextSong();
                    }
                }
            });

            changePlayPauseBtn();


            //todo: first part for likes
            likeButton.setImageResource(R.drawable.likeoff);
            String id = songList.get(number).getUploadId();
            //todo: second part to likes
            setOrGetLikes(id);

            //todo: part for notification
            supportTrackPrevious();

            saveCurrentTrackSharedPreference();
        }

    }

    //todo: likes methods
    private void setOrGetLikes(String id) {
        presenter.showIsLike(id, myUid);
    }

    @Override
    public void showHeartIfLiked(boolean check) {

        if(check){
            likeButton.setImageResource(R.drawable.likeon);
        }
        else{
            likeButton.setImageResource(R.drawable.likeoff);
        }
    }

    private void likeSong() {
        likeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                presenter.likeSong(number, myUid);
            }
        });
    }

    @Override
    public void showToastLike(String likeOrDislike) {
        Toast.makeText(Music_Player_Activity.this, likeOrDislike, Toast.LENGTH_SHORT).show();
    }

    //todo: support music method
    private void initLoopSongBtn(){
        loop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toLoop();
            }
        });
    }

    private void setBackColour(String src){
        SharedPreferences sharedPreferences= PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

        if(sharedPreferences.getBoolean("player_switch_1",true)) {
            //todo: Async download
            Download_Image_Task downloadImage = new Download_Image_Task();
            //todo: set back
            try {
                Bitmap albumBitmap = downloadImage.execute(src).get();
                Palette.from(albumBitmap).generate(new Palette.PaletteAsyncListener() {
                    @Override
                    public void onGenerated(@Nullable Palette palette) {
                        int defaultValue = 0x000000;
                        assert palette != null;
                        int vibrantDark = palette.getDarkVibrantColor(defaultValue); //Один вариант в запас
                        int mutedDark = palette.getDarkMutedColor(defaultValue);
                        View backPlayer = findViewById(R.id.backInPlayer);
                        View backToolBar = findViewById(R.id.mytoolbar);
                        backPlayer.setBackgroundColor(mutedDark);
                        backToolBar.setBackgroundColor(mutedDark);
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                            Window window = getWindow();
                            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
                            window.setStatusBarColor(mutedDark);
                        }
                    }
                });
                // clean its work?? work in onBackground?
                // bitmap.recycle();
            }catch (Exception ignored){}

        }

    }

    private void SeekBar() {
            new Timer().scheduleAtFixedRate(new TimerTask() {
                @Override
                public void run() {
                    try {
                        seekbar.setProgress(MusicPlayer.getCurrentPosition());

                        if(seekbar.getProgress() == seekbar.getMax()){
                            NextSong();
                        }

                    }catch (IllegalStateException ignored){
                    }
                }
            }, 0, 100);

        seekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {  // Пишем метод,чтобы после передвижение ползунка моталлся трек вперед-назад.
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    MusicPlayer.seekTo(progress);  // seekTo - перевести в нужную позицию.
                    if(progress == 100){
                        NextSong();
                    }
                }
                //set updatable time for seekBar
                int minutes = progress / 1000 / 60;
                int seconds = progress / 1000 - (minutes * 60);

                String minutesString = "";
                String secondsString = "";

                if (minutes < 10) {
                    minutesString = "0" + minutes;
                } else {
                    minutesString = "" + minutes;
                }

                if (seconds < 10) {
                    secondsString = "0" + seconds;
                } else {
                    secondsString = "" + seconds;
                }
                timeUpDate.setText(String.valueOf(minutesString + ":" + secondsString));
                //set updatable time for seekBar

                //set time = song duration
                /*try {
                    int allTime=MusicPlayer.getDuration();
                    int minutes2 = allTime / 1000 / 60;
                    int seconds2 = allTime/ 1000 - (minutes2 * 60);

                    String minutesString2 = "";
                    String secondsString2 = "";

                    if (minutes2 < 10) {
                        minutesString2 = "0" + minutes2;
                    } else {
                        minutesString2 = "" + minutes2;
                    }

                    if (seconds2 < 10) {
                        secondsString2 = "0" + seconds2;
                    } else {
                        secondsString2 = "" + seconds2;
                    }

                    timeDuration.setText( minutesString2 + ":" + secondsString2);
                }catch (Exception E){}

                 */


            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });

    }

    private void AnimateButton(){
        if(!nextSong.isEnabled()){     // Анимируем Вперед
            nextSong.setAlpha(0.5f);
        }else if(nextSong.isEnabled()){
            nextSong.setAlpha(1.0f);
        }
        if(!prevSong.isEnabled()){        // Анимируем Назад
            prevSong.setAlpha(0.5f);
        }else if(prevSong.isEnabled()){
            prevSong.setAlpha(1.0f);
        }
    }

    private void notificationPlayPauseBtn(){
        if(MusicPlayer.isPlaying()){
            currentStateBtn = 2;
        }
        else{
            currentStateBtn = 1;
        }
    }

    private void changePlayPauseBtn(){
        if(MusicPlayer.isPlaying()){
            startPauseBtn.setImageResource(R.drawable.start);
        }
        else{
            startPauseBtn.setImageResource(R.drawable.stop);
        }
    }

    private void saveCurrentTrackSharedPreference(){
        boolean play ;
        play = MusicPlayer.isPlaying();
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(Music_Player_Activity.this);
        preferences.edit()
                .putInt("currentPosition", this.number)
                .putBoolean("isPlaying", play)
                .apply();
    }

    //todo: action button
    private void toMusicList() {
        backToListBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent GoToMusicList = new Intent(Music_Player_Activity.this, Music_List_Activity.class);
                startActivity(GoToMusicList);
            }
        });
    }

    private void ShareBtn() {
        shareSongBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String emailText = "Track name - " + mainTitle;
                Intent intent = new Intent(Intent.ACTION_SEND);
                intent.setType("*/*");
                intent.putExtra(Intent.EXTRA_EMAIL, addresses);
                intent.putExtra(Intent.EXTRA_SUBJECT, emailText);
                intent.putExtra(Intent.EXTRA_STREAM, attachment);
                if (intent.resolveActivity(getPackageManager()) != null) {
                    startActivity(intent);
                }
            }
        });

    }

    private void GoToHomePage() {
        homeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent GoToHomePage = new Intent(Music_Player_Activity.this, Dashboard_Activity.class);
                startActivity(GoToHomePage);
            }
        });
    }

    private void toLoop() {
        if(!MusicPlayer.isLooping()){
            Toast.makeText(this, "Looping On", Toast.LENGTH_SHORT).show();
            MusicPlayer.setLooping(true);
            loop.setAlpha(1.0f);
            isLooping = true;
        }else if(MusicPlayer.isLooping()){
            Toast.makeText(this, "Looping Off", Toast.LENGTH_SHORT).show();
            MusicPlayer.setLooping(false);
            loop.setAlpha(0.5f);
            isLooping = false;
        }
    }

    private void randomSong() {   //Рандомный трек
        randomSongBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!randomSongBoolean){
                    randomSongBoolean=true;
                    Toast.makeText(Music_Player_Activity.this, "Random On", Toast.LENGTH_SHORT).show();
                    //showSnackBar("Random On");
                    randomSongBtn.setAlpha(1.0f);

                }else {
                    randomSongBoolean=false;
                    Toast.makeText(Music_Player_Activity.this, "Random Off", Toast.LENGTH_SHORT).show();
                    //  showSnackBar("Random Off");
                    randomSongBtn.setAlpha(0.5f);
                }
            }
        });


    }

    private void HQBtn() {
        hqBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(Music_Player_Activity.this, "Activate your Premium access", Toast.LENGTH_SHORT).show();
            }
        });

    }

    public void GoToSettings() {
        settingsBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent GoToSettings = new Intent(Music_Player_Activity.this, Settings_Activity.class);
                startActivity(GoToSettings);
            }
        });

    }

    //todo: shared preference part
    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {

    }

    //TODO: Block online/offline
    public void updateUserStatus( String state){
        Online_Offline_Service service = new Online_Offline_Service();
        service.updateUserStatus(state, this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateUserStatus("online");
    }

    @Override
    protected void onPause() {
        super.onPause();
        updateUserStatus("offline");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            notificationManager.cancelAll();
        }

        unregisterReceiver(broadcastReceiver);

        sharedPreferences.unregisterOnSharedPreferenceChangeListener(this);
    }


    //TODO: notification
    private void initNotification() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createChannel();
            registerReceiver(broadcastReceiver, new IntentFilter("APEX_TRACKS"));
            startService(new Intent(getBaseContext(), On_Clear_From_Music_Service.class));
        }
    }

    //todo: all staff for music control in notification
    BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getExtras().getString("musicAction");

            switch (action){
                case App_Constants.ACTION_PREVIOUS:
                    //PrevSong(null);
                    onTrackPrevious();
                    break;
                case App_Constants.ACTION_PLAY:
                    if(MusicPlayer.isPlaying()){
                        // MusicPlayer.pause();
                        onTrackPause();
                    }
                    else{
                        //MusicPlayer.start();
                        onTrackPlay();
                    }
                    break;
                case App_Constants.ACTION_NEXT:
                    //NextSong();
                    onTrackNext();

            }
        }
    };

    //1 -play 2-pause
    @Override
    public void onTrackPrevious() {
        PrevSong();
        supportTrackPrevious();
    }

    private void supportTrackPrevious(){
        notificationPlayPauseBtn();
        changePlayPauseBtn();
        Create_Music_Notification.createNotification(Music_Player_Activity.this, songList.get(number), 2, number, songList.size()-1);
    }

    @Override
    public void onTrackPlay() {
        MusicPlayer.start();
        supportTrackPlay();

    }

    private void supportTrackPlay(){
        notificationPlayPauseBtn();
        isPlaying = true;
        startPauseBtn.setImageResource(R.drawable.stop);
        Create_Music_Notification.createNotification(Music_Player_Activity.this, songList.get(number), currentStateBtn, number, songList.size()-1);
    }

    @Override
    public void onTrackPause() {
        MusicPlayer.pause();
        supportTrackPause();
    }

    private void supportTrackPause(){
        notificationPlayPauseBtn();
        isPlaying = false;
        Create_Music_Notification.createNotification(Music_Player_Activity.this, songList.get(number), currentStateBtn, number, songList.size()-1);
        startPauseBtn.setImageResource(R.drawable.start);
    }

    @Override
    public void onTrackNext() {
        NextSong();
        supportTrackNext();
    }

    private void supportTrackNext(){
        notificationPlayPauseBtn();
        changePlayPauseBtn();
        Create_Music_Notification.createNotification(Music_Player_Activity.this, songList.get(number), 2, number, songList.size()-1);
    }

    private void createChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(App_Constants.CHANNEL_ID, "musicChannel", NotificationManager.IMPORTANCE_HIGH);
            channel.setLockscreenVisibility(1);
            channel.enableLights(true);

            notificationManager = getSystemService(NotificationManager.class);
            if(notificationManager != null){
                notificationManager.createNotificationChannel(channel);
            }
        }
    }

}
