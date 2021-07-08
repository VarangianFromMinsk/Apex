package com.example.myapplication.Notes_ROOM;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.example.myapplication.R;

import java.util.concurrent.ExecutionException;

public class Add_Note_Activity extends AppCompatActivity {

    private EditText title, description;
    private Button recordBrn;
    private RadioGroup groupPriorityBtn;

    private Notes_ViewModel viewModel;
    private int gettingId;
    Model_Note note = null;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add__note_);

        getSupportActionBar().hide();

        //set only portrait view
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        //TODO: create ViewModel
        viewModel = new ViewModelProvider(this).get(Notes_ViewModel.class);

        initValues();

        Intent fromMain = getIntent();
        Bundle extras = fromMain.getExtras();
        if(extras != null ){
            gettingId = fromMain.getIntExtra("selectedModelId",1);
            try {
                note = viewModel.getModelById(gettingId);
            } catch (ExecutionException | InterruptedException e) {
                e.printStackTrace();
            }
            title.setText(note.getTitle());
            description.setText(note.getDescription());
        }

        uploadBtn();
    }


    private void initValues(){
        title = findViewById(R.id.titleNoteEt);
        description = findViewById(R.id.descrNoteEt);
        recordBrn = findViewById(R.id.UploadBtnNote);
        groupPriorityBtn = findViewById(R.id.groupBtns);
    }

    private void uploadBtn() {
        recordBrn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //TODO: get info
                String titleText = title.getText().toString().trim();
                String descriptionText = description.getText().toString().trim();
                int radioBtnId = groupPriorityBtn.getCheckedRadioButtonId();
                RadioButton radioButton = findViewById(radioBtnId);
                String priority = radioButton.getText().toString();

                Intent fromMain = getIntent();
                Bundle extras = fromMain.getExtras();
                if(extras != null){
                    if(isFilled(titleText, descriptionText)){
                        note.setTitle(titleText);
                        note.setDescription(descriptionText);
                        note.setPriority(priority);
                        viewModel.updateNote(note);
                        Intent intent = new Intent(Add_Note_Activity.this, Notes_Activity.class);
                        startActivity(intent);
                    }
                    else{
                        Toast.makeText(Add_Note_Activity.this, "Please,fill the fields", Toast.LENGTH_SHORT).show();
                    }
                }else{
                    if(isFilled(titleText, descriptionText)){
                        Model_Note noteNew = new Model_Note(titleText, descriptionText, priority);
                        viewModel.insertNote(noteNew);
                        Intent intent = new Intent(Add_Note_Activity.this, Notes_Activity.class);
                        startActivity(intent);
                    }
                    else{
                        Toast.makeText(Add_Note_Activity.this, "Please,fill the fields", Toast.LENGTH_SHORT).show();
                    }
                }

            }
        });
    }

    private boolean isFilled(String titleText, String descriptionText){
        return !titleText.isEmpty() && !descriptionText.isEmpty();
    }
}