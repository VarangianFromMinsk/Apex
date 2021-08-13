package com.example.myapplication.main.Screens.User_Profile_MVVM.Fragments;

import android.Manifest;
import android.app.Activity;
import android.app.ActivityOptions;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import android.provider.MediaStore;
import android.text.InputType;
import android.util.Log;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.myapplication.R;
import com.example.myapplication.Services.App_Constants;
import com.example.myapplication.Services.Check_Permission_Service;
import com.example.myapplication.databinding.FragmentUserUperBlockInfoBinding;
import com.example.myapplication.main.Models.Model_User;
import com.example.myapplication.main.Screens.Find_Selected_Or_My_User_Location.Users_FInd_Location;
import com.example.myapplication.main.Screens.Settings.Settings_Activity;
import com.example.myapplication.main.Screens.Show_Image_MVP.Show_Image_Activity;
import com.example.myapplication.main.Screens.Sing_In_MVP.Sign_In_Activity;
import com.example.myapplication.main.Screens.User_Profile_MVVM.Photo_From_Gallery.ActionFromGallery;
import com.example.myapplication.main.Screens.User_Profile_MVVM.Photo_From_Gallery.ItemListDialogFragment;
import com.example.myapplication.main.Screens.User_Profile_MVVM.Profile_ViewModel;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;


public class User_UperBlock_Info_Fragment extends Fragment  {


    private String nameFromBase, emailFromBase, nickFromBase, telephone, avatarUrlFromBase, backgroundImage, statusOn, timeOnlineDb;

    private String selectedUser;
    private boolean isThatCurrentUser;

    private FirebaseUser user;
    private FragmentUserUperBlockInfoBinding binding;
    private Profile_ViewModel viewModel;

    private byte[] dataCompr;
    private ProgressDialog progressDialog;

    private showGalleryPhoto galleryInterface;
    private String avatarOrBackground;
    private String imagePath = "";
    private Uri image_rui = null;

    private ActivityResultLauncher<Intent> userProfile_Avatar_ResultLauncher;
    private ActivityResultLauncher<Intent> userProfile_BackgroundImage_ResultLauncher;


