package com.example.myapplication.main.Screens.Sing_In_MVP;


import android.content.res.Configuration;
import android.util.Log;

import androidx.annotation.NonNull;

import com.example.myapplication.main.Models.Model_User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class Sing_In_Presenter {

    private final Sing_In_view view;
    private final DatabaseReference usersDatabaseReference = FirebaseDatabase.getInstance().getReference().child("users");

    public Sing_In_Presenter(Sing_In_view view) {
        this.view = view;
    }

    public void recoverPassword(FirebaseAuth auth, String email){
        auth.sendPasswordResetEmail(email).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                view.recoverPasswordComplete(task.isSuccessful());
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                view.recoverPasswordComplete(false);
            }
        });
    }

    public void registerUser(FirebaseAuth auth, String email, String password, String tag, String name, Sign_In_Activity activity){
        //TODO: register
        auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(activity, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(tag, "createUserWithEmail:success");

                            FirebaseUser user = auth.getCurrentUser();
                            createUser(user,"online", name, activity);

                            view.registerComplete(true);

                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(tag, "createUserWithEmail:failure", task.getException());
                            view.registerComplete(false);

                        }
                    }
                });
    }

    private void createUser(FirebaseUser firebaseUser, String state, String name, Sign_In_Activity activity) {
        String saveCurrentDate, saveCurrentTime;

        //todo: create check for locale
        Locale locale = new Locale("ru");
        Locale.setDefault(locale);
        Configuration config = activity.getResources().getConfiguration();
        config.locale = locale;
        activity.getResources().updateConfiguration(config,
                activity.getResources().getDisplayMetrics());

        Calendar calForDate = Calendar.getInstance();
        SimpleDateFormat currentDate = new SimpleDateFormat("MMM dd, yyyy", locale);
        saveCurrentDate = currentDate.format(calForDate.getTime());

        Calendar calForTime = Calendar.getInstance();
        SimpleDateFormat currentTime = new SimpleDateFormat("hh:mm a", locale);
        saveCurrentTime = currentTime.format(calForTime.getTime());

        Model_User user = new Model_User();
        user.setFirebaseId(firebaseUser.getUid());
        user.setEmail(firebaseUser.getEmail());
        user.setName(name);
        user.setDayOnline(saveCurrentDate);
        user.setTimeonline(saveCurrentTime);
        user.setOnline(state);
        user.setAvatarMockUpResourse("");
        user.setNick("");
        user.setTelephone("");
        user.setBackground("");

        usersDatabaseReference.child(firebaseUser.getUid()).setValue(user);
    }

    public void loginUser(FirebaseAuth auth, String email, String password, String tag, Sign_In_Activity activity){
        auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener( activity, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(tag, "signInWithEmail:success");
                            view.loginComplete(true);

                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(tag, "signInWithEmail:failure", task.getException());
                            view.loginComplete(false);
                        }
                    }
                });
    }
}
