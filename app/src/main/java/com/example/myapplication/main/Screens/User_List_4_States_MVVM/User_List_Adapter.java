package com.example.myapplication.main.Screens.User_List_4_States_MVVM;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.myapplication.R;
import com.example.myapplication.main.Models.Model_Message;
import com.example.myapplication.main.Models.Model_User;
import com.github.library.bubbleview.BubbleTextView;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;

public class User_List_Adapter extends RecyclerView.Adapter<User_List_Adapter.ChatUserViewHolder> {

    private final Context context;
    private ArrayList<Model_User> userList= new ArrayList<>();
    private final String myUid;
    private final DatabaseReference messagesDatabaseReference;

    private OnUserClickListener listener;

    private String theLastMessage;

    public ArrayList<Model_User> getUserList() {
        return userList;
    }

    //TODO: use to update current data
    public void setUserList(ArrayList<Model_User> userList) {
        this.userList = userList;
        notifyDataSetChanged();
    }

    public interface OnUserClickListener{
        void onUserClick(int position);     // метод внутри интерфейса
    }

    public User_List_Adapter(Context context) {
        this.context = context;

        this.myUid = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid();
        this.messagesDatabaseReference = FirebaseDatabase.getInstance().getReference().child("messages");
    }

    public void setOnUserClickListener(OnUserClickListener listener){
        this.listener = listener;
    }


