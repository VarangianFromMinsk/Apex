package com.example.myapplication.main.Screens.Posts.Add_Change_Post_MVP;

import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.widget.ImageView;
import androidx.annotation.NonNull;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.util.HashMap;

public class Add_Change_Post_Presenter {


    private final Add_Change_Post_view view;

    public Add_Change_Post_Presenter(Add_Change_Post_view view) {
        this.view = view;
    }


    //todo: load post info
    public void loadData(String editPostId){
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Posts");
        Query fQuery = reference.orderByChild("pId").equalTo(editPostId);
        fQuery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for(DataSnapshot ds : snapshot.getChildren()){
                    String editTitle = "" + ds.child("pTitle").getValue();
                    String editDescription = "" + ds.child("pDescr").getValue();
                    String editImage = "" + ds.child("pImage").getValue();

                    view.loadPostDataForChange(editTitle, editDescription, editImage );
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    public void loadUserInfo(String myUid){
        DatabaseReference userDbRef = FirebaseDatabase.getInstance().getReference("users");
        Query query = userDbRef.orderByChild("firebaseId").equalTo(myUid);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for(DataSnapshot ds : snapshot.getChildren()){
                    String name = "" + ds.child("name").getValue();
                    String email = "" + ds.child("email").getValue();
                    String avatar = "" + ds.child("avatarMockUpResourse").getValue();

                    view.loadCurrentUserInfo(name, email, avatar);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }

    public void uploadData(ImageView imageIv, String filePathAndName, String myUid, String name, String email, String avatar, String timeStamp, String title, String description){

        if(imageIv.getDrawable() != null){
            //todo: get image from ImageView
            Bitmap bitmap = ((BitmapDrawable)imageIv.getDrawable()).getBitmap();
            ByteArrayOutputStream baos = new ByteArrayOutputStream();

            //todo: image compress
            bitmap.compress(Bitmap.CompressFormat.PNG, 50, baos );
            byte[] data = baos.toByteArray();

            //todo: post with image
            StorageReference ref = FirebaseStorage.getInstance().getReference().child(filePathAndName);
            ref.putBytes(data).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    //todo: image uploaded to firebase storage, now get its uri
                    Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();
                    while(!uriTask.isSuccessful());

                    String downloadUri = uriTask.getResult().toString();

                    if(uriTask.isSuccessful()){
                        //todo: uri is received upload to firebase database
                        HashMap<Object, String> hashMap = new HashMap<>();
                        hashMap.put("uid", myUid);
                        hashMap.put("uName", name );
                        hashMap.put("uEmail", email );
                        hashMap.put("uAvatar", avatar );
                        hashMap.put("pId", timeStamp);
                        hashMap.put("pTitle", title );
                        hashMap.put("pDescr", description);
                        hashMap.put("pImage", downloadUri);
                        hashMap.put("pTime", timeStamp );
                        hashMap.put("pLikes", "0");
                        hashMap.put("pComments", "0");


                        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Posts");
                        ref.child(timeStamp).setValue(hashMap).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                view.uploadDataComplete(true, timeStamp);
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                view.uploadDataComplete(false, "");
                            }
                        });
                    }
                }
            });

        }
        else {
            //todo: post without image
            HashMap<Object, String> hashMap = new HashMap<>();
            hashMap.put("uid", myUid);
            hashMap.put("uName", name );
            hashMap.put("uEmail", email );
            hashMap.put("uAvatar", avatar );
            hashMap.put("pId", timeStamp);
            hashMap.put("pTitle", title );
            hashMap.put("pDescr", description);
            hashMap.put("pImage", "noImage");
            hashMap.put("pTime", timeStamp );
            hashMap.put("pLikes", "0");
            hashMap.put("pComments", "0");


            DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Posts");
            ref.child(timeStamp).setValue(hashMap).addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    view.uploadDataComplete(true, timeStamp);
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    view.uploadDataComplete(false, "");
                }
            });
        }
    }

    public void updateWasWithImage(String editImage, ImageView imageIv, String myUid, String name, String email, String avatar, String title, String description, String editPostId){

        StorageReference mPictureRef = FirebaseStorage.getInstance().getReferenceFromUrl(editImage);
        mPictureRef.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                //todo: image deleted, upload new image
                String timeStamp = String.valueOf(System.currentTimeMillis());
                String fileNameAndPath = "Posts/" + "post_" + timeStamp;

                //todo: get image from ImageView
                Bitmap bitmap = ((BitmapDrawable)imageIv.getDrawable()).getBitmap();
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                //todo: image compress
                bitmap.compress(Bitmap.CompressFormat.JPEG, 50, baos );
                byte[] data = baos.toByteArray();


                StorageReference ref = FirebaseStorage.getInstance().getReference().child(fileNameAndPath);
                ref.putBytes(data).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        //todo: image uploaded, get uri
                        Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();
                        while(!uriTask.isSuccessful());

                        String downloadUri = uriTask.getResult().toString();
                        if(uriTask.isSuccessful()) {
                            //uri is received< upload into database
                            HashMap<String, Object> hashMap = new HashMap<>();
                            hashMap.put("uid", myUid);
                            hashMap.put("uName", name );
                            hashMap.put("uEmail", email );
                            hashMap.put("uAvatar", avatar );
                            hashMap.put("pTitle", title );
                            hashMap.put("pDescr", description);
                            hashMap.put("pImage", downloadUri);

                            DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Posts");
                            ref.child(editPostId).updateChildren(hashMap).addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    view.updateWasWithImageComplete(true);
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                  view.updateWasWithImageComplete(false);
                                }
                            });
                        }

                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        view.updateWasWithImageComplete(false);
                    }
                });

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                view.updateWasWithImageComplete(false);
            }
        });
    }

    public void updateWithNowImage(ImageView imageIv, String fileNameAndPath, String myUid, String name, String email, String avatar, String title, String description, String editPostId){

        //todo: get image from ImageView
        Bitmap bitmap = ((BitmapDrawable)imageIv.getDrawable()).getBitmap();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        //todo: image compress
        bitmap.compress(Bitmap.CompressFormat.JPEG, 50, baos );
        byte[] data = baos.toByteArray();

        StorageReference ref = FirebaseStorage.getInstance().getReference().child(fileNameAndPath);
        ref.putBytes(data).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                //todo: image uploaded, get uri
                Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();
                while(!uriTask.isSuccessful());
                
                String downloadUri = uriTask.getResult().toString();
                if(uriTask.isSuccessful()) {
                    //uri is received< upload into database
                    HashMap<String, Object> hashMap = new HashMap<>();
                    hashMap.put("uid", myUid);
                    hashMap.put("uName", name );
                    hashMap.put("uEmail", email );
                    hashMap.put("uAvatar", avatar );
                    hashMap.put("pTitle", title );
                    hashMap.put("pDescr", description);
                    hashMap.put("pImage", downloadUri);

                    DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Posts");
                    ref.child(editPostId).updateChildren(hashMap).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            view.updateWithNowImageComplete(true);
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            view.updateWithNowImageComplete(false);
                        }
                    });
                }

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                view.updateWithNowImageComplete(false);
            }
        });
    }

    public void updateWithoutImage(String myUid, String name, String email, String avatar, String title, String description, String editPostId){
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("uid", myUid);
        hashMap.put("uName", name );
        hashMap.put("uEmail", email );
        hashMap.put("uAvatar", avatar );
        hashMap.put("pTitle", title );
        hashMap.put("pDescr", description);
        hashMap.put("pImage", "noImage");

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Posts");
        ref.child(editPostId).updateChildren(hashMap).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                view.updateWithoutImageComplete(true);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                view.updateWithoutImageComplete(false);
            }
        });
    }
}
