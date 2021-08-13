package com.example.myapplication.main.Screens.Chat_Activity_MVP;

import com.google.firebase.database.DataSnapshot;

public interface ChangeMessage_Interface {

    void changeMessageStart(String messageFromBase, DataSnapshot ds);
}
