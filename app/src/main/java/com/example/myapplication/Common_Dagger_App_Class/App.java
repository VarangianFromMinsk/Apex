package com.example.myapplication.Common_Dagger_App_Class;

import android.app.Application;

import com.example.myapplication.di.Common_App_Component;
import com.example.myapplication.di.Common_App_Module;
import com.example.myapplication.di.DaggerCommon_App_Component;


public class App extends Application {

    private Common_App_Component commonComponent;

    @Override
    public void onCreate() {
        super.onCreate();

       commonComponent = DaggerCommon_App_Component.builder()
                .common_App_Module(new Common_App_Module(this))
                .build();

    }

    public Common_App_Component getCommonComponent() {
        return commonComponent;
    }

}
