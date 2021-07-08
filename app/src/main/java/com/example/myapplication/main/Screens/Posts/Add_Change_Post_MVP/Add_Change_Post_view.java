package com.example.myapplication.main.Screens.Posts.Add_Change_Post_MVP;

public interface Add_Change_Post_view {

    void loadPostDataForChange(String title, String Description, String ImageUrl);

    void loadCurrentUserInfo(String name, String email, String avatar);

    void uploadDataComplete(Boolean isSuccess, String postId);

    void updateWasWithImageComplete(Boolean isSuccess);

    void updateWithNowImageComplete(Boolean isSuccess);

    void updateWithoutImageComplete(Boolean isSuccess);
}
