package com.example.myapplication.main.Screens.Chat_Activity_MVP;

import android.app.Activity;
import android.app.ActivityOptions;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.util.Pair;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.bumptech.glide.Glide;
import com.example.myapplication.R;
import com.example.myapplication.main.Models.Model_Message;
import com.example.myapplication.main.Screens.Posts.Post_Comments_MVVM.Post_Comment_Activity;
import com.example.myapplication.main.Screens.Show_Image_MVP.Show_Image_Activity;
import com.example.myapplication.main.Screens.User_Profile_MVVM.User_Profile_Activity;
import com.github.library.bubbleview.BubbleImageView;
import com.github.library.bubbleview.BubbleTextView;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.card.MaterialCardView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;

public class Message_Adapter extends ArrayAdapter<Model_Message> {

    private final List<Model_Message> messages;
    private final Activity activity;

    private String imageForCrop;

    private String myUID;


    public Message_Adapter(Activity context, int resource, List<Model_Message> messages) {
        super(context, resource, messages);

        this.messages = messages;
        this.activity = context;
    }



    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        myUID = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid();

        //todo:  Я ПИШУ/МНЕ ПИШУТ
        ViewHolder viewHolder;
        LayoutInflater layoutInflater = (LayoutInflater) activity.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);

        Model_Message modelMessage = getItem(position);
        int layoutResource = 0;
        int viewType = getItemViewType(position);

        //todo: Вызываем переопределенный метод ниже (их 2)
        if(viewType == 0){
            layoutResource = R.layout.chat_my_message_item;
        } else {
            layoutResource = R.layout.chat_your_message_item;
        }

        if(convertView != null ){
            viewHolder = (ViewHolder) convertView.getTag();
        } else {
            convertView = layoutInflater.inflate(layoutResource, parent, false);
            viewHolder = new ViewHolder(convertView);
            convertView.setTag(viewHolder);
        }

        //todo: check type
        boolean isRecord = modelMessage.getIsThatRecord().equals("true");
        boolean isRepost = modelMessage.getIsThatRepost().equals("true");

        //todo: check is that simple message without image
        boolean isText = modelMessage.getImageUrl() == null;


        mainSwitchForType(isRecord,isRepost, isText, viewHolder, modelMessage);

        setAvatarImageInChat(viewHolder, modelMessage);

        setTransAnimation(viewHolder,modelMessage,parent);

        deleteOrChangeMessage(viewHolder, modelMessage,position);

        return convertView;
    }

    private void mainSwitchForType(boolean isRecord, boolean isRepost, boolean isText, ViewHolder viewHolder, Model_Message modelMessage) {
        if(isRecord){
            //todo: hide repost
            viewHolder.postName.setVisibility(View.GONE);
            viewHolder.postDescription.setVisibility(View.GONE);
            viewHolder.postImage.setVisibility(View.GONE);
            //todo: hide also
            viewHolder.photoImageView.setVisibility(View.GONE);
            viewHolder.messageTextView.setVisibility(View.GONE);

            isThatCurrentDay(modelMessage, viewHolder);
            viewHolder.timeOfMessage.setText(modelMessage.getTimeOfMessage());
        }
        else if(isRepost){
            viewHolder.timeOfMessage.setText(modelMessage.getTimeOfMessage());
            //todo: fill like post with image
            if(modelMessage.getText().equals("Repost deleted")){
                viewHolder.postName.setVisibility(View.VISIBLE);
                viewHolder.postName.setText(R.string.Repost_deleted);
            }
            else{
                viewHolder.postName.setText("Post by : " + modelMessage.getName());
                viewHolder.postDescription.setText(modelMessage.getText());
                viewHolder.postName.setVisibility(View.VISIBLE);
                viewHolder.postDescription.setVisibility(View.VISIBLE);
                //todo:check is that with image or not
                if(isText){
                    viewHolder.postImage.setVisibility(View.GONE);
                }
                else{
                    try{
                        viewHolder.postImage.setVisibility(View.VISIBLE);
                        Glide.with(viewHolder.postImage.getContext()).load(modelMessage.getImageUrl()).into(viewHolder.postImage);
                    }catch (Exception ignored){}
                }
            }

            isThatCurrentDay(modelMessage, viewHolder);
        }
        else{
            if(isText){
                //todo: hide post views
                viewHolder.postName.setVisibility(View.GONE);
                viewHolder.postDescription.setVisibility(View.GONE);
                viewHolder.postImage.setVisibility(View.GONE);

                viewHolder.messageTextView.setVisibility(View.VISIBLE);
                viewHolder.photoImageView.setVisibility(View.GONE);
                viewHolder.messageTextView.setText(modelMessage.getText());
                viewHolder.timeOfMessage.setText(modelMessage.getTimeOfMessage());

                isThatCurrentDay(modelMessage, viewHolder);

            } else {

                //hide post views
                viewHolder.postName.setVisibility(View.GONE);
                viewHolder.postDescription.setVisibility(View.GONE);
                viewHolder.postImage.setVisibility(View.GONE);

                viewHolder.messageTextView.setVisibility(View.GONE); // change if want to image with text in one message

                viewHolder.photoImageView.setVisibility(View.VISIBLE);
                Glide.with(viewHolder.photoImageView.getContext()).load(modelMessage.getImageUrl()).into(viewHolder.photoImageView);

                viewHolder.timeOfMessage.setText(modelMessage.getTimeOfMessage());

                isThatCurrentDay(modelMessage, viewHolder);


                //show image method
                viewHolder.photoImageView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        //new method
                        Intent intent = new Intent(activity, Show_Image_Activity.class);
                        intent.putExtra("imageURL", modelMessage.getImageUrl());

                        activity.startActivity(intent);

                        // old method
                        /*
                        if(isImageFullScreen) {
                            isImageFullScreen=false;

                            RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(
                                    RelativeLayout.LayoutParams.WRAP_CONTENT,
                                    RelativeLayout.LayoutParams.WRAP_CONTENT);
                            if(modelMessage.getSender().equals(myId)){
                                layoutParams.addRule(RelativeLayout.ALIGN_PARENT_END);
                            } else {
                                layoutParams.addRule(RelativeLayout.ALIGN_PARENT_START);
                                layoutParams.setMarginStart(70);
                            }
                            layoutParams.topMargin = 40;
                            viewHolder.photoImageView.setLayoutParams(layoutParams);
                            viewHolder.photoImageView.setAdjustViewBounds(true);
                        }else{
                            isImageFullScreen=true;
                            RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(
                                    RelativeLayout.LayoutParams.MATCH_PARENT,
                                    RelativeLayout.LayoutParams.MATCH_PARENT
                            );
                            if(modelMessage.getSender().equals(myId)){
                                layoutParams.setMarginStart(10);
                                layoutParams.addRule(RelativeLayout.ALIGN_PARENT_END);
                            } else {
                                layoutParams.addRule(RelativeLayout.ALIGN_PARENT_START);
                                layoutParams.setMarginStart(70);
                                layoutParams.setMarginEnd(10);
                            }
                                layoutParams.topMargin = 40;


                            viewHolder.photoImageView.setLayoutParams(layoutParams);
                            viewHolder.photoImageView.setAdjustViewBounds(true);
                        }

                         */
                    }
                });
            }
        }
    }

    private void deleteOrChangeMessage(ViewHolder viewHolder, Model_Message modelMessage, int position) {
        viewHolder.messageLayout.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                PopupMenu popupMenu = new PopupMenu(activity,viewHolder.messageLayout, Gravity.END);
                if(!modelMessage.getIsThatRepost().contains("true") && modelMessage.getSender().equals(myUID) ){
                    popupMenu.getMenu().add(Menu.NONE,0,0,"Delete");

                    if(modelMessage.getImageUrl() == null){
                        popupMenu.getMenu().add(Menu.NONE,1,0,"Change");
                    }
                }
                else if (!modelMessage.getIsThatRepost().contains("true")){
                    popupMenu.getMenu().add(Menu.NONE,2,0,"Save in Favorite");
                }

                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        int id = item.getItemId();
                        if(id == 0){

                            AlertDialog.Builder builder = new AlertDialog.Builder(activity);
                            builder.setTitle("Delete");
                            builder.setMessage("Are your sure?");
                            //delete button
                            builder.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {

                                    deleteMessage(position);
                                }
                            });
                            builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                }
                            });
                            builder.create().show();

                        }
                        else if(id == 1){
                            //working
                            changeMessage(position);
                        }
                        else if(id==2){
                            //working
                            Toast.makeText(activity, "Appear soon", Toast.LENGTH_LONG).show();
                        }
                        return false;
                    }
                });
                popupMenu.show();

                return true;
            }

        });
    }

    private void changeMessage(int position) {
        String currentMessageKey = messages.get(position).getKeyMessage();

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("messages");
        Query  query = ref.orderByChild("keyMessage").equalTo(currentMessageKey);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for(DataSnapshot ds : snapshot.getChildren()){
                    Model_Message message = ds.getValue(Model_Message.class);
                    try{
                        if( !ds.child("imageUrl").exists() && message.getSender().equals(myUID)){
                            String messageFromBase = message.getText();
                            //hide
                            Chat_Main_Activity.messageEditText.setVisibility(View.GONE);
                            Chat_Main_Activity.sendMessageButton.setVisibility(View.GONE);

                            //show new
                           Chat_Main_Activity.changeEditText.setVisibility(View.VISIBLE);
                            Chat_Main_Activity.changeBtn.setVisibility(View.VISIBLE);

                            //set changing text
                            Chat_Main_Activity.changeEditText.setText(messageFromBase);

                            Chat_Main_Activity.changeBtn.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    String newMessage = Chat_Main_Activity.changeEditText.getText().toString().trim();
                                    HashMap<String, Object> hashMap = new HashMap<>();
                                    hashMap.put("text",newMessage);
                                    ds.getRef().updateChildren(hashMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            //return state back
                                            Chat_Main_Activity.messageEditText.setVisibility(View.VISIBLE);
                                            Chat_Main_Activity.sendMessageButton.setVisibility(View.VISIBLE);

                                            Chat_Main_Activity.changeEditText.setVisibility(View.GONE);
                                            Chat_Main_Activity.changeBtn.setVisibility(View.GONE);
                                        }
                                    });
                                }
                            });



                        }
                    }catch (Exception ignored){}
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });


    }

    private void deleteMessage(int position) {

        String currentMessageKey = messages.get(position).getKeyMessage();

        DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference("messages");
        Query  query = dbRef.orderByChild("keyMessage").equalTo(currentMessageKey);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for(DataSnapshot ds : snapshot.getChildren()){
                    //Позволяем удалить только свое сообщение
                    if(ds.child("sender").getValue().toString().equals(myUID)) {
                        if(ds.child("imageUrl").exists()){
                            ds.getRef().removeValue();

                        }else{
                            HashMap<String, Object> hashMap = new HashMap<>();
                            hashMap.put("text","Msg deleted");
                            ds.getRef().updateChildren(hashMap);
                        }

                    }
                    else{
                        Toast.makeText(activity, "You can delete only your message", Toast.LENGTH_LONG).show();
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });

    }

    private void isThatCurrentDay(Model_Message modelMessage, ViewHolder viewHolder){
        //create check for locale
        Locale locale = new Locale("ru");
        Locale.setDefault(locale);
        Configuration config = activity.getBaseContext().getResources().getConfiguration();
        config.locale = locale;
        activity.getBaseContext().getResources().updateConfiguration(config,
                activity.getBaseContext().getResources().getDisplayMetrics());

        Calendar calForDate = Calendar.getInstance();
        SimpleDateFormat currentDate = new SimpleDateFormat("EEE, MMM d, ''yy", locale);
        String saveCurrentDate = currentDate.format(calForDate.getTime());

        try{
            if(modelMessage.getDayOfMessage().equals(saveCurrentDate)){
                viewHolder.dayOfMessage.setVisibility(View.GONE);

            }else{
                viewHolder.dayOfMessage.setText(modelMessage.getDayOfMessage());
            }
        }catch (Exception ignored){}
    }

    private void setTransAnimation(ViewHolder viewHolder, Model_Message modelMessage, ViewGroup parent) {
        //todo: 1
        try{
            viewHolder.postCardView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent GoToPostComment = new Intent(activity, Post_Comment_Activity.class);
                    GoToPostComment.putExtra("postId", modelMessage.getPostId());

                    Pair<View, String> pairPostImage = Pair.create(viewHolder.postImage, "postImage");

                    ActivityOptions activityOptions = null;
                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
                        activityOptions = ActivityOptions.makeSceneTransitionAnimation((Activity) parent.getContext(), pairPostImage);
                        activity.startActivity(GoToPostComment, activityOptions.toBundle());
                    }
                    else{
                        activity.startActivity(GoToPostComment);
                    }
                }
            });
        }catch (Exception ignored){}

        //todo: 2
        try{
            viewHolder.avatarImage.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent GoToProfile = new Intent(activity, User_Profile_Activity.class);
                    GoToProfile.putExtra("hisId", modelMessage.getSender());

                    Pair<View, String> pairPostImage = Pair.create(viewHolder.avatarImage, "avatar");

                    ActivityOptions activityOptions = null;
                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
                        activityOptions = ActivityOptions.makeSceneTransitionAnimation((Activity) parent.getContext(), pairPostImage);
                        activity.startActivity(GoToProfile, activityOptions.toBundle());
                    }
                    else{
                        activity.startActivity( GoToProfile);
                    }

                }
            });
        }catch (Exception ignored){}
    }

    private void setAvatarImageInChat(ViewHolder viewHolder, Model_Message modelMessage) {
        DatabaseReference usersDatabaseReference = FirebaseDatabase.getInstance().getReference().child("users");
        Query userQuery = usersDatabaseReference.orderByChild("firebaseId").equalTo(modelMessage.getSender());
        userQuery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for(DataSnapshot ds : snapshot.getChildren()){
                    String hisImage = "" + ds.child("avatarMockUpResourse").getValue();
                    if(!hisImage.equals("")){
                        try {
                            viewHolder.avatarImage.setVisibility(View.VISIBLE);
                            // Picasso.get().load(hisImage).placeholder(R.drawable.default_avatar).into(viewHolder.avatarImage);
                            Glide.with(viewHolder.avatarImage.getContext()).load(hisImage).into(viewHolder.avatarImage);

                        }catch (Exception ignored){}
                    }else{
                        try {
                            viewHolder.avatarImage.setVisibility(View.GONE);
                        }catch (Exception ignored){}
                    }
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }


    @Override
    public int getItemViewType(int position) {

        int flag;
        Model_Message modelMessage = messages.get(position);
        if(modelMessage.isMine()){
            flag = 0;
        } else {
            flag = 1;
        }

        return flag;

    }

    @Override
    public int getViewTypeCount() {
        return 2;    // Указываем сколько у нас разметок ( У НАС 2 РАЗМЕТКИ НА СООБЩЕНИЯ)
    }

    private static class ViewHolder {

        private final BubbleImageView photoImageView;
        private final BubbleTextView messageTextView;
        private final TextView timeOfMessage;
        private final TextView dayOfMessage;
        private final CircleImageView avatarImage;
        private final RelativeLayout messageLayout;

        // Vies for Repost
        private final TextView postName;
        private final TextView postDescription;
        private final ImageView postImage;
        private final MaterialCardView postCardView;



        public ViewHolder(View view){
            photoImageView = view.findViewById(R.id.photoImageView);
            messageTextView = view.findViewById(R.id.messageTextView);
            timeOfMessage = view.findViewById(R.id.timeOfMessage);
            dayOfMessage = view.findViewById(R.id.dayOfMessage);
            avatarImage = view.findViewById(R.id.avatarInChatImageView);
            messageLayout = view.findViewById(R.id.messageLayout);

            //init views for Repost
            postName = view.findViewById(R.id.repostName);
            postDescription = view.findViewById(R.id.repostDescription);
            TextView repostDeleted = view.findViewById(R.id.repostDeletedName);
            postImage = view.findViewById(R.id.repostImage);
            postCardView = view.findViewById(R.id.chatRepostCard);

        }

    }

}
