package com.example.myapplication.main.Screens.Dashboard_MVP;

import android.util.Log;

import androidx.annotation.NonNull;

import com.example.myapplication.Services.Download_Json_Task_Weather;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.messaging.FirebaseMessaging;

import java.util.HashMap;

public class Dashboard_Presenter {

    private final Dashboard_view view;

    public Dashboard_Presenter(Dashboard_view view) {
        this.view = view;
    }

    public void loadWeather(String city_default){
        Download_Json_Task_Weather downloadJsonTask = new Download_Json_Task_Weather();
        downloadJsonTask.setView(view);
        downloadJsonTask.execute("https://api.openweathermap.org/data/2.5/weather?q="+city_default+"&appid=93cc3b4fae08e22f2504523de02a6f20&units=metric");
    }

    public void getAndLoadToken(){
        FirebaseMessaging.getInstance().getToken()
                .addOnCompleteListener(new OnCompleteListener<String>() {
                    @Override
                    public void onComplete(@NonNull Task<String> task) {
                        if (!task.isSuccessful()) {
                            Log.w("Fcm error", "Fetching FCM registration token failed", task.getException());
                            return;
                        }

                        // Get new FCM registration token
                        String token = task.getResult();

                        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

                        assert user != null;

                        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("users").child(user.getUid());
                        HashMap<String, Object> hashMap = new HashMap<>();
                        hashMap.put("token",token);
                        databaseReference.updateChildren(hashMap);
                    }
                });
    }
}
