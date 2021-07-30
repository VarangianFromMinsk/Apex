package com.example.myapplication.Services;


import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Build;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaSessionCompat;


import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;


import com.example.myapplication.R;
import com.example.myapplication.main.Models.Model_Song;
import com.example.myapplication.main.Screens.Music.Music_List_Activity_MVVM.Music_List_Activity;

import java.util.concurrent.ExecutionException;

public class Create_Music_Notification {


    public static Notification notification;
    private static Bitmap icon;


    public static void createNotification(Context context, Model_Song model_song, int playButton, int position, int size){

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            //start staff
            NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat.from(context);
            //NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            MediaSessionCompat mediaSessionCompat = new MediaSessionCompat(context, "apex");

            mediaSessionCompat.setActive(true);

            mediaSessionCompat.setMetadata(
                    new MediaMetadataCompat.Builder()
                            .build()
            );

            //Btns
            int drw_previous;
            drw_previous = R.drawable.notification_back;

            int drw_play;
            drw_play = R.drawable.notification_play;

            int drw_next;
            drw_next = R.drawable.notification_forward;


            //PREVIOUS
            PendingIntent pendingIntentPrevious;
            if(position == 0) {
                pendingIntentPrevious = null;
                drw_previous = R.drawable.notification_back_disable;
            }
            else{
                Intent intentPrevious = new Intent(context, Music_Notification_Service.class).setAction(App_Constants.ACTION_PREVIOUS);
                pendingIntentPrevious = PendingIntent.getBroadcast(context, 0, intentPrevious, PendingIntent.FLAG_UPDATE_CURRENT);
                drw_previous = R.drawable.notification_back;
            }


            //PLAY_PAUSE
            Intent intentPlay = new Intent(context, Music_Notification_Service.class).setAction(App_Constants.ACTION_PLAY);
            PendingIntent pendingIntentPlay = PendingIntent.getBroadcast(context, 0, intentPlay, PendingIntent.FLAG_UPDATE_CURRENT);
            if(playButton == 2){
                drw_play = R.drawable.notification_pause;
            }
            else{
                drw_play = R.drawable.notification_play;
            }


            //NEXT
            PendingIntent pendingIntentNext;
            if(position == size){
                pendingIntentNext = null;
                drw_next = R.drawable.notification_forwad_disable;
            }
            else{
                Intent intentNext = new Intent(context, Music_Notification_Service.class).setAction(App_Constants.ACTION_NEXT);
                pendingIntentNext = PendingIntent.getBroadcast(context, 0, intentNext, PendingIntent.FLAG_UPDATE_CURRENT);
                drw_next = R.drawable.notification_forward;
            }

            //intent to enter in musicPlayer
            //use sharedPreferences to open MusicPlaey without list
            Intent musicIntent = new Intent(context, Music_List_Activity.class);
            PendingIntent resultPendingIntent = PendingIntent.getActivity(context, 0, musicIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT);

            Download_Image_Task downloadImage = new Download_Image_Task();

            try {
                icon = downloadImage.execute(model_song.getAlbumUrl()).get();
            } catch (ExecutionException | InterruptedException e) {
                e.printStackTrace();
            }


            //Создаем уведомление в канал
             notification = new NotificationCompat.Builder(context, App_Constants.CHANNEL_ID)
                    .setSmallIcon(R.drawable.apex_512)
                    .setVisibility(androidx.core.app.NotificationCompat.VISIBILITY_PUBLIC)
                    .setContentTitle(model_song.getSongMainTitle())
                    .setContentText(model_song.getSongLastTitle())
                    .setLargeIcon(icon)
                    .setAutoCancel(true)
                    .setNotificationSilent()
                    .setBadgeIconType(androidx.core.app.NotificationCompat.BADGE_ICON_NONE)
                    .setContentIntent(resultPendingIntent)
                    .addAction(drw_previous,"Prev", pendingIntentPrevious)
                    .addAction(drw_play, "Play", pendingIntentPlay)
                    .addAction(drw_next, "Next", pendingIntentNext)
                    .setPriority(NotificationCompat.PRIORITY_LOW)
                    .setStyle(new androidx.media.app.NotificationCompat.MediaStyle()
                            .setShowActionsInCompactView(0, 1, 2)
                            .setMediaSession(mediaSessionCompat.getSessionToken()))
                    .build();
            notificationManagerCompat.notify(5,notification);
        }

    }

 }


