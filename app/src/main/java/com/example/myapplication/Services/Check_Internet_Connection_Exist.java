package com.example.myapplication.Services;

import android.content.Context;
import android.net.ConnectivityManager;

public class Check_Internet_Connection_Exist {
    public boolean checkInternet(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        return cm.getActiveNetworkInfo() != null && cm.getActiveNetworkInfo().isConnected();
    }
}
