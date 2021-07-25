package com.example.myapplication.main.Screens.Posts.Post_Comments_MVVM;

import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;

import com.example.myapplication.main.Models.Model_Comment;
import com.example.myapplication.main.Models.Model_Post;
import com.example.myapplication.main.Models.Model_User;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class Comment_Repository {

    //todo: неленивая инициализацтя
    public static final Comment_Repository instance = new Comment_Repository();

    //todo: ленивая инициализация
   // private static Comment_Repository instance;

    //TODO: comments
    private final ArrayList<Model_Comment> commentRepArray = new ArrayList<>();
    private final MutableLiveData<ArrayList<Model_Comment>> comments = new MutableLiveData<>();

    //TODO: post
    private final MutableLiveData<Model_Post> currentPost = new MutableLiveData<>();

    //TODO: currentUser
    private final MutableLiveData<Model_User> currentUser = new MutableLiveData<>();

    private String isLiked = "";
    private final MutableLiveData<String> mutIsLiked = new MutableLiveData<>();


    //todo: ленивая инициализация
    /*public static Comment_Repository getInstance(){
        if(instance == null){
            instance = new Comment_Repository();
        }
        return instance;
    }

     */

    //TODO: comments
    public MutableLiveData<ArrayList<Model_Comment>> getCommentsList(String postId){

        loadComments(postId);
        return comments;
    }

    private void loadComments(String postId) {

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Posts").child(postId).child("Comments");
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                commentRepArray.clear();
                    for(DataSnapshot ds : snapshot.getChildren()){
                        if(ds.exists()){
                            Model_Comment modelComment = ds.getValue(Model_Comment.class);
                            commentRepArray.add(modelComment);
                        }
                    }
                    comments.postValue(commentRepArray);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    //TODO: post
    public MutableLiveData<Model_Post> getCurrentPost(String postId) {

        loadCurrentPost(postId);

        return currentPost;
    }

    private void loadCurrentPost(String postId) {

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Posts");
        Query query = ref.orderByChild("pId").equalTo(postId);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for(DataSnapshot ds : snapshot.getChildren()){
                    Model_Post modelPost = ds.getValue(Model_Post.class);
                    currentPost.postValue(modelPost);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    //TODO: user
    public MutableLiveData<Model_User> getCurrentUser(String myUid) {

        loadCurrentUser(myUid);

        return currentUser;
    }

    private void loadCurrentUser(String myUid) {

        Query myRef = FirebaseDatabase.getInstance().getReference("users");
        myRef.orderByChild("firebaseId").equalTo(myUid).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for(DataSnapshot ds : snapshot.getChildren()){
                    Model_User user = ds.getValue(Model_User.class);
                    currentUser.postValue(user);
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    //TODO: usLiked
    public MutableLiveData<String> getMutIsLiked(String postId, String myUid) {

        checkIsLiked(postId, myUid);

        return mutIsLiked;
    }

    private void checkIsLiked(String postId, String myUid) {
        DatabaseReference likesRef = FirebaseDatabase.getInstance().getReference().child("Likes");
        likesRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.child(postId).hasChild(myUid)){
                    //user has liked this post
                    isLiked = "true";
                }
                else{
                    //user has not liked this post
                    isLiked ="false";
                }
                mutIsLiked.postValue(isLiked);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

}
