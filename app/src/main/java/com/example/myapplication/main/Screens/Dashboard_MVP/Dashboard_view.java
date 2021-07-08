package com.example.myapplication.main.Screens.Dashboard_MVP;

public interface Dashboard_view {

    void loadWeatherComplete(String name_from_Api, String correct_temp, String description_from_Api, String currentWeatherImage);

    void saveCurrentTown(String name_from_Api);
}
