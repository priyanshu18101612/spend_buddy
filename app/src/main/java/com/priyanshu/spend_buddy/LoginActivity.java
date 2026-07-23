package com.priyanshu.spend_buddy;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.animation.OvershootInterpolator;
import android.view.inputmethod.InputMethodManager;
import android.widget.*;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.card.MaterialCardView;
import com.google.firebase.auth.FirebaseAuth;

public class LoginActivity extends AppCompatActivity {

    EditText etEmail, etPassword;
    Button btnLogin;
    TextView tvSignup;

    FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        getWindow().setStatusBarColor(Color.parseColor("#0F172A"));

        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);


        MaterialCardView card = findViewById(R.id.loginCard);

        card.setTranslationY(120);
        card.setAlpha(0f);

        card.animate()
                .translationY(0)
                .alpha(1f)
                .setDuration(800)
                .setStartDelay(200)
                .start();

//        Button btn = findViewById(R.id.btnLogin);
//
//        btn.setScaleX(0.9f);
//        btn.setScaleY(0.9f);
//
//        btn.animate()
//                .scaleX(1f)
//                .scaleY(1f)
//                .setDuration(600)
//                .setStartDelay(400)
//                .start();

        ImageView logo = findViewById(R.id.imgLogo);

        logo.setScaleX(0.7f);
        logo.setScaleY(0.7f);
        logo.setAlpha(0f);

        logo.animate()
                .scaleX(1f)
                .scaleY(1f)
                .alpha(1f)
                .setInterpolator(new OvershootInterpolator())
                .setDuration(900)
                .start();

        btnLogin.setScaleX(0.95f);
        btnLogin.setScaleY(0.95f);

        btnLogin.animate()
                .scaleX(1f)
                .scaleY(1f)
                .setDuration(700)
                .start();

        tvSignup = findViewById(R.id.tvSignup);

        auth = FirebaseAuth.getInstance();

        btnLogin.setOnClickListener(v -> {

            InputMethodManager imm =
                    (InputMethodManager)getSystemService(INPUT_METHOD_SERVICE);

            imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(),0);

            String email = etEmail.getText().toString().trim();
            String password = etPassword.getText().toString().trim();

            if(email.isEmpty() || password.isEmpty()){
                Toast.makeText(this, "Enter all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            if (password.isEmpty()) {
                etPassword.setError("Enter Password");
                btnLogin.setEnabled(true);
                btnLogin.setText("Login");
                return;
            }

            btnLogin.setEnabled(false);
            btnLogin.setText("Signing In...");


            auth.signInWithEmailAndPassword(email, password)
                    .addOnSuccessListener(authResult -> {
                        Toast.makeText(this, "Login Successful ✅", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(this, MainActivity.class));
                        finish();
                    })
                    .addOnFailureListener(e -> {
                        btnLogin.setEnabled(true);
                        btnLogin.setText("Login");

                        Toast.makeText(this, "Login Failed ❌", Toast.LENGTH_SHORT).show();
                    } );
        });

        tvSignup.setOnClickListener(v ->
                startActivity(new Intent(this, SignupActivity.class))
        );
    }
}