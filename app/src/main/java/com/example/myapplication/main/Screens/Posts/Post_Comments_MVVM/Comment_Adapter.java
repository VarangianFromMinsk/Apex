package com.example.myapplication.main.Screens.Posts.Post_Comments_MVVM;

import android.app.Activity;
import android.app.ActivityOptions;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.text.format.DateFormat;
import android.util.Pair;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.myapplication.R;
import com.example.myapplication.main.Models.Model_Comment;
import com.example.myapplication.main.Screens.User_Profile_MVVM.User_Profile_Activity;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

import de.hdodenhof.circleimageview.CircleImageView;

public class Comment_Adapter extends RecyclerView.Adapter<Comment_Adapter.HyHolder> {

    private final Context context;
    private ArrayList<Model_Comment> commentList = new ArrayList<>();
    private final String myUid;
    private final String postId;
    private final Activity activity;

    public ArrayList<Model_Comment> getCommentList() {
        return commentList;
    }

    //TODO: use to update current data
    public void setCommentList(ArrayList<Model_Comment> commentList) {
        this.commentList = commentList;
        notifyDataSetChanged();
    }

    //TODO: main Constructor
    public Comment_Adapter(Context context, String myUid, String postId, Activity activity) {
        this.context = context;
        this.myUid = myUid;
        this.postId = postId;
        this.activity = activity;
    }

    @NonNull
    @Override
    public HyHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.row_comment, parent, false);
        return new HyHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HyHolder holder, int position) {
        //TODO: get data
        String uid = commentList.get(position).getUid();
        String name = commentList.get(position).getuName();
        String email = commentList.get(position).getuEmail();
        String image = commentList.get(position).getuAvatar();
        String cid = commentList.get(position).getcId();
        String comment = commentList.get(position).getComment();
        String timestamp = commentList.get(position).getTimestamp();

        Calendar calendar = Calendar.getInstance(Locale.getDefault());
        calendar.setTimeInMillis(Long.parseLong(timestamp));
        String pTime = DateFormat.format("dd/MM/yyyy hh:mm aa", calendar).toString();

        //TODO: set data
        holder.nameTv.setText(name);
        holder.commentTv.setText(comment);
        holder.timeTv.setText(pTime);

        //TODO: setAvatar
        initAvatar(holder, image);


        //TODO: comments click listener
        initCommentClickListener(holder, uid, cid);

    }


    private void initAvatar(HyHolder holder, String image) {
        try{
            //Picasso.get().load(image).placeholder(R.drawable.default_avatar).into(holder.avatarIv);
            Glide.with(holder.avatarIv.getContext()).load(image).placeholder(R.drawable.default_avatar).into(holder.avatarIv);
        }catch (Exception ignored){
        }
    }

    private void initCommentClickListener(HyHolder holder, String uid, String cid) {
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PopupMenu popupMenu = new PopupMenu(context,holder.itemView, Gravity.END);
                //TODO: check is comment bu currentUser
                if(myUid.equals(uid)){
                    popupMenu.getMenu().add(Menu.NONE,0,0,"Delete");
                    popupMenu.getMenu().add(Menu.NONE,1,0,"Change");
                }
                else{
                    //TODO: Stranger profile
                    popupMenu.getMenu().add(Menu.NONE,2,0,"Check profile");
                    popupMenu.getMenu().add(Menu.NONE,3,0,"Report");
                }

                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        int id = item.getItemId();
                        if(id == 0){
                            AlertDialog.Builder builder = new AlertDialog.Builder(v.getRootView().getContext());
                            builder.setTitle("Delete");
                            builder.setMessage("Are you sure to delete comment?");
                            builder.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    deleteComment(cid);
                                }
                            });
                            builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                }
                            });
                            builder.create().show();

                        }
                        else if(id == 1){
                            //TODO working
                        }
                        else if(id==2){
                            //TODO: show profile
                            Intent GoToProfile = new Intent(activity, User_Profile_Activity.class);
                            GoToProfile.putExtra("hisId", uid);

                            Pair<View, String> pairPostImage = Pair.create(holder.avatarIv, "avatar");

                            ActivityOptions activityOptions = null;
                            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
                                activityOptions = ActivityOptions.makeSceneTransitionAnimation((Activity) activity , pairPostImage);
                                activity.startActivity(GoToProfile, activityOptions.toBundle());
                            }
                            else{
                                activity.startActivity( GoToProfile);
                            }

                        }
                        else if(id==3){
                            Toast.makeText(context, "Report accepted", Toast.LENGTH_SHORT).show();
                        }
                        return false;
                    }
                });
                popupMenu.show();

            }
        });
    }

    private void deleteComment(String cid) {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Posts").child(postId);
        ref.child("Comments").child(cid).removeValue();

        //TODO: update comments count
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String comments = "" + snapshot.child("pComments").getValue();
                int newCommentBal = Integer.parseInt(comments) - 1;
                ref.child("pComments").setValue("" + newCommentBal);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    @Override
    public int getItemCount() {
        return commentList.size();
    }

    public static class HyHolder extends RecyclerView.ViewHolder{

        private final CircleImageView avatarIv;
        private final TextView nameTv;
        private final TextView commentTv;
        private final TextView timeTv;


        public HyHolder(@NonNull View itemView) {
            super(itemView);
            avatarIv = itemView.findViewById(R.id.avatarIv);
            nameTv = itemView.findViewById(R.id.nameTv);
            commentTv = itemView.findViewById(R.id.commentTv);
            timeTv = itemView.findViewById(R.id.timeTv);
        }
    }
}
