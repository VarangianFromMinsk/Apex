package com.example.myapplication.main.Screens.Sing_In_MVP;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelStoreOwner;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.Application;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.example.myapplication.databinding.SignInActivityBinding;
import com.example.myapplication.main.Screens.Dashboard_MVP.Dashboard_Activity;
import com.example.myapplication.R;
import com.example.myapplication.main.Screens.Posts.Posts_By_Recommendation_MVVM.Post_Recomm_ViewModel;
import com.google.firebase.auth.FirebaseAuth;

import java.util.Objects;

import dagger.Provides;

public class Sign_In_Activity extends AppCompatActivity implements Sing_In_view {

    private static final String TAG = "Sign_In_Activity";
    private FirebaseAuth auth;

    private ProgressDialog progressDialog;

    private boolean loginModeActive;

    private SignInActivityBinding binding;
    private Sing_In_Presenter presenter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sign_in_activity);

        Objects.requireNonNull(getSupportActionBar()).hide();

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        //todo: Fullscreen mode
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);

        presenter = new Sing_In_Presenter(this);

        binding = DataBindingUtil.setContentView(this, R.layout.sign_in_activity);

        initialization();

        videoBackground();

        initLoginBtn();

        checkHasUserBeenSigned();

        onCLickRecoverPassword();

        toggleLoginMode();
    }

    //TODO: main
    public void initialization() {
        progressDialog = new ProgressDialog(this);
        auth = FirebaseAuth.getInstance();
    }

    public void  videoBackground() {
        try {
            binding.videoLogIn.setVideoPath("android.resource://" + getPackageName() + "/" + R.raw.loginthis);
            binding.videoLogIn.start();
            binding.videoLogIn.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    binding.videoLogIn.start();
                }
            });
        } catch (Exception ignored) {}
    }

    private void initLoginBtn() {
        binding.loginSingUpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loginSingUpUser(Objects.requireNonNull(binding.emailEditText.getText()).toString().trim(), binding.passwordEditText.getText().toString().trim());
            }
        });
    }

    private void checkHasUserBeenSigned() {
        // todo: Проверяем залогиненый ли пользователь
        if (auth.getCurrentUser() != null) {
            Intent intent = new Intent(Sign_In_Activity.this, Dashboard_Activity.class);
            overridePendingTransition(0, 0);
            startActivity(intent);
        }
    }

    //TODO: login action

    private void loginSingUpUser(String email, String password) {

        if (loginModeActive) {
            //TODO: registration
            if (!binding.passwordEditText.getText().toString().trim().equals(binding.repeatpasswordEditText.getText().toString().trim())) {  // Проверяем совпадает 1 и 2 пароль при регистрации
                Toast.makeText(this, "Passwords don't match", Toast.LENGTH_LONG).show();
            } else if (Objects.requireNonNull(binding.emailEditText.getText()).toString().trim().equals("")) {
                Toast.makeText(this, "Input mail", Toast.LENGTH_LONG).show();
            } else if (binding.passwordEditText.getText().toString().trim().length() < 7) {
                Toast.makeText(this, "Min 7 symbols in password ", Toast.LENGTH_LONG).show();
            } else if (binding.nameEditText.getText().toString().trim().isEmpty()) {
                Toast.makeText(this, "Give me your Name ", Toast.LENGTH_LONG).show();
            } else {

                progressDialog.setMessage("Registering...");
                progressDialog.show();

                String name = binding.nameEditText.getText().toString().trim();

                presenter.registerUser(auth, email, password, TAG, name, this);
            }

        } else {
            if (Objects.requireNonNull(binding.emailEditText.getText()).toString().trim().equals("")) {          // Проверка что бы мэил не был пустым
                Toast.makeText(this, "Input mail", Toast.LENGTH_LONG).show();
            } else if (binding.passwordEditText.getText().toString().trim().length() < 7) {
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
        if (isSuccess) {
            Intent intent = new Intent(Sign_In_Activity.this, Dashboard_Activity.class);
            finish();
            overridePendingTransition(0, 0);
            startActivity(intent);
        } else {
            Toast.makeText(Sign_In_Activity.this, "Authentication failed.", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void loginComplete(boolean isSuccess) {
        progressDialog.dismiss();
        if (isSuccess) {
            Intent intent = new Intent(Sign_In_Activity.this, Dashboard_Activity.class);
            finish();
            overridePendingTransition(0, 0);
            startActivity(intent);
        } else {
            Toast.makeText(Sign_In_Activity.this, "Authentication failed.", Toast.LENGTH_SHORT).show();
        }
    }

    public void toggleLoginMode() {

        binding.toggleLoginSingUPTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (loginModeActive) {
                    // todo: login action
                    loginModeActive = false;
                    binding.recoverForgottenPassword.setVisibility(View.VISIBLE);
                    binding.repeatpasswordEditText.setVisibility(View.GONE);
                    binding.nameEditText.setVisibility(View.GONE);
                    binding.loginSingUpButton.setText(R.string.logIn);
                    binding.toggleLoginSingUPTextView.setText(R.string.new_account);

                    // todo: animation
                    binding.recoverForgottenPassword.animate().scaleX(1.07f).scaleY(1.04f).setDuration(200).setListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            super.onAnimationEnd(animation);
                            binding.recoverForgottenPassword.animate().scaleX(1).scaleY(1).setDuration(200);
                        }
                    });

                } else {
                    // todo: register action
                    loginModeActive = true;
                    binding.repeatpasswordEditText.setVisibility(View.VISIBLE);
                    binding.nameEditText.setVisibility(View.VISIBLE);
                    binding.loginSingUpButton.setText(R.string.register);
                    binding.toggleLoginSingUPTextView.setText(R.string.Or_Log_in);
                    binding.recoverForgottenPassword.setVisibility(View.GONE);

                    // todo: animation
                    binding.repeatpasswordEditText.animate().scaleX(1.07f).scaleY(1.04f).setDuration(200).setListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            super.onAnimationEnd(animation);
                            binding.repeatpasswordEditText.animate().scaleX(1).scaleY(1).setDuration(200);
                        }
                    });
                    binding.nameEditText.animate().scaleX(1.07f).scaleY(1.04f).setDuration(200).setListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            super.onAnimationEnd(animation);
                            binding.nameEditText.animate().scaleX(1).scaleY(1).setDuration(200);
                        }
                    });
                }
            }
        });

    }

    //TODO: recover password
    private void onCLickRecoverPassword() {
        binding.recoverForgottenPassword.setOnClickListener(new View.OnClickListener() {
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
        linearLayout.setPadding(10, 10, 10, 10);

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
        if (isSuccess) {
            Toast.makeText(Sign_In_Activity.this, "Email sent", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(Sign_In_Activity.this, "Failed", Toast.LENGTH_SHORT).show();
        }
    }

    //TODO: additional
    @Override
    public void onBackPressed() {
        //dismiss
    }

    @Override
    protected void onResume() {
        super.onResume();
        videoBackground();
    }
}