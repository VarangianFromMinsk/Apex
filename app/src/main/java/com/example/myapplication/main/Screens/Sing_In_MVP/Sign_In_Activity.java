package com.example.myapplication.main.Screens.Sing_In_MVP;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import com.example.myapplication.main.Screens.Dashboard_MVP.Dashboard_Activity;
import com.example.myapplication.R;
import com.google.firebase.auth.FirebaseAuth;

public class Sign_In_Activity extends AppCompatActivity implements Sing_In_view{

    private static final String TAG = "Sign_In_Activity";
    private FirebaseAuth auth;

    private EditText emailEditText, passwordEditText, rePasswordEditText, nameEditText ;
    private ProgressDialog progressDialog;
    private Button loginSingUpButton;
    private TextView toggleLoginSingUPTextView, recoverPassword;

    private boolean loginModeActive;

    private VideoView Video;

    private Sing_In_Presenter presenter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sign_in_activity);

        getSupportActionBar().hide();

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        //todo: Fullscreen mode
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);

        presenter = new Sing_In_Presenter(this);

        initialization ();

        videoBack();

        initLoginBtn();

        checkHasUserBeenSigned();

        onCLickRecoverPassword();
    }

    //TODO: main
    public void initialization (){
        progressDialog = new ProgressDialog(this);

        auth = FirebaseAuth.getInstance();

        Video=findViewById(R.id.videoLogIn);

        emailEditText = findViewById(R.id.emailEditText);
        passwordEditText = findViewById(R.id.passwordEditText);
        rePasswordEditText = findViewById(R.id.repeatpasswordEditText);

        nameEditText = findViewById(R.id.nameEditText);
        toggleLoginSingUPTextView = findViewById(R.id.toggleLoginSingUPTextView);
        recoverPassword  = findViewById(R.id.recoverForgottenPassword);
        loginSingUpButton = findViewById(R.id.loginSingUpButton);

        rePasswordEditText.setVisibility(View.GONE);
        nameEditText.setVisibility(View.GONE);
    }

    public void videoBack(){
        try {
            Video.setVideoPath("android.resource://"+getPackageName()+"/"+R.raw.loginthis);
            Video.start();
            Video.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    Video.start();
                }
            });
        }catch (Exception ignored){}
    }

    private void initLoginBtn() {
        loginSingUpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loginSingUpUser(emailEditText.getText().toString().trim(),passwordEditText.getText().toString().trim());
            }
        });
    }

    private void checkHasUserBeenSigned() {
        // todo: Проверяем залогиненый ли пользователь
        if (auth.getCurrentUser() != null){      // !=null  -- пользователь залогинен
            startActivity(new Intent(Sign_In_Activity.this, Dashboard_Activity.class));
        }
    }

    //TODO: login action

    private void loginSingUpUser(String email, String password){

        if(loginModeActive){
            //TODO: registration
            if(!passwordEditText.getText().toString().trim().equals(rePasswordEditText.getText().toString().trim())) {  // Проверяем совпадает 1 и 2 пароль при регистрации
                Toast.makeText(this, "Passwords don't match", Toast.LENGTH_LONG).show();
            }else if (emailEditText.getText().toString().trim().equals("")) {
                Toast.makeText(this, "Input mail", Toast.LENGTH_LONG).show();
            } else if (passwordEditText.getText().toString().trim().length() < 7) {
                Toast.makeText(this, "Min 7 symbols in password ", Toast.LENGTH_LONG).show();
            } else if (nameEditText.getText().toString().trim().isEmpty()) {
                Toast.makeText(this, "Give me your Name ", Toast.LENGTH_LONG).show();
            } else {

                progressDialog.setMessage("Registering...");
                progressDialog.show();

                String name = nameEditText.getText().toString().trim();

                presenter.registerUser(auth, email, password, TAG, name, this);
            }

        } else {
            if (emailEditText.getText().toString().trim().equals("")) {          // Проверка что бы мэил не был пустым
                Toast.makeText(this, "Input mail", Toast.LENGTH_LONG).show();
            } else if (passwordEditText.getText().toString().trim().length() < 7) {
                Toast.makeText(this, "Min 7 symbols in password ", Toast.LENGTH_LONG).show();     // т к в Firebase минимум 6 символов
            } else {
                //TODO: login
                progressDialog.setMessage("Login...");
                progressDialog.show();

                presenter.loginUser(auth, email, password, TAG, this);
            }
        }
    }

    @Override
    public void registerComplete(boolean isSuccess) {
        progressDialog.dismiss();
        if(isSuccess){
            startActivity(new Intent(Sign_In_Activity.this, Dashboard_Activity.class) );
        }
        else{
            Toast.makeText(Sign_In_Activity.this, "Authentication failed.", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void loginComplete(boolean isSuccess) {
        progressDialog.dismiss();
        if(isSuccess){
            Intent intent = new Intent(Sign_In_Activity.this, Dashboard_Activity.class);
            startActivity(intent);
        }
        else{
            Toast.makeText(Sign_In_Activity.this, "Authentication failed.", Toast.LENGTH_SHORT).show();
        }
    }

    public void toggleLoginMode(View view) {

        if(loginModeActive) {         // todo: login action
            loginModeActive = false;
            rePasswordEditText.setVisibility(View.GONE);
            nameEditText.setVisibility(View.GONE);
            loginSingUpButton.setText(R.string.logIn);
            toggleLoginSingUPTextView.setText(R.string.SingUp);
            recoverPassword.setVisibility(View.VISIBLE);
        } else {    // todo: register action
            loginModeActive = true;
            rePasswordEditText.setVisibility(View.VISIBLE);
            nameEditText.setVisibility(View.VISIBLE);
            loginSingUpButton.setText(R.string.SingUp);
            toggleLoginSingUPTextView.setText(R.string.logIn);
            recoverPassword.setVisibility(View.GONE);
        }

    }

    //TODO: recover password
    private void onCLickRecoverPassword(){
        recoverPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showRecoverPasswordDialog();
            }
        });
    }

    private void showRecoverPasswordDialog() {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Recover Password");

        LinearLayout linearLayout = new LinearLayout(this);
        EditText emailEt = new EditText(this);
        emailEt.setHint("    Your Email                                                     ");
        emailEt.setInputType(InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);

        linearLayout.addView(emailEt);
        linearLayout.setPadding(10,10,10,10);

        builder.setView(linearLayout);

        builder.setPositiveButton("Recover", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
               String email = emailEt.getText().toString().trim();
               startRecovery(email);
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

    private void startRecovery(String email) {
        progressDialog.setMessage("Sending email");
        progressDialog.show();

        presenter.recoverPassword(auth, email);
    }

    @Override
    public void recoverPasswordComplete(boolean isSuccess) {
        progressDialog.dismiss();
        if(isSuccess){
            Toast.makeText(Sign_In_Activity.this, "Email sent", Toast.LENGTH_SHORT).show();
        }
        else{
            Toast.makeText(Sign_In_Activity.this, "Failed", Toast.LENGTH_SHORT).show();
        }
    }

    //TODO: additional
    @Override
    public void onBackPressed() {
        //dismiss
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        videoBack();
    }

}