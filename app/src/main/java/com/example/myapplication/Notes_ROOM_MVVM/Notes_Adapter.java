package com.example.myapplication.Notes_ROOM_MVVM;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.R;

import java.util.List;

public class Notes_Adapter extends RecyclerView.Adapter<Notes_Adapter.NotesViewHolder> {

    private List<Model_Note> notes;
    private onNoteClickListener onNoteClickListener;


    public Notes_Adapter(List<Model_Note> notes) {
        this.notes = notes;
    }

    interface onNoteClickListener{
        void onNoteCLick(int position);
    }

    public void setOnNoteClickListener(Notes_Adapter.onNoteClickListener onNoteClickListener) {
        this.onNoteClickListener = onNoteClickListener;
    }


    @NonNull
    @Override
    public NotesViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_note, parent, false);
        return new NotesViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull NotesViewHolder holder, int position) {
        Model_Note note = notes.get(position);
         holder.titleTv.setText(note.getTitle());
         holder.desciptionTv.setText(note.getDescription());

         String priority = note.getPriority();
        switch (priority) {
            case "High":
                holder.background.setBackgroundColor(holder.background.getResources().getColor(R.color.red));
                break;
            case "Medium":
                holder.background.setBackgroundColor(holder.background.getResources().getColor(R.color.Green));
                break;
            case "Low":
                holder.background.setBackgroundColor(holder.background.getResources().getColor(R.color.gray));
                break;
        }

    }

    @Override
    public int getItemCount() {
        return notes.size();
    }

    class NotesViewHolder extends RecyclerView.ViewHolder{

        private TextView titleTv, desciptionTv;
        private ImageView background;

        public NotesViewHolder(@NonNull View itemView) {
            super(itemView);
            titleTv = itemView.findViewById(R.id.titleNoteRow);
            desciptionTv = itemView.findViewById(R.id.descriptionNoteRow);
            background = itemView.findViewById(R.id.rowNoteLinear);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(onNoteClickListener != null){
                        onNoteClickListener.onNoteCLick(getAdapterPosition());
                    }
                }
            });
        }
    }
}
