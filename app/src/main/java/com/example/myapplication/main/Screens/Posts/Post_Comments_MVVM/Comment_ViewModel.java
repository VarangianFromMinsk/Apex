package com.example.myapplication.main.Screens.Posts.Post_Comments_MVVM;

import android.app.Application;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;

import com.example.myapplication.main.Models.Model_Comment;
import com.example.myapplication.main.Models.Model_Post;
import com.example.myapplication.main.Models.Model_User;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.HashMap;

public class Comment_ViewModel extends AndroidViewModel {

    private MutableLiveData<ArrayList<Model_Comment>> mutCommentList = new MutableLiveData<>();
    private MutableLiveData<Model_Post> currentPost = new MutableLiveData<>();
    private MutableLiveData<String> mutIsLiked = new MutableLiveData<>();

    private boolean mProcessLike = false;
    private boolean mProcessComment = false;


    private MutableLiveData<Model_User> currentUser = new MutableLiveData<>();

    public Comment_ViewModel(@NonNull Application application) {
        super(application);
    }

    //TODO: comment part
    public void loadComments(String postId){
        mutCommentList = Comment_Repository.getInstance().getCommentsList(postId);
    }

    public MutableLiveData<ArrayList<Model_Comment>> getMutCommentList() {
        return mutCommentList;
    }


    //TODO: current post part
    public void loadCurrentPost(String postId){
        currentPost = Comment_Repository.getInstance().getCurrentPost(postId);
    }

    public MutableLiveData<Model_Post> getCurrentPost() {
        return currentPost;
    }


    //TODO: current user part
    public void loadCurrentUser(String myUid){
        currentUser = Comment_Repository.getInstance().getCurrentUser(myUid);
    }

    public MutableLiveData<Model_User> getCurrentUser() {
        return currentUser;
    }

    //TODO: isLiked
    public void checkIsLiked(String postId, String myUid){
        mutIsLiked = Comment_Repository.getInstance().getMutIsLiked(postId, myUid);
    }

    public MutableLiveData<String> getMutIsLiked() {
        return mutIsLiked;
    }

    //TODO: like post action
    public void likeAction(boolean isItLikeAnim, String postId, String myUid, String pLikes){
        int time = 0;
        if(isItLikeAnim){
            time = 850;
        }
        else {
            time = 50;
        }
        int finalTime = time;

        mProcessLike = true;
        DatabaseReference likesRef = FirebaseDatabase.getInstance().getReference().child("Likes");
        DatabaseReference postsRef = FirebaseDatabase.getInstance().getReference().child("Posts");

        Thread thread = new Thread(){
            @Override
            public void run(){
                try{
                    sleep(finalTime);
                }catch (Exception ignored){
                }finally {
                    likesRef.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if(mProcessLike){
                                if(snapshot.child(postId).hasChild(myUid)){
                                    //already liked, so remove like
                                    if(!isItLikeAnim){
                                        postsRef.child(postId).child("pLikes").setValue(""+(Integer.parseInt(pLikes)-1));
                                        likesRef.child(postId).child(myUid).removeValue();
                                        mProcessLike = false;
                                    }

                                }
                                else{
                                    //not liked, like it
                                    postsRef.child(postId).child("pLikes").setValue(""+(Integer.parseInt(pLikes)+1));
                                    likesRef.child(postId).child(myUid).setValue("Liked");
                                    mProcessLike = false;
                                }
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });
                }
            }
        };
        thread.start();
    }

    //TODO: send comment action
    public void commentAction(String postId, String timeStamp, String comment, String myUid, String myEmail, String myAvatar, String myName){

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Posts").child(postId).child("Comments");

        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("cId", timeStamp);
        hashMap.put("comment", comment);
        hashMap.put("timestamp", timeStamp);
        hashMap.put("uid", myUid);
        hashMap.put("uEmail", myEmail);
        hashMap.put("uAvatar", myAvatar);
        hashMap.put("uName", myName);

        //put data
        ref.child(timeStamp).setValue(hashMap).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                updateCommentCount(postId);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.d("Error in send comment", String.valueOf(e));
            }
        });

    }

    private void updateCommentCount(String postId) {
        mProcessComment = true;
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Posts").child(postId);
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(mProcessComment){
                    String comments = "" + snapshot.child("pComments").getValue();
                    int newCommentBal = Integer.parseInt(comments) + 1;
                    ref.child("pComments").setValue("" + newCommentBal);
                    mProcessComment = false;
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    //TODO: delete post part
    public void deleteWithImageAction(String pImage, String pId){
        StorageReference picRef = FirebaseStorage.getInstance().getReferenceFromUrl(pImage);
        picRef.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                //TODO: image deleted, now lets delete database
                Query fQuery = FirebaseDatabase.getInstance().getReference("Posts").orderByChild("pId").equalTo(pId);
                fQuery.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for(DataSnapshot ds : snapshot.getChildren()){
                            ds.getRef().removeValue();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
            }
        });

    }

    public void deleteWithoutImageAction(String pId){
        Query fQuery = FirebaseDatabase.getInstance().getReference("Posts").orderByChild("pId").equalTo(pId);
        fQuery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for(DataSnapshot ds : snapshot.getChildren()){
                    //remove values from firebase where pId was matched
                    ds.getRef().removeValue();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}
