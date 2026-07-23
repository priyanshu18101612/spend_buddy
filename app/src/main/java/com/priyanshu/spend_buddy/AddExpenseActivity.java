package com.priyanshu.spend_buddy;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.*;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class AddExpenseActivity extends AppCompatActivity {

    EditText etAmount, etNote;
    ChipGroup chipGroup;
    Button btnSave;

    FirebaseFirestore db;
    String userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_expense);

        etAmount = findViewById(R.id.etAmount);
        etNote = findViewById(R.id.etNote);
        chipGroup = findViewById(R.id.chipGroup);
        btnSave = findViewById(R.id.btnSave);

        db = FirebaseFirestore.getInstance();

        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        } else {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        btnSave.setOnClickListener(v -> {

            v.animate()
                    .scaleX(0.95f)
                    .scaleY(0.95f)
                    .setDuration(80)
                    .withEndAction(() ->
                            v.animate()
                                    .scaleX(1f)
                                    .scaleY(1f)
                                    .setDuration(120)
                    );

            saveExpense();
        });
    }

    private void saveExpense() {

        String amountStr = etAmount.getText().toString().trim();
        String note = etNote.getText().toString().trim();

        if (TextUtils.isEmpty(amountStr)) {
            etAmount.setError("Enter amount 💸");
            return;
        }

        long amount;
        try {
            amount = Long.parseLong(amountStr);
        } catch (Exception e) {
            etAmount.setError("Invalid amount");
            return;
        }

        if (amount <= 0) {
            etAmount.setError("Amount must be > 0");
            return;
        }

        String category = "Other";
        int checkedId = chipGroup.getCheckedChipId();

        if (checkedId != View.NO_ID) {
            Chip chip = chipGroup.findViewById(checkedId);

            if (chip != null) {
                category = chip.getText().toString();

                if (category.contains(" ")) {
                    category = category.substring(category.indexOf(" ") + 1);
                }
            }
        }

        long date = System.currentTimeMillis();

        Map<String, Object> expense = new HashMap<>();
        expense.put("amount", amount);
        expense.put("category", category);
        expense.put("title", note.isEmpty() ? category : note);
        expense.put("date", date);

        btnSave.setEnabled(false);

        db.collection("users")
                .document(userId)
                .collection("expenses")
                .add(expense)
                .addOnSuccessListener(docRef -> {

                    // 🔥 SAVE DOCUMENT ID (IMPORTANT FOR DELETE)
                    docRef.update("id", docRef.getId());

                    Toast.makeText(this, "Saved 💰", Toast.LENGTH_SHORT).show();

                    // 🔥 RESET UI (better UX)
                    etAmount.setText("");
                    etNote.setText("");
                    chipGroup.clearCheck();

                    finish();
                })
                .addOnFailureListener(e -> {
                    btnSave.setEnabled(true);
                    Toast.makeText(this, "Error saving ❌", Toast.LENGTH_SHORT).show();
                });
    }
}