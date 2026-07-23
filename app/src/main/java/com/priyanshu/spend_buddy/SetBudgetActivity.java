package com.priyanshu.spend_buddy;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.*;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class SetBudgetActivity extends AppCompatActivity {

    EditText etBudget;
    Button btnSave;
    FirebaseFirestore db;

    String userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set_budget);

        etBudget = findViewById(R.id.etBudget);
        btnSave = findViewById(R.id.btnSaveBudget);

        db = FirebaseFirestore.getInstance();

        // 🔥 GET USER ID
        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        } else {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        btnSave.setOnClickListener(v -> {

            String value = etBudget.getText().toString().trim();

            if (TextUtils.isEmpty(value)) {
                etBudget.setError("Enter budget");
                return;
            }

            long budget;

            try {
                budget = Long.parseLong(value);
            } catch (Exception e) {
                etBudget.setError("Invalid number");
                return;
            }

            // 🔥 BUTTON ANIMATION
            v.animate().scaleX(0.9f).scaleY(0.9f).setDuration(100).withEndAction(() ->
                    v.animate().scaleX(1f).scaleY(1f).setDuration(100)
            );

            Map<String, Object> data = new HashMap<>();
            data.put("amount", budget);

            // 🔥 USER-WISE SAVE (IMPORTANT FIX)
            db.collection("users")
                    .document(userId)
                    .collection("budget")
                    .document("monthly")
                    .set(data)
                    .addOnSuccessListener(unused -> {
                        Toast.makeText(this, "Budget Saved 💸", Toast.LENGTH_SHORT).show();
                        finish();
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, "Error saving ❌", Toast.LENGTH_SHORT).show();
                    });
        });
    }
}