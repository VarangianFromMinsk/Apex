package com.example.myapplication.main.Screens.Posts.Post_Comments_MVVM;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.cardview.widget.CardView;
import androidx.core.widget.NestedScrollView;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.vectordrawable.graphics.drawable.AnimatedVectorDrawableCompat;

import android.app.ActivityOptions;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.drawable.AnimatedVectorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Pair;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.myapplication.R;
import com.example.myapplication.Services.Online_Offline_Service;
import com.example.myapplication.main.Screens.Posts.Add_Change_Post_MVP.Add_Change_Post_Activity;
import com.example.myapplication.main.Models.Model_Comment;
import com.example.myapplication.main.Models.Model_Post;
import com.example.myapplication.main.Models.Model_User;
import com.example.myapplication.main.Screens.Posts.Posts_By_Friends_MVP.Post_Activity_Friends;
import com.example.myapplication.main.Screens.User_List_4_States_MVVM.User_List_Activity;
import com.example.myapplication.main.Screens.User_Profile_MVVM.User_Profile_Activity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import java.util.ArrayList;
import java.util.Objects;
import de.hdodenhof.circleimageview.CircleImageView;

public class Post_Comment_Activity extends AppCompatActivity {

    private String myUid, myEmail, myName, myAvatar, postId, pLikes, pDescr, hisName, pImage, pTime, uid;

    //todo: views
    private ImageView uPictureIv, pImageIv;
    private TextView uNameTv, pTimeTv, pTitleTv, pDescriptionTv, pLikesTv, pCommentsTv, noComments;
    private ImageButton moreBtn;
    private Button likeBtn, shareBtn;

    //todo: action views
    private ProgressDialog pd;
    private ProgressBar progressBar;
    private NestedScrollView scroll;

    //todo: add comment views
    private EditText commentEt;
    private ImageButton sendBtn;
    private CircleImageView cAvatarIv;

    //like part
    private CardView postCardView;
    private ImageView heartIv;

    //for double click
    private boolean  doubleClick = false;
    private Drawable drawable;
    private boolean isItLikeAnim = false;

    //todo: comment recycler
    private RecyclerView recyclerView;
    private LinearLayoutManager layoutManager;
    private Comment_Adapter commentAdapter;
    private DatabaseReference usersDatabaseReference;


