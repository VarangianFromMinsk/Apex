package com.example.myapplication.main.Screens.Posts.Posts_By_Recommendation_MVVM;


import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.example.myapplication.R;
import com.example.myapplication.databinding.RowPostRecommendationsBinding;
import com.example.myapplication.main.Models.Model_Post;
import com.example.myapplication.main.Screens.Posts.Post_Comments_MVVM.Post_Comment_Activity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Objects;

import javax.inject.Inject;

public class Post_Adapter_Recommendations extends RecyclerView.Adapter<Post_Adapter_Recommendations.RecommendationsViewHolder>  {

    private final Context context;
    private ArrayList<Model_Post> postListRec = new ArrayList<>();

    private final String myUid;

    private boolean mProcessLike = false;
    private final DatabaseReference likesRef;
    private final DatabaseReference postsRef;

    public ArrayList<Model_Post> getPostListRec() {
        return postListRec;
    }

    //TODO: use to update current data
    public void setPostListRec(ArrayList<Model_Post> postListRec) {
        this.postListRec = postListRec;
        notifyDataSetChanged();
    }

    //TODO: main Constructor
    @Inject
    public Post_Adapter_Recommendations(Context context) {
        this.context = context;

        myUid = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid();
        likesRef = FirebaseDatabase.getInstance().getReference().child("Likes");
        postsRef = FirebaseDatabase.getInstance().getReference().child("Posts");
    }

    @NonNull
    @Override
    public RecommendationsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        RowPostRecommendationsBinding rowPostRecommendationsBinding= DataBindingUtil.inflate(LayoutInflater.from(parent.getContext()),
                R.layout.row_post_recommendations,
                parent, false);
        return new RecommendationsViewHolder(rowPostRecommendationsBinding);
    }

    @Override
    public void onBindViewHolder(@NonNull RecommendationsViewHolder holder, int position) {
        //TODO: get data
        String pImage = postListRec.get(position).getpImage();
        String pId = postListRec.get(position).getpId();
        String pLikes = postListRec.get(position).getpLikes();

        //TODO: set likes for each post
        showLikesListener(holder, pId, pLikes);

        //TODO: set post image
        try{
            Glide.with(holder.postBinding.pRecImageIv.getContext()).load(pImage).into(holder.postBinding.pRecImageIv);
        }catch (Exception ignored){}

        //TODO: init show more
        initShowMore(holder, pId);

        //TODO: init like method
        initLikeMethod(holder, position);
    }

    private void initLikeMethod(RecommendationsViewHolder holder, int position) {
        holder.postBinding.layToSetLike.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setLikesAction(position, holder);
            }
        });
    }

    private void initShowMore(RecommendationsViewHolder holder, String pId) {
        holder.postBinding.pRecImageIv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, Post_Comment_Activity.class);
                intent.putExtra("postId", pId);
                context.startActivity(intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
            }
        });
    }

    private void setLikesAction(int position, RecommendationsViewHolder holder){

        mProcessLike = true;

        //TODO: get id of the post clicked
        String postId = postListRec.get(position).getpId();

        likesRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(mProcessLike){
                    if(snapshot.child(postId).hasChild(myUid)){
                        //TODO: already liked, so remove like
                        likesRef.child(postId).child(myUid).removeValue();
                        postsRef.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                for(DataSnapshot ds : snapshot.getChildren()){
                                    Model_Post post = ds.getValue(Model_Post.class);
                                    assert post != null;
                                    if(post.getpId().equals(postId)){
                                        int number = Integer.parseInt(post.getpLikes()) ;
                                        postsRef.child(postId).child("pLikes").setValue("" + (number - 1));
                                        holder.postBinding.pLikesRecTv.setText(String.valueOf((number - 1)));

                                    }
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {
                                Log.d("Error likeAction rec", String.valueOf(error));
                            }
                        });

                    }
                    else if (!snapshot.child(postId).hasChild(myUid)){
                        //TODO: not liked, like it
                        likesRef.child(postId).child(myUid).setValue("Liked");
                        postsRef.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                for(DataSnapshot ds : snapshot.getChildren()){
                                    Model_Post post = ds.getValue(Model_Post.class);
                                    assert post != null;
                                    if(post.getpId().equals(postId)){
                                        int number = Integer.parseInt(post.getpLikes()) ;
                                        postsRef.child(postId).child("pLikes").setValue("" + (number + 1));
                                        holder.postBinding.pLikesRecTv.setText(String.valueOf((number + 1)));
                                    }
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {
                                Log.d("Error likeAction rec", String.valueOf(error));
                            }
                        });
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.d("Error likeAction rec", String.valueOf(error));
            }
        });

    }

    private void showLikesListener(RecommendationsViewHolder holder, String postKey, String pLikes) {
        likesRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.child(postKey).hasChild(myUid)){
                    //TODO: user has liked this post
                    holder.postBinding.likeIvRecommend.setImageResource(R.drawable.likeon);
                    try{
                        holder.postBinding.pLikesRecTv.setText(String.valueOf(pLikes));
                    }catch (Exception ignored){}
                }
                else{
                    //TODO: user has not liked this post
                    holder.postBinding.likeIvRecommend.setImageResource(R.drawable.likeoff);
                    try{
                        holder.postBinding.pLikesRecTv.setText(String.valueOf(pLikes));
                    }catch (Exception ignored){}
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.d("Error in show like rec", String.valueOf(error));
            }
        });

    }

    @Override
    public int getItemCount() {
        return postListRec.size();
    }


    public static class RecommendationsViewHolder extends RecyclerView.ViewHolder{

        private final RowPostRecommendationsBinding postBinding;

        public RecommendationsViewHolder(RowPostRecommendationsBinding rowPostRecommendationsBinding) {
            super(rowPostRecommendationsBinding.getRoot());
            this.postBinding = rowPostRecommendationsBinding;
        }
    }

}
