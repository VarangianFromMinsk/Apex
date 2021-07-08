package com.example.myapplication.main.Screens.Chat_Activity_MVP;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ActivityOptions;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.util.Pair;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import com.bumptech.glide.Glide;
import com.example.myapplication.R;
import com.example.myapplication.Services.App_Constants;
import com.example.myapplication.Services.Check_Permission_Service;
import com.example.myapplication.Services.Online_Offline_Service;
import com.example.myapplication.main.Screens.Settings.Settings_Activity;
import com.example.myapplication.main.Models.Model_Message;
import com.example.myapplication.main.Screens.User_Profile_MVVM.User_Profile_Activity;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import de.hdodenhof.circleimageview.CircleImageView;

public class Chat_Main_Activity extends AppCompatActivity implements Chat_view {

    //recycler
    private ListView messageListView;
    private Message_Adapter adapter;

    //todo: block to send
    private ImageButton sendImageButton;
    @SuppressLint("StaticFieldLeak")
    public static ImageButton sendMessageButton ;
    @SuppressLint("StaticFieldLeak")
    public static EditText messageEditText;
    private TextView recipientUserNameView, recipientUserStatus, recipientUserLastTimeConnection ;
    private CircleImageView avatar;
    private ProgressDialog progressDialog;

    //staff of current user
    private String myName, myAvatarUrl, myUid;
    private String recipientUserId;
    private String recipientUserName;
    private String recipientAvatar;
    private FirebaseAuth auth;
    private StorageReference chatImagesStorageReference;

    private Uri image_rui = null;
    private byte[] dataCompr;

    //todo: voice
    //MediaRecorder mediaRecorder;
    //String audioPath;

    private Locale locale;
    private Button buttonRefreshChat;

    //todo: static check is Activity started
    public static boolean active = false;

    //todo: for "start Writing"
    private CountDownTimer timer;
    private TextView textingToYou;

    //todo: for change message
    @SuppressLint("StaticFieldLeak")
    public static EditText changeEditText;
    @SuppressLint("StaticFieldLeak")
    public static ImageButton changeBtn;

