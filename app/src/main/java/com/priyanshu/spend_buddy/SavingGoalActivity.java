package com.priyanshu.spend_buddy;

import android.animation.ValueAnimator;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.github.lzyzsd.circleprogress.DonutProgress;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import nl.dionsegijn.konfetti.core.PartyFactory;
import nl.dionsegijn.konfetti.core.emitter.Emitter;
import nl.dionsegijn.konfetti.xml.KonfettiView;

public class SavingGoalActivity extends AppCompatActivity {

    TextView goalTitle, goalAmount, streakText, dailyTargetText;
    Button btnAddSaving;
    TextView tvStreakMessage;
    TextView tvChallenge, tvReward, tvChallengeStatus;
    ProgressBar challengeProgress;
    ImageView fireIcon;

    FirebaseFirestore db;
    View fireGlow;
    KonfettiView konfettiView;
    DonutProgress circularProgress;
    LineChart savingChart;
    Button btnSetGoal;

    long savedAmount = 0;
    long targetAmount = 0;
    int streak = 0;
    long daily = 0;
    long lastSavedDate = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_saving_goal);

        goalTitle = findViewById(R.id.goalTitle);
        goalAmount = findViewById(R.id.goalAmount);
        dailyTargetText = findViewById(R.id.daily);
        circularProgress = findViewById(R.id.circularProgress);
        btnAddSaving = findViewById(R.id.btnAddSaving);
        btnSetGoal = findViewById(R.id.btnSetGoal);
        savingChart = findViewById(R.id.savingChart);
        konfettiView = findViewById(R.id.konfettiView);
        tvChallenge = findViewById(R.id.tvChallenge);
        tvReward = findViewById(R.id.tvReward);
        challengeProgress = findViewById(R.id.challengeProgress);
        streakText = findViewById(R.id.streakText);
        tvStreakMessage = findViewById(R.id.tvStreakMessage);
        tvChallengeStatus = findViewById(R.id.tvChallengeStatus);

        btnSetGoal.setOnClickListener(v -> {
            startActivity(new Intent(SavingGoalActivity.this, SetGoalActivity.class));
        });


        fireIcon = findViewById(R.id.fireIcon);
        fireGlow = findViewById(R.id.fireGlow);

        db = FirebaseFirestore.getInstance();

        loadGoal();

        btnAddSaving.setOnClickListener(v -> showAddSavingDialog());
    }

    protected void onResume() {
        super.onResume();
        loadGoal();
    }

    // ================= LOAD DATA ===============
    private void loadGoal() {

        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        db.collection("users")
                .document(userId)
                .collection("saving_goal")
                .document("current")
                .get()
                .addOnSuccessListener(doc -> {

                    if (!doc.exists()) {

                        goalTitle.setText("No Goal Set 😢");
                        goalAmount.setText("₹0 / ₹0");

                        btnSetGoal.setText("🎯 Set Goal");

                        circularProgress.setProgress(0);

                        return;
                    }
                    btnSetGoal.setText("✏ Edit Goal");

                    // ================= DATA =================
                    String title = doc.getString("title");

                    Long target = doc.getLong("targetAmount");
                    Long saved = doc.getLong("savedAmount");
                    Long dailyTarget = doc.getLong("dailyTarget");
                    Long streakVal = doc.getLong("streak");
                    Long lastDate = doc.getLong("lastSavedDate");
                    Boolean completed = doc.getBoolean("goalCompleted");
                    Long totalDaysVal = doc.getLong("goalDays");
                    Long goalStart = doc.getLong("goalStartDate");
                    long goalStartDate = goalStart != null ? goalStart : 0;

                    targetAmount = target != null ? target : 0;
                    savedAmount = saved != null ? saved : 0;
                    daily = dailyTarget != null ? dailyTarget : 0;
                    streak = streakVal != null ? streakVal.intValue() : 0;
                    lastSavedDate = lastDate != null ? lastDate : 0;

                    int totalDays = totalDaysVal != null ? totalDaysVal.intValue() : 1;
                    boolean goalCompleted = completed != null && completed;

                    // ================= UI =================
                    goalTitle.setText(title != null ? title : "My Goal");
                    goalAmount.setText("₹" + savedAmount + " / ₹" + targetAmount);

                    long today = System.currentTimeMillis() / (1000 * 60 * 60 * 24);
                    int daysPassed = (int) (today - lastSavedDate);
                    int remainingDays = totalDays - daysPassed;

                    if (remainingDays <= 0) remainingDays = 1;

                    long remainingAmount = targetAmount - savedAmount;
                    if (remainingAmount < 0) remainingAmount = 0;

                    long dailyRequired = remainingAmount / remainingDays;

                    dailyTargetText.setText("Daily Needed: ₹" + dailyRequired);
                    streakText.setText(streak + " Days");

                    // 🔥 Dynamic Streak Levels

                    if (streak <= 0) {

                        fireIcon.setVisibility(View.VISIBLE);
                        fireIcon.setColorFilter(Color.parseColor("#94A3B8"));
                        fireGlow.setVisibility(View.GONE);

                        streakText.setTextColor(Color.parseColor("#94A3B8"));
                        tvStreakMessage.setText("Start your saving journey today 🚀");

                    } else if (streak <= 3) {

                        fireIcon.setVisibility(View.VISIBLE);
                        fireGlow.setVisibility(View.GONE);

                        fireIcon.setColorFilter(Color.parseColor("#FF9800"));

                        streakText.setTextColor(Color.parseColor("#FF9800"));
                        tvStreakMessage.setText("Great start! Keep going 🔥");

                    } else if (streak <= 7) {

                        fireIcon.setVisibility(View.VISIBLE);
                        fireGlow.setVisibility(View.VISIBLE);

                        fireIcon.setColorFilter(Color.parseColor("#FF7043"));

                        streakText.setTextColor(Color.parseColor("#FFD54F"));
                        tvStreakMessage.setText("Amazing consistency! 💪");

                        startFireAnimation();

                    } else {

                        fireIcon.setVisibility(View.VISIBLE);
                        fireGlow.setVisibility(View.VISIBLE);

                        fireIcon.setColorFilter(Color.parseColor("#FFD700"));

                        streakText.setTextColor(Color.parseColor("#FFD700"));
                        tvStreakMessage.setText("You're a saving master! 🏆");

                        startFireAnimation();

                    }

                    // 📊 PROGRESS
                    int percent = 0;
                    if (targetAmount > 0) {
                        percent = (int) ((savedAmount * 100) / targetAmount);
                    }

                    if (percent < 30) {
                        circularProgress.setFinishedStrokeColor(Color.RED);
                    } else if (percent < 70) {
                        circularProgress.setFinishedStrokeColor(Color.YELLOW);
                    } else {
                        circularProgress.setFinishedStrokeColor(Color.GREEN);
                    }

                    if ((int) circularProgress.getProgress() != percent) {
                        animateProgress(circularProgress, percent);
                    }

                    generateChallenge();

                    // 🎉 GOAL COMPLETE
                    if (!goalCompleted && savedAmount >= targetAmount && targetAmount > 0) {

                        // Reward
                        UserManager.addLeaves(50);

                        // Show Dialog
                        showGoalCompletedDialog();

                        db.collection("users")
                                .document(userId)
                                .collection("saving_goal")
                                .document("current")
                                .update("goalCompleted", true);
                    }

                    // ================= 🔥 REAL GRAPH =================
                    db.collection("users")
                            .document(userId)
                            .collection("saving_history")
                            .get()
                            .addOnSuccessListener(query -> {

                                Map<Integer, Long> dailyMap = new HashMap<>();

                                for (int i = 0; i < 7; i++) {
                                    dailyMap.put(i, 0L);
                                }

                                long now = System.currentTimeMillis();

                                for (var doc2 : query) {

                                    Long amt = doc2.getLong("amount");
                                    Long dt = doc2.getLong("date");

                                    if (amt == null || dt == null) continue;

                                    if (dt < goalStartDate) continue;

                                    long diff = (now - dt) / (1000 * 60 * 60 * 24);

                                    if (diff >= 0 && diff < 7) {
                                        int index = 6 - (int) diff;
                                        dailyMap.put(index, dailyMap.get(index) + amt);
                                    }
                                }

                                ArrayList<Entry> entries = new ArrayList<>();

                                for (int i = 0; i < 7; i++) {
                                    entries.add(new Entry(i, dailyMap.get(i)));
                                }

                                LineDataSet dataSet = new LineDataSet(entries, "");

                                dataSet.setColor(Color.parseColor("#00E676"));
                                dataSet.setCircleColor(Color.WHITE);
                                dataSet.setLineWidth(3f);
                                dataSet.setMode(LineDataSet.Mode.CUBIC_BEZIER);
                                dataSet.setDrawFilled(true);
                                dataSet.setFillAlpha(80);
                                dataSet.setFillColor(Color.parseColor("#00E676"));

                                savingChart.setData(new LineData(dataSet));

                                savingChart.getDescription().setEnabled(false);
                                savingChart.getLegend().setEnabled(false);
                                savingChart.getXAxis().setEnabled(false);
                                savingChart.getAxisLeft().setEnabled(false);
                                savingChart.getAxisRight().setEnabled(false);

                                savingChart.animateX(1000);
                                savingChart.invalidate();
                            });

                })
                .addOnFailureListener(e -> {

                    Log.e("GOAL_ERROR", e.getMessage());

                    Toast.makeText(
                            this,
                            "Unable to load goal.",
                            Toast.LENGTH_SHORT
                    ).show();

                });
    }

    // ================= ADD SAVING =================
    private void showAddSavingDialog() {

        EditText input = new EditText(this);
        input.setHint("Enter amount");
        input.setInputType(InputType.TYPE_CLASS_NUMBER);

        new AlertDialog.Builder(this)
                .setTitle("Add Saving")
                .setView(input)
                .setPositiveButton("Add", (d, w) -> {

                    String val = input.getText().toString().trim();

                    if (val.isEmpty()) {
                        Toast.makeText(this, "Enter amount", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    long amount = Long.parseLong(val);

                    updateSaving(amount); // 🔥 CLEAN CALL

                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    // ================= CORE LOGIC =================
    private void updateSaving(long amount) {

        if (amount <= 0) return;

        btnAddSaving.setEnabled(false);

        btnAddSaving.setText("Saving...");

        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        long today = System.currentTimeMillis() / (1000 * 60 * 60 * 24);

        if (lastSavedDate == today - 1 && amount >= daily) {
            streak++;
        } else if (lastSavedDate == today) {
            // same day
        } else if (amount >= daily) {
            streak = 1;
        } else {
            streak = 0;
        }

        savedAmount += amount;
        lastSavedDate = today;

        db.collection("users")
                .document(userId).collection("saving_goal")
                .document("current").get()
                .addOnSuccessListener(doc -> {

                    Long goalStart = doc.getLong("goalStartDate");
                    long goalStartDate = goalStart != null ? goalStart : 0;

                    // 🔥 SAVE HISTORY (FIXED PATH)
                    Map<String, Object> history = new HashMap<>();
                    history.put("amount", amount);
                    history.put("date", System.currentTimeMillis());
                    history.put("goalStartDate", goalStartDate);

                    //String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

                    db.collection("users")
                            .document(userId)
                            .collection("saving_history")
                            .add(history);

                    // 🔥 UPDATE GOAL
                    Map<String, Object> update = new HashMap<>();
                    update.put("savedAmount", savedAmount);
                    update.put("streak", streak);
                    update.put("lastSavedDate", lastSavedDate);

                    db.collection("users").document(userId)
                            .collection("saving_goal").document("current").update(update)

                            .addOnSuccessListener(unused -> {
                                btnAddSaving.setEnabled(true);

                                btnAddSaving.setText("➕ Saving");
                                Toast.makeText(
                                        this,
                                        "₹" + amount + " added to your goal 🎯",
                                        Toast.LENGTH_SHORT
                                ).show();
                                loadGoal();

                                SharedPreferences prefs =
                                        getSharedPreferences("challenge", MODE_PRIVATE);

                                String todayKey = new SimpleDateFormat(
                                        "yyyy-MM-dd",
                                        Locale.getDefault())
                                        .format(new Date());

                                boolean claimed =
                                        prefs.getBoolean(todayKey, false);

                                if (amount >= 100 && !claimed) {

                                    prefs.edit().putBoolean(todayKey, true).apply();

                                    UserManager.addLeaves(10);

                                    challengeProgress.setProgress(100);

                                    tvChallengeStatus.setText("Completed ✅");

                                    tvReward.setText("🍃 +10 Claimed");

                                    Toast.makeText(this,
                                            "Challenge Completed! 🍃 +10 Leaves",
                                            Toast.LENGTH_LONG).show();

                                }

                            }).addOnFailureListener(e -> {

                                btnAddSaving.setEnabled(true);

                                btnAddSaving.setText("➕ Saving");

                                Toast.makeText(
                                        this,
                                        "Failed to save. Please try again.",
                                        Toast.LENGTH_SHORT
                                ).show();

                            });
                });
    }

    // ================= GOAL COMPLETE =================
    private void showGoalCompletedDialog() {


        View view = getLayoutInflater().inflate(R.layout.dialog_goal_complete, null);

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setView(view)
                .create();

        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        ImageView imgTree = view.findViewById(R.id.imgTree);
        ImageView imgTrophy = view.findViewById(R.id.imgTrophy);

        TextView tvReward = view.findViewById(R.id.tvReward);
        TextView tvGoalMessage = view.findViewById(R.id.tvGoalMessage);

        Button btnContinue = view.findViewById(R.id.btnContinue);

        imgTree.setScaleX(0f);
        imgTree.setScaleY(0f);

        imgTree.animate()
                .scaleX(1f)
                .scaleY(1f)
                .setDuration(700)
                .start();

        imgTrophy.setScaleX(0f);
        imgTrophy.setScaleY(0f);
        imgTrophy.animate()
                .scaleX(1f)
                .scaleY(1f)
                .rotationBy(360)
                .setDuration(900)
                .start();
        tvReward.setAlpha(0f);
        tvGoalMessage.setText(
                "Amazing! Your saving discipline paid off.\nKeep going for your next goal.");


        imgTrophy.animate()
                .translationYBy(-20)
                .setDuration(300)
                .withEndAction(() ->
                        imgTrophy.animate()
                                .translationY(0)
                                .setDuration(300)
                                .start())
                .start();
        tvReward.animate()
                .alpha(1f)
                .setStartDelay(500)
                .setDuration(600)
                .start();

        ValueAnimator animator = ValueAnimator.ofInt(0, 50);

        animator.setDuration(1800);

        animator.addUpdateListener(animation -> {

            int value = (int) animation.getAnimatedValue();

            tvReward.setText("🍃 +" + value + " Leaves");

        });

        animator.start();

        // 🎉 Confetti
        //KonfettiView konfettiView = view.findViewById(R.id.konfettiView);

        konfettiView.start(
                new PartyFactory(new Emitter(2, TimeUnit.SECONDS).perSecond(30))
                        .angle(0)
                        .spread(360)
                        .setSpeedBetween(2f, 10f)
                        .timeToLive(2000L)
                        .colors(Arrays.asList(
                                Color.YELLOW,
                                Color.GREEN,
                                Color.CYAN,
                                Color.MAGENTA,
                                Color.RED
                        ))
                        .position(0.5, 0.0) // top center
                        .build()
        );
        btnContinue.setEnabled(false);

        btnContinue.postDelayed(() -> {
            btnContinue.setEnabled(true);
        }, 2000);


        // Button
        btnContinue.setOnClickListener(v -> dialog.dismiss());
        dialog.show();
    }

    private void generateChallenge() {
        tvChallenge.setText("💰 Save ₹100 Today");

        tvReward.setText("Reward 🍃 +10 Leaves");

        challengeProgress.setProgress(0);

        tvChallengeStatus.setText("0 / 1 Completed");

//
    }

    private void animateProgress(DonutProgress progressBar, float target) {

        ValueAnimator animator = ValueAnimator.ofFloat(0, target);
        animator.setDuration(1200);
        animator.setInterpolator(new DecelerateInterpolator());

        animator.addUpdateListener(animation -> {
            float value = (float) animation.getAnimatedValue();
            progressBar.setProgress(value);
        });

        animator.start();
    }

    private void startFireAnimation() {

        fireIcon.setVisibility(View.VISIBLE);
        fireGlow.setVisibility(View.VISIBLE);

        fireIcon.animate()
                .scaleX(1.2f)
                .scaleY(1.2f)
                .setDuration(600)
                .withEndAction(() -> fireIcon.animate()
                        .scaleX(1f)
                        .scaleY(1f)
                        .setDuration(600)
                        .start())
                .start();

        fireGlow.animate()
                .alpha(0.3f)
                .setDuration(600)
                .withEndAction(() -> fireGlow.animate()
                        .alpha(1f)
                        .setDuration(600)
                        .withEndAction(this::startFireAnimation)
                        .start())
                .start();
    }
}


