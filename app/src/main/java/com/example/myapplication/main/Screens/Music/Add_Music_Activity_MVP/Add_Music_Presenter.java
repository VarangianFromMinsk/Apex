package com.example.myapplication.main.Screens.Music.Add_Music_Activity_MVP;

import android.content.ContentResolver;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.webkit.MimeTypeMap;
import android.widget.ImageView;

import androidx.annotation.NonNull;

import com.example.myapplication.main.Models.Model_Song;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;

public class Add_Music_Presenter {

    private final Add_Music_view view;
    private String downloadImageUri = null, downloadSongUri = null;
    private Uri audioUri,imageUri;
    private ImageView albumIv;

    public Add_Music_Presenter(Add_Music_view view) {
        this.view = view;
    }

    public void uploadFilesOnServer(String uploadId, String mainTitle, String latTitle, Uri audioUriAct, Uri imageUriAct, ImageView albumIvAct, Add_Music_Activity activity){

        DatabaseReference musicReference = FirebaseDatabase.getInstance().getReference("Music");
        StorageReference albumForSongStorageReference = FirebaseStorage.getInstance().getReference("album_for_music");
        StorageReference songStorageReference = FirebaseStorage.getInstance().getReference("music");

        audioUri = audioUriAct;
        imageUri = imageUriAct;
        albumIv = albumIvAct;


        if(!mainTitle.equals("") && !latTitle.equals("") && audioUri != null && imageUri != null ){
            Model_Song modelSong = new Model_Song();
            modelSong.setAlbumUrl(null);
            modelSong.setSongUrl(null);
            modelSong.setSongMainTitle(mainTitle);
            modelSong.setSongLastTitle(latTitle);
            modelSong.setSongDuration(null);
            modelSong.setUploadId(uploadId);

            assert uploadId != null;
            musicReference.child(uploadId).setValue(modelSong);
        }

        if(audioUri != null && imageUri != null){

            String durationTxt;

            view.loadImageStart();

            //Image
            StorageReference imageReference = albumForSongStorageReference.child("" + System.currentTimeMillis());
            if(albumIv.getDrawable() != null) {
                //get image from ImageView
                Bitmap bitmap = ((BitmapDrawable) albumIv.getDrawable()).getBitmap();
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                //image compress
                bitmap.compress(Bitmap.CompressFormat.JPEG, 35, baos);
                byte[] data = baos.toByteArray();

                imageReference.putBytes(data).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        //image uploaded to firebase storage, now get its uri
                        Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();
                        while (!uriTask.isSuccessful());

                        downloadImageUri = uriTask.getResult().toString();

                        Query query = musicReference.orderByChild("uploadId").equalTo(uploadId);
                        query.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                for(DataSnapshot ds : snapshot.getChildren()){
                                    HashMap<String, Object> hashMap = new HashMap<>();
                                    hashMap.put("albumUrl", downloadImageUri);
                                    ds.getRef().updateChildren(hashMap);
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });

                    }
                }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onProgress(@NonNull UploadTask.TaskSnapshot snapshot) {

                        double progress = (100.0 * snapshot.getBytesTransferred() / snapshot.getTotalByteCount());

                        view.setImageProgress((int) progress);

                    }
                }).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                        view.imageComplete();
                        albumIv = null;
                        imageUri = null;

                    }
                });
            }


            //Song
            StorageReference songReference = songStorageReference.child(System.currentTimeMillis() + "." + getFileExtension(audioUri,activity));
            int durationInMillis = findSongDuration(audioUri, activity);
            if(durationInMillis == 0){
                durationTxt = "NA";
            }
            durationTxt = getDurationFromMilli (durationInMillis);
            String finalDurationTxt = durationTxt;

            songReference.putFile(audioUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    //image uploaded to firebase storage, now get its uri
                    Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();
                    while (!uriTask.isSuccessful());

                    downloadSongUri = uriTask.getResult().toString();

                    Query query = musicReference.orderByChild("uploadId").equalTo(uploadId);
                    query.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            for(DataSnapshot ds : snapshot.getChildren()){
                                HashMap<String, Object> hashMap = new HashMap<>();
                                hashMap.put("songUrl", downloadSongUri);
                                ds.getRef().updateChildren(hashMap);

                                HashMap<String, Object> hashMapDur = new HashMap<>();
                                hashMapDur.put("songDuration", finalDurationTxt);
                                ds.getRef().updateChildren(hashMapDur);
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });
                }
            }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onProgress(@NonNull UploadTask.TaskSnapshot snapshot) {

                    double progress = (100.0 * snapshot.getBytesTransferred() / snapshot.getTotalByteCount());
                    view.setMusicProgress((int) progress);

                }
            }).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                    view.musicComplete();
                    audioUri = null;
                }
            });

        }
        else{
            view.noFileSelected();
        }
    }

    private String getFileExtension(Uri audioUri, Add_Music_Activity activity) {
        ContentResolver contentResolver = activity.getContentResolver();
        MimeTypeMap mime = MimeTypeMap.getSingleton();
        return mime.getExtensionFromMimeType(contentResolver.getType(audioUri));
    }

    private int findSongDuration(Uri audioUri, Add_Music_Activity activity) {
        int timeInMillisec = 0;

        try {
            MediaMetadataRetriever retriever = new MediaMetadataRetriever();
            retriever.setDataSource(activity,audioUri);
            String time = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
            timeInMillisec = Integer.parseInt(time);

            retriever.release();

            return  timeInMillisec;
        }catch (Exception E){
            return  0;
        }
    }

    private String getDurationFromMilli(int durationInMillis) {
        Date date = new Date(durationInMillis);
        SimpleDateFormat simple = new SimpleDateFormat("mm:ss", Locale.getDefault());

        return simple.format(date);
    }

}
