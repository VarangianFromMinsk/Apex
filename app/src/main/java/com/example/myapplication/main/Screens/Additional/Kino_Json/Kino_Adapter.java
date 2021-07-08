package com.example.myapplication.main.Screens.Additional.Kino_Json;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.bumptech.glide.Glide;
import com.example.myapplication.R;

import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class Kino_Adapter extends RecyclerView.Adapter<Kino_Adapter.KinoViewHolder> {

    private Context context;
    private ArrayList<Model_Kino> kinoItems;

    public Kino_Adapter(Context context, ArrayList<Model_Kino> kinoItems) {
        this.context = context;
        this.kinoItems = kinoItems;

    }

    @NonNull
    @Override
    public KinoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(context).inflate(R.layout.row_kino, parent, false);

        return new KinoViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull KinoViewHolder holder, int position) {
        Model_Kino currentKino = kinoItems.get(position); // получили данные

        String title = currentKino.getTitle();   // перевели их в строки
        String year = currentKino.getYear();
        String type = currentKino.getType();
        String posterUrl = currentKino.getPosterUrl();

        holder.titleTextView.setText(title);
        holder.typeTextView.setText(type);
        holder.yearTextView.setText(year);
        Glide.with(holder.posterImageView.getContext()).load(posterUrl).into(holder.posterImageView);
    }

    @Override
    public int getItemCount() {     // Чтобы в RecyclerView было же столько элементов,сколько в Arraylist
        return kinoItems.size();
    }

    public class KinoViewHolder extends RecyclerView.ViewHolder {

        ImageView posterImageView;
        TextView titleTextView;
        TextView yearTextView;
        TextView typeTextView;

        public KinoViewHolder(@NonNull View itemView) {
            super(itemView);

            posterImageView = itemView.findViewById(R.id.posterImageView);
            titleTextView = itemView.findViewById(R.id.titleKino);
            yearTextView = itemView.findViewById(R.id.yearKino);
            typeTextView = itemView.findViewById(R.id.typeKino);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int position = getAdapterPosition();
                    Model_Kino modelKino = kinoItems.get(position);

                    Intent GoToFullInfo = new Intent(context, Full_Kino_Info_Activity.class);
                    GoToFullInfo.putExtra("id", modelKino.getId());

                    context.startActivity(GoToFullInfo);
                }
            });

        }
    }
}
