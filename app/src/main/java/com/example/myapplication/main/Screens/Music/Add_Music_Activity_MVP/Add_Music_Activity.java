package com.example.myapplication.main.Screens.Music.Add_Music_Activity_MVP;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.myapplication.R;
import com.example.myapplication.Services.App_Constants;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;


public class Add_Music_Activity extends AppCompatActivity implements Add_Music_view{

    private EditText editTextMainTitle, editTextLastTitle;
    private TextView textViewTitleFromMachine;
    private ImageView albumIv;
    private Button chooseAlbum;
    private ProgressBar progressBarAudio;
    private ProgressBar progressBarImage;

    private Uri audioUri;
    private Uri imageUri;

    private DatabaseReference musicReference;
    //progress bar
    private ProgressDialog pd;

    private Add_Music_Presenter presenter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.add_music_activity);

        setTitle("Upload Songs");

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        presenter = new Add_Music_Presenter(this);

        initialization();

        chooseAlbumFile();

    }

    private void initialization() {
        editTextMainTitle = findViewById(R.id.songMainTitle);
        editTextLastTitle = findViewById(R.id.songLastTitle);

        textViewTitleFromMachine = findViewById(R.id.textViewSongFileSelected);
        albumIv = findViewById(R.id.albumImage);
        chooseAlbum = findViewById(R.id.uploadAlbumBtn);
        progressBarImage = findViewById(R.id.progressBarUploadAlbum);
        progressBarAudio = findViewById(R.id.progressBarUploadSong);

        musicReference = FirebaseDatabase.getInstance().getReference("Music");
    }


    private void chooseAlbumFile(){
        chooseAlbum.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("image/*");
                intent.putExtra(Intent.EXTRA_LOCAL_ONLY,true);
                startActivityForResult(Intent.createChooser(intent,"Choose an image"), App_Constants.ALBUM_REQUEST_CODE);
            }
        });
    }

    public void chooseAudioFile(View view){
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("audio/*");
        startActivityForResult(intent, App_Constants.SONG_REQUEST_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode == RESULT_OK) {
            assert data != null;
            if (data.getData() != null) {
                if (requestCode == App_Constants.SONG_REQUEST_CODE) {
                    audioUri = data.getData();
                    String fileName = getFileName(audioUri);
                    textViewTitleFromMachine.setText(fileName);
                } else if (requestCode == App_Constants.ALBUM_REQUEST_CODE) {
                    imageUri = data.getData();
                    albumIv.setImageURI(imageUri);
                }
            }
        }

    }

    public void uploadSongToFirebase(View view) {
        uploadFile();
    }

    private void uploadFile() {
        String uploadId = musicReference.push().getKey();
        String mainTitle = editTextMainTitle.getText().toString().trim();
        String latTitle = editTextLastTitle.getText().toString().trim();

        presenter.uploadFilesOnServer(uploadId, mainTitle, latTitle, audioUri, imageUri, albumIv, this);
    }

    @Override
    public void loadImageStart() {
        Toast.makeText(this, "Uploading, please wait", Toast.LENGTH_LONG).show();
        progressBarAudio.setVisibility(View.VISIBLE);
        progressBarImage.setVisibility(View.VISIBLE);
    }

    @Override
    public void setImageProgress(int progress) {
        progressBarImage.setProgress(progress);
    }

    @Override
    public void imageComplete() {
        Toast.makeText(Add_Music_Activity.this, "Album upload", Toast.LENGTH_SHORT).show();
        progressBarImage.setProgress(0);
    }

    @Override
    public void setMusicProgress(int progress) {
        progressBarAudio.setProgress((int) progress);
    }

    @Override
    public void musicComplete() {
        Toast.makeText(Add_Music_Activity.this, "Song upload", Toast.LENGTH_SHORT).show();
        progressBarAudio.setProgress(0);
        editTextMainTitle.setText("");
        editTextLastTitle.setText("");
        textViewTitleFromMachine.setText("");
    }

    @Override
    public void noFileSelected() {
        Toast.makeText(this, "No file selected to upload ", Toast.LENGTH_LONG).show();
    }


    private String getFileName(Uri uri) {
        String result = null;
        if(uri.getScheme().equals("content")){

            Cursor cursor = getContentResolver().query(uri,null,null,null,null);

            try {
                if(cursor != null && cursor.moveToFirst()){
                    result = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
                }
            }finally {
                assert cursor != null;
                cursor.close();
            }

        }

        if(result == null){
            result = uri.getPath();
            int cut = result.lastIndexOf('/');
            if(cut != -1){
                result = result.substring(cut + 1);
            }
        }

        return  result;

    }

}