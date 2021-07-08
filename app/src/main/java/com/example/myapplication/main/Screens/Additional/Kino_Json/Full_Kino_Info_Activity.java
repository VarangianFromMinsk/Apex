package com.example.myapplication.main.Screens.Additional.Kino_Json;

import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.PreferenceManager;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;
import com.example.myapplication.R;
import com.example.myapplication.Services.App_Constants;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Objects;

public class Full_Kino_Info_Activity extends AppCompatActivity {

    private String url;
    private RequestQueue requestQueue;
    private TextView titleTv , yearTv , realisedTv , runtimeTv , awardsTv , ratingTv , plotTv ;
    private ImageView posterIv;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.full_kino_info_activity);

        Objects.requireNonNull(getSupportActionBar()).hide();
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        initViews();

        fullInfo();
    }

    private void initViews() {
        titleTv = findViewById(R.id.TitleFullInfo);
        yearTv  = findViewById(R.id.yearFullInfo);
        realisedTv  = findViewById(R.id.realisedFullInfo);
        runtimeTv  = findViewById(R.id.runTimeFullInfo);
        awardsTv  = findViewById(R.id.awardsFullInfo);
        ratingTv  = findViewById(R.id.imdbRatingFullInfo);
        plotTv  = findViewById(R.id.plotFullInfo);

        posterIv = findViewById(R.id.posterFullInfo);
    }

    private void fullInfo() {

        requestQueue = Volley.newRequestQueue(this);

        Intent intent = getIntent();
        String id = "";

        if (intent != null) {
            id = intent.getStringExtra("id");
        }

        url = App_Constants.URL_REQUEST_SINGLE_TITLE + id;

        // TODO: request to internet
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET,
                url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {

                    JSONObject jsonObject = new JSONObject(String.valueOf(response));

                    //TODO: getValue
                    String title = jsonObject.getString("Title");
                    String year = jsonObject.getString("Year");
                    String realised = jsonObject.getString("Released");
                    String poster = jsonObject.getString("Poster");
                    String runtime = jsonObject.getString("Runtime");
                    String plot = jsonObject.getString("Plot");
                    String awards = jsonObject.getString("Awards");
                    String imdbRating = jsonObject.getString("imdbRating");

                    //TODO: save current search
                    SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(Full_Kino_Info_Activity.this);
                    preferences.edit().putString("last_movie_search_name", title).putString("last_movie_search_poster", poster ).apply();

                    //TODO: setValue
                    titleTv.setText(String.valueOf("Title :  " + title));
                    yearTv.setText(String.valueOf("Year :  " + year));
                    realisedTv.setText(String.valueOf("Realised :  " + realised));
                    runtimeTv.setText(String.valueOf("Runtime :  " + runtime));
                    awardsTv.setText(String.valueOf("Awards :  " + awards));
                    ratingTv.setText(String.valueOf("Rating :  " + imdbRating));
                    plotTv.setText(plot);

                    Glide.with(posterIv.getContext()).load(poster).into(posterIv);

                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                error.printStackTrace();
            }
        });

        requestQueue.add(request);
        
    }

}