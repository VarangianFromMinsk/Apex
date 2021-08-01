package com.example.myapplication.main.Screens.Posts.Add_Change_Post_MVP.di;

import com.example.myapplication.main.Screens.Posts.Add_Change_Post_MVP.Add_Change_Post_Activity;
import javax.inject.Singleton;
import dagger.Component;

@Singleton
@Component(modules = {Post_AddChange_Module.class})
public interface Post_AddChange_Component {

    void inject(Add_Change_Post_Activity addChangePostActivity);

}
