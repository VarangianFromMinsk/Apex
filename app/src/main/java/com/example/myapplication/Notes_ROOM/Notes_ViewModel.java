package com.example.myapplication.Notes_ROOM;

import android.app.Application;
import android.os.AsyncTask;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import java.util.List;
import java.util.concurrent.ExecutionException;

public class Notes_ViewModel extends AndroidViewModel {

    private static Notes_Database database;
    private LiveData<List<Model_Note>> notes;

    public Notes_ViewModel(@NonNull Application application) {
        super(application);
        database = Notes_Database.getInstance(getApplication());
        notes = database.notes_dao().getAllNotes();
    }

    public LiveData<List<Model_Note>> getNotes() {
        return notes;
    }

    public void insertNote(Model_Note note) {
        new InsertTask().execute(note);
    }

    public void deleteNote(Model_Note note) {
        new DeleteTask().execute(note);
    }

    public void deleteAllNote() {
        new DeleteAllTask().execute();
    }

    public void updateNote(Model_Note note) {
        new updateTask().execute(note);
    }

    public Model_Note getModelById(int id) throws ExecutionException, InterruptedException {
        final AsyncTask<Integer, Void, Model_Note> execute = new getByIdTask().execute(id);
        return execute.get();
    }

    private static class getByIdTask extends AsyncTask<Integer, Void, Model_Note> {

        @Override
        protected Model_Note doInBackground(Integer... integers) {
            return database.notes_dao().getById(integers[0]);
        }
    }


    private static class InsertTask extends AsyncTask<Model_Note, Void, Void>{
        @Override
        protected Void doInBackground(Model_Note... model_notes) {
            if(model_notes != null && model_notes.length > 0){
                database.notes_dao().insertNote(model_notes [0]);
            }
            return null;
        }
    }

    private static class DeleteTask extends AsyncTask<Model_Note, Void, Void>{
        @Override
        protected Void doInBackground(Model_Note... model_notes) {
            if(model_notes != null && model_notes.length > 0){
                database.notes_dao().deleteNote(model_notes [0]);
            }
            return null;
        }
    }

    private static class updateTask extends AsyncTask<Model_Note, Void, Void>{
        @Override
        protected Void doInBackground(Model_Note... model_notes) {
            if(model_notes != null && model_notes.length > 0){
                database.notes_dao().updateNote(model_notes [0]);
            }
            return null;
        }
    }

    private static class DeleteAllTask extends AsyncTask<Void, Void, Void>{
        @Override
        protected Void doInBackground(Void... model_notes) {
            database.notes_dao().deleteAllNotes();
            return null;
        }
    }
}
