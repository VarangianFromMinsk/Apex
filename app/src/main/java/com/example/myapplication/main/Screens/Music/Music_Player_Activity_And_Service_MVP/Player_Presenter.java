package com.example.myapplication.main.Screens.Music.Music_Player_Activity_And_Service_MVP;


import androidx.annotation.NonNull;
import com.example.myapplication.main.Models.Model_Song;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;

public class Player_Presenter {

    private final Player_view view;
    private boolean itsWorkMusic = false;
    private ArrayList<Model_Song> songList = new ArrayList<>();
    boolean mProcessLike = false;

    public Player_Presenter(Player_view view) {
        this.view = view;
    }


    public void loadMusicList(){
        //todo: var to load only one time
        itsWorkMusic = true;

        songList.clear();

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Music");
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(itsWorkMusic){
                    for(DataSnapshot ds : snapshot.getChildren()){
                        Model_Song song = ds.getValue(Model_Song.class);
                        songList.add(0,song);

                        itsWorkMusic = false;
                    }
                    view.dataLoadComplete(songList);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }

    public void likeSong(int number, String myUid){
        // int pLikes = Integer.parseInt(postList.get(position).getpLikes());
        mProcessLike = true;

        DatabaseReference likesRefMusic = FirebaseDatabase.getInstance().getReference("Likes_Songs");
        //get id of the post clicked
        String songId = songList.get(number).getUploadId();
        likesRefMusic.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(mProcessLike){
                    if(snapshot.child(songId).hasChild(myUid)){
                        //todo: already liked, so remove like
                        likesRefMusic.child(songId).child(myUid).removeValue();
                        mProcessLike = false;
                        view.showToastLike("Disliked");
                    }
                    else{
                        //todo: not liked, like it
                        HashMap<String, Object> hashMap = new HashMap<>();
                        hashMap.put(myUid, "Liked");
                        hashMap.put("songId", songId);
                        likesRefMusic.child(songId).setValue(hashMap);
                        mProcessLike = false;
                        view.showToastLike("Liked");
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    public void  showIsLike(String id, String myUid){
        DatabaseReference likesRefMusic = FirebaseDatabase.getInstance().getReference("Likes_Songs");
        likesRefMusic.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.child(id).hasChild(myUid)){
                    //todo: user has liked this post
                    view.showHeartIfLiked(true);
                }
                else{
                    //todo: user has not liked this post
                    view.showHeartIfLiked(false);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }


}
