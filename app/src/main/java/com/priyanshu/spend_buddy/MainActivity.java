package com.priyanshu.spend_buddy;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.*;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.*;

public class MainActivity extends AppCompatActivity {

    TextView tvMonth, tvLeft, tvBudget;
    TextView tvTreeLevel, tvTreeProgress, tvLeaves;
    ImageView imgTree;
    ProgressBar treeProgress;
    RecyclerView recyclerView;
    PieChart pieChart;

    List<Expenses> list = new ArrayList<>();
    List<Expenses> fullList = new ArrayList<>();

    ExpenseAdapter adapter;
    FirebaseFirestore db;

    ImageView settingsIcon;
    ImageView leaf1, leaf2, leaf3;

    long finalTotal = 0;
    long budget = 0;

    String userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        FirebaseFirestore db=FirebaseFirestore.getInstance();
        if(db==null){
            Log.e("FIREBASE","DB NULL");
        }else{
            Log.d("FIREBASE","DB OK");
        }

        db.collection("expenses")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (DocumentSnapshot doc : queryDocumentSnapshots) {
                        Log.d("DATA_CHECK", String.valueOf(doc.getData()));
                    }
                })
                .addOnFailureListener(e -> {
                    Log.d("DATA_CHECK", "Error: " + e.getMessage());

                });

        // 🔥 SAFE USER CHECK (IMPORTANT)
        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        Log.d("USER_ID", userId);

        // Bind
        tvMonth = findViewById(R.id.tvMonth);
        tvLeft = findViewById(R.id.tvLeft);
        tvBudget = findViewById(R.id.tvBudget);
        tvTreeLevel = findViewById(R.id.tvTreeLevel);
        tvTreeProgress = findViewById(R.id.tvTreeProgress);
        tvLeaves = findViewById(R.id.tvLeaves);
        leaf1 = findViewById(R.id.leaf1);
        leaf2 = findViewById(R.id.leaf2);
        leaf3 = findViewById(R.id.leaf3);

        imgTree = findViewById(R.id.imgTree);
        treeProgress = findViewById(R.id.treeProgress);
        recyclerView = findViewById(R.id.recyclerView);
        pieChart = findViewById(R.id.pieChart);
        settingsIcon = findViewById(R.id.settingsIcon);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new ExpenseAdapter(list);
        recyclerView.setAdapter(adapter);

        db = FirebaseFirestore.getInstance();

        settingsIcon.setOnClickListener(v -> {

            Intent intent = new Intent(MainActivity.this,
                    SettingsActivity.class);

            startActivity(intent);

        });

        // 🔥 FAB
        FloatingActionButton fab = findViewById(R.id.fabMain);
        fab.setOnClickListener(v -> showOptions());

        // 🔻 Bottom Nav
        BottomNavigationView bottomNav = findViewById(R.id.bottomNav);
        bottomNav.setOnItemSelectedListener(item -> {

            if (item.getItemId() == R.id.nav_home) return true;

            if (item.getItemId() == R.id.nav_insights) {
                startActivity(new Intent(this, InsightsActivity.class));
                return true;
            }

            if (item.getItemId() == R.id.nav_goal) {
                startActivity(new Intent(this, SavingGoalActivity.class));
                return true;
            }

            if (item.getItemId() == R.id.nav_stats) {
                startActivity(new Intent(this, StatsActivity.class));
                return true;
            }

            return false;
        });

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {

            requestPermissions(
                    new String[]{
                            android.Manifest.permission.RECEIVE_SMS,
                            android.Manifest.permission.READ_SMS
                    },
                    101
            );
        }

        if (checkSelfPermission(android.Manifest.permission.RECEIVE_SMS)
                != PackageManager.PERMISSION_GRANTED) {

            requestPermissions(
                    new String[]{
                            android.Manifest.permission.RECEIVE_SMS,
                            android.Manifest.permission.READ_SMS
                    },
                    100
            );
        }

        startLeafAnimation();

    }

    // 🔥 Bottom Sheet
    private void showOptions() {
        BottomSheetDialog dialog = new BottomSheetDialog(this);
        View view = getLayoutInflater().inflate(R.layout.bottom_sheet_options, null);

        dialog.setContentView(view);
        dialog.show();

        view.findViewById(R.id.btnAdd).setOnClickListener(v -> {
            startActivity(new Intent(this, AddExpenseActivity.class));
            dialog.dismiss();
        });

        view.findViewById(R.id.btnBudget).setOnClickListener(v -> {
            startActivity(new Intent(this, SetBudgetActivity.class));
            dialog.dismiss();
        });

        view.findViewById(R.id.btnRecurring).setOnClickListener(v -> {
            startActivity(new Intent(this, RecurringActivity.class));
            dialog.dismiss();
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(db == null){
            db=FirebaseFirestore.getInstance();
        }
        loadData();
    }

    // 🔥 LOAD USER DATA
    private void loadData() {

        if (db == null) {
            db = FirebaseFirestore.getInstance();
        }

        if (userId == null) {
            Log.e("ERROR", "UserId NULL ❌");
            return;
        }

        checkRecurringExpenses();

        db.collection("users")
                .document(userId)
                .collection("expenses").orderBy("date", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(query -> {

                    Log.d("FIREBASE_TEST", "Docs count: " + query.size());
                    Log.d("List_SIZE", "Size: " + list.size());

                    list.clear();
                    fullList.clear();

                    long total = 0;
                    Map<String, Integer> categoryMap = new HashMap<>();

                    for (DocumentSnapshot doc : query) {

                        Log.d("FIREBASE_DATA", String.valueOf(doc.getData()));
                        Log.d("RAW_DATA", String.valueOf(doc.getData()));

                        String category = doc.getString("category");
                        Long amountObj = doc.getLong("amount");
                        Long dateObj = doc.getLong("date");

                        String title = doc.getString("title");
                        String merchant = doc.getString("merchant");
                        String bank = doc.getString("bank");

                        if (title == null || title.isEmpty())
                            title = category;

                        if (merchant == null)
                            merchant = "";

                        if (bank == null)
                            bank = "";

                        if (category == null) category = "Other";
                        if (amountObj == null) continue;

                        long amount = amountObj;
                        long date = (dateObj != null) ? dateObj : 0;

                        Expenses e = new Expenses(
                                doc.getId(),
                                title,
                                category,
                                merchant,
                                bank,
                                amount,
                                date
                        );
                        e.id=doc.getId();
                        list.add(e);
                        fullList.add(e);

                        int amt = (int) amount;
                        total += amt;

                        if (categoryMap.containsKey(e.category)) {
                            categoryMap.put(e.category,
                                    categoryMap.get(e.category) + amt);
                        } else {
                            categoryMap.put(e.category, amt);
                        }
                    }

                    finalTotal = total;

                    // 🔴 EMPTY DATA FIX
                    if (list.isEmpty()) {

                        Log.d("FIREBASE_TEST", "No data found ❌");

                        tvMonth.setText("₹0");
                        tvLeft.setText("₹0");
                        tvBudget.setText("₹0");

                        pieChart.clear();
                        pieChart.setDrawHoleEnabled(true);
                        pieChart.setCenterText("₹0\nTotal");
                        pieChart.setCenterTextSize(20f);
                        pieChart.setCenterTextColor(Color.WHITE);
                        pieChart.invalidate();

                        adapter.notifyDataSetChanged();
                        return;
                    }

                    // ✅ NORMAL FLOW
                    setupChart(categoryMap, total);

                    db.collection("users")
                            .document(userId)
                            .collection("budget")
                            .document("monthly")
                            .get()
                            .addOnSuccessListener(doc -> {

                                if (doc.exists()) {

                                    Long b = doc.getLong("amount");
                                    budget = (b != null) ? b : 0;

                                    long remaining = budget - finalTotal;

                                    tvMonth.setText("₹" + finalTotal);
                                    tvLeft.setText("₹" + remaining);
                                    tvBudget.setText("₹" + budget);

                                    if (finalTotal >= budget) {
                                        tvLeft.setTextColor(Color.RED);
                                    } else if (finalTotal >= 0.8 * budget) {
                                        tvLeft.setTextColor(Color.YELLOW);
                                    } else {
                                        tvLeft.setTextColor(Color.WHITE);
                                    }
                                }
                            });
                    loadMoneyTree();
                    adapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> {
                    Log.e("FIREBASE_ERROR", e.getMessage());
                });
    }

    private void loadMoneyTree() {

        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        db.collection("users")
                .document(userId).collection("saving_goal")
                .document("current")
                .get()
                .addOnSuccessListener(doc -> {

                    if (!doc.exists()) return;

                    Long saved = doc.getLong("savedAmount");
                    Long target = doc.getLong("targetAmount");

                    long savedAmount = (saved != null) ? saved : 0;
                    long targetAmount = (target != null) ? target : 5000;

                    // Progress
                    tvTreeProgress.setText("₹" + savedAmount + " / ₹" + targetAmount);

                    db.collection("users")
                            .document(userId)
                            .get()
                            .addOnSuccessListener(userDoc -> {

                                Long leaves = userDoc.getLong("leaves");

                                if (leaves == null)
                                    leaves = 0L;

                                tvLeaves.setText("🍃 " + leaves + " Leaves");

                            });

                    int percent = 0;

                    if (targetAmount > 0) {
                        percent = (int) ((savedAmount * 100) / targetAmount);
                    }

                    treeProgress.setProgress(percent);

                    // 🌳 Tree Levels

                    if (savedAmount < 500) {

                        tvTreeLevel.setText("🌱 Seed");
                        imgTree.setImageResource(R.drawable.tree_seed);
                        animateTreeGrowth();

                    }

                    else if (savedAmount < 2000) {

                        tvTreeLevel.setText("🌿 Sapling");
                        imgTree.setImageResource(R.drawable.tree_sapling);
                        animateTreeGrowth();

                    }

                    else if (savedAmount < 5000) {

                        tvTreeLevel.setText("🌳 Young Tree");
                        imgTree.setImageResource(R.drawable.tree_young);
                        animateTreeGrowth();

                    }

                    else if (savedAmount < 10000) {

                        tvTreeLevel.setText("🌲 Mature Tree");
                        imgTree.setImageResource(R.drawable.tree_mature);
                        animateTreeGrowth();

                    }

                    else {

                        tvTreeLevel.setText("✨ Golden Tree");
                        imgTree.setImageResource(R.drawable.tree_golden);
                        animateTreeGrowth();

                    }

                });

    }
    
    private void animateTreeGrowth() {

        ObjectAnimator scaleX =
                ObjectAnimator.ofFloat(imgTree,
                        "scaleX",
                        0.85f,
                        1.15f,
                        1f);

        ObjectAnimator scaleY =
                ObjectAnimator.ofFloat(imgTree,
                        "scaleY",
                        0.85f,
                        1.15f,
                        1f);

        ObjectAnimator alpha =
                ObjectAnimator.ofFloat(imgTree,
                        "alpha",
                        0.7f,
                        1f);

        AnimatorSet set = new AnimatorSet();

        set.playTogether(scaleX, scaleY, alpha);

        set.setDuration(700);

        set.start();
    }
    private void startLeafAnimation() {

        animateLeaf(leaf1, 0);

        animateLeaf(leaf2, 400);

        animateLeaf(leaf3, 800);

    }

    private void animateLeaf(View leaf, long delay) {

        leaf.setAlpha(0.5f);

        leaf.animate()

                .translationYBy(-25f)

                .translationXBy(10f)

                .rotationBy(18f)

                .alpha(1f)

                .setStartDelay(delay)

                .setDuration(2200)

                .withEndAction(() -> {

                    leaf.setTranslationY(0);

                    leaf.setTranslationX(0);

                    leaf.setRotation(0);

                    animateLeaf(leaf,0);

                })

                .start();

    }

    // 🔥 CHART FIX FINAL
    private void setupChart(Map<String, Integer> categoryMap, long total) {

        ArrayList<PieEntry> entries = new ArrayList<>();

        for (String key : categoryMap.keySet()) {
            entries.add(new PieEntry(categoryMap.get(key), key));
        }

        PieDataSet dataSet = new PieDataSet(entries, "");

        dataSet.setColors(
                Color.parseColor("#00E676"),
                Color.parseColor("#38BDF8"),
                Color.parseColor("#FACC15"),
                Color.parseColor("#FB7185"),
                Color.parseColor("#A78BFA")
        );

        dataSet.setSliceSpace(3f);
        dataSet.setSelectionShift(8f);
        dataSet.setColors(ColorTemplate.MATERIAL_COLORS);
        dataSet.setSliceSpace(2f);

        PieData data = new PieData(dataSet);
        data.setValueTextColor(Color.WHITE);
        data.setValueTextSize(12f);
        data.setDrawValues(false);

        data.setValueFormatter(new ValueFormatter() {
            @Override
            public String getPieLabel(float value, PieEntry pieEntry) {
                return "₹" + (int) value;
            }
        });

        pieChart.clear();
        pieChart.setData(data);

        pieChart.setDrawHoleEnabled(true);
        pieChart.setHoleRadius(60f);
        pieChart.setTransparentCircleRadius(0f);

        // 🔥 CENTER TEXT FIX (FINAL)
        pieChart.setCenterText("₹" + total + "\nTotal");
        pieChart.setCenterTextSize(20f);
        pieChart.setCenterTextColor(Color.BLUE);
        pieChart.setEntryLabelColor(Color.WHITE);
        pieChart.setEntryLabelTextSize(12f);

        pieChart.getDescription().setEnabled(false);
        pieChart.getLegend().setEnabled(false);


        pieChart.animateY(1000);
        pieChart.invalidate();

        // 🔥 FILTER + CENTER CHANGE
        pieChart.setOnChartValueSelectedListener(new OnChartValueSelectedListener() {

            @Override
            public void onValueSelected(Entry e, Highlight h) {

                PieEntry entry = (PieEntry) e;

                String category = entry.getLabel();
                float amount = entry.getValue();

                pieChart.setCenterText("₹" + (int) amount + "\n" + category);

                List<Expenses> filtered = new ArrayList<>();

                for (Expenses exp : fullList) {
                    if (exp.category != null &&
                            exp.category.equalsIgnoreCase(category)) {
                        filtered.add(exp);
                    }
                }

                list.clear();
                list.addAll(filtered);
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onNothingSelected() {

                pieChart.setCenterText("₹" + finalTotal + "\nTotal");

                list.clear();
                list.addAll(fullList);
                adapter.notifyDataSetChanged();
            }
        });
    }

    private void checkRecurringExpenses() {

        int today = Calendar.getInstance().get(Calendar.DAY_OF_MONTH);
        if(db == null){
            Log.e("ERROR", "DB IS NULL IN recurring");
            return;
        }

        db.collection("users")
                .document(userId)
                .collection("recurring")
                .get()
                .addOnSuccessListener(query -> {

                    for (DocumentSnapshot doc : query) {

                        Long dayObj = doc.getLong("day");
                        if (dayObj == null) continue;

                        int day = dayObj.intValue();

                        if (day == today) {

                            long now = System.currentTimeMillis();

                            String category = doc.getString("category");
                            Long amountObj = doc.getLong("amount");

                            if (amountObj == null) continue;

                            long amount = amountObj;

                            Map<String, Object> expense = new HashMap<>();
                            expense.put("title", category);
                            expense.put("amount", amount);
                            expense.put("category", category);
                            expense.put("date", now);

                            // 🔥 DUPLICATE CHECK (IMPORTANT)
                            db.collection("users")
                                    .document(userId)
                                    .collection("expenses")
                                    .whereEqualTo("category", category)
                                    .get()
                                    .addOnSuccessListener(expenses -> {

                                        boolean alreadyAdded = false;

                                        for (DocumentSnapshot e : expenses) {

                                            Long existingDate = e.getLong("date");

                                            if (existingDate != null) {
                                                long diff = now - existingDate;

                                                // 24 hours check
                                                if (diff < 24 * 60 * 60 * 1000) {
                                                    alreadyAdded = true;
                                                    break;
                                                }
                                            }
                                        }

                                        if (!alreadyAdded) {

                                            db.collection("users")
                                                    .document(userId)
                                                    .collection("expenses")
                                                    .add(expense);
                                        }
                                    });
                        }
                    }
                });
    }
}