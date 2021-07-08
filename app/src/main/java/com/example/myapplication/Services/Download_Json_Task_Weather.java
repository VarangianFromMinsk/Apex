package com.example.myapplication.Services;


import android.os.AsyncTask;
import com.example.myapplication.main.Screens.Dashboard_MVP.Dashboard_view;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class Download_Json_Task_Weather extends AsyncTask<String, Void, String> {

    private Dashboard_view view ;

    public void setView(Dashboard_view view) {
        this.view = view;
    }

    @Override
    protected String doInBackground(String... strings) {
        URL url = null;
        HttpURLConnection urlConnection = null;
        StringBuilder result = new StringBuilder();
        try {
            url = new URL(strings[0]);
            urlConnection = (HttpURLConnection) url.openConnection();
            InputStream in = urlConnection.getInputStream();
            InputStreamReader reader = new InputStreamReader(in);
            BufferedReader bufferedReader = new BufferedReader(reader);
            String line = bufferedReader.readLine();

            while(line != null){
                result.append(line);
                line = bufferedReader.readLine();
            }

            return result.toString();

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if(urlConnection != null){
                urlConnection.disconnect();
            }
        }

        return null;
    }

    @Override
    protected void onPostExecute(String s) {
        super.onPostExecute(s);

        String name_from_Api = "no match";
        String temp_from_Api = "no match";
        String correct_temp = "";
        String description_from_Api = "no match";
        String currentWeatherImage = App_Constants.SUN;

        try {
            JSONObject jsonObject = new JSONObject(s);

            //TODO: get ready string
            name_from_Api = jsonObject.getString("name");
            if(!name_from_Api.equals("")){
                view.saveCurrentTown(name_from_Api);
            }

            //TODO: get another jsonObject
            JSONObject main = jsonObject.getJSONObject("main");
            temp_from_Api  = main.getString("temp");
            if(!temp_from_Api.contains("-")){
                correct_temp = String.valueOf("+" + temp_from_Api);
            }

            //TODO: get jsonArray from which we will get jsonObject
            JSONArray jsonArray = jsonObject.getJSONArray("weather");
            JSONObject weather = jsonArray.getJSONObject(0);
            description_from_Api = weather.getString("description");

            if(description_from_Api.contains("rain")){
                currentWeatherImage = App_Constants.RAIN;
            }
            else if(description_from_Api.contains("cloud") && description_from_Api.contains("sun") ){
                currentWeatherImage = App_Constants.CLOUD_SUN;
            }
            else if(description_from_Api.contains("cloud")){
                currentWeatherImage = App_Constants.CLOUD;
            }
            else{
                currentWeatherImage = App_Constants.SUN;
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        //TODO: set values
        view.loadWeatherComplete(name_from_Api, correct_temp, description_from_Api, currentWeatherImage);
    }

}
