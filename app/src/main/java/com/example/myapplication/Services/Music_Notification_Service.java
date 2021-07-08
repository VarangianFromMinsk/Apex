package com.example.myapplication.Services;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class Music_Notification_Service extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        context.sendBroadcast(new Intent("APEX_TRACKS").putExtra("musicAction", intent.getAction()));
    }
}
