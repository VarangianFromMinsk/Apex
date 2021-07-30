package com.example.myapplication.main.Screens.Chat_Activity_MVP;

import android.net.Uri;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.myapplication.Services.App_Constants;
import com.example.myapplication.main.Models.Model_Message;
import com.example.myapplication.main.Models.Model_Start_Writing;
import com.example.myapplication.main.Models.Model_User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

public class Chat_Presenter {

    private final Chat_view view;

    private String myName = "";
    private String  myAvatarUrl = "";

    public Chat_Presenter(Chat_view view) {
        this.view = view;
    }

    //todo: start writting
    public void showIsHisWritingToYou(String recipientUserId){
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference();
        ref.child("StartWriting").child(recipientUserId)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if(snapshot.exists()){
                            view.showIfWriting(true);
                        }
                        else{
                            view.showIfWriting(false);
                        }

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    public void createStartWriting(String myFirebaseId, String recipientUserId){
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child("StartWriting").child(myFirebaseId);
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(!snapshot.exists()){
                    Model_Start_Writing modelStartWriting = new Model_Start_Writing();
                    modelStartWriting.setSender(myFirebaseId);
                    modelStartWriting.setRecipient(recipientUserId);
                    modelStartWriting.setValue("true");
                    ref.setValue(modelStartWriting);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    public void deleteStartWriting(String myFirebaseId){
        try {
            DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child("StartWriting").child(myFirebaseId);
            ref.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if(snapshot.exists()){
                        ref.removeValue();
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        }catch (Exception ignored){}
    }

    //todo: load avatar and another info about stranger user
    public void loadRecipientUserInfo(String recipientUserId){
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child("users");
        Query userQuery = ref.orderByChild("firebaseId").equalTo(recipientUserId);
        userQuery.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for(DataSnapshot ds : snapshot.getChildren()){
                    String hisImage = "" + ds.child("avatarMockUpResourse").getValue();
                    view.loadHisInfoAndAvatar(hisImage);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    public void isHeOnline(String recipientUserId){
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child("users");
        DatabaseReference userRef = ref.child(recipientUserId);
        userRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.hasChild("online")) {
                    String lastTimeConnection = Objects.requireNonNull(snapshot.child("timeonline").getValue()).toString();
                    view.isHeOnlineCheckComplete(true,lastTimeConnection);
                }else{
                    String lastTimeConnection = Objects.requireNonNull(snapshot.child("timeonline").getValue()).toString();
                    view.isHeOnlineCheckComplete(false,lastTimeConnection);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }


    //todo: load info about current user
    public void loadInfoAboutUser(String myUid){
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child("users");
        Query query = ref.child(myUid).orderByChild("firebaseId").equalTo(myUid);
        query .addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for(DataSnapshot ds : snapshot.getChildren()){
                    if(ds.exists()){
                        Model_User user = ds.getValue(Model_User.class);
                        myName = user.getName();
                        try{
                            myAvatarUrl =  user.getAvatarMockUpResourse();
                        }catch (Exception ignored){}
                    }
                    view.infoAboutUser(myName, myAvatarUrl);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    //todo: load messages
    public void loadMessages(FirebaseAuth auth, String recipientUserId){
        DatabaseReference messagesDatabaseReference = FirebaseDatabase.getInstance().getReference().child("messages");

        ChildEventListener messagesChildEventListener = new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

                Model_Message message = snapshot.getValue(Model_Message.class);

                assert message != null;

                if (message.getSender().equals(Objects.requireNonNull(auth.getCurrentUser()).getUid()) && message.getRecipient().equals(recipientUserId)) {
                    message.setMine(true);
                    view.showMessage(message);

                } else if (message.getRecipient().equals(auth.getCurrentUser().getUid()) && message.getSender().equals(recipientUserId)) {
                    message.setMine(false);
                    view.showMessage(message);
                }


            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                view.initRefreshChat();
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {   // Удаление
                view.initRefreshChat();
            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {  // Перемещение
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        };
        messagesDatabaseReference.addChildEventListener(messagesChildEventListener);
    }


    //todo: common push
    public void pushCommonMessage(Model_Message message, String typeOfMessage, Uri downloadUri, String shareImage,
                                  String pDescription, String uName, String pImage, String pId,
                                  Chat_Main_Activity activity, Locale locale, String myName, String recipientUserId,
                                  FirebaseAuth auth, String myFirebaseId, String myAvatarUrl, String text){

        String messageForNotification = "";

        if(!typeOfMessage.equals("repostMessage")){
            //todo: path if it repost
            message.setIsThatRepost("false");
            message.setPostId(null);
            //todo: path if its record
            message.setIsThatRecord("false");
            message.setRecordUrl(null);

            message.setName(myName);

            switch (typeOfMessage) {
                case "textMessage":
                    message.setText(text);
                    messageForNotification = text;
                    message.setImageUrl(null);
                    break;
                case "imageMessage":
                    message.setImageUrl(downloadUri.toString());
                    messageForNotification = String.valueOf("User send image");
                    break;
                case "shareImageMessage":
                    message.setImageUrl(shareImage);
                    messageForNotification = String.valueOf("User shared image");
                    break;
            }
        }
        else{
            //todo: path if its repost
            message.setIsThatRecord("false");
            message.setRecordUrl(null);

            message.setIsThatRepost("true");
            message.setPostId(pId);
            message.setText(pDescription);
            messageForNotification = String.valueOf("Shared post " + pDescription);
            message.setName(uName);
            message.setImageUrl(pImage);
        }

        //todo: common path
        message.setSender(auth.getCurrentUser().getUid());
        message.setRecipient(recipientUserId);

        Calendar calForTime = Calendar.getInstance();
        SimpleDateFormat currentTime = new SimpleDateFormat("hh:mm a", locale);
        String saveCurrentTime = currentTime.format(calForTime.getTime());

        Calendar calForDate = Calendar.getInstance();
        SimpleDateFormat currentDate = new SimpleDateFormat("EEE, MMM d, ''yy", locale);
        String saveCurrentDate = currentDate.format(calForDate.getTime());

        message.setTimeOfMessage(saveCurrentTime);
        message.setDayOfMessage(saveCurrentDate);

        //todo: set key to change and delete message
        String timeStamp = String.valueOf(System.currentTimeMillis());
        message.setKeyMessage(timeStamp);

        DatabaseReference messagesDatabaseReference = FirebaseDatabase.getInstance().getReference().child("messages");
        messagesDatabaseReference.push().setValue(message);

        //todo: push notification
        getHisTokenAndSendNotificationData(messageForNotification, recipientUserId, myName, myFirebaseId, myAvatarUrl, activity);
    }


    //todo: notification
    private void getHisTokenAndSendNotificationData(String messageForHis, String hisId, String myName, String myFirebaseId, String myAvatarUrl, Chat_Main_Activity activity){
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("users").child(hisId);
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String token = Objects.requireNonNull(snapshot.child("token").getValue()).toString();

                JSONObject to = new JSONObject();
                JSONObject data = new JSONObject();
                try {
                    //data, which push to another user
                    data.put("title", myName);
                    data.put("message", messageForHis);
                    data.put("hisId", myFirebaseId);
                    data.put("hisImage", myAvatarUrl);

                    to.put("to", token);
                    to.put("data", data);

                    sendNotification(to, activity);

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void sendNotification(JSONObject to, Chat_Main_Activity activity) {

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, App_Constants.NOTIFICATION_URL, to, response ->{

        },error->{

        }){
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String,String> map = new HashMap<>();
                map.put("Authorization", "key=" + App_Constants.SERVER_KEY);
                map.put("Content-Type", "application/json");
                return map;
            }

            @Override
            public String getBodyContentType() {
                return "application/json";
            }
        };

        RequestQueue requestQueue = Volley.newRequestQueue(activity);
        request.setRetryPolicy(new DefaultRetryPolicy(30000,DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        requestQueue.add(request);

    }
}
