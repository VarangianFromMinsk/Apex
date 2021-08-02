package com.example.myapplication.main.Screens.Posts.Post_Comments_MVVM;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.vectordrawable.graphics.drawable.AnimatedVectorDrawableCompat;

import android.app.ActivityOptions;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.drawable.AnimatedVectorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.text.format.DateFormat;
import android.util.Pair;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.PopupMenu;
import android.widget.ScrollView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.myapplication.Common_Dagger_App_Class.App;
import com.example.myapplication.R;
import com.example.myapplication.Services.Check_Internet_Connection_Exist;
import com.example.myapplication.Services.Online_Offline_User_Service_To_Firebase;
import com.example.myapplication.databinding.PostCommentActivityBinding;
import com.example.myapplication.main.Models.Model_User;
import com.example.myapplication.main.Screens.Posts.Add_Change_Post_MVP.Add_Change_Post_Activity;
import com.example.myapplication.main.Models.Model_Comment;
import com.example.myapplication.main.Models.Model_Post;
import com.example.myapplication.main.Screens.Posts.Posts_By_Friends_MVP.Post_Activity_Friends;
import com.example.myapplication.main.Screens.Show_Image_MVP.Show_Image_Activity;
import com.example.myapplication.main.Screens.User_List_4_States_MVVM.User_List_Activity;
import com.example.myapplication.main.Screens.User_Profile_MVVM.User_Profile_Activity;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;
import java.util.Objects;

import javax.inject.Inject;

public class Post_Comment_Activity extends AppCompatActivity {

    private String myUid, myEmail, myName, myAvatar, postId, pLikes, pDescr, hisName, pImage, pTime, uid;

    //todo: for double click
    private boolean  doubleClick = false;
    private Drawable drawable;
    private boolean isItLikeAnim = false;

    @Inject
    LinearLayoutManager layoutManager;

    @Inject
    Check_Internet_Connection_Exist checkInternetService;

    @Inject
    Online_Offline_User_Service_To_Firebase controller;

