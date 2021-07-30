package com.example.myapplication.main.Screens.Posts.Posts_By_Recommendation_MVVM;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;

import com.example.myapplication.main.Models.Model_Post;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class  Recommendation_Repository {

    //todo: ленивая инициализация
   // private static Recommendation_Repository instance;

    //todo: неленивая инициализацтя
    //+ Простая и прозрачная реализация
    //+ Потокобезопасность
    //- Не ленивая инициализация
    public static final Recommendation_Repository instance = new Recommendation_Repository();

    private final ArrayList<Model_Post> postListFriendsRepArray = new ArrayList<>();
    private final DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Posts");
    private final MutableLiveData<ArrayList<Model_Post>> posts = new MutableLiveData<>();

    private boolean itsWork = false;

   //todo: ленивая инициализация ( избегаем )
   /* public static Recommendation_Repository getInstance(){
        if(instance == null){
            instance = new Recommendation_Repository();
        }
        return instance;
    }

    */


    public MutableLiveData<ArrayList<Model_Post>> getPostList(String searchText){

        loadPosts(searchText);

        //TODO: for example

        /*if(postListFriendsRepArray.size() == 0){
            loadPosts(searchText);
        }
         */

        return posts;
    }


    public void loadPosts(String searchText){
        //TODO: var to load only one time
        itsWork = true;

        postListFriendsRepArray.clear();

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Posts");
        //TODO: get all data from this ref
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(itsWork){
                    for(DataSnapshot ds : snapshot.getChildren()){
                        Model_Post post = ds.getValue(Model_Post.class);

                        assert post != null;

                        if(!post.getpImage().equals("noImage")){
                            if(!searchText.equals("no")){
                                if (post.getpTitle().toLowerCase().contains(searchText.toLowerCase())
                                        || post.getpDescr().toLowerCase().contains(searchText.toLowerCase())) {
                                    postListFriendsRepArray.add(0,post);
                                }
                            }
                            else{
                                postListFriendsRepArray.add(0,post);
                            }
                        }

                        itsWork = false;
                    }
                    posts.postValue(postListFriendsRepArray);
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.d("Cant load posts", String.valueOf(error));
            }
        });

    }


}
