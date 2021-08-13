package com.example.myapplication.main.Screens.Chat_Activity_MVP;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.Manifest;
import android.app.Activity;
import android.app.ActivityOptions;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
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
import android.util.Log;
import android.util.Pair;
import android.view.View;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.myapplication.Common_Dagger_App_Class.App;
import com.example.myapplication.R;
import com.example.myapplication.Services.App_Constants;
import com.example.myapplication.Services.Check_Permission_Service;
import com.example.myapplication.Services.Online_Offline_User_Service_To_Firebase;
import com.example.myapplication.databinding.ChatMainActivityBinding;
import com.example.myapplication.main.Screens.Dashboard_MVP.Dashboard_Activity;
import com.example.myapplication.main.Screens.Settings.Settings_Activity;
import com.example.myapplication.main.Models.Model_Message;
import com.example.myapplication.main.Screens.User_Profile_MVVM.User_Profile_Activity;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import org.jetbrains.annotations.NotNull;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import javax.inject.Inject;

public class Chat_Main_Activity extends AppCompatActivity implements ChangeMessage_Interface {

    @Inject
    Online_Offline_User_Service_To_Firebase controller;

    @Inject
    LinearLayoutManager layoutManager;


    private Message_Adapter adapter;
    private ProgressDialog progressDialog;

    private String myName, myAvatarUrl, myUid;
    private String recipientUserId, recipientUserName, recipientAvatar;

    private FirebaseAuth auth;
    private StorageReference chatImagesStorageReference;

    private byte[] dataCompr;
    private Uri image_rui = null;

    private Locale locale;

    //todo: static check is Activity started
    public static boolean active = false;

    //todo: for "start Writing"
    private CountDownTimer timer;
    int countOfMessage = 0;

    private Chat_ViewModel viewModel;
    private ChatMainActivityBinding binding;

