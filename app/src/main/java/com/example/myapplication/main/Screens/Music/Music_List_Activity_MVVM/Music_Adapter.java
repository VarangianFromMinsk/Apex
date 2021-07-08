package com.example.myapplication.main.Screens.Music.Music_List_Activity_MVVM;

import android.app.Activity;
import android.app.ActivityOptions;
import android.content.Context;
import android.content.Intent;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.myapplication.R;
import com.example.myapplication.main.Models.Model_Song;
import com.example.myapplication.main.Screens.Music.Music_Player_Activity_And_Service_MVP.Music_Player_Activity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class Music_Adapter extends RecyclerView.Adapter<Music_Adapter.RecyclerViewViewHolder> {

    private Context context;
    private ArrayList<Model_Song> arrayList = new ArrayList<>();
    private String myUid;

    public ArrayList<Model_Song> getArrayList() {
        return arrayList;
    }

    //TODO: use to update current data
    public void setMusicList(ArrayList<Model_Song> arrayList) {
        this.arrayList = arrayList;
        notifyDataSetChanged();
    }

    //TODO: main Constructor
    public Music_Adapter(Context context) {
        this.context = context;
    }

    @NonNull
    @Override
    public RecyclerViewViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.row_music_list,viewGroup, false);
        return new RecyclerViewViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerViewViewHolder holder, int position) {
        //TODO: get data
        Model_Song song = arrayList.get(position);
        String id = song.getUploadId();
        //TODO: get current user
        myUid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        try{
            Glide.with(holder.imageView.getContext()).load(song.getAlbumUrl()).into(holder.imageView);
        }catch (Exception ignored){ }

        holder.mainTitle.setText(song.getSongMainTitle());
        holder.lastTitle.setText(song.getSongLastTitle());
        holder.songDuration.setText(song.getSongDuration());

        showLikesListener(id, holder);

    }

    private void showLikesListener(String id, RecyclerViewViewHolder holder) {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Likes_Songs");
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.child(id).child(myUid).exists()){
                    holder.likeIv.setImageResource(R.drawable.likeon);
                }
                else {
                    holder.likeIv.setImageResource(R.drawable.likeoff);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    @Override
    public int getItemCount() {  // Возвращаем кол-во элементов из ArrayList;
        return arrayList.size();
    }


    public class RecyclerViewViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{

        public ImageView imageView;
        public TextView mainTitle, lastTitle, songDuration;
        public int position;
        private final ImageView likeIv;


        public RecyclerViewViewHolder(@NonNull View itemView) {
            super(itemView);

            itemView.setOnClickListener(this);

            imageView = itemView.findViewById(R.id.imageView);
            mainTitle = itemView.findViewById(R.id.mainTitle);
            lastTitle = itemView.findViewById(R.id.lastTitle);
            songDuration = itemView.findViewById(R.id.songDuration);
            likeIv = itemView.findViewById(R.id.likeInMusicList);
        }

        @Override
        public void onClick(View v) {

            position=getAdapterPosition();
            Model_Song song =arrayList.get(position);

            Intent GoToPlayer = new Intent(context, Music_Player_Activity.class);

            GoToPlayer.putExtra("ImageUrl", song.getAlbumUrl());
            GoToPlayer.putExtra("mainTitle", song.getSongMainTitle());
            GoToPlayer.putExtra("lastTitle", song.getSongLastTitle());
            GoToPlayer.putExtra("songUrl", song.getSongUrl());
            GoToPlayer.putExtra("musicID", song.getUploadId());
            GoToPlayer.putExtra("songDuration", song.getSongDuration());
            GoToPlayer.putExtra("Position", position);

            Pair<View, String> pairImage = Pair.create(imageView, "album");
            Pair<View, String> pairMainTitle = Pair.create(mainTitle, "mainTitle");
            Pair<View, String> pairLastTitle = Pair.create(lastTitle, "lastTitle");

            ActivityOptions activityOptions = null;
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
                activityOptions = ActivityOptions.makeSceneTransitionAnimation((Activity) itemView.getContext(), pairImage, pairMainTitle, pairLastTitle );
                context.startActivity(GoToPlayer, activityOptions.toBundle());
            }
            else{
                context.startActivity(GoToPlayer);
            }

        }

    }


}
