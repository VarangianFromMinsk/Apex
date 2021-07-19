package com.example.myapplication.main.Screens.Posts.Posts_By_Friends_MVP;

import androidx.annotation.NonNull;

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

import javax.inject.Inject;

public class Post_Friends_Presenter {

    private final ArrayList<Model_Post> postListFriends = new ArrayList<>();
    private String hisUid = "";
    private String myUid;
    private boolean itsWork;

    //TODO: инджектим зависимость через конструктор
    private final Post_List_view view;

    
    public Post_Friends_Presenter(Post_List_view view) {
        this.view = view;
    }



    public void loadData(String searchQuery){

        FirebaseAuth auth = FirebaseAuth.getInstance();
        myUid = auth.getUid();

        itsWork = true;

        postListFriends.clear();

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
                                                postListFriends.add(0,post);
                                                view.disableProgressBar();
                                            }
                                        }
                                        else{
                                            postListFriends.add(0,post);
                                            view.disableProgressBar();
                                        }
                                    }
                                    view.showData( postListFriends);
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {
                                view.disableProgressBar();
                                view.showSnackBarNoInternet("no internet, check settings");
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

                    if(checkForMainList.size() > postListFriends.size() ){
                        view.initShowNewPost();
                    }
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });

    }

}
