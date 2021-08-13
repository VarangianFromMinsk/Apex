package com.example.myapplication.main.Screens.Posts.Posts_By_Friends_MVVM;

import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;

import com.example.myapplication.main.Models.Model_Post;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class Friends_Repository {

    public static final Friends_Repository instance = new Friends_Repository();

    private final ArrayList<Model_Post> postListFriendsRepArray = new ArrayList<>();
    private final MutableLiveData<ArrayList<Model_Post>> posts = new MutableLiveData<>();
    private final MutableLiveData<Boolean> showLoad = new MutableLiveData<>();
    private final MutableLiveData<Boolean> showNewPosts = new MutableLiveData<>();

    private String hisUid = "";
    private String myUid;
    private boolean itsWork;


    public MutableLiveData<ArrayList<Model_Post>> getPosts(String searchText) {
        loadPosts(searchText);
        return posts;
    }


    public void loadPosts(String searchQuery){

        FirebaseAuth auth = FirebaseAuth.getInstance();
        myUid = auth.getUid();

        itsWork = true;

        postListFriendsRepArray.clear();

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Posts");
        //TODO: get all data from this ref
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(itsWork){
                    for(DataSnapshot ds : snapshot.getChildren()){
                        Model_Post post = ds.getValue(Model_Post.class);

                        assert post != null;
                        hisUid = post.getUid();


                        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("users");
                        Query queryFriends = ref.child(myUid).child("Friends").orderByChild("uid").equalTo(hisUid);
                        queryFriends .addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                for(DataSnapshot ds : snapshot.getChildren()){
                                    if(ds.exists()){
                                        if(!searchQuery.equals("no")){
                                            if (post.getpTitle().toLowerCase().contains(searchQuery.toLowerCase())
                                                    || post.getpDescr().toLowerCase().contains(searchQuery.toLowerCase())) {
                                                postListFriendsRepArray.add(0,post);
                                                setShowProgressBar(false);
                                            }
                                        }
                                        else{
                                            postListFriendsRepArray.add(0,post);
                                            setShowProgressBar(false);
                                        }
                                    }
                                }
                                posts.setValue(postListFriendsRepArray);
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {
                                setShowProgressBar(false);
                            }
                        });
                        itsWork = false;
                    }
                }
                else{

                    //TODO: create list to compare size
                    List<Model_Post> checkForMainList = new ArrayList<>();

                    for(DataSnapshot ds : snapshot.getChildren()){

                        Model_Post post = ds.getValue(Model_Post.class);

                        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("users");
                        Query query = ref.child(myUid).child("Friends").orderByChild("uid").equalTo(hisUid);
                        query.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                for(DataSnapshot ds : snapshot.getChildren()){
                                    if(ds.exists()){
                                        if(!searchQuery.equals("no")){
                                            assert post != null;
                                            if (post.getpTitle().toLowerCase().contains(searchQuery.toLowerCase())
                                                    || post.getpDescr().toLowerCase().contains(searchQuery.toLowerCase())) {
                                                checkForMainList.add(post);
                                            }
                                        }
                                        else{
                                            checkForMainList.add(post);
                                        }
                                    }
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });

                    }

                    if(checkForMainList.size() > postListFriendsRepArray.size() ){
                        showNewPosts.setValue(true);
                    }
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }

    //progress bar
    public MutableLiveData<Boolean> getShowLoad() {
        return showLoad;
    }

    public void setShowProgressBar(Boolean state){
        showLoad.setValue(state);
    }

    //show new posts

    public MutableLiveData<Boolean> getShowNewPosts() {
        return showNewPosts;
    }
}
