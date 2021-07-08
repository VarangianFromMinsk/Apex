package com.example.myapplication.main.Screens.Additional.Kino_Json;

public class Model_Kino {
    private String id;
    private String title;
    private String year;
    private String type;
    private String posterUrl;

    public Model_Kino(String title, String year, String type, String id, String posterUrl) {
        this.title = title;
        this.year = year;
        this.type = type;
        this.id = id;
        this.posterUrl = posterUrl;
    }

    public Model_Kino(String title, String year, String type, String posterUrl) {
        this.title = title;
        this.year = year;
        this.type = type;
        this.posterUrl = posterUrl;
    }

    public Model_Kino() {     // Создаем пустой конструктор
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public String getPosterUrl() {
        return posterUrl;
    }

    public void setPosterUrl(String posterUrl) {
        this.posterUrl = posterUrl;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getYear() {
        return year;
    }

    public void setYear(String year) {
        this.year = year;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
