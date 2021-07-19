package com.example.myapplication.Services.Api_Retrofit_Weather;

import com.jakewharton.retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class Api_Factory {

    private static Api_Factory apiFactory;
    private static Retrofit retrofit;
    private static final String BASE_URL = "https://api.openweathermap.org/data/2.5/";

    private Api_Factory(){
        retrofit = new Retrofit.Builder()
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .baseUrl(BASE_URL)
                .build();
    }

    public static Api_Factory getInstance() {
        if(apiFactory == null){
            apiFactory = new Api_Factory();
        }
        return apiFactory;
    }


    public Api_Service getApiService(){
        return retrofit.create(Api_Service.class);
    }
}
