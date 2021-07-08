package com.example.myapplication.Services;

import android.content.Context;
import android.content.res.Configuration;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class Online_Offline_Service {
    public void updateUserStatus(String state, Context context){
        String saveCurrentDate, saveCurrentTime;

        //staff to onile/offline
        FirebaseAuth auth = FirebaseAuth.getInstance();
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference usersDatabaseReference = database.getReference().child("users");

        //create check for locale
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
        usersDatabaseReference.child(key).child("timeonline").setValue(saveCurrentTime);
    }
}
