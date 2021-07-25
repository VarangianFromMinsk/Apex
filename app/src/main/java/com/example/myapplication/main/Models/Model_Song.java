package com.example.myapplication.main.Models;

import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;


@Entity(tableName = "songs")
public class Model_Song {

    @PrimaryKey(autoGenerate = true)
    private int id;

    private String albumUrl, songUrl, songMainTitle, songLastTitle, songDuration, uploadId;

    @Ignore
    public Model_Song() {
    }

    public Model_Song(int id, String albumUrl, String songUrl, String songMainTitle, String songLastTitle, String songDuration, String uploadId) {
        this.id = id;

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
        this.uploadId = uploadId;
    }

    @Ignore
    public Model_Song(String albumUrl, String songUrl, String songMainTitle, String songLastTitle, String songDuration, String uploadId) {

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

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }
}
