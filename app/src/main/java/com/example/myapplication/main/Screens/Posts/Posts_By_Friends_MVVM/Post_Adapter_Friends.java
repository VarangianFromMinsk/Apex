package com.example.myapplication.main.Screens.Posts.Posts_By_Friends_MVVM;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.AnimatedVectorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Handler;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.PopupMenu;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.RecyclerView;
import androidx.vectordrawable.graphics.drawable.AnimatedVectorDrawableCompat;

import com.bumptech.glide.Glide;
import com.example.myapplication.R;
import com.example.myapplication.databinding.RowPostsFriendsBinding;
import com.example.myapplication.main.Screens.Posts.Add_Change_Post_MVP.Add_Change_Post_Activity;
import com.example.myapplication.main.Models.Model_Post;
import com.example.myapplication.main.Screens.Posts.Post_Comments_MVVM.Post_Comment_Activity;
import com.example.myapplication.main.Screens.User_List_4_States_MVVM.User_List_Activity;
import com.example.myapplication.main.Screens.User_Profile_MVVM.User_Profile_Activity;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;
import java.util.Objects;

import javax.inject.Inject;

import static android.content.Intent.FLAG_ACTIVITY_NEW_TASK;

public class Post_Adapter_Friends extends RecyclerView.Adapter<Post_Adapter_Friends.FriendsViewHolder> {

    private final Context context;
    private ArrayList<Model_Post> postList = new ArrayList<>();

    private final String myUid;
    private final DatabaseReference likesRef;
    private final DatabaseReference postsRef;

    private boolean mProcessLike = false;

    //TODO: for double click
    private boolean  doubleClick = false;
    private Drawable drawable;
    private boolean isItLikeAnim = false;


    public ArrayList<Model_Post> getPostList() {
        return postList;
    }

    //TODO: use to update current data
    public void setPostList(ArrayList<Model_Post> postList) {
        this.postList = postList;
        notifyDataSetChanged();
    }


    //TODO: main Constructor
    @Inject
    public Post_Adapter_Friends(Context context) {
        this.context = context;

        myUid = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid();
        likesRef = FirebaseDatabase.getInstance().getReference().child("Likes");
        postsRef = FirebaseDatabase.getInstance().getReference().child("Posts");
    }

