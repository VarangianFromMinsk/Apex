package com.example.myapplication.Notes_ROOM_MVVM;

import android.content.Context;
import android.os.AsyncTask;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.example.myapplication.Notes_ROOM_MVVM.DataBase.Model_Note;
import com.example.myapplication.Notes_ROOM_MVVM.DataBase.Notes_Database;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.List;

import static org.junit.Assert.*;

@RunWith(AndroidJUnit4.class)
public class Notes_DaoTest {

    private static Notes_Database db;
    private Context context;

    @Before
    public void setUp() throws Exception {
        context = androidx.test.platform.app.InstrumentationRegistry.getInstrumentation().getContext();
        db = Notes_Database.getInstance(context);
    }

    @After
    public void closeDB(){
       // db.notes_dao().deleteAllNotes();
        db.close();
    }


    @Test
    public void checkInsertInDB() {
        List<Model_Note> notes;
        notes = db.notes_dao().getAllNotes().getValue();
        Model_Note note = new Model_Note("check","check", "high");
        new InsertTask().execute(note);
        assertEquals(1, notes.size());
        new DeleteAllTask().execute();
    }

    private static class InsertTask extends AsyncTask<Model_Note, Void, Void> {
        @Override
        protected Void doInBackground(Model_Note... model_notes) {
            if(model_notes != null && model_notes.length > 0){
                db.notes_dao().insertNote(model_notes [0]);
            }
            return null;
        }
    }

    private static class DeleteAllTask extends AsyncTask<Void, Void, Void>{
        @Override
        protected Void doInBackground(Void... model_notes) {
            db.notes_dao().deleteAllNotes();
            return null;
        }
    }


}