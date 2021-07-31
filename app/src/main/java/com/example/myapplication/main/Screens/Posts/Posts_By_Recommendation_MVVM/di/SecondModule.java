package com.example.myapplication.main.Screens.Posts.Posts_By_Recommendation_MVVM.di;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.recyclerview.widget.GridLayoutManager;
import com.example.myapplication.Services.Check_Internet_Connection_Exist;
import com.example.myapplication.Services.Online_Offline_User_Service_To_Firebase;
import com.example.myapplication.main.Screens.Posts.Posts_By_Recommendation_MVVM.Post_Adapter_Recommendations;
import javax.inject.Singleton;
import dagger.Module;
import dagger.Provides;

//TODO: включи в себя другой модуль (получаем оттуда КОНТЕКСТ)
@Module(includes = Post_Rec_Module.class)
public class SecondModule {


    @Provides
    @Singleton
    Post_Adapter_Recommendations provideRecAdapter(Context context){
        return new Post_Adapter_Recommendations(context);
    }

    @Provides
    //todo: ту без синглтона, тк нужен каждый раз новый обьект
    GridLayoutManager provideGridLayout(Context context){
        return new GridLayoutManager(context,3);
    }

    @Provides
    @Singleton
    Check_Internet_Connection_Exist provideCheckInternetConnection(){
        return new Check_Internet_Connection_Exist();
    }

    @Provides
    @Singleton
    Online_Offline_User_Service_To_Firebase provideObserverLifeCycle(SharedPreferences preferences, Context context){
        return new Online_Offline_User_Service_To_Firebase(preferences, context);
    }

}
