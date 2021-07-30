package com.example.myapplication.main.Screens.Posts.Posts_By_Recommendation_MVVM.di;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelStoreOwner;
import androidx.recyclerview.widget.GridLayoutManager;

import com.example.myapplication.main.Screens.Posts.Posts_By_Recommendation_MVVM.Post_Activity_Recommendations;
import com.example.myapplication.main.Screens.Posts.Posts_By_Recommendation_MVVM.Post_Adapter_Recommendations;
import com.example.myapplication.main.Screens.Posts.Posts_By_Recommendation_MVVM.Post_Recomm_ViewModel;

import javax.inject.Singleton;

import dagger.BindsInstance;
import dagger.Component;
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
    public Post_Recomm_ViewModel provideViewModel (AppCompatActivity activity) {
        return new ViewModelProvider(activity).get(Post_Recomm_ViewModel.class);
    }


  //  @Provides
    // @Singleton
  //  public SharedPreferences providePreferences(Context context) {
  //      return context.getSharedPreferences().get
  //  }


}
