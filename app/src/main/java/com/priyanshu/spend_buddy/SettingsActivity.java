package com.priyanshu.spend_buddy;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import android.app.AlertDialog;
import android.content.Intent;
import android.net.Uri;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.content.SharedPreferences;
import android.widget.Toast;
import android.graphics.Color;
import java.util.Calendar;
import com.google.firebase.firestore.DocumentSnapshot;

import com.google.android.material.card.MaterialCardView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class SettingsActivity extends AppCompatActivity {

    ImageView backBtn;
    TextView tvName, tvRole;
    LinearLayout layoutBudget;
    LinearLayout layoutNotification;
    LinearLayout layoutDarkMode;
    ImageView profileImage;
    SharedPreferences prefs;
    FirebaseFirestore db;
    String userId;


    LinearLayout layoutExport;
    LinearLayout layoutClearData;
    LinearLayout layoutPrivacy;
    LinearLayout layoutTerms;

    LinearLayout layoutContact;
    LinearLayout layoutGithub;
    LinearLayout layoutLinkedin;
    TextView tvLeaves;
    TextView tvLevel;;
    LinearLayout layoutAbout;
    MaterialCardView profileCard;
    Button btnLogout;
    Switch switchNotification;
    TextView tvBudget;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_settings);

        getWindow().setStatusBarColor(android.graphics.Color.parseColor("#0F172A"));
        backBtn = findViewById(R.id.backBtn);

        layoutBudget = findViewById(R.id.layoutBudget);
        layoutNotification = findViewById(R.id.layoutNotification);
        layoutDarkMode = findViewById(R.id.layoutDarkMode);

        layoutExport = findViewById(R.id.layoutExport);
        layoutClearData = findViewById(R.id.layoutClearData);
        layoutPrivacy = findViewById(R.id.layoutPrivacy);
        layoutTerms = findViewById(R.id.layoutTerms);
        tvLeaves = findViewById(R.id.tvLeaves);
        tvLevel = findViewById(R.id.tvLevel);
        layoutContact = findViewById(R.id.layoutContact);
        layoutGithub = findViewById(R.id.layoutGithub);
        layoutLinkedin = findViewById(R.id.layoutLinkedin);
        layoutAbout = findViewById(R.id.layoutAbout);
        db = FirebaseFirestore.getInstance();

        userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        btnLogout = findViewById(R.id.btnLogout);

        switchNotification = findViewById(R.id.switchNotification);
        prefs = getSharedPreferences("SpendBuddySettings", MODE_PRIVATE);
        switchNotification.setChecked(
                prefs.getBoolean("notification", true)
        );

        profileImage = findViewById(R.id.profileImage);

        ImageView profile = findViewById(R.id.profileImage);

        profile.setScaleX(0.8f);
        profile.setScaleY(0.8f);
        profile.setAlpha(0f);

        profile.animate()
                .scaleX(1f)
                .scaleY(1f)
                .alpha(1f)
                .setDuration(900)
                .start();

        //hero card animation
        profileCard = findViewById(R.id.profileCard);

        profileCard.setTranslationY(80);

        profileCard.animate()
                .translationY(0)
                .setDuration(700)
                .start();

        tvBudget = findViewById(R.id.tvBudget);
        loadDailyBudget();
         tvName = findViewById(R.id.tvName);
         tvRole = findViewById(R.id.tvRole);

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        if (user != null) {

            String email = user.getEmail();

            if (user.getDisplayName() != null &&
                    !user.getDisplayName().isEmpty()) {

                tvName.setText(user.getDisplayName());

            } else if (email != null) {

                String name = email.substring(0, email.indexOf("@"));

                name = name.replace(".", " ");
                name = name.replace("_", " ");

                // First letter capital
                name = Character.toUpperCase(name.charAt(0))
                        + name.substring(1);

                tvName.setText(name);

            }

            tvRole.setText("Smart Saver 🌱");

        }

        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        FirebaseFirestore.getInstance()
                .collection("users")
                .document(uid)
                .get()
                .addOnSuccessListener(document -> {

                    if (document.exists()) {

                        Long leaves = document.getLong("leaves");
                        Long level = document.getLong("level");

                        if (leaves == null) leaves = 0L;
                        if (level == null) level = 1L;

                        tvLeaves.setText(String.valueOf(leaves));
                        tvLevel.setText("Level " + level);

                    }

                });

        backBtn.setOnClickListener(v -> {

            finish();

            overridePendingTransition(
                    android.R.anim.fade_in,
                    android.R.anim.fade_out);

        });

        layoutBudget.setOnClickListener(v -> {

            Toast.makeText(this,
                    "Budget settings coming soon 🚀",
                    Toast.LENGTH_SHORT).show();

        });

        switchNotification.setOnCheckedChangeListener((buttonView, isChecked) -> {

            prefs.edit()
                    .putBoolean("notification", isChecked)
                    .apply();

            if (isChecked) {

                Toast.makeText(this,
                        "🔔 Notifications Enabled",
                        Toast.LENGTH_SHORT).show();

            } else {

                Toast.makeText(this,
                        "🔕 Notifications Disabled",
                        Toast.LENGTH_SHORT).show();

            }

        });


        layoutDarkMode.setOnClickListener(v ->

                Toast.makeText(this,
                        "Dark mode coming soon 🌙",
                        Toast.LENGTH_SHORT).show()

        );

        layoutExport.setOnClickListener(v ->

                Toast.makeText(this,
                        "Export feature coming soon",
                        Toast.LENGTH_SHORT).show()

        );

        layoutClearData.setOnClickListener(v -> {

            new AlertDialog.Builder(this)

                    .setTitle("Clear All Data")

                    .setMessage("Are you sure?")

                    .setPositiveButton("Yes",

                            (dialog, which) ->

                                    Toast.makeText(this,
                                            "Feature coming soon",
                                            Toast.LENGTH_SHORT).show())

                    .setNegativeButton("Cancel", null)

                    .show();
        });

        layoutContact.setOnClickListener(v -> {

            Intent emailIntent = new Intent(Intent.ACTION_SENDTO);

            emailIntent.setData(Uri.parse("mailto:"));

            emailIntent.putExtra(Intent.EXTRA_EMAIL,
                    new String[]{"Priyanshu2004mishra@gmail.com"});

            emailIntent.putExtra(Intent.EXTRA_SUBJECT,
                    "SpendBuddy Feedback");

            startActivity(Intent.createChooser(emailIntent,
                    "Contact Developer"));

        });

        layoutGithub.setOnClickListener(v -> {

            Intent browser = new Intent(Intent.ACTION_VIEW);

            browser.setData(Uri.parse(
                    "https://github.com/priyanshu18101612"));

            startActivity(browser);

        });
        layoutLinkedin.setOnClickListener(v -> {

            Intent browser = new Intent(Intent.ACTION_VIEW);

            browser.setData(Uri.parse(
                    "https://linkedin.com/in/priyanshu-ranjan-3b7144318"));

            startActivity(browser);

        });

        layoutAbout.setOnClickListener(v -> {

            new AlertDialog.Builder(this)

                    .setTitle("About SpendBuddy")

                    .setMessage(

                            "SpendBuddy v1.0\n\n"

                                    + "Built with Java, Firebase and Material Design.\n\n"

                                    + "Helping students build better financial habits.\n\n"

                                    + "Made with ❤️ by Priyanshu Ranjan"

                    )

                    .setPositiveButton("OK", null)

                    .show();

        });

        layoutPrivacy.setOnClickListener(v -> {

            new AlertDialog.Builder(this)

                    .setTitle("Privacy Policy")

                    .setMessage("Your expense data is securely stored in your Firebase account and is only accessible by you.")

                    .setPositiveButton("OK", null)

                    .show();

        });

        layoutTerms.setOnClickListener(v -> {

            new AlertDialog.Builder(this)

                    .setTitle("Terms & Conditions")

                    .setMessage("SpendBuddy is intended for personal expense tracking and educational purposes.")

                    .setPositiveButton("OK", null)

                    .show();

        });

        btnLogout.setOnClickListener(v -> {

            new AlertDialog.Builder(SettingsActivity.this)

                    .setTitle("Logout")
                    .setMessage("Are you sure you want to logout from SpendBuddy?")
                    .setIcon(android.R.drawable.ic_dialog_alert)

                    .setPositiveButton("Logout", (dialog, which) -> {

                        FirebaseAuth.getInstance().signOut();

                        Intent intent = new Intent(SettingsActivity.this,
                                LoginActivity.class);

                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK |
                                Intent.FLAG_ACTIVITY_CLEAR_TASK);

                        startActivity(intent);

                        finish();

                    })

                    .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())

                    .show();

        });

        profileImage.setOnLongClickListener(v -> {

            new AlertDialog.Builder(SettingsActivity.this)

                    .setTitle("🎉 Easter Egg")

                    .setMessage(
                            "Thanks for exploring SpendBuddy!\n\n" +
                                    "Keep Saving • Keep Growing 🌱")

                    .setPositiveButton("Awesome!", null)

                    .show();

            return true;
        });

    }

    private void loadDailyBudget() {

        db.collection("users")
                .document(userId)
                .collection("budget")
                .document("monthly")
                .get()

                .addOnSuccessListener(budgetDoc -> {

                    if (!budgetDoc.exists()) {

                        tvBudget.setText("₹0/day");
                        return;

                    }

                    Long budgetObj = budgetDoc.getLong("amount");

                    long monthlyBudget = (budgetObj != null) ? budgetObj : 0;

                    db.collection("users")
                            .document(userId)
                            .collection("expenses")
                            .get()

                            .addOnSuccessListener(query -> {

                                long spent = 0;

                                Calendar now = Calendar.getInstance();

                                int currentMonth =
                                        now.get(Calendar.MONTH);

                                int currentYear =
                                        now.get(Calendar.YEAR);

                                for (DocumentSnapshot doc : query) {

                                    Long amount =
                                            doc.getLong("amount");

                                    Long date =
                                            doc.getLong("date");

                                    if (amount == null || date == null)
                                        continue;

                                    Calendar expenseDate =
                                            Calendar.getInstance();

                                    expenseDate.setTimeInMillis(date);

                                    if (expenseDate.get(Calendar.MONTH)
                                            == currentMonth &&
                                            expenseDate.get(Calendar.YEAR)
                                                    == currentYear) {

                                        spent += amount;

                                    }

                                }

                                long remainingBudget =
                                        monthlyBudget - spent;

                                if (remainingBudget < 0)
                                    remainingBudget = 0;

                                int today =
                                        now.get(Calendar.DAY_OF_MONTH);

                                int maxDays =
                                        now.getActualMaximum(Calendar.DAY_OF_MONTH);

                                int remainingDays =
                                        maxDays - today + 1;

                                if (remainingDays <= 0)
                                    remainingDays = 1;

                                long daily =
                                        remainingBudget / remainingDays;

                                tvBudget.setText("₹" + daily + "/day");

                            });

                });

    }
}