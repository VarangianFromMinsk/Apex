package com.example.myapplication.Services;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;


import androidx.annotation.NonNull;
import androidx.lifecycle.DefaultLifecycleObserver;

import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleObserver;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.OnLifecycleEvent;


import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

import javax.inject.Inject;

public class Online_Offline_User_Service_To_Firebase implements LifecycleObserver {

    private final SharedPreferences preferences;
    private final Context context;

    @Inject
    public Online_Offline_User_Service_To_Firebase(SharedPreferences preferences, Context context) {
        this.preferences = preferences;
        this.context = context;
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
    public void onResume(@NonNull LifecycleOwner owner) {
        updateUserStatus("online", context);
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_PAUSE)
    public void onPause(@NonNull LifecycleOwner owner) {
        updateUserStatus("offline", context);
    }

    //todo: пока статик, потом через даггер и lifeCycle
    public void updateUserStatus(String state, Context context){
        String saveCurrentDate, saveCurrentTime;

        //todo: staff to onile/offline
        FirebaseAuth auth = FirebaseAuth.getInstance();
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference usersDatabaseReference = database.getReference().child("users");

        //todo: create check for locale
        Locale locale = new Locale("ru");
        Locale.setDefault(locale);
        Configuration config = context.getResources().getConfiguration();
        config.locale = locale;
        context.getResources().updateConfiguration(config,
                context.getResources().getDisplayMetrics());

        Calendar calForDate = Calendar.getInstance();
        SimpleDateFormat currentDate = new SimpleDateFormat("MMM dd, yyyy", locale);
        saveCurrentDate = currentDate.format(calForDate.getTime());

        Calendar calForTime = Calendar.getInstance();
        SimpleDateFormat currentTime = new SimpleDateFormat("hh:mm a", locale);
        saveCurrentTime = currentTime.format(calForTime.getTime());

        FirebaseUser user = auth.getCurrentUser();
        String key = "";
        if(user!=null){
            key = user.getUid();
        }

        usersDatabaseReference.child(key).child("online").setValue(state);
        usersDatabaseReference.child(key).child("dayOnline").setValue(saveCurrentDate);
        usersDatabaseReference.child(key).child("timeonline").setValue(saveCurrentTime).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(state.equals("online")) {
                   // preferences.edit().putBoolean("isUserOnline", true).apply();
                }
                else{
                  //  preferences.edit().putBoolean("isUserOnline", false).apply();
                }

            }
        });
    }

}
