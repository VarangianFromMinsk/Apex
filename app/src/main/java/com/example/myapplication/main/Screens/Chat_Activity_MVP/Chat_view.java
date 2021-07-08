package com.example.myapplication.main.Screens.Chat_Activity_MVP;

import com.example.myapplication.main.Models.Model_Message;

public interface Chat_view {

    void showIfWriting(boolean b);

    void infoAboutUser(String myName, String myAvatarUrl);

    void initRefreshChat();

    void showMessage(Model_Message message);

    void loadHisInfoAndAvatar(String hisImage);

    void isHeOnlineCheckComplete(boolean check, String lastTimeConnection);

}
