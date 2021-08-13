package com.example.myapplication.main.Screens.Chat_Activity_MVP;


import android.app.Application;
import android.net.Uri;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;

import com.example.myapplication.main.Models.Model_Message;
import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;
import java.util.Locale;

public class Chat_ViewModel extends AndroidViewModel {


    private MutableLiveData<ArrayList<Model_Message>> messages = new MutableLiveData<>();

    public Chat_ViewModel(@NonNull Application application) {
        super(application);
    }


    //todo: start writing
    public void showIsHisWritingToYou(String recipientUserId) {
        Chat_Repository.instance.showIsHisWritingToYou(recipientUserId);
    }

    public MutableLiveData<Boolean> getShowIfWriting() {
        return Chat_Repository.instance.getShowIfWriting();
    }

    public void createStartWriting(String myFirebaseId, String recipientUserId) {
        Chat_Repository.instance.createStartWriting(myFirebaseId, recipientUserId);
    }

    public void deleteStartWriting(String myFirebaseId) {
        Chat_Repository.instance.deleteStartWriting(myFirebaseId);
    }

    //todo: load his info
    public void loadRecipientUserInfo(String recipientUserId) {
        Chat_Repository.instance.loadRecipientUserInfo(recipientUserId);
    }

    public MutableLiveData<String> getHisInfoAndAvatar() {
        return Chat_Repository.instance.getHisInfoAndAvatar();
    }

    public void isHeOnline(String recipientUserId) {
        Chat_Repository.instance.isHeOnline(recipientUserId);
    }

    public MutableLiveData<Boolean> getIsOnline() {
        return Chat_Repository.instance.getIsOnline();
    }

    public MutableLiveData<String> getLastTimeConnectionCheck() {
        return Chat_Repository.instance.getLastTimeConnectionCheck();
    }

    //todo: load my info
    public void loadInfoAboutUser(String myUid) {
        Chat_Repository.instance.loadInfoAboutUser(myUid);
    }

    public MutableLiveData<String> getMyNameCheck() {
        return Chat_Repository.instance.getMyNameCheck();
    }

    public MutableLiveData<String> getMyAvatarCheck() {
        return Chat_Repository.instance.getMyAvatarCheck();
    }

    //todo:load messages
    public void loadMessages(FirebaseAuth auth, String recipientUserId) {
        Chat_Repository.instance.loadMessages(auth, recipientUserId);
    }

    public MutableLiveData<Model_Message> getNewMessage() {
        return Chat_Repository.instance.getNewMessage();
    }

    public MutableLiveData<ArrayList<Model_Message>> getMessages() {
        messages.setValue(Chat_Repository.instance.getMessages());
        return messages;
    }

    public void disableRefresh(){
        Chat_Repository.instance.disableRefresh();
    }

    public MutableLiveData<Boolean> getRefreshChatCheck() {
        return Chat_Repository.instance.getRefreshChatCheck();
    }

    //todo: push common message
    public void pushCommonMessage(Model_Message message, String typeOfMessage, Uri downloadUri, String shareImage,
                                  String pDescription, String uName, String pImage, String pId,
                                  Chat_Main_Activity activity, Locale locale, String myName, String recipientUserId,
                                  FirebaseAuth auth, String myFirebaseId, String myAvatarUrl, String text) {
        Chat_Repository.instance.pushCommonMessage(message, typeOfMessage, downloadUri, shareImage,
                pDescription, uName, pImage, pId,
                activity, locale, myName, recipientUserId,
                auth, myFirebaseId, myAvatarUrl, text);
    }
}
