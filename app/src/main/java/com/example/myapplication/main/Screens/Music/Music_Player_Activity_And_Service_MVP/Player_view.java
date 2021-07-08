package com.example.myapplication.main.Screens.Music.Music_Player_Activity_And_Service_MVP;

import com.example.myapplication.main.Models.Model_Song;

import java.util.ArrayList;

public interface Player_view {

    void dataLoadComplete(ArrayList<Model_Song> songList);

    void showToastLike(String likeOrDislike);

    void showHeartIfLiked(boolean check);
}