    @NonNull
    @Override
    public ChatUserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_user, parent, false);
        return new ChatUserViewHolder(view,listener);
    }

    @Override
    public void onBindViewHolder(@NonNull ChatUserViewHolder holder, int position) {

        Model_User currentUser = userList.get(position);
        String hisUid = userList.get(position).getFirebaseId();

        //todo: set values
        holder.UserNameChatList.setText(currentUser.getName());
        holder.dayLastConnection.setText(currentUser.getDayOnline());
        holder.timeLastConnection.setText(currentUser.getTimeonline());
        holder.onOffLine.setText(currentUser.getOnline());

        //todo: init elements
        holder.addFriendBtn.setVisibility(View.GONE);
        holder.addFriendRequestBtn.setVisibility(View.VISIBLE);
        holder.addFriendRequestBtn.setImageResource(R.drawable.add_friends);
        holder.blockBtn.setVisibility(View.VISIBLE);
        holder.blockBtn.setImageResource(R.drawable.block);


        //todo: for chat with yourself
        chatWithYourself(currentUser, holder);

        showLastMessage(currentUser, holder);

        setColorOnlineOffline(currentUser, holder);

        loadAvatar(currentUser, holder);

        //todo: check user is blocked or not
        checkIsBlocked(hisUid, holder, position);

        //todo: check im blocked or not
        showIsImBlocked(hisUid, holder, position);

        blokeAction(holder, position, hisUid);

        //todo: staff to add request
        showIsUserGetRequest(hisUid, holder, position);

        //todo: staff to addFriend
        showIfUserIsFriend(hisUid, holder, position);
        showAddUserToFriendBtn(hisUid,holder,position);

        //todo: friends action
        addFriendsAction(holder, hisUid);

        //todo: start dialog action
        startDialogActionWithCheck(holder, hisUid, position);

    }

    private void showLastMessage(Model_User currentUser, ChatUserViewHolder holder) {
        theLastMessage = "'No message'";

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("messages");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for(DataSnapshot sn : snapshot.getChildren() ){

                    Model_Message message = sn.getValue(Model_Message.class);
                    assert message != null;

                    if(message.getRecipient().equals(myUid)  &&  message.getSender().equals(currentUser.getFirebaseId()) ||
                            message.getRecipient().equals(currentUser.getFirebaseId())  &&  message.getSender().equals(myUid)){
                        theLastMessage = message.getText();

                        if(theLastMessage != null){
                            holder.lastMessage.setText(String.valueOf("' " + theLastMessage + " '"));
                        }else if(message.getImageUrl() != null){
                            holder.lastMessage.setText(String.valueOf(" Image "));
                            holder.lastMessage.setTextSize(16);
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }

    private void startDialogActionWithCheck(ChatUserViewHolder holder, String hisUid, int position) {
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                imBlockedOrNot(hisUid, position, holder);
            }
        });
    }

    private void addFriendsAction(ChatUserViewHolder holder, String hisUid) {
        holder.addFriendRequestBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addFriendRequest(hisUid, holder);
            }
        });
    }

    private void blokeAction(ChatUserViewHolder holder, int position, String hisUid) {
        holder.blockBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(userList.get(position).isBlocked()){

                    unBlockedUser(hisUid, holder, position);

                    holder.addFriendRequestBtn.setVisibility(View.VISIBLE);
                    userList.get(position).setBlocked(false);
                }
                else{
                    blockedUser(hisUid, holder, position);

                    holder.addFriendRequestBtn.setVisibility(View.GONE);
                    holder.addFriendBtn.setVisibility(View.GONE);
                    holder.blockBtn.setImageResource(R.drawable.block_on);
                    //todo: staff of request
                    deleteFriendRequest(hisUid, holder);
                    deleteFriendRequestHis(hisUid, holder);
                    userList.get(position).setBlocked(true);
                }
            }
        });
    }

    private void loadAvatar(Model_User currentUser, ChatUserViewHolder holder) {
        try {
            if(!currentUser.getAvatarMockUpResourse().equals("")){
                //Picasso.get().load(currentUser.getAvatarMockUpResourse()).placeholder(R.drawable.default_avatar).into(holder.avatarImageView);
                Glide.with(holder.avatarImageView.getContext()).load(currentUser.getAvatarMockUpResourse()).into(holder.avatarImageView);
            }
            else{
                holder.avatarImageView.setImageResource(R.drawable.default_avatar);
            }
        }catch (Exception ignored){}
    }

    private void setColorOnlineOffline(Model_User currentUser, ChatUserViewHolder holder) {
        if(currentUser.getOnline().equals("online")){
            holder.onOffLine.setTextColor(Color.parseColor("#7AC537"));
        }else {
            holder.onOffLine.setTextColor(Color.parseColor("#D32121"));
        }
    }

    private void chatWithYourself(Model_User currentUser, ChatUserViewHolder holder) {
        if(currentUser.getFirebaseId().equals(myUid)){
            holder.UserNameChatList.setText(String.valueOf("Notes"));
            holder.dayLastConnection.setText("");
            holder.timeLastConnection.setText("");
            holder.blockBtn.setVisibility(View.GONE);
            holder.onOffLine.setVisibility(View.GONE);

            holder.addFriendRequestBtn.setVisibility(View.GONE);
            holder.addFriendBtn.setVisibility(View.GONE);
        }
    }

    private void showIsImBlocked(String hisUid, ChatUserViewHolder holder, int position) {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("users");
        ref.child(hisUid).child("BlockedUsers").orderByChild("uid").equalTo(myUid)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for(DataSnapshot ds : snapshot.getChildren()) {
                            if(ds.exists()){
                                //todo: blocked
                                holder.isImBlockedRed.setVisibility(View.VISIBLE);
                                holder.addFriendRequestBtn.setVisibility(View.GONE);
                                holder.addFriendBtn.setVisibility(View.GONE);
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                    }
                });
    }

    private void imBlockedOrNot(String hisUID, int position, ChatUserViewHolder holder){
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("users");
        ref.child(hisUID).child("BlockedUsers").orderByChild("uid").equalTo(myUid)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for(DataSnapshot ds : snapshot.getChildren()) {
                            if(ds.exists()){
                                //blocked
                                return;
                            }

                        }
                        //not blocked
                        //переходим в чат
                        if(listener != null){
                            if(position != RecyclerView.NO_POSITION){
                                listener.onUserClick(position);
                            }
                        }


                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                    }
                });
    }

    private void checkIsBlocked(String hisUid, ChatUserViewHolder holder, int position) {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("users");
        ref.child(myUid).child("BlockedUsers").orderByChild("uid").equalTo(hisUid)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for(DataSnapshot ds : snapshot.getChildren()) {
                            try{
                                if(ds.exists()){
                                    holder.addFriendRequestBtn.setVisibility(View.GONE);
                                    holder.blockBtn.setImageResource(R.drawable.block_on);
                                    //todo: staff of requests
                                    deleteFriendRequest(hisUid, holder);
                                    deleteFriendRequestHis(hisUid, holder);
                                    holder.addFriendBtn.setVisibility(View.GONE);
                                    userList.get(position).setBlocked(true);
                                }
                                else{
                                    holder.addFriendRequestBtn.setVisibility(View.VISIBLE);
                                    userList.get(position).setBlocked(false);
                                }
                            }catch (Exception ignored){}

                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                    }
                });
    }

    private void blockedUser(String hisUid, ChatUserViewHolder holder, int position) {
        //block the user, by adding to current user "blockedUsers" node
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("uid", hisUid);

        //delete friend
        deleteFriend(hisUid,holder, position);

        //add in BlockedUsers
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("users");
        ref.child(myUid).child("BlockedUsers").child(hisUid).setValue(hashMap).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                notifyItemChanged(position);
            }
        });

    }

    private void deleteFriend(String hisUid, ChatUserViewHolder holder, int position) {
        //delete in my path
        DatabaseReference refMy = FirebaseDatabase.getInstance().getReference("users");
        refMy.child(myUid).child("Friends").orderByChild("uid").equalTo(hisUid)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for(DataSnapshot ds : snapshot.getChildren()){
                            if(ds.exists()){
                                ds.getRef().removeValue();
                                notifyItemChanged(position);
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                    }
                });

        //delete in his path
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("users");
        ref.child(hisUid).child("Friends").orderByChild("uid").equalTo(myUid)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for(DataSnapshot ds : snapshot.getChildren()){
                            if(ds.exists()){
                                ds.getRef().removeValue();
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                    }
                });
    }

    private void unBlockedUser(String hisUid, ChatUserViewHolder holder, int position) {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("users");
        ref.child(myUid).child("BlockedUsers").orderByChild("uid").equalTo(hisUid)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for(DataSnapshot ds : snapshot.getChildren()){
                            if(ds.exists()){
                                ds.getRef().removeValue();
                                notifyItemChanged(position);
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                    }
                });
    }

    private void addFriendRequest(String hisUid, ChatUserViewHolder holder) {
        //add the user, by adding to current user "blockedUsers" node
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("uid", myUid);

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("users");
        ref.child(hisUid).child("RequestsToFriends").child(myUid).setValue(hashMap);

    }

    private void deleteFriendRequest(String hisUid, ChatUserViewHolder holder) {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("users");
        ref.child(myUid).child("RequestsToFriends").orderByChild("uid").equalTo(hisUid)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for(DataSnapshot ds : snapshot.getChildren()){
                            if(ds.exists()){
                                ds.getRef().removeValue();
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                    }
                });
    }

    private void deleteFriendRequestHis(String hisUid, ChatUserViewHolder holder) {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("users");
        ref.child(hisUid).child("RequestsToFriends").orderByChild("uid").equalTo(myUid)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for(DataSnapshot ds : snapshot.getChildren()){
                            if(ds.exists()){
                                ds.getRef().removeValue();
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                    }
                });
    }

    private void showIsUserGetRequest(String hisUid, ChatUserViewHolder holder, int position) {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("users");
        ref.child(hisUid).child("RequestsToFriends").orderByChild("uid").equalTo(myUid)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for(DataSnapshot ds : snapshot.getChildren()) {
                            if(ds.exists()){
                                //blocked
                                holder.addFriendRequestBtn.setImageResource(R.drawable.user_friend_request);
                            }
                            else{
                                holder.addFriendRequestBtn.setImageResource(R.drawable.add_friends);
                            }

                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                    }
                });
    }

    private void showIfUserIsFriend(String hisUid, ChatUserViewHolder holder, int position) {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("users");
        ref.child(myUid).child("Friends").orderByChild("uid").equalTo(hisUid)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for(DataSnapshot ds : snapshot.getChildren()) {
                            if(ds.exists()){
                                holder.addFriendRequestBtn.setImageResource(R.drawable.user_friend);
                                holder.addFriendBtn.setVisibility(View.GONE);
                            }
                            else{
                                holder.addFriendRequestBtn.setImageResource(R.drawable.add_friends);
                            }


                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                    }
                });
    }

    private void showAddUserToFriendBtn(String hisUid, ChatUserViewHolder holder, int position){
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("users");
        ref.child(myUid).child("RequestsToFriends").orderByChild("uid").equalTo(hisUid)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for(DataSnapshot ds : snapshot.getChildren()) {
                            if(ds.exists()){
                                holder.addFriendBtn.setVisibility(View.VISIBLE);
                                holder.addFriendRequestBtn.setImageResource(R.drawable.user_friend_request);
                                addUserToFriend(hisUid,holder);
                            }
                            else{
                                holder.addFriendRequestBtn.setImageResource(R.drawable.add_friends);

                            }

                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                    }
                });
    }

    private void addUserToFriend(String hisUid, ChatUserViewHolder holder) {

        holder.addFriendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //delete User from Request in My
                deleteFriendRequest(hisUid, holder);

                //delete User from Request in his
                deleteFriendRequestHis(hisUid, holder);

                //add user in Friends by my
                HashMap<String, Object> hashMapMy = new HashMap<>();
                hashMapMy.put("uid", hisUid);
                DatabaseReference refMy = FirebaseDatabase.getInstance().getReference("users");
                refMy.child(myUid).child("Friends").child(hisUid).setValue(hashMapMy);

                //add user in Friends by his
                HashMap<String, Object> hashMapHis = new HashMap<>();
                hashMapHis.put("uid", myUid);
                DatabaseReference refHis = FirebaseDatabase.getInstance().getReference("users");
                refHis.child(hisUid).child("Friends").child(myUid).setValue(hashMapHis);

                //notifyDataSetChanged();
            }
        });

    }


    @Override
    public int getItemCount() {
        return userList.size();
    }

    public static class ChatUserViewHolder extends RecyclerView.ViewHolder {

        private final ImageView avatarImageView;
        private final TextView UserNameChatList;
        private final TextView dayLastConnection;
        private final TextView timeLastConnection;
        private final TextView onOffLine;
        private final BubbleTextView lastMessage;
        private final ImageButton blockBtn;
        private final ImageButton addFriendRequestBtn;
        private final ImageButton addFriendBtn;
        private final ImageView isImBlockedRed;
        private ProgressBar progressBar;
        private final RecyclerView recyclerView;


        public ChatUserViewHolder(@NonNull View itemView, OnUserClickListener listener) {
            super(itemView);

            avatarImageView = itemView.findViewById(R.id.avatarImageView);
            UserNameChatList = itemView.findViewById(R.id.UserNameChatList);
            dayLastConnection = itemView.findViewById(R.id.dayLastConnection);
            timeLastConnection = itemView.findViewById(R.id.timeLastConnection);
            onOffLine = itemView.findViewById(R.id.onOffLine);
            lastMessage = itemView.findViewById(R.id.userLastMessage);
            blockBtn = itemView.findViewById(R.id.blockBtn);
            addFriendRequestBtn =  itemView.findViewById(R.id.addRequestToFriend);
            addFriendBtn =  itemView.findViewById(R.id.addToFriend);
            isImBlockedRed = itemView.findViewById(R.id.blockByAnotherUser);
            recyclerView = itemView.findViewById(R.id.userListRecyclerView);


            /*itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(listener != null){
                        int position = getAdapterPosition();
                        if(position != RecyclerView.NO_POSITION){
                            listener.onUserClick(position);
                        }
                    }
                }
            });

             */
        }
    }

}