    private PostCommentActivityBinding binding;
    private Comment_ViewModel viewModel;
    private Comment_Adapter commentAdapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.post_comment_activity);

        Objects.requireNonNull(getSupportActionBar()).hide();

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        initialization();

        getMainIntent();

        checkNetworkState();

        loadCurrentUserInfo();

        loadPostInfo();

        createRecyclerViewAndLoadComments();

        checkLikeListener();

        postCommentBtn();

        initShowProfile();

        initShareBtn();

        likePostBtn();

        initMoreBtn();

        scrollToBottom();
    }

    //TODO: main methods
    private void initialization(){

        ((App) getApplication()).getCommonComponent().inject(this);

        binding = DataBindingUtil.setContentView(this, R.layout.post_comment_activity);

        viewModel = new ViewModelProvider(this).get(Comment_ViewModel.class);

        //TODO: lifecycle
        getLifecycle().addObserver(controller);

        //TODO: get current user id
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        assert user != null;
        myUid = user.getUid();
    }

    private void getMainIntent(){
        Intent intent =getIntent();
        Bundle extras = intent.getExtras();
        if(extras != null){
            postId = intent.getStringExtra("postId");
        }
    }

    private void checkNetworkState() {
        if(!checkInternetService.checkInternet(this)){
            showSnackBar("Internet disable", null, null);
        }
    }

    private void loadCurrentUserInfo() {
        viewModel.loadCurrentUser(myUid);
        viewModel.getCurrentUser().observe(this, new Observer<Model_User>() {
            @Override
            public void onChanged(Model_User user) {
                myAvatar = user.getAvatarMockUpResourse();
                myName = user.getName();
                myEmail = user.getEmail();
                try {
                    Glide.with(binding.cAvatarIv.getContext()).load(myAvatar).placeholder(R.drawable.default_avatar).into(binding.cAvatarIv);
                    //Picasso.get().load(myAvatar).placeholder(R.drawable.default_avatar).into(cAvatarIv);
                }catch (Exception ignored){}
            }
        });


    }

    private void loadPostInfo() {
        viewModel.loadCurrentPost(postId);
        viewModel.getCurrentPost().observe(this, new Observer<Model_Post>() {
            @Override
            public void onChanged(Model_Post post) {
                //TODO: get data
                pLikes = post.getpLikes();
                pDescr = post.getpDescr();
                hisName = post.getuName();
                pImage = post.getpImage();
                pTime = post.getpTime();
                uid = post.getUid();

                //TODO: set data
                binding.pTitleTv.setText(post.getpTitle());
                binding.pDescriptionTv.setText(pDescr);
                binding.pLikesTv.setText(String.valueOf(post.getpLikes() + " Likes"));

                Calendar calendar = Calendar.getInstance(Locale.getDefault());
                calendar.setTimeInMillis(Long.parseLong(pTime));
                String pTimeCorrect = DateFormat.format("dd/MM/yyyy hh:mm aa", calendar).toString();
                binding.pTimeTv.setText(pTimeCorrect);

                binding.pCommentsTv.setText(String.valueOf(post.getpComments() + " Comments"));
                binding.uNameTv.setText(hisName);

                //TODO: set image
                if(pImage.equals("noImage")){
                    //TODO: hide imageView
                    binding.pLikeClickComment.setVisibility(View.GONE);
                    binding.pImageIv.setVisibility(View.GONE);
                }
                else {
                    binding.pLikeClickComment.setVisibility(View.VISIBLE);
                    binding.pImageIv.setVisibility(View.VISIBLE);

                    likeAnimation();

                    //TODO: set image in post
                    try{
                        Glide.with(binding.pImageIv.getContext()).load(pImage).into(binding.pImageIv);
                    }catch (Exception ignored){ }
                }

                //TODO: set avatar, who was posted that
                String hisAvatar = post.getuAvatar();
                try{
                    Glide.with(binding.uPictureIv.getContext()).load(hisAvatar).placeholder(R.drawable.default_avatar).into(binding.uPictureIv);
                }catch (Exception ignored){}
            }
        });
    }

    private void createRecyclerViewAndLoadComments() {
        //layoutManager = new LinearLayoutManager(this);
        binding.recyclerViewForComments.setLayoutManager(layoutManager);
        commentAdapter = new Comment_Adapter(this, myUid, postId, Post_Comment_Activity.this);
        commentAdapter.setCommentList(new ArrayList<Model_Comment>());
        binding.recyclerViewForComments.setAdapter(commentAdapter);


        //TODO: part of VM observes
        viewModel.loadComments(postId);
        viewModel.getMutCommentList().observe(this, new Observer<ArrayList<Model_Comment>>() {
            @Override
            public void onChanged(ArrayList<Model_Comment> comments) {
                commentAdapter.setCommentList(comments);
                binding.noCommentsInPostComment.setVisibility(View.GONE);
                binding.progressbarInUPostComment.setVisibility(View.GONE);
            }
        });
    }

    private void checkLikeListener() {
        viewModel.checkIsLiked(postId, myUid);
        viewModel.getMutIsLiked().observe(this, new Observer<String>() {
            @Override
            public void onChanged(String state) {
                if(state.equals("true")){
                    //user has liked this post
                    binding.likeBtn.setCompoundDrawablesWithIntrinsicBounds(R.drawable.likeon,0,0,0);
                    binding.likeBtn.setText(R.string.Liked);
                }
                else{
                    //user has not liked this post
                    binding.likeBtn.setCompoundDrawablesWithIntrinsicBounds(R.drawable.likeoff,0,0,0);
                    binding.likeBtn.setText(R.string.Like);
                }
            }
        });

    }

    private void likePostBtn(){
        binding.likeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isItLikeAnim = false;
                likePostAction();
            }
        });

    }

    private void likePostAction() {
        viewModel.likeAction(isItLikeAnim, postId, myUid, pLikes);
    }

    private void likeAnimation() {

        Handler handler=new Handler();
        Runnable r=new Runnable(){
            @Override
            public void run(){
                //Actions when Single Clicked
                doubleClick=false;
            }
        };

        drawable = binding.pLikeClickComment.getDrawable();

        binding.cardInPostComment.setOnClickListener(new View.OnClickListener() {
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
                    binding.pLikeClickComment.setAlpha(1.0f);

                    if(drawable instanceof AnimatedVectorDrawableCompat){
                        isItLikeAnim = true;
                        //todo: action with data
                        likePostAction();
                        AnimatedVectorDrawableCompat dAnimCompat = (AnimatedVectorDrawableCompat) AppCompatResources.getDrawable(Post_Comment_Activity.this,R.drawable.avd_apex_like);
                        binding.pLikeClickComment.setImageDrawable(dAnimCompat);
                        assert dAnimCompat != null;
                        dAnimCompat.start();

                    }
                    else if (drawable instanceof AnimatedVectorDrawable){
                        isItLikeAnim = true;
                        //todo: action with data
                        likePostAction();
                        AnimatedVectorDrawable dAnim = (AnimatedVectorDrawable) AppCompatResources.getDrawable(Post_Comment_Activity.this,R.drawable.avd_apex_like);
                        binding.pLikeClickComment.setImageDrawable(dAnim);
                        assert dAnim != null;
                        dAnim.start();
                    }

                    // isItLikeAnim = false;
                    doubleClick = false;

                }else {
                    isItLikeAnim = false;
                    doubleClick=true;
                    handler.postDelayed(r, 500);

                    //todo: showImageFullScreen
                    Intent intent = new Intent(Post_Comment_Activity.this, Show_Image_Activity.class);
                    intent.putExtra("imageURL", pImage);
                    overridePendingTransition(0, 0);
                    startActivity(intent);
                }

            }
        });


    }

    private void postCommentBtn(){
        binding.sendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                postComment();
            }
        });
    }

    private void postComment() {

        String comment = binding.commentEt.getText().toString().trim();

        //TODO: check validate
        if(TextUtils.isEmpty(comment)){
            Toast.makeText(this, "Comment is empty", Toast.LENGTH_LONG).show();
        }
        else {
            String timeStamp = String.valueOf(System.currentTimeMillis());
            viewModel.commentAction(postId, timeStamp, comment, myUid, myEmail, myAvatar, myName);
            binding.commentEt.setText("");
        }

    }

    //TODO: additional staff
    private void scrollToBottom(){
        binding.scrollViewPostComment.post(new Runnable() {
            @Override
            public void run() {
                binding.scrollViewPostComment.fullScroll(ScrollView.FOCUS_DOWN);
            }
        });
    }

    private void initShareBtn() {
        binding.shareBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Post_Comment_Activity.this, User_List_Activity.class)
                        .putExtra("Text", pDescr)
                        .putExtra("pUserName",hisName)
                        .putExtra("pImage",pImage)
                        .putExtra("pTime",pTime)
                        .putExtra("pId", postId)
                        .putExtra("typeOfUserList", "all")
                        .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            }
        });
    }

    private void initShowProfile() {
        binding.uPictureIv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!myUid.equals(uid)){
                    Intent GoToProfile = new Intent(Post_Comment_Activity.this, User_Profile_Activity.class)
                            .putExtra("hisId",uid)
                            .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

                    Pair<View, String> pairPostImage = Pair.create(binding.uPictureIv, "avatar");

                    ActivityOptions activityOptions = null;
                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
                        activityOptions = ActivityOptions.makeSceneTransitionAnimation(Post_Comment_Activity.this, pairPostImage);
                        startActivity(GoToProfile, activityOptions.toBundle());
                    }
                    else{
                        startActivity( GoToProfile);
                    }

                }
                else{
                    Toast.makeText(Post_Comment_Activity.this, "Its you", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void initMoreBtn() {
        binding.moreBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PopupMenu popupMenu = new PopupMenu(Post_Comment_Activity.this,binding.moreBtn, Gravity.END);

                //show delete option is only created by current user
                if(uid.equals(myUid)){
                    binding.moreBtn.setVisibility(View.VISIBLE);
                    popupMenu.getMenu().add(Menu.NONE,0,0,"Delete");
                    popupMenu.getMenu().add(Menu.NONE,1,0,"Change");
                }
                else{
                    popupMenu.getMenu().add(Menu.NONE,2,0,"Report");
                    popupMenu.getMenu().add(Menu.NONE,3,0,"Save");
                }


                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        int id = item.getItemId();
                        if(id == 0){
                            startDelete(postId,pImage);
                        }
                        else if(id == 1){
                            Intent intent = new Intent(Post_Comment_Activity.this, Add_Change_Post_Activity.class)
                                    .putExtra("key","editPost")
                                    .putExtra("editPostId",postId)
                                    .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(intent);
                        }
                        else if(id == 2){
                            Toast.makeText(Post_Comment_Activity.this, "Coming soon", Toast.LENGTH_SHORT).show();
                        }
                        else if(id == 3){
                            Toast.makeText(Post_Comment_Activity.this, "Coming soon", Toast.LENGTH_SHORT).show();
                        }
                        return false;
                    }
                });
                popupMenu.show();
            }
        });
    }

    private void startDelete(String pId, String pImage) {
        //TODO: check image in post is exist
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
        viewModel.deleteWithImageAction(pImage, pId);
        startActivity(new Intent(Post_Comment_Activity.this, Post_Activity_Friends.class).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
    }

    private void deleteWithoutImage(String pId) {
        viewModel.deleteWithoutImageAction(pId);
        startActivity(new Intent(Post_Comment_Activity.this, Post_Activity_Friends.class).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
    }

    private void showSnackBar(final String mainText, final String action, View.OnClickListener listener) {
        Snackbar.make(findViewById(android.R.id.content), mainText, Snackbar.LENGTH_INDEFINITE).setAction(action, listener).show();
    }
}