    public static User_UperBlock_Info_Fragment newInstance(String selectedUser, boolean isThatCurrentUser) {
        User_UperBlock_Info_Fragment fragment = new User_UperBlock_Info_Fragment();
        Bundle args = new Bundle();
        args.putString("SelectedUser", selectedUser);
        args.putBoolean("isThatCurrentUser", isThatCurrentUser);
        fragment.setArguments(args);
        return fragment;
    }



    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        try {
            galleryInterface = (showGalleryPhoto) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString() + "activity must implement interface");
        }

        userProfile_Avatar_ResultLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                new ActivityResultCallback<ActivityResult>() {
                    @Override
                    public void onActivityResult(ActivityResult result) {
                        if (result.getResultCode() == Activity.RESULT_OK) {

                            commonUpdateMethod(false,null, result, "avatar");
                        }
                    }
                });

        userProfile_BackgroundImage_ResultLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                new ActivityResultCallback<ActivityResult>() {
                    @Override
                    public void onActivityResult(ActivityResult result) {
                        if (result.getResultCode() == Activity.RESULT_OK) {

                            commonUpdateMethod(false,null, result, "background");
                        }
                    }
                });

    }

    public void commonUpdateMethod(Boolean isThatFromBottomSheet, String pathOfImage, ActivityResult result, String avatarOrBackground) {

        Uri selectedImageUri;

        Intent data = null;
        try {
            data = result.getData();
        } catch (Exception e) {
            Log.d("Loading image","load from gallery view");
        }

        if(data != null || isThatFromBottomSheet || image_rui != null){
            final StorageReference imageReference;

            if(image_rui != null){
                selectedImageUri = image_rui;
            }
            else if (isThatFromBottomSheet){
                selectedImageUri = Uri.parse(pathOfImage);
            }
            else{
                selectedImageUri = data.getData();
            }

            String timeStamp = String.valueOf(System.currentTimeMillis());
           
            if(avatarOrBackground.equals("avatar")){
                progressDialog.setMessage("Sending avatar...");
                StorageReference userAvatarStorageReference = FirebaseStorage.getInstance().getReference().child("users_images");
                timeStamp = String.valueOf(System.currentTimeMillis());
                imageReference = userAvatarStorageReference.child("image_" + timeStamp);
            }
            else{
                progressDialog.setMessage("Sending background...");
                StorageReference userBackgroudStorageReference = FirebaseStorage.getInstance().getReference().child("users_backgrounds");
                imageReference = userBackgroudStorageReference.child("image_" + timeStamp);
            }
            progressDialog.show();

            //todo: Compress image
            Bitmap bitmap = null;
            try {
                bitmap = MediaStore.Images.Media.getBitmap(requireActivity().getContentResolver(), selectedImageUri);
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.JPEG, 50, baos);
                dataCompr = baos.toByteArray();
            } catch (IOException e) {
                e.printStackTrace();
            }


            //todo: upload on server
            UploadTask uploadTask = imageReference.putFile(selectedImageUri);
            uploadTask = imageReference.putBytes(dataCompr);

            Task<Uri> urlTask = uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                @Override
                public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                    if (!task.isSuccessful()) {
                        progressDialog.dismiss();
                        Log.d("User Profile", "upload to firebase failed");
                    }
                    //todo: Continue with the task to get the download URL
                    progressDialog.dismiss();
                    return imageReference.getDownloadUrl();
                }
            }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                @Override
                public void onComplete(@NonNull Task<Uri> task) {
                    if (task.isSuccessful()) {
                        Uri downloadUri = task.getResult();
                        if(!avatarOrBackground.equals("avatar")){
                            viewModel.updateCurrentAvatarOrBackground(selectedUser, "background", downloadUri.toString());
                        }
                        else{
                            viewModel.updateCurrentAvatarOrBackground(selectedUser, "avatarMockUpResourse", downloadUri.toString());
                        }
                        galleryInterface.updateProfileAfterChangeAvatarOrBackground();
                    } else {
                        Toast.makeText(requireContext(), "Loading failed", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
        else{
            Toast.makeText(requireContext(), "Loading failed", Toast.LENGTH_SHORT).show();
        }

    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            selectedUser = getArguments().getString("SelectedUser");
            isThatCurrentUser = getArguments().getBoolean("isThatCurrentUser");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater,
                R.layout.fragment_user__uper_block__info,
                container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initializing();
        viewModel = new ViewModelProvider(requireActivity()).get(Profile_ViewModel.class);
        loadUserInfo();

        initShowBigProfilePhoto(view);
        initChangePasswordAction();
        initCheckSettings();
        initSingOutFromProfile();

        initLoadAvatarAction();
        initLoadBackgroundImageAction();
        initShowGeoPosition();

        waitToLoadFromGalleryView();

    }

    private void initializing() {
        user = FirebaseAuth.getInstance().getCurrentUser();
        progressDialog = new ProgressDialog(requireContext());
    }

    private void waitToLoadFromGalleryView() {
        viewModel.getShareImages().observe(getViewLifecycleOwner(), new Observer<ArrayList<String>>() {
            @Override
            public void onChanged(ArrayList<String> images) {
                commonUpdateMethod(true,images.get(0), null, avatarOrBackground);
                galleryInterface.disableProgressBar();
            }
        });
    }



    public interface showGalleryPhoto{

        void disableProgressBar();

        void updateProfileAfterChangeAvatarOrBackground();

        void showGalleryPhotoInterface(String avatarOrBackground);
    }

    private void loadUserInfo() {

        viewModel.getMutCurrentUser().observe(getViewLifecycleOwner(), new Observer<Model_User>() {
            @Override
            public void onChanged(Model_User user) {
                if (!isThatCurrentUser) {
                    hideViewsIfIsStrangerProfile();
                }

                //TODO: get data
                avatarUrlFromBase = user.getAvatarMockUpResourse();
                nameFromBase = user.getName();
                statusOn = user.getOnline();
                timeOnlineDb = user.getTimeonline();

                //TODO: set data
                binding.onOffLineProfile.setText(statusOn);
                backgroundImage = user.getBackground();

                try {
                    nickFromBase = user.getNick();
                    telephone = user.getTelephone();
                } catch (Exception ignored) {
                }


                if (statusOn.equals("online")) {
                    binding.onOffLineProfile.setTextColor(Color.parseColor("#7AC537"));
                    binding.timeOnlineProfile.setVisibility(View.GONE);
                } else {
                    binding.onOffLineProfile.setTextColor(Color.parseColor("#D32121"));
                    binding.timeOnlineProfile.setVisibility(View.VISIBLE);
                    binding.timeOnlineProfile.setText(timeOnlineDb);
                }

                try {
                    binding.profileScreenName.setText(nameFromBase);
                    binding.profileScreenNickname.setText(nickFromBase);
                } catch (Exception ignored) {
                }

                //TODO: set images
                try {
                    if (!backgroundImage.equals("")) {
                        Glide.with(binding.backInUserProfile).load(backgroundImage).into(binding.backInUserProfile);
                    } else {
                        Glide.with(binding.backInUserProfile).load(R.drawable.wallpaper).into(binding.backInUserProfile);
                    }
                    if (!avatarUrlFromBase.equals("")) {
                        Glide.with(binding.profileScreenAvatar).load(avatarUrlFromBase).placeholder(R.drawable.default_avatar).into(binding.profileScreenAvatar);
                    }
                } catch (Exception ignored) {
                }
            }
        });
    }

    private void initShowGeoPosition() {
        binding.locationButtonInProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent location = new Intent(requireContext(), Users_FInd_Location.class)
                        .putExtra("selectedUser", selectedUser);
                startActivity(location);
            }
        });
    }

    private void initLoadBackgroundImageAction() {
        binding.addBackground.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (checkCurrentPermission()) {
                    showImagePickDialog("background");
                }
            }
        });
    }

    private void initLoadAvatarAction() {
        binding.addUserPhotoButtonInProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (checkCurrentPermission()) {
                    showImagePickDialog("avatar");
                }
            }
        });
    }

    private boolean checkCurrentPermission() {
        return Check_Permission_Service.checkPermission(requireContext(), requireActivity(), Manifest.permission.CAMERA, App_Constants.CAMERA_REQUEST_CODE_POST)
                && Check_Permission_Service.checkPermission(requireContext(), requireActivity(), Manifest.permission.READ_EXTERNAL_STORAGE, App_Constants.READING_REQUEST_CODE_POST)
                && Check_Permission_Service.checkPermission(requireContext(), requireActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE, App_Constants.WRITE_REQUEST_CODE_POST);
    }

    private void showImagePickDialog(String avatarOrBackgroundCheck) {
        String[] options = {"Camera", "Gallery"};
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Choose image from");
        builder.setItems(options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (which == 0) {
                    pickFromCamera(avatarOrBackgroundCheck);
                }
                if (which == 1) {
                    galleryInterface.showGalleryPhotoInterface(avatarOrBackgroundCheck);
                    avatarOrBackground = avatarOrBackgroundCheck;
                   // pickFromGallery(avatarOrBackground);
                }
            }
        });
        builder.create().show();
    }

    private void pickFromGallery(String avatarOrBackground) {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");

        checkBeforePickPhoto(avatarOrBackground, intent);
    }

    private void pickFromCamera(String avatarOrBackground) {
        ContentValues cv = new ContentValues();
        cv.put(MediaStore.Images.Media.TITLE, "Temp Pick");
        cv.put(MediaStore.Images.Media.DESCRIPTION, "Temp Descr");
        image_rui = requireActivity().getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, cv);

        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, image_rui);

        checkBeforePickPhoto(avatarOrBackground, intent);
    }

    private void checkBeforePickPhoto(String avatarOrBackground, Intent intent) {
        if (avatarOrBackground.equals("avatar")) {
            userProfile_Avatar_ResultLauncher.launch(intent);
        } else {
            userProfile_BackgroundImage_ResultLauncher.launch(intent);
        }
    }


    private void initSingOutFromProfile() {
        binding.singOutButtonInProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(requireContext());
                builder.setMessage("Sing Out?");
                //delete button
                builder.setPositiveButton("exit", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        FirebaseAuth.getInstance().signOut();    // Выходим из аккаунта
                        // updateUserStatus("offline");
                        startActivity(new Intent(requireContext(), Sign_In_Activity.class));
                    }
                });
                builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                builder.create().show();
            }
        });
    }

    private void initCheckSettings() {
        binding.settingsButtonInProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent GoToSettingsFromProfile = new Intent(requireContext(), Settings_Activity.class);
                startActivity(GoToSettingsFromProfile);
            }
        });
    }

    private void initChangePasswordAction() {
        binding.passwordButtonInProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isThatCurrentUser) {
                    ChangePassword();
                }
            }
        });
    }

    private void ChangePassword() {
        final String email = user.getEmail();

        //todo: dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Change Password");

        RelativeLayout relativeLayout = new RelativeLayout(requireContext());

        EditText prevPass = new EditText(requireContext());
        prevPass.setInputType(InputType.TYPE_TEXT_VARIATION_PASSWORD);
        prevPass.setId(View.generateViewId());
        prevPass.setHint("  Previous password                                                     ");
        relativeLayout.addView(prevPass);

        EditText newPass = new EditText(requireContext());
        newPass.setHint(" New password                                                       ");
        newPass.setInputType(InputType.TYPE_TEXT_VARIATION_PASSWORD);

        RelativeLayout.LayoutParams newPassLayoutParams = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.WRAP_CONTENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT);

        //todo: Also, we want tv2 to appear below tv1, so we are adding rule to tv2LayoutParams.
        newPassLayoutParams.addRule(RelativeLayout.BELOW, prevPass.getId());

        relativeLayout.addView(newPass, newPassLayoutParams);
        relativeLayout.setPadding(50, 10, 50, 10);

        builder.setView(relativeLayout);

        builder.setPositiveButton("Change", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (email != null) {
                    String oldPass = prevPass.getText().toString().trim();
                    AuthCredential credential = EmailAuthProvider.getCredential(email, oldPass);

                    user.reauthenticate(credential).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                String newPassword = newPass.getText().toString().trim();

                                viewModel.updatePassword(user, newPassword);
                                viewModel.getCheckChangePassword().observe(getViewLifecycleOwner(), new Observer<String>() {
                                    @Override
                                    public void onChanged(String s) {
                                        Toast.makeText(requireContext(), s, Toast.LENGTH_LONG).show();
                                    }
                                });
                            } else {
                                Toast.makeText(requireContext(), "Wrong previous Password", Toast.LENGTH_LONG).show();
                            }
                        }
                    });
                } else {
                    Toast.makeText(requireContext(), "Please, reload app", Toast.LENGTH_LONG).show();
                }
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.create().show();
    }

    private void initShowBigProfilePhoto(View view) {
        binding.profileScreenAvatar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!avatarUrlFromBase.equals("")) {
                    Intent intent = new Intent(requireContext(), Show_Image_Activity.class);
                    intent.putExtra("imageURL", avatarUrlFromBase);

                    Pair<View, String> pair1 = Pair.create(view.findViewById(R.id.profileScreenAvatar), "avatar");

                    ActivityOptions activityOptions = null;
                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
                        activityOptions = ActivityOptions.makeSceneTransitionAnimation(requireActivity(), pair1);
                        startActivity(intent, activityOptions.toBundle());
                    } else {
                        startActivity(intent);
                    }
                }

            }
        });
    }

    private void hideViewsIfIsStrangerProfile() {
        binding.passwordButtonInProfile.setVisibility(View.GONE);
        binding.settingsButtonInProfile.setVisibility(View.GONE);
        binding.singOutButtonInProfile.setVisibility(View.GONE);
        binding.addBackground.setVisibility(View.GONE);
        binding.addUserPhotoButtonInProfile.setVisibility(View.GONE);

        RelativeLayout.LayoutParams newParams = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.WRAP_CONTENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT);

        newParams.addRule(RelativeLayout.ALIGN_PARENT_START, RelativeLayout.ALIGN_PARENT_TOP);

        binding.relLayWithLocBtn.setLayoutParams(newParams);
    }

}