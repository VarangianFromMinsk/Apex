package com.example.myapplication.main.Screens.Additional.Kino_Json;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.myapplication.R;
import com.example.myapplication.Services.App_Constants;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Objects;

public class Kino_Search_Info_Activity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private Kino_Adapter kinoAdapter;
    private ArrayList<Model_Kino> movies;
    private RequestQueue requestQueue;
    private EditText titleName;
    private String url;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.kino_search_info_activity);

        Objects.requireNonNull(getSupportActionBar()).hide();
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        initialization();

        getMovies();
    }

    private void initialization(){
        titleName = findViewById(R.id.editTitleName);

        recyclerView = findViewById(R.id.kinoRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        movies = new ArrayList<>();
        requestQueue = Volley.newRequestQueue(this);

        url = App_Constants.URL_KINO_DEF_REQUEST;
    }

    private void getMovies() {

        // TODO: request to internet
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET,
                url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {

                    JSONArray jsonArray = response.getJSONArray("Search");     // Используем Array тк данные сайт выдает в Array формате

                    for (int i = 0; i < jsonArray.length(); i ++){     // Проганяем все данные от 0 до последнего!
                        JSONObject jsonObject = jsonArray.getJSONObject(i);

                        String title = jsonObject.getString("Title");
                        String year = jsonObject.getString("Year");
                        String type = jsonObject.getString("Type");
                        String poster = jsonObject.getString("Poster");
                        String id = jsonObject.getString("imdbID");

                        Model_Kino modelKino = new Model_Kino();
                        modelKino.setTitle(title);
                        modelKino.setYear(year);
                        modelKino.setType(type);
                        modelKino.setPosterUrl(poster);
                        modelKino.setId(id);

                        movies.add(modelKino);
                    }

                    kinoAdapter = new Kino_Adapter(Kino_Search_Info_Activity.this,movies);

                    recyclerView.setAdapter(kinoAdapter);

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

    public void searchKino(View view) {
        titleName.getText().toString().trim();
        String moviename = titleName.getText().toString().trim();
        url = App_Constants.URL_REQUEST_KINO +  moviename;

        // TODO:refresh recyclerView
        movies.clear();
        kinoAdapter.notifyDataSetChanged();

        getMovies();
    }
}