    private Comment_ViewModel viewModel;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.post_comment_activity);

        Objects.requireNonNull(getSupportActionBar()).hide();

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        initialization();

        getMainIntent();

        viewModel = new ViewModelProvider(this).get(Comment_ViewModel.class);

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

        progressBar = (ProgressBar) findViewById(R.id.progressbarInUPostComment);

        uPictureIv = findViewById(R.id.uPictureIv);
        pImageIv = findViewById(R.id.pImageIv);
        uNameTv = findViewById(R.id.uNameTv);
        pTimeTv = findViewById(R.id.pTimeTv);
        pTitleTv = findViewById(R.id.pTitleTv);
        pDescriptionTv = findViewById(R.id.pDescriptionTv);
        pLikesTv = findViewById(R.id.pLikesTv);
        pCommentsTv = findViewById(R.id.pCommentsTv);
        moreBtn = findViewById(R.id.moreBtn);
        likeBtn = findViewById(R.id.likeBtn);
        shareBtn = findViewById(R.id.shareBtn);

        noComments = findViewById(R.id.noCommentsInPostComment);

        commentEt = findViewById(R.id.commentEt);
        sendBtn = findViewById(R.id.sendBtn);
        cAvatarIv = findViewById(R.id.cAvatarIv);

        scroll = (NestedScrollView) this.findViewById(R.id.scrollViewPostComment);

        postCardView = findViewById(R.id.cardInPostComment);
        heartIv = findViewById(R.id.pLikeClickComment);

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

    private void loadCurrentUserInfo() {
        viewModel.loadCurrentUser(myUid);
        viewModel.getCurrentUser().observe(this, new Observer<Model_User>() {
            @Override
            public void onChanged(Model_User user) {
                myAvatar = user.getAvatarMockUpResourse();
                myName = user.getName();
                myEmail = user.getEmail();
                try {
                    Glide.with(cAvatarIv.getContext()).load(myAvatar).placeholder(R.drawable.default_avatar).into(cAvatarIv);
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
                pTitleTv.setText(post.getpTitle());
                pDescriptionTv.setText(pDescr);
                pLikesTv.setText(String.valueOf(post.getpLikes() + " Likes"));
                pTimeTv.setText(pTime);
                pCommentsTv.setText(String.valueOf(post.getpComments() + " Comments"));
                uNameTv.setText(hisName);

                //TODO: set image
                if(pImage.equals("noImage")){
                    //TODO: hide imageView
                    heartIv.setVisibility(View.GONE);
                    pImageIv.setVisibility(View.GONE);
                }
                else {
                    heartIv.setVisibility(View.VISIBLE);
                    pImageIv.setVisibility(View.VISIBLE);

                    likeAnimation();

                    //TODO: set image in post
                    try{
                        Glide.with(pImageIv.getContext()).load(pImage).into(pImageIv);
                    }catch (Exception ignored){ }
                }

                //TODO: set avatar, who was posted that
                String hisAvatar = post.getuAvatar();
                try{
                    Glide.with(uPictureIv.getContext()).load(hisAvatar).placeholder(R.drawable.default_avatar).into(uPictureIv);
                }catch (Exception ignored){}
            }
        });
    }

    private void createRecyclerViewAndLoadComments() {
        //TODO: part of recyclerview
        recyclerView = findViewById(R.id.recyclerViewForComments);
        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        commentAdapter = new Comment_Adapter(this, myUid, postId, Post_Comment_Activity.this);
        commentAdapter.setCommentList(new ArrayList<Model_Comment>());
        recyclerView.setAdapter(commentAdapter);


        //TODO: part of VM observes
        viewModel.loadComments(postId);
        viewModel.getMutCommentList().observe(this, new Observer<ArrayList<Model_Comment>>() {
            @Override
            public void onChanged(ArrayList<Model_Comment> comments) {
                commentAdapter.setCommentList(comments);
                noComments.setVisibility(View.GONE);
                progressBar.setVisibility(View.GONE);
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
                    likeBtn.setCompoundDrawablesWithIntrinsicBounds(R.drawable.likeon,0,0,0);
                    likeBtn.setText(R.string.Liked);
                }
                else{
                    //user has not liked this post
                    likeBtn.setCompoundDrawablesWithIntrinsicBounds(R.drawable.likeoff,0,0,0);
                    likeBtn.setText(R.string.Like);
                }
            }
        });

    }

    private void likePostBtn(){
        likeBtn.setOnClickListener(new View.OnClickListener() {
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

        drawable = heartIv.getDrawable();

        postCardView.setOnClickListener(new View.OnClickListener() {
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
                    heartIv.setAlpha(1.0f);

                    if(drawable instanceof AnimatedVectorDrawableCompat){
                        isItLikeAnim = true;
                        //todo: action with data
                        likePostAction();
                        AnimatedVectorDrawableCompat dAnimCompat = (AnimatedVectorDrawableCompat) AppCompatResources.getDrawable(Post_Comment_Activity.this,R.drawable.avd_apex_like);
                        heartIv.setImageDrawable(dAnimCompat);
                        assert dAnimCompat != null;
                        dAnimCompat.start();

                    }
                    else if (drawable instanceof AnimatedVectorDrawable){
                        isItLikeAnim = true;
                        //todo: action with data
                        likePostAction();
                        AnimatedVectorDrawable dAnim = (AnimatedVectorDrawable) AppCompatResources.getDrawable(Post_Comment_Activity.this,R.drawable.avd_apex_like);
                        heartIv.setImageDrawable(dAnim);
                        assert dAnim != null;
                        dAnim.start();
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

    private void postCommentBtn(){
        sendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                postComment();
            }
        });
    }

    private void postComment() {

        String comment = commentEt.getText().toString().trim();

        //TODO: check validate
        if(TextUtils.isEmpty(comment)){
            Toast.makeText(this, "Comment is empty", Toast.LENGTH_LONG).show();
        }
        else {
            String timeStamp = String.valueOf(System.currentTimeMillis());
            viewModel.commentAction(postId, timeStamp, comment, myUid, myEmail, myAvatar, myName);
            commentEt.setText("");
        }

    }

    //TODO: additional staff
    private void scrollToBottom(){
        scroll.post(new Runnable() {
            @Override
            public void run() {
                scroll.fullScroll(ScrollView.FOCUS_DOWN);
            }
        });
    }

    private void initShareBtn() {
        shareBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Post_Comment_Activity.this, User_List_Activity.class);
                intent.putExtra("Text", pDescr);
                intent.putExtra("pUserName",hisName);
                intent.putExtra("pImage",pImage);
                intent.putExtra("pTime",pTime);
                intent.putExtra("pId", postId);
                startActivity(intent);
            }
        });
    }

    private void initShowProfile() {
        uPictureIv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!myUid.equals(uid)){
                    Intent GoToProfile = new Intent(Post_Comment_Activity.this, User_Profile_Activity.class);
                    GoToProfile.putExtra("hisId",uid);

                    Pair<View, String> pairPostImage = Pair.create(uPictureIv, "avatar");

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
        moreBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PopupMenu popupMenu = new PopupMenu(Post_Comment_Activity.this,moreBtn, Gravity.END);

                //show delete option is only created by current user
                if(uid.equals(myUid)){
                    moreBtn.setVisibility(View.VISIBLE);
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
                            Intent intent = new Intent(Post_Comment_Activity.this, Add_Change_Post_Activity.class);
                            intent.putExtra("key","editPost");
                            intent.putExtra("editPostId",postId);
                            startActivity(intent);
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
        startActivity(new Intent(Post_Comment_Activity.this, Post_Activity_Friends.class));
    }

    private void deleteWithoutImage(String pId) {
        viewModel.deleteWithoutImageAction(pId);
    }

    //TODO: Block online/offline
    public void updateUserStatus( String state){
        Online_Offline_Service service = new Online_Offline_Service();
        service.updateUserStatus(state, this);

    }

    @Override
    protected void onResume() {
        super.onResume();
        updateUserStatus("online");
    }

    @Override
    protected void onPause() {
        super.onPause();
        updateUserStatus("offline");
    }

}