    private Chat_Presenter presenter;


    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.chat_main_activity);

        Objects.requireNonNull(getSupportActionBar()).hide();

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        presenter = new Chat_Presenter(this);

        initialization();

        createMainLocal();

        getMainIntent();

        messageListener();

        makeCustomToolBar();

        eventOfMessageEditText();

        sendTextMessageBtn();

        sendImageBtn();

        showOnlineOffline();

        setAvatar();

        createListView();

        userListener();

        updateUserStatus("online");

        //todo: in process
        //voiceMessage();

        showIsHisWritingToYou();

        getRepostIntent();

        getShareImage();

    }

    private void initialization(){

        //todo: create for refresh btn
        buttonRefreshChat = findViewById(R.id.buttonRefreshChat);
        buttonRefreshChat.animate().translationY(+200).setDuration(10);

        auth = FirebaseAuth.getInstance();
        myUid = auth.getUid();

        progressDialog = new ProgressDialog(this);

        DatabaseReference usersDatabaseReference = FirebaseDatabase.getInstance().getReference().child("users");

        FirebaseStorage storage = FirebaseStorage.getInstance();
        chatImagesStorageReference = storage.getReference().child("chat_images");
        StorageReference chatRecordsStorageReference = storage.getReference().child("chat_records");

        recipientUserNameView = findViewById(R.id.textNameToolBar);
        recipientUserStatus = findViewById(R.id.textStatusToolBar);
        recipientUserLastTimeConnection = findViewById(R.id.textLastTimeToolBar);
        sendImageButton = findViewById(R.id.sendPhotoButton);
        sendMessageButton = findViewById(R.id.sendMessageButton);

        messageEditText = findViewById(R.id.messageEditText);

        myName = "DefaultUser";
        messageListView = findViewById(R.id.massageList);
        avatar = findViewById(R.id.avatarInChatImageView);

        //todo: for change message
        changeEditText = findViewById(R.id.changeMessage);
        changeBtn = findViewById(R.id.changeButton);

        textingToYou = findViewById(R.id.textingToYou);
    }

    private void createMainLocal(){
        locale = new Locale("ru");
        Locale.setDefault(locale);
        Configuration config = getBaseContext().getResources().getConfiguration();
        config.locale = locale;
        getBaseContext().getResources().updateConfiguration(config,
                getBaseContext().getResources().getDisplayMetrics());
    }

    private void getMainIntent(){
        Intent intent = getIntent();
        if(intent != null){
            recipientUserId = intent.getStringExtra("recipientUserId");
            recipientUserName = intent.getStringExtra("recipientUserName");
            recipientAvatar = intent.getStringExtra("recipientAvatar");
        }
    }

    private void makeCustomToolBar(){
        //todo: toolbar
        ImageView avatarInToolBar = findViewById(R.id.avatarInToolBar);

        if(!recipientAvatar.equals("")){
            try{
                Glide.with(avatarInToolBar).load(recipientAvatar).into(avatarInToolBar);
            }catch (Exception ignored){
            }
        }

        recipientUserNameView.setText(recipientUserName);

        avatarInToolBar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent GoToProfile = new Intent(Chat_Main_Activity.this, User_Profile_Activity.class);
                GoToProfile.putExtra("hisId",recipientUserId);

                Pair<View, String> pairAvatar = Pair.create(findViewById(R.id.avatarInToolBar), "avatar2");
                Pair<View, String> pairName = Pair.create(findViewById(R.id.textNameToolBar), "name");

                ActivityOptions activityOptions = null;
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
                    activityOptions = ActivityOptions.makeSceneTransitionAnimation(Chat_Main_Activity.this, pairAvatar, pairName);
                    startActivity(GoToProfile, activityOptions.toBundle());
                }
                else{
                    startActivity(GoToProfile);
                }
            }
        });


    }

    //todo: part of messageEt event and start writing
    private void eventOfMessageEditText () {
        messageEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                if (s.length() >= 3){
                    countDownTimerStartWritting();
                }
                else{
                    removeChatTimer();
                }
            }

            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

                if( s.toString().trim().length() > 0){
                    sendMessageButton.setEnabled(true);
                    sendMessageButton.setImageResource(R.drawable.send);
                }else {
                    sendMessageButton.setEnabled(false);
                    sendMessageButton.setImageResource(R.drawable.send_focus);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        //todo: set max length of message
        messageEditText.setFilters(new InputFilter[]{new InputFilter.LengthFilter(250)});
    }

    private  void showIsHisWritingToYou(){
        presenter.showIsHisWritingToYou(recipientUserId);
    }

    @Override
    public void showIfWriting(boolean check) {
        if(check){
            textingToYou.animate().alpha(1).translationY(0).setDuration(600);
            messageListView.animate().translationY(-60).setDuration(400);
        }
        else{
            textingToYou.animate().alpha(0).translationY(+40).setDuration(600);
            messageListView.animate().translationY(0).setDuration(400);
        }
    }

    private void removeChatTimer() {
        if (timer != null){
            timer.onFinish();
            timer.cancel();
            timer = null;
        }
    }

    private void countDownTimerStartWritting(){
        timer =  new CountDownTimer(3000,500) {
            public void onTick(long millisUntilFinished) {
                if( messageEditText.getText().toString().length() > 3) {
                    createStartWriting();
                }
                else{
                    deleteStartWriting();
                }
            }

            public void onFinish() {
                deleteStartWriting();
            }
        }.start();

    }

    private  void createStartWriting(){
        presenter.createStartWriting(myUid, recipientUserId);
    }

    private  void deleteStartWriting(){
        presenter.deleteStartWriting(myUid);
    }


    //todo: common pushMessage
    private void pushCommonMessage(Model_Message message, String typeOfMessage, Uri downloadUri, String shareImage, String pDescription, String uName, String pImage, String pId) {

        String text = messageEditText.getText().toString().trim();

        presenter.pushCommonMessage(message, typeOfMessage, downloadUri, shareImage, pDescription, uName, pImage,
                pId, this, locale, myName, recipientUserId, auth, myUid, myAvatarUrl, text);
    }

    //todo: create list and get info about messages and users
    private void createListView() {
        List<Model_Message> modelMessages = new ArrayList<>();
        adapter = new Message_Adapter(this,R.layout.message_item, modelMessages);

        messageListView.setAdapter(adapter);
    }

    private void userListener(){
        presenter.loadInfoAboutUser(myUid);
    }

    @Override
    public void infoAboutUser(String myNamePr, String myAvatarUrlPr) {
        myName = myNamePr;
        myAvatarUrl = myAvatarUrlPr;
    }


    //todo: load messages
    @RequiresApi(api = Build.VERSION_CODES.M)
    private void messageListener(){
        presenter.loadMessages(auth, recipientUserId);
    }

    @Override
    public void showMessage(Model_Message message) {
        adapter.add(message);
        adapter.notifyDataSetChanged();
    }


    @Override
    public void initRefreshChat() {
        refreshBtn();
    }

    //todo: refresh Chat
    public void refreshBtn(){
        buttonRefreshChat.setVisibility(View.VISIBLE);
        buttonRefreshChat.animate().alpha(1).translationY(0).setDuration(300);
        messageListView.animate().translationY(-120).setDuration(200);

        buttonRefreshChat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = getIntent();
                finish();
                overridePendingTransition(0, 0);
                startActivity(intent);
            }
        });
    }

    //todo: main sends event
    public void sendTextMessageBtn(){
        sendMessageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!messageEditText.getText().toString().trim().isEmpty()){
                    Model_Message message = new Model_Message();
                    pushCommonMessage(message, "textMessage", null, null, null, null, null, null);
                    messageEditText.setText("");
                }
                else{
                    Toast.makeText(Chat_Main_Activity.this, "Empty", Toast.LENGTH_SHORT).show();
                }

            }
        });

    }

    private void sendImageBtn(){
        sendImageButton.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.M)
            @Override
            public void onClick(View v) {
                if(checkCurrentPermission()){
                    showImagePickDialog();
                }
            }
        });
    }

    //todo: cut staff for voice
    /*

    private void voiceMessage() {
        if(checkPermForVoice()){
            recordButton.setRecordView(recordView);
            recordButton.setListenForRecord(false);
            recordButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    recordButton.setListenForRecord(true);
                    recordView.setOnRecordListener(new OnRecordListener() {
                        @Override
                        public void onStart() {
                            //Start Recording..
                            Log.d("RecordView", "onStart");
                            setUpRecording();
                            try {
                                mediaRecorder.prepare();
                                mediaRecorder.start();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }

                            recordView.setVisibility(View.VISIBLE);
                            //hide
                            sendImageButton.setVisibility(View.GONE);
                            messageEditText.setVisibility(View.GONE);
                            sendMessageButton.setVisibility(View.GONE);

                        }

                        @Override
                        public void onCancel() {
                            //On Swipe To Cancel
                            Log.d("RecordView", "onCancel");
                            //clean audio
                            mediaRecorder.reset();
                            mediaRecorder.release();
                            File file = new File(audioPath);
                            if(file.exists())
                                file.delete();

                            recordView.setVisibility(View.GONE);
                            showBlockText();

                        }

                        @RequiresApi(api = Build.VERSION_CODES.N)
                        @Override
                        public void onFinish(long recordTime) {
                            //Stop Recording..
                            Log.d("RecordView", "onFinish");

                            mediaRecorder.stop();
                            mediaRecorder.release();

                            recordView.setVisibility(View.GONE);
                            showBlockText();

                            sendRecordingMessage(audioPath);
                        }

                        @Override
                        public void onLessThanSecond() {
                            //When the record time is less than One Second
                            Log.d("RecordView", "onLessThanSecond");

                            mediaRecorder.reset();
                            mediaRecorder.release();

                            File file = new File(audioPath);
                            if(file.exists())
                                file.delete();

                            mediaRecorder.reset();
                            mediaRecorder.release();

                            recordView.setVisibility(View.GONE);
                            showBlockText();

                        }
                    });
                }
            });

        }
    }

    private void showBlockText(){
        sendImageButton.setVisibility(View.VISIBLE);
        messageEditText.setVisibility(View.VISIBLE);
        sendMessageButton.setVisibility(View.VISIBLE);
    }

    private void sendRecordingMessage(String audioPath){
        StorageReference ref = FirebaseStorage.getInstance().getReference("recording").child("Recording " + System.currentTimeMillis());
        Uri audioFile = Uri.fromFile(new File(audioPath));

        progressDialog.setMessage("Sending ...");
        progressDialog.show();
        progressDialog.getCurrentFocus();

        ref.putFile(audioFile).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                progressDialog.dismiss();

                Task<Uri> audioUrl = taskSnapshot.getStorage().getDownloadUrl();
                audioUrl.addOnCompleteListener(new OnCompleteListener<Uri>() {
                    @Override
                    public void onComplete(@NonNull Task<Uri> task) {
                        if(task.isSuccessful()){
                            String recordUrl = task.getResult().toString();

                            Model_Message message = new Model_Message();
                            //path if its repost
                            message.setIsThatRepost("false");
                            message.setPostId(null);
                            //path if its record
                            message.setIsThatRecord("true");
                            message.setRecordUrl(recordUrl);

                            message.setImageUrl(null);
                            message.setName(myName);
                            message.setSender(auth.getCurrentUser().getUid());
                            message.setRecipient(recipientUserId);

                            Calendar calForTime = Calendar.getInstance();
                            SimpleDateFormat currentTime = new SimpleDateFormat("hh:mm a");
                            String saveCurrentTime = currentTime.format(calForTime.getTime());

                            Calendar calForDate = Calendar.getInstance();
                            SimpleDateFormat currentDate = new SimpleDateFormat("EEE, MMM d, ''yy");
                            String saveCurrentDate = currentDate.format(calForDate.getTime());

                            message.setTimeOfMessage(saveCurrentTime);
                            message.setDayOfMessage(saveCurrentDate);
                            //блок установки время
                            messagesDatabaseReference.push().setValue(message);
                        }
                        progressDialog.dismiss();
                    }
                });
            }
        });
        progressDialog.dismiss();

    }

    private void setUpRecording(){
        mediaRecorder = new MediaRecorder();
        mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        mediaRecorder.setAudioEncoder(MediaRecorder.OutputFormat.AMR_NB);

        File file = new File(Environment.getExternalStorageDirectory().getAbsolutePath(),"Recording " + System.currentTimeMillis());
        if(!file.exists())
            file.mkdirs();

        audioPath = file.getAbsolutePath() + System.currentTimeMillis() + ".3gp";
        mediaRecorder.setOutputFile(audioPath);

    }

    private boolean checkPermForVoice() {
        if(ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED){
            return true;
        }
        else{
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO}, App_Constants.RECORDING_REQUEST_CODE);
            return true;
        }
    }
     */

    //todo: additional intents
    private void getShareImage() {
        Intent intentFromShareImage = getIntent();
        if(intentFromShareImage != null){
            if(intentFromShareImage.getStringExtra("shareImage") != null){
                String shareImage =intentFromShareImage.getStringExtra("shareImage");

                Model_Message message = new Model_Message();

                pushCommonMessage(message, "shareImageMessage",null, shareImage, null, null, null, null);
            }
        }
    }

    private void getRepostIntent(){
        //all staff to repost
        Intent intentFromPost = getIntent();
        if(intentFromPost != null){
            if(intentFromPost.getStringExtra("isIntentExist") != null){
                String pDescription = intentFromPost.getStringExtra("Text");
                String uName = intentFromPost.getStringExtra("pUserName");
                String pImage = intentFromPost.getStringExtra("pImage");
                String pId = intentFromPost.getStringExtra("pId");


                Model_Message message = new Model_Message();

                pushCommonMessage(message, "repostMessage",null, null, pDescription, uName, pImage, pId);

            }
        }
    }


    //todo: customize
    private void setAvatar(){
        presenter.loadRecipientUserInfo(recipientUserId);
    }

    @Override
    public void loadHisInfoAndAvatar(String hisImagePr) {
        try {
            Glide.with(avatar).load(hisImagePr).placeholder(R.drawable.default_avatar).into(avatar);
        }catch (Exception ignored){}
    }

    public void settingsFromChat(View view) {
        Intent GoToSettingsFromProfile = new Intent(Chat_Main_Activity.this, Settings_Activity.class);
        startActivity(GoToSettingsFromProfile);
    }

    private  void showOnlineOffline(){
        presenter.isHeOnline(recipientUserId);
    }

    @Override
    public void isHeOnlineCheckComplete(boolean check, String lastTimeConnection) {
        if(check){
            recipientUserStatus.setText(R.string.online_text);
            changeColourStatus(lastTimeConnection);
        }
        else{
            recipientUserStatus.setText(R.string.offline_text);
            changeColourStatus(lastTimeConnection);
        }
    }

    private void changeColourStatus(String lastTimeConnection) {
        if(recipientUserStatus.getText().equals("online")){
            recipientUserStatus.setTextColor(Color.parseColor("#7AC537"));
            recipientUserLastTimeConnection.setVisibility(View.INVISIBLE);
        }else {
            recipientUserStatus.setTextColor(Color.parseColor("#D32121"));
            recipientUserLastTimeConnection.setVisibility(View.VISIBLE);
            recipientUserLastTimeConnection.setText(lastTimeConnection);
        }
    }


    //TODO: Block online/offline
    public void updateUserStatus(String state) {
        Online_Offline_Service service = new Online_Offline_Service();
        service.updateUserStatus(state, this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        adapter.notifyDataSetChanged();
        updateUserStatus("online");
        active = true;
    }

    @Override
    protected void onPause() {
        super.onPause();
        updateUserStatus("offline");
        active = false;
        deleteStartWriting();
    }

    @Override
    protected void onStart() {
        super.onStart();
        active = true;
    }

    @Override
    protected void onStop() {
        super.onStop();
        active = false;
    }

    //todo: message always been in bottom of list
    private void scrollMyListViewToBottom() {
        messageListView.post(new Runnable() {
            @Override
            public void run() {
                // Select the last row so it will scroll into view...
                messageListView.setSelection(adapter.getCount() - 1);
            }
        });
    }

    //todo: all staff image

    private boolean checkCurrentPermission() {
        Check_Permission_Service service = new Check_Permission_Service();
        if(service.checkPermission(this, Chat_Main_Activity.this, Manifest.permission.CAMERA, App_Constants.CAMERA_REQUEST_CODE)
                && service.checkPermission(this,Chat_Main_Activity.this, Manifest.permission.READ_EXTERNAL_STORAGE, App_Constants.READING_REQUEST_CODE)
                && service.checkPermission(this,Chat_Main_Activity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE, App_Constants.WRITE_REQUEST_CODE)){
            return true;
        }
        return false;
    }

    private void showImagePickDialog() {
        String[] options = {"Camera","Gallery"};
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Choose image from");
        builder.setItems(options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if(which==0){

                    pickFromCamera();
                }
                if(which==1){
                    pickFromGallery();
                }
            }
        });
        builder.create().show();
    }

    private void pickFromGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, App_Constants.IMAGE_PICK_GALLERY_CODE);
    }

    private void pickFromCamera() {
        ContentValues cv= new ContentValues();
        cv.put(MediaStore.Images.Media.TITLE, "Temp Pick");
        cv.put(MediaStore.Images.Media.DESCRIPTION, "Temp Descr");
        image_rui = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, cv);

        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, image_rui);
        startActivityForResult(intent, App_Constants.IMAGE_PICK_CAMERA_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        //todo: get path to file
        Uri selectedImageUri;
        if(resultCode == RESULT_OK){
            if(requestCode == App_Constants.IMAGE_PICK_GALLERY_CODE || requestCode == App_Constants.IMAGE_PICK_CAMERA_CODE ){
                if(requestCode == App_Constants.IMAGE_PICK_CAMERA_CODE){
                    selectedImageUri = image_rui;
                }
                else{
                    assert data != null;
                    selectedImageUri = data.getData();
                }

                //todo: Compress image
                Bitmap bitmap = null;
                try {
                    bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), selectedImageUri);
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 50, baos );
                    dataCompr = baos.toByteArray();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                StorageReference imageReference = chatImagesStorageReference.child("image").child(Arrays.toString(selectedImageUri.getLastPathSegment().split("/")));

                //todo: upload on server
                UploadTask uploadTask = imageReference.putFile(selectedImageUri);
                uploadTask = imageReference.putBytes(dataCompr);

                progressDialog.setMessage("Sending image...");
                progressDialog.show();
                progressDialog.getCurrentFocus();


                Task<Uri> urlTask = uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                    @Override
                    public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                        if (!task.isSuccessful()) {
                            progressDialog.dismiss();
                            throw Objects.requireNonNull(task.getException());
                        }

                        // todo: Continue with the task to get the download URL
                        progressDialog.dismiss();
                        return imageReference.getDownloadUrl();
                    }
                }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                    @Override
                    public void onComplete(@NonNull Task<Uri> task) {
                        scrollMyListViewToBottom();
                        if (task.isSuccessful()) {
                            Uri downloadUri = task.getResult();
                            Model_Message message = new Model_Message();

                            pushCommonMessage(message, "imageMessage", downloadUri, null, null, null, null, null);

                        }
                    }
                });
            }
        }
    }

}