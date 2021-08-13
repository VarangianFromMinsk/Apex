package com.example.myapplication.di;


import com.example.myapplication.databinding.DashboardActivityBinding;
import com.example.myapplication.main.Screens.Chat_Activity_MVP.Chat_Main_Activity;
import com.example.myapplication.main.Screens.Dashboard_MVP.Dashboard_Activity;
import com.example.myapplication.main.Screens.Music.Add_Music_Activity_MVP.Add_Music_Activity;
import com.example.myapplication.main.Screens.Music.Music_List_Activity_MVVM.Music_List_Activity;
import com.example.myapplication.main.Screens.Music.Music_Player_Activity_And_Service_MVP.Music_Player_Activity;
import com.example.myapplication.main.Screens.Posts.Add_Change_Post_MVP.Add_Change_Post_Activity;
import com.example.myapplication.main.Screens.Posts.Post_Comments_MVVM.Post_Comment_Activity;
import com.example.myapplication.main.Screens.Posts.Posts_By_Friends_MVVM.Post_Activity_Friends;
import com.example.myapplication.main.Screens.Posts.Posts_By_Recommendation_MVVM.Post_Activity_Recommendations;
import com.example.myapplication.main.Screens.Posts.Posts_By_Recommendation_MVVM.Post_Recomm_ViewModel;
import com.example.myapplication.main.Screens.Show_Image_MVP.Show_Image_Activity;
import com.example.myapplication.main.Screens.User_List_4_States_MVVM.User_List_Activity;
import com.example.myapplication.main.Screens.User_Profile_MVVM.User_Profile_Activity;

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

    void inject(User_List_Activity userListActivity);

    void inject(User_Profile_Activity userProfileActivity);

    void inject(Chat_Main_Activity chatMainActivity);

    void inject(Dashboard_Activity dashboard_activity);

    void inject(Add_Music_Activity addMusicActivity);

    void inject(Music_List_Activity musicListActivity);

    void inject(Music_Player_Activity musicPlayerActivity);

                //todo: another staff
    void inject(Post_Recomm_ViewModel postRecommViewModel);

}
