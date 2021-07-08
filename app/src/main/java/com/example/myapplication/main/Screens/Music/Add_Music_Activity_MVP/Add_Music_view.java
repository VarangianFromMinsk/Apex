package com.example.myapplication.main.Screens.Music.Add_Music_Activity_MVP;

public interface Add_Music_view {

    void loadImageStart();

    void setImageProgress(int progress);

    void imageComplete();

    void setMusicProgress(int progress);

    void musicComplete();

    void noFileSelected();
}
