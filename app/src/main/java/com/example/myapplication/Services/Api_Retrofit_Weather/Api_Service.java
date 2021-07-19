package com.example.myapplication.Services.Api_Retrofit_Weather;

import com.example.myapplication.Services.Weather_Use_By_POJO.Weather_Response;

import io.reactivex.Observable;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface Api_Service {
    //TODO: example "weather?q=Minsk&appid=93cc3b4fae08e22f2504523de02a6f20&units=metric"
    @GET("weather")
    Observable <Weather_Response> getWeather(@Query("appid") String apiKey, @Query("q") String city, @Query("units") String type);
}
