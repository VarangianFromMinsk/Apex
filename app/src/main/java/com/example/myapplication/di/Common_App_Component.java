package com.example.myapplication.di;


import com.example.myapplication.main.Screens.Posts.Add_Change_Post_MVP.Add_Change_Post_Activity;
import com.example.myapplication.main.Screens.Posts.Post_Comments_MVVM.Post_Comment_Activity;
import com.example.myapplication.main.Screens.Posts.Posts_By_Friends_MVP.Post_Activity_Friends;
import com.example.myapplication.main.Screens.Posts.Posts_By_Recommendation_MVVM.Post_Activity_Recommendations;
import com.example.myapplication.main.Screens.Posts.Posts_By_Recommendation_MVVM.Post_Recomm_ViewModel;
import com.example.myapplication.main.Screens.Show_Image_MVP.Show_Image_Activity;

import javax.inject.Singleton;

import dagger.Component;

//todo: мост между даггером и зависимостями
@Singleton
@Component(modules = {Common_App_Module.class})
public interface Common_App_Component {

    //todo: должны быть сугобо активити/фрагмент которая получит зависимости

    //todo: activity
    void inject(Post_Comment_Activity postCommentActivity);

    void inject(Add_Change_Post_Activity addChangePostActivity);

    void inject(Post_Activity_Friends postActivityFriends);

    void inject(Post_Activity_Recommendations postActivityRecommendations);

    void inject(Show_Image_Activity showImageActivity);

    //todo: another staff
    void inject (Post_Recomm_ViewModel postRecommViewModel);

}
