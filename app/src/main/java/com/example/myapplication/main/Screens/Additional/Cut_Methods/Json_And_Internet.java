package com.example.myapplication.main.Screens.Additional.Cut_Methods;

import androidx.appcompat.app.AppCompatActivity;

import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.concurrent.ExecutionException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import retrofit2.http.Url;

public class Json_And_Internet extends AppCompatActivity {

    private String yandex = "https://yandex.by/";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

         DownloadTask downloadTask = new DownloadTask();

        try {
           String result =  downloadTask.execute(yandex).get();
            Log.i("Url" , result);
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }


        //TODO: work with Strings
        //String m = "Парашютист";
       // Log.i("Sub", m.substring(4,10));

      //  String m = "Mississippi";
      //  Pattern pattern = Pattern.compile("Mi(.*?)pi");
      //  Matcher matcher = pattern.matcher(m);
      //  while(matcher.find()){
      //      Log.i("Sub", matcher.group(1));
       // }

    }

    private  static class DownloadTask extends AsyncTask<String, Void, String>{

        @Override
        protected String doInBackground(String... strings) {
            StringBuilder result = new StringBuilder();
            URL url = null;
            HttpURLConnection urlConnection = null;
            try {
                url = new URL(strings[0]);
                urlConnection = (HttpURLConnection) url.openConnection(); // TODO: we opened connection like a brouser;
                InputStream in = urlConnection.getInputStream();
                InputStreamReader reader = new InputStreamReader(in);
                BufferedReader bufferedReader = new BufferedReader(reader); // TODO: теперь можем читать данные из инета стрроками
                String line = bufferedReader.readLine();

                while(line != null){
                    result.append(line);
                     line = bufferedReader.readLine();
                }
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                // TODO: close connection
                if(urlConnection != null){
                    urlConnection.disconnect();
                }
            }
            Log.i("Url" , strings[0]);
            return result.toString();
        }
    }
}