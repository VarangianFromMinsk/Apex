package com.example.myapplication.main.Screens.User_List_4_States_MVVM;


import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;

import com.example.myapplication.main.Models.Model_User;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class User_List_Repository {

    private static User_List_Repository instance;
    private final ArrayList<Model_User> userRepArray = new ArrayList<>();
    private final MutableLiveData<ArrayList<Model_User>> users = new MutableLiveData<>();
    private boolean isWork = false;


    public static User_List_Repository getInstance(){
        if(instance == null){
            instance = new User_List_Repository();
        }
        return instance;
    }


    public MutableLiveData<ArrayList<Model_User>> getPostList(String searchText, String type, String myUid){
        loadPosts(searchText, type, myUid);

        //TODO: for example
        /*if(userRepArray.size() == 0){
            loadPosts(searchText);
        }
         */

        return users;
    }

    public void loadPosts(String search, String type, String myUid ){

        isWork = true;

        userRepArray.clear();

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("users");
        //TODO: get all data from this ref
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(isWork){
                    for(DataSnapshot ds : snapshot.getChildren()){
                        Model_User user = ds.getValue(Model_User.class);

                        assert user != null;
                        String hisUid = user.getFirebaseId();

                        if(type.equals("all")){
                            //todo: all users
                            if(search.equals("no")) {
                                userRepArray.add(0, user);
                            }
                            else{
                                if(user.getName().toLowerCase().contains(search.toLowerCase())
                                        || user.getNick().toLowerCase().contains(search.toLowerCase())){
                                    userRepArray.add(0, user);
                                }
                            }
                            users.setValue(userRepArray);
                        }
                        else if(type.equals("friends")){
                            //todo: friends
                            ref.child(myUid).child("Friends").orderByChild("uid").equalTo(hisUid)
                                    .addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                                            for(DataSnapshot ds : snapshot.getChildren()) {
                                                if(ds.exists()){
                                                    if(search.equals("no")){
                                                        userRepArray.add(0, user);
                                                    }
                                                    else{
                                                        if(user.getName().toLowerCase().contains(search.toLowerCase())
                                                                || user.getNick().toLowerCase().contains(search.toLowerCase())){
                                                            userRepArray.add(0, user);
                                                        }
                                                    }

                                                }
                                            }
                                            users.setValue(userRepArray);
                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError error) {
                                        }
                                    });
                        }
                        else if(type.equals("requests")){
                            ref.child(myUid).child("RequestsToFriends").orderByChild("uid").equalTo(hisUid)
                                    .addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                                            for(DataSnapshot ds : snapshot.getChildren()) {
                                                if(ds.exists()){
                                                    if(search.equals("no")){
                                                        userRepArray.add(0, user);
                                                    }
                                                    else{
                                                        if(user.getName().toLowerCase().contains(search.toLowerCase())
                                                                || user.getNick().toLowerCase().contains(search.toLowerCase())){
                                                            userRepArray.add(0, user);
                                                        }
                                                    }
                                                }
                                            }
                                            users.setValue(userRepArray);
                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError error) {
                                        }
                                    });
                        }
                        else if(type.equals("ban")){
                            ref.child(myUid).child("BlockedUsers").orderByChild("uid").equalTo(hisUid)
                                    .addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                                            for(DataSnapshot ds : snapshot.getChildren()) {
                                                if(ds.exists()){
                                                    if(search.equals("no")){
                                                        userRepArray.add(0, user);
                                                    }
                                                    else{
                                                        if(user.getName().toLowerCase().contains(search.toLowerCase())
                                                                || user.getNick().toLowerCase().contains(search.toLowerCase())){
                                                            userRepArray.add(0, user);
                                                        }
                                                    }

                                                }
                                            }
                                            users.setValue(userRepArray);
                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError error) {
                                        }
                                    });
                        }

                    }
                    isWork = false;
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }


}
