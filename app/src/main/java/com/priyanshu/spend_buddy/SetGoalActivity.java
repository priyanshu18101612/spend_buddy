package com.priyanshu.spend_buddy;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import com.google.firebase.auth.FirebaseAuth;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;


public class SetGoalActivity extends AppCompatActivity {

    EditText etGoalTitle, etTargetAmount, etDays;
    Button btnSaveGoal;
    FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set_goal);

        etGoalTitle = findViewById(R.id.etGoalTitle);
        etTargetAmount = findViewById(R.id.etTargetAmount);
        etDays = findViewById(R.id.etDays);
        btnSaveGoal = findViewById(R.id.btnSaveGoal);

        db = FirebaseFirestore.getInstance();

        btnSaveGoal.setOnClickListener(v -> saveGoal());
    }

    private void saveGoal() {

        String title = etGoalTitle.getText().toString().trim();
        String targetStr = etTargetAmount.getText().toString().trim();
        String daysStr = etDays.getText().toString().trim();

        if (title.isEmpty()) {
            etGoalTitle.setError("Enter goal name");
            return;
        }

        if (targetStr.isEmpty()) {
            etTargetAmount.setError("Enter amount");
            return;
        }

        if (daysStr.isEmpty()) {
            etDays.setError("Enter days");
            return;
        }

        long target = Long.parseLong(targetStr);
        int days = Integer.parseInt(daysStr);

        if (target <= 0 || days <= 0) {
            Toast.makeText(this, "Invalid input", Toast.LENGTH_SHORT).show();
            return;
        }

        long goalStartDate = System.currentTimeMillis();
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        Map<String, Object> goal = new HashMap<>();
        goal.put("title", title);
        goal.put("targetAmount", target);
        goal.put("savedAmount", 0);
        goal.put("goalDays", days);
        goal.put("streak", 0);
        goal.put("lastSavedDate", 0);
        goal.put("goalCompleted", false);
        goal.put("goalStartDate", goalStartDate);
        long dailyTarget = target / days;
        goal.put("dailyTarget", dailyTarget);

        db.collection("users")
                .document(userId)
                .collection("saving_goal")
                .document("current")
                .set(goal)
                .addOnSuccessListener(unused -> {
                    Toast.makeText(this, "Goal Set 🎯", Toast.LENGTH_SHORT).show();
                    finish();
                });
    }
}