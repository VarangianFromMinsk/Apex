package com.example.myapplication.main.Screens.User_Profile_MVVM;


import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;

import com.example.myapplication.main.Models.Model_Post;
import com.example.myapplication.main.Models.Model_User;
import com.example.myapplication.main.Models.Model_Song;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import java.util.ArrayList;

public class Profile_Repository {

    private static Profile_Repository instance;

    //todo: posts part
    private final ArrayList<Model_Post> postListMyRepArray = new ArrayList<>();
    private MutableLiveData<ArrayList<Model_Post>> posts = new MutableLiveData<>();
    private boolean itsWorkPosts = false;
    private final MutableLiveData<String> countOfPost = new MutableLiveData<>();

    //todo: music part
    private final ArrayList<Model_Song> myMusicRepArray = new ArrayList<>();
    private  MutableLiveData<ArrayList<Model_Song>> music = new MutableLiveData<>();
    private boolean itsWorkMusic = false;
    private final MutableLiveData<String> countOfSongs = new MutableLiveData<>();

    //todo: current user part
    private Model_User curUser = new Model_User();
    private MutableLiveData<Model_User> mutUserData = new MutableLiveData<>();

    //todo: change user password
    private MutableLiveData<String> checkChangePassword = new MutableLiveData<>();


    //TODO: main constructor
    public static Profile_Repository getInstance() {
        if(instance == null){
            instance = new Profile_Repository();
        }
        return instance;
    }



    //TODO: get posts
    public MutableLiveData<ArrayList<Model_Post>> getPosts(String selectedUser) {
        posts = new MutableLiveData<>();
        loadPosts(selectedUser);
        return posts;
    }

    public void loadPosts(String selectedUser){
        //TODO: var to load only one time
        itsWorkPosts = true;

        postListMyRepArray.clear();

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Posts");
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(itsWorkPosts){
                    for(DataSnapshot ds : snapshot.getChildren()){
                        Model_Post post = ds.getValue(Model_Post.class);

                        assert post != null;
                        String hisUid = post.getUid();

                        if(selectedUser.equals(hisUid)){
                            postListMyRepArray.add(0,post);
                        }

                        itsWorkPosts = false;
                    }
                    posts.postValue(postListMyRepArray);
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }

    public MutableLiveData<String> getCountOfPost(String selectedUser) {
        loadCountOfPost(selectedUser);
        return countOfPost;
    }

    private void loadCountOfPost(String selectedUser) {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Posts");
        Query  query = ref.orderByChild("uid").equalTo(selectedUser);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    countOfPost.setValue(String.valueOf(snapshot.getChildrenCount()));
                }
                else {
                    countOfPost.setValue("0");
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }




    //TODO: get music
    public MutableLiveData<ArrayList<Model_Song>> getMusic(String selectedUser) {
        music = new MutableLiveData<>();
        loadMusic(selectedUser);
        return music;
    }

    private void loadMusic(String selectedUser) {
        //todo: var to load only one time
        itsWorkMusic = true;

        myMusicRepArray.clear();

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Music");
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(itsWorkMusic){
                    for(DataSnapshot ds : snapshot.getChildren()){
                        Model_Song song = ds.getValue(Model_Song.class);

                        assert song != null;

                        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Likes_Songs");
                        ref.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                if(snapshot.child(song.getUploadId()).child(selectedUser).exists()){
                                    myMusicRepArray.add(0,song);
                                }
                                music.postValue(myMusicRepArray);
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {
                            }
                        });

                        itsWorkMusic = false;
                    }
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }

    public MutableLiveData<String> getCountOfSongs(String selectedUser) {
        loadCountOfSongs(selectedUser);
        return countOfSongs;
    }

    private void loadCountOfSongs(String selectedUser) {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Likes_Songs");
        //get all data from this ref
        Query  query = ref.orderByChild(selectedUser).equalTo("Liked");
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    countOfSongs.setValue(String.valueOf(snapshot.getChildrenCount()));
                }
                else {
                    countOfSongs.setValue("0");
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }



    //TODO: get info about current user
    public MutableLiveData<Model_User> getMutUserData(String selectedUser) {
        mutUserData = new MutableLiveData<>();
        loadUser(selectedUser);
        return mutUserData;
    }

    private void loadUser(String selectedUser) {
        DatabaseReference usersDatabaseReference = FirebaseDatabase.getInstance().getReference().child("users");
        Query userQuery = usersDatabaseReference.orderByChild("firebaseId").equalTo(selectedUser);
        userQuery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for(DataSnapshot ds : snapshot.getChildren()){
                    curUser = ds.getValue(Model_User.class);
                }
                mutUserData.postValue(curUser);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }



    //TODO: change user data
    public void changeUserData(String selectedUser, String path, String text){
        DatabaseReference usersDatabaseReference = FirebaseDatabase.getInstance().getReference().child("users");
        usersDatabaseReference.child(selectedUser).child(path).setValue(text);
    }



    //TODO: update current avatar or background
    public void updateCurrentAvatarOrBackground(String keyUser, String path, String imageUrl){
        DatabaseReference usersDatabaseReference = FirebaseDatabase.getInstance().getReference().child("users");
        usersDatabaseReference.child(keyUser).child(path).setValue(imageUrl);
    }


    //TODO: update user password
    public MutableLiveData<String> getCheckChangePassword(FirebaseUser user, String newPassword) {
        checkChangePassword = new MutableLiveData<>();
        changePassword(user, newPassword);
        return checkChangePassword;
    }

    private void changePassword(FirebaseUser user, String newPassword) {
        user.updatePassword(newPassword).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(!task.isSuccessful()){
                    checkChangePassword.postValue("Something went wrong. Please try again later");
                }else {
                    checkChangePassword.postValue("Password Successfully Modified");
                }
            }
        });
    }

}
