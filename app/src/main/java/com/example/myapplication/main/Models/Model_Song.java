package com.example.myapplication.main.Models;

import com.google.firebase.database.Exclude;

public class Model_Song {

    private String albumUrl, songUrl, songMainTitle, songLastTitle, songDuration, uploadId, mKey;

    public Model_Song() {
    }

    public Model_Song(String albumUrl, String songUrl, String songMainTitle, String songLastTitle, String songDuration, String uploadId, String mKey) {

        if(songMainTitle.equals("")){
            songMainTitle = "No main title";
        }

        if(songLastTitle.equals("")){
            songLastTitle = "No main title";
        }

        this.albumUrl = albumUrl;
        this.songUrl = songUrl;
        this.songMainTitle = songMainTitle;
        this.songLastTitle = songLastTitle;
        this.songDuration = songDuration;
        this.uploadId =uploadId;
        this.mKey = mKey;
    }

    public String getAlbumUrl() {
        return albumUrl;
    }

    public void setAlbumUrl(String albumUrl) {
        this.albumUrl = albumUrl;
    }

    public String getSongUrl() {
        return songUrl;
    }

    public void setSongUrl(String songUrl) {
        this.songUrl = songUrl;
    }

    public String getSongMainTitle() {
        return songMainTitle;
    }

    public void setSongMainTitle(String songMainTitle) {
        this.songMainTitle = songMainTitle;
    }

    public String getSongLastTitle() {
        return songLastTitle;
    }

    public void setSongLastTitle(String songLastTitle) {
        this.songLastTitle = songLastTitle;
    }

    public String getSongDuration() {
        return songDuration;
    }

    public void setSongDuration(String songDuration) {
        this.songDuration = songDuration;
    }

    public String getUploadId() {
        return uploadId;
    }

    public void setUploadId(String uploadId) {
        this.uploadId = uploadId;
    }

    @Exclude
    public String getmKey() {
        return mKey;
    }

    @Exclude
    public void setmKey(String mKey) {
        this.mKey = mKey;
    }
}