    private ActivityResultLauncher<Intent> Chat_ResultLauncher;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.chat_main_activity);

        Objects.requireNonNull(getSupportActionBar()).hide();

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        initialization();

        createMainLocal();

        getMainIntent();

        userListener();

        createListView();

        messageListener();

        checkPermission();

        makeCustomToolBar();

        getShareImage();

        getRepostIntent();

        eventOfMessageEditText();

        sendTextMessageBtn();

        sendImageBtn();

        initImageResultListener();

        showOnlineOffline();

        setAvatar();

        //todo: in process
        //voiceMessage();

        showIsHisWritingToYou();
    }

    private void initialization() {
        ((App) getApplication()).getCommonComponent().inject(this);

        viewModel = new ViewModelProvider(this).get(Chat_ViewModel.class);

        binding = DataBindingUtil.setContentView(this, R.layout.chat_main_activity);

        //TODO: lifecycle
        getLifecycle().addObserver(controller);


        binding.buttonRefreshChat.animate().translationY(+200).setDuration(10);

        auth = FirebaseAuth.getInstance();
        myUid = auth.getUid();
        progressDialog = new ProgressDialog(this);

        FirebaseStorage storage = FirebaseStorage.getInstance();
        chatImagesStorageReference = storage.getReference().child("chat_images");
    }

    private void createMainLocal() {
        locale = new Locale("ru");
        Locale.setDefault(locale);
        Configuration config = getBaseContext().getResources().getConfiguration();
        config.locale = locale;
        getBaseContext().getResources().updateConfiguration(config,
                getBaseContext().getResources().getDisplayMetrics());
    }

    private void getMainIntent() {
        Intent intent = getIntent();
        if (intent != null) {
            recipientUserId = intent.getStringExtra("recipientUserId");
            recipientUserName = intent.getStringExtra("recipientUserName");
            recipientAvatar = intent.getStringExtra("recipientAvatar");
        }
    }

    private void makeCustomToolBar() {

        if (!recipientAvatar.equals("")) {
            try {
                Glide.with(binding.avatarInToolBar).load(recipientAvatar).into(binding.avatarInToolBar);
            } catch (Exception ignored) {
            }
        }

        binding.textNameToolBar.setText(recipientUserName);

        binding.avatarInToolBar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent GoToProfile = new Intent(Chat_Main_Activity.this, User_Profile_Activity.class);
                GoToProfile.putExtra("hisId", recipientUserId);

                Pair<View, String> pairAvatar = Pair.create(findViewById(R.id.avatarInToolBar), "avatar2");
                Pair<View, String> pairName = Pair.create(findViewById(R.id.textNameToolBar), "name");

                ActivityOptions activityOptions = null;
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
                    activityOptions = ActivityOptions.makeSceneTransitionAnimation(Chat_Main_Activity.this, pairAvatar, pairName);
                    startActivity(GoToProfile, activityOptions.toBundle());
                } else {
                    startActivity(GoToProfile);
                }
            }
        });


    }


    @Override
    public void changeMessageStart(String messageFromBase, DataSnapshot ds) {
        //hide
        binding.messageEditText.setVisibility(View.GONE);
        binding.sendMessageButton.setVisibility(View.GONE);

        //show new
        binding.changeMessage.setVisibility(View.VISIBLE);
        binding.changeButton.setVisibility(View.VISIBLE);

        //set changing text
        binding.changeMessage.setText(messageFromBase);

        binding.changeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String newMessage = binding.changeMessage.getText().toString().trim();
                HashMap<String, Object> hashMap = new HashMap<>();
                hashMap.put("text", newMessage);
                ds.getRef().updateChildren(hashMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        //return state back
                        binding.changeMessage.setVisibility(View.GONE);
                        binding.changeButton.setVisibility(View.GONE);

                        binding.messageEditText.setVisibility(View.VISIBLE);
                        binding.sendMessageButton.setVisibility(View.VISIBLE);
                    }
                });
            }
        });

    }


    //todo: load messages
    private void messageListener() {

        viewModel.loadMessages(auth, recipientUserId);

        viewModel.getNewMessage().observe(this, new Observer<Model_Message>() {
            @Override
            public void onChanged(Model_Message message) {
                adapter.add(message);
                //adapter.notifyDataSetChanged();
                countOfMessage++;
                Log.d("timesOfUpdate", "time" + countOfMessage );
            }
        });

        viewModel.getRefreshChatCheck().observe(this, new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean state) {
                if (state) {
                    refreshBtn();
                }
            }
        });
    }

    //todo: part of messageEt event and start writing
    private void eventOfMessageEditText() {
        binding.messageEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                if (s.length() >= 3) {
                    countDownTimerStartWritting();
                } else {
                    removeChatTimer();
                }
            }


            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

                if (s.toString().trim().length() > 0) {
                    binding.sendMessageButton.setEnabled(true);
                    binding.sendMessageButton.setImageResource(R.drawable.send);
                } else {
                    binding.sendMessageButton.setEnabled(false);
                    binding.sendMessageButton.setImageResource(R.drawable.send_focus);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        //todo: set max length of message
        binding.messageEditText.setFilters(new InputFilter[]{new InputFilter.LengthFilter(250)});
    }

    private void showIsHisWritingToYou() {
        viewModel.showIsHisWritingToYou(recipientUserId);

        viewModel.getShowIfWriting().observe(this, new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean state) {
                if (state) {
                    binding.textingToYou.animate().alpha(1).translationY(0).setDuration(600);

                    binding.massageList.animate().translationY(-60).setDuration(400);
                } else {
                    binding.textingToYou.animate().alpha(0).translationY(+40).setDuration(600);
                    binding.massageList.animate().translationY(0).setDuration(400);
                }
            }
        });
    }


    private void removeChatTimer() {
        if (timer != null) {
            timer.onFinish();
            timer.cancel();
            timer = null;
        }
    }

    private void countDownTimerStartWritting() {
        timer = new CountDownTimer(3000, 500) {
            public void onTick(long millisUntilFinished) {
                if (binding.messageEditText.getText().toString().length() > 3) {
                    createStartWriting();
                } else {
                    deleteStartWriting();
                }
            }

            public void onFinish() {
                deleteStartWriting();
            }
        }.start();

    }

    private void createStartWriting() {
        viewModel.createStartWriting(myUid, recipientUserId);
    }

    private void deleteStartWriting() {
        viewModel.deleteStartWriting(myUid);
    }


    //todo: common pushMessage
    private void pushCommonMessage(Model_Message message, String typeOfMessage, Uri downloadUri, String shareImage, String pDescription, String uName, String pImage, String pId) {

        String text = binding.messageEditText.getText().toString().trim();

        viewModel.pushCommonMessage(message, typeOfMessage, downloadUri, shareImage, pDescription, uName, pImage,
                pId, this, locale, myName, recipientUserId, auth, myUid, myAvatarUrl, text);

        Log.d("myName", myName + myUid);
    }

    //todo: create list and get info about messages and users
    private void createListView() {
        List<Model_Message> modelMessages = new ArrayList<>();
        adapter = new Message_Adapter(this, R.layout.message_item, modelMessages);

        binding.massageList.setAdapter(adapter);
    }

    private void userListener() {
        viewModel.loadInfoAboutUser(myUid);

        viewModel.getMyAvatarCheck().observe(this, new Observer<String>() {
            @Override
            public void onChanged(String avatar) {
                myAvatarUrl = avatar;
            }
        });

        viewModel.getMyNameCheck().observe(this, new Observer<String>() {
            @Override
            public void onChanged(String name) {
                myName = name;
            }
        });
    }


    //todo: refresh Chat
    public void refreshBtn() {
        binding.buttonRefreshChat.setVisibility(View.VISIBLE);
        binding.buttonRefreshChat.animate().alpha(1).translationY(0).setDuration(300);
        binding.massageList.animate().translationY(-120).setDuration(200);

        binding.buttonRefreshChat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = getIntent();
                finish();
                overridePendingTransition(0, 0);
                startActivity(intent);
            }
        });

        viewModel.disableRefresh();
    }

    //todo: main sends event
    public void sendTextMessageBtn() {
        binding.sendMessageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!binding.messageEditText.getText().toString().trim().isEmpty()) {
                    Model_Message message = new Model_Message();
                    pushCommonMessage(message, "textMessage", null, null, null, null, null, null);
                    //layoutManager.scrollToPosition(messagesCheck.size() - 1);
                    layoutManager.setStackFromEnd(true);
                    binding.messageEditText.setText("");
                } else {
                    Toast.makeText(Chat_Main_Activity.this, "Empty", Toast.LENGTH_SHORT).show();
                }

            }
        });

    }

    private void sendImageBtn() {
        binding.sendPhotoButton.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.M)
            @Override
            public void onClick(View v) {
                showImagePickDialog();
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
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            Intent intentFromShareImage = getIntent();
            if (intentFromShareImage.getStringExtra("shareImage") != null) {
                String shareImage = intentFromShareImage.getStringExtra("shareImage");

                changeStateOfEditText(true);

                binding.shareContentButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Model_Message message = new Model_Message();
                        pushCommonMessage(message, "shareImageMessage", null, shareImage, null, null, null, null);
                        adapter.notifyDataSetChanged();
                        changeStateOfEditText(false);
                    }
                });

            }
        }
    }

    private void getRepostIntent() {
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            Intent intentFromPost = getIntent();
            if (intentFromPost.getStringExtra("isIntentExist") != null) {
                String pDescription = intentFromPost.getStringExtra("Text");
                String uName = intentFromPost.getStringExtra("pUserName");
                String pImage = intentFromPost.getStringExtra("pImage");
                String pId = intentFromPost.getStringExtra("pId");

                changeStateOfEditText(true);

                binding.shareContentButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Model_Message message = new Model_Message();
                        pushCommonMessage(message, "repostMessage", null, null, pDescription, uName, pImage, pId);
                        adapter.notifyDataSetChanged();
                        changeStateOfEditText(false);
                    }
                });
            }
        }
    }

    private void changeStateOfEditText(boolean state){
        if(state){
            binding.sendPhotoButton.setVisibility(View.GONE);
            binding.messageEditText.setVisibility(View.GONE);
            binding.sendMessageButton.setVisibility(View.GONE);
            binding.changeButton.setVisibility(View.GONE);

            binding.sendShareContent.setVisibility(View.VISIBLE);
            binding.shareContentButton.setVisibility(View.VISIBLE);
        }
        else{
            binding.sendPhotoButton.setVisibility(View.VISIBLE);
            binding.messageEditText.setVisibility(View.VISIBLE);
            binding.sendMessageButton.setVisibility(View.VISIBLE);
            binding.sendShareContent.setVisibility(View.GONE);
            binding.shareContentButton.setVisibility(View.GONE);
        }
    }


    //todo: customize
    private void setAvatar() {
        viewModel.loadRecipientUserInfo(recipientUserId);
        viewModel.getHisInfoAndAvatar().observe(this, new Observer<String>() {
            @Override
            public void onChanged(String hisImage) {
                try {
                    Glide.with(binding.avatarInToolBar).load(hisImage).placeholder(R.drawable.default_avatar).into(binding.avatarInToolBar);
                } catch (Exception ignored) {
                }
            }
        });
    }

    public void settingsFromChat(View view) {
        Intent GoToSettingsFromProfile = new Intent(this, Settings_Activity.class);
        startActivity(GoToSettingsFromProfile);
    }

    private void showOnlineOffline() {

        viewModel.isHeOnline(recipientUserId);

        viewModel.getIsOnline().observe(this, new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean state) {
                Log.d("Chat", String.valueOf(state));
                if (state) {
                    binding.textStatusToolBar.setText(R.string.online_text);
                } else {
                    binding.textStatusToolBar.setText(R.string.offline_text);
                }
            }
        });

        viewModel.getLastTimeConnectionCheck().observe(this, new Observer<String>() {
            @Override
            public void onChanged(String lastTimeConnection) {
                changeColourStatus(lastTimeConnection);
            }
        });
    }


    private void changeColourStatus(String lastTimeConnection) {
        if (binding.textStatusToolBar.getText().equals("online")) {
            binding.textStatusToolBar.setTextColor(Color.parseColor("#7AC537"));
            binding.textLastTimeToolBar.setVisibility(View.INVISIBLE);
        } else {
            binding.textStatusToolBar.setTextColor(Color.parseColor("#D32121"));
            binding.textLastTimeToolBar.setVisibility(View.VISIBLE);
            binding.textLastTimeToolBar.setText(lastTimeConnection);
        }
    }


    @Override
    protected void onResume() {
        super.onResume();
        adapter.notifyDataSetChanged();
        active = true;
    }

    @Override
    protected void onPause() {
        super.onPause();
        active = false;
        deleteStartWriting();
    }


    //todo: message always been in bottom of list
    private void scrollMyListViewToBottom() {
        binding.massageList.post(new Runnable() {
            @Override
            public void run() {
                //  binding.massageList.setSelection(adapter.getCount() - 1);
            }
        });
    }

    protected void checkPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                + ContextCompat.checkSelfPermission(
                this, Manifest.permission.ACCESS_FINE_LOCATION)
                + ContextCompat.checkSelfPermission(
                this, Manifest.permission.ACCESS_COARSE_LOCATION)
                + ContextCompat.checkSelfPermission(
                this, Manifest.permission.READ_EXTERNAL_STORAGE)
                + ContextCompat.checkSelfPermission(
                this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {

            // Do something, when permissions not granted
            if (ActivityCompat.shouldShowRequestPermissionRationale(
                    this, Manifest.permission.CAMERA)
                    || ActivityCompat.shouldShowRequestPermissionRationale(
                    this, Manifest.permission.READ_EXTERNAL_STORAGE)
                    || ActivityCompat.shouldShowRequestPermissionRationale(
                    this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                // If we should give explanation of requested permissions

                // Show an alert dialog here with request explanation
                android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
                builder.setMessage("Camera, Read External and Write External, Your Geo-position" +
                        "This permissions are needed for normal work.");
                builder.setTitle("Please grant those permissions");
                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        ActivityCompat.requestPermissions(
                                Chat_Main_Activity.this,
                                new String[]{
                                        Manifest.permission.CAMERA,
                                        Manifest.permission.READ_EXTERNAL_STORAGE,
                                        Manifest.permission.WRITE_EXTERNAL_STORAGE,
                                        Manifest.permission.ACCESS_FINE_LOCATION,
                                        Manifest.permission.ACCESS_COARSE_LOCATION
                                },
                                App_Constants.ALL_APP_REQUEST_CODE
                        );
                    }
                });
                builder.setNeutralButton("Cancel", null);
                android.app.AlertDialog dialog = builder.create();
                dialog.show();
            } else {
                // Directly request for required permissions, without explanation
                ActivityCompat.requestPermissions(
                        this,
                        new String[]{
                                Manifest.permission.CAMERA,
                                Manifest.permission.READ_EXTERNAL_STORAGE,
                                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                                Manifest.permission.ACCESS_FINE_LOCATION,
                                Manifest.permission.ACCESS_COARSE_LOCATION
                        },
                        App_Constants.ALL_APP_REQUEST_CODE
                );
            }
        } else {
            // Do something, when permissions are already granted
            Log.d("Perm already granted", "yes");
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NotNull String[] permissions, @NotNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case App_Constants.ALL_APP_REQUEST_CODE: {
                // When request is cancelled, the results array are empty
                if (
                        (grantResults.length > 0) &&
                                (grantResults[0]
                                        + grantResults[1]
                                        + grantResults[2]
                                        + grantResults[3]
                                        + grantResults[4]
                                        == PackageManager.PERMISSION_GRANTED
                                )
                ) {
                    // Permissions are granted
                    Toast.makeText(this, "Thanks, enjoy!", Toast.LENGTH_SHORT).show();
                } else {
                    // Permissions are denied
                    Toast.makeText(this, "Permissions denied.", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }


    //todo: all staff image
    private void showImagePickDialog() {
        String[] options = {"Camera", "Gallery"};
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Choose image from");
        builder.setItems(options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (which == 0) {
                    pickFromCamera();
                }
                if (which == 1) {
                    pickFromGallery();
                }
            }
        });
        builder.create().show();
    }

    private void pickFromGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK)
                .setType("image/*");
        Chat_ResultLauncher.launch(intent);
    }

    private void pickFromCamera() {
        ContentValues cv = new ContentValues();
        cv.put(MediaStore.Images.Media.TITLE, "Temp Pick");
        cv.put(MediaStore.Images.Media.DESCRIPTION, "Temp Descr");
        image_rui = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, cv);

        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, image_rui);

        Chat_ResultLauncher.launch(intent);
    }

    private void initImageResultListener() {
        Chat_ResultLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                new ActivityResultCallback<ActivityResult>() {
                    @Override
                    public void onActivityResult(ActivityResult result) {
                        if (result.getResultCode() == Activity.RESULT_OK) {

                            Uri selectedImageUri;
                            Intent data = result.getData();

                            if (data != null || image_rui != null) {

                                if(image_rui != null){
                                    selectedImageUri = image_rui;
                                }
                                else{
                                    selectedImageUri = data.getData();
                                }

                                String timeStamp = String.valueOf(System.currentTimeMillis());
                                String fileNameAndPath =  "image_" + timeStamp;

                                StorageReference imageReference = chatImagesStorageReference.child("image")
                                        .child(fileNameAndPath);

                                //todo: Compress image
                                Bitmap bitmap = null;
                                try {
                                    bitmap = MediaStore.Images.Media.getBitmap(Chat_Main_Activity.this.getContentResolver(), selectedImageUri);
                                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                                    bitmap.compress(Bitmap.CompressFormat.JPEG, 50, baos);
                                    dataCompr = baos.toByteArray();
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }


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
                                            Toast.makeText(Chat_Main_Activity.this, "Something's wrong", Toast.LENGTH_SHORT).show();
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
                                            adapter.notifyDataSetChanged();
                                        }
                                    }
                                });
                            }
                        }
                    }
                });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }
}