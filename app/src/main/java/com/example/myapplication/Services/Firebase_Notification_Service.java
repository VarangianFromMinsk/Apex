package com.example.myapplication.Services;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;

import android.media.RingtoneManager;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;

import com.example.myapplication.R;
import com.example.myapplication.main.Screens.Chat_Activity_MVP.Chat_Main_Activity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ExecutionException;

public class Firebase_Notification_Service extends FirebaseMessagingService {

    private final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
    private Bitmap avatarBitmap = null;

    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {

        if(remoteMessage.getData().size()>0){

            Map<String, String> map = remoteMessage.getData();
            String title = map.get("title");
            String message = map.get("message");
            String hisId = map.get("hisId");
            String hisImage = map.get("hisImage");


            //check is Chat open
            if(!Chat_Main_Activity.active){
                if(Build.VERSION.SDK_INT > Build.VERSION_CODES.O){
                    createOreoNotification(title, message, hisId, hisImage);
                }
                else{
                    createNormalNotification(title, message, hisId, hisImage);
                }
            }
        }

        super.onMessageReceived(remoteMessage);
    }

    @Override
    public void onDeletedMessages() {
        super.onDeletedMessages();
    }

    @Override
    public void onNewToken(@NonNull String s) {
        if(user != null){
            updateToken(s);
        }
        super.onNewToken(s);
    }


    private void updateToken(String token){

        try {
            assert user != null;

            DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("users").child(user.getUid());
            HashMap<String, Object> hashMap = new HashMap<>();
            hashMap.put("token",token);
            databaseReference.updateChildren(hashMap);
        }catch (Exception ignored){}

    }

    private void showAvatar(String hisImage){
        Download_Image_Task downloadImage = new Download_Image_Task();
        try {
            Bitmap sourceBitmap = downloadImage.execute(hisImage).get();
            int dimension = getSquareCropDimensionForBitmap(sourceBitmap);
            avatarBitmap = ThumbnailUtils.extractThumbnail(sourceBitmap, dimension, dimension);

        } catch (ExecutionException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    public int getSquareCropDimensionForBitmap(Bitmap bitmap) {
        //todo: use the smallest dimension of the image to crop to
        return Math.min(bitmap.getWidth(), bitmap.getHeight());
    }

    private void createNormalNotification(String title, String message, String hisId, String hisImage){

        showAvatar(hisImage);

        Uri uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this,"15");
        builder.setContentTitle(title)
                .setContentText(message)
                .setTicker(null)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setLargeIcon(avatarBitmap)
                .setAutoCancel(true)
                .setSound(uri);

        Intent intent = new Intent(this, Chat_Main_Activity.class);
        intent.putExtra("recipientUserId", hisId);
        intent.putExtra("recipientAvatar", hisImage);
        intent.putExtra("recipientUserName", title);

        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_ONE_SHOT);

        builder.setContentIntent(pendingIntent);

        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        manager.notify(new Random().nextInt(85-65), builder.build());

    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void createOreoNotification(String title, String message, String hisId, String hisImage){

        showAvatar(hisImage);

        NotificationChannel channel = new NotificationChannel(App_Constants.CHANNEL_ID_FIREBASE_CLOUD_MESSAGING, "Message", NotificationManager.IMPORTANCE_DEFAULT);
        channel.setShowBadge(true);
        channel.enableLights(true);
        channel.enableVibration(true);
        channel.setDescription("Message Description");
        channel.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);

        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        manager.createNotificationChannel(channel);

        Intent intent = new Intent(this, Chat_Main_Activity.class);
        intent.putExtra("recipientUserId", hisId);
        intent.putExtra("recipientAvatar", hisImage);
        intent.putExtra("recipientUserName", title);

        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_ONE_SHOT);

        Notification notification = new Notification.Builder(this,App_Constants.CHANNEL_ID_FIREBASE_CLOUD_MESSAGING)
                .setContentTitle(title)
                .setBadgeIconType(R.drawable.apex_512)
                .setContentText(message)
                .setSmallIcon(R.drawable.apex_512)
                .setLargeIcon(avatarBitmap)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .setTicker(null)
                .build();


        manager.notify(new Random().nextInt(85-65), notification);

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }
}
