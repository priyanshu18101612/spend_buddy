package com.priyanshu.spend_buddy;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.widget.*;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.*;

public class RecurringActivity extends AppCompatActivity {

    EditText etAmount;
    TextView tvDate;
    Button btnSave;
    RecyclerView recyclerView;

    FirebaseFirestore db;
    String userId;

    int selectedDay = -1;
    ChipGroup chipGroup;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recurring);

        etAmount = findViewById(R.id.etAmount);
        tvDate = findViewById(R.id.tvDate);
        btnSave = findViewById(R.id.btnSave);
        recyclerView = findViewById(R.id.recurringList);
        chipGroup = findViewById(R.id.chipGroupCategory);

        db = FirebaseFirestore.getInstance();

        // 🔥 USER CHECK
        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        } else {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // 📅 DATE PICKER
        tvDate.setOnClickListener(v -> {
            Calendar calendar = Calendar.getInstance();

            DatePickerDialog dialog = new DatePickerDialog(this,
                    (view, year, month, dayOfMonth) -> {
                        selectedDay = dayOfMonth;
                        tvDate.setText("Date: " + dayOfMonth + "/" + (month + 1));
                    },
                    calendar.get(Calendar.YEAR),
                    calendar.get(Calendar.MONTH),
                    calendar.get(Calendar.DAY_OF_MONTH));

            dialog.show();
        });

        // 💾 SAVE
        btnSave.setOnClickListener(v -> {

            v.animate().scaleX(0.9f).scaleY(0.9f).setDuration(100)
                    .withEndAction(() ->
                            v.animate().scaleX(1f).scaleY(1f).setDuration(100)
                    );

            saveRecurring();
        });

        loadRecurring();
    }

    // 🔥 SAVE
    private void saveRecurring() {

        String amountStr = etAmount.getText().toString().trim();

        if (amountStr.isEmpty()) {
            Toast.makeText(this, "Enter amount 💸", Toast.LENGTH_SHORT).show();
            return;
        }

        if (selectedDay == -1) {
            Toast.makeText(this, "Select date 📅", Toast.LENGTH_SHORT).show();
            return;
        }

        long amount;
        try {
            amount = Long.parseLong(amountStr);
        } catch (Exception e) {
            Toast.makeText(this, "Invalid amount", Toast.LENGTH_SHORT).show();
            return;
        }

        // 🔥 CATEGORY
        String category = "Other";
        int selectedId = chipGroup.getCheckedChipId();

        if (selectedId != -1) {
            Chip chip = findViewById(selectedId);
            category = chip.getText().toString();
        }

        Map<String, Object> data = new HashMap<>();
        data.put("amount", amount);
        data.put("day", selectedDay);
        data.put("category", category);
        data.put("timestamp", System.currentTimeMillis());

        // 🔥 USER-WISE SAVE
        db.collection("users")
                .document(userId)
                .collection("recurring")
                .add(data)
                .addOnSuccessListener(docRef -> {

                    // 🔥 SAVE ID (important for delete later)
                    docRef.update("id", docRef.getId());

                    Toast.makeText(this, "Saved 🔁", Toast.LENGTH_SHORT).show();

                    etAmount.setText("");
                    tvDate.setText("Select Date 📅");
                    chipGroup.clearCheck();
                    selectedDay = -1;

                    loadRecurring();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Error ❌", Toast.LENGTH_SHORT).show()
                );
    }

    // 🔄 LOAD
    private void loadRecurring() {

        db.collection("users")
                .document(userId)
                .collection("recurring")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {

                    List<RecurringModel> list = new ArrayList<>();

                    for (var doc : queryDocumentSnapshots) {

                        String id = doc.getId();

                        Long amountObj = doc.getLong("amount");
                        Long dayObj = doc.getLong("day");

                        long amount = (amountObj != null) ? amountObj : 0;
                        long day = (dayObj != null) ? dayObj : 0;

                        String category = doc.getString("category");

                        list.add(new RecurringModel(id, amount, day, category));
                    }

                    recyclerView.setAdapter(new RecurringAdapter(list));
                });
    }
}