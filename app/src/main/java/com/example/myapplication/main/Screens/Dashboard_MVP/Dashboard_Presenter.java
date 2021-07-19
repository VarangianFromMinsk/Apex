package com.example.myapplication.main.Screens.Dashboard_MVP;

import android.util.Log;

import androidx.annotation.NonNull;

import com.example.myapplication.Services.Api_Retrofit_Weather.Api_Factory;
import com.example.myapplication.Services.Api_Retrofit_Weather.Api_Service;
import com.example.myapplication.Services.App_Constants;
import com.example.myapplication.Services.Download_Json_Task_Weather;
import com.example.myapplication.Services.Weather_Use_By_POJO.Weather;
import com.example.myapplication.Services.Weather_Use_By_POJO.Weather_Response;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.messaging.FirebaseMessaging;

import java.util.HashMap;
import java.util.List;

import io.reactivex.Scheduler;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

public class Dashboard_Presenter {

    private final Dashboard_view view;

    private Disposable disposable;

    public Dashboard_Presenter(Dashboard_view view) {
        this.view = view;
    }

    public void loadWeather(String city_default){

        //TODO: first var
        /*
        Download_Json_Task_Weather downloadJsonTask = new Download_Json_Task_Weather();
        downloadJsonTask.setView(view);
        downloadJsonTask.execute("https://api.openweathermap.org/data/2.5/weather?q="+city_default+"&appid=93cc3b4fae08e22f2504523de02a6f20&units=metric");
         */


        //TODO: use retrofit
        Api_Factory api_factory = Api_Factory.getInstance();
        Api_Service api_service = api_factory.getApiService();
        disposable = api_service.getWeather(App_Constants.API_KEY_WEATHER, city_default, "metric" )
                .subscribeOn(Schedulers.computation())  // TODO: в каком потоке выполняем
                .observeOn(AndroidSchedulers.mainThread())  // TODO: в какой поток возвращаем
                .subscribe(new Consumer<Weather_Response>() {
                    @Override
                    public void accept(Weather_Response weather_response) throws Exception {
                        List<Weather> weather = weather_response.getWeather();
                        String description = weather.get(0).getDescription();

                        String Name = weather_response.getName().toString();
                        Log.d("NameCity", Name);

                        String currentWeatherImage = App_Constants.SUN;

                        if(description.contains("rain")){
                            currentWeatherImage = App_Constants.RAIN;
                        }
                        else if(description.contains("cloud") && description.contains("sun") ){
                            currentWeatherImage = App_Constants.CLOUD_SUN;
                        }
                        else if(description.contains("cloud")){
                            currentWeatherImage = App_Constants.CLOUD;
                        }
                        else{
                            currentWeatherImage = App_Constants.SUN;
                        }

                        view.loadWeatherComplete(weather_response.getName(), String.valueOf(weather_response.getMain().getTemp()),description,currentWeatherImage);

                        view.saveCurrentTown(weather_response.getName());

                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {
                        // TODO: для ошибок
                    }
                });


    }

    public void removeApiObserver(){
        if(disposable != null){
            disposable.dispose();
        }
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
