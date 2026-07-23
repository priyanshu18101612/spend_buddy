package com.priyanshu.spend_buddy;

import android.content.Intent;
import android.os.Bundle;
import android.widget.*;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.HashMap;
import java.util.Map;

public class SignupActivity extends AppCompatActivity {

    EditText etEmail, etPassword, etConfirmPassword;
    Button btnSignup;
    TextView tvLogin;
    FirebaseFirestore db;

    FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);
        btnSignup = findViewById(R.id.btnSignup);
        tvLogin = findViewById(R.id.tvLogin);

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        // 🔥 SIGNUP BUTTON
        btnSignup.setOnClickListener(v -> {

            String email = etEmail.getText().toString().trim();
            String pass = etPassword.getText().toString().trim();
            String confirm = etConfirmPassword.getText().toString().trim();

            // ❗ VALIDATION
            if (email.isEmpty() || pass.isEmpty() || confirm.isEmpty()) {
                Toast.makeText(this, "Fill all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!pass.equals(confirm)) {
                Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show();
                return;
            }

            // 🔥 FIREBASE SIGNUP
            auth.createUserWithEmailAndPassword(email, pass)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {

                            FirebaseUser user = auth.getCurrentUser();

                            if (user != null) {

                                String uid = user.getUid();

                                Map<String, Object> profile = new HashMap<>();

                                profile.put("email", email);

                                // Email se naam nikalna
                                String name = email.substring(0, email.indexOf("@"));
                                name = name.replace(".", " ");
                                name = name.replace("_", " ");

                                profile.put("name", name);

                                profile.put("leaves", 0);

                                profile.put("level", 1);

                                profile.put("joinedOn", System.currentTimeMillis());

                                db.collection("users")
                                        .document(uid)
                                        .set(profile, com.google.firebase.firestore.SetOptions.merge())
                                        .addOnSuccessListener(unused -> {

                                            Toast.makeText(this,
                                                    "Signup Success ✅",
                                                    Toast.LENGTH_SHORT).show();

                                            auth.signOut();

                                            startActivity(new Intent(
                                                    SignupActivity.this,
                                                    LoginActivity.class));

                                            finish();

                                        });

                            }

                        }

                        else {
                            String error = task.getException() != null ?
                                    task.getException().getMessage() : "Signup Failed";

                            Toast.makeText(this, error, Toast.LENGTH_LONG).show();
                        }
                    });
        });

        // 🔥 LOGIN TEXT CLICK
        tvLogin.setOnClickListener(v -> {
            startActivity(new Intent(SignupActivity.this, LoginActivity.class));
            finish();
        });
    }
}
