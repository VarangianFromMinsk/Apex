package com.example.myapplication.Notes_ROOM_MVVM;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.view.View;

import com.example.myapplication.Notes_ROOM_MVVM.DataBase.Model_Note;
import com.example.myapplication.R;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

public class Notes_Activity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private List<Model_Note> notes = new ArrayList<>();
    private Notes_Adapter adapter;

    private Notes_ViewModel viewModel;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notes_);

        getSupportActionBar().hide();

        //set only portrait view
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        //TODO:Create ViewModel
        viewModel = new ViewModelProvider(this).get(Notes_ViewModel.class);

        initfloatBtn();

        initRecyclerView();

        removeFromRecyclerView();

    }

    private void initRecyclerView(){
        recyclerView = findViewById(R.id.recycler_notes);

        adapter = new Notes_Adapter(notes);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        getData();

        recyclerView.setAdapter(adapter);

        adapter.setOnNoteClickListener(new Notes_Adapter.onNoteClickListener() {
            @Override
            public void onNoteCLick(int position) {
                Intent update = new Intent(Notes_Activity.this, Add_Note_Activity.class);
                update.putExtra("selectedModelId", notes.get(position).getId());
                startActivity(update);
            }
        });
    }

    private void getData(){
        //TODO:value is observable now
        LiveData <List<Model_Note>> notesFromDb = viewModel.getNotes();
        //TODO: check to change
        notesFromDb.observe(this, new Observer<List<Model_Note>>() {
            @Override
            public void onChanged(List<Model_Note> model_notes) {
                notes.clear();
                notes.addAll(model_notes);
                adapter.notifyDataSetChanged();
            }
        });

    }

    private void removeFromRecyclerView() {
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                remove(viewHolder.getAdapterPosition());
            }
        });
        itemTouchHelper.attachToRecyclerView(recyclerView);
    }

    private void remove(int position){
        Model_Note note = notes.get(position);
        viewModel.deleteNote(note);
    }

    private void initfloatBtn() {
        FloatingActionButton floatingActionButton = findViewById(R.id.floatingAddNotes);
        floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent addNote = new Intent(Notes_Activity.this, Add_Note_Activity.class);
                startActivity(addNote);
            }
        });
    }
}