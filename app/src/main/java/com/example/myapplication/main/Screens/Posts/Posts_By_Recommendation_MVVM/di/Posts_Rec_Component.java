package com.example.myapplication.main.Screens.Posts.Posts_By_Recommendation_MVVM.di;

import com.example.myapplication.main.Screens.Posts.Posts_By_Recommendation_MVVM.Post_Activity_Recommendations;
import com.example.myapplication.main.Screens.Posts.Posts_By_Recommendation_MVVM.Post_Recomm_ViewModel;

import javax.inject.Singleton;
import dagger.Component;


//todo: мост между даггером и зависимостями
@Component(modules = {Post_Rec_Module.class,SecondModule.class})
@Singleton
public interface Posts_Rec_Component {

    //todo: должна быть сугобо активити/фрагмент которая получит зависимости
    void inject (Post_Activity_Recommendations postActivityRecommendations);

    void inject (Post_Recomm_ViewModel postRecommViewModel);

 }