    @NonNull
    @Override
    public FriendsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        RowPostsFriendsBinding rowPostsFriendsBinding = DataBindingUtil.inflate(LayoutInflater.from(parent.getContext()),
                R.layout.row_posts_friends,
                parent, false);
        return new FriendsViewHolder(rowPostsFriendsBinding);
    }

    @Override
    public void onBindViewHolder(@NonNull FriendsViewHolder holder, int position) {
        //TODO: get data
        String uid = postList.get(position).getUid();
        String uEmail = postList.get(position).getuEmail();
        String uName = postList.get(position).getuName();
        String uAvatar = postList.get(position).getuAvatar();
        String pId = postList.get(position).getpId();
        String pTitle = postList.get(position).getpTitle();
        String pDescription = postList.get(position).getpDescr();
        String pImage = postList.get(position).getpImage();
        String pTimeStamp = postList.get(position).getpTime();
        String pLikes = postList.get(position).getpLikes();
        String pComments = postList.get(position).getpComments();

        //TODO: set likes for each post
        showLikesListener(holder, pId, pLikes);


        //TODO:convert timestamp to normal data
        Calendar calendar = Calendar.getInstance(Locale.getDefault());
        String pTime = DateFormat.format("dd/MM/yyyy hh:mm aa", calendar).toString();

        //TODO: set other info
        holder.postBinding.uNameTv.setText(uName);
        holder.postBinding.pTimeTv.setText(pTime);
        holder.postBinding.pTitleTv.setText(pTitle);
        holder.postBinding.pDescriptionTv.setText(pDescription);
        holder.postBinding.pCommentsTv.setText(String.valueOf(pComments + " Comments"));


        //TODO: set user avatar
        setUserAvatar(holder, uAvatar);

        //TODO: set post image
        // if ref = "noImage" - hide ImageView
        boolean isPostWithImage = false;
        if(pImage.equals("noImage")){
            holder.postBinding.pImageIv.setVisibility(View.GONE);
        }
        else {
            isPostWithImage = true;

            holder.postBinding.pImageIv.setVisibility(View.VISIBLE);
            try{
                //Picasso.get().load(pImage).into(holder.pImageIv);
                Glide.with(holder.postBinding.pImageIv.getContext()).load(pImage).into(holder.postBinding.pImageIv);
            }catch (Exception ignored){}
        }


        //TODO: double click part
        initDoubleCLick(holder, isPostWithImage, position);


        //TODO: init button clicks
        holder.postBinding.moreBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showMoreOptions(holder.postBinding.moreBtn, uid, myUid, pId, pImage);
            }
        });

        //TODO: init likeBtn
        holder.postBinding.likeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isItLikeAnim = false;
                setLikesAction(position, holder);
            }
        });

        //TODO: init comments
        initCommentBtn(holder, pId);

        //TODO: init share
        initShareBtn(holder, pDescription, uName, pImage, pTime, pId);

        //TODO: init path to user profile
        initClickOnAvatar(holder, uid);

    }

    private void initClickOnAvatar(FriendsViewHolder holder, String uid) {
        holder.postBinding.uPictureIv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                assert user != null;
                String currentUser = user.getUid();
                if(!currentUser.equals(uid)){
                    Intent GoToProfile = new Intent(context, User_Profile_Activity.class)
                            .putExtra("hisId",uid)
                            .setFlags(FLAG_ACTIVITY_NEW_TASK);
                    context.startActivity( GoToProfile);
                }
                else{
                    Toast.makeText(context, "Its you", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void initShareBtn(FriendsViewHolder holder, String pDescription, String uName, String pImage, String pTime, String pId) {
        holder.postBinding.shareBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, User_List_Activity.class)
                        .putExtra("Text", pDescription)
                        .putExtra("pUserName",uName)
                        .putExtra("pImage",pImage)
                        .putExtra("pTime",pTime)
                        .putExtra("pId", pId)
                        .putExtra("typeOfUserList", "all")
                        .setFlags(FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(intent);
            }
        });
    }

    private void initCommentBtn(FriendsViewHolder holder, String pId) {
        holder.postBinding.commentBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, Post_Comment_Activity.class)
                        .putExtra("postId", pId)
                        .setFlags(FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(intent);
            }
        });
    }

    private void setUserAvatar(FriendsViewHolder holder, String uAvatar) {
        try{
            //Picasso.get().load(uAvatar).placeholder(R.drawable.default_avatar).into(holder.uPictureIv);
            Glide.with(holder.postBinding.uPictureIv.getContext()).load(uAvatar).placeholder(R.drawable.default_avatar).into(holder.postBinding.uPictureIv);
        }catch (Exception ignored){ }
    }

    private void showMoreOptions(ImageButton moreBtn, String uid, String myUid, String pId, String pImage) {
        PopupMenu popupMenu = new PopupMenu(context,moreBtn, Gravity.END);

        //show delete option is only created by current user
        if(uid.equals(myUid)){
            popupMenu.getMenu().add(Menu.NONE,0,0,"Delete");
            popupMenu.getMenu().add(Menu.NONE,1,0,"Change");
        }
        else{
            popupMenu.getMenu().add(Menu.NONE,3,0,"Report");
            popupMenu.getMenu().add(Menu.NONE,4,0,"Save");
        }
        popupMenu.getMenu().add(Menu.NONE,2,0,"View Comments");


        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                int id = item.getItemId();
                if(id == 0){
                    startDelete(pId,pImage);
                }
                else if(id == 1){
                    Intent intent = new Intent(context, Add_Change_Post_Activity.class)
                            .putExtra("key","editPost")
                            .putExtra("editPostId",pId)
                            .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    context.startActivity(intent);
                }
                else if(id==2){
                    Intent intent = new Intent(context, Post_Comment_Activity.class)
                            .putExtra("postId", pId)
                            .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    context.startActivity(intent);
                }
                else if(id==3){
                    Toast.makeText(context, "coming soon", Toast.LENGTH_SHORT).show();
                }
                else if(id==4){
                    Toast.makeText(context, "coming soon", Toast.LENGTH_SHORT).show();
                }
                return false;
            }
        });
        popupMenu.show();


    }

    private void initDoubleCLick(FriendsViewHolder holder, boolean isPostWithImage, int position) {
        if(isPostWithImage){
            holder.postBinding.pLikeClick.setVisibility(View.VISIBLE);

            Handler handler=new Handler();
            Runnable r=new Runnable(){
                @Override
                public void run(){
                    //Actions when Single Clicked
                    doubleClick=false;
                }
            };

            drawable = holder.postBinding.pLikeClick.getDrawable();

            holder.postBinding.postCardVIew.setOnClickListener(new View.OnClickListener() {
                @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
                @Override
                public void onClick(View v) {
                    Runnable r = new Runnable() {
                        @Override
                        public void run() {
                            doubleClick = false;
                        }
                    };

                    if (doubleClick) {
                        //double click logic
                        holder.postBinding.pLikeClick.setVisibility(View.VISIBLE);
                        holder.postBinding.pLikeClick.setAlpha(1.0f);
                        isItLikeAnim = true;

                        if(drawable instanceof AnimatedVectorDrawableCompat){
                            AnimatedVectorDrawableCompat dAnimCompat = (AnimatedVectorDrawableCompat) AppCompatResources.getDrawable(context,R.drawable.avd_apex_like);
                            holder.postBinding.pLikeClick.setImageDrawable(dAnimCompat);
                            assert dAnimCompat != null;
                            dAnimCompat.start();
                            setLikesAction(position, holder);

                        }
                        else if (drawable instanceof AnimatedVectorDrawable){
                            AnimatedVectorDrawable dAnim = (AnimatedVectorDrawable) AppCompatResources.getDrawable(context,R.drawable.avd_apex_like);
                            holder.postBinding.pLikeClick.setImageDrawable(dAnim);
                            assert dAnim != null;
                            dAnim.start();
                            setLikesAction(position, holder);
                        }

                        // isItLikeAnim = false;
                        doubleClick = false;

                    }else {
                        isItLikeAnim = false;
                        doubleClick=true;
                        handler.postDelayed(r, 500);
                    }

                }
            });

        }
        else{
            holder.postBinding.pLikeClick.setVisibility(View.GONE);
        }
    }

    private void setLikesAction(int position, FriendsViewHolder holder){
        int time = 0;
        if(isItLikeAnim){
            time = 850;
        }
        else {
            time = 50;
        }
        int finalTime = time;

        Thread thread = new Thread(){
            @Override
            public void run(){
                try{
                    sleep(finalTime);
                }catch (Exception ignored){
                }finally {
                    mProcessLike = true;

                    //get id of the post clicked
                    String postId = postList.get(position).getpId();

                    likesRef.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if(mProcessLike){
                                if(snapshot.child(postId).hasChild(myUid)){
                                    //TODO: already liked, so remove like
                                    if(!isItLikeAnim){
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
                                                        holder.postBinding.pLikesTv.setText(String.valueOf((number - 1) + " Likes"));
                                                    }

                                                }

                                            }

                                            @Override
                                            public void onCancelled(@NonNull DatabaseError error) {
                                                Log.d("Error in setLike", String.valueOf(error));
                                            }
                                        });
                                    }

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
                                                    holder.postBinding.pLikesTv.setText(String.valueOf((number + 1) + " Likes"));
                                                }

                                            }

                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError error) {
                                            Log.d("Error in setLike", String.valueOf(error));
                                        }
                                    });
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

    private void showLikesListener(FriendsViewHolder holder, String postKey, String pLikes) {
        likesRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.child(postKey).hasChild(myUid)){
                    //TODO: user has liked this post
                    holder.postBinding.likeBtn.setCompoundDrawablesWithIntrinsicBounds(R.drawable.likeon,0,0,0);
                    holder.postBinding.likeBtn.setText(R.string.Liked);
                    try{
                    holder.postBinding.pLikesTv.setText(String.valueOf(pLikes + " Likes"));
                    }catch (Exception ignored){}

                }
                else{
                    //TODO: user has not liked this post
                    holder.postBinding.likeBtn.setCompoundDrawablesWithIntrinsicBounds(R.drawable.likeoff,0,0,0);
                    holder.postBinding.likeBtn.setText(R.string.Like);
                    try{
                        holder.postBinding.pLikesTv.setText(String.valueOf(pLikes + " Likes"));
                    }catch (Exception ignored){}

                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.d("Error in show like", String.valueOf(error));
            }
        });

    }

    //TODO: Block of deleting post //Block of deleting post
    private void startDelete(String pId, String pImage) {
        //check image in post is exist
        if(pImage.equals("noImage")){
            //post is without image
            deleteWithoutImage(pId);
        }
        else{
            //post is with image
            deleteWithImage(pId, pImage);
        }
    }

    private void deleteWithImage(String pId, String pImage) {

        ProgressDialog pd = new ProgressDialog(context);
        pd.setMessage("Deleting");

        StorageReference picRef = FirebaseStorage.getInstance().getReferenceFromUrl(pImage);
        picRef.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                //todo: image deleted, now delete from database
                Query fQuery = FirebaseDatabase.getInstance().getReference("Posts").orderByChild("pId").equalTo(pId);
                fQuery.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for(DataSnapshot ds : snapshot.getChildren()){
                            //remove values from firebase where pId was matched
                            ds.getRef().removeValue();
                            pd.dismiss();
                            Toast.makeText(context, "Deleted Success", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                pd.dismiss();
            }
        });

    }

    private void deleteWithoutImage(String pId) {

        ProgressDialog pd = new ProgressDialog(context);
        pd.setMessage("Deleting");


        Query fQuery = FirebaseDatabase.getInstance().getReference("Posts").orderByChild("pId").equalTo(pId);
        fQuery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for(DataSnapshot ds : snapshot.getChildren()){
                    //todo: remove values from firebase where pId was matched
                    ds.getRef().removeValue();
                    pd.dismiss();
                    Toast.makeText(context, "Deleted Success", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }


    @Override
    public int getItemCount() {
        return postList.size();
    }


    public static class FriendsViewHolder extends RecyclerView.ViewHolder{

        private final RowPostsFriendsBinding postBinding;

        public FriendsViewHolder(RowPostsFriendsBinding rowPostsFriendsBinding) {
            super(rowPostsFriendsBinding.getRoot());

            this.postBinding = rowPostsFriendsBinding;

        }
    }
}
